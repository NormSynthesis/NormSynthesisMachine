package es.csic.iiia.nsm.norm.reasoning;

import java.util.ArrayList;
import java.util.List;

import es.csic.iiia.nsm.agent.AgentAction;
import es.csic.iiia.nsm.agent.AgentContext;
import es.csic.iiia.nsm.agent.language.PredicatesDomains;
import es.csic.iiia.nsm.agent.language.SetOfPredicatesWithTerms;
import es.csic.iiia.nsm.agent.language.SetOfStrings;
import es.csic.iiia.nsm.config.DomainFunctions;
import es.csic.iiia.nsm.config.Goal;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.NormModality;
import es.csic.iiia.nsm.norm.evaluation.NormComplianceOutcomes;
import es.csic.iiia.nsm.norm.evaluation.NormsApplicableInView;
import es.csic.iiia.nsm.norm.evaluation.NormsApplicableToAgentContext;
import es.csic.iiia.nsm.perception.View;
import es.csic.iiia.nsm.perception.ViewTransition;

/**
 * The norm reasoner employs the {@code NormEngine} (by extending it) to
 * reason about norms applicability and compliance. It includes methods to
 * compute which norms apply to the agents in a {@code View}, and to
 * assess if the agents have complied or infringed norms in a transition of 
 * views (a {@code ViewTransition})
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 * @see NormEngine
 * @see View
 * @see ViewTransition
 */
public class NormReasoner extends NormEngine {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------

	private PredicatesDomains predDomains;
	private DomainFunctions dmFunctions;
	
	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------

	/**
	 * Constructor
	 * 
	 * @param dmFunctions the domain functions
	 */
	public NormReasoner(List<Goal> goals, PredicatesDomains predDomains,
			DomainFunctions dmFunctions) {
		
		super(predDomains);
		
		this.predDomains = predDomains;
		this.dmFunctions = dmFunctions;
	}

	/**
	 * Returns a {@code List} of the norms that apply to the
	 * agents in a given {@code viewTransition}
	 * 
	 *  @param viewTransition the transition of views
	 *  @return 	a {@code List} of the norms that apply to the
	 * 						agents in a given {@code viewTransition}
	 */
	public NormsApplicableInView getNormsApplicable(
			ViewTransition viewTransition) {

		List<Long> agentIds = new ArrayList<Long>();
		View pView = viewTransition.getView(-1);
		View view = viewTransition.getView(0);

		NormsApplicableInView nAppl = new NormsApplicableInView();
		nAppl.setViewTransition(viewTransition);

		/* Just check norm applicability for those agents that
		 * exist in all views of the stream */
		for(Long agentId : pView.getAgentIds())	{
			if(view.getAgentIds().contains(agentId)) {
				agentIds.add(agentId);
			}
		}

		/* Add norm applicability for each agent 
		 * (View Agent Applicable norms) */
		for(Long agentId : agentIds) {
			
			AgentContext aContext = this.dmFunctions.
					agentContextFunction(agentId, pView);
			
			NormsApplicableToAgentContext nAppToPred =
					this.getNormsApplicable(
							aContext.getDescription());

			if(nAppToPred.getApplicableNorms().size() > 0) {
				nAppl.add(agentId, nAppToPred);
			}
		}
		return nAppl;
	}

	/**
	 * 
	 * @param agContext
	 * @return
	 */
	public NormsApplicableToAgentContext getNormsApplicable(
			SetOfPredicatesWithTerms context) {

		/* Add facts to the rule engine and reason about norms*/
		this.reset();
		this.addFacts(context);
		this.reason();
		
		/* Generate object to return */
		NormsApplicableToAgentContext nAppToPred =
				new NormsApplicableToAgentContext(context, this.applicableNorms);
		
		return nAppToPred;
	}
	
	
//	/**
//	 * Returns a {@code List} of the norms that apply to an agent with id
//	 * {@code agentId} in a given {@code view}
//	 * 
//	 *  @param agentId the id of the agent
//	 *  @param view the view
//	 *  @return 	a {@code List} of the norms that apply to an agent with id
//	 * 						{@code agentId} in a given {@code view}
//	 */
//	private NormsApplicableToAgentContext getApplicableNorms(
//			long agentId, View view) {
//
//		AgentContext aContext = this.dmFunctions.
//				agentContextFunction(agentId, view);
//		SetOfPredicatesWithTerms context = aContext.getDescription();
//
//		/* Add facts to the rule engine and reason about norms*/
//		
//		this.reset();
//		this.addFacts(context);
//		this.reason();
//
//		NormsApplicableToAgentContext nAppToPred =
//				new NormsApplicableToAgentContext(context, this.applicableNorms);
//
//		return nAppToPred;
//	}

	/**
	 * Employs {@code normApplicability} to compute and return 
	 * an object {@code NormComplianceOutcomes} that contains information
	 * about the norms that agents complied with or infringed in a given
	 * {@code ViewTransition} which is contained in the {@code normApplicability}  
	 * 
	 * @param 	normApplicability an object containing a {@code ViewTransition}
	 * 					a list of agents to which some norms apply in the given
	 * 					transition of views
	 * @param goal the goal 
	 * @return an object {@code NormComplianceOutcomes} that contains information
	 * 					about the norms that agents complied with or infringed in a given
	 * 					{@code ViewTransition} which is contained in the 
	 * 					{@code normApplicability}  
	 * @see NormsApplicableInView
	 * @see NormComplianceOutcomes
	 */
	public NormComplianceOutcomes checkNormComplianceAndOutcomes(
			NormsApplicableInView normApplicability, Goal goal) {

		NormComplianceOutcomes gNormCompliance = new NormComplianceOutcomes();
		ViewTransition vTrans = normApplicability.getViewTransition();
		List<Long> agentIds = normApplicability.getAgentIds();

		/* Check norm compliance and conflict of each agent in the view */
		for(Long agentId : agentIds) {
			NormsApplicableToAgentContext agentApplicableNorms =
					normApplicability.get(agentId); // norms that apply to the agent

			for(Norm norm : agentApplicableNorms.getApplicableNorms()) {
				View view = vTrans.getView(0);
				SetOfPredicatesWithTerms agContext =
						agentApplicableNorms.getAgentContext();

				/* Check norm compliance */
				boolean isFulfillment = this.hasFulfilledNorm(
						vTrans, agentId, norm);

				/* Check outcome (conflict/no conflict) of the norm 
				 * compliance/infringement */
				boolean hasConflict = this.dmFunctions.hasConflict(
						view, agentId, goal);

				/* Divide applicable norms between complied/infringed norms
				 * with/without conflict*/				
				if(isFulfillment) {
					if(hasConflict) {
						gNormCompliance.addFulfilmentWithConflict(agContext, norm);
					}
					else {
						gNormCompliance.addFulfilmentWithNoConflict(agContext, norm);
					}
				}
				else {
					if(hasConflict) {
						gNormCompliance.addInfringementWithConflict(agContext, norm);
					}
					else {
						gNormCompliance.addInfringementWithNoConflict(agContext, norm);
					}
				}
			}
		}
		return gNormCompliance;
	}

	/**
	 * Returns <tt>true<tt> if the agent with id {@code agId} has fulfilled
	 * the given {@code norm} in the transition of views {@code vTrans}.
	 * Otherwise, it returns <tt>false<tt>
	 * 
	 * @param vTrans the transition of views
	 * @param agId the id of the agent
	 * @param norm the norm 
	 * @return <tt>true<tt> if the agent with id {@code agId} has fulfilled
	 * 					the given {@code norm} in the transition of views
	 * 					{@code vTrans}
	 */
	public boolean hasFulfilledNorm(ViewTransition vTrans,
			long agId, Norm norm) {
		
		NormModality modality = norm.getModality();
		AgentAction normAction = norm.getAction();

		/* Retrieve the actions that the agent performed 
		 * during the transition of states */
		List<AgentAction> agActions = dmFunctions.
				agentActionFunction(agId, vTrans);

		for(AgentAction agAction : agActions) {

			/* If the norm prohibits to perform the action, and the agent did not
			 * performed the given action, then it fulfilled the norm */
			if(modality == NormModality.Prohibition &&
					agAction != normAction) {
				return true;
			}

			/* If the norm obligates to perform the action, and the agent 
			 * performed the given action, then it infringed the norm */
			else if(modality == NormModality.Obligation && 
					agAction == normAction) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns <tt>true</tt> if norm {@code nA} satisfies norm {@code nB},
	 * namely their postconditions are equal, and the precondition of 
	 * {@code nA} satisfies the precondition of {@code nB}
	 * 
	 * @param nA the norm to satisfy nB
	 * @param nB the norm to be satisfied by nA
	 * @return <tt>true</tt> if norm {@code nA} satisfies norm {@code nB},
	 * 					namely their postconditions are equal, and the precondition of 
	 * 					{@code nA} satisfies the precondition of {@code nB}
	 */
	public boolean satisfies(Norm nA, Norm nB) {
		SetOfPredicatesWithTerms nAPrecond = nA.getPrecondition();
		SetOfPredicatesWithTerms nBPrecond = nB.getPrecondition();

		NormModality nAModality = nA.getModality();
		NormModality nBModality = nB.getModality();
		AgentAction nAAction = nA.getAction();
		AgentAction nBAction = nB.getAction();

		/* Check that post-conditions are the same*/
		if(nAModality != nBModality || nAAction != nBAction) {
			return false;
		}
		for(String predicate : nAPrecond.getPredicates()) {
			String termA = nAPrecond.getTerms(predicate).get(0);
			String termB = nBPrecond.getTerms(predicate).get(0);

			if(termA.equals(termB)) {
				continue;
			}
			SetOfStrings nAParentTerms = this.predDomains.
					getParentTerms(predicate, termA);

			if(!nAParentTerms.contains(termB)) {
				return false;
			}
		}
		return true;
	}
}
