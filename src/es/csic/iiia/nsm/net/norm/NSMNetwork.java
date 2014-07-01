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
import es.csic.iiia.nsm.norm.Norm;
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
public class NSMNetwork<T> {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------
	
	private DirectedSparseMultigraph<T, NetworkEdge> graph;		// graph of nodes	
	private NormSynthesisMachine nsm;							// the norm synthesis machine
	private NormSynthesisSettings nsmSettings;		// the norm synthesis settings
	
	private Map<T, T> index;											// index of nodes
	private Map<T, NetworkNodeState> states;			// state of each node
	private Map<T, Utility> utilities;						// utilities of each node
	
	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	public NSMNetwork(NormSynthesisMachine nsm) {
		this.nsm = nsm;
		this.nsmSettings = nsm.getNormSynthesisSettings();		
		
		this.graph = new DirectedSparseMultigraph<T,NetworkEdge>();
		
		this.index = new HashMap<T, T>();
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
	 * Sets the state of a node to active in the network
	 * 
	 * @param node the node to activate
	 */
	public void activate(T node) {
		this.states.put(node, NetworkNodeState.Active);
	}
	
	/**
	 * Sets the state of a node to inactive in the network
	 * 
	 * @param node the node to deactivate
	 */
	public void deactivate(T node) {
		this.states.put(node, NetworkNodeState.Inactive);
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
			this.graph.addEdge(new NetworkEdge(type),
					nA, nB);
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
	 * Returns a {@code List} containing all the parents of the given
	 * {@code node} in the network. That is, all those nodes
	 * that are the destination of a generalisation relationship with the
	 * given {@code node}
	 * 
	 * @param node the node
	 * @return a {@code List} containing all the parents of the given
	 * 					{@code node} in the network. That is, all those nodes
	 * 					that are the destination of a generalisation relationship
	 * 					with the given {@code node}
	 */
	public List<T> getParents(T node) {
		List<T> parents = new ArrayList<T>();

		/* The node has no parents (no outgoing generalisation relationships) */
		if(this.graph.getOutEdges(node) == null) {
			return parents;
		}
		/* Check relationships with other nodes */
		for(NetworkEdge edge : this.graph.getOutEdges(node)) {
			
			/* If it is a generalisation relationship, retrieve
			 * its destination (the parent, general node) */ 
			if(edge.getRelationship() == NetworkEdgeType.Generalisation) {
				parents.add(this.graph.getDest(edge));
			}
		}
		return parents;
	}

	/**
	 * Returns a {@code List} containing all the children of the given
	 * {@code node} in the network. That is, all those nodes
	 * that are the source of a generalisation relationship with the
	 * given {@code node}
	 * 
	 * @param node the node
	 * @return a {@code List} containing all the children of the given
	 * 					{@code node} in the network. That is, all those nodes
	 * 					that are the source of a generalisation relationship
	 * 					with the given {@code node}
	 */
	public List<T> getChildren(T node) {
		List<T> children = new ArrayList<T>();

		/* The node has no children (no incoming generalisation relationships) */
		if(this.graph.getInEdges(node) == null) {
			return children;
		}

		/* Check relationships with other nodes */
		for(NetworkEdge edge : this.graph.getInEdges(node))	 {
			
			/* If it is a generalisation relationship, retrieve
			 * its source (the child, specific node) */ 
			if(edge.getRelationship() == NetworkEdgeType.Generalisation) {
				children.add(this.graph.getSource(edge));
			}
		}
		return children;
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
	 * Returns a {@code List} of the nodes that are active in the network
	 * 
	 * @return a {@code List} of the nodes that are active in the network
	 */
	public List<T> getActiveNodes() {
		List<T> ret = new ArrayList<T>();
		
		/* Add the node if it is active */
		for(T node : this.getNodes()) {
			if(this.isActive(node)) {
				ret.add(node);
			}
		}		
		return ret;
	}

	/**
	 * Returns a {@code List} of the nodes that are inactive in the network
	 * 
	 * @return a {@code List} of the nodes that are inactive in the network
	 */
	public List<T> getInactiveNodes() {
		List<T> ret = new ArrayList<T>();
		
		/* Add the node if it is inactive */
		for(T node : this.getNodes()) {
			if(!this.isActive(node)) {
				ret.add(node);
			}
		}		
		return ret;
	}
	
	/**
	 * Returns a {@code List} of all the nodes that whether are active
	 * in the network or are inactive but represented by an active node.
	 * 
	 * @return a {@code List} of all the nodes that whether are active
	 * 					in the network or are inactive but represented by an
	 * 					active node
	 */
	public List<T> getRepresentedNodes() {
		List<T> ret = new ArrayList<T>();
		
		/* Add the node if it is represented by some active ancestor node */
		for(T node : this.getNodes()) {
			if(this.isRepresented(node)) {
				ret.add(node);
			}
		}		
		return ret;
	}
	
	/**
	 * Returns a {@code List} of all the nodes that are not represented
	 * in the network. That is, those nodes that are inactive
	 * in the network and all its ancestors are inactive as well
	 * 
	 * @return a {@code List} of all the nodes that are not represented
	 * 					in the network. That is, those nodes that are inactive
	 *			 		in the network and all its ancestors are
	 *					inactive as well
	 */
	public List<T> getNotRepresentedNodes() {
		List<T> ret = new ArrayList<T>();

		/* Add the node if it is not represented by any active ancestor node */
		for(T node : this.getNodes()) {
			if(!this.isRepresented(node)) {
				ret.add(node);
			}
		}		
		return ret;
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
	 * Returns the cardinality of the network
	 * 
	 * @return the cardinality of the network
	 */
	public int getCardinality() {
		return this.graph.getVertexCount();
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
	
	/**
	 * Returns <tt>true</tt> if the state of the node is
	 * active in the network
	 * 
	 * @return <tt>true</tt> if the state of the node is
	 * 					active in the network
	 */
	public boolean isActive(T node) {
		return this.states.get(node)	== NetworkNodeState.Active;
	}

	/**
	 * Returns <tt>true</tt> if one of the following conditions hold:
	 * <ol>
	 * <li> the given {@code node} is active in the network; or
	 * <li>	the given {@code node} is inactive but some of its ancestors
	 * 			(the nodes that generalise and represent it) are active in
	 * 			the network
	 * </ol>
	 * 
	 * @param node the node
	 * @return <tt>true</tt> if the node is active in the network,
	 * or it is inactive but some of its ancestors are active in the network
	 */
	public boolean isRepresented(T node) {
		if(this.isActive(node)) {
			return true;
		}
		else {
			List<T> parents = this.getParents(node);
			for(T parent : parents) {
				if(this.isRepresented(parent)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Returns <tt>true</tt> if the node is a leaf in the
	 * network, namely it has generalisation level = 0
	 * 
	 * @param node the node
	 * @return <tt>true</tt> if the node is a leaf in the
	 * 					network, namely it has generalisation level = 0
	 */
	public boolean isLeaf(T node) {
		return this.getChildren(node).size() <= 0;
	}
}
