package es.csic.iiia.nsm.norm.evaluation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.csic.iiia.nsm.agent.language.SetOfPredicatesWithTerms;
import es.csic.iiia.nsm.norm.Norm;

/**
 * Class containing information about the norms that each agent has 
 * fulfilled/infringed, and the agent contexts in which those norms
 * are applicable. Furthermore, the class has information about the number
 * of conflicts that arose after agents fulfilled/infringed the norms
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
public class NormComplianceOutcomes {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------
	
	private List<Norm> allNorms;					// all fulfilled and infringed norms
	private List<Norm> fulfilledNorms;		// all the fulfilled norms
	private List<Norm> infringedNorms;		// all the infringed norms
	
	private Map<Norm, Integer> numFulfilmentsWithConflict;
	private Map<Norm, Integer> numFulfilmentsWithNoConflict;	
	private Map<Norm, Integer> numInfringementsWithConflict;		
	private Map<Norm, Integer> numInfringementsWithNoConflict;	
	
	private Map<Norm, List<SetOfPredicatesWithTerms>> 
		normsApplicableToAgentContexts; 	/* agent contexts in which each
																				fulfilled and infringed norm 
																				was applicable */ 
	
	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	public NormComplianceOutcomes() {
		this.allNorms = new ArrayList<Norm>();
		this.fulfilledNorms = new ArrayList<Norm>();
		this.infringedNorms = new ArrayList<Norm>();
		this.numFulfilmentsWithConflict = new HashMap<Norm, Integer>();
		this.numFulfilmentsWithNoConflict = new HashMap<Norm, Integer>();
		this.numInfringementsWithConflict = new HashMap<Norm, Integer>();
		this.numInfringementsWithNoConflict = new HashMap<Norm, Integer>();
		this.normsApplicableToAgentContexts = 
				new HashMap<Norm, List<SetOfPredicatesWithTerms>>();
	}

	/**
	 * Adds a new norm compliance that lead to conflicts
	 * 
	 * @param agContext the context in which an agent fulfilled the norm
	 * @param norm the norm that an agent fulfilled
	 */
	public void addFulfilmentWithConflict(SetOfPredicatesWithTerms agContext,
			Norm norm) {
		this.add(numFulfilmentsWithConflict, fulfilledNorms, agContext, norm);
	}
	
	/**
	 * Adds a new norm compliance that did not end up with conflicts
	 * 
	 * @param agContext the context in which an agent fulfilled the norm
	 * @param norm the norm that an agent fulfilled
	 */
	public void addFulfilmentWithNoConflict(SetOfPredicatesWithTerms agContext, 
			Norm norm) {
		this.add(numFulfilmentsWithNoConflict, fulfilledNorms, agContext, norm);
	}

	/**
	 * Adds a new norm infringement that lead to conflicts
	 * 
	 * @param agContext the context in which an agent infringed the norm
	 * @param norm the norm that an agent infringed
	 */
	public void addInfringementWithConflict(SetOfPredicatesWithTerms agContext, 
			Norm norm) {
		this.add(numInfringementsWithConflict, infringedNorms, agContext, norm);
	}

	/**
	 * Adds a new norm infringement that did not end up with conflicts
	 * 
	 * @param agContext the context in which an agent infringed the norm
	 * @param norm the norm that an agent infringed
	 */
	public void addInfringementWithNoConflict(SetOfPredicatesWithTerms agContext,
			Norm norm) {
		this.add(numInfringementsWithNoConflict, infringedNorms, agContext, norm);
	}

	/**
	 * Returns the {@code List} of all norms that agents fulfilled 
	 * or infringed
	 * 
	 * @return the {@code List} of all norms that agents fulfilled 
	 * 					or infringed
	 */
	public List<Norm> getAllNorms() {
		return this.allNorms;
	}
	
	/**
	 * Returns the {@code List} of norms that agents fulfilled
	 * 
	 * @return the {@code List} of norms that agents fulfilled
	 */
	public List<Norm> getFulfilledNorms() {
		return this.fulfilledNorms;
	}

	/**
	 * Returns the {@code List} of norms that agents infringed
	 * 
	 * @return the {@code List} of norms that agents infringed
	 */
	public List<Norm> getInfringedNorms() {
		return this.infringedNorms;
	}

	/**
	 * Returns the number of norm compliances that lead to conflicts
	 * 
	 * @return the number of norm compliances that lead to conflicts
	 */
	public int getNumFulfilmentsWithConflict(Norm norm) {
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
	public int getNumFulfilmentsWithNoConflict(Norm norm) {
		if(!this.numFulfilmentsWithNoConflict.containsKey(norm)) {
			return 0;
		}
		return this.numFulfilmentsWithNoConflict.get(norm);
	}

	/**
	 * Returns the number of norm infringements that lead to conflicts
	 * 
	 * @return the number of norm infringements that lead to conflicts
	 */
	public int getNumInfringementsWithConflict(Norm norm) {
		if(!this.numInfringementsWithConflict.containsKey(norm)) {
			return 0;
		}
		return this.numInfringementsWithConflict.get(norm);
	}

	/**
	 * Returns the number of norm infringements that did not end
	 * up with conflicts
	 * 
	 * @return the number of norm infringements that
	 * 					did not end up with conflicts
	 */
	public int getNumInfrsWithNoConflict(Norm norm) {
		if(!this.numInfringementsWithNoConflict.containsKey(norm)) {
			return 0;
		}
		return this.numInfringementsWithNoConflict.get(norm);
	}	

	/**
	 * Returns the number of norm compliances or infringements 
	 * that lead to conflicts
	 * 
	 * @return the number of norm compliances or infringements 
	 * 					that lead to conflicts
	 */
	public int getNumConflicts(Norm norm, NormCompliance action) {
		if(action == NormCompliance.FULFILMENT) {
			return this.getNumFulfilmentsWithConflict(norm);
		}
		else {
			return this.getNumInfringementsWithConflict(norm);
		}
	}

	/**
	 * Returns the number of norm compliances or infringements 
	 * that did not end up with conflicts
	 * 
	 * @return the number of norm compliances or infringements 
	 * 					that did not end up with conflicts
	 */
	public int getNumNoConflicts(Norm norm, NormCompliance action) {
		if(action == NormCompliance.FULFILMENT) {
			return this.getNumFulfilmentsWithNoConflict(norm);
		}
		else {
			return this.getNumInfrsWithNoConflict(norm);
		}
	}
	
	/**
	 * Returns all the agent contexts in which a norm was applicable
	 * (adding compliances and infringements)
	 * 
	 * @param norm the norm
	 * @return a list of the agent contexts in which the norm was applicable
	 */
	public List<SetOfPredicatesWithTerms> getAgentContextsWhereNormApplies(
			Norm norm) {
		return this.normsApplicableToAgentContexts.get(norm);
	}
	
	/**
	 * Returns a {@code List} of compliance actions for a given {@code norm}.
	 * That is, if the norm has been fulfilled (namely the norm exists in the 
	 * list of all fulfilled norms), it returns a list with the compliance
	 * action "Compliance". If the norm has also been infringed, the
	 * returned list will also contain "Infringement"
	 * 
	 * @param norm the norm 
	 * @return a {@code List} of compliance actions for a given {@code norm}
	 * @see NormCompliance
	 */
	public List<NormCompliance> getCompliance(Norm norm) {
		List<NormCompliance> complAcs = new ArrayList<NormCompliance>();
		
		if(this.fulfilledNorms.contains(norm)) {
			complAcs.add(NormCompliance.FULFILMENT);
		}
		if(this.infringedNorms.contains(norm)) {
			complAcs.add(NormCompliance.INFRINGEMENT);
		}
		return complAcs;
	}
	
	/**
	 * Clears the goal norm compliance
	 */
	public void clear() {
		this.fulfilledNorms.clear();
		this.infringedNorms.clear();

		for(Norm norm : this.numFulfilmentsWithConflict.keySet()) {
			this.numFulfilmentsWithConflict.put(norm, 0);
		}
		for(Norm norm : this.numFulfilmentsWithNoConflict.keySet()) {
			this.numFulfilmentsWithNoConflict.put(norm, 0);
		}
		for(Norm norm : this.numInfringementsWithConflict.keySet()) {
			this.numInfringementsWithConflict.put(norm, 0);
		}
		for(Norm norm : this.numInfringementsWithNoConflict.keySet()) {
			this.numInfringementsWithNoConflict.put(norm, 0);
		}		
		for(List<SetOfPredicatesWithTerms> list :
			this.normsApplicableToAgentContexts.values()) {
			list.clear();
		}
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
	private void add(Map<Norm, Integer> map, List<Norm> list,
			SetOfPredicatesWithTerms agContext, Norm norm) {
		
		if(!map.containsKey(norm)) {
			map.put(norm, 0);
		}
		map.put(norm, map.get(norm)+1);
		
		if(!list.contains(norm)) {
			list.add(norm);
		}		
		if(!this.normsApplicableToAgentContexts.containsKey(norm)) {
			this.normsApplicableToAgentContexts.put(norm,
					new ArrayList<SetOfPredicatesWithTerms>());
		}
		
		List<SetOfPredicatesWithTerms> naac =	
				this.normsApplicableToAgentContexts.get(norm);
		
		if(!naac.contains(agContext)) {
			naac.add(agContext);
		}
		
		/* Add the norm to the list of all norms, if it does not exist yet */
		if(!this.allNorms.contains(norm)) {
			this.allNorms.add(norm);
		}
	}
}
