/**
 * 
 * @author Javier Morales (jmorales@iiia.csic.es)
 */
package es.csic.iiia.nsm.strategy.simone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.csic.iiia.nsm.config.Dimension;
import es.csic.iiia.nsm.config.Goal;
import es.csic.iiia.nsm.config.NormSynthesisSettings;
import es.csic.iiia.nsm.net.norm.NetworkNode;
import es.csic.iiia.nsm.net.norm.NormativeNetwork;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.evaluation.Utility;
import es.csic.iiia.nsm.norm.group.net.NormGroupNetwork;
import es.csic.iiia.nsm.norm.refinement.lion.NormAttribute;

/**
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 *
 */
public class SIMONENormClassifier {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------

	protected List<Dimension> normEvDimensions;
	private NormSynthesisSettings nsmSettings;
	private NormativeNetwork normativeNetwork;

	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------

	public SIMONENormClassifier(List<Dimension> normEvDimensions,
			NormSynthesisSettings nsmSettings, NormativeNetwork normativeNetwork,
			NormGroupNetwork normGroupNetwork, SIMONEOperators operators) {

		this.normEvDimensions = normEvDimensions;
		this.nsmSettings = nsmSettings;
		this.normativeNetwork = normativeNetwork;
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


		/* Perform detection of ineffective, unnecessary and substitutable norms */
		for(Norm nA : norms) {

			/* 1. Detect norms that are ineffective, unnecessary 
			 * and/or generalisable */
			this.checkEffectiveness(nA, normClassifications);
			this.checkNecessity(nA, normClassifications);
			this.checkGeneralisation(nA, normClassifications);
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
					float topBoundary = (float)utility.getPerformanceRange(dim, goal).
							getCurrentTopBoundary();
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

		float satDegree = this.nsmSettings.getSpecialisationBoundary(dim, goal);
		float epsilon = this.nsmSettings.getSpecialisationBoundaryEpsilon(dim, goal);
		//		float topBoundary = utility.getPerformanceRange(dim, goal).
		//				getCurrentTopBoundary();
		float avg = (float)utility.getPerformanceRange(dim, goal).
				getCurrentAverage();

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

		float satDegree = this.nsmSettings.getSpecialisationBoundary(dim, goal);
		float epsilon = this.nsmSettings.getSpecialisationBoundaryEpsilon(dim, goal);

		//		float bottomBoundary = utility.getPerformanceRange(dim, goal).
		//				getCurrentBottomBoundary();
		float avg = (float)utility.getPerformanceRange(dim, goal).
				getCurrentAverage();

		//		if(utility.getPerformanceRange(dim, goal).getPunctualValues().size() < 20)
		//			return false;

		/* The norm performs well */
		//		if(bottomBoundary > satDegree) {
		if(avg >= (satDegree + epsilon)) {
			return true;
		}
		return false;
	}
}
