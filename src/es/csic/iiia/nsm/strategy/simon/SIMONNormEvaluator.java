/**
 * 
 * @author Javier Morales (jmorales@iiia.csic.es)
 */
package es.csic.iiia.nsm.strategy.simon;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.csic.iiia.nsm.agent.EnvironmentAgentAction;
import es.csic.iiia.nsm.agent.language.SetOfPredicatesWithTerms;
import es.csic.iiia.nsm.config.Dimension;
import es.csic.iiia.nsm.config.DomainFunctions;
import es.csic.iiia.nsm.config.Goal;
import es.csic.iiia.nsm.config.NormSynthesisSettings;
import es.csic.iiia.nsm.metrics.NormSynthesisMetrics;
import es.csic.iiia.nsm.net.norm.NormativeNetwork;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.NormModality;
import es.csic.iiia.nsm.norm.evaluation.NormComplianceOutcomes;
import es.csic.iiia.nsm.norm.evaluation.NormsApplicableInView;
import es.csic.iiia.nsm.norm.group.evaluation.NormGroupOutcomes;
import es.csic.iiia.nsm.norm.reasoning.NormReasoner;
import es.csic.iiia.nsm.perception.ViewTransition;

/**
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 *
 */
public class SIMONNormEvaluator {

	protected List<Dimension> normEvDimensions;
	protected NormSynthesisSettings nsmSettings;
	protected NormReasoner normReasoner;
	protected NormSynthesisMetrics nsMetrics;
	protected DomainFunctions dmFunctions;
	protected NormativeNetwork normativeNetwork;
	protected SIMONUtilityFunction utilityFunction;
	protected SIMONOperators operators;
	
	protected Map<Norm, List<SetOfPredicatesWithTerms>> negRewardedNorms;
	
	/**
	 * 
	 */
	public SIMONNormEvaluator(List<Dimension> normEvDimensions, 
			NormSynthesisSettings nsmSettings, DomainFunctions dmFunctions,
			NormativeNetwork normativeNetwork, NormReasoner normReasoner,
			NormSynthesisMetrics nsMetrics, SIMONUtilityFunction utilityFunction,
			SIMONOperators operators) {
		
		this.normEvDimensions = normEvDimensions;
		this.nsmSettings = nsmSettings;
		this.dmFunctions = dmFunctions;
		this.normativeNetwork = normativeNetwork;
		this.utilityFunction = utilityFunction;
		this.normReasoner = normReasoner;
		this.operators = operators;
		this.nsMetrics = nsMetrics;
		
		this.negRewardedNorms = new HashMap<Norm, 
				List<SetOfPredicatesWithTerms>>();
	}
	
	/**
	 * @param normCompliance 
	 * @param normGroupCompliance 
	 * 
	 */
	public void step(List<ViewTransition> viewTransitions, 
			Map<ViewTransition, NormsApplicableInView> normApplicability, 
			Map<Goal, Map<ViewTransition, NormComplianceOutcomes>> normCompliance, 
			Map<Goal, NormGroupOutcomes> normGroupCompliance) {
		
		/* Compute norm applicability */
		this.normApplicability(viewTransitions, normApplicability);

		/* Detect norms and norm groups compliance */
		this.normCompliance(normApplicability, normCompliance);

		/* Update utilities and performances of norms and norm groups */
		this.updateUtilitiesAndPerformances(normCompliance);
	}
	
	/**
	 * Computes the norm applicability perceived by each sensor.
	 * (recall that each view transition is perceived by a particular sensor)
	 * 
	 * @param vTransitions the list of perceptions of each sensor
	 * @return a map containing the norms that are applicable to each
	 * agent in each view transition
	 */
	protected void normApplicability(List<ViewTransition> vTransitions, 
			Map<ViewTransition, NormsApplicableInView> normApplicability)	{

		/* Clear norm applicability from previous tick */
		normApplicability.clear();

		/* Get applicable norms of each viewTransition (of each sensor) */
		for(ViewTransition vTrans : vTransitions) {
			NormsApplicableInView normsAppInView;
			normsAppInView = this.normReasoner.getNormsApplicable(vTrans);
			normApplicability.put(vTrans, normsAppInView);
		}
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
			NormsApplicableInView> normApplicability,
			Map<Goal, Map<ViewTransition, NormComplianceOutcomes>> normCompliance) {

		/* Check norm compliance in the view in terms of each system goal */
		for(Goal goal : this.nsmSettings.getSystemGoals()) {

			/* Clear norm compliance of previous tick */
			normCompliance.get(goal).clear();

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

				normCompliance.get(goal).put(vTrans, nCompliance);
			}
		}
	}

	//---------------------------------------------------------------------------
	// Private methods
	//---------------------------------------------------------------------------

	/**
	 * Updates norm utilities and performances based on
	 * their norm compliance in the current time step
	 * 
	 * @param normCompliance the norm compliance in the current time step
	 */
	protected void	updateUtilitiesAndPerformances(Map<Goal, 
			Map<ViewTransition,NormComplianceOutcomes>> normCompliance) {

		/* On the one hands, we evaluate norms in terms of several goals
		 * and dimensions. On the other hand, norm groups are only evaluated
		 * in terms of system goals and their "Effectiveness" */
		for(Goal goal : this.nsmSettings.getSystemGoals()) {
			for(Dimension dim : this.normEvDimensions)	{
				for(ViewTransition vTrans : normCompliance.get(goal).keySet()) {

					/* Evaluate norms and retrieve a list of negatively rewarded norms */
					this.negRewardedNorms = this.utilityFunction.evaluate(dim, goal, 
							normCompliance.get(goal).get(vTrans), normativeNetwork);
					
					/* Add norms that have been rewarded with a negative value */
					this.addNegRewardedNorms(negRewardedNorms);
				}
			}
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
				
				/* The norm does not exist -> add it to the normative network */
				if(!exists) {
					Norm specNorm = new Norm(precond, mod, action);
					this.operators.add(specNorm);
					this.operators.activate(specNorm);
				}
			}
		}
	}
}
