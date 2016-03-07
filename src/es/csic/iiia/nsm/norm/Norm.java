package es.csic.iiia.nsm.norm;

import es.csic.iiia.nsm.agent.AgentAction;
import es.csic.iiia.nsm.agent.language.SetOfPredicatesWithTerms;
import es.csic.iiia.nsm.net.norm.NetworkNode;

/**
 * A norm is composed of a precondition and a postcondition.
 * The precondition of the norm is a set of predicates with terms
 * (an object {@code SetOfPredicatesWithTerms}), and the
 * postcondition of the norm is a deontic operator, which is
 * composed of a modality (i.e., a {@code NormModality}) and
 * an action ({@code AgentAction}).
 * <p>
 * Whenever the local context of an agent satisfies the precondition
 * of a norm, then the norm applies to the agent and the postcondition
 * (that is, the deontic operator) applies to it. 
 * <p>
 * As an example, consider a traffic scenario, and a norm which
 * precondition is "I have a car to my left", and which postcondition is
 * "prohibition(Go)". This norm specifies that an agent is prohibited to
 * go whenever it perceives a car to its left. Therefore, if at a particular
 * moment, the context of an agent is "I have a car to my left", then the norm
 * applies to the agent and hence it is prohibited to go.
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 * @see NormModality
 * @see AgentAction
 */
public class Norm implements Comparable<Norm>, NetworkNode {

	//---------------------------------------------------------------------------	
	// Attributes
	//---------------------------------------------------------------------------
	
	private int id; // the id of the norm
	private SetOfPredicatesWithTerms precondition; // the norm precondition
	private NormModality modality; // the modality of the deontic operator
	private AgentAction action; // the action of the deontic operator
//	private Goal goal; // the goal for what the norm was created to
	
	//---------------------------------------------------------------------------	
	// Methods
	//---------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * 
	 * @param precondition the precondition of the norm
	 * @param modality the modality (Prohibition/Obligation)
	 * @param action the action regulated by the norm
	 * @param goal the goal that the norm was generated to regulate 
	 */
	public Norm(SetOfPredicatesWithTerms precondition, NormModality modality, 
			AgentAction action) {
		this.precondition = precondition;
		this.modality = modality;
		this.action = action;
//		this.goal = goal;
	}

	/**
	 * Returns the id of the norm
	 * 
	 * @return the id of the norm
	 */
	public int getId() {
		return this.id;
	}
	
	/**
	 * Sets the new {@code id} of the norm
	 * 
	 * @param id the new norm id
	 */
	public void setId(int id)	{
		this.id = id;
	}
	
	/**
	 * Returns the name of the norm
	 * 
	 * @return the name of the norm
	 */
	public String getName() {
		return "N" + id;
	}
	
	/**
	 * Returns the precondition of the norm
	 * 
	 * @return the precondition of the norm
	 */
	public SetOfPredicatesWithTerms getPrecondition() {
		return this.precondition;
	}
	
	/**
	 * Returns the modality of the deontic operator
	 * 
	 * @return the modality of the deontic operator
	 * @see NormModality
	 */
	public NormModality getModality() {
		return this.modality;
	}

	/**
	 * Returns the action that the norm regulates
	 * 
	 * @return the action that the norm regulates
	 * @see AgentAction
	 */
	public AgentAction getAction() {
		return this.action;
	}

//	/**
//	 * Returns the goal for which the norm was created to
//	 * 
//	 * @return the goal for which the norm was created to
//	 * @see Goal
//	 */
//	public Goal getGoal() {
//		return this.goal;
//	}
	
	/**
	 * Compares the id of this norm to that of {@code otherNorm}
	 * Used to sort norms by id
	 * 
	 * @param otherNorm the other norm to compare this with
	 */
	@Override
	public int compareTo(Norm otherNorm)	{
		if(otherNorm.getId() < this.id) {
			return 1;
		}
		else if(otherNorm.getId() > this.id) {
			return -1;
		}
		return 0;
	}
	
	/**
	 * Returns <tt>true</tt> if the norm equals the {@code otherNorm}.
	 * Two norms are equal if they have equal precondition, 
	 * deontic operator and action
	 * 
	 * @param otherNorm the other norm to compare this with
	 * @return <tt>true</tt> if the norm equals the {@code otherNorm}.
	 */
	public boolean equals(Norm otherNorm) {
		if(!this.precondition.equals(otherNorm.getPrecondition())) {
			return false;
		}
		if(!this.modality.equals(otherNorm.getModality())) {
			return false;
		}
		if(!this.action.equals(otherNorm.getAction())) {
			return false;
		}
		return true;
	}
	
	/**
	 * Returns a description of the norm
	 * 
	 * @return a string describing the norm
	 */
	@Override
	public String toString()	{
		return this.getName() + ": (" + this.precondition + 
				", " + modality + "(" + action + "))";
	}
	
	/**
	 * Returns a description of the norm (without the name)
	 * 
	 * @return a {@code String} describing the norm (without the name)
	 */
	public String getDescription()	{
		return "(" + this.precondition + ", " + modality + 
				"(" + action + "))";
	}
}
