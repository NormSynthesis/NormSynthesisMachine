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
import es.csic.iiia.nsm.metrics.NormSynthesisMetrics;
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
	private NormSynthesisMetrics nsMetrics;
	
	private Map<Norm,Norm> substitutableNorms;
	private Minkowski minkowski;

	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------

	public LIONNormClassifier(List<Dimension> normEvDimensions,
			NormSynthesisSettings nsmSettings, NormativeNetwork normativeNetwork,
			NormGroupNetwork normGroupNetwork, LIONOperators operators,
			NormSynthesisMetrics nsMetrics) {

		this.nsMetrics = nsMetrics;
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

			/* 1. Detect norms that are ineffective, unnecessary, generalisable and/or
			 * substitutable */
			this.checkEffectiveness(nA, normClassifications);
			this.checkNecessity(nA, normClassifications);

			/* Only check for generalisation active norms (which, in fact, will be
			 * the ones that may be generalised at the end) */
			if(this.normativeNetwork.getState(nA) == NetworkNodeState.ACTIVE) {
				this.checkGeneralisation(nA, normClassifications);
			}

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
	
//			/* Update complexities metrics */
//			this.nsMetrics.incNumNodesVisited();
		}
		return normClassifications;
	}


	/**
	 * 
	 * @param norm
	 */
	private void checkEffectiveness(Norm norm,
			Map<Norm,List<NormAttribute>> normClassifications) {

		/* General norms are not classified as ineffective */
		if(!this.normativeNetwork.isLeaf(norm)) {
			return;
		}

		List<NormAttribute> attributes = this.normativeNetwork.getAttributes(norm);
		boolean isClassifiedAsEffective = attributes.contains(NormAttribute.EFFECTIVE);
		boolean isClassifiedAsIneffective = attributes.contains(NormAttribute.INEFFECTIVE);
		boolean hasNotBeenClassified = !isClassifiedAsEffective && !isClassifiedAsIneffective;
		
		for(Goal goal : nsmSettings.getSystemGoals()) {

			/* The norm is classified as effective (or even it has not been
			 * classified as ineffective yet) and now it under performs
			 * -> classify the norm as ineffective */
			if(isClassifiedAsEffective || hasNotBeenClassified) 
			{
				if(this.shouldBeDeactivated(norm,Dimension.Effectiveness, goal)) {
					this.assignAttribute(norm, NormAttribute.INEFFECTIVE,
							normClassifications);
				}
			}

			/* The norm is classified as ineffective (or even it has not been
			 * classified as effective yet) and now it performs well
			 * -> classify the norm as effective */
			if(isClassifiedAsIneffective || hasNotBeenClassified) 
			{
				if(this.shouldBeActivated(norm,	Dimension.Effectiveness, goal)) {
					this.assignAttribute(norm, NormAttribute.EFFECTIVE,
							normClassifications);
				}
			}
		}
	}

	/**
	 * 
	 * @param norm
	 */
	private void checkNecessity(Norm norm,
			Map<Norm,List<NormAttribute>> normClassifications) {

		/* General norms are not classified as unnecessary */
		if(!this.normativeNetwork.isLeaf(norm)) {
			return;
		}

		List<NormAttribute> attributes = this.normativeNetwork.getAttributes(norm);
		boolean isClassifiedAsNecessary = attributes.contains(NormAttribute.NECESSARY);
		boolean isClassifiedAsUnnecessary = attributes.contains(NormAttribute.UNNECESSARY);
		boolean hasNotBeenClassified = !isClassifiedAsNecessary && !isClassifiedAsUnnecessary;
		
		for(Goal goal : nsmSettings.getSystemGoals()) {

			/* The norm is classified as necessary (or even it has not been
			 * classified as unnecessary yet) and now it under performs
			 * -> classify the norm as unnecessary */
			if(isClassifiedAsNecessary || hasNotBeenClassified) 
			{
				if(this.shouldBeDeactivated(norm,Dimension.Necessity, goal)) {
					this.assignAttribute(norm, NormAttribute.UNNECESSARY,
							normClassifications);
				}
			}

			/* The norm is classified as unnecessary (or even it has not been
			 * classified as necessary yet) and now it performs well
			 * -> classify the norm as necessary */
			if(isClassifiedAsUnnecessary || hasNotBeenClassified)
			{
				if(this.shouldBeActivated(norm, Dimension.Necessity, goal)) {
					this.assignAttribute(norm, NormAttribute.NECESSARY,
							normClassifications);
				}
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
				for(Goal goal : this.nsmSettings.getSystemGoals()) 
				{
					Utility utility = this.normativeNetwork.getUtility(norm);
					float topBoundary = (float)utility.
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

				/* Deactivate norm group to detect complementarity */
				this.normGroupNetwork.setState(groupFF, NetworkNodeState.INACTIVE);
				this.normGroupNetwork.setState(groupFI, NetworkNodeState.INACTIVE);
				this.normGroupNetwork.setState(groupIF, NetworkNodeState.INACTIVE);
				this.normGroupNetwork.setState(groupII, NetworkNodeState.INACTIVE);

				System.out.println("Complementarity detected " + nA);
				System.out.println("Complementarity detected " + nB);
				System.out.println("------------------------------------------------");
				
				/* Update complexities metrics */
				this.nsMetrics.incNumNodesVisited();
				this.nsMetrics.incNumNodesVisited();
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
					!this.shouldBeDeactivated(groupFF, Dimension.Effectiveness, goal);

			/* Conditions to be substitutable*/
			if(groupFFDoesNotUnderPerform && groupFISimilarToFF && groupIFSimilarToFF) {

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

				System.out.println("Substitutability detected " + nA);
				System.out.println("Substitutability detected " + nB);
				System.out.println("------------------------------------------------");
				
				/* Update complexities metrics */
				this.nsMetrics.incNumNodesVisited();
				this.nsMetrics.incNumNodesVisited();
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
	private boolean shouldBeDeactivated(NetworkNode node, Dimension dim, Goal goal) {
		Utility utility = null;
		if(node instanceof Norm) {
			utility =  this.normativeNetwork.getUtility((Norm)node);
		}
		else if(node instanceof NormGroup) {
			utility =  this.normGroupNetwork.getUtility((NormGroup)node);
		}

		float satDegree = this.nsmSettings.getSpecialisationBoundary(dim, goal);
		float epsilon = this.nsmSettings.getSpecialisationBoundaryEpsilon(dim, goal);
		float avg = (float)utility.getPerformanceRange(dim, goal).getCurrentAverage();

		int minNumEvalsToClassify = this.nsmSettings.getMinEvaluationsToClassifyNorms();
		int numValues = utility.getPerformanceRange(dim, goal).getNumPunctualValues();

		/* The norm under performs */
		if(numValues > minNumEvalsToClassify) {
			if(avg <= Math.max(0, (satDegree - epsilon))) {
				return true;
			}
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
	private boolean shouldBeActivated(NetworkNode node, Dimension dim, Goal goal) {
		Utility utility = null;
		if(node instanceof Norm) {
			utility =  this.normativeNetwork.getUtility((Norm)node);
		}
		else if(node instanceof NormGroup) {
			utility =  this.normGroupNetwork.getUtility((NormGroup)node);
		}

		float satDegree = this.nsmSettings.getSpecialisationBoundary(dim, goal);
		float epsilon = this.nsmSettings.getSpecialisationBoundaryEpsilon(dim, goal);
		float avg = (float)utility.getPerformanceRange(dim, goal).getCurrentAverage();

		int minNumEvalsToClassify = this.nsmSettings.getMinEvaluationsToClassifyNorms();
		int numValues = utility.getPerformanceRange(dim, goal).getNumPunctualValues();

		/* The norm performs well */
		if(numValues > minNumEvalsToClassify) {
			if(avg >= (satDegree + epsilon)) {
				return true;
			}
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
		int minNumEvalsToClassify =
				this.nsmSettings.getMinEvaluationsToClassifyNormGroups();
		
		List<Double> avg1 = perfRangeGroup1.getAverage();
		List<Double> avg2 = perfRangeGroup2.getAverage();
		List<Double> topBndr1 = perfRangeGroup1.getTopBoundary();
		List<Double> topBndr2 = perfRangeGroup2.getTopBoundary();
		List<Double> bottomBndr1 = perfRangeGroup1.getBottomBoundary();
		List<Double> bottomBndr2 = perfRangeGroup2.getBottomBoundary();

		List<List<Double>> seriesGroup1 = new ArrayList<List<Double>>();
		List<List<Double>> seriesGroup2 = new ArrayList<List<Double>>();
		seriesGroup1.add(avg1);
		seriesGroup1.add(topBndr1);
		seriesGroup1.add(bottomBndr1);
		seriesGroup2.add(avg2);
		seriesGroup2.add(topBndr2);
		seriesGroup2.add(bottomBndr2);

		for(int i=0; i<seriesGroup1.size(); i++) {
			List<Double> s1 = seriesGroup1.get(i);
			List<Double> s2 = seriesGroup2.get(i);

			int numValues = s1.size();
			if(numValues > s2.size()) {
				numValues = s2.size();
			}

			/* Minimum number of values to evaluate similarity */ 
			if(numValues < minNumEvalsToClassify) {
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
		int minNumEvalsToClassify =
				this.nsmSettings.getMinEvaluationsToClassifyNormGroups();
		
		List<Double> avg1 = perfRangeGroup1.getAverage();
		List<Double> avg2 = perfRangeGroup2.getAverage();
		List<Double> topBndr1 = perfRangeGroup1.getTopBoundary();
		List<Double> topBndr2 = perfRangeGroup2.getTopBoundary();
		List<Double> bottomBndr1 = perfRangeGroup1.getBottomBoundary();
		List<Double> bottomBndr2 = perfRangeGroup2.getBottomBoundary();

		List<List<Double>> seriesGroup1 = new ArrayList<List<Double>>();
		List<List<Double>> seriesGroup2 = new ArrayList<List<Double>>();
		seriesGroup1.add(avg1);
		seriesGroup1.add(topBndr1);
		seriesGroup1.add(bottomBndr1);
		seriesGroup2.add(avg2);
		seriesGroup2.add(topBndr2);
		seriesGroup2.add(bottomBndr2);

		for(int i=0; i<seriesGroup1.size(); i++) {
			List<Double> s1 = seriesGroup1.get(i);
			List<Double> s2 = seriesGroup2.get(i);

			int numValues = s1.size();
			if(numValues > s2.size()) {
				numValues = s2.size();
			}

			/* Minimum number of values to evaluate similarity */ 
			if(numValues < minNumEvalsToClassify) {
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

	/**
	 * 
	 * @param floats
	 * @return
	 */
	private float average(List<Double> doubles) {
		float avg = 0f;
		for(Double num : doubles) {
			avg += num;
		}		
		return avg/doubles.size();
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
}
