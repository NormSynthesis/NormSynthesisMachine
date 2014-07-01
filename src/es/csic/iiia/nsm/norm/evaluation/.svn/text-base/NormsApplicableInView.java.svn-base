package es.csic.iiia.nsm.norm.evaluation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.csic.iiia.nsm.perception.ViewTransition;

/**
 * Norms that apply to each agent within a particular transition of views
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 * @see NormsApplicableToAgentContext
 * @see ViewTransition
 */
public class NormsApplicableInView {

	//---------------------------------------------------------------------------
	// Attributes 
	//---------------------------------------------------------------------------
		
	/* Agents with their applicable norms at the view in time t-1 */
	private Map<Long, NormsApplicableToAgentContext> normsApplicableToAgents;
	
	/* The view transition */
	private ViewTransition viewTransition;
	
	//---------------------------------------------------------------------------
	// Methods 
	//---------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	public NormsApplicableInView() {
		this.normsApplicableToAgents = 
				new HashMap<Long, NormsApplicableToAgentContext> ();
	}
	
	/**
	 * Adds into {@code applNorms} the norms that apply to
	 * the agent with id {@code agentId}
	 * 
	 * @param agentId the id of the agent
	 * @param norms the norms that apply to the agent
	 */
	public void add(Long agentId, NormsApplicableToAgentContext applNorms) {
		this.normsApplicableToAgents.put(agentId, applNorms);
	}
	
	/**
	 * Returns the list of agents to which norms apply
	 * 
	 * @return the list of agent id's
	 */
	public List<Long> getAgentIds() {
		List<Long> agents = new ArrayList<Long>();
		agents.addAll(this.normsApplicableToAgents.keySet());
		
		return agents;
	}
	
	/**
	 * Returns the norms that apply to an agent with id {@code agentId}
	 * 
	 * @param agentId the id of the agent
	 * @return the list of norms that apply to the agent
	 */
	public NormsApplicableToAgentContext get(Long agentId) {
		if(!this.normsApplicableToAgents.containsKey(agentId))	{
			return new NormsApplicableToAgentContext();
		}
		return this.normsApplicableToAgents.get(agentId);
	}
		
	/**
	 * Clears the list of agents and the norms that apply to them
	 */
	public void clear() {
		this.normsApplicableToAgents.clear();
	}
	
	/**
	 * Returns the corresponding view transition
	 * 
	 * @return the view transition
	 * @see ViewTransition
	 */
	public ViewTransition getViewTransition() {
		return this.viewTransition;
	}
	
	/**
	 * Sets the transition of views in which some norms
	 * applied to some agents
	 * 
	 * @param viewTransition the transition of views
	 * @see ViewTransition
	 */
	public void setViewTransition(ViewTransition viewTransition)	{
		this.viewTransition = viewTransition;
	}
	
	/**
	 * Returns a description of the norm applicability
	 * 
	 * @return the description of the norm applicability
	 */
	public String toString() {
		return this.normsApplicableToAgents.toString();
	}
	
	/**
	 * Returns <tt>true</tt> if no applicable norm has been added yet
	 * 
	 * @return <tt>true</tt> if no norm has been added to the map
	 */
	public boolean isEmpty(){ 
		return this.normsApplicableToAgents.isEmpty();
	} 
}
