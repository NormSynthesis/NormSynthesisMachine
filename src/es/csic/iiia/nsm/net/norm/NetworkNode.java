package es.csic.iiia.nsm.net.norm;

/**
 * A node in the normative network. It may be a {@code Norm}
 * or a {@code NormGroup}
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
public interface NetworkNode {
	
	/**
	 * Returns the id of the node
	 *  
	 * @return the id of the node
	 */
	public int getId();

	/**
	 * Returns a description of the node
	 * 
	 * @return a description of the node
	 */
	public String getDescription();
}
