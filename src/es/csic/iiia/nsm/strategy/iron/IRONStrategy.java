package es.csic.iiia.nsm.strategy.iron;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.csic.iiia.nsm.NormSynthesisMachine;
import es.csic.iiia.nsm.agent.AgentAction;
import es.csic.iiia.nsm.agent.language.PredicatesDomains;
import es.csic.iiia.nsm.agent.language.SetOfPredicatesWithTerms;
import es.csic.iiia.nsm.config.Dimension;
import es.csic.iiia.nsm.config.DomainFunctions;
import es.csic.iiia.nsm.config.Goal;
import es.csic.iiia.nsm.config.NormSynthesisSettings;
import es.csic.iiia.nsm.net.norm.NetworkNodeState;
import es.csic.iiia.nsm.net.norm.NormativeNetwork;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.NormModality;
import es.csic.iiia.nsm.norm.NormativeSystem;
import es.csic.iiia.nsm.norm.evaluation.NormComplianceOutcomes;
import es.csic.iiia.nsm.norm.evaluation.NormsApplicableInView;
import es.csic.iiia.nsm.norm.evaluation.Utility;
import es.csic.iiia.nsm.norm.generation.Conflict;
import es.csic.iiia.nsm.norm.reasoning.NormReasoner;
import es.csic.iiia.nsm.norm.refinement.iron.GeneralisationTrees;
import es.csic.iiia.nsm.norm.refinement.iron.PotentialGeneralisation;
import es.csic.iiia.nsm.perception.Monitor;
import es.csic.iiia.nsm.perception.ViewTransition;
import es.csic.iiia.nsm.strategy.NormSynthesisStrategy;

/**
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 *
 */
public class IRONStrategy implements NormSynthesisStrategy {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------

	private NormSynthesisMachine nsm;
	private NormSynthesisSettings nsmSettings; 
	private NormReasoner normReasoner;
	private NormativeNetwork normativeNetwork;

	private PredicatesDomains predDomains;
	private DomainFunctions dmFunctions;
	private Monitor monitor;

	private IRONUtilityFunction utilityFunction;	
	private IRONOperators operators;

	private Map<Goal,List<Conflict>> conflicts;
	private Map<Goal, NormComplianceOutcomes> normCompliance;
	private Map<ViewTransition, NormsApplicableInView> normApplicability;

	private GeneralisationTrees genTrees;
	
	private List<Norm> createdNorms;
	private List<Norm> activatedNorms;
	private List<Norm> 	normAdditions;
	private List<Norm> normDeactivations;
	
	private boolean hasNonRegulatedConflictsThisTick;
	
	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------

	/**
	 * Constructor 
	 * 
	 * @param nsm the norm synthesis machine
	 */
	public IRONStrategy(NormSynthesisMachine nsm) {
		this.nsm = nsm;
		this.nsmSettings = nsm.getNormSynthesisSettings();
		this.predDomains = nsm.getPredicatesDomains();
		this.dmFunctions = nsm.getDomainFunctions();
		this.normativeNetwork = nsm.getNormativeNetwork();
		this.monitor = nsm.getMonitor();
		
		this.genTrees = new GeneralisationTrees(this.predDomains, 
				this.dmFunctions, this.normativeNetwork);
		this.normReasoner = new NormReasoner(this.nsmSettings.getSystemGoals(), 
				this.predDomains, this.dmFunctions);

		this.operators = new IRONOperators(this, nsm, normReasoner);
		this.utilityFunction = new IRONUtilityFunction();

		this.normCompliance = new HashMap<Goal, NormComplianceOutcomes>();
		this.conflicts = new HashMap<Goal, List<Conflict>>();
		this.normApplicability = new HashMap<ViewTransition, NormsApplicableInView>();
		
		this.createdNorms = new ArrayList<Norm>();
		this.activatedNorms = new ArrayList<Norm>();
		this.normAdditions = new ArrayList<Norm>();
		this.normDeactivations = new ArrayList<Norm>();
		
		for(Goal goal : nsmSettings.getSystemGoals()) {
			this.normCompliance.put(goal, new NormComplianceOutcomes());
		}
		
		this.hasNonRegulatedConflictsThisTick = false;
	}

	/**
	 * 
	 */
	@Override
	public NormativeSystem execute() {

		/*-------------------
		 *  Norm generation
		 *-------------------*/

		/* Obtain monitor perceptions */
		List<ViewTransition> viewTransitions = new ArrayList<ViewTransition>();
		obtainPerceptions(viewTransitions);

		/* Conflict detection */
		conflicts = conflictDetection(viewTransitions);

		for(Goal goal : conflicts.keySet()) {
			for(Conflict conflict : conflicts.get(goal)) {
				operators.create(conflict, goal);
			}	
		}

		/*-------------------
		 *  Norm evaluation
		 *-------------------*/

		/* Compute norm applicability */
		this.normApplicability = this.normApplicability(viewTransitions);

		/* Detect norm applicability and compliance */
		this.normCompliance(this.normApplicability);

		/* Update utilities and performances */
		this.updateUtilitiesAndPerformances(this.normCompliance);

		/*-------------------
		 *  Norm refinement
		 *-------------------*/

		/* Create list of norms that have been complied/infringed */
		List<Norm> norms = new ArrayList<Norm>();
		for(Goal goal : normCompliance.keySet()) {
			for(Norm norm : normCompliance.get(goal).getFulfilledNorms()) {
				if(!norms.contains(norm)) {
					norms.add(norm);
				}
			}
			for(Norm norm : normCompliance.get(goal).getInfringedNorms()) {
				if(!norms.contains(norm)) {
					norms.add(norm);
				}
			}
		}

		/* Specialise under performing norms, try to
		 * generalise norms that do not under perform */
		for(Norm norm : norms) {
			if(this.normativeNetwork.getState(norm) == NetworkNodeState.ACTIVE) {
				if(isUnderperforming(norm)) {
					this.specialiseDown(norm);
				}
				else {
					this.generaliseUp(norm);
				}
			}
		}
		
		/* Return the normative system */
		return normativeNetwork.getNormativeSystem();
	}

	//------------------------------------------------------------------------------------------
	// Private methods
	//------------------------------------------------------------------------------------------

	/**
	 * Retrieves the perceptions of each sensor in the monitor
	 * of the Norm Synthesis Machine. Specifically, it returns 
	 * a <tt>ViewStream<tt>
	 * @param viewTransitions 
	 * 
	 * @return 
	 */
	private void obtainPerceptions(List<ViewTransition> viewTransitions) {
		this.monitor.getPerceptions(viewTransitions);
	}

	/**
	 * Given a list of view transitions (from t-1 to t), returns a list of 
	 * conflicts for each goal of the system
	 * 
	 * @param viewTransitions the list of perceptions of each sensor
	 */
	private Map<Goal, List<Conflict>> conflictDetection(
			List<ViewTransition> viewTransitions) {
		this.hasNonRegulatedConflictsThisTick = false;
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
	 * 
	 * @return an object containing the norms that are applicable to each
	 * agent in each view transition
	 */
	private Map<ViewTransition, NormsApplicableInView> normApplicability(
			List<ViewTransition> vTransitions)	{

		/* Clear norm applicability from previous tick */
		for(NormsApplicableInView vNormAppl : this.normApplicability.values()) {
			vNormAppl.clear();
		}

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
	private void normCompliance(Map<ViewTransition,
			NormsApplicableInView> normApplicability) {

		/* Clear norm compliance from previous tick */
		for(NormComplianceOutcomes nc : normCompliance.values()) {
			nc.clear();
		}

		/* Evaluate norm compliance and conflicts in each 
		 * view transition with respect to each system goal */
		for(ViewTransition vTrans : normApplicability.keySet()) {
			NormsApplicableInView vNormAppl = normApplicability.get(vTrans);

			/* If there is no applicable norm in the view, continue */
			if(vNormAppl.isEmpty()) {
				continue;
			}

			/* Check norm compliance in the view in terms of each system goal */
			for(Goal goal : this.nsmSettings.getSystemGoals()) {
				NormComplianceOutcomes nCompliance = this.normReasoner.
						checkNormComplianceAndOutcomes(vNormAppl, goal);

				this.normCompliance.put(goal, nCompliance);
			}
		}
	}

	/**
	 * Updates norm utilities and performances based on
	 * their norm compliance in the current time step
	 * 
	 * @param normCompliance the norm compliance in the current time step
	 */
	private void	updateUtilitiesAndPerformances(
			Map<Goal, NormComplianceOutcomes> normCompliance) {

		for(Dimension dim : this.nsm.getNormEvaluationDimensions())	{
			for(Goal goal : this.nsmSettings.getSystemGoals()) {
				this.utilityFunction.evaluate(dim, goal, 
						normCompliance.get(goal), normativeNetwork);
			}
		}
	}

	/**
	 * 
	 * @param norm
	 */
	private void generaliseUp(Norm norm) {
		List<PotentialGeneralisation> validGeneralisations;
		validGeneralisations = validGeneralisations(normativeNetwork, norm);

		for(PotentialGeneralisation generalisation : validGeneralisations) {
			Norm parent = generalisation.getParent();
			List<Norm> children = generalisation.getChildren();

			for(Norm child : children) {
				this.operators.add(child);				
			}
			this.operators.add(parent);
			
			/* Generalise norms */
			operators.generalise(normativeNetwork, parent, children);
		}
	}

	/**
	 * Recursively specialises a norm into its children, its children
	 * into their children, and so on
	 * 
	 * @param norm the norm to specialise
	 * 
	 */
	private void specialiseDown(Norm norm) {
		List<Norm> children = this.normativeNetwork.getChildren(norm);

		/* Only specialise norms that are represented by
		 * an active norm, whether itself or a parent norm */
		if(this.normativeNetwork.isRepresented(norm)) {

			/* Specialise down all parent norms */
			List<Norm> parents = this.normativeNetwork.getParents(norm);
			for(Norm parent : parents) {
				this.specialiseDown(parent);
			}

			/* If the norm has no children, we simply deactivate it */
			if(this.normativeNetwork.isLeaf(norm)) {
				operators.deactivate(normativeNetwork, norm);
			}
			/* If the norm has children, specialise into all of them */
			else {
				operators.specialise(normativeNetwork, norm, children);
			}

			/* Check children utility to specialise them or not */
			for(Norm child : children) {
				if(this.normativeNetwork.getState(child) == NetworkNodeState.ACTIVE &&
						this.isUnderperforming(child))
				{
					this.specialiseDown(child);
				}
			}
		}
	}

	/**
	 * Returns <tt>true</tt> if the norm is under performing
	 * 
	 * @param norm - the norm
	 * @return <tt>true</tt> if the norm is under performing
	 */
	private boolean isUnderperforming(Norm norm) {
		for(Dimension dim : this.nsm.getNormEvaluationDimensions()) 	{
			for(Goal goal : this.nsmSettings.getSystemGoals()) {
				
				Utility utility = this.normativeNetwork.getUtility(norm);
				float topBoundary = utility.getPerformanceRange(dim, goal).getCurrentTopBoundary();
				float satDegree = this.nsmSettings.getSpecialisationBoundary(dim, goal);

				if(topBoundary < satDegree) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 
	 * @param norm
	 * @return
	 */
	private boolean hasGeneralisationUtility(Norm norm, NormativeNetwork nNetwork) {
		for(Dimension dim : this.nsm.getNormEvaluationDimensions())	 {
			for(Goal goal : this.nsmSettings.getSystemGoals()) {
				float satDegree = this.nsmSettings.getGeneralisationBoundary(dim, goal);
				Utility utility = nNetwork.getUtility(norm);

				return utility.getScore(Dimension.Effectiveness, goal) >= satDegree;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param nNetwork
	 * @param norm
	 * @return
	 */
	private List<PotentialGeneralisation> validGeneralisations(NormativeNetwork nNetwork, Norm norm) {
		List<PotentialGeneralisation> potGens = this.genTrees.get(norm);
		List<PotentialGeneralisation> validGens = new ArrayList<PotentialGeneralisation>();
		
		for(PotentialGeneralisation potGen : potGens) {
			
			/* Only perform the generalisation if it has not been performed */
			if(!potGen.isPerformed()) {
				if(this.isValid(potGen, nNetwork)) {
					potGen.setPerformed(true);
					validGens.add(potGen);
				}	
			}
		}
		return validGens;
	}

	/**
	 * 
	 * @param candGen
	 * @param nNetwork
	 * 
	 * @return
	 */
	private boolean isValid(PotentialGeneralisation candGen, NormativeNetwork nNetwork) {
		for(Norm norm : candGen.getChildren()) {
			SetOfPredicatesWithTerms precond = norm.getPrecondition();
			NormModality modality = norm.getModality();
			AgentAction action = norm.getAction();

			// If child does not exist -> Not valid
			if(!nNetwork.contains(precond, modality, action))
				return false;

			Norm childNorm = nNetwork.getNorm(precond, modality, action);

			// If child exists but it is not covered -> Not valid
			if(!nNetwork.isRepresented(childNorm)){
				return false;
			}

			// If child exists but has not enough utility -> Not valid
			if(!this.hasGeneralisationUtility(childNorm, nNetwork)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @param norm
	 */
	public void normCreated(Norm norm) {
		this.createdNorms.add(norm);
		this.normAdditions.add(norm);
	}

	/**
	 * 
	 * @param norm
	 */
	public void normActivated(Norm norm) {
		this.activatedNorms.add(norm);
		this.normAdditions.add(norm);
	}

	/**
	 * 
	 * @param norm
	 */
	public void normDeactivated(Norm norm) {
		this.normDeactivations.add(norm);
	}

	/**
	 * 
	 * @return
	 */
	public boolean hasNonRegulatedConflictsThisTick() {
		return this.hasNonRegulatedConflictsThisTick;
	}
	
	/**
	 * 
	 * @return
	 */
	public GeneralisationTrees getGeneralisationTrees() {
		return this.genTrees;
	}

	/**
	 * 
	 */
  @Override
  public void newNonRegulatedConflictsSolvedThisTick() {
	  this.hasNonRegulatedConflictsThisTick = true;
  }
}
