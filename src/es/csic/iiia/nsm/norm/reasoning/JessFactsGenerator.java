package es.csic.iiia.nsm.norm.reasoning;

import es.csic.iiia.nsm.agent.language.TaxonomyOfNaturalNumbers;
import es.csic.iiia.nsm.agent.language.PredicatesDomains;
import es.csic.iiia.nsm.agent.language.SetOfPredicatesWithTerms;
import es.csic.iiia.nsm.agent.language.Taxonomy;
import es.csic.iiia.nsm.agent.language.TaxonomyOfTerms;

/**
 * Tool employed to translate the preconditions of norms and the agent
 * contexts of the Norm Synthesis Machine to facts in the format that the
 * Jess rule engine can interpret
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 * @see JessFactType
 * @see NormEngine
 */
public class JessFactsGenerator {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------
	
	private PredicatesDomains predDomains;

	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * 
	 * @param predicatesDomains the predicates and their domains
	 */
	public JessFactsGenerator(PredicatesDomains predicatesDomains) {
		this.predDomains = predicatesDomains;
	}
	
	/**
	 * Generates a string that describes a fact with the format of Jess
	 * 
	 * @param predicatesWithTerms the facts to translate
	 * @param factType the format that the facts must be generated in
	 * @return the facts in Jess format
	 */
	public String generateFacts(SetOfPredicatesWithTerms predicatesWithTerms, 
			JessFactType factType) {
		
		String facts = "", refCarFact = "";
		String slotLeftSep, slotRightSep, slotValSep;
		String assertLeftSep = "", assertRightSep = " ";

		/* Set syntactic differences between Reasoner and Norm facts */
		slotLeftSep 	= (factType == JessFactType.WorldFact ? "(" : "{");
		slotRightSep 	= (factType == JessFactType.WorldFact ? ")" : "}");
		slotValSep 		= (factType == JessFactType.WorldFact ? " " : " == ");
		
		if(factType == JessFactType.WorldFact) {
			assertLeftSep = "(assert ";
			assertRightSep = ") ";
		}
		facts += assertLeftSep;
		facts += refCarFact;

		for(String predicate : predicatesWithTerms.getPredicates()) {	
			boolean factsAdded = false;
			
			for(String term : predicatesWithTerms.getTerms(predicate)) {	
				Taxonomy taxonomy = predDomains.getDomain(predicate);
				
				/* Natural numbers, put the number or, in case of "any",
				 * don't put the pair predicate(term) */
				if(taxonomy instanceof TaxonomyOfNaturalNumbers) 
				{
					if(!term.equals("any")) {
						factsAdded = true;
						facts += "(" + predicate + slotLeftSep;
						facts += "value" + slotValSep + term;
					}
				}
				
				/* Taxonomy of terms (enumeration) */
				else if (taxonomy instanceof TaxonomyOfTerms) {
					
					/* The top term represents anything, so it is not 
					 * necessary to add it in the jess rule's precondition */
					if(this.predDomains.isTop(predicate, term)) {
						continue;
					}
					
					factsAdded = true;
					facts += "(" + predicate + slotLeftSep;
					int i=0;
					
					TaxonomyOfTerms termsTaxonomy = (TaxonomyOfTerms) taxonomy;
					for(String childTerm : termsTaxonomy.getRepresentedTerms(term)) {
						if(i>0) {
							facts += " || ";
						}
						facts += "value" + slotValSep + childTerm;
						i++;
					}
				}
			}			
			if(factsAdded) {
				facts += slotRightSep + ")";
			}
		}
		facts += assertRightSep;
		return facts;
	}
}