package es.csic.iiia.nsm.norm.generation.cbr;

import java.util.List;

import es.csic.iiia.nsm.norm.generation.Conflict;
import es.csic.iiia.nsm.perception.View;
import es.csic.iiia.nsm.perception.ViewTransition;

/**
 * A case description, containing the conflict source (which is a transition
 * of views for consecutive time steps) and the conflict itself (at time t=0)
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 * @see ViewTransition
 */
public class CaseDescription {
	
	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------
	
	private View conflict;										// the view containing the conflict
	private ViewTransition conflictSource;		// the source of the conflict
	private List<Long> conflictingAgents;			// the agents that are in conflict
	
	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------
	
	/**
	 * Constructor with conflict
	 * 
	 * @param 	conflict the conflict of the case description, which contains
	 * 					the conflict source (i.e., the transition of views previous to
	 * 					the conflict), the conflict (i.e., the view in which the conflict
	 * 					has been detected), and the agents that are in conflict 
	 * @see Conflict
	 */
	public CaseDescription(Conflict conflict) {
		this.conflict = conflict.getConflict();
		this.conflictSource = conflict.getConflictSource();
		this.conflictingAgents = conflict.getConflictingAgents();
	}
	
	/**
	 * Returns the similarity between this case description 
	 * and the {@code otherDesc}
	 * 
	 * @param otherDesc the other case description
	 * @return the similarity with the other case description
	 * @see CaseDescription
	 */
	public float getSimilarity(CaseDescription otherDesc) {
		float similarity = 0f;
		int numTimeSteps = conflictSource.getNumTimeSteps();
		
		for(int timeStep=0; timeStep<numTimeSteps; timeStep++) {
			View view = conflictSource.getView(timeStep);
			View otherView = otherDesc.getConflictSource().getView(timeStep);
			similarity += view.getSimilarity(otherView);
		}
		similarity += conflict.getSimilarity(otherDesc.getConflict());
		
		return similarity;
	}

	/**
	 * Returns the conflicting view of the case description
	 * 
	 * @return the view containing the conflict
	 */
	public View getConflict() 	{
		return this.conflict;
	}
	
	/**
	 * Returns the source of the conflict
	 * 
	 * @return the source of the conflict
	 * @see ViewTransition
	 */
	public ViewTransition getConflictSource() {
		return this.conflictSource;
	}

	/**
	 * Returns the list of conflicting agents 
	 * 
	 * @return the list of conflicting agents
	 */
	public List<Long> getConflictingAgents() {
	  return this.conflictingAgents;
	}
}
