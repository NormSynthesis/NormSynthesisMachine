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
import es.csic.iiia.nsm.norm.refinement.iron.GeneralisationTrees;
import es.csic.iiia.nsm.strategy.iron.IRONStrategy;

/**
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 *
 */
public class IRONMetrics implements NormSynthesisMetrics {

	private boolean converged;
	
	private long numTicksWithoutNormChanges,
	numTicksWithoutNonRegulatedConflicts, numChangesOfNormativeSystem;
	
	private long numNormAdditions, numNormRemovals;
	private NormSynthesisSettings nsmSettings;
	private NormativeNetwork normativeNetwork;
	private IRONStrategy strategy;
	
	private List<Norm> addedNorms, removedNorms;
	private NormativeSystem normativeSystem;
	private GeneralisationTrees genTrees;
	
	/**
	 * 
	 */
	public IRONMetrics(NormSynthesisSettings nsmSettings, 
			NormativeNetwork normativeNetwork, IRONStrategy strategy)
	{
		this.nsmSettings = nsmSettings;
		this.normativeNetwork = normativeNetwork;
		this.strategy = strategy;
		this.genTrees = genTrees;
		
		this.addedNorms = new ArrayList<Norm>();
		this.removedNorms = new ArrayList<Norm>();
		this.normativeSystem = new NormativeSystem();
		
		this.converged = false;
		this.numTicksWithoutNormChanges = 0;
		this.numTicksWithoutNonRegulatedConflicts= 0;
		this.numChangesOfNormativeSystem = 0;
		this.numNormAdditions = 0;
		this.numNormAdditions = 0;
		this.numNormRemovals = 0;
	}
	
	/**
	 * 
	 */
	public void update()
	{	
		this.addedNorms.clear();
		this.removedNorms.clear();

		/*
		 * Check norm addition and removal
		 */
		NormativeSystem ns = normativeNetwork.getNormativeSystem();
		
		// Check norm additions 
		for(Norm norm : ns){
			if(!normativeSystem.contains(norm)){
				normativeSystem.add(norm);
				this.addedNorms.add(norm);
			}
		}

		// Check norm removals
		for(Norm norm : normativeSystem){
			if(!ns.contains(norm)){
				this.removedNorms.add(norm);
			}
		}	

		// Remove norms from normative systems
		for(Norm norm : removedNorms){
			this.normativeSystem.remove(norm);
		}
		
		this.numNormAdditions += this.addedNorms.size();
		this.numNormRemovals += this.removedNorms.size();
		
		/*
		 * Metrics variables management
		 */
		if(this.hasNormativeSystemChangedThisTick()) {
			this.numChangesOfNormativeSystem++;
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
		
		/*
		 * Check system convergence 
		 */
		if(!converged) {
			long numTicksConvergence = nsmSettings.getNumTicksOfStabilityForConvergence();
			
			// Check convergence
			this.converged = this.numTicksWithoutNormChanges >= numTicksConvergence &&
			this.numTicksWithoutNonRegulatedConflicts >= numTicksConvergence;
		}		
	}
	
	/**
	 * 
	 * @return
	 */
	private boolean hasNonRegulatedConflictsThisTick() {
		Map<Goal, List<Conflict>> conflicts = this.strategy.getNonRegulatedConflictsThisTick();
		int ret = 0;
		
		for(Goal goal : conflicts.keySet()) {
			ret += conflicts.get(goal).size();
		}

		return ret > 0;
  }

	/**
	 * 
	 */
	public boolean hasNormativeSystemChangedThisTick() {
		if(addedNorms.size() > 0 || removedNorms.size() > 0)
			return true;
		return false;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean hasConverged() {
		return this.converged;
	}

	/**
	 * 
	 */
	@Override
  public int getNormativeNetworkCardinality() {
	 return this.normativeNetwork.getCardinality();
  }

	/**
	 * 
	 */
	@Override
  public List<Norm> getNormsAddedThisTick() {
		return this.addedNorms;
  }

	/**
	 * 
	 */
	@Override
  public List<Norm> getNormsRemovedThisTick() {
	  return this.removedNorms;
  }

	/**
	 * 
	 * @return
	 */
	@Override
  public int getNormativeSystemCardinality() {
	  return this.normativeNetwork.getNormativeSystem().size();
  }

	/**
	 * 
	 * @return
	 */
	public long getNumNormAdditions() {
	  return numNormAdditions;
  }

	/**
	 * 
	 * @return
	 */
	public long getNumNormRemovals() {
	  return numNormRemovals;
  }
	
	/**
	 * 
	 * @return
	 */
	public long getNumChangesOfNormativeSystem() {
	  return numChangesOfNormativeSystem;
  }
  
  /**
   * 
   * @param norm
   * @return
   */
  public Utility getNormUtility(Norm norm) {
  	return this.normativeNetwork.getUtility(norm);
  }
  
  /**
   * 
   * @return
   */
  public int getNumNormsInMemory() {
	  return this.genTrees.computeMetrics();
  }
  
  /**
   * 
   */
  public NormSynthesisSettings getNormSynthesisSettings() {
  	return this.nsmSettings;
  } 
}
