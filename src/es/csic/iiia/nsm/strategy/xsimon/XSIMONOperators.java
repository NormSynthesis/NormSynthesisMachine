package es.csic.iiia.nsm.strategy.xsimon;

import java.util.ArrayList;
import java.util.List;

import es.csic.iiia.nsm.NormSynthesisMachine;
import es.csic.iiia.nsm.agent.language.PredicatesDomains;
import es.csic.iiia.nsm.config.DomainFunctions;
import es.csic.iiia.nsm.config.Goal;
import es.csic.iiia.nsm.net.norm.NetworkEdgeType;
import es.csic.iiia.nsm.net.norm.NormativeNetwork;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.evaluation.NormsApplicableInView;
import es.csic.iiia.nsm.norm.generation.Conflict;
import es.csic.iiia.nsm.norm.generation.NormGenerationMachine;
import es.csic.iiia.nsm.norm.generation.cbr.CBRNormGenerationMachine;
import es.csic.iiia.nsm.norm.reasoning.NormReasoner;
import es.csic.iiia.nsm.norm.refinement.xsimon.NormAttribute;

/**
 * The operators that the SIMON strategy uses to perform norm synthesis
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
public class XSIMONOperators {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------
	
	protected NormReasoner normReasoner;					// norm reasoner
	protected DomainFunctions dmFunctions;				// domain functions
	protected PredicatesDomains predDomains;			// predicates and their domains
	protected XSIMONStrategy strategy;						// the norm synthesis strategy
	protected NormativeNetwork normativeNetwork;	// the normative network
	protected NormGenerationMachine genMachine;	// the norm generation machine
	
	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * 
	 * @param 	strategy the SIMON norm synthesis strategy
	 * @param 	normReasoner the norm reasoner, to reason about norm
	 * 					applicability	and compliance
	 * @param 	nsm the norm synthesis machine
	 */
	public XSIMONOperators(XSIMONStrategy strategy, NormReasoner normReasoner, 
			NormSynthesisMachine nsm) {
		
		this.strategy = strategy;
		this.normReasoner = normReasoner;
		this.dmFunctions = nsm.getDomainFunctions();
		this.genMachine = new CBRNormGenerationMachine(nsm, normReasoner);
		this.predDomains = nsm.getPredicatesDomains();
		this.normativeNetwork = nsm.getNormativeNetwork();
	}

	/**
	 * Creates norms to regulate a given {@code conflict} that the norm
	 * synthesis machine has perceived in the scenario. The conflict
	 * is detected in terms of a system {@code goal}
	 * 
	 * @param conflict the perceived conflict
	 * @param goal the goal with respect to which the conflict has arisen
	 * @see Conflict
	 * @see Goal
	 */
	public void create(Conflict conflict, Goal goal) {
		List<Norm> normsToAdd = new ArrayList<Norm>();
		List<Norm> normsToActivate = new ArrayList<Norm>();
		List<Norm> norms;
		
		/* Perform norm generation */
		norms = genMachine.generateNorms(conflict, dmFunctions, goal);

		for(Norm norm : norms) {
			
			/* If the normative network does not contain the norm, (i.e., the norm
			 * does not exist), then add it to the normative network */
			if(!normativeNetwork.contains(norm)) {
				normsToAdd.add(norm);
			}
			/* If the normative network contains the norm, but it is not represented
			 * (that is, the norm and all its ancestors are inactive),
			 * then activate the norm */
			else	if(!normativeNetwork.isRepresented(norm)) {
				normsToActivate.add(norm);
				
				/* TODO: Aqui se podria hacer que cuando se reactiva una norma A 
				 * substituible con B, pero que se reactiva ahora en un contexto en
				 * que se generaria A o C, pues marcar que A es complementaria con C 
				 * Asi quedaria como que A es substituible con B y complementaria con C,
				 * entonces la que habria que desactivar es la B. 
				 */
			}
		}

		/* Add norms to add */
		for(Norm norm : normsToAdd)	{
			this.add(norm);
		}
		/* Activate norms */
		for(Norm norm : normsToActivate)	{
			this.activate(norm);
			this.normativeNetwork.getUtility(norm).reset();
			this.normativeNetwork.removeAttribute(norm, NormAttribute.Generalisable);
		}
	}

	/**
	 * Adds a norm to the normative network (if the normative network
	 * does not contain it yet) and activates it by setting its state
	 * to <tt>active</tt> in the normative network
	 * 
	 * @param norm the norm to add
	 */
	public void add(Norm norm) {
		if(!normativeNetwork.contains(norm)) {			
			normativeNetwork.add(norm);
			
			this.activate(norm);
			this.strategy.normCreated(norm);
		}
	}

	/**
	 * Activates a given {@code norm} in the normative network, resets
	 * its utility and adds the norm to the norm reasoner. Thus, the
	 * strategy will take the norm into account to compute norm
	 * applicability and compliance
	 * 
	 * @param norm the norm to activate
	 */	
	public void activate(Norm norm) {
		normativeNetwork.activate(norm);

		/* Add norm to the norm engine */
		this.normReasoner.addNorm(norm);
		this.strategy.normActivated(norm);
	}

	/**
	 * Deactivates a given {@code norm} in the normative network and removes
	 * it from the norm reasoner. Thus, the strategy will not take
	 * the norm into account to compute norm applicability and compliance
	 * 
	 * @param norm the norm to deactivate
	 */
	public void deactivate(Norm norm) {
		normativeNetwork.deactivate(norm);
		
		/* Remove the norm from the norm engine */
		this.normReasoner.removeNorm(norm);		
	}

	/**
	 * Generalises a {@code child} norm to a {@code parent} norm
	 * 
	 * @param child the child norm
	 * @param parent the parent norm
	 */
	public void generalise(Norm child, Norm parent) {		
		this.normativeNetwork.addGeneralisation(child, parent);

		/* Deactivate child norm */
		normativeNetwork.deactivate(child);
	}

	/**
	 * Specialises a norm in the normative network
	 * 
	 * @param norm the norm to specialise
	 * @param children the children into which to specialise the norm
	 */
	public void specialise(Norm norm, List<Norm> children) {
		this.deactivate(norm);

		/* Activate child norms */
		for(Norm child : normativeNetwork.getChildren(norm)) {
			if(!normativeNetwork.isRepresented(child))
				this.activate(child);
		}
	}
}
