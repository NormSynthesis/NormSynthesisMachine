package es.csic.iiia.nsm.strategy;

import java.util.List;
import java.util.Map;

import es.csic.iiia.nsm.config.Goal;
import es.csic.iiia.nsm.norm.NormativeSystem;
import es.csic.iiia.nsm.norm.generation.Conflict;

/**
 * A norm synthesis strategy, which performs the norm synthesis cycle
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
public interface NormSynthesisStrategy {
		
	/**
	 * Executes the strategy, performing the norm synthesis cycle
	 * 
	 * @return the {@code NormativeSystem} system resulting from the
	 * 					norm synthesis cycle
	 */
	public NormativeSystem execute();
	
	/**
	 * Returns a map that saves the conflicts that the strategy has detected
	 * with respect to each goal during the current tick 
	 * 
	 * @return 	a map that saves the conflicts that the strategy has detected
	 * 					with respect to each goal during the current tick
	 */
	public Map<Goal, List<Conflict>> getNonRegulatedConflictsThisTick();
}
