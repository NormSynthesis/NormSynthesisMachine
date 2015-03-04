package es.csic.iiia.nsm.norm.group.net;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.csic.iiia.nsm.NormSynthesisMachine;
import es.csic.iiia.nsm.net.norm.GeneralisationNetwork;
import es.csic.iiia.nsm.net.norm.NetworkNodeState;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.evaluation.NormCompliance;
import es.csic.iiia.nsm.norm.group.NormGroup;
import es.csic.iiia.nsm.norm.group.NormGroupCombination;

/**
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 *
 */
public class NormGroupNetwork extends GeneralisationNetwork<NormGroup> {

	//---------------------------------------------------------------------------
	// Static attributes
	//---------------------------------------------------------------------------
	
	private static int NORM_GROUP_COUNT = 0;	// number of norms in the network

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------

	private Map<Integer, NormGroup> ids;					// norm groups' id's
	private Map<Norm, Map<Norm,NormGroupCombination>> nGroupCombinations;
	private Map<NormGroupCombination, NetworkNodeState> normGroupStates;
	
	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------

	/**
	 * @param nsm
	 */
	public NormGroupNetwork(NormSynthesisMachine nsm) {
		super(nsm);
		this.ids = new HashMap<Integer, NormGroup>();
		this.normGroupStates = new HashMap<NormGroupCombination, NetworkNodeState>();
		this.nGroupCombinations = new HashMap<Norm,
				Map<Norm,NormGroupCombination>>();
	}

	/**
	 * Adds a given {@code normGroup} to the network if it does not exist yet.
	 * Additionally, it sets the utility for the new norm group and sets its
	 * generalisation level
	 * 
	 * @param normGroup the norm group to add
	 */
	public void add(NormGroup normGroup) {
		if(!this.contains(normGroup)) {
			
			normGroup.setId(++NORM_GROUP_COUNT);
			super.add(normGroup);

			/* Index norm group for fast access */
			this.ids.put(normGroup.getId(), normGroup);
			
			/* Add norm group to the corresponding norm group combination */
			List<Norm> norms = normGroup.getAllNorms();
			Collections.sort(norms);
			
			Norm n1 = normGroup.getAllNorms().get(0); // TODO: Guarrada maxima!!!
			Norm n2 = normGroup.getAllNorms().get(1);
			
			/* The combination does not exist -> Add it*/
			if(!this.nGroupCombinations.containsKey(n1)) {
				this.nGroupCombinations.put(n1, 
						new HashMap<Norm,NormGroupCombination>());
			}
			if(!this.nGroupCombinations.containsKey(n2)) {
				this.nGroupCombinations.put(n2, 
						new HashMap<Norm,NormGroupCombination>());
			}
			
			if(!this.nGroupCombinations.get(n1).containsKey(n2)) {
				NormGroupCombination nGrComb = new NormGroupCombination(n1,n2);
				this.nGroupCombinations.get(n1).put(n2, nGrComb);
				this.nGroupCombinations.get(n2).put(n1, nGrComb);
			}
			
			NormCompliance nComplN1 = normGroup.getCompliance(n1);
			NormCompliance nComplN2 = normGroup.getCompliance(n2);
			NormGroupCombination nGrComb = this.nGroupCombinations.get(n1).get(n2);

			if(!nGrComb.contains(nComplN1, nComplN2)) {
				nGrComb.put(nComplN1, nComplN2, normGroup);
				
				this.normGroupStates.put(nGrComb, NetworkNodeState.ACTIVE);
			}
		}
	}
	
	/**
	 * Deactivates all the norm groups in a norm group combination
	 * 
	 * @param nGrComb the norm group combination
	 */
	public void setState(NormGroupCombination nGrComb, NetworkNodeState state) {
		this.normGroupStates.put(nGrComb, state);
		
		for(NormGroup nGroup : nGrComb.getAllNormGroups()) {
			this.setState(nGroup, state);
		}
	}
	
	/**
	 * Returns true if the norm group combination is active
	 * 
	 * @param nGrComb
	 * @return
	 */
	public boolean isActive(NormGroupCombination nGrComb) {
		return this.normGroupStates.get(nGrComb) == NetworkNodeState.ACTIVE;
	}

	/**
	 * Returns the {@code List} of all the norm groups in the network
	 * 
	 * @return the {@code List} of all the norm groups in the network
	 */
	public Collection<NormGroup> getNormGroups() {
		return super.getNodes();
	}
	
	/**
	 * Returns a {@code List} of the norm groups that are
	 * active in the network
	 * 
	 * @return a {@code List} of the norm groups that are
	 * 					active in the network
	 */
	public List<NormGroup> getActiveNormGroups() {
		List<NormGroup> ret = new ArrayList<NormGroup>();
		
		/* Add the node if it is active */
		for(NormGroup nGroup : this.getNormGroups()) {
			if(this.getState(nGroup) == NetworkNodeState.ACTIVE) {
				ret.add(nGroup);
			}
		}		
		return ret;
	}
	
	/**
	 * Returns a {@code List} of the norm groups that are
	 * inactive in the network
	 * 
	 * @return a {@code List} of the norm groups that are
	 * 					inactive in the network
	 */
	public List<NormGroup> getInactiveNormGroups() {
		return super.getInactiveNodes();
	}

	/**
	 * Returns a {@code List} of all the norm groups that whether are active
	 * in the network or are inactive but represented by an active norm group
	 * 
	 * @return a {@code List} of all the norm groups that whether are active
	 * 					in the network or are inactive but represented by an
	 * 					active norm group
	 */
	public List<NormGroup> getRepresentedNormGroups() {
		return this.getRepresentedNodes();
	}
	
	/**
	 * Returns a {@code List} of all the norm groups that are not represented
	 * in the network. That is, those norm groups that are inactive
	 * in the network and all its ancestors are inactive as well
	 * 
	 * @return a {@code List} of all the norm groups that are not represented
	 * 					in the network. That is, those norm groups that are inactive
	 *			 		in the network and all its ancestors are
	 *					inactive as well
	 */
	public List<NormGroup> getNotRepresentedNormGroups() {
		return super.getNotRepresentedNodes();
	}

	/**
	 * Returns the norm with the given {@code id}
	 * 
	 * @param id the id of the norm group
	 * @return the norm group with the given id
	 */
	public NormGroup getNormGroupWithId(int id)  {
		return this.ids.get(id);
	}

	/**
	 * 
	 * @param normGroupDescription
	 * @return
	 */
	public NormGroup getNormGroupWithDescription(String normGroupDescription) {
		for(NormGroup ng : this.getNodes()) {
			if(ng.getDescription().equals(normGroupDescription)) {
				return ng;
			}
		}
		return null;
	}
	
	/**
	 * Returns a map that contains the {@code NormGroupCombination} that the 
	 * {@code norm} has with several norms 
	 *  
	 * @param norm the norm of the norm group combinations
	 * @return a map that contains the {@code NormGroupCombination} that the 
	 * 					{@code norm} has with several norms 
	 */
	public Map<Norm, NormGroupCombination> getNormGroupCombinations(Norm norm) {
		return this.nGroupCombinations.get(norm);
	}
	
	/**
	 * Returns the {@code NormGroupCombination} that corresponds to norms 
	 * {@code n1} and {@code n2} received by parameter
	 * 
	 * @param n1 the first norm of the norm group combination
	 * @param n2 the second norm of the norm group combination
	 * @return the {@code NormGroupCombination} that corresponds to norms 
	 * 					{@code n1} and {@code n2} received by parameter
	 */
	public NormGroupCombination getNormGroupCombination(Norm n1, Norm n2) {
		return this.nGroupCombinations.get(n1).get(n2);
	}
	
	/**
	 * Returns <tt>true</tt> map that contains the {@code NormGroupCombination} 
	 * that the {@code norm} has with several norms 
	 *  
	 * @param norm the norm of the norm group combinations
	 * @return a map that contains the {@code NormGroupCombination} that the 
	 * 					{@code norm} has with several norms 
	 */
	public boolean hasNormGroupCombinations(Norm norm) {
		return this.nGroupCombinations.containsKey(norm);
	}
	
	/**
	 * Returns <tt>true</tt> if the normative network contains the norm
	 * 
	 * @param normGroup the norm group to search for
	 * @return <tt>true</tt> if the network contains the norm group
	 */
	public boolean contains(NormGroup normGroup)	{
		for(NormGroup ng : this.getNodes()) {
			if(ng.equals(normGroup)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns <tt>true</tt> if the normative network contains the norm
	 * 
	 * @param normGroup the norm group to search for
	 * @return <tt>true</tt> if the network contains the norm group
	 */
	public boolean contains(String normGroupDescription)	{
		for(NormGroup ng : this.getNodes()) {
			if(ng.getDescription().equals(normGroupDescription)) {
				return true;
			}
		}
		return false;
	}
}
