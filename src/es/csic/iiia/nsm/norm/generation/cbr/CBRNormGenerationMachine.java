package es.csic.iiia.nsm.norm.generation.cbr;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import es.csic.iiia.nsm.agent.EnvironmentAgentAction;
import es.csic.iiia.nsm.agent.EnvironmentAgentContext;
import es.csic.iiia.nsm.agent.language.SetOfPredicatesWithTerms;
import es.csic.iiia.nsm.config.DomainFunctions;
import es.csic.iiia.nsm.config.Goal;
import es.csic.iiia.nsm.metrics.NormSynthesisMetrics;
import es.csic.iiia.nsm.net.norm.NormativeNetwork;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.NormModality;
import es.csic.iiia.nsm.norm.evaluation.NormsApplicableToAgentContext;
import es.csic.iiia.nsm.norm.generation.Conflict;
import es.csic.iiia.nsm.norm.generation.NormGenerationMachine;
import es.csic.iiia.nsm.norm.reasoning.NormReasoner;
import es.csic.iiia.nsm.perception.View;
import es.csic.iiia.nsm.perception.ViewTransition;
import es.csic.iiia.nsm.strategy.NormSynthesisStrategy;

/**
 * A norm generation machine that uses Case Based Reasoning (CBR)
 * to generates norms from conflicts.
 * Each generated norm is aimed to regulate the given conflict,
 * trying to prevent it from happening again in the future
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 * @see Goal
 * @see Conflict
 */
public class CBRNormGenerationMachine implements NormGenerationMachine {

	//---------------------------------------------------------------------------
	// Attributes 
	//---------------------------------------------------------------------------

	private Random random;
	private CaseBase caseBase;									// the case base
	private NormativeNetwork normativeNetwork;	// the normative network
	private NormReasoner normReasoner;
	private NormSynthesisStrategy strategy;
	private NormSynthesisMetrics nsMetrics;
	
	//---------------------------------------------------------------------------
	// Methods 
	//---------------------------------------------------------------------------

	/**
	 * Constructor
	 * 
	 * @param nsm the norm synthesis machine
	 * @param normReasoner 
	 */
	public CBRNormGenerationMachine(NormativeNetwork normativeNetwork, 
			NormReasoner normReasoner, NormSynthesisStrategy strategy,
			Random random, NormSynthesisMetrics nsMetrics) {
		
		this.random = random;
		this.caseBase = new CaseBase();
		this.normativeNetwork = normativeNetwork;
		this.normReasoner = normReasoner;
		this.strategy = strategy;
		this.nsMetrics = nsMetrics;
	}

	/**
	 * Generates norms from a given {@code conflict} using
	 * Case Based Reasoning (CBR). The method works as follows:
	 * <ol>
	 * <li> it creates a new {@code CaseDescription} for the given
	 * 			{@code conflict};
	 * <li> it searches into the {@code CaseBase} for a {@code Case} that
	 * 			has a similar {@code CaseDescription};
	 * <li> then, it may happen two things: that a similar case is found,
	 * 			or it is not:
	 * 			<br> if it finds a similar {@code Case}, then it adapts its best
	 * 			solution to solve the new {@code Conflict}.
	 * 			<br> Otherwise, if no similar case has been found, it generates 
	 * 			a random solution to solve the new {@code CaseDescription}, 
	 * 			namely the new conflict. Thus, the conflict should be avoided
	 * 			in the future.
	 *
	 * @return a {@code List} of norms that are aimed to solve the
	 * 					given {@code conflict}
	 */
	@Override
	public List<Norm> generateNorms(Conflict conflict, 
			DomainFunctions dmFunctions, Goal goal) {

		CaseDescription cDesc = new CaseDescription(conflict);
		Case cse = caseBase.searchForSimilarCase(cDesc);
		CaseSolution sol;

		/* No similar case has been found -> Solve case by generating
		 * a random solution*/
		if(cse != null) {
			sol = this.adaptBestSolution(cse, conflict);
		}
		/* A similar case has been found -> Adapt the best solution
		 * of the found similar case*/
		else {
			sol = this.solveCase(cDesc, dmFunctions, goal);
		}
		return sol.getNorms();
	}

	/**
	 * Creates a new random {@code CaseSolution} to solve the conflict
	 * described in the {@code CaseDescription}. The reasoning employed
	 * to generate the random solution is based on the principle:
	 * if we prohibit some of the actions that the conflicting agents
	 * performed in the situation previous to the conflict, then maybe
	 * we can avoid the conflict to happen again.
	 * Therefore, the random solution generation works as follows:
	 * <ol>
	 * <li>	it retrieves the conflicting view, namely the {@code View}
	 * 			that contains the conflict;
	 * <li>	it chooses an agent that is in conflict in the conflicting
	 * 			{@code View};
	 * <li>	it retrieves the conflict source, namely the {@code View}
	 * 			that describes the situation previous to the conflict;
	 * <li>	it retrieves the action that the chosen agent performed 
	 * 			in the transition from the conflict source to the
	 * 			conflicting {@code View}; 
	 * <li>	it generates a {@code CaseSolution} that prohibits the
	 * 			agent to perform the retrieved action in the situation
	 * 			described by the conflict source {@code View}; and 
	 * <li>	since agents cannot understand case solutions, the generated
	 * 			{@code CaseSolution} is translated to norms that will be
	 * 			given to the agents.
	 * 
	 * @param 	cDesc the {@code CaseDescription} that describes
	 * 					the conflicting situation
	 * @param 	dmFunctions the domain functions, which are used to 
	 * 					translate case solutions to norms
	 * @param 	goal the goal with respect to the conflict has been detected
	 * @return the new {@code CaseSolution} for the case description
	 */
	private CaseSolution solveCase(CaseDescription cDesc,
			DomainFunctions dmFunctions, Goal goal) {

		List<Norm> norms = new ArrayList<Norm>();
		CaseSolution sol = new CaseSolution();
		SetOfPredicatesWithTerms precondition;
		Norm norm;
		
		/* Identify conflict and view to solve */
		int timeStepToSolve = this.identifyTimeStepToSolve(cDesc);

		ViewTransition conflictSource = cDesc.getConflictSource();
		View viewToSolve = conflictSource.getView(timeStepToSolve);

		/* Randomly choose the agent responsible for the conflict */
		List<Long> conflictingAgents = cDesc.getConflictingAgents();
		List<Long> responsibleAgents = new ArrayList<Long>();
		responsibleAgents.add(conflictingAgents.get(
				random.nextInt(conflictingAgents.size())));

		/* Retrieve those norms that are represented and apply
		 * to each agent that are considered as involved in the conflict */
		List<Norm> representedNormsApplicable = new ArrayList<Norm>();
		
		for(long agentId : responsibleAgents) {
			EnvironmentAgentContext aContext = dmFunctions.agentContextFunction(
					agentId, viewToSolve);
		
			NormsApplicableToAgentContext normsApplicable = 
					this.normReasoner.getNormsApplicable(aContext.getDescription());

			for(Norm n : normsApplicable.getApplicableNorms()) {
				if(this.normativeNetwork.isRepresented(n)) {
					representedNormsApplicable.add(n);
				}
			}
		}
		
		if(!representedNormsApplicable.isEmpty()) {
			return sol;
		}
		
//		List<Long> agentsWithApplicableNorms = normApplicability.getAgentIds();
//		
//		/* If any norms applied to the conflicting agents,
//		 * then do not generate norms */
//		for(long agentId : responsibleAgents) {
//			if(agentsWithApplicableNorms.contains(agentId)) {
//				return sol;
//			}
//		}
		
		this.nsMetrics.newNonRegulatedConflictsSolvedThisTick();
		
		/* For each responsible agent, generate a norm */
		for(Long agentId : responsibleAgents) {
			List<EnvironmentAgentAction> actions;
			EnvironmentAgentAction action;

			
			/* Forbid a random action of those performed by the agent */
			actions = dmFunctions.agentActionFunction(agentId,
					cDesc.getConflictSource());
//			action = actions.get(actions.size() + timeStepToSolve);
			action = actions.get(random.nextInt(actions.size()));
			
			/* Create norm's precondition */
			EnvironmentAgentContext aContext = dmFunctions.agentContextFunction(agentId, viewToSolve);
			precondition = aContext.getDescription();
			
			if(precondition.isEmpty()) {
				continue;
			}

			/* The norm exists in the normative network -> Retrieve it */
			if(normativeNetwork.contains(precondition,
					NormModality.Prohibition, action)) {
				
				norm = normativeNetwork.getNorm(precondition,
						NormModality.Prohibition, action);

				/* Only activate the if it is not represented, namely
				 * the norm and all its ancestors are inactive */
				if(!normativeNetwork.isRepresented(norm)) {
					sol.addProhibitedAction(viewToSolve, agentId, action);
					norms.add(norm);
					sol.addNorm(norm);
				}
			}
			/* The norm does not exist in the normative network 
			 * -> Create it and add it to the normative network */
			else {
				norm = new Norm(precondition, NormModality.Prohibition, action);
				sol.addProhibitedAction(viewToSolve, agentId, action);
				norms.add(norm);
				sol.addNorm(norm);
			}
		}
		return sol;	
	}

	/**
	 * Adapts the best solution of a given case, to solve a given {@code conflict}
	 * 
	 * @param cse the case from which to adapt its best solution
	 * @param conflict the conflict to solve
	 * @return a new {@code CaseSolution} aimed to solve
	 * 					the given {@code conflict}
	 */
	private CaseSolution adaptBestSolution(Case cse, Conflict conflict) {
		//		List<Norm> norms = new ArrayList<Norm>();

		// TODO
		return null;
	}

	/**
	 * Returns the time step in which the view to solve is
	 * 
	 * @param cDesc the case description
	 * @return the time step in which the view to solve is
	 */
	private int identifyTimeStepToSolve(CaseDescription cDesc) {
		return -1;
	}

}
