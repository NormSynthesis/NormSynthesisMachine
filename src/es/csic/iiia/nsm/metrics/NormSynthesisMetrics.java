package es.csic.iiia.nsm.metrics;

import java.util.List;

import es.csic.iiia.nsm.config.NormSynthesisSettings;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.evaluation.Utility;

/**
 * Metrics of the norm synthesis machine
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
public interface NormSynthesisMetrics {
	
	/**
	 * Returns <tt>true</tt> if the normative system has changed
	 * (norms added/removed) during the current time step
	 * 
	 * @return <tt>true</tt> if the normative system has changed
	 * 					(norms added/removed) during the current time step
	 */
	public boolean hasNormativeSystemChangedThisTick();
	
	/**
	 * Returns <tt>true</tt> if the NSM has converged to a normative system
	 * 
	 * @return <tt>true</tt> if the NSM has converged to a normative system
	 */
	public boolean hasConverged();
	
	/**
	 * Returns the cardinality of the normative network
	 * 
	 * @return the cardinality of the normative network
	 */
	public int getNormativeNetworkCardinality();
	
	/**
	 * Returns  the cardinality of the normative system
	 * 
	 * @return the cardinality of the normative system
	 */
	public int getNormativeSystemCardinality();
	
	/**
	 * Returns the {@code List} of norms that have been added to
	 * the normative network during the current tick
	 * 
	 * @return the {@code List} of norms that have been added to
	 *					the normative network during the current tick
	 */
	public List<Norm> getNormsAddedThisTick();
	
	/**
	 * Returns the {@code List} of norms that have been removed
	 * from the normative network during the current tick
	 * 
	 * @return the {@code List} of norms that have been removed
	 * 					from the normative network during the current tick
	 */
	public List<Norm> getNormsRemovedThisTick();
	
	/**
	 * Returns the norm synthesis settings
	 * 
	 * @return the norm synthesis settings
	 * @see NormSynthesisSettings
	 */
	public NormSynthesisSettings getNormSynthesisSettings();
	
	/**
	 * Returns the utility of a given norm in the normative network 
	 * 
	 * @param norm the norm
	 * @return the utility of the norm
	 * @see Utility
	 */
	public Utility getNormUtility(Norm norm);
	
	/**
	 * Updates the metrics
	 */
	public void update();
}
