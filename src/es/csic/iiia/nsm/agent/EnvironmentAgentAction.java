package es.csic.iiia.nsm.agent;

/**
 * An action available to the environment agents. An agent
 * action may be whatever that can be performed by the agents.
 * As an example, within a traffic scenario, some examples of action
 * are "Go forward", "Stop", "Turn left", or "Turn right".
 *  
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
public interface EnvironmentAgentAction {

	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------
	
	/**
	 * Returns a {@code String} with the name of the action
	 * 
	 * @return a {@code String} with the name of the action
	 */
	public String toString();
}
