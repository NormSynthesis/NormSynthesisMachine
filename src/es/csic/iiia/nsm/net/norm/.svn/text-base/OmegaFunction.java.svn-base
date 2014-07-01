package es.csic.iiia.nsm.net.norm;

import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.NormativeSystem;

/**
 * An omega function returns the normative system represented by
 * a normative network. For simplicity, this class does not contain 
 * a normative network (not to recompute the normative system
 * from the normative network every time that the getter is called).
 * Alternatively, the omega function receives (by means of method
 * update) a norm that has changed its state in the normative network,
 * and then the particular implementation of the omega function decides
 * what to do with the norm, whether adding or removing it from
 * the normative system
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 *
 */
public abstract class OmegaFunction {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------

	protected NormativeSystem normativeSystem;	// The normative system

	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	public OmegaFunction() {
		this.normativeSystem = new NormativeSystem();	
	}
	
	/**
	 * Returns the normative system represented by the normative network
	 * 
	 * @return the normative system represented by the normative network
	 * @see NormativeSystem
	 */
	public NormativeSystem getNormativeSystem() {
		return this.normativeSystem;
	}
	
	/**
	 * Updates the normative system. If the received norm is active,
	 * then add the norm to the normative system. Otherwise, if the norm
	 * is inactive, remove it from the normative system
	 * 
	 * @param norm the norm to update
	 * @param state the norm state
	 */
	public abstract void update(Norm norm, NormativeNetwork network);
	
}
