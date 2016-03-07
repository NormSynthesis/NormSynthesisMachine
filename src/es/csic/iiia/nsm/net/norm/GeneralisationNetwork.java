package es.csic.iiia.nsm.net.norm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.csic.iiia.nsm.NormSynthesisMachine;

/**
 * A generalisation network is a norm synthesis network whose nodes
 * stand for nodes and whose edges stand for generalisation
 * relationships between nodes. Additionally, the network contains
 * the following information about the nodes it contains:
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
public class GeneralisationNetwork<T> extends NormSynthesisNetwork<T>{

	//---------------------------------------------------------------------------
	// Attributes 
	//---------------------------------------------------------------------------
	
	protected Map<T, Integer> genLevels;								// generalisation levels
	
	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	public GeneralisationNetwork(NormSynthesisMachine nsm) {
		super(nsm);
		this.genLevels = new HashMap<T, Integer>();
	}

	/**
	 * Adds a given {@code node} to the network if it does not exist yet
	 * 
	 * @param node the node to add
	 */
	public void add(T node) {
		/* Set the generalisation level of the node */
		this.genLevels.put(node, 1); 
		super.add(node);
	}
	
	
	/**
	 * 
	 * @param child
	 * @param parent
	 */
	public void addGeneralisation(T child, T parent) {
		super.addRelationship(child, parent, NetworkEdgeType.GENERALISATION);
		
		/* Set the level of the parent node */
//		int gLevel = this.genLevels.get(child);
//		this.genLevels.put(parent, gLevel+1);	
		
		// TODO: REPASAR ESTO... he comentado las dos lineas de arriba por los problemas que daba con Iosu.
		int childGenLevel = this.genLevels.get(child);
		int parentGenLevel = this.genLevels.get(parent);
		this.genLevels.put(parent, Math.max((childGenLevel+1), parentGenLevel));	
	}
	
	/**
	 * 
	 * @param child
	 * @param parent
	 */
	public void removeGeneralisation(T child, T parent) {
		super.removeRelationship(child, parent, NetworkEdgeType.GENERALISATION);
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
			if(edge.getRelationship() == NetworkEdgeType.GENERALISATION) {
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
			if(edge.getRelationship() == NetworkEdgeType.GENERALISATION) {
				children.add(this.graph.getSource(edge));
			}
		}
		return children;
	}
	

	/**
	 * Returns a {@code List} containing all the brothers of the given
	 * {@code node} in the network. That is, all those nodes
	 * that are children of the node's parents
	 * 
	 * @param node the node
	 * @return a {@code List} containing all the brothers of the given
	 * 				{@code node} in the network. That is, all those nodes
	 * 				that are children of the node's parents
	 */
	public List<T> getBrothers(T node) {
		List<T> brothers = new ArrayList<T>();
		List<T> parents = this.getParents(node);

		for(T parent : parents) {
			List<T> children = this.getChildren(parent);
			
			for(T child : children) {
				if(!child.equals(node) && !brothers.contains(child)
						&& this.getGeneralisationLevel(node) == 
						this.getGeneralisationLevel(child)) {
					
					brothers.add(child);
				}
			}
		}
		return brothers;
	}

	/**
	 * Returns a {@code List} containing all the brothers of the given
	 * {@code node} from a parent in the network. That is, all those nodes
	 * that are children of the node's parents
	 * 
	 * @param node the node
	 * @return a {@code List} containing all the brothers of the given
	 * 				{@code node} from a parent in the network. That is, all
	 * 				those nodes that are children of the node's parents
	 */
	public List<T> getBrothers(T node, T parent) {
		List<T> brothers = new ArrayList<T>();
		List<T> children = this.getChildren(parent);
		
		for(T child : children) {
			if(!child.equals(node) && !brothers.contains(child)
					&& this.getGeneralisationLevel(node) == 
					this.getGeneralisationLevel(child)) {
				
				brothers.add(child);
			}
		}
		return brothers;
	}
	
	/**
	 * Returns the top boundary of the network. That is,
	 * those nodes that are in the top of it (have no parents)
	 * 
	 * @return the 	top boundary of the network. That is, those nodes
	 * 							that are in the top of it (have no parents)
	 */
	public List<T> getTopBoundary() {
		List<T> ret = new ArrayList<T>();
		
		for(T node : this.getNodes()) {
			if(this.getParents(node).isEmpty()) {
				ret.add(node);
			}
		}
		return ret;
	}
	
	/**
	 * Returns a {@code List} of the norms that are active in the network
	 * 
	 * @return a {@code List} of the norms that are active in the network
	 */
	public List<T> getActiveNodes() {
		List<T> ret = new ArrayList<T>();
		
		/* Add the node if it is active */
		for(T node : this.getNodes()) {
			if(this.getState(node) == NetworkNodeState.ACTIVE) {
				ret.add(node);
			}
		}		
		return ret;
	}

	/**
	 * Returns a {@code List} of the norms that are inactive in the network
	 * 
	 * @return a {@code List} of the norms that are inactive in the network
	 */
	public List<T> getInactiveNodes() {
		List<T> ret = new ArrayList<T>();
		
		/* Add the node if it is active */
		for(T node : this.getNodes()) {
			if(this.getState(node) != NetworkNodeState.ACTIVE) {
				ret.add(node);
			}
		}		
		return ret;
	}
	
	/**
	 * Returns a {@code List} of all the norms that whether are active
	 * in the network or are inactive but represented by an active norm.
	 * 
	 * @return a {@code List} of all the norms that whether are active
	 * 					in the network or are inactive but represented by an
	 * 					active norm
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
	 * Returns a {@code List} of all the norms that are not represented
	 * in the normative network. That is, those norms that are inactive
	 * in the normative network and all its ancestors are inactive as well
	 * 
	 * @return a {@code List} of all the norms that are not represented
	 * 					in the normative network. That is, those norms that are inactive
	 *			 		in the normative network and all its ancestors are
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
	 * Returns the generalisation level of a node in the network.
	 * The generalisation level indicates the position (i.e., height) of the
	 * node in the network. As an example, while a "leaf" node in the network
	 * has level 0, its immediate parent has level 1, and the parent of the
	 * parent has level 2  
	 * 
	 * @param node the node
	 * @return the generalisation level of the node
	 */
	public int getGeneralisationLevel(T node) {
		return this.genLevels.get(node);
	}
	
//	/**
//	 * Sets the generalisation level of a node in the network.
//	 * The generalisation level indicates the position (i.e., height) of the
//	 * node in the network. As an example, while a "leaf" node in the network
//	 * has level 0, its immediate parent has level 1, and the parent of the
//	 * parent has level 2  
//	 * 
//	 * @param node the node
//	 * @param genLevel the generalisation level of the node
//	 */
//	public void setGeneralisationLevel(T node, int genLevel) {
//		this.genLevels.put(node, genLevel);
//	}

	/**
	 * Returns <tt>true</tt> if one of the following conditions hold:
	 * <ol>
	 * <li> the given {@code norm} is active in the network; or
	 * <li>	the given {@code norm} is inactive but some of its ancestors
	 * 			(the nodes that generalise and represent it) are active in
	 * 			the network
	 * </ol>
	 * 
	 * @param norm the norm
	 * @return <tt>true</tt> if the norm is active in the network,
	 * or it is inactive but some of its ancestors are active in the network
	 */
	public boolean isRepresented(T node) {
		if(this.getState(node) == NetworkNodeState.ACTIVE) {
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
	 * 
	 * @param ancestor
	 * @param norm
	 * @return
	 */
	public boolean isAncestor(T ancestor, T node) {
		List<T> parents = this.getParents(node);
		for(T parent : parents) {
			if(parent.equals(ancestor)) {
				return true;
			}
			else {
				return this.isAncestor(ancestor, parent);
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
