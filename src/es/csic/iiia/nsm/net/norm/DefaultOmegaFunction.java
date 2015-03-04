package es.csic.iiia.nsm.net.norm;

import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.NormativeSystem;

/**
 * A default omega function that keeps in the normative system
 * those norms that are <tt>active</tt> in the normative network
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
public class DefaultOmegaFunction extends OmegaFunction {

	/**
	 * Updates the normative system. If the received norm is active,
	 * then add the norm to the normative system. Otherwise, if the norm
	 * is inactive, remove it from the normative system
	 * 
	 * @param norm the norm to update
	 * @see NormativeSystem
	 * @see OLDNormativeNetwork
	 */
	public void update(Norm norm, NormativeNetwork network) {
		if(network.getState(norm) == NetworkNodeState.ACTIVE) {
			normativeSystem.add(norm);
		}
		else if(network.getState(norm) != NetworkNodeState.ACTIVE) {
			normativeSystem.remove(norm);
		}
	}
}
	