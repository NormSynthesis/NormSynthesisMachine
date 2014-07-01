package es.csic.iiia.nsm.agent.language;

import java.util.HashMap;
import java.util.Map;

/**
 * Domains for each possible {@code predicate} in the contexts of the agents.
 * Each predicate has a particular {@code domain}, which is represented as
 * a {@code taxonomy} of terms. The taxonomy represents the generalisation
 * relationships between the terms of a predicate's domain
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
public class PredicatesDomains {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------
	
	private SetOfStrings predicates;						// the possible predicates
	private Map<String, Taxonomy> predDomains;	// the predicates domains
	
	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	public PredicatesDomains() {
		this.predicates = new SetOfStrings();
		this.predDomains = new HashMap<String, Taxonomy>();
	}
	
	/**
	 * Adds a predicate (if it is not already contained) and its
	 * corresponding domain (as a taxonomy of terms)
	 * 
	 * @param predicate the predicate
	 * @param taxonomy the predicate's domain as a taxonomy of terms
	 * @see Taxonomy
	 */
	public void addPredicateDomain(String predicate, Taxonomy taxonomy) {
		this.predDomains.put(predicate, taxonomy);
		this.predicates.add(predicate);
	}
	
	/**
	 * Returns a {@code SetOfStrings} that contains all the predicates
	 * that may appear in the agents' contexts 
	 * 
	 * @return a {@code SetOfStrings} that contains all the predicates
	 * 					that may appear in the agents' contexts
	 * @see SetOfStrings 
	 */
	public SetOfStrings getPredicates() {
		return this.predicates;
	}
	
	/**
	 * Returns an object {@code Taxonomy} that contains the domain of
	 * the {@code predicate} passed by parameter
	 * 
	 * @param predicate the predicate of which to return the domain
	 * @return an object {@code Taxonomy} that contains the domain of
	 * 					the {@code predicate} passed by parameter
	 * @see Taxonomy
	 */
	public Taxonomy getDomain(String predicate) {
		return this.predDomains.get(predicate);
	}
	
	/**
	 * Returns the immediate parent of a {@code term} contained in
	 * the domain of the given {@code predicate} 
	 * 
	 * @param predicate the predicate
	 * @param term the term in the domain of {@code predicate}
	 * @return the immediate parent of a {@code term} contained in
	 * 					domain of the given {@code predicate}
	 * @see Taxonomy
	 */
	public String getImmediateParentTerm(String predicate, String term) {
		Taxonomy o = this.predDomains.get(predicate);
		return o.getImmediateParentTerm(term);
	}
	
	/**
	 * Returns the most specific {@code term} that generalises (subsumes)
	 * a pair of terms in the domain of the given {@code predicate}
	 * 
	 * @param predicate the predicate 
	 * @param t1 the first term
	 * @param t2 the second term
	 * @return the most specific {@code term} that generalises (subsumes)
	 * 					a pair of terms in the domain of the given {@code predicate}
	 * @see Taxonomy
	 */
	public String getMostSpecifficGeneralisation(String predicate,
			String t1, String t2) {		
		Taxonomy o = this.predDomains.get(predicate);
		return o.getMostSpecifficGeneralisation(t1, t2);
	}
	
	/**
	 * Returns an object {@code SetOfStrings} that contains a set of sorted
	 * terms that are more general than the given {@code term} in the domain
	 * of the given {@code predicate}
	 * 
	 * @param predicate the predicate
	 * @param term the term in the domain of {@code predicate}
	 * @return an object {@code SetOfStrings} that contains a set of sorted
	 * 					terms that are more general than the given {@code term} in the
	 * 					domain of the given {@code predicate}
	 * @see Taxonomy
	 */
	public SetOfStrings getParentTerms(String predicate, String term) {
		Taxonomy o = this.predDomains.get(predicate);
		return o.getParentTerms(term);
	}
	
	/**
	 * Returns the {@code level} of generalisation of the given {@code term}
	 * in the taxonomy (i.e., the domain) of the given {@code predicate}.
	 * The generalisation level indicates the position (i.e., height) of the term
	 * in the taxonomy of the predicate's domain. As an example, while a "leaf"
	 * term in the taxonomy has level 0, its immediate parent has level 1,
	 * and the parent of the parent has level 2  
	 * 
	 * @param predicate the predicate
	 * @param term the term
	 * @return the level of generalisation of the given {@code term}
	 * 					in the taxonomy (i.e., the domain) of the given {@code predicate}
	 * @see Taxonomy
	 */
	public int getGeneralisationLevel(String predicate, String term) {
		Taxonomy o = this.predDomains.get(predicate);
		return o.getGeneralisationLevel(term);
	}
	
	/**
	 * Returns <tt>true</tt> if the given {@code term} is in the top of the
	 * taxonomy (i.e., the domain) of the given {@code predicate}. In other
	 * words, it returns <tt>true</tt> if the term is the most general
	 * term in the predicate's domain
	 * 
	 * @param predicate the predicate
	 * @param term the term 
	 * @return <tt>true</tt> if the given {@code term} is in the top of the
	 * 					taxonomy (i.e., the domain) of the given {@code predicate}
	 * @see Taxonomy
	 */
	public boolean isTop(String predicate, String term) {
		Taxonomy o = this.predDomains.get(predicate);
		return o.isTop(term); 
	}
}
