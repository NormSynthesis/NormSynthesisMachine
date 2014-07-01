package es.csic.iiia.nsm.norm.refinement.iron;

import java.util.ArrayList;
import java.util.List;

import es.csic.iiia.nsm.agent.language.PredicatesDomains;
import es.csic.iiia.nsm.agent.language.SetOfPredicatesWithTerms;
import es.csic.iiia.nsm.agent.language.SetOfStrings;
import es.csic.iiia.nsm.agent.language.TaxonomyOfTerms;
import es.csic.iiia.nsm.config.DomainFunctions;

/**
 * Class containing methods to help IRON to generate potential generalisations.
 * Specifically, this class contains methods to:
 * <ol>
 * <li> retrieve the potential generalisations of a given agent context;
 * <li> retrieve the agent contexts that a general context represents.
 * </ol>
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
public class GeneralisationReasoner {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------

	private DomainFunctions dmFunctions;
	private PredicatesDomains predDomains;

	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------

	/**
	 * Constructor
	 * 
	 * @param dmFunctions the domain functions
	 */
	public GeneralisationReasoner(PredicatesDomains predDomains, 
			DomainFunctions dmFunctions) {
		
		this.predDomains = predDomains;
		this.dmFunctions = dmFunctions;
	}

	/**
	 * Returns a {@code List} of the potential generalisations of a given 
	 * agent context {@code agContext}. Specifically, this method generates a
	 * potential generalisation for each term of each predicate in the original
	 * agent context.
	 * <p>
	 * As an example, consider a traffic scenario with cars that
	 * have different colours: <tt>{redCar, blueCar}</tt>.
	 * Consider that all terms <tt>redCar and blueCar</tt> may be generalised
	 * to a third term <tt>car</tt>, which does not specify any colour. 
	 * Consider now an agent context like "left(redCar)&right(blueCar)",
	 * which describes the situation of an agent that perceives a red car 
	 * to its left, and a blue car to its right. The potential generalisations
	 * of this agent context are:
	 * <ol>
	 * <li> "left(car)&right(blueCar)"; and
	 * <li> "left(redCar)&right(car)".
	 * </ol>
	 * 
	 * Notice that each potential generalisation generalises one term
	 * of a predicate. For instance, the first potential generalisation
	 * holds whenever an agent perceives a car (whether red or blue) to its
	 * left, and a blue car to its right. 
	 * 
	 * @param agContext the agent context to generalise
	 * @return a {@code List} of the potential generalisations of a given 
	 * 					agent context {@code agContext}. Specifically, this method 
	 * 					generates a potential generalisation for each term of each
	 * 					predicate in the original agent context.
	 */
	public List<SetOfPredicatesWithTerms> getParentContexts(
			SetOfPredicatesWithTerms agContext) {

		SetOfStrings predicates = agContext.getPredicates();
		SetOfPredicatesWithTerms parentPrecond;
		List<SetOfPredicatesWithTerms> parentContexts = 
				new ArrayList<SetOfPredicatesWithTerms>();

		/* For each term of each predicate, we search for a possible
		 * generalisation and generate a new potential parent context */
		for(String predToGen : predicates) {
			SetOfStrings terms = agContext.getTerms(predToGen);

			for(String termToGen : terms) {

				/* If the term to generalise is in the top of
				 * the taxonomy, do not generalise the term */
				if(this.predDomains.isTop(predToGen, termToGen)) {
					continue;
				}
				
				/* Get generalisation of the term */
				String parentTerm = this.predDomains.
						getImmediateParentTerm(predToGen, termToGen);

				/* Create and fill parent precondition */
				parentPrecond = new SetOfPredicatesWithTerms();

				/* Fill predicates in the same order than the original agent context */
				for(String pred : agContext.getPredicates()) {

					/* If "pred" is the predicate to generalise,
					 * fill with the general term */
					if(pred.equals(predToGen)) {
						parentPrecond.add(pred, parentTerm);
					}

					/* Otherwise, fill with a copy of the set of terms in the
					 * original agent context */
					else {
						parentPrecond.add(pred, agContext.getTerms(pred));
					}
				}
				parentContexts.add(parentPrecond);
			}
		}
		return parentContexts;
	}

	/**
	 * Returns a {@code List} of the potential specialisations of a given 
	 * agent context {@code agContext} <b>that are consistent</b>. That is, 
	 * all those potential specialisations which description "make sense"
	 * in the specific domain where the NSM performs norm synthesis.
	 * <p>
	 * This method calls private method {@code getAllChildContexts} to
	 * generate all the potential specialisations of the agent context,
	 * but it just returns those that "are consistent" in the specific
	 * domain.
	 *
	 * @param agContext the agent context to generalise
	 * @return a {@code List} of the potential specialisations of a given 
	 * 					agent context {@code agContext} <b>that are consistent</b>.
	 * 					That is, all those potential specialisations which
	 * 					description "make sense" in the specific domain where the
	 * 					NSM performs norm synthesis.
	 */
	public List<SetOfPredicatesWithTerms> getChildContexts(
			SetOfPredicatesWithTerms agContext) {
		
		List<SetOfPredicatesWithTerms> childContexts = 
				new ArrayList<SetOfPredicatesWithTerms>();

		/* Generate all child nodes */
		List<SetOfPredicatesWithTerms> allChildContexts =
				this.getAllChildContexts(agContext);

		/* Get only the child terms that are consistent */
		for(SetOfPredicatesWithTerms child : allChildContexts) {
			if(this.dmFunctions.isConsistent(child)) {
				childContexts.add(child);
			}
		}
		return childContexts;
	}

	/**
	 * Returns a {@code List} of the potential specialisations of a given 
	 * agent context {@code agContext}. Specifically, this method generates a
	 * potential specialisation for each term of each predicate in the original
	 * agent context.
	 * <p>
	 * As an example, consider a traffic scenario with cars that
	 * have different colours: <tt>{redCar, blueCar}</tt>.
	 * Consider that all terms <tt>redCar and blueCar</tt> may be generalised
	 * to a third term <tt>car</tt>, which does not specify any colour. 
	 * Consider now an agent context like "left(car)&right(car)",
	 * which describes the situation of an agent that perceives a car, 
	 * (whether red or blue) to is left and right positions.
	 * The potential generalisations of this agent context are:
	 * <ol>
	 * <li> "left(redCar)&right(car)";
	 * <li> "left(blueCar)&right(car)";
	 * <li> "left(car)&right(redCar)";
	 * <li> "left(car)&right(blueCar)"
	 * </ol>
	 * 
	 * Notice that this method generates a potential specialisation
	 * for each possible value of each general term in the original agent context.
	 * Thus, since the term <tt>car</tt> represents two values, <tt>redCar</tt>
	 * and <tt>blueCar</tt>, and the agent context has two terms <tt>car</tt>, 
	 * the method generates 2x2=4 potential specialisations.
	 *
	 * @param agContext the agent context to generalise
	 * @return a {@code List} of the potential specialisations of a given 
	 * 					agent context {@code agContext}. Specifically, this method
	 * 					generates a potential specialisation for each term of each
	 * 					predicate in the original agent context.
	 */
	private List<SetOfPredicatesWithTerms> getAllChildContexts(
			SetOfPredicatesWithTerms agContext) {
		
		SetOfStrings predicates = agContext.getPredicates();
		SetOfPredicatesWithTerms childPrecond;
		List<SetOfPredicatesWithTerms> childContexts = 
				new ArrayList<SetOfPredicatesWithTerms>();

		/* For each term of each predicate, we search for a possible
		 * generalisation and generate a new potential parent context */
		for(String predToSpec : predicates) {
			SetOfStrings terms = agContext.getTerms(predToSpec);

			for(String termToSpec : terms) {

				/* Get generalisation of the term */
				TaxonomyOfTerms childTerms = (TaxonomyOfTerms) this.predDomains.
						getDomain(predToSpec);

				/* Fill predicates in the same order than the original agent context */
				for(String childTerm : childTerms.getRepresentedTerms(termToSpec)) {
					if(childTerm.equals(termToSpec)) {
						continue;
					}
					
					/* Create and fill parent precondition */
					childPrecond = new SetOfPredicatesWithTerms();
					
					for(String pred : agContext.getPredicates()) {

						/* If "pred" is the predicate to generalise,
						 * fill with the general term */
						if(pred.equals(predToSpec)) {
							childPrecond.add(pred, childTerm);
						}

						/* Otherwise, fill with a copy of the set of terms in the
						 * original agent context */
						else {
							childPrecond.add(pred, agContext.getTerms(pred));
						}						
					}
					childContexts.add(childPrecond);
				}
			}
		}
		return childContexts;
	}
}
