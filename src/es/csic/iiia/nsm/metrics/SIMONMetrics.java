package es.csic.iiia.nsm.metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import es.csic.iiia.nsm.config.Goal;
import es.csic.iiia.nsm.config.NormSynthesisSettings;
import es.csic.iiia.nsm.net.norm.NormativeNetwork;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.NormativeSystem;
import es.csic.iiia.nsm.norm.evaluation.Utility;
import es.csic.iiia.nsm.norm.generation.Conflict;
import es.csic.iiia.nsm.strategy.simon.SIMONStrategy;

/**
 * Class containing information about several norm synthesis metrics
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 *
 */
public class SIMONMetrics implements NormSynthesisMetrics {

	private boolean converged;		// has the norm synthesis process converged?

	private NormSynthesisSettings nsmSettings;
	private NormativeNetwork normativeNetwork;
	private SIMONStrategy strategy;
	private List<Norm> addedNorms;
	private List<Norm> removedNorms;
	private NormativeSystem normativeSystem;

	private long numTicksWithoutNormChanges;
	private long numTicksWithoutNonRegulatedConflicts;
//	private long numChangesOfNormativeSystem;
//	private long numNormAdditions;
//	private long numNormRemovals;
		
	/**
	 * Constructor 
	 */
	public SIMONMetrics(NormSynthesisSettings settings,
			NormativeNetwork normativeNetwork, SIMONStrategy strategy)
	{
		this.nsmSettings = settings;
		this.normativeNetwork = normativeNetwork;
		this.strategy = strategy;
		
		this.addedNorms = new ArrayList<Norm>();
		this.removedNorms = new ArrayList<Norm>();
		this.normativeSystem = new NormativeSystem();
		
		this.converged = false;
		this.numTicksWithoutNormChanges = 0;
		this.numTicksWithoutNonRegulatedConflicts= 0;
//		this.numChangesOfNormativeSystem = 0;
//		this.numNormAdditions = 0;
//		this.numNormAdditions = 0;
//		this.numNormRemovals = 0;
	}
	
	/**
	 * Updates the norm synthesis metrics
	 */
	public void update()
	{	
		this.addedNorms.clear();
		this.removedNorms.clear();

		NormativeSystem ns = normativeNetwork.getNormativeSystem();
		
		/* Check norm additions */ 
		for(Norm norm : ns){
			if(!normativeSystem.contains(norm)){
				normativeSystem.add(norm);
				this.addedNorms.add(norm);
			}
		}

		/* Check norm removals */
		for(Norm norm : normativeSystem) {
			if(!ns.contains(norm)){
				this.removedNorms.add(norm);
			}
		}	

		/* Remove norms from normative systems */
		for(Norm norm : removedNorms) {
			this.normativeSystem.remove(norm);
		}
		
//		this.numNormAdditions += this.addedNorms.size();
//		this.numNormRemovals += this.removedNorms.size();
		
		/* Metrics variables management */
		if(this.hasNormativeSystemChangedThisTick()) {
//			this.numChangesOfNormativeSystem++;
			this.numTicksWithoutNormChanges = 0l;
		}
		else {
			this.numTicksWithoutNormChanges++;
		}
		
		if(this.hasNonRegulatedConflictsThisTick()) {
			this.numTicksWithoutNonRegulatedConflicts = 0l;
		}
		else {
			this.numTicksWithoutNonRegulatedConflicts++;
		}
		
		/* Check norm synthesis convergence */
		if(!converged) {
			long numTicksConvergence = nsmSettings.getNumTicksOfStabilityForConvergence();
			
			this.converged = this.numTicksWithoutNormChanges >= numTicksConvergence &&
			this.numTicksWithoutNonRegulatedConflicts >= numTicksConvergence;
		}		
	}
	
	/**
	 * Returns true if have arisen new conflict (those that are not
	 * regulated by norms) during the current tick
	 * 
	 * @return true if non regulated conflicts have arisen this tick
	 */
	private boolean hasNonRegulatedConflictsThisTick()
	{
		Map<Goal, List<Conflict>> conflicts = this.strategy.getNonRegulatedConflictsThisTick();
		int ret = 0;
		
		for(Goal goal : conflicts.keySet())
			ret += conflicts.get(goal).size();

		return ret > 0;
  }

	/**
	 * Returns information about if the normative system has changed this tick
	 * 
	 * @return true if the normative system has changed this tick
	 */
	public boolean hasNormativeSystemChangedThisTick()
	{
		if(addedNorms.size() > 0 || removedNorms.size() > 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * Returns true if the norm synthesis process has converged
	 * 
	 * @return true if the norm synthesis process has converged
	 */
	public boolean hasConverged() {
		return this.converged;
	}

	/**
	 * Returns the cardinality of the normative network
	 * 
	 * @return the cardinality of the normative network
	 */
	@Override
  public int getNormativeNetworkCardinality() {
	 return this.normativeNetwork.getCardinality();
  }

	/**
	 * Returns the cardinality of the normative system
	 * 
	 * @return the cardinality of the normative system
	 */
	@Override
  public int getNormativeSystemCardinality() {
	  return this.normativeNetwork.getNormativeSystem().size();
  }
	
	/**
	 * Returns the norms that have been added to the normative
	 * system during the current tick
	 * 
	 * @return the norms added this tick
	 */
	@Override
  public List<Norm> getNormsAddedThisTick() {
		return this.addedNorms;
  }

	/**
	 * Returns the norms that have been removed from the normative
	 * system during the current tick
	 * 
	 * @return the norms removed this tick
	 */
	@Override
  public List<Norm> getNormsRemovedThisTick() {
	  return this.removedNorms;
  }
	
  /**
   * 
   */
  public NormSynthesisSettings getNormSynthesisSettings() {
  	return this.nsmSettings;
  } 
  
  /**
   * 
   * @param norm
   * @return
   */
  public Utility getNormUtility(Norm norm){
		return this.normativeNetwork.getUtility(norm);
	}
  
///**
// * 
// * @return
// */
//public long getNumChangesOfNormativeSystem() {
//  return numChangesOfNormativeSystem;
//}

}