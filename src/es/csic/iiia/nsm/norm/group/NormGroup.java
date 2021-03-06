package es.csic.iiia.nsm.norm.group;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.csic.iiia.nsm.config.Goal;
import es.csic.iiia.nsm.net.norm.NetworkNode;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.evaluation.NormCompliance;

/**
 * A group of norms that are applicable together, and hence can be evaluated
 * together as a group. Thus, we can evaluate for instance how two norms 
 * perform together to avoid conflicts whenever one is fulfilled and the other
 * is infringed
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
public class NormGroup
implements Comparable<NormGroup>, NetworkNode {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------

	private int id;										// the id of the norm group
	private String name;								// name describing the norm group
	private Goal goal;									// the goal of all the norms in the group
	private List<Norm> allNorms;				// list of all norms in the norm group
	private List<Norm> fulfilledNorms;	// list of fulfilled norms
	private List<Norm> infringedNorms;	// list of infringed norms
	private Map<Norm, NormCompliance> normCompliance;

	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------

	/**
	 * Constructs a new norm group, specifying the size of its performance range
	 * 
	 * @param perfRangeSize the size of the norm group's performance range
	 */
	public NormGroup() {
		this.fulfilledNorms = new ArrayList<Norm>();
		this.infringedNorms = new ArrayList<Norm>();
		this.allNorms = new ArrayList<Norm>();
		this.normCompliance = new HashMap<Norm, NormCompliance>();

		this.name = NormGroup.getDescription(fulfilledNorms, infringedNorms);
	}

	/**
	 * Adds a norm to the corresponding list (fulfilled or infringed norm) based
	 * on the {@code nComplianceAction} that an agent has performed. If
	 * variable {@code nComplianceAction} equals to <i>Compliance</i>, the 
	 * norm will be added to the list of fulfilled norms. Otherwise, it will be
	 * added to the list of infringed norms
	 * 
	 * @param norm the norm to add
	 * @param nComplianceAction the norm compliance action
	 * @see NormCompliance
	 */
	public void addNorm(Norm norm, NormCompliance nComplianceAction) {
		if(nComplianceAction == NormCompliance.FULFILMENT) {
			this.addFulfilledNorm(norm);
		}
		else {
			this.addInfringedNorm(norm);
		}
	}

	/**
	 * Adds a fulfilled {@code Norm} to the norm group
	 * 
	 * @param norm the fulfilled norm
	 */
	public void addFulfilledNorm(Norm norm) {
		this.fulfilledNorms.add(norm);
		this.allNorms.add(norm);
		this.normCompliance.put(norm, NormCompliance.FULFILMENT);

		this.name = NormGroup.getDescription(fulfilledNorms, infringedNorms);
	}

	/**
	 * Adds an infringed {@code Norm} to the norm group
	 * 
	 * @param norm the infringed norm
	 */
	public void addInfringedNorm(Norm norm) {
		this.infringedNorms.add(norm);
		this.allNorms.add(norm);
		this.normCompliance.put(norm, NormCompliance.INFRINGEMENT);

		this.name = NormGroup.getDescription(fulfilledNorms, infringedNorms);
	}

	/**
	 * Returns a {@code List} of all the norms in the norm group 
	 * that have been fulfilled
	 * 
	 * @return a {@code List} of all the norms in the norm group 
	 * 					that have been fulfilled
	 */
	public List<Norm> getFulfilledNorms() {
		return this.allNorms;
	}

	/**
	 * Returns a {@code List} of all the norms in the norm group 
	 * that have been infringed
	 * 
	 * @return a {@code List} of all the norms in the norm group 
	 * 					that have been infringed
	 */
	public List<Norm> getInfringedNorms() {
		return this.allNorms;
	}

	/**
	 * Returns a {@code List} of all the norms in the norm group, 
	 * whether they have been fulfilled or infringed
	 * 
	 * @return a {@code List} of all the norms in the norm group, 
	 * 					whether they have been fulfilled or infringed
	 */
	public List<Norm> getAllNorms() {
		return this.allNorms;
	}

	/**
	 * Returns <i>Fulfilment</i> if the norm is fulfilled in this norm group.
	 * Otherwise, it returns <i>Infringement</i>
	 *  
	 * @param norm the norm to check compliance about
	 * @return	<i>Fulfilment</i> if the norm is fulfilled in this norm group.
	 * 					Otherwise, it returns <i>Infringement</i>
	 */
	public NormCompliance getCompliance(Norm norm) {
		return this.normCompliance.get(norm);
	}

	/**
	 * Returns the goal in which terms the norm group is evaluated
	 * 
	 * @return the goal in which terms the norm group is evaluated
	 */
	public Goal getGoal() {
		return this.goal;
	}

	/**
	 * Returns the name of the norm group
	 * 
	 * @return the name of the norm group
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Returns the id of the norm group
	 * 
	 * @return the id of the norm group
	 */
	public int getId() {
		return this.id;
	}

	/**
	 * Sets the id of the norm group
	 * 
	 * @param id the id of the norm group
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Returns a description of the norm group
	 * 
	 * @return a string describing the norm group
	 */
	public String getDescription() {
		return this.toString();
	}

	/**
	 * Returns <tt>true</tt> if this norm group contains the same
	 * norms and compliances/infringements than the {@code otherNormGroup}
	 * 
	 * @param otherNormGroup the other norm group
	 * @return <tt>true</tt> if this norm group contains the same
	 * 					norms and compliances/infringements than the
	 * 					{@code otherNormGroup} 
	 */
	public boolean equals(NormGroup otherNormGroup) {
		return this.getName().equals(otherNormGroup.getName());
	}

	/**
	 * Returns <tt>true</tt> if this norm group contains the given {@code norm}
	 * 
	 * @param norm the norm to search
	 * @return <tt>true</tt> if this norm group contains the given {@code norm}
	 */
	public boolean contains(Norm norm) {
		for(Norm n : this.allNorms) {
			if(n.equals(norm))
				return true;
		}
		return false;
	}

	/**
	 * Returns the value <code>0</code> if the argument group is equal to
	 * this group; a value less than <code>0</code> if this group
	 * is lower than the group argument; and a value greater than
	 * <code>0</code> if this group is greater than the group argument 
	 * 
	 * @param cmpNorm the norm to compare
	 * @return the value <code>0</code> if the argument group is equal to
	 *          this group; a value less than <code>0</code> if this group
	 *          is lower than the group argument; and a
	 *          value greater than <code>0</code> if this group is
	 *          greater than the group argument
	 */
	@Override
	public int compareTo(NormGroup otherNormGroup) {
		List<Norm> theseNorms = this.getAllNorms();
		List<Norm> otherNorms = otherNormGroup.getAllNorms();
		Collections.sort(theseNorms);
		Collections.sort(otherNorms);

		/* Different size */
		if(theseNorms.size() > otherNorms.size()) {
			return 1;
		}
		else if(theseNorms.size() < otherNorms.size()) {
			return -1;
		}

		/* Order first by norm identifiers */
		for(int i=0; i<theseNorms.size(); i++) {
			Norm nA = theseNorms.get(i);
			Norm nB = otherNorms.get(i);

			if(nA.getId() > nB.getId()) {
				return 1;
			}
			else if(nA.getId() < nB.getId()) {
				return -1;
			}
		}

		/* Same norms... order by compliance */
		for(int i=0; i<theseNorms.size(); i++) {
			Norm nA = theseNorms.get(i);
			Norm nB = otherNorms.get(i);

			/* Same ids -> Sort by action */
			NormCompliance nACompl = this.getCompliance(nA);
			NormCompliance nBCompl = otherNormGroup.getCompliance(nB);

			int comp = nACompl.compareTo(nBCompl);
			if(comp != 0) {
				return comp;
			}
		}

		return 0;
	}

	/**
	 * Returns a string describing the norm group
	 * 
	 * @return a string describing the norm group
	 */
	public String toString() {
		return this.getName();
	}

	/**
	 * Returns the number of different norms into the norm group
	 * 
	 * @return the number of different norms into the norm group
	 */
	public int size() {
		return this.allNorms.size();
	}

	/**
	 * 
	 * @return
	 */
	public String toStringDetailed() {
		String s = "Norm Group " + this.getName() + "\n\n";

		s += "------------------\n";
		s += "Norms of the group\n";
		s += "------------------\n\n";

		// Add norm information
		for(Norm norm : this.allNorms) {
			s += norm.toString() + ") \n";
		}
		return s;
	}

	//---------------------------------------------------------------------------
	// Static methods
	//---------------------------------------------------------------------------

	/**
	 * Returns a string that describes a norm group composed by a
	 * {@code List} of fulfilled norms, and a {@code List} of infringed norms
	 * 
	 * @param fulfilledNorms the {@code List} of fulfilled norms
	 * @param infringedNorms the {@code List} of infringed norms
	 * @return a string that describes a norm group composed
	 * 					by a {@code List} of fulfilled norms, and a {@code List}
	 * 					of infringed norms 
	 */
	public static String getDescription(List<Norm> fulfilledNorms, 
			List<Norm> infringedNorms) {

		String name = "";
		int numNorms = fulfilledNorms.size() + infringedNorms.size();
		int i=0;
		Map<Norm, Norm> norms = new HashMap<Norm, Norm>();

		for(Norm norm : fulfilledNorms) {
			norms.put(norm,norm);	
		}
		for(Norm norm : infringedNorms) {
			norms.put(norm,norm);	
		}

		List<Norm> allNorms = new ArrayList<Norm>(norms.keySet());
		Collections.sort(allNorms);

		// Build name
		for(Norm norm : allNorms) {
			i++;
			name += norm.getName() + (fulfilledNorms.contains(norm) ? "F" : "I");
			name += ((i < numNorms) ? "-" : "");  
		}
		return name;
	}

	/**
	 * 
	 * @param n1
	 * @param n1cplAct
	 * @param n2
	 * @param n1cplAct
	 * @return
	 */
	public static String getDescription(Norm n1, NormCompliance n1cplAct,
			Norm n2, NormCompliance n2cplAct) {

		int i=0;
		int numNorms = 2;
		List<Norm> norms = new ArrayList<Norm>();
		norms.add(n1);
		norms.add(n2);

		Collections.sort(norms);

		String name = "";
		for(Norm norm : norms) {
			i++;

			if(norm.equals(n1)) {
				name += n1.getName() +
						(n1cplAct == NormCompliance.FULFILMENT ? "F" : "I");
			}
			else {
				name += n2.getName() +
						(n2cplAct == NormCompliance.FULFILMENT ? "F" : "I");
			}
			name += ((i < numNorms) ? "-" : "");  
		}
		return name;
	}
}
