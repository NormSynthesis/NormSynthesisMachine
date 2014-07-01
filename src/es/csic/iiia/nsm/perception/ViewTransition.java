package es.csic.iiia.nsm.perception;

import java.util.HashMap;
import java.util.Map;

/**
 * A view transition describes the evolution of a part of the system scenario 
 * along a period of time. It consists in a list of views ({@code View}),
 * where each view corresponds to a given time step.
 * As an example, a view transition composed of three views contains
 * information about the evolution of a part of the scenario
 * during three consecutive time steps
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
public class ViewTransition {

	//---------------------------------------------------------------------------
	// Attributes 
	//---------------------------------------------------------------------------

	private Sensor sensor;							// The sensor that perceived the view transition
	private Map<Integer, View> views;		// Time steps (t-n, ..., t) and their views
	
	//---------------------------------------------------------------------------
	// Methods 
	//---------------------------------------------------------------------------
	
	/**
	 * Constructor with sensor 
	 * 
	 *  @param sensor the sensor that perceived the view stream
	 */
	public ViewTransition(Sensor sensor) {
		this.sensor = sensor;
		this.views = new HashMap<Integer, View>();
	}

	/**
	 * Returns the <tt>View<tt> that corresponds to a particular <tt>timestep<tt>
	 * 
	 * @param timestep the time step of the view to get
	 * @return the view that corresponds to the given time step
	 */
	public View getView(int timestep) {
		return this.views.get(timestep);
	}
	
	/**
	 * Sets a view for a particular time step. The value 0 is the final 
	 * time step. Previous steps are n (t-n), n+1, (t-n+1) and so on until t
	 * 
	 * @param timestep - the time step
	 * @param view - the view that corresponds to the given time step
	 */
	public void setView(int timestep, View view) {
		this.views.put(timestep, view);
	}
	
	/**
	 * Returns the number of time steps that the stream has information about
	 * 
	 * @return the number of time steps 
	 */
	public int getNumTimeSteps() {
		return this.views.keySet().size();
	}
	
	/**
	 * Returns the sensor that perceives the view
	 * 
	 * @return the sensor that perceives the view
	 */
	public Sensor getSensor() {
		return sensor;
	}

	/**
	 * Removes the views inside the view stream
	 */
	public void clear() {
		this.views.clear();
	}	
}
