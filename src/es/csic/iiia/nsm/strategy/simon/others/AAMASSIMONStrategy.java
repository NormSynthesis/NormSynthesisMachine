package es.csic.iiia.nsm.strategy.simon.others;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.csic.iiia.nsm.NormSynthesisMachine;
import es.csic.iiia.nsm.NormSynthesisMachine.NormGeneralisationMode;
import es.csic.iiia.nsm.agent.EnvironmentAgentAction;
import es.csic.iiia.nsm.agent.language.PredicatesDomains;
import es.csic.iiia.nsm.agent.language.SetOfPredicatesWithTerms;
import es.csic.iiia.nsm.config.Dimension;
import es.csic.iiia.nsm.config.DomainFunctions;
import es.csic.iiia.nsm.config.Goal;
import es.csic.iiia.nsm.config.NormSynthesisSettings;
import es.csic.iiia.nsm.metrics.NormSynthesisMetrics;
import es.csic.iiia.nsm.net.norm.NormativeNetwork;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.NormModality;
import es.csic.iiia.nsm.norm.NormativeSystem;
import es.csic.iiia.nsm.norm.evaluation.NormComplianceOutcomes;
import es.csic.iiia.nsm.norm.evaluation.NormsApplicableInView;
import es.csic.iiia.nsm.norm.evaluation.Utility;
import es.csic.iiia.nsm.norm.generation.Conflict;
import es.csic.iiia.nsm.norm.reasoning.NormReasoner;
import es.csic.iiia.nsm.norm.refinement.simon.GeneralisableNorms;
import es.csic.iiia.nsm.norm.refinement.simon.NormIntersection;
import es.csic.iiia.nsm.perception.Monitor;
import es.csic.iiia.nsm.perception.ViewTransition;
import es.csic.iiia.nsm.strategy.NormSynthesisStrategy;

/**
 * The SIMON norm synthesis strategy
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
public class AAMASSIMONStrategy implements NormSynthesisStrategy {

	/**
	 * Generalisation mode of SIMON. Mode <tt>Shallow<tt> sets SIMON to perform
	 * shallow generalisations, while mode <tt>Deep<tt> sets SIMON to perform
	 * deep generalisations
	 * 
	 * @author "Javier Morales (jmorales@iiia.csic.es)"
	 */
	public enum GeneralisationMode {
		Deep, Shallow;
	}

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------

	protected NormSynthesisMachine nsm;
	protected NormSynthesisSettings nsmSettings; 
	protected NormReasoner normReasoner;
	protected NormativeNetwork normativeNetwork;
	protected NormSynthesisMetrics nsMetrics;
	protected DomainFunctions dmFunctions;
	protected PredicatesDomains predicatesDomains;
	protected Monitor monitor;

	protected AAMASSIMONUtilityFunction utilityFunction;	
	protected AAMASSIMONOperators operators;
	protected NormGeneralisationMode genMode;
	protected int genStep;

	protected Map<Goal,List<Conflict>> conflicts;
	protected Map<Goal,Map<ViewTransition, NormComplianceOutcomes>> normCompliance;
	protected Map<ViewTransition, NormsApplicableInView> normApplicability;
	protected Map<String, NormIntersection> normIntersections;
	protected Map<Norm, List<SetOfPredicatesWithTerms>> negRewardedNorms;

	protected List<ViewTransition> viewTransitions; 
	protected List<Norm> normsWithSpecScore;
	protected List<Norm> normsWithGenScore;
	protected List<Norm> generalisableNorms;
	protected List<Norm> specialisableNorms;
	protected List<Norm> visitedNorms;
	
	protected List<Norm> normsInNormativeNetwork;
	protected List<Norm> normsInNormativeSystem;
	protected List<Norm> normsAddedToNNThisCycle;
	protected List<Norm> normsAddedToNSThisCycle;
	protected List<Norm> normsRemovedFromNSThisCycle;
	
	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------

	/**
	 * Constructor
	 * 
	 * @param 	nsm the norm synthesis machine
	 * @param 	genMode the SIMON generalisation mode
	 */
	public AAMASSIMONStrategy(NormSynthesisMachine nsm,
			NormGeneralisationMode genMode, int genStep) {

		this.nsm = nsm;
		this.genMode = genMode;
		this.genStep = genStep;
		this.nsMetrics = nsm.getNormSynthesisMetrics();
		this.nsmSettings = nsm.getNormSynthesisSettings();
		this.dmFunctions = nsm.getDomainFunctions();
		this.predicatesDomains = nsm.getPredicatesDomains();
		this.normativeNetwork = nsm.getNormativeNetwork();
		this.monitor = nsm.getMonitor();
		this.normReasoner = nsm.getNormReasoner();

		this.operators = new AAMASSIMONOperators(this, normReasoner, nsm);
		this.utilityFunction = new AAMASSIMONUtilityFunction();
		this.conflicts = new HashMap<Goal, List<Conflict>>();

		this.normApplicability = new HashMap<ViewTransition, 
				NormsApplicableInView>();

		this.normCompliance = new HashMap<Goal,
				Map<ViewTransition,NormComplianceOutcomes>>();

		this.viewTransitions = new ArrayList<ViewTransition>();
		this.normsWithSpecScore = new ArrayList<Norm>();
		this.normsWithGenScore = new ArrayList<Norm>();
		this.generalisableNorms = new ArrayList<Norm>();
		this.specialisableNorms = new ArrayList<Norm>();
		this.visitedNorms = new ArrayList<Norm>();

		this.normsInNormativeNetwork = new ArrayList<Norm>();
		this.normsInNormativeSystem  = new ArrayList<Norm>();
		this.normsAddedToNNThisCycle = new ArrayList<Norm>();
		this.normsAddedToNSThisCycle = new ArrayList<Norm>();
		this.normsRemovedFromNSThisCycle = new ArrayList<Norm>();

		this.negRewardedNorms = new HashMap<Norm, 
				List<SetOfPredicatesWithTerms>>();
		this.normIntersections= new HashMap<String, NormIntersection>();

		for(Goal goal : nsmSettings.getSystemGoals()) {
			this.normCompliance.put(goal, new HashMap<ViewTransition,
					NormComplianceOutcomes>());
		}
	}

	/**
	 * Executes IRON's strategy and outputs the resulting normative system.
	 * The norm synthesis cycle consists in three steps:
	 * <ol>
	 * <li> Norm generation. Generates norms for each detected conflict.
	 * 			Generated norms are aimed to avoid detected conflicts in the future;
	 * <li> Norm evaluation. Evaluates norms in terms of their effectiveness
	 * 			and necessity, based on the outcome of their compliances and 
	 * 			infringements, respectively; and
	 * <li> Norm refinement. Generalises norms which utilities are over
	 * 			generalisation thresholds, and specialises norms which utilities
	 * 			are under specialisation thresholds. Norm generalisations can be
	 * 			performed in Shallow or Deep mode.
	 * 
	 * @return the normative system resulting from the norm synthesis cycle
	 */
	public NormativeSystem execute() {
		this.visitedNorms.clear();
		this.negRewardedNorms.clear();

		this.normGeneration();
		this.normEvaluation();
		this.normRefinement();

		/* Return the current normative system */
		return normativeNetwork.getNormativeSystem();
	}

	//---------------------------------------------------------------------------
	// Private methods
	//---------------------------------------------------------------------------

	/**
	 * Executes norm generation
	 */
	private void normGeneration() {

		/* Obtain monitor perceptions */
		viewTransitions = new ArrayList<ViewTransition>();
		obtainPerceptions(viewTransitions);

		/* Conflict detection */
		conflicts = conflictDetection(viewTransitions);

		/* Norm generation */
		for(Goal goal : conflicts.keySet()) {
			for(Conflict conflict : conflicts.get(goal)) {
				operators.create(conflict, goal);
			}	
		}
	}

	/**
	 * Executes norm evaluation
	 */
	private void normEvaluation() {

		/* Compute norm applicability */
		this.normApplicability = this.normApplicability(viewTransitions);

		/* Detect norm applicability and compliance */
		this.normCompliance(this.normApplicability);

		/* Update utilities and performances */
		this.updateUtilitiesAndPerformances(this.normCompliance);
	}

	/**
	 * Executes norm refinement
	 */
	private void normRefinement() {

		/* Add norms that have been rewarded with a negative value */
		this.addNegRewardedNorms(negRewardedNorms);

		/* Monitor norm utilities to detect utilities passing thresholds */
		this.checkThresholds();

		/* Specialise norms that under perform */
		List<Norm> visited = new ArrayList<Norm>();
		for(Norm norm : this.specialisableNorms)	{
			specialiseDown(norm, visited);
		}

		/* Generalise norms that may be generalised */
		for(Norm norm : this.generalisableNorms) {
			generaliseUp(norm, genMode, genStep);
		}
		
		this.manageNormControlLists();
	}

	/**
	 * Calls scenario monitors to perceive agents interactions
	 * 
	 * @return a {@code List} of the monitor perceptions, where each perception
	 *  				is a view transition from t-1 to t
	 */
	private void obtainPerceptions(List<ViewTransition> viewTransitions) {
		this.monitor.getPerceptions(viewTransitions);
	}

	/**
	 * Given a list of view transitions (from t-1 to t), this method
	 * returns a list of conflicts with respect to each goal of the system
	 * 
	 * @param viewTransitions the list of perceptions of each sensor
	 */
	protected Map<Goal, List<Conflict>> conflictDetection(
			List<ViewTransition> viewTransitions) {

		this.nsMetrics.resetNonRegulatedConflicts();
		this.conflicts.clear();

		/* Conflict detection is computed in terms of a goal */
		for(Goal goal : this.nsmSettings.getSystemGoals())		{
			List<Conflict> goalConflicts = new ArrayList<Conflict>();

			for(ViewTransition vTrans : viewTransitions) {
				goalConflicts.addAll(dmFunctions.getConflicts(goal, vTrans));
			}  	
			conflicts.put(goal, goalConflicts);
		}
		return conflicts;
	}

	/**
	 * Computes the norm applicability perceived by each sensor.
	 * (recall that each view transition is perceived by a particular sensor)
	 * 
	 * @param vTransitions the list of perceptions of each sensor
	 * @return a map containing the norms that are applicable to each
	 * agent in each view transition
	 */
	protected Map<ViewTransition, NormsApplicableInView> normApplicability(
			List<ViewTransition> vTransitions)	{

		/* Clear norm applicability from previous tick */
		this.normApplicability.clear();

		/* Get applicable norms of each viewTransition (of each sensor) */
		for(ViewTransition vTrans : vTransitions) {
			NormsApplicableInView normApplicability;
			normApplicability = this.normReasoner.getNormsApplicable(vTrans);
			this.normApplicability.put(vTrans, normApplicability);
		}
		return this.normApplicability;
	}

	/**
	 * Computes norms' compliance based on the norms that were applicable 
	 * to each agent in the previous time step (norm applicability)
	 * and the actions that agents performed in the transition from the 
	 * previous to the current time step
	 * 
	 * @param normApplicability norms that were applicable in the previous tick
	 */
	protected void normCompliance(Map<ViewTransition,
			NormsApplicableInView> normApplicability) {

		/* Check norm compliance in the view in terms of each system goal */
		for(Goal goal : this.nsmSettings.getSystemGoals()) {

			/* Clear norm compliance of previous tick */
			this.normCompliance.get(goal).clear();

			/* Evaluate norm compliance and conflicts in each 
			 * view transition with respect to each system goal */
			for(ViewTransition vTrans : normApplicability.keySet()) {
				NormsApplicableInView vNormAppl = normApplicability.get(vTrans);

				/* If there is no applicable norm in the view, continue */
				if(vNormAppl.isEmpty()) {
					continue;
				}
				NormComplianceOutcomes nCompliance = this.normReasoner.
						checkNormComplianceAndOutcomes(vNormAppl, goal);

				this.normCompliance.get(goal).put(vTrans, nCompliance);
			}
		}
	}

	/**
	 * Updates norm utilities and performances based on
	 * their norm compliance in the current time step
	 * 
	 * @param normCompliance the norm compliance in the current time step
	 */
	protected void	updateUtilitiesAndPerformances(
			Map<Goal, Map<ViewTransition,NormComplianceOutcomes>> normCompliance) {

		for(Goal goal : this.nsmSettings.getSystemGoals()) {
			for(ViewTransition vTrans : normCompliance.get(goal).keySet()) {
				for(Dimension dim : this.nsm.getNormEvaluationDimensions())	{
					this.negRewardedNorms =
							this.utilityFunction.evaluate(dim, goal, 
									normCompliance.get(goal).get(vTrans), normativeNetwork);	
				}
			}
		}
	}

	/**
	 * Checks the utilities of the norms that have been applicable during the 
	 * current time step, in order to assess if any of the norms' utilities
	 * have crossed the generalisation or specialisation thresholds.
	 * For each norm that which utilities have crossed the generalisation
	 * threshold (and they are over this threshold), this method adds it 
	 * to a list of norms that can be generalised. By contrast, for each norm
	 * which utilities have crossed the specialisation threshold (and they are
	 * under this threshold), this method adds it to a list of norms that can
	 * be specialised
	 */
	protected void checkThresholds() {
		this.generalisableNorms.clear();
		this.specialisableNorms.clear();

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

		/* Remove random component of norm applicability */
		Collections.sort(normsToRevise);

		for(Norm norm : normsToRevise) {

			/* If the norm wasn't under performing and now it is,
			 * then it is a candidate for specialisation
			 */
			if((!this.normsWithSpecScore.contains(norm)) && 
					isUnderperforming(norm) && 
					this.normativeNetwork.isLeaf(norm))
			{ 
				this.normsWithSpecScore.add(norm);
				this.specialisableNorms.add(norm);
			}
			
			/* If the norm wasn't generalisable and now it is,
			 * then it is a candidate for generalisation */
			else if(!this.normsWithGenScore.contains(norm) &&
					hasGeneralisationUtility(norm) && 
					this.normativeNetwork.isRepresented(norm))
			{			
				this.normsWithGenScore.add(norm);
				this.generalisableNorms.add(norm);
			}
		}
	}

	/**
	 * 
	 */
	protected void manageNormControlLists() {
		this.normsAddedToNNThisCycle.clear();
		this.normsAddedToNSThisCycle.clear();
		this.normsRemovedFromNSThisCycle.clear();
		
		/* Check norm additions to the normative network */
		for(Norm norm : this.normativeNetwork.getNorms()) {
			if(!this.normsInNormativeNetwork.contains(norm)) {
				this.normsInNormativeNetwork.add(norm);
				this.normsAddedToNNThisCycle.add(norm);
			}
		}
		
		/* Check norm additions to the normative system */
		for(Norm norm : this.normativeNetwork.getNormativeSystem()) {
			if(!this.normsInNormativeSystem.contains(norm)) {
				this.normsInNormativeSystem.add(norm);
				this.normsAddedToNSThisCycle.add(norm);
			}
		}
		
		/* Check norm removals from the normative system */
		List<Norm> toRemove = new ArrayList<Norm>();
		for(Norm norm : this.normsInNormativeSystem) {
			if(!this.normativeNetwork.getNormativeSystem().contains(norm)) {
				toRemove.add(norm);
				this.normsRemovedFromNSThisCycle.add(norm);
			}
		}
		for(Norm norm : toRemove) {
			this.normsInNormativeSystem.remove(norm);
		}
		
		/* Add to the norm reasoner those new norms that are represented */
		List<Norm> normsRepresented = this.normativeNetwork.getRepresentedNorms();
		for(Norm norm : normsRepresented) {
			if(!this.normReasoner.contains(norm)) {
				this.normReasoner.addNorm(norm);
			}
		}
		
		/* Remove from the norm reasoner those norms
		 * that are no longer represented */
		toRemove.clear();
		for(Norm norm : this.normReasoner.getNorms()) {
			if(!normsRepresented.contains(norm)) {
				toRemove.add(norm);
			}
		}
		for(Norm norm : toRemove) {
			this.normReasoner.removeNorm(norm);
		}
		
		/* New norms in the normative system are new candidates 
		 * to be generalised or specialised at any time */
		for(Norm norm : this.normsAddedToNNThisCycle) {
			this.normsWithGenScore.remove(norm);
			this.normsWithSpecScore.remove(norm);
		}
		for(Norm norm : this.normsAddedToNSThisCycle) {
			this.normsWithGenScore.remove(norm);
			this.normsWithSpecScore.remove(norm);
		}
	}
	
	/**
	 * Adds norms to a list of norms that have been negatively rewarded
	 * during the current time step (during the norm evaluation phase)
	 * 
	 * @param negRewNorms the negatively rewarded norms
	 */
	protected void addNegRewardedNorms(Map<Norm,
			List<SetOfPredicatesWithTerms>> negRewNorms) {

		for(Norm norm : negRewardedNorms.keySet()) {
			List<SetOfPredicatesWithTerms> agContexts = negRewNorms.get(norm);

			for(SetOfPredicatesWithTerms precond : agContexts) {
				NormModality mod = norm.getModality();
				EnvironmentAgentAction action = norm.getAction();

				boolean exists = this.normativeNetwork.
						contains(precond, mod, action);

				if(!exists) {
					Norm specNorm = new Norm(precond, mod, action);
					this.operators.add(specNorm);
				}
			}
		}
	}

	/**
	 * Recursively specialises a norm into its children, its children
	 * into their children, and so on
	 * 
	 * @param norm the norm to specialise
	 */
	public void specialiseDown(Norm norm, List<Norm> visited) {
		if(visited.contains(norm)) {
			return;
		}
		visited.add(norm);

		/* Specialise down all parent norms */
		List<Norm> parents = this.normativeNetwork.getParents(norm);
		for(Norm parent : parents) {
			this.specialiseDown(parent, visited);
		}
		
		List<Norm> children = this.normativeNetwork.getChildren(norm);

		/* Only specialise norms that are represented by
		 * an active norm, whether itself or a parent norm */
		if(this.normativeNetwork.isRepresented(norm)) {

			/* Activate children */
			List<Norm> childrenToActivate = new ArrayList<Norm>(children);
			operators.specialise(norm, childrenToActivate);

			/* Specialise under performing children */
			for(Norm child : children) {
				if(this.isUnderperforming(child) && !visited.contains(child)) {
//					this.specialiseDown(child, visited);
				}
				else if(this.hasGeneralisationUtility(child)) {
					this.generalisableNorms.add(child);
				}
			}
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
					this.normativeNetwork.isRepresented(parent)) {
				generalise = false;
			}

			/* 5. Perform the norm generalisation if all the previous tests
			 * have been passed */
			if(generalise) {
				if(this.normativeNetwork.contains(n1)) {
					n1 = this.normativeNetwork.getNorm(n1);
				}
				else {
					this.operators.add(n1);
				}
				if(this.normativeNetwork.contains(n2)) {
					n2 = this.normativeNetwork.getNorm(n2);
				}
				else {
					this.operators.add(n2);
				}
				if(this.normativeNetwork.contains(parent)) {
					parent = this.normativeNetwork.getNorm(parent);
				}
				else {
					this.operators.add(parent);
				}

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
					this.operators.deactivate(child);
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
					this.predicatesDomains, genMode);
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
	 * Returns <tt>true</tt> if the norm is under performing, namely
	 * if any of the upper bound of its performance ranges is under
	 * the specialisation threshold
	 * 
	 * @param norm the norm
	 * @return <tt>true</tt> if the norm is under performing, namely
	 * 					if any of the upper bound of its performance ranges is under
	 * 					the specialisation threshold
	 */
	protected boolean isUnderperforming(Norm norm) {

		for(Dimension dim : this.nsm.getNormEvaluationDimensions()) 	{
			for(Goal goal : this.nsmSettings.getSystemGoals()) {
				Utility utility = this.normativeNetwork.getUtility(norm);
				float topBoundary = (float)utility.getPerformanceRange(dim, goal).
						getCurrentTopBoundary();
				float satDegree = this.nsmSettings.getSpecialisationBoundary(dim, goal);

				if(topBoundary < satDegree) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns <tt>true</tt> if the norm has enough utility to be generalised,
	 * namely if all the lower bounds of its performance ranges are over
	 * the generalisation threshold
	 * 
	 * @param norm the norm
	 * @return <tt>true</tt> if the norm has enough utility to be generalised,
	 * 					namely if all the lower bounds of its performance ranges are over
	 * 					the generalisation threshold
	 */
	protected boolean hasGeneralisationUtility(Norm norm) {

		for(Dimension dim : this.nsm.getNormEvaluationDimensions())	 {
			for(Goal goal : this.nsmSettings.getSystemGoals()) {

				Utility utility = this.normativeNetwork.getUtility(norm);
				float bottomBoundary = (float)utility.getPerformanceRange(dim, goal).
						getCurrentBottomBoundary();
				float satDegree = this.nsmSettings.getGeneralisationBoundary(dim, goal);

				if(bottomBoundary < satDegree) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * 
	 * @return
	 */
	public AAMASSIMONOperators getOperators() {
		return this.operators;
	}
	
	/* (non-Javadoc)
	 * @see es.csic.iiia.nsm.strategy.NormSynthesisStrategy#addDefaultNormativeSystem(java.util.List)
	 */
  public void addDefaultNormativeSystem(List<Norm> defaultNorms) {
	  for(Norm norm : defaultNorms) {
	  	this.operators.add(norm);
	  }
  }
}