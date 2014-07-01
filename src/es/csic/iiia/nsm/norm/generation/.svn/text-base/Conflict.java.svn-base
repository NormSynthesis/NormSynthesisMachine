package es.csic.iiia.nsm.norm.generation;

import java.util.List;

import es.csic.iiia.nsm.perception.Sensor;
import es.csic.iiia.nsm.perception.View;
import es.csic.iiia.nsm.perception.ViewTransition;

/**
 * A conflict describes a situation where several agents in the system
 * are in conflict. For instance, in a traffic scenario a conflict may
 * be two cars that at a given time step where in different locations, 
 * but in a posterior time step they are in the same position
 * (they have collided).
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
public class Conflict {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------
	
	private Sensor sensor;	// The sensor that perceived the conflict
	private View conflict;	// The view in which the conflict has been perceived
	private ViewTransition conflictSource;	// The views previous to the conflict
	private List<Long> conflictingAgents;		// The agents that are in conflict 
	
	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------
	
	/**
	 * Constructor 
	 */
	public Conflict(Sensor sensor, View conflict, ViewTransition conflictSource, 
			List<Long> conflictingAgents) {
		
		this.sensor = sensor;
		this.conflict = conflict;
		this.conflictSource = conflictSource;
		this.conflictingAgents = conflictingAgents;
	}
	
	/**
	 * Returns the sensor that perceived the conflict
	 * 
	 * @return the sensor that perceived the conflict
	 * @see Sensor
	 */
	public Sensor getSensor() {
		return this.sensor;
	}
	
	/**
	 * Returns the view in which the conflict has been perceived
	 * 
	 * @return the view in which the conflict has been perceived
	 * @see View
	 */
	public View getConflict() {
		return this.conflict;
	}
	
	/**
	 * Returns the views previous to the conflict
	 * 
	 * @return the views previous to the conflict
	 * @see ViewTransition
	 */
	public ViewTransition getConflictSource() {
		return this.conflictSource;
	}

	/**
	 * Returns the agents that are in conflict 
	 * 
	 * @return the agents that are in conflict
	 */
	public List<Long> getConflictingAgents() {
	  return this.conflictingAgents;
  }
}
