package es.csic.iiia.nsm.norm.evaluation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * The sliding window of a performance range. It consists in several
 * series that contain the last N values of a performance range:
 * <ol>
 * <li> the punctual values;
 * <li> the average of the punctual values;
 * <li> the top boundary of the performance range, which is computed as the 
 * 			average + the standard deviation of the average
 * <li> the bottom boundary of the performance range, which is computed as the 
 * 			average - the standard deviation of the average 
 * </ol> 
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
public class PerformanceRange {

	//---------------------------------------------------------------------------
	// Atributes
	//---------------------------------------------------------------------------

	private long maxSlidingValues;
	private boolean hasNewValue;
	
	private List<Double> punctualValues;
	private List<Double> movingAverage;
	private List<Double> topBoundary;
	private List<Double> bottomBoundary;
	
	private LinkedList<Double> slidingPunctualValues;
	private LinkedList<Double> slidingMovingAverage;
	private LinkedList<Double> slidingTopBoundary;
	private LinkedList<Double> slidingBottomBoundary;
	
	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------

	/**
	 * Constructor
	 * 
	 * @param size
	 */
	public PerformanceRange(long size){
		this.punctualValues = new ArrayList<Double>();
		this.movingAverage = new ArrayList<Double>();
		this.topBoundary = new ArrayList<Double>();
		this.bottomBoundary = new ArrayList<Double>();
		this.slidingPunctualValues = new LinkedList<Double>();
		this.slidingMovingAverage = new LinkedList<Double>();
		this.slidingTopBoundary = new LinkedList<Double>();
		this.slidingBottomBoundary = new LinkedList<Double>();
		
		this.maxSlidingValues = size;
		this.hasNewValue = false;
	}

	/**
	 * Adds a value to the window
	 * 
	 * @param value
	 */
	public void addValue(double value) {
		this.hasNewValue = true;
		
		/* Add new punctual value */
		this.add(punctualValues, slidingPunctualValues, value);

		/* Compute and add new average value */
		double avg = this.getAvg();
		this.add(movingAverage, slidingMovingAverage, avg);
		
		/* Compute and add new boundaries values */
		double stdDev = this.getStdDev();
		double topBnd = avg + stdDev;
		double btmBnd = avg - stdDev;
		this.add(topBoundary, slidingTopBoundary, topBnd);
		this.add(bottomBoundary, slidingBottomBoundary, btmBnd);
		
		/* Remove old values */
		if(this.slidingPunctualValues.size() > this.maxSlidingValues) {
			this.slidingPunctualValues.remove();
		}
		if(this.slidingMovingAverage.size() > this.maxSlidingValues) {
			this.slidingMovingAverage.remove();
		}
		if(this.slidingTopBoundary.size() > this.maxSlidingValues) {
			this.slidingTopBoundary.remove();
		}
		if(this.slidingBottomBoundary.size() > this.maxSlidingValues) {
			this.slidingBottomBoundary.remove();
		}
	}

	/**
	 * Returns the list of all values in the punctual values series
	 * 
	 * @return the list of all values in the punctual values series
	 */
	public List<Double> getPunctualValues() {
		return this.punctualValues;
	}
	
	/**
	 * Returns the list of all values in the average series
	 * 
	 * @return the list of all values in the average series
	 */
	public List<Double> getAverage() {
		return this.movingAverage;
	}
	
	/**
	 * Returns the list of all values in the top boundary series
	 * 
	 * @return the list of all values in the top boundary series
	 */
	public List<Double> getTopBoundary() {
		return this.topBoundary;
	}
	
	/**
	 * Returns the list of all values in the bottom boundary series
	 * 
	 * @return the list of all values in the bottom boundary series
	 */
	public List<Double> getBottomBoundary() {
		return this.bottomBoundary;
	}
	
	/**
	 * Returns the list of last N values in the punctual values series
	 * 
	 * @return the list of last N values in the punctual values series
	 */
	public LinkedList<Double> getSlidingPunctualValues() {
		return this.slidingPunctualValues;
	}
	
	/**
	 * Returns the list of last N values in the average series
	 * 
	 * @return the list of last N values in the average series
	 */
	public LinkedList<Double> getSlidingAverage() {
		return this.slidingMovingAverage;
	}
	
	/**
	 * Returns the list of last N values in the top boundary series
	 * 
	 * @return the list of last N values in the top boundary series
	 */
	public LinkedList<Double> getSlidingTopBoundary() {
		return this.slidingTopBoundary;
	}
	
	/**
	 * Returns the list of last N values in the bottom boundary series
	 * 
	 * @return the list of last N values in the bottom boundary series
	 */
	public LinkedList<Double> getSlidingBottomBoundary() {
		return this.slidingBottomBoundary;
	}
	
	/**
	 * Returns the last value of the punctual values series
	 * 
	 * @return the last value of the punctual values series
	 */
	public double getCurrentPunctualValue() {
		if(!this.slidingPunctualValues.isEmpty()) {
			return this.slidingPunctualValues.getLast();
		}
		return 0f;
	}
	
	/**
	 * Returns the last value of the average series
	 * 
	 * @return the last value of the average series
	 */
	public double getCurrentAverage() {
		if(!this.slidingMovingAverage.isEmpty()) {
			return this.slidingMovingAverage.getLast();
		}
		return 0f;	
	}
	
	/**
	 * Returns the last value of the top boundary series
	 * 
	 * @return the last value of the top boundary series
	 */
	public double getCurrentTopBoundary() {
		if(!this.slidingTopBoundary.isEmpty()) {
			return this.slidingTopBoundary.getLast();
		}
		return 0f;
	}
	
	/**
	 * Returns the last value of the bottom boundary series
	 * 
	 * @return the last value of the bottom boundary series
	 */
	public double getCurrentBottomBoundary() {
		if(!this.slidingBottomBoundary.isEmpty()) {
			return this.slidingBottomBoundary.getLast();
		}
		return 0f;
	}
	
	/**
	 * Returns the number of values in the sliding values series
	 * 
	 * @return the number of values in the sliding values series
	 */
	public int getNumSlidingPunctualValues() {
		return this.slidingPunctualValues.size();
	}

	/**
	 * Returns the number of values in the punctual values series
	 * 
	 * @return the number of values in the punctual values series
	 */
	public int getNumPunctualValues() {
		return this.punctualValues.size();
	}
	
	/**
	 * Returns <tt>true</tt> if the performance range has a new
	 * value to be plotted
	 * 
	 * @return <tt>true</tt> if the performance range has a new
	 * 					value to be plotted
	 */
	public boolean hasNewValue() {
		return this.hasNewValue;
	}
	
	/**
	 * Sets the boolean flag {@code hasNewValue}, which indicates if the 
	 * performance range has a new value to be plotted
	 * 
	 * @param newValue
	 */
	public void setNewValue(boolean newValue) {
		this.hasNewValue = newValue;
	}
	
	//---------------------------------------------------------------------------
	// Private methods to compute series
	//---------------------------------------------------------------------------
	
	/**
	 * Adds a value to the given performance range series
	 * 
	 * @param series the series 
	 * @param slidingSeries the sliding window series
	 */
	private void add(List<Double> series, LinkedList<Double> slidingSeries,
			double value) {
		
		series.add(value);
		slidingSeries.offer(value);
	}
	
	/**
	 * Returns the sum of the punctual values series
	 * 
	 * @return the sum of the punctual values series
	 */
	private double getSum(){
		double sum = 0;

		if(this.punctualValues.size() == 0)
			return 0f;

		Iterator<Double> it = slidingPunctualValues.listIterator();
		while(it.hasNext()){
			sum+=it.next();
		}
		return sum;
	}
	
	/**
	 * Returns the average of the punctual values series
	 * 
	 * @return the average of the punctual values series
	 */
	private double getAvg(){
		int numSlidingValues = this.slidingPunctualValues.size();
		double sum = this.getSum();
		if(numSlidingValues == 0) {
			return 0f;
		}
		double ret = sum / (double) numSlidingValues;
		return ret;
	}
	
	/**
	 * Returns the standard deviation of punctual values series
	 * 
	 * @return
	 */
	private double getStdDev() {
		double stdDev = Math.sqrt(this.getVar());
		return (double) stdDev;
	}

	/**
	 * Returns the variance of the punctual values series
	 * 
	 * @return the variance of the punctual values series
	 */
	private double getVar() {
		int numSlidingValues = this.slidingPunctualValues.size();
		double var = 0f;

		for(Double num : this.slidingPunctualValues) {
			var += Math.pow((num - slidingMovingAverage.getLast()), 2);
		}
		var /= numSlidingValues;
		return var;
	}
}
