package es.csic.iiia.nsm.metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.csic.iiia.nsm.NormSynthesisMachine;
import es.csic.iiia.nsm.config.Dimension;
import es.csic.iiia.nsm.config.Goal;
import es.csic.iiia.nsm.config.NormSynthesisSettings;
import es.csic.iiia.nsm.net.norm.NormativeNetwork;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.NormativeSystem;
import es.csic.iiia.nsm.norm.evaluation.Utility;
import es.csic.iiia.nsm.norm.refinement.iron.GeneralisationReasoner;
import es.csic.iiia.nsm.utilities.SlidingWindowMetric;

/**
 * Class containing information about several norm synthesis metrics
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 *
 */
public class DefaultNormSynthesisMetrics implements NormSynthesisMetrics {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------
	
	protected boolean converged;		// has the norm synthesis process converged?

	protected NormSynthesisSettings 	nsmSettings;			// norm synthesis settings
	protected NormativeNetwork 				normativeNetwork;	// the normative network
	protected GeneralisationReasoner 	genReasoner;			// to reason about norms
	protected Utility nsUtility;
	
	protected long numTicksWithoutNormChanges;
	protected long numTicksWithoutNonRegulatedConflicts;
	
	protected List<Dimension> dimensions;
	protected List<Norm> addedNorms;
	protected List<Norm> removedNorms;
	protected List<Norm> normsInNormativeNetwork;
	protected List<Norm> normsInNormativeSystem;
	protected List<Norm> normsAddedToNNThisCycle;
	protected List<Norm> normsAddedToNSThisCycle;
	protected List<Norm> normsRemovedFromNSThisCycle;

	private int 		fitoussiMinimality;								// #norms represented
	private double 	conflictivityRatio;								// ? 
	private long	 	numNodesVisited;									// overall #visits to norms
	private long 		numNodesInMemory;									// overall #norms in memory
	private long	 	numNodesSynthesised;							// overall #norms synthesised
	private double 	minComputationTime;								// min strategy computation time
	private double 	maxComputationTime;								// max strategy computation time
	private double 	medianComputationTime;						// median computation time
	private double 	totalComputationTime;							// overall computation time
	
	private SlidingWindowMetric allComputationTimes;
	private SlidingWindowMetric nonRegulatedConflictsWindow;

	private boolean hasNonRegulatedConflictsThisTick;
	
	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------
	
	/**
	 * Constructor 
	 */
	public DefaultNormSynthesisMetrics(NormSynthesisMachine nsm) {
		
		this.nsmSettings = nsm.getNormSynthesisSettings();
		this.normativeNetwork = nsm.getNormativeNetwork();
		this.dimensions = nsm.getNormEvaluationDimensions();
		
		this.converged = false;
		this.hasNonRegulatedConflictsThisTick = false;
		this.numTicksWithoutNormChanges = 0;
		this.numTicksWithoutNonRegulatedConflicts= 0;
		this.conflictivityRatio = 0;
		this.fitoussiMinimality = 0;
		
		this.minComputationTime = Double.MAX_VALUE;
		this.maxComputationTime = Double.MIN_VALUE;
		this.medianComputationTime = 0;
		this.totalComputationTime = 0;
		this.numNodesSynthesised = 0;
		this.numNodesInMemory = 0;
		this.numNodesVisited = 0;
		
		this.addedNorms = new ArrayList<Norm>();
		this.removedNorms = new ArrayList<Norm>();
		this.normsInNormativeNetwork = new ArrayList<Norm>();
		this.normsInNormativeSystem  = new ArrayList<Norm>();
		this.normsAddedToNNThisCycle = new ArrayList<Norm>();
		this.normsAddedToNSThisCycle = new ArrayList<Norm>();
		this.normsRemovedFromNSThisCycle = new ArrayList<Norm>();

		int pSize = this.nsmSettings.getNormsPerformanceRangesSize();
		this.nsUtility = new Utility(0, pSize, nsm.getNormEvaluationDimensions(),
				nsmSettings.getSystemGoals());
		
		this.genReasoner = new GeneralisationReasoner(nsm.getPredicatesDomains(),
				nsm.getDomainFunctions());
		
		this.allComputationTimes = new SlidingWindowMetric(50000);
		this.nonRegulatedConflictsWindow = new SlidingWindowMetric(
				nsmSettings.getNumTicksOfStabilityForConvergence());
	}
	
	/**
	 * Updates the norm synthesis metrics
	 */
	public void update(double timeStep) {
		this.normsAddedToNNThisCycle.clear();
		this.normsAddedToNSThisCycle.clear();
		this.normsRemovedFromNSThisCycle.clear();
				
		/* Check norm additions to the normative network */
		for(Norm norm : this.normativeNetwork.getNorms()) {
			if(!this.normsInNormativeNetwork.contains(norm)) {
				this.normsInNormativeNetwork.add(norm);
				this.normsAddedToNNThisCycle.add(norm);
			}
		}
		
		/* Check norm additions to the normative system */
		for(Norm norm : this.normativeNetwork.getNormativeSystem()) {
			if(!this.normsInNormativeSystem.contains(norm)) {
				this.normsInNormativeSystem.add(norm);
				this.normsAddedToNSThisCycle.add(norm);
			}
		}
		
		/* Check norm removals from the normative system */
		List<Norm> toRemove = new ArrayList<Norm>();
		for(Norm norm : this.normsInNormativeSystem) {
			if(!this.normativeNetwork.getNormativeSystem().contains(norm)) {
				this.normsRemovedFromNSThisCycle.add(norm);
				toRemove.add(norm);
			}
		}
		for(Norm norm : toRemove) {
			this.normsInNormativeSystem.remove(norm);
		}
		
		
		/* Metrics variables management */
		if(this.hasNormativeSystemChangedThisTick()) {
			this.numTicksWithoutNormChanges = 0l;
		}
		else {
			this.numTicksWithoutNormChanges++;
		}
		
		if(this.hasNonRegulatedConflictsThisTick) {
			this.numTicksWithoutNonRegulatedConflicts = 0l;
			this.nonRegulatedConflictsWindow.addValue(1);
		}
		else {
			this.numTicksWithoutNonRegulatedConflicts++;
			this.nonRegulatedConflictsWindow.addValue(0);
		}
		
		double sum = this.nonRegulatedConflictsWindow.getSum();
		this.conflictivityRatio = sum / nsmSettings.getNumTicksOfStabilityForConvergence();
		
		/* Check norm synthesis convergence */
//		if(!converged) {
			long numTicksConvergence = nsmSettings.getNumTicksOfStabilityForConvergence();
			
			this.converged = this.numTicksWithoutNormChanges >= numTicksConvergence;
					// &&	this.numTicksWithoutNonRegulatedConflicts >= numTicksConvergence;
//		}	
		
		this.updateNormativeSystemMetrics();
	}

	/**
	 * Updates the normative system metrics
	 */
	private void updateNormativeSystemMetrics() {
		NormativeSystem ns = this.normativeNetwork.getNormativeSystem();
		
		for(Dimension dim : this.dimensions) {
			for(Goal goal : this.nsmSettings.getSystemGoals()) {
				List<Float> scores = new ArrayList<Float>();
				for(Norm norm : ns) {
					float score = (float) this.normativeNetwork.
							getUtility(norm).getPerformanceRange(dim, goal).
							getCurrentAverage();
					
					scores.add(score);
				}
				Collections.sort(scores);
				if(!scores.isEmpty()) {
					float medianSc = scores.get(scores.size()/2);
					this.nsUtility.setScore(dim, goal, medianSc);
				}
			}
		}
	}
	
	/**
	 * Returns information about if the normative system has changed this tick
	 * 
	 * @return true if the normative system has changed this tick
	 */
	public boolean hasNormativeSystemChangedThisTick() {
		if(!this.normsAddedToNSThisCycle.isEmpty() || 
				!this.normsRemovedFromNSThisCycle.isEmpty())
		{
			return true;
		}
		return false;
	}
	
	/**
	 * Returns information about if the normative network has changed this tick
	 * 
	 * @return true if the normative network has changed this tick
	 */
	public boolean hasNormativeNetworkChangedThisTick() {
		if(!this.normsAddedToNNThisCycle.isEmpty()) {
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
	 * Returns  the cardinality of the normative system
	 * 
	 * @return the cardinality of the normative system
	 */
	@Override
  public int getNormativeSystemMinimality() {
	  return this.normativeNetwork.getNormativeSystem().size();
  }
	
	/**
	 * Returns the f-minimality (Fitoussi's minimality)
	 * of the normative system (i.e., the number of leave
	 * norms it represetents)
	 * 
	 * @return 	the f-minimality (Fitoussi's minimality)
	 * 					of the normative system (i.e., the number of leave
	 * 					norms it represetents)
	 */
	public int getNormativeSystemFitoussiMinimality() {
		return this.fitoussiMinimality;
	}
	
	/**
	 * Returns the {@code List} of norms that have been added to
	 * the normative network during the current tick
	 * 
	 * @return the {@code List} of norms that have been added to
	 *					the normative network during the current tick
	 */
	@Override
  public List<Norm> getNormsAddedThisTick() {
		return this.addedNorms;
  }

	/**
	 * Returns the {@code List} of norms that have been removed
	 * from the normative network during the current tick
	 * 
	 * @return the {@code List} of norms that have been removed
	 * 					from the normative network during the current tick
	 */
	@Override
  public List<Norm> getNormsRemovedThisTick() {
	  return this.removedNorms;
  }
	
	/**
	 * Returns the norm synthesis settings
	 * 
	 * @return the norm synthesis settings
	 * @see NormSynthesisSettings
	 */
  public NormSynthesisSettings getNormSynthesisSettings() {
  	return this.nsmSettings;
  } 
  
	/**
	 * Returns the utility of a given norm in the normative network 
	 * 
	 * @param norm the norm
	 * @return the utility of the norm
	 * @see Utility
	 */
  public Utility getNormUtility(Norm norm){
		return this.normativeNetwork.getUtility(norm);
	}
  
  /**
   * 
   * @return
   */
  public double getConflictivityRatio() {
  	return this.conflictivityRatio;
  }
  
	/**
	 * 
	 */
	@Override
	public void newNonRegulatedConflictsSolvedThisTick() {
		this.hasNonRegulatedConflictsThisTick = true;
	}
	
	/* (non-Javadoc)
	 * @see es.csic.iiia.nsm.metrics.NormSynthesisMetrics#resetNonRegulatedConflicts()
	 */
  @Override
  public void resetNonRegulatedConflicts() {
  	this.hasNonRegulatedConflictsThisTick = false;
  }
  
  /**
	 * 
	 * @param timeCost
	 */
	public void addNewComputationTime(double compTime) {
		this.totalComputationTime += compTime;
		this.allComputationTimes.addValue(compTime);
		
		if(compTime < this.minComputationTime) {
			this.minComputationTime = compTime;
		}
		else if(compTime > this.maxComputationTime) {
			this.maxComputationTime = compTime;
		}
		this.medianComputationTime = allComputationTimes.getMedian();
	}
	
	/**
	 * 
	 */
	public void incNumNodesVisited() {
		this.numNodesVisited++;
	}
	
	/**
	 * 
	 */
	public void incNumNodesInMemory() {
		this.numNodesInMemory++;
	}
	
	/**
	 * 
	 */
	public void incNumNodesSynthesised() {
		this.numNodesSynthesised++;
	}
	
	/**
	 * 
	 * @return
	 */
	public long getNumNodesVisited() {
		return numNodesVisited;
	}
	
	/**
	 * 
	 * @return
	 */
	public long getNumNodesInMemory() {
		return numNodesInMemory;
	}
	
	/**
	 * 
	 * @return
	 */
	public long getNumNodesSynthesised() {
		return numNodesSynthesised;
	}
	
	/**
	 * 
	 * @return
	 */
	public double getMinComputationTime() {
		return minComputationTime;
	}
	
	/**
	 * 
	 * @return
	 */
	public double getMaxComputationTime() {
		return maxComputationTime;
	}

	/**
	 * 
	 * @return
	 */
	public double getMedianComputationTime() {
		return this.medianComputationTime;
	}
	
	/**
	 * 
	 * @return
	 */
	public double getTotalComputationTime() {
		return this.totalComputationTime;
	}
	
	/**
	 * 
	 * @return
	 */
	public long getNumTicksOfStability() {
		return this.numTicksWithoutNormChanges;
	}

	/* (non-Javadoc)
	 * @see es.csic.iiia.nsm.metrics.NormSynthesisMetrics#getNormativeSystemUtility()
	 */
  @Override
  public Utility getNormativeSystemUtility() {
	  return this.nsUtility;
  }
}