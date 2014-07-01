package es.csic.iiia.nsm.perception;

/**
 * A sensor that perceives the scenario
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 *
 */
public interface Sensor {

	/**
	 * Returns the id of the sensor
	 * 
	 * @return the id of the sensor
	 */
	public int getId();
	
	/**
	 * Gives a description about where the sensor is placed
	 * 
	 * @return the location of the sensor
	 */
	public String getLocation();
	
	/**
	 * Sets the number of time steps of the window.
	 * For instance, for a window with t-2, t-1, t,
	 * you should set a perception window of 3 time steps 
	 * 
	 * @param ticks the number of time steps of the window
	 */
	public void setPerceptionWindow(int ticks);
	
	/**
	 * Returns the last perception performed by the sensor, which
	 * consists of a {@code ViewTransition} that contains as many views as
	 * it was set in the perception window. For instance, a window 
	 * of 3 time steps will perceive view streams of 3 views (one
	 * (for each time step) 
	 * 
	 * @return a {@code ViewTransition} that contains as many views as
	 * 					it was set in the perception window. For instance, a window 
	 * 					of 3 time steps will perceive view streams of 3 views (one
	 * 					(for each time step) 
	 */
	public ViewTransition getPerception();
}
