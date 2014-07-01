package es.csic.iiia.nsm.agent.language;

import java.util.HashMap;

/**
 * A set of predicates with terms. It consists in a set of {@code predicates},
 * where each {@code predicate} contains a non-empty set of {@code terms}. The
 * set of predicates has the form p_1(t_1, ..., t_n), ..., p_m(t_1, ..., t_m),
 * where p_i stands for the <i>ith</i> predicate and t_j stands
 * for the <i>jth</i> term of a predicate 
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
public class SetOfPredicatesWithTerms extends HashMap<String, SetOfStrings> {
	
	//------------------------------------------------------------------------
	// Static attributes
	// ------------------------------------------------------------------------
	
	private static final long serialVersionUID = 3504929358302321134L;
	
	// ------------------------------------------------------------------------
	// Attributes
	// ------------------------------------------------------------------------
	
	private SetOfStrings predicates;	 // the set of predicates 

	// ------------------------------------------------------------------------
	// Methods
	// ------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	public SetOfPredicatesWithTerms() {
		super();
		this.predicates = new SetOfStrings();
	}
	
	/**
	 * Constructor copying another object {@code SetOfPredicatesWithTerms}
	 * 
	 * @param otherSet the set of predicates and terms to copy
	 */
	public SetOfPredicatesWithTerms(SetOfPredicatesWithTerms otherSet) 	{
		this();
		
		for(String p : otherSet.getPredicates()) {
			for(String t : otherSet.getTerms(p)) {
				this.add(p, t);		// add pair predicate/term
			}
		}
	}
	
	/**
	 * Adds a {@code predicate} and its corresponding {@code term} to the set
	 * 
	 * @param predicate the predicate to add
	 * @param term the term to add for the given {@code predicate} 
	 */
	public void add(String predicate, String term) {
		this.predicates.add(predicate);
		
		if(!this.containsKey(predicate))	{
			this.put(predicate, new SetOfStrings());		
		}
		this.get(predicate).add(term);
	}
	
	/**
	 * Adds a {@code predicate} and a set of terms
	 * (as a {@code SetOfStrings}) to the set
	 * 
	 * @param predicate the predicate to add
	 * @param terms the set of terms to add for the given {@code predicate}
	 */
	public void add(String predicate, SetOfStrings terms) {
		for(String term : terms) {
			this.add(predicate, term);
		}
	}
	
	/**
	 * Adds a set of predicates with terms {@code predsWithTerms}
	 * to this set
	 * 
	 * @param predsWithTerms the set of predicates and terms to add
	 */
	public void add(SetOfPredicatesWithTerms predsWithTerms) {
		for(String predicate : predsWithTerms.getPredicates()) {
			for(String term : predsWithTerms.getTerms(predicate)) {
				this.add(predicate, term);		
			}
		}
	}
	
	/**
	 * Removes a {@code term} from the set of terms of a given
	 * {@code predicate} in this set. It does not remove the given
	 * {@code predicate}
	 * 
	 * @param predicate the predicate that contains the term
	 * @param term the term to remove
	 */
	public void removeTerm(String predicate, String term)	{
		this.get(predicate).remove(term);
	}
	
	/**
	 * Returns the set of predicates in this set
	 * 
	 * @return the set of predicates in this set
	 */
	public SetOfStrings getPredicates()	{
		return this.predicates;
	}
	
	/**
	 * Returns the set of terms of a predicate in this set
	 * 
	 * @param predicate the predicate
	 * @return the set of terms of the predicate
	 */
	public SetOfStrings getTerms(String predicate)	{
		return this.get(predicate);
	}

	/**
	 * Returns <tt>true</tt> if the set of predicates with terms
	 * contains the pair predicate(term)
	 * 
	 * @param predicate the predicate
	 * @param term the term of the predicate
	 * @return <tt>true</tt> if the pair predicate(term) is contained
	 * 					in this set of predicates and terms
	 */
	public boolean contains(String predicate, String term) {
		for(String p : this.keySet()) {
			SetOfStrings myTerms = this.get(predicate);	
			
			/* Search the term into the terms of each predicate */
			for(String t : myTerms)	{
				if(p.equals(predicate) && t.equals(term)) {	
					return true;	
				}
			}
		}
		return false;
	}
	
	/**
	 * Clears the set, removing all predicates and terms
	 */
	public void clear() {
		super.clear();
		this.predicates.clear();
	}
	
	/**
	 * Returns <tt>true</tt> if this set of predicates with terms and
	 * the {@code otherSet} contain the same predicates, and the same
	 * terms for each predicate. No matter the order of the predicates
	 * and the terms of each predicate.
	 * 
	 * @param otherSet the other set to compare this set with
	 * @return <tt>true</tt> if the two sets are equal
	 */
	public boolean equals(SetOfPredicatesWithTerms otherSet) {
		if(!this.predicates.equals(otherSet.getPredicates())) {
			return false;
		}
		for(String predicate : this.predicates) {
			SetOfStrings terms = this.getTerms(predicate);
			
			if(!terms.equals(otherSet.getTerms(predicate))) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Returns a description of this set of predicates with terms.
	 * The {@code String} has the form p_1(t_1, ..., t_n), ..., 
	 * p_m(t_1, ..., t_m), where p_i stands for the <i>ith</i>
	 * predicate and t_j stands for the <i>jth</i> term of a predicate
	 *  
	 * @return a {@code String} describing this set
	 */
	public String toString() {
		String s = "";
		int i=0;
		
		for(String p : this.getPredicates()) {
			int j=0;
			
			if(i>0) {
				s += "&";
			}
			s += p + "(";
			
			for(String t : this.getTerms(p)) {
				if(j>0) {
					s += "|";
				}
				s += t;
				j++;
			}
			s += ")";
			i++;
		}
		return s;
	}
}
