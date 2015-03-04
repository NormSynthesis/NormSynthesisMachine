package es.csic.iiia.nsm.norm;

import java.util.ArrayList;
import java.util.List;

/**
 * A normative system is a set of norms, implemented by means
 * of an {@code ArrayList} of norms
 * 
 * @author Javier Morales (jmorales@iiia.csic.es)
 */
public class NormativeSystem extends ArrayList<Norm>{

	//---------------------------------------------------------------------------
  // Static attributes 
  //---------------------------------------------------------------------------
	
	private static final long serialVersionUID = 8245680654641852211L; // Id
  
	//---------------------------------------------------------------------------
  // Attributes 
  //---------------------------------------------------------------------------
	
	private int id;
	
  //---------------------------------------------------------------------------
  // Methods 
  //---------------------------------------------------------------------------
	
	/**
	 * 
	 */
	public NormativeSystem() {}
	
	/**
	 * 
	 * @param id
	 */
	public NormativeSystem(int id) {
		this.id = id;
	}
	
	/**
	 * Adds a norm to the set
	 * 
	 * @param norm the norm to add
	 */
	@Override
	public boolean add(Norm norm) {
		if(!this.contains(norm)) {
			return super.add(norm);
		}
		return false;
	}
	
	/**
	 * Adds a {@code List} of norms to the set
	 * 
	 * @param norms the list of norms
	 */
	public void addAll(List<Norm> norms) {
		for(Norm norm : norms) {
			if(!this.contains(norm)) {
				this.add(norm);
			}
		}
	}

	/**
	 * Returns <tt>true</tt> if the norm set contains the given norm.
	 * It performs the search by comparing norm id's.
	 * 
	 * @param norm the norm to search
	 * @return <tt>true</tt> if the norm set contains the given norm
	 */
	public boolean contains(Norm norm) {
		for(Norm n : this) {
			if(n.equals(norm))
				return true;
		}
		return false;
	}

	/**
	 * Returns the norm with the given {@code id}, if the normative system
	 * contains the norm. In case the normative system does not contain the 
	 * norm, it returns {@code null} 
	 * 
	 * @param id the id of the norm
	 * @return the norm with the given {@code id}, if the normative system
	 * 					contains the norm. In case the normative system does not
	 * 					contain the norm, it returns {@code null} 
	 */
	public Norm getNormWithId(int id) {
		Norm norm = null;
		for(Norm n : this) {
			if(n.getId() == id) {
				norm = n;
				break;
			}
		}
		return norm;
	}
	
	/**
	 * 
	 * @param otherNS
	 * @return
	 */
	public boolean isSubsetOf(NormativeSystem otherNS) {
		
		/* Do all the norms in this NS exist in the other NS? */
		for(Norm norm : this) {
			if(!otherNS.contains(norm)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Returns the id of the normative system
	 * 
	 * @return the id of the normative system
	 * @return
	 */
	public int getId() {
		return this.id;
	}
}
