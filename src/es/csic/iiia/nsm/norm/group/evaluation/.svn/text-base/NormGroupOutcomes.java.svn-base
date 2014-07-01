package es.csic.iiia.nsm.norm.group.evaluation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.csic.iiia.nsm.norm.group.NormGroup;

/**
 * Class containing information about the norm groups that each agent has 
 * complied/infringed, and the agent contexts in which those norms
 * are applicable. Furthermore, the class has information about the number
 * of conflicts that arose after agents complied/infringed the norms
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
public class NormGroupOutcomes {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------

	private List<NormGroup> allNormGroups;		// all norm groups
	private Map<NormGroup, Integer> numFulfilmentsWithConflict;		
	private Map<NormGroup, Integer> numFulfilmentsWithNoConflict;	


	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public NormGroupOutcomes() {
		this.allNormGroups = new ArrayList<NormGroup>();
		this.numFulfilmentsWithConflict = new HashMap<NormGroup, Integer>();
		this.numFulfilmentsWithNoConflict = new HashMap<NormGroup, Integer>();
	}

	/**
	 * Adds a new norm compliance that lead to conflicts
	 * 
	 * @param agContext the context in which an agent complied with the norm
	 * @param norm the norm that an agent complied with
	 */
	public void addComplsWithConflict(NormGroup normGroup, int num) {
		this.add(numFulfilmentsWithConflict, normGroup, num);
	}

	/**
	 * Adds a new norm compliance that did not end up with conflicts
	 * 
	 * @param agContext the context in which an agent complied with the norm
	 * @param norm the norm that an agent complied with
	 */
	public void addComplsWithNoConflict(NormGroup normGroup, int num) {
		this.add(numFulfilmentsWithNoConflict, normGroup, num);
	}

	/**
	 * Returns the {@code List} of norms that agents complied with
	 * 
	 * @return the {@code List} of norms that agents complied with
	 */
	public List<NormGroup> getNormGroups() {
		return this.allNormGroups;
	}

	/**
	 * Returns the number of norm compliances that lead to conflicts
	 * 
	 * @return the number of norm compliances that lead to conflicts
	 */
	public int getNumComplsWithConflict(NormGroup norm) {
		if(!this.numFulfilmentsWithConflict.containsKey(norm)) {
			return 0;
		}
		return this.numFulfilmentsWithConflict.get(norm);
	}

	/**
	 * Returns the number of norm compliances that did not end up with conflicts
	 * 
	 * @return the number of norm compliances that did not end up with conflicts
	 */
	public int getNumComplsWithNoConflict(NormGroup norm) {
		if(!this.numFulfilmentsWithNoConflict.containsKey(norm)) {
			return 0;
		}
		return this.numFulfilmentsWithNoConflict.get(norm);
	}

	/**
	 * Clears the goal norm compliance
	 */
	public void clear() {
		this.allNormGroups.clear();
		this.numFulfilmentsWithConflict.clear();
		this.numFulfilmentsWithNoConflict.clear();
	}

	//---------------------------------------------------------------------------
	// Private methods
	//---------------------------------------------------------------------------

	/**
	 * Adds a norm that has been applicable to an agent context, to a given 
	 * list of norm compliances/infringements and to a map that sums up the 
	 * number of conflicts that arose after the norm compliance/infringement
	 * 
	 * @param map the map
	 * @param list the list
	 * @param agContext the agent context
	 * @param norm the norm
	 */
	private void add(Map<NormGroup, Integer> map, NormGroup norm, int inc) {
		if(!map.containsKey(norm)) {
			map.put(norm, 0);
		}
		map.put(norm, map.get(norm) + inc);

		if(!allNormGroups.contains(norm)) {
			allNormGroups.add(norm);
		}
	}
}
