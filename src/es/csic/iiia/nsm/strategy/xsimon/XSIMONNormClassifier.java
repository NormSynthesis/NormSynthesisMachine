/**
 * 
 * @author Javier Morales (jmorales@iiia.csic.es)
 */
package es.csic.iiia.nsm.strategy.xsimon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.csic.iiia.nsm.config.Dimension;
import es.csic.iiia.nsm.config.Goal;
import es.csic.iiia.nsm.config.NormSynthesisSettings;
import es.csic.iiia.nsm.net.norm.NetworkEdgeType;
import es.csic.iiia.nsm.net.norm.NetworkNode;
import es.csic.iiia.nsm.net.norm.NormativeNetwork;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.evaluation.NormCompliance;
import es.csic.iiia.nsm.norm.evaluation.PerformanceRange;
import es.csic.iiia.nsm.norm.evaluation.Utility;
import es.csic.iiia.nsm.norm.group.NormGroup;
import es.csic.iiia.nsm.norm.group.NormGroupCombination;
import es.csic.iiia.nsm.norm.group.net.NormGroupNetwork;
import es.csic.iiia.nsm.norm.refinement.xsimon.NormAttribute;
import es.csic.iiia.nsm.utilities.Minkowski;

/**
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 *
 */
public class XSIMONNormClassifier {

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

	public XSIMONNormClassifier(List<Dimension> normEvDimensions,
			NormSynthesisSettings nsmSettings, NormativeNetwork normativeNetwork,
			NormGroupNetwork normGroupNetwork, XSIMONOperators operators) {

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

			List<NormAttribute> normTags = this.normativeNetwork.getTags(nA);
			boolean isIneffective = normTags.contains(NormAttribute.Ineffective);
			boolean isUnnecessary = normTags.contains(NormAttribute.Unnecessary);
			boolean isGeneralisable = normTags.contains(NormAttribute.Generalisable);

			/* 1. Detect ineffectiveness, unnecessariness and generalisability for 
			 * those norms that are not ineffective and/or unnecessary */
			if(!isIneffective) {
				this.checkEffectiveness(nA, normClassifications);
			}
			if(!isUnnecessary) {
				this.checkNecessity(nA, normClassifications);
			}
			if(!isGeneralisable) {
				this.checkGeneralisation(nA, normClassifications);
			}

			/* If the norm is not a leaf, then do not detect substitutability */
			if(!this.normativeNetwork.isLeaf(nA)) {
				continue;
			}

			/* 2. Detect substitutability based on the norm group combinations that
			 * the norm has with other norms in the normative network. We use only
			 * those norm group combinations that contain a norm nB which is not 
			 * substitutable with norm nA */
			if(normGroupNetwork.hasNormGroupCombinations(nA)) {
				Map<Norm, NormGroupCombination> nGrCombs = 
						normGroupNetwork.getNormGroupCombinations(nA);

				/* Compute substitutability if both norms are not substitutable yet */
				for(Norm nB : nGrCombs.keySet()) {
					NormGroupCombination nABComb = nGrCombs.get(nB);

					if(normGroupNetwork.isActive(nABComb) &&
							!normativeNetwork.areSubstitutable(nA, nB)) {
						
						this.checkSubstitutability(nA, nB, nABComb,
								normClassifications);
					}
				}
			}
		}

		/* Add norm attributes to the normative network */
		this.normativeNetwork.addAttributes(normClassifications);

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
		normClassifications.get(norm).add(attribute);
	}

	/**
	 * 
	 * @param norm
	 */
	private void checkEffectiveness(Norm norm,
			Map<Norm,List<NormAttribute>> normClassifications) {
		
		for(Goal goal : nsmSettings.getSystemGoals()) {
			if(this.underPerforms(norm,	Dimension.Effectiveness, goal)) {
				this.assignAttribute(norm, NormAttribute.Ineffective,
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
		
		for(Goal goal : nsmSettings.getSystemGoals()) {
			if(this.underPerforms(norm,	Dimension.Necessity, goal)) {
				this.assignAttribute(norm, NormAttribute.Unnecessary,
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
		
		for(Dimension dim : this.normEvDimensions)	 {
			for(Goal goal : this.nsmSettings.getSystemGoals()) {

				Utility utility = this.normativeNetwork.getUtility(norm);
				float bottomBoundary = utility.
						getPerformanceRange(dim, goal).getCurrentBottomBoundary();
				float satDegree = this.nsmSettings.
						getGeneralisationBoundary(dim, goal);

				if(bottomBoundary < satDegree) {
					return;
				}
			}
		}
		this.assignAttribute(norm, NormAttribute.Generalisable,
				normClassifications);
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

		NormGroup groupFF = nABComb.get(NormCompliance.Fulfilment,
				NormCompliance.Fulfilment);
		NormGroup groupFI = nABComb.get(NormCompliance.Fulfilment,
				NormCompliance.Infringement);
		NormGroup groupIF = nABComb.get(NormCompliance.Infringement,
				NormCompliance.Fulfilment);

		for(Goal goal : nsmSettings.getSystemGoals()) {

			boolean groupFISimilarToFF = this.areSimilar(groupFI, groupFF, goal);
			boolean groupIFSimilarToFF = this.areSimilar(groupIF, groupFF, goal);
			boolean groupFFDoesNotUnderPerform =
					!this.underPerforms(groupFF, Dimension.Effectiveness, goal);

			/* Conditions to be substitutable*/
			if(groupFFDoesNotUnderPerform && 
					groupFISimilarToFF &&
					groupIFSimilarToFF) {
				
				/* Add substitutability attributes */
				this.assignAttribute(nA, NormAttribute.Substitutable,
						normClassifications);
				this.assignAttribute(nB, NormAttribute.Substitutable,
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

		float scAvg = utility.getScoreAverage(dim, goal);
		float satDegree = this.nsmSettings.getSpecialisationBoundary(dim, goal);
		float scStdDevPlus = utility.getPerformanceRange(dim, goal).
				getCurrentTopBoundary();

		/* The norm is ineffective */
		if(scAvg + scStdDevPlus < satDegree) {
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
			if(numValues < 25) {
				return false;
			}

			try {
				distance = this.minkowski.distance(s1, s2, numValues);
			} 
			catch(IllegalArgumentException e) {
				return false;
			}

			if(distance > 1.5) {
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

}
