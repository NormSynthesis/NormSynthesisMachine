package es.csic.iiia.nsm.norm.group;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.evaluation.NormCompliance;

/**
 * A class that contains the four combinations of compliance of a
 * pair of norms. That is, <i>Fulfilment/Fulfilment</i>,
 * <i>Fulfilment/Infringement</i>, <i>Infringement/Fulfilment</i> and
 * <i>Infringement/Infringement</i>
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
public class NormGroupCombination 
extends HashMap<NormCompliance, HashMap<NormCompliance, NormGroup>> {

	//---------------------------------------------------------------------------
	// Static attributes
	//---------------------------------------------------------------------------
	
	private static final long serialVersionUID = 2409088169144412031L;
	
	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------
	
  private List<Norm> norms;
  private List<NormGroup> allNormGroups;
	private int numNormGroups;
	
  //---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------
		
	/**
	 * Constructor with norms
	 * 
	 * @param n1 the first norm
	 * @param n2 the second norm
	 */
	public NormGroupCombination(Norm n1, Norm n2) {
		super();
		
		this.numNormGroups = 0;
		this.allNormGroups = new ArrayList<NormGroup>();
		this.norms = new ArrayList<Norm>();
		this.norms.add(n1);
		this.norms.add(n2);
		
		Collections.sort(norms);
		
		this.put(NormCompliance.FULFILMENT, 
				new HashMap<NormCompliance, NormGroup>());
		this.put(NormCompliance.INFRINGEMENT, 
				new HashMap<NormCompliance, NormGroup>());
	}
	
	/**
	 * Adds a new norm group to the norm group combination
	 * 
	 * @param nComplN1 the first norm compliance
	 * @param nComplN2 the second norm compliance
	 * @param nGroup the norm group
	 */
	public void put(NormCompliance nComplN1, NormCompliance nComplN2,
			NormGroup nGroup) {
		
		this.get(nComplN1).put(nComplN2, nGroup);
		if(!this.allNormGroups.contains(nGroup)) {
			this.allNormGroups.add(nGroup);
		}
		this.numNormGroups++;
	}
	
	/**
	 * Returns the norm group that corresponds to the given combination
	 * of norms compliance
	 * 
	 * @param nComplN1 the first norm compliance
	 * @param nComplN2 the second norm compliance
	 * @return the norm group that corresponds to the given combination
	 * 					of norms compliance
	 */
	public NormGroup get(NormCompliance nComplN1, NormCompliance nComplN2) {
		return this.get(nComplN1).get(nComplN2);
	}
	
	/**
	 * Returns the norm groups that correspond to the four combinations
	 * of norm compliance
	 * 
	 * @return the norm groups that correspond to the four combinations
	 * 					of norm compliance
	 */
	public List<NormGroup> getAllNormGroups() {
		return this.allNormGroups;
	}
	
  /**
   * Removes the norm group that corresponds to the given
   * norm compliance combination
	 * 
	 * @param nComplN1 the first norm compliance
	 * @param nComplN2 the second norm compliance
	 */
  public void remove(NormCompliance nComplN1, NormCompliance nComplN2) {
  	this.get(nComplN1).remove(nComplN2);
  }
  
  /**
   * Returns <tt>true</tt> if the norm group combination contains the norm 
   * group that corresponds to the given norm compliance
   *  
   * @param nComplN1 the first norm compliance
   * @param nComplN2 the second norm compliance
   * @return
   */
  public boolean contains(NormCompliance nComplN1, NormCompliance nComplN2) {
  	if(!this.containsKey(nComplN1)) {
  		return false;
  	}
  	return this.get(nComplN1).containsKey(nComplN2);
  }
  
  /**
   * Returns <tt>true</tt> if this combination contains the four groups that 
   * correspond to the four possible combinations of norm compliance
   * 
   * @return <tt>true</tt> if this combination contains the four groups that 
   * 					correspond to the four possible combinations of norm compliance
   */
  public boolean containsAllCombinations() {
  	return this.numNormGroups == 4;
  }
}
