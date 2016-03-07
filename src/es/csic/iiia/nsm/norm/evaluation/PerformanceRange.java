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
	
	private List<Float> punctualValues;
	private List<Float> average;
	private List<Float> topBoundary;
	private List<Float> bottomBoundary;
	
	private LinkedList<Float> slidingPunctualValues;
	private LinkedList<Float> slidingAverage;
	private LinkedList<Float> slidingTopBoundary;
	private LinkedList<Float> slidingBottomBoundary;
	
	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------

	/**
	 * Constructor
	 * 
	 * @param size
	 */
	public PerformanceRange(long size){
		this.punctualValues = new ArrayList<Float>();
		this.average = new ArrayList<Float>();
		this.topBoundary = new ArrayList<Float>();
		this.bottomBoundary = new ArrayList<Float>();
		this.slidingPunctualValues = new LinkedList<Float>();
		this.slidingAverage = new LinkedList<Float>();
		this.slidingTopBoundary = new LinkedList<Float>();
		this.slidingBottomBoundary = new LinkedList<Float>();
		
		this.maxSlidingValues = size;
		this.hasNewValue = false;
	}

	/**
	 * Adds a value to the window
	 * 
	 * @param value
	 */
	public void addValue(float value) {
		this.hasNewValue = true;
		
		/* Add new punctual value */
		this.add(punctualValues, slidingPunctualValues, value);

		/* Compute and add new average value */
		float avg = this.getAvg();
		this.add(average, slidingAverage, avg);
		
		/* Compute and add new boundaries values */
		float stdDev = this.getStdDev();
		float topBnd = avg + stdDev;
		float btmBnd = avg - stdDev;
		this.add(topBoundary, slidingTopBoundary, topBnd);
		this.add(bottomBoundary, slidingBottomBoundary, btmBnd);
		
		/* Remove old values */
		if(this.slidingPunctualValues.size() > this.maxSlidingValues) {
			this.slidingPunctualValues.remove();
		}
		if(this.slidingAverage.size() > this.maxSlidingValues) {
			this.slidingAverage.remove();
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
	public List<Float> getPunctualValues() {
		return this.punctualValues;
	}
	
	/**
	 * Returns the list of all values in the average series
	 * 
	 * @return the list of all values in the average series
	 */
	public List<Float> getAverage() {
		return this.average;
	}
	
	/**
	 * Returns the list of all values in the top boundary series
	 * 
	 * @return the list of all values in the top boundary series
	 */
	public List<Float> getTopBoundary() {
		return this.topBoundary;
	}
	
	/**
	 * Returns the list of all values in the bottom boundary series
	 * 
	 * @return the list of all values in the bottom boundary series
	 */
	public List<Float> getBottomBoundary() {
		return this.bottomBoundary;
	}
	
	/**
	 * Returns the list of last N values in the punctual values series
	 * 
	 * @return the list of last N values in the punctual values series
	 */
	public LinkedList<Float> getSlidingPunctualValues() {
		return this.slidingPunctualValues;
	}
	
	/**
	 * Returns the list of last N values in the average series
	 * 
	 * @return the list of last N values in the average series
	 */
	public LinkedList<Float> getSlidingAverage() {
		return this.slidingAverage;
	}
	
	/**
	 * Returns the list of last N values in the top boundary series
	 * 
	 * @return the list of last N values in the top boundary series
	 */
	public LinkedList<Float> getSlidingTopBoundary() {
		return this.slidingTopBoundary;
	}
	
	/**
	 * Returns the list of last N values in the bottom boundary series
	 * 
	 * @return the list of last N values in the bottom boundary series
	 */
	public LinkedList<Float> getSlidingBottomBoundary() {
		return this.slidingBottomBoundary;
	}
	
	/**
	 * Returns the last value of the punctual values series
	 * 
	 * @return the last value of the punctual values series
	 */
	public float getCurrentPunctualValue() {
		return this.slidingPunctualValues.getLast();
	}
	
	/**
	 * Returns the last value of the average series
	 * 
	 * @return the last value of the average series
	 */
	public float getCurrentAverage() {
		return this.slidingAverage.getLast();
	}
	
	/**
	 * Returns the last value of the top boundary series
	 * 
	 * @return the last value of the top boundary series
	 */
	public float getCurrentTopBoundary() {
		return this.slidingTopBoundary.getLast();
	}
	
	/**
	 * Returns the last value of the bottom boundary series
	 * 
	 * @return the last value of the bottom boundary series
	 */
	public float getCurrentBottomBoundary() {
		return this.slidingBottomBoundary.getLast();
	}
	
	/**
	 * Returns the number of values in the sliding series
	 * 
	 * @return the number of values in the sliding series
	 */
	public int getNumSlidingValues() {
		return this.slidingPunctualValues.size();
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
	private void add(List<Float> series, LinkedList<Float> slidingSeries,
			float value) {
		
		series.add(value);
		slidingSeries.offer(value);
	}
	
	/**
	 * Returns the sum of the punctual values series
	 * 
	 * @return the sum of the punctual values series
	 */
	private float getSum(){
		float sum = 0;

		if(this.punctualValues.size() == 0)
			return 0f;

		Iterator<Float> it = slidingPunctualValues.listIterator();
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
	private float getAvg(){
		int numSlidingValues = this.slidingPunctualValues.size();
		float sum = this.getSum();
		if(numSlidingValues == 0) {
			return 0f;
		}
		float ret = sum / (float) numSlidingValues;
		return ret;
	}
	
	/**
	 * Returns the standard deviation of punctual values series
	 * 
	 * @return
	 */
	private float getStdDev() {
		double stdDev = Math.sqrt(this.getVar());
		return (float) stdDev;
	}

	/**
	 * Returns the variance of the punctual values series
	 * 
	 * @return the variance of the punctual values series
	 */
	private float getVar() {
		int numSlidingValues = this.slidingPunctualValues.size();
		float var = 0f;

		for(Float num : this.slidingPunctualValues) {
			var += Math.pow((num - slidingAverage.getLast()), 2);
		}
		var /= numSlidingValues;
		return var;
	}
}
