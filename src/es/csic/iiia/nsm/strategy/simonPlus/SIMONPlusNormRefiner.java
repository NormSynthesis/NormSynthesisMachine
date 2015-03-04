/**
 * 
 * @author Javier Morales (jmorales@iiia.csic.es)
 */
package es.csic.iiia.nsm.strategy.simonPlus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
public class SIMONPlusNormRefiner {

	protected List<Dimension> normEvDimensions;
	protected NormSynthesisSettings nsmSettings;
	protected DomainFunctions dmFunctions;
	protected PredicatesDomains predDomains;
	protected NormReasoner normReasoner;
	protected NormativeNetwork normativeNetwork;
	protected NormGeneralisationMode genMode;
	protected NormSynthesisMetrics nsMetrics;

	protected SIMONPlusNormClassifier normClassifier;
	protected SIMONPlusUtilityFunction utilityFunction;	
	protected SIMONPlusOperators operators;

	protected Map<String, NormIntersection> normIntersections;
	protected Map<Norm, List<NormAttribute>> normClassifications;

	protected NormativeSystem feasibleGenNorms;
	protected NormativeSystem notFeasibleGenNorms;

	protected boolean isNormGenReactiveToConflicts;
	protected int genStep;

	/**
	 * 
	 * @param normativeNetwork
	 */
	public SIMONPlusNormRefiner(List<Dimension> normEvDimensions,
			NormSynthesisSettings nsmSettings, DomainFunctions dmFunctions,
			PredicatesDomains predDomains, NormativeNetwork normativeNetwork,
			NormReasoner normReasoner, NormSynthesisMetrics nsMetrics,
			SIMONPlusOperators operators, NormGeneralisationMode genMode,
			int genStep) {

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

		this.feasibleGenNorms = new NormativeSystem();
		this.notFeasibleGenNorms = new NormativeSystem();

		this.normIntersections = new HashMap<String, NormIntersection>();
		this.normClassifications = new HashMap<Norm, List<NormAttribute>>();
		this.normClassifier = new SIMONPlusNormClassifier(normEvDimensions,
				nsmSettings,normativeNetwork, operators, nsMetrics);

		this.isNormGenReactiveToConflicts = this.nsmSettings.
				isNormGenerationReactiveToConflicts();
	}

	/**
	 * 
	 * @param normClassifications
	 */
	public void step(Map<ViewTransition, NormsApplicableInView> normApplicability) {

		List<Norm> normsToActivate = new ArrayList<Norm>();
		List<Norm> normsToDeactivate = new ArrayList<Norm>();
		List<Norm> normsToGeneralise = new ArrayList<Norm>();
		boolean isEffective, isNecessary;
		boolean isIneffective, isUnnecessary;
		boolean classifiedInEffectiveness;

		/* Compute norms that must be revised */
		List<Norm> normsToRevise = this.checkNormsToRevise(normApplicability);

		/* Classify norms */
		this.normClassifications = this.normClassifier.step(normsToRevise);
		Set<Norm> normsClassified  = this.normClassifications.keySet();

		/* Refine norms based on norm classifications */
		for(Norm norm : normsClassified) {
			List<NormAttribute> attributes = normClassifications.get(norm);
			isEffective = attributes.contains(NormAttribute.EFFECTIVE);
			isNecessary = attributes.contains(NormAttribute.NECESSARY);
			isIneffective = attributes.contains(NormAttribute.INEFFECTIVE);
			isUnnecessary = attributes.contains(NormAttribute.UNNECESSARY);
			classifiedInEffectiveness = isEffective || isIneffective;

			/* If the norm is whether ineffective or unnecessary, then deactivate
			 * it (specialise it into its children) */
			if(isIneffective || isUnnecessary) {
				normsToDeactivate.add(norm);
			}

			/* Only if norm generation is not reactive to conflicts:
			 * activate norms when they are proven to be necessary */
			if((isEffective || !classifiedInEffectiveness) && isNecessary) {
				if(this.normativeNetwork.isLeaf(norm) &&
						!this.normativeNetwork.isRepresented(norm)) 
				{
					normsToActivate.add(norm);
				}
			}

			/* Check if the norm can be generalised */
			boolean isGeneralisable = attributes.contains(NormAttribute.GENERALISABLE);
			if(isGeneralisable) {
				normsToGeneralise.add(norm);
			}
		}

		/* Activate, deactivate and generalise norms */
		this.activateUp(normsToActivate);
		this.deactivateUp(normsToDeactivate);
		this.generaliseUp(normsToGeneralise);
	}

	/**
	 * 
	 * @param norms
	 */
	private void activateUp(List<Norm> norms) {
		for(Norm norm : norms) {
			this.activateUp(norm);
		}
	}

	/**
	 * Recursively specialises a norm into its children, its children
	 * into their children, and so on
	 * 
	 * @param norm the norm to specialise
	 */
	private void deactivateUp(List<Norm> norms) {
		List<Norm> visited = new ArrayList<Norm>();
		for(Norm norm : norms) {
			this.deactivateUp(norm, visited);
		}
	}

	/**
	 * 
	 * @param norms
	 * @param genMode
	 * @param genStep
	 */
	private void generaliseUp(List<Norm> norms) {
		for(Norm norm : norms) {
			this.generaliseUp(norm, genMode, genStep);
		}
	}

	/**
	 * Reactivates a norm that was previously created, activated and deactivated.
	 * This reactivation is performed recursively, trying to re-generalise the norm
	 * again, activating those parents that can be reactivated  
	 * 
	 * @param norm the norm to reactivate
	 */	
	public void activateUp(Norm norm) {
		if(!normativeNetwork.isRepresented(norm)) {
			this.notFeasibleGenNorms.clear();

			/* Activate the norm */
			this.operators.activate(norm);

			List<Norm> children = this.normativeNetwork.getChildren(norm);
			List<Norm> parents = this.normativeNetwork.getParents(norm);

			/* Deactivate all its children and set them as GENERALISED */
			for(Norm child : children) {
				if(this.normativeNetwork.isRepresented(child)) {
					this.operators.deactivate(child, NetworkNodeState.GENERALISED); 
				}
			}

			/* If the norm has no parents then it will remain active
			 * representing its children. Then, we try to generalise it up */
			if(parents.isEmpty()) {
				this.normativeNetwork.removeAttribute(norm, NormAttribute.GENERALISABLE);
			}

			/* Otherwise, if the norm has parents, check for each parent
			 * if it can be reactivated. A parent can be reactivated if it does
			 * not include any norm that is not represented, namely if it does
			 * not represent any discarded norms */
			else {

				/* Check if any parent can be reactivated */
				for(Norm parent : parents) {
					if(this.isGenNormFeasible(parent)) {
						this.activateUp(parent);
					}
				}
			}
		}
	}

	/**
	 * 
	 * @param norm
	 * @param deactState
	 * @param visited
	 */
	public void deactivateUp(Norm norm,	List<Norm> visited) {
		this.feasibleGenNorms.clear();

		/* Update complexities metrics */
		this.nsMetrics.incNumNodesVisited();

		if(visited.contains(norm)) {
			return;
		}
		visited.add(norm);

		/* Specialise down all parent norms */
		List<Norm> parents = this.normativeNetwork.getParents(norm);
		for(Norm parent : parents) {
			this.deactivateUp(parent, visited);
		}

		/* Only specialise norms that are represented by
		 * an active norm (whether itself or a parent norm)
		 * or which status is CREATED */
		if(this.normativeNetwork.getState(norm) == NetworkNodeState.CREATED ||
				this.normativeNetwork.isRepresented(norm)) 
		{
			operators.specialise(norm);

			System.out.println("SPECIALISATION ");
			System.out.println("Norm: " + norm);
			System.out.println("Children: " + this.normativeNetwork.getChildren(norm));
			System.out.println("--------------------------------------------------");
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
	public void generaliseUp(Norm normA, NormGeneralisationMode genMode, int genStep) {

		/* Variables to control generalisation flow */
		boolean genNormsExist, parentIsFeasible, genNormsAreConsistent;
		boolean parentIsRepresented, gensPerformed;

		NormativeSystem NS = (NormativeSystem)normativeNetwork.
				getNormativeSystem().clone();

		/* Norms to perform generalisations */
		Norm n1 = null;
		Norm n2 = null;
		Norm parent = null;

		do {
			if(genMode == NormGeneralisationMode.Deep) {
				NS = (NormativeSystem)normativeNetwork.getNormativeSystem().clone();
			}

			genNormsExist = false;
			parentIsFeasible = false;
			genNormsAreConsistent = false;
			parentIsRepresented = false;
			gensPerformed = false;
			int numNormsInNS = NS.size();
			int numNorm = 0;

			/* Retrieve NORM B: Perform pairwise comparisons of each
			 * norm with the other norms in the normative system */
			while(!gensPerformed && numNorm < numNormsInNS) {
				Norm normB = NS.get(numNorm);
				numNorm++;

				/* Update complexities metrics */
				this.nsMetrics.incNumNodesVisited();

				/* Never generalise a norm with itself */
				if(!normA.equals(normB)) {

					/* 1. Compare norms and get generalisable norms */
					GeneralisableNorms genNorms =
							this.getGeneralisableNorms(normA, normB, genStep);

					/* 2. Check that there exist norms to perform a generalisation */
					genNormsExist = this.existsGeneralisation(genNorms);

					/* 3. Check that the parent norm is feasible */
					if(genNormsExist) {
						n1 = genNorms.getNormA();
						n2 = genNorms.getNormB();
						parent = genNorms.getParent();
						parentIsFeasible = this.isGenNormFeasible(parent);
					}

					/* 4. Check if the parent norm is already created and active
					 * (that is, the same parent norm has been created in a 
					 * previous generalisation with other two norms) */
					if(genNormsExist && parentIsFeasible) {
						parentIsRepresented = this.normativeNetwork.isRepresented(parent);
					}

					/* 5. Check that the parent norm is feasible (that is, it was not
					 * stated as not feasible in a previous iteration of this method 
					 * in the current time step */
					if(genNormsExist && parentIsFeasible && !parentIsRepresented) {
						genNormsAreConsistent = this.areConsistent(genNorms);
					}

					/* 6. Do not generalise if any of the generalisable norms or the parent
					 * norm are not consistent with the given domain they have been
					 * generated for */
					if(genNormsExist && parentIsFeasible && 
							!parentIsRepresented && genNormsAreConsistent) {

						this.performGeneralisation(n1, n2, parent);
						gensPerformed = true;

						System.out.println("GENERALISATION ");
						System.out.println("N1: " + n1);
						System.out.println("N2: " + n2);
						System.out.println("parent: " + parent);
						System.out.println("--------------------------------------------");
					}
				}
			}
		} while(gensPerformed && genMode == NormGeneralisationMode.Deep); 
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
	 * @param genNorms
	 */
	private boolean existsGeneralisation(GeneralisableNorms genNorms) {

		/* There is no possible generalisation */
		if(genNorms == null) {
			return false;
		}

		/* If there is a possible generalisation, then get generalisable
		 * norms and check that all norms exist */
		else {
			for(Norm norm : genNorms.getAllNorms()) {
				if(norm == null) {
					return false;
				}
			}
		}
		/* There is a possible generalisation */
		return true;
	}

	/**
	 * @param genNorms
	 * @return
	 */
	private boolean areConsistent(GeneralisableNorms genNorms) {
		for(Norm norm : genNorms.getAllNorms()) {
			if(!this.dmFunctions.isConsistent(norm.getPrecondition())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @param feasibleGenNorms
	 * @param notFeasibleGenNorms
	 */
	private boolean isGenNormFeasible(Norm genNorm) {
		boolean feasible = true;
		List<Norm> notRepresented;

		/* If the parent is not feasible, then do not keep on computing */
		if(notFeasibleGenNorms.contains(genNorm)) {
			feasible = false;
		}
		/* If we do not know about the parent's feasibility, then compute
		 * its feasibility and save it in the corresponding list */
		else if(!feasibleGenNorms.contains(genNorm)) {

			/* Check that the parent norm does not contain a discarded norm */
			notRepresented =	this.normativeNetwork.getNotRepresentedNorms();

			for(Norm norm: notRepresented) {
				NetworkNodeState nState = this.normativeNetwork.getState(norm);

				/* If the general norm represents a norm that must not be represented, 
				 * then the general norm is not feasible */
				if(nState != NetworkNodeState.CREATED) {
					if(this.normReasoner.satisfies(norm, genNorm) && 
							!norm.equals(genNorm))	
					{
						this.notFeasibleGenNorms.add(genNorm);
						feasible = false;
						break;
					}	
				}
			}
			/* If the general norm is feasible, add it to the  
			 * list that keeps track of feasible general norms */
			if(feasible) {
				this.feasibleGenNorms.add(genNorm);
			}
		}
		return feasible;
	}

	/**
	 * @param n1
	 * @param n2
	 * @param parent
	 */
	private void performGeneralisation(Norm n1, Norm n2, Norm parent) {
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
			// this.operators.activate(parent); TODO: Cambio por activate recursivo
			this.activateUp(parent);

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
