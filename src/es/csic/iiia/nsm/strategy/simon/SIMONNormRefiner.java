/**
 * 
 * @author Javier Morales (jmorales@iiia.csic.es)
 */
package es.csic.iiia.nsm.strategy.simon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.csic.iiia.nsm.NormSynthesisMachine.NormGeneralisationMode;
import es.csic.iiia.nsm.agent.EnvironmentAgentAction;
import es.csic.iiia.nsm.agent.language.PredicatesDomains;
import es.csic.iiia.nsm.config.Dimension;
import es.csic.iiia.nsm.config.DomainFunctions;
import es.csic.iiia.nsm.config.NormSynthesisSettings;
import es.csic.iiia.nsm.metrics.NormSynthesisMetrics;
import es.csic.iiia.nsm.net.norm.NetworkNodeState;
import es.csic.iiia.nsm.net.norm.NormativeNetwork;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.NormModality;
import es.csic.iiia.nsm.norm.NormativeSystem;
import es.csic.iiia.nsm.norm.evaluation.NormsApplicableInView;
import es.csic.iiia.nsm.norm.reasoning.NormReasoner;
import es.csic.iiia.nsm.norm.refinement.lion.NormAttribute;
import es.csic.iiia.nsm.norm.refinement.simon.GeneralisableNorms;
import es.csic.iiia.nsm.norm.refinement.simon.NormIntersection;
import es.csic.iiia.nsm.perception.ViewTransition;

/**
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 *
 */
public class SIMONNormRefiner {

	protected List<Dimension> normEvDimensions;
	protected NormSynthesisSettings nsmSettings;

	protected SIMONNormClassifier normClassifier;
	protected DomainFunctions dmFunctions;
	protected PredicatesDomains predDomains;
	protected NormReasoner normReasoner;
	protected NormativeNetwork normativeNetwork;
	protected NormSynthesisMetrics nsMetrics;
	protected Map<Norm, List<NormAttribute>> normClassifications;

	protected SIMONUtilityFunction utilityFunction;	
	protected SIMONOperators operators;
	protected NormGeneralisationMode genMode; 
	protected int genStep;

	protected Map<String, NormIntersection> normIntersections;

	/**
	 * 
	 * @param normativeNetwork
	 */
	public SIMONNormRefiner(List<Dimension> normEvDimensions,
			NormSynthesisSettings nsmSettings, DomainFunctions dmFunctions,
			PredicatesDomains predDomains, NormativeNetwork normativeNetwork,
			NormReasoner normReasoner, NormSynthesisMetrics nsMetrics,
			SIMONOperators operators, NormGeneralisationMode genMode, int genStep) {

		this.normEvDimensions = normEvDimensions;
		this.nsmSettings = nsmSettings;
		this.dmFunctions = dmFunctions;
		this.predDomains = predDomains;
		this.normativeNetwork = normativeNetwork;
		this.normReasoner = normReasoner;
		this.operators = operators;
		this.genMode = genMode;
		this.genStep = genStep;
		this.nsMetrics = nsMetrics;
		
		this.normIntersections = new HashMap<String, NormIntersection>();
		this.normClassifications = new HashMap<Norm, List<NormAttribute>>();
		this.normClassifier = new SIMONNormClassifier(normEvDimensions,
				nsmSettings,normativeNetwork, operators, nsMetrics);
	}

	/**
	 * 
	 * @param normClassifications
	 */
	public void step(Map<ViewTransition, NormsApplicableInView> normApplicability,
			List<Norm> normsActivatedDuringGeneration) {
		
		List<Norm> processed = new ArrayList<Norm>();
		List<Norm> visited = new ArrayList<Norm>();
		
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

			boolean isIneffective = attributes.contains(NormAttribute.INEFFECTIVE);
			boolean isUnnecessary = attributes.contains(NormAttribute.UNNECESSARY);
			boolean isGeneralisable = attributes.contains(NormAttribute.GENERALISABLE);
			
			/* If the norm is whether ineffective or unnecessary, then deactivate
			 * it (specialise it into its children) */
			if(isIneffective || isUnnecessary) {
				visited.clear();
				specialiseDown(norm, NetworkNodeState.DISCARDED, visited);
			}

			/* If the norm has enough utility to be generalised, 
			 * then try to generalise it */
			else if(isGeneralisable) {
				generaliseUp(norm, genMode, genStep);
			}
			
			/* Update complexities metrics */
			this.nsMetrics.incNumNodesVisited();
		}
	}

	/**
	 * Recursively specialises a norm into its children, its children
	 * into their children, and so on
	 * 
	 * @param norm the norm to specialise
	 */
	public void specialiseDown(Norm norm, NetworkNodeState specState, List<Norm> visited) {
		if(visited.contains(norm)) {
			return;
		}
		visited.add(norm);

		/* Update complexities metrics */
		this.nsMetrics.incNumNodesVisited();
		
		/* Specialise down all parent norms */
		List<Norm> parents = this.normativeNetwork.getParents(norm);
		for(Norm parent : parents) {
			this.specialiseDown(parent, specState, visited);
		}

		List<Norm> children = this.normativeNetwork.getChildren(norm);

		/* Only specialise norms that are represented by
		 * an active norm, whether itself or a parent norm */
		if(this.normativeNetwork.isRepresented(norm)) {
			List<Norm> childrenToActivate = new ArrayList<Norm>(children);
			operators.specialise(norm, specState, childrenToActivate);
			
			System.out.println("SPECIALISATION ");
			System.out.println("Norm: " + norm);
			System.out.println("Children: " + childrenToActivate);
			System.out.println("----------------------------------------------------------------------");
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
	public void generaliseUp(Norm normA,
			NormGeneralisationMode genMode, int genStep) {

		/* Get all the active norms (the normative system) */
		NormativeSystem NS = (NormativeSystem)
				normativeNetwork.getNormativeSystem().clone();

		/* Compute matches with each norm */
		for(Norm normB : NS) {
			
			/* Update complexities metrics */
			this.nsMetrics.incNumNodesVisited();
			
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
				
//				/* Update complexities metrics */
//				this.nsMetrics.incNumNodesVisited();
			}

			/* 4. Do not generalise if any of the generalisable norms or the parent
			 * norm are not consistent with the given domain they have been 
			 * generated for */
			for(Norm norm : genNorms.getAllNorms()) {
				if(!this.dmFunctions.isConsistent(norm.getPrecondition())) {
					generalise = false;
					break;
				}
				
//				/* Update complexities metrics */
//				this.nsMetrics.incNumNodesVisited();
			}

			/* Do not generalise either if the parent norm is already created
			 * and active (that is, the same parent norm has been created in a 
			 * previous generalisation with other two norms) */
			if(this.normativeNetwork.contains(parent) && 
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
					this.operators.activate(parent);
					this.normativeNetwork.getUtility(parent).reset();
				}

				/* Perform the norm generalisation */
				if(!this.normativeNetwork.isAncestor(parent, n1)) {
					this.operators.generalise(n1, parent);	
				}
				if(!this.normativeNetwork.isAncestor(parent, n2)) {
					this.operators.generalise(n2, parent); 
				}

				/* Deactivate children */
				for(Norm child : this.normativeNetwork.getChildren(parent)) {
					this.operators.deactivate(child, NetworkNodeState.GENERALISED);
					
					/* Update complexities metrics */
					this.nsMetrics.incNumNodesVisited();
				}
				
				System.out.println("GENERALISATION ");
				System.out.println("N1: " + n1);
				System.out.println("N2: " + n2);
				System.out.println("parent: " + parent);
				System.out.println("----------------------------------------------------------------------");
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
		EnvironmentAgentAction action = normA.getAction();
		EnvironmentAgentAction otherAction = normB.getAction();
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

			genNorms = new GeneralisableNorms(intersection, genStep);
		}		
		return genNorms;
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
