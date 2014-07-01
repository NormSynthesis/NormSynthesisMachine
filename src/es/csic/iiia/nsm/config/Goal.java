package es.csic.iiia.nsm.config;

/**
 * The goal of a system. The Norm Synthesis Machine uses
 * system goals to perform conflict detection. The detection of a
 * conflict is always performed based on a system goal. A particular
 * situation may be conflictive from the point of view of a particular
 * goal, while it may not be conflictive with respect to another goal.
 * <p>
 * As an example, consider a traffic scenario where travelling cars stand
 * for agents, and there are two system goals: (1) to avoid collisions
 * between cars, and (2) to avoid traffic jams. Consider now a transition
 * of views in which all the cars remain stopped. From the point of view
 * of goal 1, there is no conflict since there are no collisions. However,
 * from the point of view of goal 2 the situation in the view transition is 
 * conflictive, since all cars remain stopped and it represents a 
 * traffic jam. 
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
public abstract class Goal {

	/**
	 * Returns the name of the goal
	 * 
	 * @return a {@code String} with the goal's name
	 */
	public String toString() {
		return this.getName();
	}

	/**
	 * Returns the name of the goal
	 * 
	 * @return a {@code String} with the goal's name
	 */
	public abstract String getName();

	/**
	 * Returns the description of the goal
	 *  
	 * @return a {@code String} with the goal's description
	 */
	public abstract String getDescription();

}
