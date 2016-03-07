package es.csic.iiia.nsm.norm.refinement.iron;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.csic.iiia.nsm.agent.AgentAction;
import es.csic.iiia.nsm.agent.language.PredicatesDomains;
import es.csic.iiia.nsm.agent.language.SetOfPredicatesWithTerms;
import es.csic.iiia.nsm.config.DomainFunctions;
import es.csic.iiia.nsm.net.norm.NormativeNetwork;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.NormModality;

/**
 * Class containing the potential generalisation of each norm in the normative
 * network. Every time the NSM generates a new norm, it computes its potential
 * generalisations and adds them to this data structure. Then, the IRON
 * strategy continuously checks along time if any potential generalisation
 * may be performed. Whenever a potential generalisation fulfils the conditions
 * to be performed, the NSM generalises norms as follows:
 * <ol>
 * <li> it retrieves the potential parent from the potential generalisation;
 * <li> it adds the parent norm to the normative network and activates it;
 * <li> it deactivates the child norms in the normative network; and
 * <li> it sets the potential generalisation as "performed".
 * </ol>
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 * @see PotentialGeneralisation
 */
public class GeneralisationTrees {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------
	
	private Map<Norm, List<PotentialGeneralisation>> potentialGens;
	private GeneralisationReasoner genReasoner;
	
	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------
	
	/**
	 * Constructor with parameters
	 * 
	 * @param dmFunctions the domain functions
	 * @param nNetwork the normative network
	 */
	public GeneralisationTrees(PredicatesDomains predDomains, 
			DomainFunctions dmFunctions, NormativeNetwork nNetwork) {
		
		this.potentialGens = new HashMap<Norm, List<PotentialGeneralisation>>();
		this.genReasoner = new GeneralisationReasoner(predDomains, dmFunctions);
	}

	/**
	 * Creates the potential generalisations of a norm
	 * 
	 * @param norm the norm for whith to create potential generalisations
	 */
	public void add(Norm norm) {
		List<PotentialGeneralisation> candidateGens =
				new ArrayList<PotentialGeneralisation>();
		
		List<SetOfPredicatesWithTerms> pPreconds = this.genReasoner.
				getParentContexts(norm.getPrecondition());

		/* Norm context generalisation */
		for(SetOfPredicatesWithTerms pPrecond : pPreconds) {
			NormModality mod = norm.getModality();
			AgentAction action = norm.getAction();

			Norm parent = new Norm(pPrecond, mod, action);
			List<Norm> children = new ArrayList<Norm>();
			List<SetOfPredicatesWithTerms> chPreconds = this.genReasoner.
					getChildContexts(pPrecond);

			for(SetOfPredicatesWithTerms chPrecond : chPreconds) {
				Norm chNorm = new Norm(chPrecond, mod, action);
				children.add(chNorm);
			}

			// Add new candidate generalisation
			PotentialGeneralisation cGen = new PotentialGeneralisation(
					parent, children);

			if(this.contains(cGen)) {
				cGen = this.get(cGen);
			}
			candidateGens.add(cGen);				
		}

		// Add candidate generalisations of the norm
		this.potentialGens.put(norm, candidateGens);
	}

	/**
	 * Returns a {@code List} of all the potential generalisations
	 * of a given {@code norm}
	 * 
	 * @param norm the norm
	 * @return a {@code List} of all the potential generalisations
	 * 					of a given {@code norm}
	 */
	public List<PotentialGeneralisation> get(Norm norm) {
		return this.potentialGens.get(norm);
	}

	/**
	 * Returns <tt>true</tt> if this data structure contains the given 
	 * potential generalisation {@code potGen}
	 * 
	 * @param potGen the potential generalisation
	 * @return <tt>true</tt> if this data structure contains the given 
	 *					potential generalisation {@code potGen}
	 */
	private boolean contains(PotentialGeneralisation potGen) {
		for(Norm norm : this.potentialGens.keySet()) {
			List<PotentialGeneralisation> candGens = this.potentialGens.get(norm);

			for(PotentialGeneralisation candGen : candGens) {
				if(potGen.equals(candGen)) {
					return true;
				}
			}	
		}
		return false;
	}

	/**
	 * Returns a {@code PotentialGeneralisation} that matches the other
	 * potential generalisation {@code otherPotGen}, if it is contained
	 * in this data structure
	 * 
	 * @param otherPotGen the potential generalisation to search for
	 * @return a {@code PotentialGeneralisation} that matches the other
	 * 					potential generalisation {@code otherPotGen}, if it is contained
	 * 					in this data structure
	 */
	private PotentialGeneralisation get(PotentialGeneralisation otherPotGen){
		for(Norm norm : this.potentialGens.keySet()) {
			List<PotentialGeneralisation> potGens = this.potentialGens.get(norm);

			for(PotentialGeneralisation potGen : potGens) {
				if(potGen.equals(otherPotGen)) {
					return potGen;
				}
			}	
		}
		return null;
	}

	/**
	 * 
	 */
	public int computeMetrics() {
		List<Norm> generalisationNodes = new ArrayList<Norm>();

//		for(List<CandidateGeneralisation> cGenList : this.candidateGens.values()) {
//			for(CandidateGeneralisation cGen : cGenList) {
//				
//				Norm p = cGen.getParent();
//				if(this.contains(generalisationNodes, p)) {
//					continue;
//				}
//
//				if(this.nNetwork.contains(p.getPrecondition(), p.getModality(),
//						p.getAction(), p.getGoal())) {
//					continue;
//				}
//
//				/* Add node */
//				generalisationNodes.add(p);
//				
//				for(Norm c : cGen.getChildren()) {
//					if(this.contains(generalisationNodes, c)) {
//						continue;
//					}
//
//					if(this.nNetwork.contains(c.getPrecondition(), c.getModality(),
//							c.getAction(), c.getGoal())) {
//						continue;
//					}
//
//					// Add node
//					generalisationNodes.add(c);
//				}
//			}
//		}
		return generalisationNodes.size();
	}

}