package es.csic.iiia.nsm.agent.language;

import java.util.Collection;
import java.util.HashMap;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;

/**
 * A taxonomy of terms that represents the domain of a predicate
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 * @see Taxonomy
 */
public class TaxonomyOfTerms implements Taxonomy {

	// --------------------------------------------------------------------------
	// Attributes
	// --------------------------------------------------------------------------

	private String predicate;
	private DirectedSparseMultigraph<String, TaxonomyEdge> termsGraph;
	private SetOfStrings terms;
	private HashMap<String, Integer> termsLevels;
	private int numLevels;
	
	// --------------------------------------------------------------------------
	// Methods
	// --------------------------------------------------------------------------

	/**
	 * Constructor with predicate
	 *  
	 * @param predicate the predicate to which the taxonomy corresponds
	 */
	public TaxonomyOfTerms(String predicate) {
		this.predicate = predicate;
		this.numLevels = 0;
		
		this.termsGraph = new DirectedSparseMultigraph<String, TaxonomyEdge>();
		this.termsLevels = new HashMap<String, Integer>();
		this.terms = new SetOfStrings();
	}

	/**
	 * Constructor with copy of another taxonomy
	 * 
	 * @param predicate the predicate to which the taxonomy corresponds
	 * @param taxonomy the taxonomy that represents the domain of the predicate
	 */
	public TaxonomyOfTerms(String predicate, TaxonomyOfTerms taxonomy) {
		this(predicate);

		for(String term : taxonomy.getTerms()) {
			this.addTerm(term);
			String parentTerm = taxonomy.getImmediateParentTerm(term);

			if(parentTerm != null) {
				this.addRelationship(term, parentTerm);
			}
		}
	}

	/**
	 * Returns the predicate to which the taxonomy corresponds
	 * 
	 * @return the predicate to which the taxonomy corresponds
	 */
	public String getPredicate() {
		return this.predicate;
	}

	/**
	 * Returns the immediate parent of a given {@code term} 
	 * 
	 * @param term the specific term
	 * @return the immediate parent of the term
	 */
	public String getImmediateParentTerm(String term) {

		/* The term has no parent */
		if(this.termsGraph.getOutEdges(term) == null) {
			return null;
		}
		for(TaxonomyEdge edge : this.termsGraph.getOutEdges(term)) {
			return this.termsGraph.getDest(edge);
		}
		return null;
	}
	
	/**
	 * Returns the most specific term that generalises a pair of terms
	 * {@code t1} and {@code t2}
	 * 
	 * @param t1 the first term
	 * @param t2 the second term
	 * @return The most specific term that generalises both terms t1 and t2
	 */
	@Override
	public String getMostSpecifficGeneralisation(String t1, String t2) {		
		SetOfStrings t1Parents = this.getParentTerms(t1); 
		SetOfStrings t2Parents = this.getParentTerms(t2);

		for(String term : t1Parents) {
			if(t2Parents.contains(term)) {
				return term;
			}
		}
		return null;
	}

	/**
	 * Returns a {@code SetOfStrings} that contains a set of all the
	 * terms that are more general than the given {@code term} in the taxonomy
	 * 
	 * @param term the term
	 * @return an object {@code SetOfStrings} that contains a set of sorted
	 * 					terms that are more general than the given {@code term}
	 */
	@Override
	public SetOfStrings getParentTerms(String term) {
		SetOfStrings parentTerms = new SetOfStrings();
		this.getParentTerms(term, parentTerms);

		return parentTerms;
	}

	/**
	 * Returns the {@code level} of generalisation of the given {@code term}
	 * in the taxonomy (i.e., the domain). The generalisation level indicates
	 * the position (i.e., height) of the term in the taxonomy. As an example,
	 * while a "leaf" term in the taxonomy has level 0, its immediate parent
	 * has level 1, and the parent of the parent has level 2  
	 * 
	 * @param term the term
	 * @return the {@code level} of generalisation of the given {@code term}
	 * 					in the taxonomy
	 */
	@Override
	public int getGeneralisationLevel(String term) {
		if(!this.termsLevels.containsKey(term))
			return -1;

		return this.termsLevels.get(term);
	}

	/**
	 * Returns <tt>true</tt> if the given {@code term} is in the top of the
	 * taxonomy. In other words, it returns <tt>true</tt> if the term is the
	 * most general term in the taxonomy
	 * 
	 * @param term the term 
	 * @return <tt>true</tt> if the given {@code term} is in the top
	 * 					of the taxonomy
	 */
	public boolean isTop(String term) {
		if(!termsLevels.containsKey(term)) {
			return false;
		}
		int termLevel = this.termsLevels.get(term);
		return termLevel == this.numLevels;
	}
	
	/**
	 * Returns <tt>true</tt> if the taxonomy contains a given {@code term},
	 * <tt>false</tt> otherwise
	 * 
	 * @param term the term 
	 * @return <tt>true</tt> if the taxonomy contains the given {@code term}
	 */
	public boolean contains(String term) {
		return this.terms.contains(term);
	}

	// --------------------------------------------------------------------------
	// Class methods
	// --------------------------------------------------------------------------

	/**
	 * Adds a term to the taxonomy, and sets its generalisation level to 0 
	 * 
	 * @param term the term to add
	 */
	public void addTerm(String term) {
		this.termsGraph.addVertex(term.toString());
		this.terms.add(term);
		this.termsLevels.put(term, 0);
	}

	/**
	 * Adds a generalisation relationship from term t1 to term t2
	 * 
	 * @param t1 the first (more specific) term
	 * @param t2 the second (more general) term
	 */
	public void addRelationship(String t1, String t2) {
		int newTermLevel = termsLevels.get(t1) + 1;
		
		this.termsGraph.addEdge(new TaxonomyEdge(), t1, t2);
		this.termsLevels.put(t2, newTermLevel);
		
		if(newTermLevel > this.numLevels) {
			this.numLevels = newTermLevel;
		}
	}

	/**
	 * Returns a {@code SetOfStrings} containing all the terms that 
	 * the given {@code term} represents in the taxonomy
	 * 
	 * @param term the term to search	 
	 * @return a {@code SetOfStrings} containing all the terms that 
	 * 					the given {@code term} represents in the taxonomy
	 */
	public SetOfStrings getRepresentedTerms(String term) {
		SetOfStrings childTerms = new SetOfStrings();

		if(!this.termsGraph.containsVertex(term)) {
			return childTerms;
		}
		Collection<TaxonomyEdge> edges = this.termsGraph.getInEdges(term);

		/* The term does not represent any term in the taxonomy */
		if(edges.isEmpty()) {
			childTerms.add(term);
			return childTerms;
		}

		for(TaxonomyEdge edge : this.termsGraph.getInEdges(term)) {
			String child = this.termsGraph.getSource(edge);
			childTerms.add(child);
		}
		return childTerms;
	}
	
	/**
	 * Returns a {@code SetOfStrings} containing all the terms in the taxonomy
	 * 
	 * @return a {@code SetOfStrings} containing all the terms in the taxonomy
	 */
	public SetOfStrings getTerms()	{
		return this.terms;
	}

	// --------------------------------------------------------------------------
	// Private methods
	// --------------------------------------------------------------------------
	
	/**
	 * Recursively fills the set of strings {@code parentTerms} with all
	 * the terms that are more general than the given {@code term}
	 * 
	 * @param term the term
	 */
	private void getParentTerms(String term, SetOfStrings parentTerms) {
		String parent = this.getImmediateParentTerm(term);
		
		if(parent != null) {
			parentTerms.add(parent);
			this.getParentTerms(parent, parentTerms);
		}
		return;		
	}
}

/**
 * An edge between the terms of a taxonomy of terms
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
class TaxonomyEdge {}
