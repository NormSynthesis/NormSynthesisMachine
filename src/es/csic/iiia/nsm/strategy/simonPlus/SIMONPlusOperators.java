package es.csic.iiia.nsm.strategy.simonPlus;

import java.util.ArrayList;
import java.util.List;

import es.csic.iiia.nsm.NormSynthesisMachine;
import es.csic.iiia.nsm.agent.language.PredicatesDomains;
import es.csic.iiia.nsm.config.Dimension;
import es.csic.iiia.nsm.config.DomainFunctions;
import es.csic.iiia.nsm.config.Goal;
import es.csic.iiia.nsm.config.NormSynthesisSettings;
import es.csic.iiia.nsm.metrics.NormSynthesisMetrics;
import es.csic.iiia.nsm.net.norm.NetworkNodeState;
import es.csic.iiia.nsm.net.norm.NormativeNetwork;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.generation.Conflict;
import es.csic.iiia.nsm.norm.generation.NormGenerationMachine;
import es.csic.iiia.nsm.norm.generation.cbr.CBRNormGenerationMachine;
import es.csic.iiia.nsm.norm.reasoning.NormReasoner;
import es.csic.iiia.nsm.norm.refinement.lion.NormAttribute;

/**
 * The operators that the SIMON strategy uses to perform norm synthesis
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
public class SIMONPlusOperators {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------

	protected NormReasoner normReasoner;					// norm reasoner
	protected NormReasoner normEvaluationReasoner;// norm reasoner
	protected DomainFunctions dmFunctions;				// domain functions
	protected PredicatesDomains predDomains;			// predicates and their domains
	protected SIMONPlusStrategy strategy;					// the norm synthesis strategy
	protected NormativeNetwork normativeNetwork;	// the normative network
	protected NormGenerationMachine genMachine;		// the norm generation machine
	protected NormSynthesisSettings nsmSettings;	// norm synthesis settings
	protected NormSynthesisMetrics nsMetrics;

	protected List<Norm> normsToAddToReasoner;
	protected List<Norm> normsToAddToEvaluationReasoner;
	protected List<Norm> normsToRemoveFromEvaluationReasoner;
	
	protected boolean isNormGenReactiveToConflicts;

	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------

	/**
	 * Constructor
	 * 
	 * @param 	strategy the SIMON norm synthesis strategy
	 * @param 	normGenerationReasoner the norm reasoner, to reason about norm
	 * 					applicability	and compliance
	 * @param 	nsm the norm synthesis machine
	 */
	public SIMONPlusOperators(SIMONPlusStrategy strategy, 
			NormReasoner normGenerationReasoner, 
			NormReasoner agentsNormApplicabilityReasoner, 
			NormSynthesisMachine nsm) {

		this.strategy = strategy;
		this.normReasoner = normGenerationReasoner;
		this.normEvaluationReasoner = agentsNormApplicabilityReasoner;
		this.nsmSettings = nsm.getNormSynthesisSettings();
		this.dmFunctions = nsm.getDomainFunctions();
		this.predDomains = nsm.getPredicatesDomains();
		this.normativeNetwork = nsm.getNormativeNetwork();
		this.nsMetrics = nsm.getNormSynthesisMetrics();

		this.genMachine = new CBRNormGenerationMachine(this.normativeNetwork,
				nsm.getNormReasoner(), strategy, nsm.getRandom(), 
				nsm.getNormSynthesisMetrics());

		this.isNormGenReactiveToConflicts = 
				nsmSettings.isNormGenerationReactiveToConflicts();
		
		this.normsToAddToReasoner = new ArrayList<Norm>();
		this.normsToAddToEvaluationReasoner = new ArrayList<Norm>();
		this.normsToRemoveFromEvaluationReasoner = new ArrayList<Norm>();
	}

	/**
	 * Creates norms to regulate a given {@code conflict} that the norm
	 * synthesis machine has perceived in the scenario. The conflict
	 * is detected in terms of a system {@code goal}
	 * 
	 * @param conflict the perceived conflict
	 * @param goal the goal with respect to which the conflict has arisen
	 * @see Conflict
	 * @see Goal
	 */
	public void create(Conflict conflict, Goal goal) {
		List<Norm> createdNorms;

		/* Generate norms to avoid the conflict in the future */
		createdNorms = genMachine.generateNorms(conflict, dmFunctions, goal);

		/* Add norms to add and set their state to 'created' */
		for(Norm norm : createdNorms)	{
			
			/* New addition to the normative network. Add the norm to the
			 * normative network and set its state to 'created' whether if
			 * norm generation is reactive or it is not */
			if(!this.normativeNetwork.contains(norm)) {
				this.add(norm);
				this.hibernate(norm);
			}
			
			/* Norm generation is reactive. Add the norm (if it does not exist
			 * yet in the normative network), reset its utility and classifications
			 * and activate it */
			if(this.isNormGenReactiveToConflicts) {
				if(!this.normativeNetwork.contains(norm)) {
					this.add(norm);
				}
				else {
					this.hibernate(norm);
				}
			}
		}
	}

	/**
	 * Adds a norm to the normative network (if the normative network
	 * does not contain it yet) and activates it by setting its state
	 * to <tt>active</tt> in the normative network
	 * 
	 * @param norm the norm to add
	 */
	public void add(Norm norm) {
		if(!normativeNetwork.contains(norm)) {

			/* Add the norm to the network in case it does not exist on it */
			this.normativeNetwork.add(norm);

			/* Link the norms to other norms that may
			 * generalise (and represent) the norm */
			this.link(norm);

			/* Add norm to the norms reasoner */
			this.normsToAddToReasoner.add(norm);
			this.normsToAddToEvaluationReasoner.add(norm);
//			this.normReasoner.addNorm(norm);
//			this.normEvaluationReasoner.addNorm(norm);
			
			/* Update complexities metrics */
			this.nsMetrics.incNumNodesSynthesised();
			this.nsMetrics.incNumNodesInMemory();
		}
	}

	/**
	 * Activates a given {@code norm} in the normative network, resets
	 * its utility and adds the norm to the norm reasoner. Thus, the
	 * strategy will take the norm into account to compute norm
	 * applicability and compliance
	 * 
	 * @param norm the norm to activate
	 */	
	public void activate(Norm norm) {

		/* Only activate the norm if it is not already 
		 * represented by another norm */
		if(!normativeNetwork.isRepresented(norm)) {
			normativeNetwork.setState(norm, NetworkNodeState.ACTIVE);	
		}
	}

	/**
	 * Deactivates a given {@code norm} in the normative network and removes
	 * it from the norm reasoner. Thus, the strategy will not take
	 * the norm into account to compute norm applicability and compliance
	 * 
	 * @param norm the norm to deactivate
	 */
	public void deactivate(Norm norm, NetworkNodeState newState) {
		this.normativeNetwork.setState(norm, newState);
	}

	/**
	 * 
	 * @param norm
	 */
	public void hibernate(Norm norm) {
		if(this.normativeNetwork.getState(norm) != NetworkNodeState.CREATED) {
			this.normativeNetwork.setState(norm, NetworkNodeState.CREATED);
			this.normativeNetwork.getUtility(norm).reset();
			this.normativeNetwork.removeAttribute(norm, NormAttribute.EFFECTIVE);
			this.normativeNetwork.removeAttribute(norm, NormAttribute.INEFFECTIVE);
			this.normativeNetwork.removeAttribute(norm, NormAttribute.NECESSARY);
			this.normativeNetwork.removeAttribute(norm, NormAttribute.UNNECESSARY);
			
			if(this.isNormGenReactiveToConflicts) {
				this.normsToAddToEvaluationReasoner.add(norm);
//				this.normEvaluationReasoner.addNorm(norm);
			}
		}
	}
	
	/**
	 * 
	 * @param norm
	 */
	public void discard(Norm norm) {
		this.deactivate(norm, NetworkNodeState.DISCARDED);
		
		/* If norm generation is reactive to conflicts, then remove the norm
		 * from the norm evaluation reasoner so that it is no longer taking
		 * into account in the norm evaluation stage */ 
		if(this.isNormGenReactiveToConflicts) {
			this.normsToRemoveFromEvaluationReasoner.add(norm);
//			this.normEvaluationReasoner.removeNorm(norm);
		}
		/* Otherwise, reset the norm's effectiveness so that the NSM keeps
		 * on evaluating the norm in terms of its necessity */
		else {			
			this.normativeNetwork.getUtility(norm).resetDimension(Dimension.Effectiveness);
			this.normativeNetwork.removeAttribute(norm, NormAttribute.EFFECTIVE);
			this.normativeNetwork.removeAttribute(norm, NormAttribute.INEFFECTIVE);
			this.normativeNetwork.removeAttribute(norm, NormAttribute.NECESSARY);
			this.normativeNetwork.removeAttribute(norm, NormAttribute.UNNECESSARY);
		}
	}

	/**
	 * Generalises a {@code child} norm to a {@code parent} norm
	 * 
	 * @param child the child norm
	 * @param parent the parent norm
	 */
	public void generalise(Norm child, Norm parent) {		
		this.normativeNetwork.addGeneralisation(child, parent);

		/* Deactivate the child norm if it is represented by
		 * an ancestor (the parent norm, likely) */
		for(Norm p : this.normativeNetwork.getParents(child)) {
			if(this.normativeNetwork.isRepresented(p)) {
				this.deactivate(child, NetworkNodeState.GENERALISED); 
//				break;
			}

			/* Update complexities metrics */
			this.nsMetrics.incNumNodesVisited();
		}
	}

	/**
	 * Specialises a norm in the normative network
	 * 
	 * @param norm the norm to specialise
	 * @param children the children into which to specialise the norm
	 */
	public void specialise(Norm norm) {
		List<Norm> children = this.normativeNetwork.getChildren(norm);

		/* Deactivation of a general norm */
		if(children.size() > 0) {
			this.deactivate(norm, NetworkNodeState.SPECIALISED);
		}
		else {
			this.discard(norm);
		}

		/* Activate child norms that are not represented by an ancestor */
		for(Norm child : children) {
			if(!normativeNetwork.isRepresented(child)) {
				this.activate(child);
			}
		}

		/* Update complexities metrics */
		this.nsMetrics.incNumNodesVisited();
	}

	/**
	 * 
	 * @param norm
	 */
	public void link(Norm norm) {
		List<Norm> topBoundary =
				(List<Norm>)this.normativeNetwork.getTopBoundary();
		List<Norm> visitedNorms = new ArrayList<Norm>();

		for(Norm normB : topBoundary) {
			if(!norm.equals(normB)) {
				this.searchRelationships(norm, normB, visitedNorms);
			}
		}
	}

	/**
	 * 
	 * @param normA
	 * @param normB
	 * @param visitedNorms
	 */
	private void searchRelationships(Norm normA, Norm normB,
			List<Norm> visitedNorms) 
	{
		List<Norm> normAChildren = this.normativeNetwork.getChildren(normA);
		List<Norm> normBChildren = this.normativeNetwork.getChildren(normB);
		boolean linked = false;

		List<Norm> normBSatisfiedChildren = 
				this.normReasoner.getSatisfiedNorms(normA, normBChildren);
		List<Norm> normBChildrenSatisfyingA = 
				this.normReasoner.getNormsSatisfying(normBChildren, normA);
		List<Norm> normBChildrenNotSatisfyingA = 
				this.normReasoner.getNormsNotSatisfying(normBChildren, normA);


		/* A norm must not generalise itself */
		if(normA.equals(normB)) {
			return;
		}

		/* Check if normA can be a parent of normB. With this aim, check if
		 * normB satisfies normA (normA includes normB). If so, generalise
		 * from normB to normA only if normA is not already an ancestor of normB */
		if(normReasoner.satisfies(normB, normA))	{
			if(!this.normativeNetwork.isAncestor(normA, normB)) {
				this.generalise(normB, normA);
				linked = true;
			}

			/* Ensure that normB does not have a child that is already a child of normA */
			for(Norm normBChild : normBChildren) {
				if(normAChildren.contains(normBChild)) {
					this.normativeNetwork.removeGeneralisation(normBChild, normA);
				}

				/* Update complexities metrics */
				this.nsMetrics.incNumNodesVisited();
			}
		} 

		/* Check if normA can be a child of normB (normA satisfies normB) */
		else if(normReasoner.satisfies(normA, normB)) {

			/*If normA satisfies normB and it does not satisfy any child of normB,
			 * then normA is a direct child of normB. Generalise from normA to
			 * normB, only if normB is not already an ancestor of normA */
			if(normBSatisfiedChildren.isEmpty()) {
				if(!this.normativeNetwork.isAncestor(normB, normA)) {
					this.generalise(normA, normB);
					linked = true;
				}

				/* Check that normA is not "between" normB and any of normB's children.
				 * With this aim, check if there is any child of normB that satisfies
				 * normA. For every child of normB that satisfies normA, remove the
				 * generalisation from that child to normB, and generalise from the
				 * child to normA. In conclusion, "put normA between normB and
				 * that child"*/
				if(this.normativeNetwork.isAncestor(normB, normA)) {
					for(Norm normBChild : normBChildrenSatisfyingA) {
						this.normativeNetwork.removeGeneralisation(normBChild, normB);
						this.generalise(normBChild, normA);
					}
				}
			}
		}

		/* Si no se ha conseguido enlazar con nadie, seguimos buscando */
		if(!linked) {
			for(Norm normBChild : normBChildren) {
				this.searchRelationships(normA, normBChild, visitedNorms);
			}
		} 

		/* Si se ha enlazado continuamos igual, por si se puede seguir enlazando
		 * por abajo. La unica cuestion es que solo seguimos bajando por
		 * aquellos hijos que no satisfacen A (para asegurarnos de que si A
		 * ya se ha puesto como padre de B,  no se ponga tambien como padre 
		 * de sus hijas */
		else if(!normBChildrenNotSatisfyingA.isEmpty()) {		
			for(Norm normBChild : normBChildrenNotSatisfyingA) {
				this.searchRelationships(normA, normBChild, visitedNorms);
			}
		}

		/* Update complexities metrics */
		this.nsMetrics.incNumNodesVisited();
	}

	/**
	 * 
	 * @return
	 */
	public List<Norm> getNormsToAddToReasoner() {
		return normsToAddToReasoner;
	}

	/**
	 * 
	 * @return
	 */
	public List<Norm> getNormsToAddToEvaluationReasoner() {
		return normsToAddToEvaluationReasoner;
	}

	/**
	 * 
	 * @return
	 */
	public List<Norm> getNormsToRemoveFromEvaluationReasoner() {
		return normsToRemoveFromEvaluationReasoner;
	}
	
	/**
	 * 
	 */
	public void clearNormControlLists() {
		this.normsToAddToReasoner.clear();
		this.normsToAddToEvaluationReasoner.clear();
		this.normsToRemoveFromEvaluationReasoner.clear();
	}
}
