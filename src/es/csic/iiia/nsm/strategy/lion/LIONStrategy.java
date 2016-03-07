package es.csic.iiia.nsm.strategy.lion;

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
import es.csic.iiia.nsm.perception.Monitor;
import es.csic.iiia.nsm.perception.ViewTransition;
import es.csic.iiia.nsm.strategy.NormSynthesisStrategy;

/**
 * The XSIMON norm synthesis strategy
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
public class LIONStrategy implements NormSynthesisStrategy {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------

	protected List<Dimension> normEvDimensions;
	protected NormSynthesisMachine nsm;
	protected NormSynthesisSettings nsmSettings;
	protected NormReasoner normReasoner;
	protected NormativeNetwork normativeNetwork;
	protected NormGroupNetwork normGroupNetwork;

	protected LIONNormGenerator normGenerator;
	protected LIONNormEvaluator normEvaluator;
	protected LIONNormClassifier normClassifier;
	protected LIONNormRefiner normRefiner;

	protected DomainFunctions dmFunctions;
	protected PredicatesDomains predicatesDomains;
	protected Monitor monitor;

	protected LIONUtilityFunction utilityFunction;	
	protected LIONOperators operators;
	protected NormGeneralisationMode genMode; 
	protected int genStep;

	protected Map<ViewTransition, NormsApplicableInView> normApplicability;
	protected Map<Goal,Map<ViewTransition, NormComplianceOutcomes>> normCompliance;
	protected Map<Goal,NormGroupOutcomes> normGroupCompliance;

	protected List<Norm> normsInNormativeNetwork;
	protected List<Norm> normsInNormativeSystem;
	protected List<Norm> normsAddedToNNThisCycle;
	protected List<Norm> normsAddedToNSThisCycle;
	protected List<Norm> normsRemovedFromNSThisCycle;
	protected List<Norm> visitedNorms;

	protected List<ViewTransition> viewTransitions;
	protected Map<Goal,List<Conflict>> conflicts;

	protected boolean hasNonRegulatedConflictsThisTick;

	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------

	/**
	 * Constructor
	 * 
	 * @param 	nsm the norm synthesis machine
	 * @param 	genMode the SIMON generalisation mode
	 */
	public LIONStrategy(NormSynthesisMachine nsm,
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
		this.normReasoner = nsm.getNormReasoner();
		
		this.operators = new LIONOperators(this, normReasoner, nsm);
		this.utilityFunction = new LIONUtilityFunction();
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

		this.normGenerator = new LIONNormGenerator(nsmSettings, monitor,
				dmFunctions, operators);

		this.normEvaluator = new LIONNormEvaluator(normEvDimensions,
				nsmSettings, dmFunctions, normativeNetwork, normGroupNetwork,
				normReasoner, utilityFunction, operators);

		this.normRefiner = new LIONNormRefiner(normEvDimensions, 
				nsmSettings, dmFunctions, predicatesDomains, normativeNetwork, 
				normGroupNetwork, normReasoner, operators,
				genMode, genStep, nsm.getRandom());

		this.normsInNormativeNetwork = new ArrayList<Norm>();
		this.normsInNormativeSystem  = new ArrayList<Norm>();
		this.normsAddedToNNThisCycle = new ArrayList<Norm>();
		this.normsAddedToNSThisCycle = new ArrayList<Norm>();
		this.normsRemovedFromNSThisCycle = new ArrayList<Norm>();
		this.visitedNorms = new ArrayList<Norm>();

		this.hasNonRegulatedConflictsThisTick = false;
	}

	/**
	 * Executes the norm synthesis strategy and outputs the resulting
	 * normative system. The norm synthesis cycle consists in three steps:
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
		this.hasNonRegulatedConflictsThisTick = false;
		this.visitedNorms.clear();

		/* Norm generation */
		List<Norm> normsActivated = this.normGenerator.step(viewTransitions, conflicts);

		/* Norm evaluation */
		this.normEvaluator.step(viewTransitions, normApplicability,
				normCompliance, normGroupCompliance);

		/* Norm refinement */
		this.normRefiner.step(normApplicability, normsActivated);

		/* Manage lists that control new additions to the normative network,
		 * normative system, as well as norms that have been removed */
		this.manageNormControlLists();
		
		/* Return the current normative system */
		return normativeNetwork.getNormativeSystem();
	}

	/**
	 * 
	 */
	protected void manageNormControlLists() {
		this.normsAddedToNNThisCycle.clear();
		this.normsAddedToNSThisCycle.clear();
		this.normsRemovedFromNSThisCycle.clear();
		
		/* Check norm additions to the normative network */
		for(Norm norm : this.normativeNetwork.getNorms()) {
			if(!this.normsInNormativeNetwork.contains(norm)) {
				this.normsInNormativeNetwork.add(norm);
				this.normsAddedToNNThisCycle.add(norm);
			}
		}
		
		/* Check norm additions to the normative system */
		for(Norm norm : this.normativeNetwork.getNormativeSystem()) {
			if(!this.normsInNormativeSystem.contains(norm)) {
				this.normsInNormativeSystem.add(norm);
				this.normsAddedToNSThisCycle.add(norm);
			}
		}
		
		/* Check norm removals from the normative system */
		List<Norm> toRemove = new ArrayList<Norm>();
		for(Norm norm : this.normsInNormativeSystem) {
			if(!this.normativeNetwork.getNormativeSystem().contains(norm)) {
				toRemove.add(norm);
				this.normsRemovedFromNSThisCycle.add(norm);
			}
		}
		for(Norm norm : toRemove) {
			this.normsInNormativeSystem.remove(norm);
		}
		
		/* Add to the norm reasoner those new norms that are now represented */
		List<Norm> normsRepresented = this.normativeNetwork.getRepresentedNorms();
		for(Norm norm : normsRepresented) {
			if(!this.normReasoner.contains(norm)) {
				this.normReasoner.addNorm(norm);
			}
		}
	}

	public LIONOperators getOperators() {
		return this.operators;
	}
	
	/**
	 * 
	 * @return
	 */
	public List<Norm> getNormsAddedToNSThisCycle() {
		return this.normsAddedToNSThisCycle;
	}

	/**
	 * 
	 * @return
	 */
	public List<Norm> getNormsRemovedFromNSThisCycle() {
		return this.normsRemovedFromNSThisCycle;
	}

	/**
	 * 
	 * @return
	 */
	public boolean hasNonRegulatedConflictsThisTick() {
		return this.hasNonRegulatedConflictsThisTick;
	}

	/**
	 * 
	 */
	@Override
	public void newNonRegulatedConflictsSolvedThisTick() {
		this.hasNonRegulatedConflictsThisTick = true;
	}
}