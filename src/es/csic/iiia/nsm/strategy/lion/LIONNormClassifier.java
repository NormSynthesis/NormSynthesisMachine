/**
 * 
 * @author Javier Morales (jmorales@iiia.csic.es)
 */
package es.csic.iiia.nsm.strategy.lion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.csic.iiia.nsm.config.Dimension;
import es.csic.iiia.nsm.config.Goal;
import es.csic.iiia.nsm.config.NormSynthesisSettings;
import es.csic.iiia.nsm.net.norm.NetworkNode;
import es.csic.iiia.nsm.net.norm.NetworkNodeState;
import es.csic.iiia.nsm.net.norm.NormativeNetwork;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.evaluation.NormCompliance;
import es.csic.iiia.nsm.norm.evaluation.PerformanceRange;
import es.csic.iiia.nsm.norm.evaluation.Utility;
import es.csic.iiia.nsm.norm.group.NormGroup;
import es.csic.iiia.nsm.norm.group.NormGroupCombination;
import es.csic.iiia.nsm.norm.group.net.NormGroupNetwork;
import es.csic.iiia.nsm.norm.refinement.lion.NormAttribute;
import es.csic.iiia.nsm.utilities.Minkowski;

/**
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 *
 */
public class LIONNormClassifier {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------

	protected List<Dimension> normEvDimensions;
	private NormSynthesisSettings nsmSettings;
	private NormativeNetwork normativeNetwork;
	private NormGroupNetwork normGroupNetwork;
	private Map<Norm,Norm> substitutableNorms;
	private Minkowski minkowski;

	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------

	public LIONNormClassifier(List<Dimension> normEvDimensions,
			NormSynthesisSettings nsmSettings, NormativeNetwork normativeNetwork,
			NormGroupNetwork normGroupNetwork, LIONOperators operators) {

		this.normEvDimensions = normEvDimensions;
		this.nsmSettings = nsmSettings;
		this.normativeNetwork = normativeNetwork;
		this.normGroupNetwork = normGroupNetwork;
		this.substitutableNorms = new HashMap<Norm,Norm>();
		this.minkowski = new Minkowski(2);
	}

	/**
	 * 
	 */
	public Map<Norm, List<NormAttribute>> step(List<Norm> norms) {
		Map<Norm,List<NormAttribute>> normClassifications;

		/* Classify norms by assigning attributes to them */ 
		normClassifications = this.classify(norms);

		return normClassifications;
	}

	/**
	 * Classifies the norms in the list received by parameter. Whenever it
	 * classifies a norm, it assigns a label to the norm in the normative network
	 * 
	 * @param norms the list of norms
	 */
	public Map<Norm,List<NormAttribute>> classify(List<Norm> norms) {
		Map<Norm,List<NormAttribute>> normClassifications = 
				new HashMap<Norm,List<NormAttribute>>();

		this.substitutableNorms.clear();

		/* Perform detection of ineffective, unnecessary and substitutable norms */
		for(Norm nA : norms) {
			//
			//			
			//			boolean isIneffective = normAttributes.contains(NormAttribute.Ineffective);
			//			boolean isGeneralisable = normAttributes.contains(NormAttribute.Generalisable);

			/* 1. Detect norms that are ineffective, unnecessary, generalisable and/or
			 * substitutable */
			//			if(!isIneffective) {
			this.checkEffectiveness(nA, normClassifications);
			//			}
			//			if(!isUnnecessary) {
			this.checkNecessity(nA, normClassifications);
			//			}
			//			if(!isGeneralisable) {
			this.checkGeneralisation(nA, normClassifications);
			//			}

			/* If the norm is not a leaf, then do not detect substitutability */
			if(!this.normativeNetwork.isLeaf(nA)) {
				continue;
			}

			/* 2. Detect substitutability/complementarity based on the norm
			 * group combinations that the norm has with other norms in
			 * the normative network */
			if(normGroupNetwork.hasNormGroupCombinations(nA)) {
				Map<Norm, NormGroupCombination> nGrCombs = 
						normGroupNetwork.getNormGroupCombinations(nA);

				for(Norm nB : nGrCombs.keySet()) {
					NormGroupCombination nABComb = nGrCombs.get(nB);
					
					if(normGroupNetwork.isActive(nABComb)) {
					
						/* Check substitutability */
						if(!normativeNetwork.areSubstitutable(nA, nB)) {
							this.checkSubstitutability(nA, nB, nABComb,
									normClassifications);
						}
					
						/* Check complementarity */
						if(!normativeNetwork.areComplementary(nA, nB)) {
							this.checkComplementarity(nA, nB, nABComb,
									normClassifications);		
						}
					}
				}
			}
		}
		return normClassifications;
	}

	/**
	 * 
	 * @param norm
	 * @param attribute
	 */
	private void assignAttribute(Norm norm, NormAttribute attribute,
			Map<Norm,List<NormAttribute>> normClassifications) {

		if(!normClassifications.containsKey(norm)) {
			normClassifications.put(norm, new ArrayList<NormAttribute>());
		}
		if(!normClassifications.get(norm).contains(attribute)) {
			normClassifications.get(norm).add(attribute);
		}

		/* Add attribute in the normative network */
		this.normativeNetwork.addAttribute(norm, attribute);

		/* Remove the dual attribute from the normative network */
		switch(attribute) 
		{
		case EFFECTIVE: 
			this.normativeNetwork.removeAttribute(norm, NormAttribute.INEFFECTIVE);
			break;

		case INEFFECTIVE:
			this.normativeNetwork.removeAttribute(norm, NormAttribute.EFFECTIVE);
			break;

		case NECESSARY:
			this.normativeNetwork.removeAttribute(norm, NormAttribute.UNNECESSARY);
			break;

		case UNNECESSARY:
			this.normativeNetwork.removeAttribute(norm, NormAttribute.NECESSARY);
			break;
			
		default:
			break;
		}
	}

	/**
	 * 
	 * @param norm
	 */
	private void checkEffectiveness(Norm norm,
			Map<Norm,List<NormAttribute>> normClassifications) {

		List<NormAttribute> normAttributes = 
				this.normativeNetwork.getAttributes(norm);		
		boolean isEffective =
				normAttributes.contains(NormAttribute.NECESSARY);

		for(Goal goal : nsmSettings.getSystemGoals()) {
			if(isEffective && 
					this.underPerforms(norm,	Dimension.Effectiveness, goal))
			{
				this.assignAttribute(norm, NormAttribute.INEFFECTIVE,
						normClassifications);
			}
			else if(!isEffective && 
					this.performsWell(norm,	Dimension.Effectiveness, goal))
			{
				this.assignAttribute(norm, NormAttribute.EFFECTIVE,
						normClassifications);
			}
		}
	}

	/**
	 * 
	 * @param norm
	 */
	private void checkNecessity(Norm norm,
			Map<Norm,List<NormAttribute>> normClassifications) {

		List<NormAttribute> normAttributes = 
				this.normativeNetwork.getAttributes(norm);		
		boolean isNecessary =
				normAttributes.contains(NormAttribute.NECESSARY);

		for(Goal goal : nsmSettings.getSystemGoals()) {
			if(isNecessary && 
					this.underPerforms(norm, Dimension.Necessity, goal)) 
			{
				this.assignAttribute(norm, NormAttribute.UNNECESSARY,
						normClassifications);
			}
			else if(!isNecessary && 
					this.performsWell(norm,	Dimension.Necessity, goal)) 
			{
				this.assignAttribute(norm, NormAttribute.NECESSARY,
						normClassifications);
			}
		}
	}

	/**
	 * 
	 * @param norm
	 */
	private void checkGeneralisation(Norm norm,
			Map<Norm,List<NormAttribute>> normClassifications) {

		List<NormAttribute> normAttributes = 
				this.normativeNetwork.getAttributes(norm);
		boolean isGeneralisable =
				normAttributes.contains(NormAttribute.GENERALISABLE);

		if(!isGeneralisable) {
			for(Dimension dim : this.normEvDimensions)	 {
				for(Goal goal : this.nsmSettings.getSystemGoals()) {

					Utility utility = this.normativeNetwork.getUtility(norm);
					//					float bottomBoundary = utility.
					//							getPerformanceRange(dim, goal).getCurrentBottomBoundary();
					float topBoundary = utility.
							getPerformanceRange(dim, goal).getCurrentTopBoundary();
					float satDegree = this.nsmSettings.
							getGeneralisationBoundary(dim, goal);

					if(topBoundary < satDegree) {
						return;
					}
				}
			}
			this.assignAttribute(norm, NormAttribute.GENERALISABLE,
					normClassifications);
		}
	}

	/**
	 * 
	 * @param nA
	 * @param nB
	 */
	private void checkComplementarity(Norm nA, Norm nB,
			NormGroupCombination nABComb,
			Map<Norm,List<NormAttribute>> normClassifications) {

		if(!nABComb.containsAllCombinations()) {
			return;
		}

		NormGroup groupFF = nABComb.get(NormCompliance.FULFILMENT,
				NormCompliance.FULFILMENT);
		NormGroup groupFI = nABComb.get(NormCompliance.FULFILMENT,
				NormCompliance.INFRINGEMENT);
		NormGroup groupIF = nABComb.get(NormCompliance.INFRINGEMENT,
				NormCompliance.FULFILMENT);
		NormGroup groupII = nABComb.get(NormCompliance.INFRINGEMENT,
				NormCompliance.INFRINGEMENT);
		
		for(Goal goal : nsmSettings.getSystemGoals()) {

			boolean groupFFGreaterThanFI = this.isGreater(groupFF, groupFI, goal);
			boolean groupFFGreaterThanIF = this.isGreater(groupFF, groupIF, goal);

			/* Conditions to be substitutable*/
			if(groupFFGreaterThanFI && groupFFGreaterThanIF) {
				this.assignAttribute(nA, NormAttribute.COMPLEMENTARY,
						normClassifications);
				this.assignAttribute(nB, NormAttribute.COMPLEMENTARY,
						normClassifications);

				/* Add complementariness relationship */
				this.normativeNetwork.addComplementarity(nA, nB);
				
				System.out.println("Complementarity detected " + nA);
				System.out.println("Complementarity detected " + nB);
				System.out.println("-----------------------------------------------------------------");
				
				/* Deactivate norm group to detect complementarity */
				this.normGroupNetwork.setState(groupFF, NetworkNodeState.INACTIVE);
				this.normGroupNetwork.setState(groupFI, NetworkNodeState.INACTIVE);
				this.normGroupNetwork.setState(groupIF, NetworkNodeState.INACTIVE);
				this.normGroupNetwork.setState(groupII, NetworkNodeState.INACTIVE);
			}
		}
	}
	
	/**
	 * 
	 * @param nA
	 * @param nB
	 */
	private void checkSubstitutability(Norm nA, Norm nB, 
			NormGroupCombination nABComb,
			Map<Norm,List<NormAttribute>> normClassifications) {

		if(!nABComb.containsAllCombinations()) {
			return;
		}

		NormGroup groupFF = nABComb.get(NormCompliance.FULFILMENT,
				NormCompliance.FULFILMENT);
		NormGroup groupFI = nABComb.get(NormCompliance.FULFILMENT,
				NormCompliance.INFRINGEMENT);
		NormGroup groupIF = nABComb.get(NormCompliance.INFRINGEMENT,
				NormCompliance.FULFILMENT);

		for(Goal goal : nsmSettings.getSystemGoals()) {

			boolean groupFISimilarToFF = this.areSimilar(groupFI, groupFF, goal);
			boolean groupIFSimilarToFF = this.areSimilar(groupIF, groupFF, goal);
			boolean groupFFDoesNotUnderPerform =
					!this.underPerforms(groupFF, Dimension.Effectiveness, goal);

			/* Conditions to be substitutable*/
			if(groupFFDoesNotUnderPerform && 
					groupFISimilarToFF &&
					groupIFSimilarToFF) {

				String nAPrecond = nA.getPrecondition().toString();
				String nBPrecond = nB.getPrecondition().toString();

				System.out.println("substitutability detected " + nA);
				System.out.println("substitutability detected " + nB);
				System.out.println("-----------------------------------------------------------------");
				
				if(nAPrecond.equals("l(>)&f(>)&r(>)") || nAPrecond.equals("l(<)&f(<)&r(<)")) {
					System.out.println("Substitutability False positive" + nAPrecond);
				}
				if(nBPrecond.equals("l(>)&f(>)&r(>)") || nBPrecond.equals("l(<)&f(<)&r(<)")) {
					System.out.println("Substitutability False positive" + nBPrecond);
				}

				/* Add substitutability attributes */
				this.assignAttribute(nA, NormAttribute.SUBSTITUTABLE,
						normClassifications);
				this.assignAttribute(nB, NormAttribute.SUBSTITUTABLE,
						normClassifications);

				/* Add substitutability relationship */
				this.normativeNetwork.addSubstitutability(nA, nB);

				/* Tag them as substitutable to refine */ 
				this.substitutableNorms.put(nA, nB);
				this.substitutableNorms.put(nB, nA);
			}
		}
	}

	/**
	 * 
	 * @return
	 */
	public Norm getSubstitutableNorm(Norm nA) {
		return substitutableNorms.get(nA);
	}

	//---------------------------------------------------------------------------
	// Private methods 
	//---------------------------------------------------------------------------

	/**
	 * Returns <tt>true</tt> if the node (whether it is a norm or a norm group)
	 * under performs with respect to a certain dimension (effectiveness or 
	 * necessity), and in terms of all system goals
	 * 
	 * @param node the node (norm or norm group)
	 * @param dim the evaluation dimension
	 * @return <tt>true</tt> if the node (whether it is a norm or a norm group)
	 * 					under performs with respect to a certain dimension (effectiveness
	 * 					or necessity), and in terms of all system goals
	 */
	private boolean underPerforms(NetworkNode node, Dimension dim, Goal goal) {
		Utility utility = null;
		if(node instanceof Norm) {
			utility =  this.normativeNetwork.getUtility((Norm)node);
		}
		else if(node instanceof NormGroup) {
			utility =  this.normGroupNetwork.getUtility((NormGroup)node);
		}

		float satDegree = this.nsmSettings.getSpecialisationBoundary(dim, goal);
		float epsilon = this.nsmSettings.getSpecialisationBoundaryEpsilon(dim, goal);
		//		float topBoundary = utility.getPerformanceRange(dim, goal).
		//				getCurrentTopBoundary();
		float avg = utility.getPerformanceRange(dim, goal).getCurrentAverage();

		//		if(utility.getPerformanceRange(dim, goal).getNumSlidingValues() < 20)
		//			return false;

		/* The norm under performs */
		//		if(topBoundary < satDegree) {
		if(avg <= Math.max(0, (satDegree - epsilon))) {
			return true;
		}
		return false;
	}

	/**
	 * Returns <tt>true</tt> if the node (whether it is a norm or a norm group)
	 * under performs with respect to a certain dimension (effectiveness or 
	 * necessity), and in terms of all system goals
	 * 
	 * @param node the node (norm or norm group)
	 * @param dim the evaluation dimension
	 * @return <tt>true</tt> if the node (whether it is a norm or a norm group)
	 * 					under performs with respect to a certain dimension (effectiveness
	 * 					or necessity), and in terms of all system goals
	 */
	private boolean performsWell(NetworkNode node, Dimension dim, Goal goal) {
		Utility utility = null;
		if(node instanceof Norm) {
			utility =  this.normativeNetwork.getUtility((Norm)node);
		}
		else if(node instanceof NormGroup) {
			utility =  this.normGroupNetwork.getUtility((NormGroup)node);
		}

		float satDegree = this.nsmSettings.getSpecialisationBoundary(dim, goal);
		float epsilon = this.nsmSettings.getSpecialisationBoundaryEpsilon(dim, goal);

		//		float bottomBoundary = utility.getPerformanceRange(dim, goal).
		//				getCurrentBottomBoundary();
		float avg = utility.getPerformanceRange(dim, goal).getCurrentAverage();

		//		if(utility.getPerformanceRange(dim, goal).getPunctualValues().size() < 20)
		//			return false;

		/* The norm performs well */
		//		if(bottomBoundary > satDegree) {
		if(avg >= (satDegree + epsilon)) {
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @param group1
	 * @param group2
	 * @param goal
	 * @return
	 */
	private boolean isGreater(NormGroup group1, NormGroup group2, Goal goal) {
		double distance = 1;
		
		PerformanceRange perfRangeGroup1 = this.normGroupNetwork.
				getUtility(group1).getPerformanceRange(Dimension.Effectiveness, goal);
		PerformanceRange perfRangeGroup2 = this.normGroupNetwork.
				getUtility(group2).getPerformanceRange(Dimension.Effectiveness, goal);

		List<Float> avg1 = perfRangeGroup1.getAverage();
		List<Float> avg2 = perfRangeGroup2.getAverage();
		List<Float> topBndr1 = perfRangeGroup1.getTopBoundary();
		List<Float> topBndr2 = perfRangeGroup2.getTopBoundary();
		List<Float> bottomBndr1 = perfRangeGroup1.getBottomBoundary();
		List<Float> bottomBndr2 = perfRangeGroup2.getBottomBoundary();

		List<List<Float>> seriesGroup1 = new ArrayList<List<Float>>();
		List<List<Float>> seriesGroup2 = new ArrayList<List<Float>>();
		seriesGroup1.add(avg1);
		seriesGroup1.add(topBndr1);
		seriesGroup1.add(bottomBndr1);
		seriesGroup2.add(avg2);
		seriesGroup2.add(topBndr2);
		seriesGroup2.add(bottomBndr2);
		
		for(int i=0; i<seriesGroup1.size(); i++) {
			List<Float> s1 = seriesGroup1.get(i);
			List<Float> s2 = seriesGroup2.get(i);

			int numValues = s1.size();
			if(numValues > s2.size()) {
				numValues = s2.size();
			}

			/* Minimum number of values to evaluate similarity */ 
			if(numValues < 20) {
				return false;
			}

			try {
				distance = this.minkowski.distance(s1, s2, numValues);
			} 
			catch(IllegalArgumentException e) {
				return false;
			}

			float s1Avg = this.average(s1);
			float s2Avg = this.average(s2);
			
			if(distance < 1.2 || s1Avg <= s2Avg) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 
	 * @param group1
	 * @param group2
	 * @param goal
	 * @return
	 */
	private boolean areSimilar(NormGroup group1, NormGroup group2, Goal goal) {
		double distance = 1;

		PerformanceRange perfRangeGroup1 = this.normGroupNetwork.
				getUtility(group1).getPerformanceRange(Dimension.Effectiveness, goal);
		PerformanceRange perfRangeGroup2 = this.normGroupNetwork.
				getUtility(group2).getPerformanceRange(Dimension.Effectiveness, goal);

		List<Float> avg1 = perfRangeGroup1.getAverage();
		List<Float> avg2 = perfRangeGroup2.getAverage();
		List<Float> topBndr1 = perfRangeGroup1.getTopBoundary();
		List<Float> topBndr2 = perfRangeGroup2.getTopBoundary();
		List<Float> bottomBndr1 = perfRangeGroup1.getBottomBoundary();
		List<Float> bottomBndr2 = perfRangeGroup2.getBottomBoundary();

		List<List<Float>> seriesGroup1 = new ArrayList<List<Float>>();
		List<List<Float>> seriesGroup2 = new ArrayList<List<Float>>();
		seriesGroup1.add(avg1);
		seriesGroup1.add(topBndr1);
		seriesGroup1.add(bottomBndr1);
		seriesGroup2.add(avg2);
		seriesGroup2.add(topBndr2);
		seriesGroup2.add(bottomBndr2);

		for(int i=0; i<seriesGroup1.size(); i++) {
			List<Float> s1 = seriesGroup1.get(i);
			List<Float> s2 = seriesGroup2.get(i);

			int numValues = s1.size();
			if(numValues > s2.size()) {
				numValues = s2.size();
			}

			/* Minimum number of values to evaluate similarity */ 
			if(numValues < 20) {
				return false;
			}

			try {
				distance = this.minkowski.distance(s1, s2, numValues);
			} 
			catch(IllegalArgumentException e) {
				return false;
			}

			if(distance > 1.2) {
				return false;
			}
		}
		return true;
	}
	//
	///**
	//* Returns the norms in a list of {@code norms} that are leaves
	//* in the normative network. That is, norms that do not generalise
	//* any other norm, and hence have generalisation level 0
	//* 
	//* @param norms the list of norms
	//* @return the norms in a list of {@code norms} that are leaves
	//* 					in the normative network. That is, norms that do not generalise
	//* 					any other norm, and hence have generalisation level 0
	//*/
	//protected List<Norm> extractLeafNorms(List<Norm> norms) {
	//List<Norm> leaves = new ArrayList<Norm>();
	//
	///* Extract norms that are leaves in the normative network */
	//for(Norm norm : norms) {
	//	if(this.normativeNetwork.isLeaf(norm)) {
	//		leaves.add(norm);
	//	}
	//}
	//return leaves;
	//}
	
	/**
	 * 
	 * @param floats
	 * @return
	 */
	private float average(List<Float> floats) {
		float avg = 0f;
		for(Float num : floats) {
			avg += num;
		}		
		return avg/floats.size();
	}
}
