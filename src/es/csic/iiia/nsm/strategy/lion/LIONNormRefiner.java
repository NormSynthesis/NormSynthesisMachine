/**
 * 
 * @author Javier Morales (jmorales@iiia.csic.es)
 */
package es.csic.iiia.nsm.strategy.lion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import es.csic.iiia.nsm.NormSynthesisMachine;
import es.csic.iiia.nsm.NormSynthesisMachine.NormGeneralisationMode;
import es.csic.iiia.nsm.agent.AgentAction;
import es.csic.iiia.nsm.agent.language.PredicatesDomains;
import es.csic.iiia.nsm.config.Dimension;
import es.csic.iiia.nsm.config.DomainFunctions;
import es.csic.iiia.nsm.config.NormSynthesisSettings;
import es.csic.iiia.nsm.net.norm.NetworkNodeState;
import es.csic.iiia.nsm.net.norm.NormativeNetwork;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.NormModality;
import es.csic.iiia.nsm.norm.NormativeSystem;
import es.csic.iiia.nsm.norm.evaluation.NormsApplicableInView;
import es.csic.iiia.nsm.norm.group.NormGroupCombination;
import es.csic.iiia.nsm.norm.group.net.NormGroupNetwork;
import es.csic.iiia.nsm.norm.reasoning.NormReasoner;
import es.csic.iiia.nsm.norm.refinement.lion.NormAttribute;
import es.csic.iiia.nsm.norm.refinement.simon.GeneralisableNorms;
import es.csic.iiia.nsm.norm.refinement.simon.NormIntersection;
import es.csic.iiia.nsm.perception.ViewTransition;

/**
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 *
 */
public class LIONNormRefiner {

	protected Random random;
	
	protected List<Dimension> normEvDimensions;
	protected NormSynthesisSettings nsmSettings;

	protected LIONNormClassifier normClassifier;
	protected DomainFunctions dmFunctions;
	protected PredicatesDomains predDomains;
	protected NormReasoner normReasoner;
	protected NormativeNetwork normativeNetwork;
	protected NormGroupNetwork normGroupNetwork;

	protected Map<Norm, List<NormAttribute>> normClassifications;

	protected LIONUtilityFunction utilityFunction;	
	protected LIONOperators operators;
	protected NormGeneralisationMode genMode; 
	protected int genStep;

	protected Map<String, NormIntersection> normIntersections;
	protected Map<NormGroupCombination, Integer> potentialComplementaryPairs;

	/**
	 * 
	 * @param normativeNetwork
	 */
	public LIONNormRefiner(List<Dimension> normEvDimensions,
			NormSynthesisSettings nsmSettings, DomainFunctions dmFunctions,
			PredicatesDomains predDomains, NormativeNetwork normativeNetwork,
			NormGroupNetwork normGroupNetwork, NormReasoner normReasoner,
			LIONOperators operators, NormGeneralisationMode genMode, int genStep,
			Random random) {

		this.random = random;
		this.normEvDimensions = normEvDimensions;
		this.nsmSettings = nsmSettings;
		this.dmFunctions = dmFunctions;
		this.predDomains = predDomains;
		this.normativeNetwork = normativeNetwork;
		this.normGroupNetwork = normGroupNetwork;
		this.normReasoner = normReasoner;
		this.operators = operators;
		this.genMode = genMode;
		this.genStep = genStep;

		this.potentialComplementaryPairs = new HashMap<NormGroupCombination,Integer>();
		this.normIntersections = new HashMap<String, NormIntersection>();
		this.normClassifications = new HashMap<Norm, List<NormAttribute>>();
		this.normClassifier = new LIONNormClassifier(normEvDimensions,
				nsmSettings,normativeNetwork, normGroupNetwork, operators);
	}

	/**
	 * 
	 * @param normClassifications
	 */
	public void step(Map<ViewTransition, NormsApplicableInView> normApplicability,
			List<Norm> normsActivatedDuringGeneration) {
		
		List<Norm> processed = new ArrayList<Norm>();
		List<Norm> visited = new ArrayList<Norm>();

		/* 1. First, deactivate all those norms that have a substitutability 
		 * relationship with those norms that have been activated during 
		 * the norm generation phase */
		for(Norm norm : normsActivatedDuringGeneration) {
			List<Norm> substitutable = normativeNetwork.getSubstitutableNorms(norm);
			if(!substitutable.isEmpty()) {
				this.normativeNetwork.addAttribute(norm, NormAttribute.SUBSTITUTER);
				System.out.println("Reactivate substitutable (maybe it's complementary... " + norm);
			}
			
			for(Norm toSubstitute : substitutable) {
				System.out.println("Deactivates substitutable " + toSubstitute);
				System.out.println("-----------------------------------------------------------------");
				
				this.normativeNetwork.removeAttribute(toSubstitute, NormAttribute.SUBSTITUTER);
				this.specialiseDown(toSubstitute, NetworkNodeState.SUBSTITUTED, visited);
				
				NormGroupCombination nGrComb = 
						this.normGroupNetwork.getNormGroupCombination(norm, toSubstitute);
				
				if(!this.potentialComplementaryPairs.containsKey(nGrComb)) {
					this.potentialComplementaryPairs.put(nGrComb, 0);
				}
				int numReactivations = this.potentialComplementaryPairs.get(nGrComb);
				this.potentialComplementaryPairs.put(nGrComb, numReactivations+1);
				
				/* Cycle detection. If the pair of norms have substituted one another
				 * 5 times or more, then remove the substitutability relationship
				 * (due to a possible false positive) */
				if(numReactivations > 5) {
					this.normativeNetwork.removeSubstitutability(norm, toSubstitute);
				}
			}
		}
		visited.clear();
		
		/* Compute norms that must be revised */
		List<Norm> normsToRevise = this.checkNormsToRevise(normApplicability);

		/* Classify norms */
		this.normClassifications = this.normClassifier.step(normsToRevise);

		/* Refine norms based on norm classifications */
		for(Norm norm : normClassifications.keySet()) {
			if(processed.contains(norm)) {
				continue;
			}
			List<NormAttribute> attributes = normClassifications.get(norm);

			boolean isEffective = attributes.contains(NormAttribute.EFFECTIVE);
			boolean isNecessary = attributes.contains(NormAttribute.NECESSARY);
			boolean isIneffective = attributes.contains(NormAttribute.INEFFECTIVE);
			boolean isUnnecessary = attributes.contains(NormAttribute.UNNECESSARY);

			/* If the norm is whether ineffective or unnecessary, then deactivate
			 * it (specialise it into its children) */
			if(isIneffective || isUnnecessary) {
				visited.clear();
				specialiseDown(norm, NetworkNodeState.DISCARDED, visited);
			}

			if(isEffective && isNecessary &&
					this.normativeNetwork.isLeaf(norm) &&
					!this.normativeNetwork.isRepresented(norm)) {
				this.operators.activate(norm);
			}

			/* If the norm has enough utility to be generalised, 
			 * then try to generalise it */
			boolean isGeneralisable = attributes.contains(NormAttribute.GENERALISABLE);
			if(isGeneralisable) {
				generaliseUp(norm, genMode, genStep);
			}
			
			/* If the norm is substitutable, retrieve the norm it is 
			 * substitutable with and choose one of them to be specialised */
			boolean isSubstitutable = attributes.contains(NormAttribute.SUBSTITUTABLE);
			if(isSubstitutable) {
				Norm norm2 = this.normClassifier.getSubstitutableNorm(norm);
				Norm normToSubstitute = this.chooseNormToSubstitute(norm, norm2);
				Norm substituter;
				if(norm.equals(normToSubstitute)) {
					substituter = norm2;
				}
				else {
					substituter = norm;
				}

				System.out.println("Substituting norm " + normToSubstitute);
				System.out.println("Substituter " + substituter);
				System.out.println("-----------------------------------------------------------");
				
				/* Specialise norm to substitute */
				visited.clear();
				specialiseDown(normToSubstitute, NetworkNodeState.SUBSTITUTED, visited);

				processed.add(norm);
				processed.add(norm2);
				
				/* Mark substituter */
				this.normativeNetwork.addAttribute(substituter, NormAttribute.SUBSTITUTER);
			}
		}
	}

	/**
	 * Recursively specialises a norm into its children, its children
	 * into their children, and so on
	 * 
	 * @param norm the norm to specialise
	 */
	protected void specialiseDown(Norm norm, NetworkNodeState specState, List<Norm> visited) {
		if(visited.contains(norm)) {
			return;
		}
		visited.add(norm);

		/* Specialise down all parent norms */
		List<Norm> parents = this.normativeNetwork.getParents(norm);
		for(Norm parent : parents) {
			this.specialiseDown(parent, specState, visited);
		}

		/* Codigo nuevo: Cuando una norma hoja va mal, entonces se especializan todos
		 * los padres que la generalizan. De esta manera, una norma general solo 
		 * se desactiva si tiene alguna hija que va mal
		 */
		List<Norm> children = this.normativeNetwork.getChildren(norm);

		/* Only specialise norms that are represented by
		 * an active norm, whether itself or a parent norm */
		if(this.normativeNetwork.isRepresented(norm)) {
			List<Norm> childrenToActivate = new ArrayList<Norm>(children);
			operators.specialise(norm, specState, childrenToActivate);
		}
	}

	/**
	 * Tries to generalise up a norm {@code normA} together with other norms in
	 * the normative system. Notice that, unlike in the case of IRON, SIMON does
	 * not compare a norm with all the other norms in the normative network, but
	 * only to those of the normative system. The SIMON norm generalisation has
	 * two working modes:
	 * <ol>
	 * <li> <b>shallow</b> generalisation, which performs norm generalisations
	 * 			by comparing the preconditions of norms in a similar way to that of
	 * 			IRON; and 
	 * <li> <b>deep</b> generalisation, which performs a deeper search by 
	 * 			computing the <b>intersection</b> of terms in the predicates of
	 * 			norms' preconditions.
	 * </ol>
	 * 
	 * Furthermore, the SIMON generalisation requires a generalisation step
	 * {@code genStep}, which describes how many predicates/terms can be
	 * generalised at the same time. As an example, consider two norms with 3
	 * predicates each one of them, and each predicate with 1 term. Consider now
	 * that 2 of the 3 terms in both norms intersect, but the third predicate in
	 * both norms are different and have a common subsumer term (a potential
	 * generalisation). If the generalisation step is genStep=1, then we may
	 * generalise these two norms, since it allows to generalise 1 predicate/term
	 * at the same time. However, if 1 of the 3 terms intersect, and hence 2
	 * terms are different, then we cannot generalise them, since the
	 * generalisation step {@code genStep} does not allow to generalise two
	 * predicates/terms at the same time.
	 * 
	 * @param normA the norm to generalise
	 * @param genMode the SIMON generalisation mode (Shallow/Deep)
	 * @param genStep the generalisation step
	 */
	protected void generaliseUp(Norm normA,
			NormGeneralisationMode genMode, int genStep) {


		/* Get all the active norms (the normative system) */
		NormativeSystem NS = (NormativeSystem)
				normativeNetwork.getNormativeSystem().clone();

		/* Compute matches with each norm */
		for(Norm normB : NS) {
			boolean generalise = true;

			/* Never generalise the norm with itself */
			if(normA.equals(normB)) {
				continue;
			}

			/* 1. Get generalisable norms */
			GeneralisableNorms genNorms = this.
					getGeneralisableNorms(normA, normB, genStep);

			/* If there is no possible generalisation, do nothing */
			if(genNorms == null) {
				continue;
			}

			/* 2. If there is a possible generalisation, then get generalisable
			 * norms and the parent norm */
			Norm n1 = genNorms.getNormA();
			Norm n2 = genNorms.getNormB();
			Norm parent = genNorms.getParent();

			if(n1 == null || n2 == null || parent == null) {
				continue;
			}

			/* 3. Check that the parent norm does not contain an inactive norm */
			for(Norm inactive: this.normativeNetwork.getNotRepresentedNorms()){
				if(this.normReasoner.satisfies(inactive, parent) && 
						!inactive.equals(parent))	{
					generalise = false;
					break;
				}
			}

			/* 4. Do not generalise if any of the generalisable norms or the parent
			 * norm are not consistent with the given domain they have been 
			 * generated for */
			for(Norm norm : genNorms.getAllNorms()) {
				if(!this.dmFunctions.isConsistent(norm.getPrecondition())) {
					generalise = false;
					break;
				}
			}

			/* Do not generalise either if the parent norm is already created
			 * and active (that is, the same parent norm has been created in a 
			 * previous generalisation with other two norms) */
			if(this.normativeNetwork.contains(parent) && 
//					(this.normativeNetwork.getState(parent) == NetworkNodeState.ACTIVE ||
//					this.normativeNetwork.getState(parent) == NetworkNodeState.REPRESENTED)) { 
				this.normativeNetwork.isRepresented(parent)) {
				
				// TODO: Cambiar logica con estados active / represented
				generalise = false;
			}

			/* 5. Perform the norm generalisation if all the previous tests
			 * have been passed */
			if(generalise) {
				List<Norm> allNorms = new ArrayList<Norm>();
				allNorms.add(n1);
				allNorms.add(n2);
				allNorms.add(parent);

				for(Norm norm : allNorms) {

					/* If the normative network contains the norm, retrieve it
					 * and save it in the provisional norms list */
					if(this.normativeNetwork.contains(norm)) {
						int idx = allNorms.indexOf(norm);
						norm = this.normativeNetwork.getNorm(norm.getPrecondition(),
								norm.getModality(), norm.getAction());
						allNorms.set(idx, norm);
					}

					/* Else, add the norm to the normative network */
					else {
						this.operators.add(norm);
					}
				}

				n1 = allNorms.get(0);
				n2 = allNorms.get(1);
				parent = allNorms.get(2);

				/* Activate parent norm */
				if(!this.normativeNetwork.isRepresented(parent)) {
//				if(this.normativeNetwork.getState(parent) != NetworkNodeState.ACTIVE &&
//						this.normativeNetwork.getState(parent) != NetworkNodeState.REPRESENTED) {
					
					this.operators.activate(parent);
					this.normativeNetwork.getUtility(parent).reset();
				}

				/* Perform the norm generalisation */
				this.operators.generalise(n1, parent);
				this.operators.generalise(n2, parent);

				/* Deactivate children */
				for(Norm child : this.normativeNetwork.getChildren(parent)) {
					this.operators.deactivate(child, NetworkNodeState.REPRESENTED); // TODO: Cambiado
				}
			}
		}
	}


	/**
	 * Given two norms Returns the generalisable norms {@code normA} and
	 * {@code normB}, it performs the following operations:
	 * <ol>
	 * <li> it computes the intersection between norms A and B;
	 * <li> if the predicates of both norms are equal but K={@code genStep}
	 * 			predicates, then it computes their generalisable norms. Otherwise,
	 * 			it return a <tt>null</tt> {@code GeneralisableNorms}
	 * </ol> 
	 * 
	 * @param normA the first norm 
	 * @param normB the second norm
	 * @param genStep the generalisation step
	 * @return the generalisable norms A and B, and their potential parent
	 */
	protected GeneralisableNorms getGeneralisableNorms(
			Norm normA, Norm normB, int genStep) {

		NormModality normModality = normA.getModality();
		NormModality otherNormModality = normB.getModality();
		AgentAction action = normA.getAction();
		AgentAction otherAction = normB.getAction();
		GeneralisableNorms genNorms = null;
		NormIntersection intersection;

		/* Check that post-conditions are the same*/
		if(normModality != otherNormModality || action != otherAction) {
			return genNorms;
		}

		/* Get the intersection between both norm preconditions */
		String desc = NormIntersection.getDescription(normA, normB);

		if(!this.normIntersections.containsKey(desc)) {
			intersection = new NormIntersection(normA, normB, 
					this.predDomains, genMode);
			this.normIntersections.put(desc, intersection);
		}
		intersection = this.normIntersections.get(desc);

		/* If both norms have all their predicates in common but K predicates,
		 * then generalise both norms are generalisable */
		if(intersection.getDifferenceCardinality() > 0 && 
				intersection.getDifferenceCardinality() <= genStep) {

			genNorms = new GeneralisableNorms(intersection,
					this.predDomains, genMode);
		}		
		return genNorms;
	}

	/**
	 * 
	 * @param nA
	 * @param nB
	 */
	protected Norm chooseNormToSubstitute(Norm nA, Norm nB) {

		/* 1. Preserve the norm that is complementary with a third norm */
		boolean nAIsComplementary = this.normativeNetwork.isComplementary(nA);
		boolean nBIsComplementary = this.normativeNetwork.isComplementary(nB);

		if(nAIsComplementary && !nBIsComplementary) {
			return nB;
		}
		if(nBIsComplementary &&!nAIsComplementary) {
			return nA;
		}
//		String nAPrecond = nA.getPrecondition().toString();
//		String nBPrecond = nB.getPrecondition().toString();
//
//		if(nAPrecond.equals("l(>)&f(>)&r(>)") || nAPrecond.equals("l(<)&f(<)&r(<)") ||
//				nAPrecond.equals("l(>)&f(>)&r(-)") || nAPrecond.equals("l(-)&f(<)&r(<)")) {
//			return nA; 
//		}
//		if(nBPrecond.equals("l(>)&f(>)&r(>)") || nBPrecond.equals("l(<)&f(<)&r(<)") ||
//				nBPrecond.equals("l(>)&f(>)&r(-)") || nBPrecond.equals("l(-)&f(<)&r(<)")) {
//			return nB;
//		}

		/* 2. Preserve the norm that is substituting a third norm */
		if(this.normativeNetwork.isSubstituter(nA) &&
				!this.normativeNetwork.isSubstituter(nB)) {
			return nB;
		}
		else if(this.normativeNetwork.isSubstituter(nB) &&
				!this.normativeNetwork.isSubstituter(nA)) {
			return nA;
		}
		
		/* 4. In case of a draw, promote the norm with the lowest
		 * substitutability index (that with the lowest number of brother
		 * norms that have been substituted */
		double nASubsIndex = this.computeSubstitutabilityIndex(nA);
		double nBSubsIndex = this.computeSubstitutabilityIndex(nB);

		
		if(nASubsIndex != nBSubsIndex) {
			System.out.println("Subindex " + nA + ": " + nASubsIndex);
			System.out.println("Subindex " + nB + ": " + nBSubsIndex);
			return (nASubsIndex > nBSubsIndex ? nA : nB);
		}
		
		/* 3. Compute the highest generalisation level in the generalisation tree
		 * of each norm. Preserve the norm in the most generalised sub-tree */
		double nAGenIndex = this.computeGeneralisationIndex(nA);
		double nBGenIndex = this.computeGeneralisationIndex(nB);

		if(nAGenIndex != nBGenIndex) {
			System.out.println("GenIndex " + nA + ": " + nAGenIndex);
			System.out.println("Genindex " + nB + ": " + nBGenIndex);
			return (nAGenIndex > nBGenIndex ? nB : nA);
		}
		
		/* 5. Randomly choose one of the norms */
		if(random.nextBoolean()) {
			return nA;
		}
		return nB;
	}

	/**
	 * 
	 * @return
	 */
	private double computeGeneralisationIndex(Norm norm) {
		List<Norm> visited = new ArrayList<Norm>();
		return this.computeGeneralisationDegree(norm, visited);
	}

	/**
	 * 
	 * @return
	 */
	private double computeGeneralisationDegree(Norm norm, List<Norm> visited) {

		/* Check that the norm is represented and not visited before */
		if(!this.normativeNetwork.isRepresented(norm) || visited.contains(norm)) {
//		if((this.normativeNetwork.getState(norm) != NetworkNodeState.ACTIVE && 
//				this.normativeNetwork.getState(norm) != NetworkNodeState.REPRESENTED) ||
//				visited.contains(norm)) {
			
			return 0; // TODO: Cambiar logica con estados active / represented
		}
		visited.add(norm);

		List<Norm> parents = this.normativeNetwork.getParents(norm);
		List<Norm> children = this.normativeNetwork.getChildren(norm);
		int genLevel = this.normativeNetwork.getGeneralisationLevel(norm);
		int numChildren = children.size();

		double genDegree = numChildren * Math.pow(10, genLevel);

		/* Explore in height (parents) */
		for(Norm parent : parents) {
			double parentGenDegree = this.computeGeneralisationDegree(parent, visited);
			genDegree += parentGenDegree;
		}
		return genDegree;
	}

	/**
	 * 
	 * @return
	 */
	private double computeSubstitutabilityIndex(Norm norm) {
		List<Norm> visited = new ArrayList<Norm>();
		return this.computeSubstitutabilityDegree(norm, visited, 0);
	}

	/**
	 * 
	 * @return
	 */
	private double computeSubstitutabilityDegree(Norm norm,
			List<Norm> visited,	int distance) {

		/* Check that the norm is represented and not visited before */
		if(!this.normativeNetwork.isRepresented(norm) || visited.contains(norm)) {
			return 0;
		}
		visited.add(norm);
		List<Norm> parents = this.normativeNetwork.getParents(norm);
		List<Norm> brothers = this.normativeNetwork.getBrothers(norm);
		
		double subsDegree = 0.0;
		for(Norm brother : brothers) {
			if(normativeNetwork.getState(brother) == NetworkNodeState.SUBSTITUTED) {
				subsDegree += Math.pow(10, distance * -1);
			}
		}

		/* Explore in height (parents) */
		for(Norm parent : parents) {
			double parentSubsDegree = this.computeSubstitutabilityDegree(parent,
					visited, distance+1);
			subsDegree += parentSubsDegree;
		}
		return subsDegree;
	}

	/**
	 * 
	 */
	private List<Norm> checkNormsToRevise(
			Map<ViewTransition, NormsApplicableInView> normApplicability) {

		List<Norm> normsToRevise = new ArrayList<Norm>();

		for(NormsApplicableInView vna : normApplicability.values()) {
			for(long id : vna.getAgentIds()) {
				for(Norm norm : vna.get(id).getApplicableNorms()) {
					if(!normsToRevise.contains(norm)) {
						normsToRevise.add(norm);
					}
				}
			}
		}
		return normsToRevise;
	}
}
