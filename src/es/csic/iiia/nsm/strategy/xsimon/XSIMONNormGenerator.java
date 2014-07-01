package es.csic.iiia.nsm.strategy.xsimon;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import es.csic.iiia.nsm.config.DomainFunctions;
import es.csic.iiia.nsm.config.Goal;
import es.csic.iiia.nsm.config.NormSynthesisSettings;
import es.csic.iiia.nsm.norm.generation.Conflict;
import es.csic.iiia.nsm.perception.Monitor;
import es.csic.iiia.nsm.perception.ViewTransition;

/**
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 *
 */
public class XSIMONNormGenerator {

	protected XSIMONOperators operators;
	protected NormSynthesisSettings nsmSettings;
	protected Monitor monitor;
	protected DomainFunctions dmFunctions;

	/**
	 * 
	 */
	public XSIMONNormGenerator(NormSynthesisSettings nsmSettings,
			Monitor monitor, DomainFunctions dmFunctions, XSIMONOperators operators) {

		this.nsmSettings = nsmSettings;
		this.monitor = monitor;
		this.dmFunctions = dmFunctions;
		this.operators = operators;
	}

	/**
	 * 
	 * @return
	 */
	public void step(List<ViewTransition> viewTransitions,
			Map<Goal,List<Conflict>> conflicts) {

		/* Obtain monitor perceptions */
		obtainPerceptions(viewTransitions);

		/* Conflict detection */
		conflictDetection(viewTransitions, conflicts);

		/* Norm generation */
		for(Goal goal : conflicts.keySet()) {
			for(Conflict conflict : conflicts.get(goal)) {
				operators.create(conflict, goal);
			}	
		}
	}


	/**
	 * Calls scenario monitors to perceive agents interactions
	 * 
	 * @return a {@code List} of the monitor perceptions, where each perception
	 *  				is a view transition from t-1 to t
	 */
	protected void obtainPerceptions(List<ViewTransition> viewTransitions) {
		this.monitor.getPerceptions(viewTransitions);
	}

	/**
	 * Given a list of view transitions (from t-1 to t), this method
	 * returns a list of conflicts with respect to each goal of the system
	 * 
	 * @param viewTransitions the list of perceptions of each sensor
	 */
	protected Map<Goal, List<Conflict>> conflictDetection(
			List<ViewTransition> viewTransitions, 
			Map<Goal,List<Conflict>> conflicts) {

		conflicts.clear();

		/* Conflict detection is computed in terms of a goal */
		for(Goal goal : this.nsmSettings.getSystemGoals())		{
			List<Conflict> goalConflicts = new ArrayList<Conflict>();

			for(ViewTransition vTrans : viewTransitions) {
				goalConflicts.addAll(dmFunctions.getNonRegulatedConflicts(goal, vTrans));
			}  	
			conflicts.put(goal, goalConflicts);
		}
		return conflicts;
	}
}
