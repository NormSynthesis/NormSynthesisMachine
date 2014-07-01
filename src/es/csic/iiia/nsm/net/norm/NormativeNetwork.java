/**
 * 
 * @author Javier Morales (jmorales@iiia.csic.es)
 */
package es.csic.iiia.nsm.net.norm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.csic.iiia.nsm.NormSynthesisMachine;
import es.csic.iiia.nsm.agent.AgentAction;
import es.csic.iiia.nsm.agent.language.SetOfPredicatesWithTerms;
import es.csic.iiia.nsm.config.Goal;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.NormModality;
import es.csic.iiia.nsm.norm.NormativeSystem;
import es.csic.iiia.nsm.norm.evaluation.Utility;
import es.csic.iiia.nsm.norm.refinement.xsimon.NormAttribute;

/**
 * A normative network is a directed graph whose nodes stand for norms
 * and whose edges stand for relationships between norms. Additionally,
 * the normative network contains the following information about the norms
 * it contains:
 * <ol>
 * <li>	the state (whether active or inactive) of each norm in
 * 			the normative network; 
 * <li>	the utility of each norm, which contains information about how
 * 			the norm performs to avoid conflicts with respect to the system
 * 			norm evaluation dimensions and goals; and
 * <li>	the generalisation level of each norm in the network, which stands
 * 			for the height of the norm in the generalisation graph
 * </ol>
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 * @see Norm
 * @see Utility
 */
public class NormativeNetwork extends NSMNetwork<Norm> {

	//---------------------------------------------------------------------------
	// Static attributes
	//---------------------------------------------------------------------------
	
	private static int NORM_COUNT = 0;			// number of norms in the network

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------

	private OmegaFunction omegaFunction;								// the omega function
	private Map<Integer, Norm> ids;											// nodes identifiers	
	private Map<Norm, List<NormAttribute>> attributes; 	// norm attributes
	private Map<Norm, Integer> genLevels;								// generalisation levels
	
	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------

	/**
	 * @param nsm
	 */
	public NormativeNetwork(NormSynthesisMachine nsm) {
		super(nsm);		
		
		this.ids = new HashMap<Integer, Norm>();
		this.attributes = new HashMap<Norm, List<NormAttribute>>();
		this.genLevels = new HashMap<Norm, Integer>();
	}

	/**
	 * Adds a given {@code norm} to the normative network if it does
	 * not exist yet. Additionally, it sets the utility for the new norm
	 * and sets its generalisation level
	 * 
	 * @param norm the norm to add
	 */
	public void add(Norm norm) {
		if(!this.contains(norm)) {

			norm.setId(++NORM_COUNT);
			super.add(norm);

			/* Set the generalisation level of the norm */
			this.genLevels.put(norm, 1); 
			
			/* Index norm for fast access */
			this.ids.put(norm.getId(), norm);
			this.attributes.put(norm, new ArrayList<NormAttribute>());
		}
	}
	
	/**
	 * 
	 * @param norm
	 * @param tag
	 */
	public void addAttribute(Norm norm, NormAttribute attribute) {
		if(!this.attributes.containsKey(norm)) {
			this.attributes.put(norm, new ArrayList<NormAttribute>());
		}
		List<NormAttribute> attributes = this.attributes.get(norm);
		if(!attributes.contains(attribute)) {
			attributes.add(attribute);
		}
	}
	
	/**
	 * 
	 * @param norm
	 * @param attributes
	 */
	public void addAttributes(Norm norm, List<NormAttribute> attributes) {
		for(NormAttribute attr : attributes) {
			this.addAttribute(norm, attr);
		}
	}

	/**
	 * 
	 * @param norm
	 * @param attributes
	 */
	public void addAttributes(Map<Norm,List<NormAttribute>> attributes) {
		for(Norm norm : attributes.keySet()) {
			this.addAttributes(norm, attributes.get(norm));
		}
	}
	
	/**
	 * 
	 * @param norm
	 * @param tag
	 */
	public void removeAttribute(Norm norm, NormAttribute attribute) {
		if(this.attributes.containsKey(norm)) {
			List<NormAttribute> attributes = this.attributes.get(norm);
			if(attributes.contains(attribute)) {
				int idx = attributes.indexOf(attribute);
				attributes.remove(idx);
			}
		}
	}
	
	/**
	 * 
	 * @param child
	 * @param parent
	 */
	public void addGeneralisation(Norm child, Norm parent) {
		super.addRelationship(child, parent, NetworkEdgeType.Generalisation);
		
		/* Set the level of the parent node */
		int gLevel = this.genLevels.get(child);
		this.genLevels.put(parent, gLevel+1);	
	}
	
	/**
	 * 
	 * @param child
	 * @param parent
	 */
	public void removeGeneralisation(Norm child, Norm parent) {
		super.removeRelationship(child, parent, NetworkEdgeType.Generalisation);
	}

	/**
	 * 
	 * @param child
	 * @param parent
	 */
	public void addSubstitutability(Norm nA, Norm nB) {
		super.addRelationship(nA, nB, NetworkEdgeType.Substitutability);
		super.addRelationship(nB, nA, NetworkEdgeType.Substitutability);
	}
	
	/**
	 * Sets the state of a norm to active in the normative
	 * network, and updates the omega function to update
	 * the normative system
	 * 
	 * @param norm the norm to activate
	 * @see OmegaFunction
	 */
	public void activate(Norm norm) {
		super.activate(norm);
		this.omegaFunction.update(norm, this);
	}

	/**
	 * Sets the state of a norm to inactive in the normative
	 * network, and updates the omega function to update
	 * the normative system
	 * 
	 * @param norm the norm to activate
	 * @see OmegaFunction
	 */
	public void deactivate(Norm norm) {
		super.deactivate(norm);
		this.omegaFunction.update(norm, this);
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
	public List<Norm> getBrothers(Norm norm) {
		List<Norm> brothers = new ArrayList<Norm>();
		List<Norm> parents = this.getParents(norm);

		for(Norm parent : parents) {
			List<Norm> children = this.getChildren(parent);
			
			for(Norm child : children) {
				if(!child.equals(norm) && !brothers.contains(child)
						&& this.getGeneralisationLevel(norm) == 
						this.getGeneralisationLevel(child)) {
					
					brothers.add(child);
				}
			}
		}
		return brothers;
	}
	
	/**
	 * Returns the {@code List} of all the norms in the network
	 * 
	 * @return the {@code List} of all the norms in the network
	 */
	public Collection<Norm> getNorms() {
		return super.getNodes();
	}
	
	/**
	 * Returns a {@code List} of the norms that are
	 * active in the normative network
	 * 
	 * @return a {@code List} of the norms that are
	 * 					active in the normative network
	 */
	public List<Norm> getActiveNorms() {
		return super.getActiveNodes();
	}
	
	/**
	 * Returns a {@code List} of the norms that are
	 * inactive in the normative network
	 * 
	 * @return a {@code List} of the norms that are
	 * 					inactive in the normative network
	 */
	public List<Norm> getInactiveNorms() {
		return super.getInactiveNodes();
	}

	/**
	 * Returns a {@code List} of all the norms that whether are active
	 * in the network or are inactive but represented by an active norm.
	 * 
	 * @return a {@code List} of all the norms that whether are active
	 * 					in the network or are inactive but represented by an
	 * 					active norm
	 */
	public List<Norm> getRepresentedNorms() {
		return this.getRepresentedNodes();
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
	public List<Norm> getNotRepresentedNorms() {
		return super.getNotRepresentedNodes();
	}

	/**
	 * Returns a norm in the normative network with the given
	 * {@code precondition}, {@code modality} and {@code action}
	 * 
	 * @param precondition the norm precondition
	 * @param modality the norm modality
	 * @param action the regulated action
	 * @param goal the goal regulated by the norm
	 * @return the norm with the given elements
	 */
	public Norm getNorm(SetOfPredicatesWithTerms precondition,
			NormModality modality, AgentAction action, Goal goal) {
		
		Norm n = new Norm(precondition, modality, action, goal);
		for(Norm norm : this.getNodes()) {
			if(n.equals(norm)) {
				return norm;
			}
		}
		return null;
	}

	/**
	 * Returns the norm with the given {@code id}
	 * 
	 * @param id the id of the norm
	 * @return the norm with the given id
	 */
	public Norm getNormWithId(int id)  {
		return this.ids.get(id);
	}
	
	/**
	 * Returns the generalisation level of a norm in the network.
	 * The generalisation level indicates the position (i.e., height) of the
	 * norm in the network. As an example, while a "leaf" norm in the network
	 * has level 0, its immediate parent has level 1, and the parent of the
	 * parent has level 2  
	 * 
	 * @param norm the norm
	 * @return the generalisation level of the norm
	 */
	public int getGeneralisationLevel(Norm norm) {
		return this.genLevels.get(norm);
	}
	
	/**
	 * Returns the normative system represented by this normative network
	 * 
	 * @return the normative system represented by this normative network
	 * @see OmegaFunction
	 */
	public NormativeSystem getNormativeSystem() {
		return this.omegaFunction.getNormativeSystem();
	}

	/**
	 * Returns the list of tags assigned to the {@code norm} received 
	 * by parameter
	 *  
	 * @param norm the norm 
	 * @return the list of tags assigned to the {@code norm} received 
	 * 					by parameter
	 */
	public List<NormAttribute> getTags(Norm norm) {
		return this.attributes.get(norm);
	}
	
	/**
	 * 
	 * @param ancestor
	 * @param norm
	 * @return
	 */
	public boolean isAncestor(Norm ancestor, Norm norm) {
		List<Norm> parents = this.getParents(norm);
		
		for(Norm parent : parents) {
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
	 * Returns <tt>true</tt> if the given {@code norm} has been assigned
	 * the tag <i>Ineffective</i>
	 * 
	 * @param norm the norm to check
	 * @return <tt>true</tt> if the given {@code norm} has been assigned
	 * 					the tag <i>Ineffective</i>
	 */
	public boolean isIneffective(Norm norm) {
		return this.attributes.get(norm).contains(NormAttribute.Ineffective);
	}
	
	/**
	 * Returns <tt>true</tt> if the given {@code norm} has been assigned
	 * the tag <i>Unnecessary</i>
	 * 
	 * @param norm the norm to check
	 * @return <tt>true</tt> if the given {@code norm} has been assigned
	 * 					the tag <i>Unnecessary</i>
	 */
	public boolean isUnnecessary(Norm norm) {
		return this.attributes.get(norm).contains(NormAttribute.Unnecessary);
	}

	/**
	 * Returns <tt>true</tt> if the given {@code norm} has been assigned
	 * the tag <i>Generalisable</i>
	 * 
	 * @param norm the norm to check
	 * @return <tt>true</tt> if the given {@code norm} has been assigned
	 * 					the tag <i>Generalisable</i>
	 */
	public boolean isGeneralisable(Norm norm) {
		return this.attributes.get(norm).contains(NormAttribute.Generalisable);
	}
	
	/**
	 * Returns <tt>true</tt> if the given {@code norm} has been assigned
	 * the tag <i>Substitutable</i>
	 * 
	 * @param norm the norm to check
	 * @return <tt>true</tt> if the given {@code norm} has been assigned
	 * 					the tag <i>Substitutable</i>
	 */
	public boolean isSubstitutable(Norm norm) {
		return this.attributes.get(norm).contains(NormAttribute.Substitutable);
	}
	
	/**
	 * Returns <tt>true</tt> if the given {@code norm} has been assigned
	 * the tag <i>Exclussive</i>
	 * 
	 * @param norm the norm to check
	 * @return <tt>true</tt> if the given {@code norm} has been assigned
	 * 					the tag <i>Exclussive</i>
	 */
	public boolean isExclussive(Norm norm) {
		return this.attributes.get(norm).contains(NormAttribute.Exclussive);
	}
	
	/**
	 * Returns <tt>true</tt> if the given {@code norm} has been assigned
	 * the tag <i>Complementary</i>
	 * 
	 * @param norm the norm to check
	 * @return <tt>true</tt> if the given {@code norm} has been assigned
	 * 					the tag <i>Complementary</i>
	 */
	public boolean isComplementary(Norm norm) {
		return this.attributes.get(norm).contains(NormAttribute.Complementary);
	}
	
	/**
	 * Returns <tt>true</tt> if a given pair of norms {@code n1} and {@code n2}
	 * have a substitutability relationship in this normative network
	 * 
	 * @param n1 the first norm
	 * @param n2 the second norm
	 * @return <tt>true</tt> if a given pair of norms {@code n1} and {@code n2}
	 * 					have a substitutability relationship in this normative network
	 */
	public boolean areSubstitutable(Norm n1, Norm n2) {
		List<NetworkEdge> edges = this.getRelationships(n1, n2);
		
		for(NetworkEdge edge : edges) {
			if(edge.getRelationship() == NetworkEdgeType.Substitutability) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Sets the omega function, namely the function that computes the 
	 * normative system from the normative network
	 * 
	 * @param omegaFunction the new omega function 
	 * @see OmegaFunction
	 */
	public void setOmegaFunction(OmegaFunction omegaFunction) {
		this.omegaFunction = omegaFunction;
	}
	
	/**
	 * Returns <tt>true</tt> if the normative network contains a norm 
	 * with the given {@code precondition}, {@code modality} and {@code action}
	 * 
	 * @param precondition the norm precondition
	 * @param modality the norm modality
	 * @param action the regulated action
	 * @param goal the goal regulated by the norm
	 * @return <tt>true</tt> if the normative network contains a norm 
	 * 					with the given {@code precondition}, {@code modality}
	 * 					and {@code action}
	 */
	public boolean contains(SetOfPredicatesWithTerms precondition, 
			NormModality modality, AgentAction action, Goal goal) {
		
		Norm n = new Norm(precondition, modality, action, goal);
		for(Norm norm : this.getNodes()) {
			if(n.equals(norm)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns <tt>true</tt> if the normative network contains the norm
	 * 
	 * @param norm the norm to search for
	 * @return <tt>true</tt> if the normative network contains the norm
	 */
	public boolean contains(Norm n)	{
		for(Norm norm : this.getNorms()) {
			if(n.equals(norm)) {
				return true;
			}
		}
		return false;
	}
}
