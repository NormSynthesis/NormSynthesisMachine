package es.csic.iiia.nsm.perception;

import java.util.ArrayList;
import java.util.List;

/**
 * A monitor is employed by the Norm Synthesis Machine to perceive the scenario
 * of the system it synthesises norms for. The monitor incorporates a set
 * of sensors that perceive the scenario in a distributed manner.
 *  
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 * @see Sensor
 */
public class Monitor {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------

	private List<Sensor> sensors;	// The sensors of the monitor

	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------

	/**
	 * Constructor 
	 */
	public Monitor() {
		this.sensors = new ArrayList<Sensor>();
	}

	/**
	 * Adds a sensor to the monitor
	 * 
	 * @param sensor the sensor to add
	 */
	public void addSensor(Sensor sensor) {
		this.sensors.add(sensor);
	}

	/**
	 * Returns a {@code List} with the perceptions ({@code ViewTransition})
	 * of each {@code Sensor} in the {@code Monitor}
	 * 
	 * @return a {@code List} with the perceptions ({@code ViewTransition})
	 * of each {@code Sensor} in the {@code Monitor} 
	 */
	public void getPerceptions(List<ViewTransition> viewTransitions)	{
		viewTransitions.clear();
		
		for(Sensor sensor : this.sensors) {
			viewTransitions.add(sensor.getPerception());
		}
	}

	/**
	 * Returns the number of sensors of the monitor
	 * 
	 * @return the number of sensors of the monitor
	 */
	public int getNumSensors() {
		return this.sensors.size();
	}
}
