package es.csic.iiia.nsm.norm.evaluation;

import java.util.HashMap;
import java.util.List;

import es.csic.iiia.nsm.config.Dimension;
import es.csic.iiia.nsm.config.Goal;

/**
 * The utility of a norm, that may have utility with respect to several
 * dimensions and goals, and hence may be evaluated in terms of several
 * dimensions and goals
 *  
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 * @see Dimension
 * @see Goal
 */
public class Utility {

	//---------------------------------------------------------------------------
	// Attributes 
	//---------------------------------------------------------------------------

	private float defaultUtility;
	private int perfRangeSize;

	private List<Dimension> dimensions;
	private List<Goal> goals;

	private HashMap<Dimension, HashMap<Goal, Float>> scores;
	private HashMap<Dimension,	HashMap<Goal, PerformanceRange>> perfRanges;

	//---------------------------------------------------------------------------
	// Methods 
	//---------------------------------------------------------------------------

	/**
	 * Constructor
	 * 
	 * @param dimensions the norm evaluation dimensions
	 * @param goals the system goals
	 */
	public Utility(float defaultUtility, int perfRangeSize) {
		this.defaultUtility = defaultUtility;
		this.perfRangeSize = perfRangeSize;

		this.scores = new HashMap<Dimension, HashMap<Goal, Float>>();	
		this.perfRanges = new HashMap<Dimension,
				HashMap<Goal, PerformanceRange>>();
	}

	/**
	 * Constructor with dimensions and goals
	 * 
	 * @param dimensions the norm evaluation dimensions
	 * @param goals the system goals
	 */
	public Utility(float defaultUtility, int perfRangeSize,
			List<Dimension> dimensions, List<Goal> goals) {

		this(defaultUtility, perfRangeSize);
		this.dimensions = dimensions;
		this.goals = goals;

		this.reset();
	}

	/**
	 * Initialises the utility, setting all the scores
	 * to the norms' utility default value 
	 * 
	 * @see Dimension
	 * @see Goal
	 */
	public void reset() {
		for(Dimension dim : dimensions) {
			HashMap<Goal, Float> dimScores = new HashMap<Goal, Float>();
			HashMap<Goal, PerformanceRange> dimPerformances =
					new HashMap<Goal, PerformanceRange>();

			this.scores.put(dim, dimScores);
			this.perfRanges.put(dim, dimPerformances);

			for(Goal goal : goals) {
				PerformanceRange perfRange = 
						new PerformanceRange(perfRangeSize);
				perfRange.addValue(defaultUtility);

				this.scores.get(dim).put(goal, defaultUtility);
				this.perfRanges.get(dim).put(goal, perfRange);
			}
		}
	}

	/**
	 * Returns the score of the utility for a given dimension/goal
	 * 
	 * @param dim the norm evaluation dimension
	 * @param goal the system goal
	 * @return the score for the given dimension/goal
	 * @see Dimension
	 * @see Goal
	 */
	public float getScore(Dimension dim, Goal goal) {
		return this.scores.get(dim).get(goal);
	}

	/**
	 * Sets the score of the utility for a given dimension/goal
	 * 
	 * @param dim the norm evaluation dimension 
	 * @param goal the system goal
	 * @param score the new score
	 * @see Dimension
	 * @see Goal
	 */
	public void setScore(Dimension dim, Goal goal, float score) {
		this.scores.get(dim).put(goal, score);
		this.perfRanges.get(dim).get(goal).addValue(score);
	}

	/**
	 * Returns the average score of the utility for a
	 * given dimension/goal for a period of time
	 * 
	 * @param dim the norm evaluation dimension
	 * @param goal the system goal
	 * @return the average score for the given dimension/goal for
	 * 					the period of time given by the method
	 * 					{@code NormSynthesisSettings.getNormsPerformanceRangesSize()}
	 */
	public float getScoreAverage(Dimension dim, Goal goal) {
		return this.perfRanges.get(dim).get(goal).getCurrentAverage();
	}
	
	/**
	 * Returns the score window used to compute the performance
	 * range of the norm during a period of time, in terms of a 
	 * dimension/goal
	 * 
	 * @param dim the norm evaluation dimension
	 * @param goal the system goal
	 * 
	 * @return the score window 
	 */
	public PerformanceRange getPerformanceRange(Dimension dim, Goal goal) {
		return this.perfRanges.get(dim).get(goal);
	}
}
