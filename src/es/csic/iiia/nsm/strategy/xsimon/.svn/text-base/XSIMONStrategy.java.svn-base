package es.csic.iiia.nsm.strategy.xsimon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.csic.iiia.nsm.NormSynthesisMachine;
import es.csic.iiia.nsm.NormSynthesisMachine.NormGeneralisationMode;
import es.csic.iiia.nsm.agent.language.PredicatesDomains;
import es.csic.iiia.nsm.config.Dimension;
import es.csic.iiia.nsm.config.DomainFunctions;
import es.csic.iiia.nsm.config.Goal;
import es.csic.iiia.nsm.config.NormSynthesisSettings;
import es.csic.iiia.nsm.net.norm.NormativeNetwork;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.NormativeSystem;
import es.csic.iiia.nsm.norm.evaluation.NormComplianceOutcomes;
import es.csic.iiia.nsm.norm.evaluation.NormsApplicableInView;
import es.csic.iiia.nsm.norm.generation.Conflict;
import es.csic.iiia.nsm.norm.group.evaluation.NormGroupOutcomes;
import es.csic.iiia.nsm.norm.group.net.NormGroupNetwork;
import es.csic.iiia.nsm.norm.reasoning.NormReasoner;
import es.csic.iiia.nsm.norm.refinement.xsimon.NormAttribute;
import es.csic.iiia.nsm.perception.Monitor;
import es.csic.iiia.nsm.perception.ViewTransition;
import es.csic.iiia.nsm.strategy.NormSynthesisStrategy;

/**
 * The XSIMON norm synthesis strategy
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
public class XSIMONStrategy implements NormSynthesisStrategy {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------

	protected List<Dimension> normEvDimensions;
	protected NormSynthesisMachine nsm;
	protected NormSynthesisSettings nsmSettings; 
	protected NormReasoner normReasoner;
	protected NormativeNetwork normativeNetwork;
	protected NormGroupNetwork normGroupNetwork;
	
	private XSIMONNormGenerator normGenerator;
	private XSIMONNormEvaluator normEvaluator;
	private XSIMONNormClassifier normClassifier;
	private XSIMONNormRefiner normRefiner;

	protected DomainFunctions dmFunctions;
	protected PredicatesDomains predicatesDomains;
	protected Monitor monitor;
	
	protected XSIMONUtilityFunction utilityFunction;	
	protected XSIMONOperators operators;
	protected NormGeneralisationMode genMode; 
	protected int genStep;
	
	protected Map<ViewTransition, NormsApplicableInView> normApplicability;
	protected Map<Goal,Map<ViewTransition, NormComplianceOutcomes>> normCompliance;
	private Map<Goal,NormGroupOutcomes> normGroupCompliance;
	private Map<Norm, List<NormAttribute>> normClassifications;

	
	protected List<Norm> createdNorms;
	protected List<Norm> activatedNorms;
	protected List<Norm> 	normAdditions;
	protected List<Norm> normDeactivations;
	protected List<Norm> visitedNorms;

	protected List<ViewTransition> viewTransitions;
	protected Map<Goal,List<Conflict>> conflicts;
	
	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------

	/**
	 * Constructor
	 * 
	 * @param 	nsm the norm synthesis machine
	 * @param 	genMode the SIMON generalisation mode
	 */
	public XSIMONStrategy(NormSynthesisMachine nsm,
			NormGeneralisationMode genMode, int genStep) {

		this.nsm = nsm;
		this.genMode = genMode;
		this.genStep = genStep;

		this.nsmSettings = nsm.getNormSynthesisSettings();
		this.normEvDimensions = nsm.getNormEvaluationDimensions();
		this.dmFunctions = nsm.getDomainFunctions();
		this.predicatesDomains = this.nsm.getPredicatesDomains();
		this.normativeNetwork = nsm.getNormativeNetwork();
		this.normGroupNetwork = this.nsm.getNormGroupNetwork();
		this.monitor = nsm.getMonitor();

		this.normReasoner = new NormReasoner(this.nsmSettings.getSystemGoals(), 
				this.predicatesDomains, this.dmFunctions);
		
		this.operators = new XSIMONOperators(this, normReasoner, nsm);
		this.utilityFunction = new XSIMONUtilityFunction();

		this.normApplicability = new HashMap<ViewTransition, 
				NormsApplicableInView>();

		this.normCompliance = new HashMap<Goal,
				Map<ViewTransition,NormComplianceOutcomes>>();
		for(Goal goal : nsmSettings.getSystemGoals()) {
			this.normCompliance.put(goal, new HashMap<ViewTransition,
					NormComplianceOutcomes>());
		}
		
		this.normGroupCompliance = new HashMap<Goal,NormGroupOutcomes>();
		for(Goal goal : nsmSettings.getSystemGoals()) {
			this.normGroupCompliance.put(goal, new NormGroupOutcomes());
		}
		
		this.viewTransitions = new ArrayList<ViewTransition>();
		this.conflicts = new HashMap<Goal, List<Conflict>>();
		
		this.normGenerator = new XSIMONNormGenerator(nsmSettings, monitor,
				dmFunctions, operators);
		
		this.normEvaluator = new XSIMONNormEvaluator(normEvDimensions,
				nsmSettings, dmFunctions, normativeNetwork, normGroupNetwork,
				normReasoner, utilityFunction, operators);
		
		this.normClassifier = new XSIMONNormClassifier(normEvDimensions,
				nsmSettings,normativeNetwork, normGroupNetwork, operators);
		
		this.normRefiner = new XSIMONNormRefiner(normClassifier, normativeNetwork,
				dmFunctions, predicatesDomains, normReasoner, operators,
				genMode, genStep);
		
		this.createdNorms = new ArrayList<Norm>();
		this.activatedNorms = new ArrayList<Norm>();
		this.normAdditions = new ArrayList<Norm>();
		this.normDeactivations = new ArrayList<Norm>();
		this.visitedNorms = new ArrayList<Norm>();
	}

	/**
	 * Executes IRON's strategy and outputs the resulting normative system.
	 * The norm synthesis cycle consists in three steps:
	 * <ol>
	 * <li> Norm generation. Generates norms for each detected conflict.
	 * 			Generated norms are aimed to avoid detected conflicts in the future;
	 * <li> Norm evaluation. Evaluates norms in terms of their effectiveness
	 * 			and necessity, based on the outcome of their compliances and 
	 * 			infringements, respectively; and
	 * <li> Norm refinement. Generalises norms which utilities are over
	 * 			generalisation thresholds, and specialises norms which utilities
	 * 			are under specialisation thresholds. Norm generalisations can be
	 * 			performed in Shallow or Deep mode.
	 * 
	 * @return the normative system resulting from the norm synthesis cycle
	 */
	public NormativeSystem execute() {	
		
		this.normAdditions.clear();
		this.normDeactivations.clear();
		this.createdNorms.clear();
		this.activatedNorms.clear();
		this.visitedNorms.clear();

		this.normGenerator.step(viewTransitions, conflicts);
		this.normEvaluator.step(viewTransitions, normApplicability,
				normCompliance, normGroupCompliance);
		
		/* Compute norms that must be revised */
		List<Norm> normsToRevise = new ArrayList<Norm>();

		for(NormsApplicableInView vna : normApplicability.values()) {
			for(long id : vna.getAgentIds()) {
				for(Norm norm : vna.get(id).getApplicableNorms()) {
					if(!normsToRevise.contains(norm)) {
						normsToRevise.add(norm);
					}
				}
			}
		}
		
		normClassifications = this.normClassifier.step(normsToRevise);
		
		this.normRefiner.step(normClassifications);

		/* Link new norms that have been generalised to
		 * other potential child norms in the normative network */
		for(Norm normA : this.createdNorms) {
			for(Norm normB : this.normativeNetwork.getActiveNorms()) {
				if(!normA.equals(normB))
					this.searchRelationships(normA, normB, null, visitedNorms);
			}
		}
		
		/* Return the current normative system */
		return normativeNetwork.getNormativeSystem();
	}

	/**
	 * Searches for potential children of norm A
	 * 
	 * @param normA the norm to search children
	 * @param normB the norm to start the search for
	 * @param parentOfNormB the parent of norm B
	 * @param visitedNorms a list of already visited norms (to avoid cycles)
	 */
	protected void searchRelationships(Norm normA, Norm normB,
			Norm parentOfNormB, List<Norm> visitedNorms) {

		/* Norm A is parent of norm B, and is not already an ancestor */
		if(normReasoner.satisfies(normB, normA) &&
				!this.normativeNetwork.isAncestor(normA, normB)) {
			
			if(parentOfNormB != null) {
				if(normReasoner.satisfies(normA, parentOfNormB)) {
					this.normativeNetwork.removeGeneralisation(
							normB, parentOfNormB);
					
					this.operators.generalise(normA, parentOfNormB);
				}
			}
			
			/* Generalise only if normA is not already an ancestor of normB */
			if(!this.normativeNetwork.isAncestor(normA, normB)) {
				this.operators.generalise(normB, normA);
			}
		}
		else {
			List<Norm> children = this.normativeNetwork.getChildren(normB);
			List<Norm> satisfiedChildren = new ArrayList<Norm>();

			for(Norm child : children) {
				if(!normA.equals(child)) {
					if(normReasoner.satisfies(normA, child)) {
						satisfiedChildren.add(child);
					}

					if(!visitedNorms.contains(child)) {
						visitedNorms.add(child);
						this.searchRelationships(normA, child, normB, visitedNorms);
					}
				}
			}

			// Norm B is a direct parent of norm A
			if(normReasoner.satisfies(normA, normB) && satisfiedChildren.isEmpty() &&
					!this.normativeNetwork.isAncestor(normB, normA)) {
				this.operators.generalise(normA, normB); 
			}
		}
	}
	
	/**
	 * 
	 * @param norm
	 */
	public void normCreated(Norm norm) {
		this.createdNorms.add(norm);
		this.normAdditions.add(norm);
	}

	/**
	 * 
	 * @param norm
	 */
	public void normActivated(Norm norm) {
		this.activatedNorms.add(norm);
		this.normAdditions.add(norm);
	}

	/**
	 * 
	 * @param norm
	 */
	public void normDeactivated(Norm norm) {
		this.normDeactivations.add(norm);
	}

	/**
	 * 
	 * @return
	 */
	public List<Norm> getNewAdditionsToNormativeSystem() {
		return this.normAdditions;
	}

	/**
	 * 
	 * @return
	 */
	public Map<Goal, List<Conflict>> getNonRegulatedConflictsThisTick() {
		return this.conflicts;
	}
}