package es.csic.iiia.nsm.perception;

import java.util.List;

/**
 * A view is a subset of a system state at a given time t. For instance,
 * in a traffic scenario a view may be a description of the traffic
 * situation in a part of the road, at time t. 
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 *
 */
public interface View {

	/**
	 * Returns a {@code List} with the id's of the agents in the view
	 * 
	 * @return a {@code List} with the id's of the agents in the view
	 */
	public List<Long> getAgentIds();
	
	/**
	 * Returns a float describing how similar this view is
	 * to the {@code otherView}
	 * 
	 * @param otherView the view to compare with this view
	 * @return a float describing how similar this view is
	 * 					to the {@code otherView}
	 */
	public float getSimilarity(View otherView);
}
