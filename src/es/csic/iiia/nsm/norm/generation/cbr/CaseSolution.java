package es.csic.iiia.nsm.norm.generation.cbr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.csic.iiia.nsm.agent.AgentAction;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.perception.View;

/**
 * A case solution is a solution aimed to solve a conflicting situation
 * described in the {@code CaseDescription} of a {@code Case}.
 * In particular, a case solution describes what actions are 
 * prohibited and obligated for some agents in certain views.
 * For instance, a very simple case solution would contain a single 
 * {@code View}, and an agent with id {@code agentId} which is prohibited
 * to perform a single action in that {@code View}
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 * @see Case
 * @see CaseDescription
 * @see View
 */
public class CaseSolution {
	
	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------
	
	private Map<View, Map<Long, AgentAction>> prohibitedActions;
	private Map<View, Map<Long, AgentAction>> obligatedActions;
	private List<Norm> norms; 
	
	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	public CaseSolution() {
		this.prohibitedActions = new HashMap<View, Map<Long, AgentAction>>();
		this.obligatedActions = new HashMap<View, Map<Long, AgentAction>>();
		this.norms = new ArrayList<Norm>();
	}
	
	/**
	 * Adds an {@code action} that the agent with id {@code agentId} is obligated
	 * to perform in the situation described by the {@code view}
	 * 
	 * @param 	view the view in which the agent is obligated
	 * 					to perform the {@code action}
	 * @param 	agentId the id of the agent that is obligated to perform
	 * 					the action in the situation described by the given {@code view}
	 * @param 	action the action that the agent is obligated to perform
	 * 					in the given {@code view}
	 */
	public void addObligatedAction(View view, Long agentId, AgentAction action) {
		if(!this.obligatedActions.containsKey(view)) {
			this.obligatedActions.put(view, new HashMap<Long, AgentAction>());
		}
		this.obligatedActions.get(view).put(agentId, action);
	}	
	
	/**
	 * Adds an {@code action} that the agent with id {@code agentId} is prohibited
	 * to perform in the situation described by the {@code view}
	 * 
	 * @param 	view the view in which the agent is prohibited
	 * 					to perform the {@code action}
	 * @param 	agentId the id of the agent that is prohibited to perform
	 * 					the action in the situation described by the given {@code view}
	 * @param 	action the action that the agent is prohibited to perform
	 * 					in the given {@code view}
	 */
	public void addProhibitedAction(View view, Long agentId, AgentAction action) {
		if(!this.prohibitedActions.containsKey(view)) {
			this.prohibitedActions.put(view, new HashMap<Long, AgentAction>());
		}
		this.prohibitedActions.get(view).put(agentId, action);
	}
	
	/**
	 * Returns a {@code Map} that contains all the agents that have an obligated
	 * action in the given {@code view}, together with the {@code action} they
	 * are prohibited to perform
	 * 
	 * @return a {@code Map} that contains all the agents that have a prohibited
	 * 					action in the given {@code view}, together with the
	 * 					{@code action} they are prohibited to perform
	 */
	public Map<Long, AgentAction> getObligatedActions(View view) {
		return this.obligatedActions.get(view);
	}
	
	/**
	 * Returns a {@code Map} that contains all the agents that have a prohibited
	 * action in the given {@code view}, together with the {@code action} they
	 * are prohibited to perform
	 * 
	 * @return a {@code Map} that contains all the agents that have a prohibited
	 * 					action in the given {@code view}, together with the
	 * 					{@code action} they are prohibited to perform
	 */
	public Map<Long, AgentAction> getProhibitedActions(View view) {
		return this.prohibitedActions.get(view);
	}

	/**
	 * Adds a norm resulting from translating this case solution to norms
	 * that can be understood by the environment agents
	 * 
	 * @param norm the norm to add
	 * @see Norm
	 */
	public void addNorm(Norm norm) {
		this.norms.add(norm);
	}
	
	/**
	 * Returns the {@code List} of norms resulting from translating this case
	 * solution to norms that can be understood by the environment agents
	 * 
	 * @return the {@code List} of norms resulting from translating this case
	 * 					solution to norms that can be understood by the environment
	 * 					agents
	 * @see Norm
	 */
	public List<Norm> getNorms() {
		return norms;
	}	
	
	/**
	 * Returns the score of this case solution
	 * 
	 * @return the score of this case solution
	 */
	public float getScore() {
		return 0f;
	}
}
