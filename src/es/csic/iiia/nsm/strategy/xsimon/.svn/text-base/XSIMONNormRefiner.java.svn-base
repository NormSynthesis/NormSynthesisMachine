/**
 * 
 * @author Javier Morales (jmorales@iiia.csic.es)
 */
package es.csic.iiia.nsm.strategy.xsimon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import es.csic.iiia.nsm.NormSynthesisMachine;
import es.csic.iiia.nsm.NormSynthesisMachine.NormGeneralisationMode;
import es.csic.iiia.nsm.agent.AgentAction;
import es.csic.iiia.nsm.agent.language.PredicatesDomains;
import es.csic.iiia.nsm.config.DomainFunctions;
import es.csic.iiia.nsm.net.norm.NormativeNetwork;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.NormModality;
import es.csic.iiia.nsm.norm.NormativeSystem;
import es.csic.iiia.nsm.norm.reasoning.NormReasoner;
import es.csic.iiia.nsm.norm.refinement.simon.GeneralisableNorms;
import es.csic.iiia.nsm.norm.refinement.simon.NormIntersection;
import es.csic.iiia.nsm.norm.refinement.xsimon.NormAttribute;

/**
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 *
 */
public class XSIMONNormRefiner {

	protected XSIMONNormClassifier normClassifier;
	protected DomainFunctions dmFunctions;
	protected PredicatesDomains predDomains;
	protected NormReasoner normReasoner;
	protected NormativeNetwork normativeNetwork;
	
	protected XSIMONUtilityFunction utilityFunction;	
	protected XSIMONOperators operators;
	protected NormGeneralisationMode genMode; 
	protected int genStep;
	
	protected Map<String, NormIntersection> normIntersections;
	
	/**
	 * 
	 * @param normativeNetwork
	 */
	public XSIMONNormRefiner(XSIMONNormClassifier normClassifier,
			NormativeNetwork normativeNetwork, 
			DomainFunctions dmFunctions, PredicatesDomains predDomains,
			NormReasoner normReasoner, XSIMONOperators operators, 
			NormGeneralisationMode genMode, int genStep) {
	
		this.normClassifier = normClassifier;
		this.normativeNetwork = normativeNetwork;
		this.dmFunctions = dmFunctions;
		this.predDomains = predDomains;
		this.normReasoner = normReasoner;
		this.operators = operators;
		this.genMode = genMode;
		this.genStep = genStep;
		
		this.normIntersections= new HashMap<String, NormIntersection>();
	}
	
	/**
	 * 
	 * @param normClassifications
	 */
	public void step(Map<Norm, List<NormAttribute>> normClassifications) {
		List<Norm> processed = new ArrayList<Norm>();
		
		for(Norm norm : normClassifications.keySet()) {
			if(processed.contains(norm)) {
				continue;
			}
			List<NormAttribute> attributes = normClassifications.get(norm);

			for(NormAttribute attribute : attributes) {

				switch(attribute) {

				/* If the norm is whether ineffective or unnecessary, then deactivate
				 * it (specialise it into its children) */
				case Ineffective:
				case Unnecessary:

					specialiseDown(norm);
					break;

					/* If the norm has enough utility to be generalised, 
					 * then try to generalise it */
				case Generalisable:

					generaliseUp(norm, genMode, genStep);
					break;

				case Substitutable:
				
					Norm norm2 = this.normClassifier.getSubstitutableNorm(norm);
					Norm normToSpecialise = this.chooseSubstitutableNormToSpecialise(norm, norm2);
					
					specialiseDown(normToSpecialise);
					
					processed.add(norm);
					processed.add(norm2);
					
					break;
					
				case Complementary:
				case Exclussive:
				default:
					break; 
				}
			}
		}
	}
	
	/**
	 * Recursively specialises a norm into its children, its children
	 * into their children, and so on
	 * 
	 * @param norm the norm to specialise
	 */
	protected void specialiseDown(Norm norm) {
		List<Norm> children = this.normativeNetwork.getChildren(norm);

		/* Only specialise norms that are represented by
		 * an active norm, whether itself or a parent norm */
		if(this.normativeNetwork.isRepresented(norm)) {

			/* Specialise down all parent norms */
			List<Norm> parents = this.normativeNetwork.getParents(norm);
			for(Norm parent : parents) {
				this.specialiseDown(parent);
			}

			/* If the norm has no children, we simply deactivate it */
			if(this.normativeNetwork.isLeaf(norm)) {
				operators.deactivate(norm);
			}
			/* If the norm has children, specialise into all of them */
			else {			
				operators.specialise(norm, children);
			}

			Map<Norm, List<NormAttribute>> classified = 
					this.normClassifier.step(children);
			
			this.step(classified);
		}
	}

	/**
	 * Tries to generalise up a norm {@code normA} together with other norms in
	 * the normative system. Notice that, unlike in the case of IRON, SIMON does
	 * not compare a norm with all the other norms in the normative network, but
	 * only to those of the normative system. The SIMON norm generalisation has
	 * two working modes:
	 * <ol>
	 * <li> <b>shallow</b> generalisation, which performs norm generalisations
	 * 			by comparing the preconditions of norms in a similar way to that of
	 * 			IRON; and 
	 * <li> <b>deep</b> generalisation, which performs a deeper search by 
	 * 			computing the <b>intersection</b> of terms in the predicates of
	 * 			norms' preconditions.
	 * </ol>
	 * 
	 * Furthermore, the SIMON generalisation requires a generalisation step
	 * {@code genStep}, which describes how many predicates/terms can be
	 * generalised at the same time. As an example, consider two norms with 3
	 * predicates each one of them, and each predicate with 1 term. Consider now
	 * that 2 of the 3 terms in both norms intersect, but the third predicate in
	 * both norms are different and have a common subsumer term (a potential
	 * generalisation). If the generalisation step is genStep=1, then we may
	 * generalise these two norms, since it allows to generalise 1 predicate/term
	 * at the same time. However, if 1 of the 3 terms intersect, and hence 2
	 * terms are different, then we cannot generalise them, since the
	 * generalisation step {@code genStep} does not allow to generalise two
	 * predicates/terms at the same time.
	 * 
	 * @param normA the norm to generalise
	 * @param genMode the SIMON generalisation mode (Shallow/Deep)
	 * @param genStep the generalisation step
	 */
	protected void generaliseUp(Norm normA,
			NormGeneralisationMode genMode, int genStep) {

		/* Get all the active norms (the normative system) */
		NormativeSystem NS = (NormativeSystem)
				normativeNetwork.getNormativeSystem().clone();

		/* Compute matches with each norm */
		for(Norm normB : NS) {
			boolean generalise = true;

			/* Never generalise the norm with itself */
			if(normA.equals(normB)) {
				continue;
			}

			/* 1. Get generalisable norms */
			GeneralisableNorms genNorms = this.
					getGeneralisableNorms(normA, normB, genStep);

			/* If there is no possible generalisation, do nothing */
			if(genNorms == null) {
				continue;
			}

			/* 2. If there is a possible generalisation, then get generalisable
			 * norms and the parent norm */
			Norm n1 = genNorms.getNormA();
			Norm n2 = genNorms.getNormB();
			Norm parent = genNorms.getParent();

			if(n1 == null || n2 == null || parent == null) {
				continue;
			}

			/* 3. Check that the parent norm does not contain an inactive norm */
			for(Norm inactive: this.normativeNetwork.getNotRepresentedNorms()){
				if(this.normReasoner.satisfies(inactive, parent))	{
					generalise = false;
					break;
				}
			}

			/* 4. Do not generalise if any of the generalisable norms or the parent
			 * norm are not consistent with the given domain they have been 
			 * generated for */
			for(Norm norm : genNorms.getAllNorms()) {
				if(!this.dmFunctions.isConsistent(norm.getPrecondition())) {
					generalise = false;
					break;
				}
			}

			/* 5. Perform the norm generalisation if all the previous tests
			 * have been passed */
			if(generalise) {
				if(this.normativeNetwork.contains(n1)) {
					n1 = this.normativeNetwork.getNorm(n1.getPrecondition(),
							n1.getModality(), n1.getAction(), n1.getGoal());
				}
				else {
					this.operators.add(n1);
				}
				if(this.normativeNetwork.contains(n2)) {
					n2 = this.normativeNetwork.getNorm(n2.getPrecondition(),
							n2.getModality(), n2.getAction(), n2.getGoal());
				}
				else {
					this.operators.add(n2);
				}
				if(this.normativeNetwork.contains(parent)) {
					parent = this.normativeNetwork.getNorm(parent.getPrecondition(),
							parent.getModality(), parent.getAction(), parent.getGoal());
				}
				else {
					this.operators.add(parent);
				}

				this.operators.generalise(n1, parent);
				this.operators.generalise(n2, parent);
			}
		}
	}

	/**
	 * Given two norms Returns the generalisable norms {@code normA} and
	 * {@code normB}, it performs the following operations:
	 * <ol>
	 * <li> it computes the intersection between norms A and B;
	 * <li> if the predicates of both norms are equal but K={@code genStep}
	 * 			predicates, then it computes their generalisable norms. Otherwise,
	 * 			it return a <tt>null</tt> {@code GeneralisableNorms}
	 * </ol> 
	 * 
	 * @param normA the first norm 
	 * @param normB the second norm
	 * @param genStep the generalisation step
	 * @return the generalisable norms A and B, and their potential parent
	 */
	protected GeneralisableNorms getGeneralisableNorms(
			Norm normA, Norm normB, int genStep) {

		NormModality normModality = normA.getModality();
		NormModality otherNormModality = normB.getModality();
		AgentAction action = normA.getAction();
		AgentAction otherAction = normB.getAction();
		GeneralisableNorms genNorms = null;
		NormIntersection intersection;

		/* Check that post-conditions are the same*/
		if(normModality != otherNormModality || action != otherAction) {
			return genNorms;
		}

		/* Get the intersection between both norm preconditions */
		String desc = NormIntersection.getDescription(normA, normB);

		if(!this.normIntersections.containsKey(desc)) {
			intersection = new NormIntersection(normA, normB, 
					this.predDomains, genMode);
			this.normIntersections.put(desc, intersection);
		}
		intersection = this.normIntersections.get(desc);

		/* If both norms have all their predicates in common but K predicates,
		 * then generalise both norms are generalisable */
		if(intersection.getDifferenceCardinality() > 0 && 
				intersection.getDifferenceCardinality() <= genStep) {

			genNorms = new GeneralisableNorms(intersection,
					this.predDomains, genMode);
		}		
		return genNorms;
	}
	
	/**
	 * 
	 * @param nA
	 * @param nB
	 */
	protected Norm chooseSubstitutableNormToSpecialise(Norm nA, Norm nB) {
		
		/* Substitutable brothers strategy */
		List<Norm> nABrothers = this.normativeNetwork.getBrothers(nA);
		List<Norm> nBBrothers = this.normativeNetwork.getBrothers(nB);
		
		for(Norm nABrother : nABrothers) {
			for(Norm nBBrother : nBBrothers) {
				boolean brothersAreSubstitutable = 
						this.normativeNetwork.areSubstitutable(nABrother, nBBrother);
				
				if(brothersAreSubstitutable) {
					boolean nABrotherInactive = !this.normativeNetwork.isRepresented(nABrother);
					boolean nBBrotherInactive = !this.normativeNetwork.isRepresented(nBBrother);
					
					/* If the nA's brother was deactivated because of substitutability, 
					 * then deactivate nA (for coherence in the normative network) */
					if(nABrotherInactive && !nBBrotherInactive) {
						return nA;
					}
					
					/* Otherwise, if the nB's brother was deactivated because of
					 * substitutability, then deactivate nB */
					if(!nABrotherInactive && nBBrotherInactive) {
						return nB;
					}
				}
			}	
		}
		
		/* Random strategy */
		Random rnd = NormSynthesisMachine.getRandom();
		
		if(rnd.nextBoolean()) {
			return nA;
		}
		return nB;
	}
}
