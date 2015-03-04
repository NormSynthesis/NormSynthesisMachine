package es.csic.iiia.nsm.config;

import java.util.List;

import javax.swing.JPanel;

import es.csic.iiia.nsm.agent.EnvironmentAgentAction;
import es.csic.iiia.nsm.agent.EnvironmentAgentContext;
import es.csic.iiia.nsm.agent.language.SetOfPredicatesWithTerms;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.generation.Conflict;
import es.csic.iiia.nsm.perception.View;
import es.csic.iiia.nsm.perception.ViewTransition;

/**
 * Domain functions that allow the Norm Synthesis Machine (NSM) to perform
 * norm synthesis for a particular domain. Specifically, the norm synthesis
 * cycle requires the following domain functions:
 * 
 * (1)	agents' language functions, that allow to create descriptions
 * 			of the environment by means of an agent's language;
 * (2) 	conflict detection functions, that allow to define what situations
 * 			represent a conflict in the particular domain. 
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 *
 */
public interface DomainFunctions {

	//---------------------------------------------------------------------------
	// Agents' language functions
	//---------------------------------------------------------------------------

	/**
	 * Returns <tt>true</tt> if a given {@code agentContext} description
	 * is consistent, namely the situation it describes "makes sense".
	 * As an example, in a traffic scenario, a description that says
	 * "there is a car in front of me, and an ambulance to my right", 
	 * makes sense, and hence the method should return <tt>true</tt>.
	 * By contrast, a situation like "there is a car in top of me"
	 * does not make sense, and hence the method should return <tt>false</tt> 
	 * <p>
	 * <b>NOTE: In a domain in which any combination of the predicates and terms
	 * would make sense, this method could return always <tt>true</tt>. 
	 * <p>
	 * <b>NOTE 2:</b>	You should implement this method only if you are
	 * going to use the {@code IRON} norm synthesis strategy. Otherwise, 
	 * there is no need that you implement this method, which could
	 * return <tt>false</tt> always.
	 *  
	 * @param agentContext the agent context description 
	 * @return <tt>true</tt> if the set of predicates and terms is 
	 * 					consistent, namely it makes sense. It returns <tt>false</tt>
	 * 					otherwise
	 */
	public boolean isConsistent(SetOfPredicatesWithTerms agentContext);

	/**
	 * Returns the local context of a reference agent with id {@code agentId}
	 * in a given {@code view}. The context of an agent in a view is a
	 * description of the view from the agent's local perspective, in terms
	 * of the agent language. In other words, the agent context describes
	 * how the agent is perceiving the view, by means of its words
	 * 
	 * @param agentId the id of the reference agent
	 * @param view the view in which the agent perceives its local context
	 * @return a {@code SetOfPredicatesWithTerms} that describes 
	 * 						the agent's local context 
	 */
	public EnvironmentAgentContext agentContextFunction(long agentId,
			View view);
	
	/**
	 * Returns the {@code List} of actions that the reference agent
	 * with id {@code agentId} performed in the transition of views
	 * {@code viewTransition}
	 * 
	 * @param 	agentId the id of the reference agent
	 * @param 	viewTransition the transition of views in which
	 * 					the agent performed the actions
	 * @return the {@code List} of actions that the reference agent
	 * 					with id {@code agentId} performed in the transition of views
	 */
	public List<EnvironmentAgentAction> agentActionFunction(long agentId,
			ViewTransition viewTransition);
	
	//---------------------------------------------------------------------------
	// Conflict detection functions
	//---------------------------------------------------------------------------
	
	/**
	 * Returns a {@code List} containing the new, non-regulated conflicts
	 * that have arisen during a transition of views {@code viewTransition}.
	 * That is, those conflicts that have been originated  during the 
	 * {@code viewTransition} by some agents to which no norms
	 * applied before the conflict.
	 * 
	 * @return a {@code List} containing the new, non-regulated conflicts
	 * 					that have arisen during a transition of views
	 * 					{@code viewTransition}.
	 * @see Goal
	 * @see Conflict
	 */
	public List<Conflict> getConflicts(Goal goal,	ViewTransition viewTransition);
	
	/**
	 * Returns a {@code List} containing the new, non-regulated conflicts
	 * that an agent with id {@code agentId} is involved in during a transition
	 * of views {@code viewTransition}. That is, those conflicts that have been
	 * originated  during the {@code viewTransition} by the agent with id
	 * {@code agentId} to which no norms applied before the conflict.
	 * 
	 * @return 	a {@code List} containing the new, non-regulated conflicts
	 * 					that an agent with id {@code agentId} is involved in during
	 * 					a transition of views {@code viewTransition}. That is, those
	 * 					conflicts that have been originated  during the
	 * 					{@code viewTransition} by the agent with id {@code agentId}
	 * 					to which no norms applied before the conflict.
	 * 
	 * @see Goal
	 * @see Conflict
	 */
	public List<Conflict> getConflicts(Goal goal,	ViewTransition viewTransition,
			long agentId);
	
	/**
	 * Returns <tt>true</tt> if the agent with id {@code agentId} is in
	 * conflict in a {@code view} with respect to a system {@code goal}
	 * 
	 * @param view the view in which to check if the agent is in conflict
	 * @param agentId the id of the reference agent 
	 * @param goal the goal 
	 * @return <tt>true</tt> if the agent with id {@code agentId}
	 * 					is in conflict in the view
	 * @see Goal
	 * @see Conflict
	 */
	public boolean hasConflict(View view, long agentId, Goal goal);
	
	/**
	 * 
	 * @param norm
	 * @return
	 */
	public JPanel getNormDescriptionPanel(Norm norm);
}
