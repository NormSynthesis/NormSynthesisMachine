package es.csic.iiia.nsm.net.norm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import es.csic.iiia.nsm.NormSynthesisMachine;
import es.csic.iiia.nsm.config.Dimension;
import es.csic.iiia.nsm.config.Goal;
import es.csic.iiia.nsm.config.NormSynthesisSettings;
import es.csic.iiia.nsm.norm.evaluation.Utility;

/**
 * A network is a directed graph whose nodes stand for nodes
 * and whose edges stand for relationships between nodes. Additionally,
 * the network contains the following information about the nodes
 * it contains:
 * <ol>
 * <li>	the state (whether active or inactive) of each node in
 * 			the network; 
 * <li>	the utility of each node, which contains information about how
 * 			the node performs to avoid conflicts with respect to the system
 * 			node evaluation dimensions and goals; and
 * <li>	the generalisation level of each node in the network, which stands
 * 			for the height of the node in the generalisation graph
 * </ol>
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
public class NormSynthesisNetwork<T> {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------
	
	protected DirectedSparseMultigraph<T, NetworkEdge> graph;		// graph of nodes	
	protected NormSynthesisMachine nsm;							// the norm synthesis machine
	protected NormSynthesisSettings nsmSettings;		// the norm synthesis settings
	
	protected Map<T, T> index;											// index of nodes
	protected Map<T, NetworkNodeState> states;			// state of each node
	protected Map<T, Utility> utilities;						// utilities of each node
	
	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	public NormSynthesisNetwork(NormSynthesisMachine nsm) {
		this.nsm = nsm;
		this.nsmSettings = nsm.getNormSynthesisSettings();		
		
		this.index = new HashMap<T, T>();
		this.graph = new DirectedSparseMultigraph<T,NetworkEdge>();
		this.states = new HashMap<T, NetworkNodeState>();
		this.utilities = new HashMap<T, Utility>();
	}

	/**
	 * Adds a given {@code node} to the network if it does not exist yet
	 * 
	 * @param node the node to add
	 */
	public void add(T node) {
		if(!this.graph.containsVertex(node)) {
			this.graph.addVertex(node);
			
			/* Add node to the set of nodes (no repetitions) */
			this.index.put(node, node);
			
			/* Set node utility */
			float normsDefUtility = nsmSettings.getNormsDefaultUtility();
			int perfRangeSz = nsmSettings.getNormsPerformanceRangesSize();
			List<Dimension> dims = this.nsm.getNormEvaluationDimensions();
			List<Goal> goals = this.nsmSettings.getSystemGoals();
			Utility utility = new Utility(normsDefUtility, perfRangeSz, dims, goals);
			this.utilities.put(node, utility);
		}
	}

	/**
	 * Removes a given {@code node} from the network if it exists yet
	 * 
	 * @param node the node to remove
	 */
	public void remove(T node) {
		if(this.graph.containsVertex(node)) {
			this.graph.removeVertex(node);
		}
	}	

	/**
	 * Adds a relationship between a node {@code nA} and a node
	 * {@code nB}, in the direction nA to nB, just in case
	 * the relationship does not exist yet
	 * 
	 * @param nA the child node
	 * @param nB the parent node
	 * @param type the type of the relationship
	 * @see NetworkEdgeType
	 */
	protected void addRelationship(T nA, T nB, NetworkEdgeType type) {
		if(this.graph.findEdge(nA, nB) == null) {
	
			/* Add new edge (relationship) of type "Generalisation" */
			this.graph.addEdge(new NetworkEdge(type), nA, nB);
		}
	}

	/**
	 * Removes a relationship of a certain {@code type} between a node
	 * {@code nA} and a node {@code nB}, just in case the relationship
	 * exists previously
	 * 
	 * @param nA the child node
	 * @param nB the parent node
	 */
	protected void removeRelationship(T nA, T nB, NetworkEdgeType type) {
		List<NetworkEdge> remove = new ArrayList<NetworkEdge>();
		
		for(NetworkEdge edge : this.graph.getOutEdges(nA)) {
			if(edge.getRelationship() == type &&
					this.graph.getDest(edge) == nB)	{
				remove.add(edge);
			}
		}
		for(NetworkEdge edge: remove) {
			this.graph.removeEdge(edge);
		}
	}

	/**
	 * Returns the {@code List} of all the nodes in the network
	 * 
	 * @return the {@code List} of all the nodes in the network
	 */
	public Collection<T> getNodes() {
		return this.graph.getVertices();
	}
	
	/**
	 * Returns a {@code List} of those network edges (that is, the relationships)
	 * that start at node {@code node1} and finish at node {@code node2}
	 * 
	 * @param node1 the initial node
	 * @param node2 the final node
	 * @return a {@code List} of those network edges (that is, the
	 * 					relationships) that start at node {@code node1} and finish
	 * 					at node {@code node2}
	 */
	public List<NetworkEdge> getRelationships(T node1, T node2) {
		List<NetworkEdge> edges = new ArrayList<NetworkEdge>();
		
		Collection<NetworkEdge> outEdges = this.graph.getOutEdges(node1);
		for(NetworkEdge outEdge : outEdges) {
			if(this.graph.getDest(outEdge).equals(node2)) {
				edges.add(outEdge);
			}
		}
		return edges;
	}
	
	/**
	 * Returns the utility of a given {@code node} in the network
	 * 
	 * @param node the node
	 * @return an object {@code Utility}, the utility of the node
	 * @see Utility
	 */
	public Utility getUtility(T node) {
		return this.utilities.get(node);
	}

	/**
	 * Returns the cardinality of the network
	 * 
	 * @return the cardinality of the network
	 */
	public int getCardinality() {
		return this.graph.getVertexCount();
	}

	/**
	 * 
	 * @param node
	 * @return
	 */
	public NetworkNodeState getState(T node) {
		if(this.contains(node)) {
			return this.states.get(node);
		}
		return null;
	}
	
	/**
	 * Sets the new score in the utility of a given {@code node} for a given
	 * {@code Dimension} and {@code Goal}
	 * 
	 * @param node the node
	 * @param dim the dimension of the score (effectiveness/necessity)
	 * @param goal the goal of the score
	 * @param score the new score
	 */
	public void setScore(T node, Dimension dim, Goal goal, float score) {
		this.utilities.get(node).setScore(dim, goal, score);
	}

	/**
	 * 
	 * @param node
	 * @param state
	 */
	public void setState(T node, NetworkNodeState state) {
		if(this.contains(node)) {
			this.states.put(node, state);	
		}
	}

	/**
	 * Returns <tt>true</tt> if the network contains the node
	 * 
	 * @param node the node to search for
	 * @return <tt>true</tt> if the network contains the node
	 */
	public boolean contains(T n)	{
		return this.graph.containsVertex(n);
	}
	
}
