package es.csic.iiia.nsm.norm.reasoning;

import java.util.ArrayList;
import java.util.List;

import jess.JessEvent;
import jess.JessException;
import jess.JessListener;
import jess.Rete;
import es.csic.iiia.nsm.agent.language.PredicatesDomains;
import es.csic.iiia.nsm.agent.language.SetOfPredicatesWithTerms;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.NormativeSystem;

/**
 * Computes norms' applicability for the agent contexts, namely the
 * situations that the agents perceive from their local point of view.
 * To reason about norm applicability, the norm engine employs Jess
 * ({@link http://herzberg.ca.sandia.gov/}), an engine to reason about rules.
 * Jess works as follows: 
 * <ol>
 * <li> the user adds some rules of the form "IF .. THEN..." to the Jess rule
 * 			database. Each norm consists of a precondition (IF), namely a string
 * 			of world facts that describes a state of the world, and a
 * 			postcondition (THEN), namely the actions that must be performed
 * 			whenever the situation described in the postcondition of the rule
 * 			is satisfied;
 * <li> the user adds some world facts to the facts database in the form of 
 * 			strings that describe the current state of the world; and
 * <li>	the Jess rule engine executes its algorithms to assess which norms
 * 			apply to the facts that describe the current state of the world
 * </ol>
 * As an example, consider we add to the Jess rules database a rule like
 * IF "It is 8 in the morning" THEN "I must go to the gym".
 * Consider now that it is 7 in the morning, and we add to Jess the fact 
 * "It is 7 in the morning". Then, Jess will not find any rule that applies
 * to that fact. However, if we add the fact "It is 8 in the morning", Jess
 * will fire the previous rule and "I must go to the gym" will hold.
 * <p>
 * In Jess, the facts in rules' preconditions and world facts have a
 * specific format, which differs from the format of the norms' preconditions
 * and agent context used by the Norm Synthesis Machine. For this reason,
 * the norm engine employs a {@code JessFactsGenerator} to translate the facts
 * in the NSM to facts that the Jess rule engine can understand.
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 * @see JessFactsGenerator
 */
public class NormEngine implements JessListener {

	//---------------------------------------------------------------------------
	// Attributes																															
	//---------------------------------------------------------------------------

	protected PredicatesDomains predDomains	;	// predicates and their domains
	protected NormativeSystem norms;					// the current normative system
	protected List<Norm> applicableNorms;			// norms applicable to the facts
	protected JessFactsGenerator factFactory; // to create facts for Jess
	protected Rete ruleEngine;								// the Jess rule engine
	
	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------

	/**
	 * Constructor
	 * 
	 * @param predDomains predicates and their domains
	 */
	public NormEngine(PredicatesDomains predDomains) 	{
		this.predDomains = predDomains;
		this.factFactory = new JessFactsGenerator(predDomains);
		
		this.ruleEngine = new Rete();
		this.norms = new NormativeSystem();
		this.applicableNorms = new ArrayList<Norm>();
		
		/* Add this reasoner ass a listener of the rule engine */
		ruleEngine.addJessListener(this);
		ruleEngine.setEventMask(ruleEngine.getEventMask() 
				| JessEvent.DEFRULE_FIRED);

		this.addPredicateTemplates();
	}

	/**
	 * Resets the norm engine by clearing the facts in the Jess rule engine
	 */
	public void reset() {
		try {
			ruleEngine.eval("(reset)");
			this.applicableNorms.clear();
		}
		catch (JessException e) {
			e.printStackTrace();
		}		
	}

	/**
	 * Adds a {@code SetOfPredicatesWithTerms} that represents the context of 
	 * an agent in the scenario, namely the world facts that the agent knows.
	 * Recall that the context of an agent is a  piece of information that
	 * the agent knows about the state of the system, described from 
	 * its local point of view
	 * 
	 * @param agContext the world fact that describes the context of an agent
	 */
	public String addFacts(SetOfPredicatesWithTerms agContext)  {
		String facts = this.factFactory.generateFacts(agContext,
				JessFactType.WorldFact);
		
		/* Clear previous facts and add new ones */
		try {
			ruleEngine.eval(facts);
		}
		catch (JessException e) {
			e.printStackTrace();
		}		
		return facts;
	}
	
	/**
	 * Executes the Jess rule reasoning algorithm and returns a {@code List}
	 * with the norms that apply to the facts that have been previously
	 * added to the Jess facts database
	 * 
	 * @return a {@code List} with the norms that apply to the facts that have
	 * 					been previously added to the Jess facts database
	 */
	public List<Norm> reason() {
		try {
			ruleEngine.run();
		} 
		catch (JessException e) {
			e.printStackTrace();
		}
		return this.applicableNorms;
	}
	
	/**
	 * Adds a norm to the Jess rules database. With this aim, it must
	 * previously translate the precondition of the norm to facts in the format
	 * that Jess can understand.
	 * 
	 * @param norm the norm to add
	 */
	public void addNorm(Norm norm) {
		if(!this.contains(norm)) 
		{
			SetOfPredicatesWithTerms precondition = norm.getPrecondition();
			
			/* Translate the norm's precondition to the format of the 
			 * Jess rules precondition */
			String facts = this.factFactory.generateFacts(precondition, 
					JessFactType.RulePrecondition);
	
			/* Generate Jess rule */
			String jessRule = "(defrule " + norm.getName() + " \"N\" "+ facts + "=> )";
	
			try {
				ruleEngine.eval(jessRule);
				norms.add(norm);
			}
			catch (JessException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Removes a norm from the rule database of Jess
	 * 
	 * @param norm the norm to remove
	 */
	public void removeNorm(Norm norm) {
		try {
			norms.remove(norm);
			ruleEngine.unDefrule(norm.getName());
		}
		catch (JessException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @return
	 */
	public List<Norm> getNorms() {
		return this.norms;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean contains(Norm norm) {
		return this.norms.contains(norm);
	}
	
	//--------------------------------------------------------------------------------
	// Rules
	//--------------------------------------------------------------------------------

	/**
	 * Adds to Jess a template for each possible predicate in the domain
	 */
	private void addPredicateTemplates() {
		try {
			ruleEngine.reset();

			/* Add templates to the knowledge base*/
			for(String predicate : predDomains.getPredicates()) {
				String template = "(deftemplate " + predicate + " (slot value))";
				ruleEngine.eval(template);
			}
		}
		catch (JessException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Fired when a rule has been activated. Updates the linked norm
	 * 
	 * @param je the Jess event that informs about the fired norm 
	 */
	@Override
	public void eventHappened(JessEvent je) throws JessException {
		int normId = -1;
		int type = je.getType();

		switch (type) {
		case JessEvent.DEFRULE_FIRED:
			normId = obtainFiredRule(je.getObject());
			break;
		}

		/* Activate the norm associated to this rule */
		this.applicableNorms.add(norms.getNormWithId(normId));
	}

	/**
	 * Returns the name of the norm that Jess has fired
	 * 
	 * @param o the fired norm
	 * @return the id of the fired norm
	 */
	private int obtainFiredRule(Object o) {
		String s = o.toString();
		int ind = s.indexOf("MAIN::");
		int i = ind + 6;
		int j = i;

		while(!s.substring(j, j+1).equals(" ")) {
			j++;
		}
		return Integer.valueOf(s.substring(i+1, j));
	}
}
