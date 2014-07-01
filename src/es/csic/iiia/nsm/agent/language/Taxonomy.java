package es.csic.iiia.nsm.agent.language;

/**
 * A taxonomy of terms in the domain of a predicate. A taxonomy of terms
 * represents the generalisation relationships between the terms of a
 * predicate's domain
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
public interface Taxonomy {
	
	/**
	 * Returns the predicate to which the taxonomy corresponds
	 * 
	 * @return the predicate to which the taxonomy corresponds
	 */
	public String getPredicate();
	
	/**
	 * Returns the immediate parent of a given {@code term} 
	 * 
	 * @param term the specific term
	 * @return The immediate parent of the term
	 */
	public abstract String getImmediateParentTerm(String term);
	
	/**
	 * Returns the most specific term that generalises a pair of terms
	 * {@code t1} and {@code t2}
	 * 
	 * @param t1 the first term
	 * @param t2 the second term
	 * @return The most specific term that generalises both terms t1 and t2
	 */
	public abstract String getMostSpecifficGeneralisation(String t1, String t2);
	
	/**
	 * Returns a {@code SetOfStrings} that contains a set of all the
	 * terms that are more general than the given {@code term} in the taxonomy
	 * 
	 * @param term the term
	 * @return an object {@code SetOfStrings} that contains a set of sorted
	 * 					terms that are more general than the given {@code term}
	 */
	public abstract SetOfStrings getParentTerms(String term);
	
	/**
	 * Returns the level of generalisation of the given {@code term}
	 * in the taxonomy (i.e., the domain). The generalisation level indicates
	 * the position (i.e., height) of the term in the taxonomy. As an example,
	 * while a "leaf" term in the taxonomy has level 0, its immediate parent
	 * has level 1, and the parent of the parent has level 2  
	 * 
	 * @param term the term
	 * @return the {@code level} of generalisation of the given {@code term}
	 * 					in the taxonomy
	 */
	public abstract int getGeneralisationLevel(String term);
	
	/**
	 * Returns <tt>true</tt> if the given {@code term} is in the top of the
	 * taxonomy. In other words, it returns <tt>true</tt> if the term is the
	 * most general term in the taxonomy
	 * 
	 * @param term the term 
	 * @return <tt>true</tt> if the given {@code term} is in the top
	 * 					of the taxonomy
	 */
	public abstract boolean isTop(String term);
	
	/**
	 * Returns <tt>true</tt> if the taxonomy contains a given {@code term},
	 * <tt>false</tt> otherwise
	 * 
	 * @param term the term 
	 * @return <tt>true</tt> if the taxonomy contains the given {@code term}
	 */
	public abstract boolean contains(String term);
}
