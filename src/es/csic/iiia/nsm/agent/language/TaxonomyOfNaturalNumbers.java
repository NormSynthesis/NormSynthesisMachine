package es.csic.iiia.nsm.agent.language;

/**
 * The taxonomy of natural numbers. This taxonomy consists of all natural
 * numbers from -infinite to +infinite, all of them generalised by term "any".
 * Thus, a pair of two random natural numbers may be generalised to term "any"
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 * @see Taxonomy
 */
public class TaxonomyOfNaturalNumbers implements Taxonomy {
	
	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------
	
	private String predicate; // the predicate to which the taxonomy corresponds
	
	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------
	
	/**
	 * Constructor with predicate
	 *  
	 * @param predicate the predicate to which the taxonomy corresponds
	 */
	public TaxonomyOfNaturalNumbers(String predicate) {
		this.predicate = predicate;
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
	 * Returns the immediate parent of a number. The generalisation of
	 * any number is "any". Therefore, it returns "any" if {@code term}
	 * is a natural number, and {@code null} if the term is not a natural number 
	 *  
	 * @return "any" if {@code term} is a natural number, and
	 * 					{@code null} otherwise
	 */
	@Override
	public String getImmediateParentTerm(String term) 	{
		if(this.isNaturalNumber(term))
			return "any";
		
		return null;
	}

	/**
	 * Returns the most specific term that generalises a pair of terms,
	 * in case that both terms are natural numbers. In particular,
	 * the generalisation between any pair of numbers is term "any"
	 * 
	 * @param t1 the first term
	 * @param t2 the second term
	 * @return term "any", in case terms t1 and t2 are natural numbers
	 */
	@Override
	public String getMostSpecifficGeneralisation(String t1, String t2) {
		if(this.isNaturalNumber(t1) && this.isNaturalNumber(t2)) {
			return "any";
		}
		return null;
	}

	/**
	 * Returns a {@code SetOfStrings} that contains term "any" if
	 * {@code term} is a natural number, and is empty otherwise
	 * 
	 * @return a {@code SetOfStrings} that contains term "any" if
	 * {@code term} is a natural number, and is empty otherwise
	 */
	@Override
	public SetOfStrings getParentTerms(String term) {
		SetOfStrings terms = new SetOfStrings();
		terms.add("any");
		
		return terms;
	}

	/**
	 * Returns the level of generalisation of the given {@code term}
	 * in the taxonomy (i.e., the domain), always that {@code term} is a
	 * natural number. If {@code term} is a natural number, then it is a leaf
	 * in the taxonomy, and hence this method returns 0 (generalisation level 0).
	 * If the {@code term} equals to "any", then its generalisation level is 1.
	 * Otherwise, the {@code term} does not belong to the taxonomy and then
	 * the method returns -1.  
	 * 
	 * @param term the term
	 * @return 0 if {@code term} is a natural number, 1 if {@code term} is "any",
	 * 					and -1 otherwise (since the {@code term} does not belong to the
	 * 					taxonomy)
	 */
	@Override
	public int getGeneralisationLevel(String term) {
		if(this.isNaturalNumber(term)) {
			return 0;
		}
		else if(term.equals("any")) {
			return 1;
		}
		return -1;
	}
	
	/**
	 * Returns <tt>true</tt> if the given {@code term} is in the top of the
	 * taxonomy, namely the given {@code term} is "any". It returns
	 * <tt>false</tt> otherwise.
	 * 
	 * @param term the term 
	 * @return <tt>true</tt> if the given {@code term} is in the top
	 * 					of the taxonomy, namely the given {@code term} is "any".
	 * 					It returns <tt>false</tt> otherwise.
	 */
	public boolean isTop(String term) {
		return term.equals("any");
	}
	
	/**
	 * Returns <tt>true</tt> if the taxonomy contains a given {@code term},
	 * and the {@code term} is a natural number
	 * 
	 * @param term the term 
	 * @return <tt>true</tt> if the taxonomy contains the given {@code term},
	 * 					and the {@code term} is a natural number
	 */
	@Override
	public boolean contains(String term) {
		if(this.isNaturalNumber(term)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Returns <tt>true</tt> if the term is a natural number
	 * 
	 * @param term the term
	 * @return <tt>true</tt> if the term is a natural number
	 */
	private boolean isNaturalNumber(String term) {  
	  try {  
	    Integer.parseInt(term);  
	  }  
	  catch(NumberFormatException nfe) {  
	    return false;  
	  }  
	  return true;  
	}
}
