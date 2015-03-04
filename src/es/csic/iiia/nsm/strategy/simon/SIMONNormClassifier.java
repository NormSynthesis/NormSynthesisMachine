package es.csic.iiia.nsm.strategy.simon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.csic.iiia.nsm.config.Dimension;
import es.csic.iiia.nsm.config.Goal;
import es.csic.iiia.nsm.config.NormSynthesisSettings;
import es.csic.iiia.nsm.metrics.NormSynthesisMetrics;
import es.csic.iiia.nsm.net.norm.NetworkNodeState;
import es.csic.iiia.nsm.net.norm.NormativeNetwork;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.evaluation.Utility;
import es.csic.iiia.nsm.norm.refinement.lion.NormAttribute;

/**
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 *
 */
public class SIMONNormClassifier {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------

	protected List<Dimension> normEvDimensions;
	private NormSynthesisSettings nsmSettings;
	private NormativeNetwork normativeNetwork;
	private NormSynthesisMetrics nsMetrics;
	
	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------

	public SIMONNormClassifier(List<Dimension> normEvDimensions,
			NormSynthesisSettings nsmSettings, NormativeNetwork normativeNetwork,
			SIMONOperators operators, NormSynthesisMetrics nsMetrics) {

		this.normEvDimensions = normEvDimensions;
		this.nsmSettings = nsmSettings;
		this.normativeNetwork = normativeNetwork;
		this.nsMetrics = nsMetrics;
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

		/* Perform detection of ineffective, unnecessary and generalisable norms */
		for(Norm nA : norms) {
			this.checkEffectiveness(nA, normClassifications);
			this.checkNecessity(nA, normClassifications);
			
			/* Only check for generalisation active norms (which, in fact, will be
			 * the ones that may be generalised at the end) */
			if(this.normativeNetwork.getState(nA) == NetworkNodeState.ACTIVE) {
				this.checkGeneralisation(nA, normClassifications);
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
		boolean isClassifedAsNecessary = attributes.contains(NormAttribute.NECESSARY);
		boolean isClassifiedAsUnnecessary = attributes.contains(NormAttribute.UNNECESSARY);
		boolean hasNotBeenClassified = !isClassifedAsNecessary && !isClassifiedAsUnnecessary;

		for(Goal goal : nsmSettings.getSystemGoals()) {

			/* The norm is classified as necessary (or even it has not been
			 * classified as unnecessary yet) and now it under performs
			 * -> classify the norm as unnecessary */
			if(isClassifedAsNecessary || hasNotBeenClassified) 
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
				for(Goal goal : this.nsmSettings.getSystemGoals()) {

					if(this.shouldBeDeactivated(norm, dim, goal)) {
						return;
					}
//					Utility utility = this.normativeNetwork.getUtility(norm);
//
//					float topBoundary = (float)utility.getPerformanceRange(dim, goal).
//							getCurrentTopBoundary();
//					float satDegree = this.nsmSettings.
//							getGeneralisationBoundary(dim, goal);
//
//					if(topBoundary < satDegree) {
//						return;
//					}
				}
			}
			this.assignAttribute(norm, NormAttribute.GENERALISABLE,
					normClassifications);
		}
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
	private boolean shouldBeDeactivated(Norm norm, Dimension dim, Goal goal) {
		Utility utility = this.normativeNetwork.getUtility(norm);
		float topBoundary = (float)utility.getPerformanceRange(dim, goal).
				getCurrentTopBoundary();
		float satDegree = this.nsmSettings.getSpecialisationBoundary(dim, goal);

		if(topBoundary < satDegree) {
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
	private boolean shouldBeActivated(Norm norm, Dimension dim, Goal goal) {
		Utility utility = this.normativeNetwork.getUtility(norm);
		float bottomBoundary = (float)utility.getPerformanceRange(dim, goal).
				getCurrentBottomBoundary();
		float satDegree = this.nsmSettings.getSpecialisationBoundary(dim, goal);

		if(bottomBoundary > satDegree) {
			return true;
		}
		return false;
	}
}