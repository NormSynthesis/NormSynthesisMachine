package es.csic.iiia.nsm.norm.evaluation;

import java.util.ArrayList;
import java.util.List;

import es.csic.iiia.nsm.agent.language.SetOfPredicatesWithTerms;
import es.csic.iiia.nsm.norm.Norm;

/**
 * Norms that apply to a particular agent context
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
public class NormsApplicableToAgentContext {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------

	private SetOfPredicatesWithTerms agentContext;
	private List<Norm> applicableNorms;

	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	public NormsApplicableToAgentContext() 	{
		this.applicableNorms = new ArrayList<Norm>();
	}

	/**
	 * Constructor with agent context
	 * 
	 * @param agentContext the agent context
	 */
	public NormsApplicableToAgentContext(SetOfPredicatesWithTerms agentContext) {
		this();
		this.agentContext = agentContext;
	}

	/**
	 * Constructor with agent context and norm
	 * 
	 * @param agentContext the agent context
	 * @param norm a norm applicable to the given agent context
	 */
	public NormsApplicableToAgentContext(SetOfPredicatesWithTerms agentContext,
			Norm norm) {
		this(agentContext);
		this.add(norm);
	}
	
	/**
	 * Constructor with an agent context and a list of applicable norms
	 * 
	 * @param agentContext the agent context
	 * @param norms a {@code List} of norms that apply to the given agent context
	 */
	public NormsApplicableToAgentContext(SetOfPredicatesWithTerms agentContext,
			List<Norm> norms) {
		this(agentContext);

		for(Norm norm :norms)	{
			this.add(norm);
		}
	}

	/**
	 * Adds a norm applicable to the agent context
	 * 
	 * @param norm the norm to add
	 */
	public void add(Norm norm) {		
		if(!applicableNorms.contains(norm)) {
			applicableNorms.add(norm);
		}
	}

	/**
	 * Returns the agent context in which norms apply
	 * 
	 * @return the agent context
	 */
	public SetOfPredicatesWithTerms getAgentContext()  {
		return this.agentContext;
	}

	/**
	 * Returns the {@code List} of norms that apply to the agent context
	 *  
	 * @return the {@code List} of norms that apply to the agent context
	 */
	public List<Norm> getApplicableNorms() {
		return this.applicableNorms;
	}
		
	/**
	 * Clears the list of applicable norms
	 */
	public void clear() {
		this.applicableNorms.clear();
	}
	
	/**
	 * Returns <tt>true</tt> if this object contains the same agent context and
	 * list of norms than the {@code other}
	 * 
	 * @return <tt>true</tt> if this object contains the same agent context and
	 * 					list of norms than the {@code other}
	 */
	public boolean equals(NormsApplicableToAgentContext other) {
		SetOfPredicatesWithTerms thisContext = this.getAgentContext();
		SetOfPredicatesWithTerms otherContext = other.getAgentContext();
		List<Norm> thisApplicableNorms = this.getApplicableNorms();
		List<Norm> otherApplicableNorms = other.getApplicableNorms();
		
		/* Check agent contexts */
		if(!thisContext.equals(otherContext)) {
			return false;
		}		
		/* Check that the two structures contain the same norms */
		for(Norm norm : thisApplicableNorms) {
			if(!otherApplicableNorms.contains(norm)) {
				return false;
			}
		}
		for(Norm norm : otherApplicableNorms) {
			if(!thisApplicableNorms.contains(norm)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Returns a {@code String} describing the contents
	 * 
	 * @return a {@code String} describing the contents
	 */
	public String toString() {
		return this.agentContext.toString() + " : " + this.applicableNorms;
	}
}
