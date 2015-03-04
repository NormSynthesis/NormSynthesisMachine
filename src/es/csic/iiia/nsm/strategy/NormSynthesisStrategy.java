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
		IRON, SIMON, SIMONPlus, LION; 
	}
//	
//	/**
//	 * 
//	 * @param defaultNorms
//	 */
//	public void addDefaultNormativeSystem(List<Norm> defaultNorms);
	
	/**
	 * Executes the strategy, performing the norm synthesis cycle
	 * 
	 * @return the {@code NormativeSystem} system resulting from the
	 * 					norm synthesis cycle
	 */
	public NormativeSystem execute();
	
}
