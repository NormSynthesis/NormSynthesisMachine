package es.csic.iiia.nsm.norm.generation;

import java.util.List;

import es.csic.iiia.nsm.config.DomainFunctions;
import es.csic.iiia.nsm.config.Goal;
import es.csic.iiia.nsm.norm.Norm;

/**
 * A norm generation machine generates norms from conflicts.
 * Each generated norm is aimed to regulate the given conflict,
 * trying to prevent it from happening again in the future
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 * @see Goal
 * @see Conflict
 */
public interface NormGenerationMachine {

	/**
	 * Generates norms to regulate a conflict. Each generated norm is aimed
	 * to regulate the given conflict, trying to prevent it from happening
	 * again in the future
	 * 
	 * @param conflict the conflict to regulate
	 * @param dmInputs domain-dependent inputs for norm synthesis
	 * @param goal the goal with respect to which generate norms
	 * @return the {@code List} of norms to regulate the conflict
	 * @see Conflict
	 * @see Norm
	 * @see Goal
	 */
	public List<Norm> generateNorms(Conflict conflict,
			DomainFunctions dmInputs, Goal goal);
}
