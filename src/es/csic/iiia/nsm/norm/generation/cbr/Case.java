package es.csic.iiia.nsm.norm.generation.cbr;

import java.util.ArrayList;

/**
 * A case of a Case Base. Each case contains: 
 * <ol>
 * <li> a {@code CaseDescription} that describes a conflicting situation 
 * 			that must be resolved; and
 * <li> a {@code List} of possible solutions (objects {@code CaseSolution})
 * 			that are aimed to solve the case.
 * </ol>
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 * @see CaseDescription
 * @see CaseSolution
 */
public class Case {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------
	
	private int id = 0;
	private CaseDescription description = null;
	private ArrayList<CaseSolution> solutions = null;

	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------

	/**
	 * Constructor
	 * 
	 * @param description the description of the case
	 */
	public Case(CaseDescription description) {
		this.description = description;
		this.solutions = new ArrayList<CaseSolution>();
	}

	/**
	 * Returns the solution ({@code CaseSolution}) with the best score
	 * 
	 * @return the solution ({@code CaseSolution}) with the best score
	 * @see CaseSolution
	 */
	public CaseSolution getBestSolution(){
		if(solutions.size() == 0)
			return null;

		CaseSolution best = solutions.get(0);
		for(CaseSolution cur : solutions){
			if(cur.getScore() > best.getScore()){
				best = cur;
			}
		}
		return best;
	}

	/**
	 * Returns the description of the case 
	 * 
	 * @return the description of the case
	 * @see CaseDescription
	 */
	public CaseDescription getDescription() {
		return description;
	}

	/**
	 * Returns the {@code List} of solutions of the case 
	 * 
	 * @return the {@code List} of solutions of the case
	 * @see CaseSolution
	 */
	public ArrayList<CaseSolution> getSolutions(){
		return solutions;
	}

	/**
	 * Returns the identifier of the case
	 * 
	 * @return the identifier of the case
	 */
	public int getId() {
		return id;
	}

	/**
	 * Returns a {@code String} that describes the case
	 * 
	 * @return a {@code String} that describes the case
	 */
	public String toString(){
		return "Case " + id;
	}

	/**
	 * Returns a {@code String} with the name of the case
	 * 
	 * @return a {@code String} with the name of the case
	 */
	public String getName() {
		return "C" + this.id;
	}
}
