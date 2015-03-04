package es.csic.iiia.nsm.config;

import java.util.List;

import es.csic.iiia.nsm.NormSynthesisMachine.NormGeneralisationMode;

/**
 * Basic settings of the Norm Synthesis Machine. For instance,
 * the default utility of norms, or the number of ticks of stability
 * for the NSM to converge
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
public interface NormSynthesisSettings {

	/**
	 * Returns the mode of the norm synthesis (CUSTOM, IRON, SIMON, LION)
	 * 
	 * @return the mode of the norm synthesis (CUSTOM, IRON, SIMON, LION)
	 */
	public String getNormSynthesisStrategy();
		
	/* Goals and dimensions to synthesise and evaluate norms */
	
	/**
	 * The goals of the system
	 * 
	 * @return a {@code List} containing the system goals
	 * @see Goal
	 */
	public List<Goal> getSystemGoals();
	
	/* Norm generation settings */
	
	/**
	 * Returns true if the norm generation stage must be extremely
	 * reactive to conflicts, namely if norms must be generated
	 * from a single conflict. Otherwise, it returns false. 
	 * 
	 * @return 	true if the norm generation stage must be extremely
	 * 					reactive to conflicts, namely if norms must be generated
	 * 					from a single conflict. Otherwise, it returns false.
	 */
	public boolean isNormGenerationReactiveToConflicts();
	
	/* Norm evaluation settings */
	
	/**
	 * The default utility when norms are created. Typically, it is set to 0.5  
	 * 
	 * @return the default norms' utility
	 */
	public float getNormsDefaultUtility();
	
	/**
	 * The learning rate (alpha) to compute rewards for norm evaluation.
	 * Typically, it is set to 0.1 or 0.2
	 * 
	 * @return the norm evaluation learning rate (alpha parameter)
	 */
	public float getNormEvaluationLearningRate();
	
	/**
	 * Returns the size of the window to compute norms' performance ranges
	 * 
	 * @return the size of the window to compute norms' performance ranges
	 */
	public int getNormsPerformanceRangesSize();
	
	/* Norm refinement settings */
	
	/**
	 * Returns the norm generalisation mode (Shallow/Deep) to be applied
	 * during the norm refinement phase
	 * 
	 * @return 	the norm generalisation mode (Shallow/Deep) to be applied
	 * 					during the norm refinement phase
	 * 
	 * @see NormGeneralisationMode
	 */
	public NormGeneralisationMode getNormGeneralisationMode();
	
	/**
	 * Returns the norm generalisation step (namely, the number of norm 
	 * predicates that can be generalised simultaneously) to be used during
	 * the norm refinement phase 
	 * 
	 * @return 	the norm generalisation step (namely, the number of norm 
	 * 					predicates that can be generalised simultaneously) to be 
	 * 					used during the norm refinement phase
	 */
	public int getNormGeneralisationStep();

	/**
	 * Returns the boundary under which a norm's utility is
	 * considered low enough to specialise the norm 
	 * 
	 * @param dim the dimension of the utility (effectiveness/necessity)
	 * @param goal the system goal of the utility
	 * @return the specialisation boundary
	 * @see Dimension
	 * @see Goal
	 * @see NormGroupUtility
	 */
	public float getGeneralisationBoundary(Dimension dim, Goal goal);
	
	/**
	 * Returns the boundary over which a norm's utility is
	 * considered high enough to be generalised with other norms 
	 * 
	 * @param dim the dimension of the utility (effectiveness/necessity)
	 * @param goal the goal of the utility
	 * @return the generalisation boundary
	 * @see Dimension
	 * @see Goal
	 * @see NormGroupUtility
	 */
	public float getSpecialisationBoundary(Dimension dim, Goal goal);
	
	/**
	 * Returns the boundary over which a norm's utility is
	 * considered high enough to be generalised with other norms 
	 * 
	 * @param dim the dimension of the utility (effectiveness/necessity)
	 * @param goal the goal of the utility
	 * @return the generalisation boundary
	 * @see Dimension
	 * @see Goal
	 * @see NormGroupUtility
	 */
	public float getSpecialisationBoundaryEpsilon(Dimension dim, Goal goal);
	
	/* Convergence criterion */
	
	/**
	 * Returns the number of ticks to consider that the norm synthesis machine 
	 * has converged to a normative system. It is considered to have converged
	 * whenever, during N ticks: 
	 * 
	 * (1) the system remains with no conflicts; and
	 * (2) the normative system remains without changes.
	 * 
	 * @return the number of ticks of stability
	 */
	public long getNumTicksOfStabilityForConvergence();
	

	/**
	 * 
	 * @return
	 */
	public int getMinEvaluationsToClassifyNorms();
		
	/**
	 * 
	 * @return
	 */
	public int getMinEvaluationsToClassifyNormGroups();
	
}
