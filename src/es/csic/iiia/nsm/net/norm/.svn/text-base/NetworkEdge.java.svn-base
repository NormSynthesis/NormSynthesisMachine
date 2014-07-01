package es.csic.iiia.nsm.net.norm;

/**
 * An edge between two norms in the normative network. Since norms may have
 * different types of relationships, the {@code NormativeNetworkEdge} contains
 * a attribute {@code relationship} that describes the type of relationship
 * that the edge represents
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 * @see NetworkEdgeType
 */
public class NetworkEdge {

	//---------------------------------------------------------------------------
	// Attributes 
	//---------------------------------------------------------------------------

	private NetworkEdgeType relationship; 	// relationship between nodes	

	//---------------------------------------------------------------------------
	// Methods 
	//---------------------------------------------------------------------------

	/**
	 * Constructor
	 * 
	 * @param relationship the type of relationship that the edge represents
	 * @param predicate
	 */
	public NetworkEdge(NetworkEdgeType relationship) {
		this.relationship = relationship;
	}

	/**
	 * Returns the relationship that the edge represents
	 * 
	 * @return the relationship that the edge represents
	 */
	public NetworkEdgeType getRelationship() {
		return this.relationship;
	}

	/**
	 * Returns a {@code String} that describes the edge
	 * 
	 * @return a {@code String} that describes the edge
	 */
	public String toString() {
		return this.relationship.toString();
	}
}
