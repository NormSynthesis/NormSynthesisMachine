package es.csic.iiia.nsm.strategy.iron;

import es.csic.iiia.nsm.config.Dimension;
import es.csic.iiia.nsm.config.Goal;
import es.csic.iiia.nsm.metrics.NormSynthesisMetrics;
import es.csic.iiia.nsm.net.norm.NormativeNetwork;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.evaluation.NormComplianceOutcomes;

/**
 * An utility function evaluates norms in terms of some system dimensions
 * and goals. Norms are evaluated by using information about their outcomes
 * in the system, specifically in terms of their effectiveness and necessity
 * by means of RL formulas and computing rewards
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
public class IRONUtilityFunction  {

//	private NormSynthesisMetrics nsMetrics;
	
	//---------------------------------------------------------------------------
	// Methods 
	//---------------------------------------------------------------------------
	
	/**
	 * 
	 * @param nsMetrics
	 */
	public IRONUtilityFunction(NormSynthesisMetrics nsMetrics) {
//		this.nsMetrics = nsMetrics;
	}
	
	/**
	 * Evaluates the norms that have been complied and infringed during the current
	 * time step, in terms of a dimension and a goal. It evaluates norms based on
	 * the outcomes of their compliances and infringements. For more information,
	 * see the documentation of {@code Dimension}
	 * 
	 * @param 	dim the dimension that the norm is evaluated in terms of
	 * @param 	goal the goal that the norm is evaluated in terms of
	 * @param 	nCompliance the object that contains information about the norms
	 * 					compliances and infringements during the current time step, and the
	 * 					outcomes of those compliances and infringements (conflict/no conflict)
	 * @param 	nNetwork the normative network

	 * @see Dimension
	 * @see Goal
	 */
	public void evaluate(Dimension dim, Goal goal, 
			NormComplianceOutcomes nCompliance, NormativeNetwork nNetwork) {
		
		float oldScore, score, reward;

		switch(dim) {
		
		/* Evaluate norms' effectiveness based on its compliances */
		case Effectiveness:

			for(Norm appNorm : nCompliance.getFulfilledNorms()) {
				int nAC = nCompliance.getNumFulfilmentsWithConflict(appNorm);
				int nANoC = nCompliance.getNumFulfilmentsWithNoConflict(appNorm);

				if(nNetwork.getUtility(appNorm) == null) {
					break;
				}

				oldScore = nNetwork.getUtility(appNorm).getScore(dim, goal);
				reward = (float) nANoC / (nAC + nANoC); 
				score = (float) (oldScore + 0.1 * (reward - oldScore));
				nNetwork.setScore(appNorm, dim, goal, score);
				
//				/* Update complexities metrics */
//				this.nsMetrics.incNumNodesVisited();
			}
			break;

		/* Evaluate norms' necessity based on its infringements */
		case Necessity:

			for(Norm violNorm : nCompliance.getInfringedNorms()) {
				
				int nVC = nCompliance.getNumInfringementsWithConflict(violNorm);
				int nVNoC = nCompliance.getNumInfrsWithNoConflict(violNorm);	

				oldScore = nNetwork.getUtility(violNorm).getScore(dim, goal);
				reward = (float) nVC / (nVC + nVNoC); 
				score = (float) (oldScore + 0.1 * (reward - oldScore));

				nNetwork.setScore(violNorm, dim, goal, score);
				
//				/* Update complexities metrics */
//				this.nsMetrics.incNumNodesVisited();
			}
			break;
		}
	}
}
