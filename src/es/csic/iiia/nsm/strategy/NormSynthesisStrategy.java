package es.csic.iiia.nsm.strategy;

import es.csic.iiia.nsm.norm.NormativeSystem;

/**
 * A norm synthesis strategy, which performs the norm synthesis cycle
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
public interface NormSynthesisStrategy {
		
	/**
	 * 
	 * @author "Javier Morales (jmorales@iiia.csic.es)"
	 *
	 */
	public enum Option {
		BASE, IRON, SIMON, SIMONPlus, LION; 
	}
	
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
	public boolean hasNonRegulatedConflictsThisTick();
	
	/**
	 * 
	 */
	public void newNonRegulatedConflictsSolvedThisTick();
}
