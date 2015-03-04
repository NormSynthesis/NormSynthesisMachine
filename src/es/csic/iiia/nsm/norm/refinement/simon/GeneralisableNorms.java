package es.csic.iiia.nsm.norm.refinement.simon;

import java.util.ArrayList;
import java.util.List;

import es.csic.iiia.nsm.agent.EnvironmentAgentAction;
import es.csic.iiia.nsm.agent.language.PredicatesDomains;
import es.csic.iiia.nsm.agent.language.SetOfPredicatesWithTerms;
import es.csic.iiia.nsm.agent.language.SetOfStrings;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.NormModality;

/**
 * Constructs a potential generalisation for two child norms. It receives
 * the intersection {@code NormIntersection} between two norms A and B, and
 * computes the norms A' and B' that can be generalised together to a
 * parent norm. In the case of a <i>Shallow</i> generalisation, the
 * generalisable norms A' and B' will directly be norms A and B in
 * the intersection, respectively. However, in the case of a <i>Deep</i>
 * generalisation, norms A' and B' may be other two norms generalised
 * by norms A and B 
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 * @see NormIntersection
 */
public class GeneralisableNorms {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------

	private Norm normA, normB;
	private Norm parent;
	private List<Norm> allNorms;

	private NormModality modality;
	private EnvironmentAgentAction action;
	private PredicatesDomains predDomains;

	//	private int genStep;

	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------

	/**
	 * Constructor with parameters
	 * 
	 * @param 	n1 the norm that must be checked if can be generalised with
	 * 					norm {@code n2}
	 * @param 	n2 the norm that must be checked if can be generalised with
	 * 					norm {@code n1}
	 * @param 	intersection the intersection between 
	 * @param 	predDomains
	 * @param 	genMode
	 */
	public GeneralisableNorms(NormIntersection intersection, int genStep) {
		this.predDomains = intersection.getPredicatesDomains();
		this.parent = null;
		this.allNorms = new ArrayList<Norm>();

		this.modality = intersection.getModality();
		this.action = intersection.getAction();

		/* Search for the generalisable norms */
		if(intersection.getDifferenceCardinality() > 0 && 
				intersection.getDifferenceCardinality() <= genStep) 
		{
			this.generate(intersection);
		}
		
		this.allNorms.add(normA);
		this.allNorms.add(normB);
		this.allNorms.add(parent);
	}

	/**
	 * Takes the norms A and B that have an intersection {@code normIntersection}
	 * and finds the corresponding norms A' and B' that can be generalised
	 * together to a parent norm. In the case of a <i>Shallow</i> generalisation,
	 * the generalisable norms A' and B' will directly be norms A and B in
	 * the intersection, respectively. However, in the case of a <i>Deep</i>
	 * generalisation, norms A' and B' may be other two norms generalised
	 * by norms A and B 
	 * 
	 * @param normIntersection the intersection between two norms A and B
	 */
	private void generate(NormIntersection normIntersection) {
		SetOfPredicatesWithTerms intersection = normIntersection.getIntersection();
		SetOfPredicatesWithTerms difference = normIntersection.getDifference();
		SetOfPredicatesWithTerms normAPrecond = new SetOfPredicatesWithTerms();
		SetOfPredicatesWithTerms normBPrecond = new SetOfPredicatesWithTerms();
		SetOfPredicatesWithTerms parentPrecond = new SetOfPredicatesWithTerms();
		SetOfPredicatesWithTerms allPredicates = 
				new SetOfPredicatesWithTerms(intersection);
		allPredicates.add(difference);

		boolean existsParentNorm = false;
		
		for(String predicate : this.predDomains.getPredicates()) {
			SetOfStrings terms = allPredicates.getTerms(predicate);

			/* Difference predicate: Most specific generalisation between
			 * the two terms */
			if(terms.size() > 1) {
				String termA = terms.get(0);
				String termB = terms.get(1);
				String generalTerm = this.predDomains.getMostSpecifficGeneralisation(
						predicate, termA, termB);

				/* Add the general term to the parent norm, and one of the specific
				 * terms to the each child norm */
				if(generalTerm != null) {
					parentPrecond.add(predicate, generalTerm);
					normAPrecond.add(predicate, termA);
					normBPrecond.add(predicate, termB);	
					existsParentNorm = true;
				}
			}
			/* Equal predicate. Add the pair predicate/term */
			else {
				String term = terms.get(0);
				parentPrecond.add(predicate, term);
				normAPrecond.add(predicate, term);
				normBPrecond.add(predicate, term);
			}
		}

		if(existsParentNorm) {
			this.normA = new Norm(normAPrecond, modality, action);
			this.normB = new Norm(normBPrecond, modality, action);
			this.parent = new Norm(parentPrecond, modality, action);
		}
	}

	/**
	 * Returns {@code normA}, namely one of the child norms that are
	 * generalisable with the other norm {@code normB}
	 * 
	 * @return {@code normA}, namely one of the child norms that are
	 * 					generalisable with the other norm {@code normB}
	 */
	public Norm getNormA() {
		return this.normA;
	}

	/**
	 * Returns {@code normB}, namely one of the child norms that are
	 * generalisable with the other norm {@code normA}
	 * 
	 * @return {@code normB}, namely one of the child norms that are
	 * 					generalisable with the other norm {@code normA}
	 */
	public Norm getNormB() {
		return this.normB;
	}

	/**
	 * Returns the potential parent of norms {@code normA} and {@code normB}
	 * 
	 * @return the potential parent of norms {@code normA} and {@code normB}
	 */
	public Norm getParent() {
		return this.parent;
	}

	/**
	 * Returns a {@code List} of all the norms in the class, namely the 
	 * {@code normA}, the {@code normB}, and the {@code parent} norm
	 * 
	 * @return a {@code List} of all the norms in the class, namely the 
	 * 					{@code normA}, the {@code normB}, and the {@code parent} norm
	 */
	public List<Norm> getAllNorms() {
		return this.allNorms;
	}
}
