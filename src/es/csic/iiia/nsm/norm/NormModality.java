package es.csic.iiia.nsm.norm;

/**
 * A norm modality is an obligation/prohibition that allows
 * to build deontic operators of the form "obligation(action)",
 * "prohibition(action)".
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
public enum NormModality {
	Prohibition, Obligation;
	
	/**
	 * Returns a {@code String} that represents the label of the modality
	 * 
	 * @return a {@code String} that represents the label of the modality 
	 */
	public String toString() {
		switch(this) {
		case Prohibition: 	return "prh";
		case Obligation: 		return "obl";
		default:						return "";
		}
	}
}

