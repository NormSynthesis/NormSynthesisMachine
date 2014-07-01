package es.csic.iiia.nsm.norm.reasoning;

/**
 * Defines the type of a fact generated for the Jess rule engine
 * In Jess, the format of the rules' preconditions and the world facts
 * are slightly different. For this reason, whenever we want to generate a
 * Jess fact from a set of predicates with terms, we must specify the format
 * of the fact we are generating, whether it is for a {@code WorldFact} or
 * a {@code RulePrecondition}.
 *   
 * @author  "Javier Morales (jmorales@iiia.csic.es)"
 */
public enum JessFactType {
	WorldFact, RulePrecondition
}