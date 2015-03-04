package es.csic.iiia.nsm.strategy.simon.others;

import java.util.ArrayList;
import java.util.List;

import es.csic.iiia.nsm.NormSynthesisMachine;
import es.csic.iiia.nsm.agent.language.PredicatesDomains;
import es.csic.iiia.nsm.config.DomainFunctions;
import es.csic.iiia.nsm.config.Goal;
import es.csic.iiia.nsm.net.norm.NetworkNodeState;
import es.csic.iiia.nsm.net.norm.NormativeNetwork;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.generation.Conflict;
import es.csic.iiia.nsm.norm.generation.NormGenerationMachine;
import es.csic.iiia.nsm.norm.generation.cbr.CBRNormGenerationMachine;
import es.csic.iiia.nsm.norm.reasoning.NormReasoner;
import es.csic.iiia.nsm.norm.refinement.lion.NormAttribute;

/**
 * The operators that the SIMON strategy uses to perform norm synthesis
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
public class ImplicitSIMONOperators {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------

	protected NormReasoner normReasoner;					// norm reasoner
	protected DomainFunctions dmFunctions;				// domain functions
	protected PredicatesDomains predDomains;			// predicates and their domains
	//	protected SIMONStrategy strategy;						// the norm synthesis strategy
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
	public ImplicitSIMONOperators(ImplicitSIMONStrategy strategy, NormReasoner normReasoner, 
			NormSynthesisMachine nsm) {

		//		this.strategy = strategy;
		this.normReasoner = normReasoner;
		this.dmFunctions = nsm.getDomainFunctions();
		this.predDomains = nsm.getPredicatesDomains();
		this.normativeNetwork = nsm.getNormativeNetwork();

		this.genMachine = new CBRNormGenerationMachine(this.normativeNetwork,
				normReasoner, strategy, nsm.getRandom(),nsm.getNormSynthesisMetrics());
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
			}
		}

		/* Add norms to add */
		for(Norm norm : normsToAdd)	{
			this.add(norm);
			//			this.activate(norm);
			//			this.link(norm);
		}

		/* Activate norms */
		for(Norm norm : normsToActivate)	{
			this.activate(norm);
			this.normativeNetwork.getUtility(norm).reset();
			this.normativeNetwork.removeAttribute(norm, NormAttribute.GENERALISABLE);
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

			/* Add the norm to the network in case it does not exist on it */
			this.normativeNetwork.add(norm);

			/* Activate the norm and link it to other norms in the network */
			this.activate(norm);
			this.link(norm);
			//			this.strategy.normCreated(norm);
			//			this.strategy.normAdded(norm);
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
		this.normativeNetwork.setState(norm, NetworkNodeState.ACTIVE);

		//		/* Add norm to the norm engine */
		//		this.normReasoner.addNorm(norm);
		//		this.strategy.normActivated(norm);
	}

	/**
	 * Deactivates a given {@code norm} in the normative network and removes
	 * it from the norm reasoner. Thus, the strategy will not take
	 * the norm into account to compute norm applicability and compliance
	 * 
	 * @param norm the norm to deactivate
	 */
	public void deactivate(Norm norm) {
		this.normativeNetwork.setState(norm, NetworkNodeState.INACTIVE);

		/* Remove the norm from the norm engine */
		//		this.normReasoner.removeNorm(norm);
		//		this.strategy.normDeactivated(norm);
	}

	/**
	 * Generalises a {@code child} norm to a {@code parent} norm
	 * 
	 * @param child the child norm
	 * @param parent the parent norm
	 */
	public void generalise(Norm child, Norm parent) {		
		this.normativeNetwork.addGeneralisation(child, parent);

		/* Deactivate the child norm if it is represented by
		 * an ancestor (the parent norm, likely) */
		for(Norm p : this.normativeNetwork.getParents(child)) {
			if(this.normativeNetwork.isRepresented(p)) {
				this.deactivate(child);
				break;
			}
		}

		/* Deactivate child norm */
		//		this.normativeNetwork.deactivate(child);

		//		/* Activate parent norm if it is not represented by
		//		 * an active ancestor */
		//		if(!this.normativeNetwork.isNormRepresented(parent)) {
		//			this.activate(parent);
		//		}
		//
		//		this.strategy.normDeactivated(child);
	}

	/**
	 * Specialises a norm in the normative network
	 * 
	 * @param norm the norm to specialise
	 * @param children the children into which to specialise the norm
	 */
	public void specialise(Norm norm, List<Norm> children) {
		this.deactivate(norm);
		//		this.normReasoner.removeNorm(norm);

		/* Activate child norms that are not represented by an ancestor */
		for(Norm child : children) {
			if(!normativeNetwork.isRepresented(child)) {
				this.activate(child);
			}
		}
	}

	/**
	 * 
	 * @param norm
	 */
	private void link(Norm norm) {
		List<Norm> topBoundary =
				(List<Norm>)this.normativeNetwork.getTopBoundary();
		List<Norm> visitedNorms = new ArrayList<Norm>();

		for(Norm normB : topBoundary) {
			if(!norm.equals(normB)) {
				this.searchRelationships(norm, normB, visitedNorms);
			}
		}

		//		List<Norm> notRepresented = new ArrayList<Norm>();
		//		notRepresented = normativeNetwork.getNorms();
		//		for(Norm n : normativeNetwork.getNorms()) {
		//			if(n.getPrecondition().toString().contains("*") && 
		//					this.normativeNetwork.getChildren(n).isEmpty()) {
		//				System.out.println();
		//			}
		//		}
	}

	/**
	 * 
	 * @param normA
	 * @param normB
	 * @param visitedNorms
	 */
	private void searchRelationships(Norm normA, Norm normB, List<Norm> visitedNorms) {
		List<Norm> normAChildren = this.normativeNetwork.getChildren(normA);
		List<Norm> normBChildren = this.normativeNetwork.getChildren(normB);
		boolean linked = false;

		List<Norm> normBSatisfiedChildren = 
				this.normReasoner.getSatisfiedNorms(normA, normBChildren);
		List<Norm> normBChildrenSatisfyingA = 
				this.normReasoner.getNormsSatisfying(normBChildren, normA);
		List<Norm> normBChildrenNotSatisfyingA = 
				this.normReasoner.getNormsNotSatisfying(normBChildren, normA);


		/* Para evitar generalizaciones a una misma norma */
		if(normA.equals(normB) /* || visitedNorms.contains(normB)*/) {
			return;
		}

		/* Comprobacion de paternidad. Compruebo si A puede ser padre de B.
		 * Compruebo si B satisface a A. Si lo hace, generalizo de 
		 * B a A siempre que A no sea ya un ancestor */
		if(normReasoner.satisfies(normB, normA))	{
			if(!this.normativeNetwork.isAncestor(normA, normB)) {
				this.generalise(normB, normA);
				linked = true;
			}

			/* Nos aseguramos de que B no tenga ningun hijo que sea tambien hijo de A */
			for(Norm normBChild : normBChildren) {
				if(normAChildren.contains(normBChild)) {
					this.normativeNetwork.removeGeneralisation(normBChild, normA);
				}
			}
		} 

		/* Comprobacion de descendencia. Compruebo si A es hija de B.
		 * Primero compruebo si A satisface a B */
		else if(normReasoner.satisfies(normA, normB)) {

			/* Si A satisface a B y no satisface a ninguno de sus hijos, 
			 * entonces A es hija directa de B */
			if(normBSatisfiedChildren.isEmpty()) {

				/* A es hija de B. Generalizamos, siempre que no sea ancestor ya */ 
				if(!this.normativeNetwork.isAncestor(normB, normA)) {
					this.generalise(normA, normB);
					linked = true;
				}

				/* Ahora comprobamos que A no esté en medio de B y alguna de sus hijas.
				 * Para ello, comprobamos si hay alguna hija de B que satisfaga a A.
				 * Para cada una de ellas que satisfaga A, quitamos la generalizacion
				 * de la hija hacia B y generalizamos a A */
				if(this.normativeNetwork.isAncestor(normB, normA)) {
					for(Norm normBChild : normBChildrenSatisfyingA) {
						this.normativeNetwork.removeGeneralisation(normBChild, normB);
						this.generalise(normBChild, normA);
					}
				}
			}
		}

		/* Si no se ha conseguido enlazar con nadie, seguimos buscando */
		if(!linked) {
			for(Norm normBChild : normBChildren) {
				this.searchRelationships(normA, normBChild, visitedNorms);
			}
		} 

		/* Si se ha enlazado continuamos igual, por si se puede seguir enlazando
		 * por abajo. La unica cuestion es que solo seguimos bajando por
		 * aquellos hijos que no satisfacen A (para asegurarnos de que si A
		 * ya se ha puesto como padre de B,  no se ponga tambien como padre 
		 * de sus hijas */
		else if(!normBChildrenNotSatisfyingA.isEmpty()) {		
			for(Norm normBChild : normBChildrenNotSatisfyingA) {
				this.searchRelationships(normA, normBChild, visitedNorms);
			}
		}
	}


	//	
	//	boolean linked = false;
	//
	//	/* Para evitar generalizaciones a una misma norma */
	//	if(normA.equals(normB) /* || visitedNorms.contains(normB)*/) {
	//		return;
	//	}
	//
	//	/* Hacemos constar la norma como visitada para evitar
	//	 * pasar varias veces por el mismo sitio 
	//	 */
	//	List<Norm> normBChildren = this.normativeNetwork.getChildren(normB);
	//	visitedNorms.add(normB);
	//
	//	/* Comprobacion de paternidad. Compruebo si A puede ser padre de B.
	//	 * Primero compruebo si B satisface a A. Si lo hace, generalizo de 
	//	 * B a A siempre que A no sea ya un ancestor */
	//	if(normReasoner.satisfies(normB, normA))	{
	//		if(!this.normativeNetwork.isAncestor(normA, normB)) {
	//			this.generalise(normB, normA);
	//			//				if(this.normativeNetwork.isNormRepresented(normB)) {
	////			this.deactivate(normB); TODO: Esto lo hace el operador generalise ya
	//			//				}
	//			linked = true;
	//		}
	//
	//		/* Faltaba esta puta mierda... */
	//		List<Norm> normAChildren = this.normativeNetwork.getChildren(normA);
	//		for(Norm normBChild : normBChildren) {
	//			if(normAChildren.contains(normBChild)) {
	//				this.normativeNetwork.removeGeneralisation(normBChild, normA);
	//			}
	//		}
	//	} 
	//
	//	/* Comprobacion de descendencia. Compruebo si A es hija de B.
	//	 * Primero compruebo si A satisface a B */
	//	else if(normReasoner.satisfies(normA, normB)) {
	//
	//		/* Si A satisface a B, puede ser hija directa o ir más abajo en el arbol.
	//		 * Para comprobarlo, miramos si A satisface algun hijo de B */
	//		List<Norm> normBSatisfiedChildren = new ArrayList<Norm>();
	//
	//		for(Norm normBChild : normBChildren) {
	//			if(!normA.equals(normBChild) && 
	//					normReasoner.satisfies(normA, normBChild)) {
	//				normBSatisfiedChildren.add(normBChild);
	//			}
	//		}
	//
	//		/* Si A satisface a B y no satisface a ninguno de sus hijos, 
	//		 * entonces A es hija directa de B */
	//		if(normBSatisfiedChildren.isEmpty()) {
	//
	//			/* A es hija de B. Generalizamos, siempre que no sea ancestor ya */ 
	//			if(!this.normativeNetwork.isAncestor(normB, normA)) {
	//				this.generalise(normA, normB);
	//
	//				// TODO: Esto ya lo hace ahora el operador generalise
	////				if(this.normativeNetwork.isRepresented(normB)) {
	////					this.deactivate(normA);
	////				}
	//				linked = true;
	//			}
	//
	//			/* Ahora comprobamos que A no esté en medio de B y alguna de sus hijas.
	//			 * Para ello, comprobamos si hay alguna hija de B que satisfaga a A.
	//			 * Para cada una de ellas que satisfaga A, quitamos la generalizacion
	//			 * de la hija hacia B y generalizamos a A */
	//			List<Norm> normBChildrenSatisfyingA = new ArrayList<Norm>();
	//			List<Norm> normBChildrenNotSatisfyingA = new ArrayList<Norm>();
	//			for(Norm normBChild : normBChildren) {	
	//				if(!normA.equals(normBChild)) {
	//					if(normReasoner.satisfies(normBChild, normA)) {
	//						normBChildrenSatisfyingA.add(normBChild);
	//					}
	//					else {
	//						normBChildrenNotSatisfyingA.add(normBChild);
	//						// TODO : Anyadir aqui un } else { normBChildrenNotSatisfyingA.add(normBChild); } y que baje por ellos?
	//					}
	//				}
	//				
	//					
	//
	//			}
	//
	//			/* ESTE CASO FUNCIONA, O ESO PARECE */
	//			if(this.normativeNetwork.isAncestor(normB, normA)) {
	//				for(Norm normBChild : normBChildrenSatisfyingA) {
	//					this.normativeNetwork.removeGeneralisation(normBChild, normB);
	//					this.generalise(normBChild, normA);
	//				}
	//			}
	//		}
	//	}
	//
	//	/* Si no se ha conseguido enlazar con nadie, seguimos buscando */
	//	if(!linked) { // Esto del linked no se yo... habria que hacer lo del TODO que he puesto arriba
	//		for(Norm normBChild : normBChildren) {
	//			this.searchRelationships(normA, normBChild, visitedNorms);
	//		}
	//	}

}
