package es.csic.iiia.nsm.norm.generation.cbr;

import java.util.ArrayList;

/**
 * A case base is a base of CBR cases. Each case in the case base represents
 * a conflicting situation that contains some solutions aimed to solve the
 * case
 *
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 * @see Case
 */
public class CaseBase {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------
	
	private ArrayList<Case> cases;	// the list of cases

	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	public CaseBase() {
		cases = new ArrayList<Case>();
	}

	/**
	 * Returns the {@code Case} that is most similar to the case
	 * with description {@code desc}
	 * 
	 * @param desc the other case description
	 * @return the {@code Case} that is most similar to the case
	 * 					with description {@code desc}
	 */
	public Case searchForSimilarCase(CaseDescription desc) {
		for(Case cse : cases){
			float diff = cse.getDescription().getSimilarity(desc);
			if(diff == 0f) {
				return cse;
			}
		}
		return null;
	}

	/**
	 * Returns the description of the case base
	 * 
	 * @return a {@code String} describing the case base
	 */
	@Override
	public String toString(){
		return "CaseBase status:\n\tCases: " + cases.size();
	}
}
