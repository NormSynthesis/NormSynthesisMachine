package es.csic.iiia.nsm.norm.refinement.simon;

import es.csic.iiia.nsm.NormSynthesisMachine.NormGeneralisationMode;
import es.csic.iiia.nsm.agent.EnvironmentAgentAction;
import es.csic.iiia.nsm.agent.language.PredicatesDomains;
import es.csic.iiia.nsm.agent.language.SetOfPredicatesWithTerms;
import es.csic.iiia.nsm.agent.language.SetOfStrings;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.NormModality;

/**
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 *
 */
public class NormIntersection {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------

	private Norm normA, normB;
	private SetOfPredicatesWithTerms intersection;
	private SetOfPredicatesWithTerms difference;
	private PredicatesDomains predDomains;
	
	private NormModality modality;
	private EnvironmentAgentAction action;

	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------

	/**
	 * Generates the intersection of the preconditions of two norms that have the
	 * same postcondition. A norm intersection can be computed in two
	 * different ways, <i>Shallow</i> and <i>Deep</i>:
	 * <ol>
	 * <li> in the <i>Shallow</i> mode, those terms that are <b>equal</b> in both
	 * norms are added to the intersection, and those that are different are
	 * added to the difference between norms;
	 * <li> in the <i>Deep</i> mode, whenever the corresponding terms of a
	 * predicate in both norms have an <b>intersection</b>, then it adds the most
	 * specific term to the intersection, and those that are different are
	 * added to the difference between norms.
	 * </ol>
	 * 
	 * @param normA the first norm
	 * @param normB the second norm 
	 * @param predDomains the predicate domains
	 * @param genMode the SIMON generalisation mode (Shallow/Deep)
	 */
	public NormIntersection(Norm normA, Norm normB,
			PredicatesDomains predDomains, NormGeneralisationMode genMode) {

		this.normA = normA;
		this.normB = normB;
		this.predDomains = predDomains;
		
		this.modality = normA.getModality();
		this.action = normA.getAction();
		
		this.intersection = new SetOfPredicatesWithTerms();
		this.difference = new SetOfPredicatesWithTerms();

		/* Only compute the intersection for those norms 
		 * that have the same postcondition (we only generalise the 
		 * postconditions of norms, by now. Sorry :( */
		if(normA.getModality() == normB.getModality() && 
				normA.getAction() == normB.getAction()) {

			/* Create norm intersection depending on the SIMON 
			 * generalisation mode (Shallow or Deep) */
			if(genMode == NormGeneralisationMode.Shallow) {
				this.generateShallowIntersection();	
			}
			else if(genMode == NormGeneralisationMode.Deep) {
				this.generateDeepIntersection();
			}
		}
	}

	/**
	 * Generates a deep intersection between two norms. That is, it visits 
	 * each predicate in both norms, checking if their terms have an 
	 * intersection or not. If the terms have an intersection, then it
	 * adds the most specific term of them two to the intersection.
	 * Otherwise, if the terms  do not intersect, then it adds both
	 * terms to the difference between norms
	 */
	private void generateDeepIntersection() {
		SetOfPredicatesWithTerms precondNormA = normA.getPrecondition();
		SetOfPredicatesWithTerms precondNormB = normB.getPrecondition();
		SetOfStrings predicates = precondNormA.getPredicates();
		String genTerm, specTerm;

		for(String predicate : predicates) {	
			String term1 = precondNormA.getTerms(predicate).get(0);
			String term2 = precondNormB.getTerms(predicate).get(0);

			/* The predicates intersect because they contain the same term */
			if(term1.equals(term2)) {
				intersection.add(predicate, term1);
				continue;
			}
			/* Sort terms to differ which is the more specific and 
			 * the more general term */
			if(predDomains.getGeneralisationLevel(predicate, term1) 
					> predDomains.getGeneralisationLevel(predicate, term2)) {
				genTerm = term1;
				specTerm = term2;
			}
			else {
				genTerm = term2;
				specTerm = term1;
			}
			/* Retrieve all the terms that generalise the specific term */
			SetOfStrings specTermParents = predDomains.
					getParentTerms(predicate, specTerm);

			/* Check if the genTerm generalises the specific term*/
			if(specTermParents.contains(genTerm)) {
				intersection.add(predicate, specTerm);
			}
			else {
				difference.add(predicate, term1);
				difference.add(predicate, term2);
			}
		}
	}

	/**
	 * Generates a shallow intersection between two norms. That is, it visits 
	 * each predicate in both norms, checking if their terms are equal or not.
	 * If the terms are equal, then it adds the predicate/term to the
	 * intersection. Otherwise, if the terms of the predicate in both norms
	 * are different, then it adds then it adds both terms to the difference
	 * between norms
	 */
	private void generateShallowIntersection() {
		SetOfPredicatesWithTerms precondN1 = normA.getPrecondition();
		SetOfPredicatesWithTerms precondN2 = normB.getPrecondition();
		SetOfStrings predicates = precondN1.getPredicates();

		for(String predicate : predicates) {
			String term1 = precondN1.getTerms(predicate).get(0);
			String term2 = precondN2.getTerms(predicate).get(0);

			/* In a shallow intersection, the predicates intersect only
			 * if they contain exactly the same term */
			if(term1.equals(term2)) {
				intersection.add(predicate, term1);
				continue;
			}
			/* If the predicates do not intersect, add them to the difference */
			else {
				difference.add(predicate, term1);
				difference.add(predicate, term2);
			}
		}
	}

	/**
	 * Returns the intersection between norms A and B
	 * 
	 * @return the intersection between norms
	 */
	public SetOfPredicatesWithTerms getIntersection() {
		return this.intersection;
	}

	/**
	 * Returns the difference between norms A and B
	 * 
	 * @return the difference between norms A and B
	 */
	public SetOfPredicatesWithTerms getDifference() {
		return this.difference;
	}

	/**
	 * Returns the modality of norms A and B
	 * 
	 * @return the modality of norms A and B
	 */
	public NormModality getModality() {
		return this.modality;
	}
	
	/**
	 * Returns the action of norms A and B
	 * 
	 * @return
	 */
	public EnvironmentAgentAction getAction() {
		return this.action;
	}
	
	/**
	 * Returns the number of intersected predicates between norms A and B
	 * 
	 * @return the number of intersected predicates between norms A and B
	 */
	public int getIntersectionCardinality() {
		return this.intersection.getPredicates().size();
	}

	/**
	 * Returns the number of different predicates between norms A and B
	 * 
	 * @return the number of different predicates between norms A and B
	 */
	public int getDifferenceCardinality() {
		return this.difference.getPredicates().size();
	}

	/**
	 * Returns a String describing the intersection
	 * 
	 * @return a String describing the intersection
	 */
	public String toString() {
		return "Intersec: " + this.intersection.toString() 
				+ "Diff: " + this.difference.toString();
	}

	/**
	 * Returns the predicates and domains employed to create the intersection
	 * 
	 * @return the predicates and domains employed to create the intersection
	 */
	public PredicatesDomains getPredicatesDomains() {
		return this.predDomains;
	}
	
	/**
	 * Returns an identifier for two norms that can be intersected
	 *  
	 * @return an identifier for two norms that can be intersected
	 */
	public static String getDescription(Norm n1, Norm n2) {
		if(n1.getId() < n2.getId()) {
			return n1.getId() + "-" + n2.getId();
		}
		return n2.getId() + "-" + n1.getId();
	}
}
