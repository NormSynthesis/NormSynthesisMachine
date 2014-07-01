package es.csic.iiia.nsm.agent;

/**
 * An environment agent within the scenario
 *  
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
public interface EnvironmentAgent {

	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------
	
	/**
	 * The id of the environment agent, which must be unique.
	 * That is, the NSM requires that two agents never have the
	 * same id, since the norm synthesis cycle may not work correctly
	 * 
	 * @return a {@code long} number, which is the id of the environment agent
	 */
	public long getId();
}
