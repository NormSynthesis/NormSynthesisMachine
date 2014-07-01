package es.csic.iiia.nsm.strategy.iron;

import java.util.ArrayList;
import java.util.List;

import es.csic.iiia.nsm.NormSynthesisMachine;
import es.csic.iiia.nsm.agent.AgentAction;
import es.csic.iiia.nsm.agent.language.PredicatesDomains;
import es.csic.iiia.nsm.agent.language.SetOfPredicatesWithTerms;
import es.csic.iiia.nsm.config.DomainFunctions;
import es.csic.iiia.nsm.config.Goal;
import es.csic.iiia.nsm.net.norm.NormativeNetwork;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.NormModality;
import es.csic.iiia.nsm.norm.generation.Conflict;
import es.csic.iiia.nsm.norm.generation.NormGenerationMachine;
import es.csic.iiia.nsm.norm.generation.cbr.CBRNormGenerationMachine;
import es.csic.iiia.nsm.norm.reasoning.NormReasoner;
import es.csic.iiia.nsm.norm.refinement.iron.GeneralisationTrees;

/**
 * The operators that the IRON strategy uses to perform norm synthesis.
 * Specifically, IRON uses operators <tt>create</tt>, <tt>add</tt>,
 * <tt>activate</tt>, <tt>deactivate</tt>, <tt>generalise</tt> and
 * <tt>specialise</tt>
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 *
 */
public class IRONOperators {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------
	
	protected NormReasoner normReasoner;					// norm reasoner
	protected DomainFunctions dmFunctions;				// domain functions
	protected PredicatesDomains predDomains;			// predicates and their domains
	protected IRONStrategy strategy;							// the norm synthesis strategy
	protected NormativeNetwork normativeNetwork;	// the normative network
	protected NormGenerationMachine genMachine;	// the norm generation machine
	protected GeneralisationTrees genTrees;			// potential generalisations
	
	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------
	
	/**
	 *  
	 * @param strategy
	 * @param nsm
	 * @param normReasoner
	 */
	public IRONOperators(IRONStrategy strategy, NormSynthesisMachine nsm, 
			NormReasoner normReasoner) {
		
		this.strategy = strategy;
		this.normReasoner = normReasoner;
		this.dmFunctions = nsm.getDomainFunctions();
		this.genMachine = new CBRNormGenerationMachine(nsm, normReasoner);
		this.predDomains = nsm.getPredicatesDomains();
		this.normativeNetwork = nsm.getNormativeNetwork();
		this.genTrees = strategy.getGeneralisationTrees();
	}

	/**
	 * Uses a CBR-based norm generation machine to generate norms from a given
	 * given {@code conflict}. Generated norms are aimed to avoid the conflict
	 * in the future. Finally, it adds the generated norms to the
	 * {@code NormativeNetwork}
	 *  
	 * @param conflict the conflict to avoid in the future by means of norms
	 * @param goal the goal in which terms the conflict was detected
	 */
	public void create(Conflict conflict, Goal goal) {
		List<Norm> normsToAdd = new ArrayList<Norm>();
		List<Norm> normsToActivate = new ArrayList<Norm>();
		List<Norm> norms;
		
		/* Norm generation */
		norms = genMachine.generateNorms(conflict, dmFunctions, goal);

		for(Norm norm : norms) {

			/* The norm does not exist -> Add it to the normative network */
			if(!normativeNetwork.contains(norm)) {
				normsToAdd.add(norm);
			}
			/* The norm already exists and it is not active -> Activate it */
			else	if(!normativeNetwork.isActive(norm)) {
				normsToActivate.add(norm);
			}
		}

		/* Add norms to add */
		for(Norm norm : normsToAdd)	{
			this.add(norm);
			
			/* Create norm's candidate generalisations */
			this.genTrees.add(norm);
		}
		/* Activate norms */
		for(Norm norm : normsToActivate)	{
			this.activate(norm);	
		}
	}


	/**
	 * Adds a norm to the normative network and activates it by 
	 * means of method {@code activate(Norm)}
	 * 
	 * @param norm the norm to add to the normative network
	 * @see NormativeNetwork
	 */
	public void add(Norm norm) {
		if(!normativeNetwork.contains(norm)) {			
			normativeNetwork.add(norm);
			this.activate(norm);
			this.strategy.normCreated(norm);
		}
	}

	/**
	 * Activates a norm (i.e., sets its state to <tt>active</tt>
	 * in the normative network
	 * 
	 * @param norm the norm to activate in the normative network
	 * @see NormativeNetwork
	 */
	public void activate(Norm norm) {
		normativeNetwork.activate(norm);
		normativeNetwork.getUtility(norm).reset();

		/* Add norm to the norm engine */
		this.normReasoner.addNorm(norm);
		this.strategy.normActivated(norm);
	}
	
	/**
	 * Deactivates a norm (i.e., sets its state to <tt>inactive</tt>
	 * in the normative network
	 * 
	 * @param norm the norm to deactivate in the normative network
	 * @see NormativeNetwork
	 */
	public void deactivate(NormativeNetwork normativeNetwork, Norm norm) {
		normativeNetwork.deactivate(norm);
		this.normReasoner.removeNorm(norm);
	}

	/**
	 * Generalises a {@code List} of {@code children} norms to a {@code parent}.
	 * With this aim, it: (1) adds generalisation relationships from each child
	 * to the parent in the normative network; (2) activates the {@code parent}
	 * norm; and (3) deactivates the {@code children}
	 * 
	 * @param normativeNetwork the normative network
	 * @param parent the parent norm
	 * @param children the {@code List} of child norms
	 * @see NormativeNetwork
	 */
	public void generalise(NormativeNetwork normativeNetwork, 
			Norm parent, List<Norm> children) {
		
		normativeNetwork.activate(parent);
		normativeNetwork.add(parent);	

		/* Create norm's candidate generalisations */
		this.genTrees.add(parent);

		/* Generate child norms, add relationships and deactivate children */
		for(Norm ch : children) {
			SetOfPredicatesWithTerms precond = ch.getPrecondition();
			NormModality modality = ch.getModality();
			AgentAction action = ch.getAction();
			Goal goal = ch.getGoal();
			Norm child;

			// If the norm exists, get it. Otherwise, create and add it to the normative network
			if(normativeNetwork.contains(precond, modality, action, goal)) {
				child = normativeNetwork.getNorm(precond, modality, action, goal);
			}
			else {
				child = new Norm(precond, modality, action, goal);
				normativeNetwork.add(child);
			}

			// Deactivate child norm and add relationship with the parent
			this.normativeNetwork.addGeneralisation(child, parent);
			this.normativeNetwork.deactivate(child);
		}
	}

	/**
	 * Specialises a given {@code norm} into its {@code children}.
	 * With this aim, it: (1) deactivates the {@code norm}; and (2) activates
	 * its {@code children}
	 * 
	 * @param normativeNetwork the normative network
	 * @param norm the norm to specialise
	 * @param children the {@code List} of child norms
	 * @see NormativeNetwork
	 */
	public void specialise(NormativeNetwork normativeNetwork, Norm norm, List<Norm> children) {

		/* Deactivate norm*/
		normativeNetwork.deactivate(norm);

		/* Activate child norms*/
		for(Norm child : normativeNetwork.getChildren(norm)) {
			normativeNetwork.activate(child);
		}
	}
}
