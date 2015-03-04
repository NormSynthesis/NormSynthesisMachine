package es.csic.iiia.nsm.utilities;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * SlidingWinowMetric - Used to trace aggregated indicator changes over time.
 *
 * @author Jan Koeppen (jankoeppen@gmx.net)
 *
 */
public class SlidingWindowMetric {

	private boolean newValue;
	private long size = 0;
	private LinkedList<Double> queue = null;

	/**
	 * Constructor
	 * 
	 * @param size
	 */
	public SlidingWindowMetric(long size){
		queue = new LinkedList<Double>();
		this.size = size;
		this.newValue = false;
	}

	/**
	 * Adds a value to the window
	 * 
	 * @param value
	 */
	public void  addValue(double value){
		queue.offer(value);
		if(queue.size()>this.size)
			queue.remove();
		
		this.newValue = true;
	}

	/**
	 * 
	 * @return
	 */
	public boolean hasNewValue() {
		return this.newValue;
	}
	
	/**
	 * 
	 * @param newValue
	 */
	public void setNewValue(boolean newValue) {
		this.newValue = newValue;
	}
	
	/**
	 * 
	 * @return
	 */
	public double getLastValue() {
		return this.queue.getLast();
	}

	/**
	 * Returns the total sum of the values in the window
	 * 
	 * @return
	 */
	public double getSum(){
		double sum = 0;

		if(this.getNumvalues() == 0)
			return 0f;

		Iterator<Double> it = queue.listIterator();
		while(it.hasNext()){
			sum+=it.next();
		}
		return sum;
	}

	/**
	 * Returns the total sum of the values in the window
	 * 
	 * @return
	 */
	public double getSumUntil(int idx){
		double sum = 0;
		int i = 0;
		
		if(this.getNumvalues() == 0)
			return 0f;

		Iterator<Double> it = queue.listIterator();
		while(it.hasNext() && i<=idx){
			sum+=it.next();
			i++;
		}
		return sum;
	}
	
	/**
	 * 
	 * @return
	 */
	public double getStdDev() {
		double stdDev = Math.sqrt(this.getVar());
		return (double) stdDev;
	}
	
	/**
	 * 
	 * @return
	 */
	public double getStdDevUntil(int idx) {
		double stdDev = Math.sqrt(this.getVarUntil(idx));
		return (double) stdDev;
	}
	
	/**
	 * 
	 * @return
	 */
	public double getVar() {
		int numValues = this.getNumvalues();
		double avg = this.getAvg();
		double var = 0f;

		for(Double num : queue) {
			var += Math.pow((num - avg), 2);
		}
		var /= numValues;
		return var;
	}

	/**
	 * 
	 * @return
	 */
	public double getVarUntil(int idx) {
		int numValues = idx+1;
		int i =0 ;
		double avg = this.getAvgUntil(idx);
		double var = 0f;

		for(Double num : queue) {
			if(i>idx)
				break;
			
			var += Math.pow((num - avg), 2);
			i++;
		}
		var /= numValues;
		return var;
	}
	
	/**
	 * 
	 * @return
	 */
	public double getAvg(){
		double sum = this.getSum();
		int numValues = this.getNumvalues();

		if(numValues == 0)
			return 0f;

		double ret = sum / (double) numValues;
		return ret;
	}
	
	/**
	 * 
	 * @return
	 */
	public double getMedian() {
		long size = this.queue.size();
		int middle = this.queue.size() / 2;
    if (size % 2 == 1) {
        return queue.get(middle);
    } else {
        return (queue.get(middle-1) + queue.get(middle)) / 2.0;
    }
	}
	
	/**
	 * 
	 * @return
	 */
	public double getAvgUntil(int idx){
		double sum = this.getSumUntil(idx);
		int numValues = idx+1;

		if(numValues == 0)
			return 0f;

		double ret = sum / (double) numValues;
		return ret;
	}

	/**
	 * 
	 * @return
	 */
	public long getSize() {
		return size;
	}

	/**
	 * Returns the sum of the positive values in the list
	 *   
	 * @return the sum
	 */
	public double getPosNumSum() {
		double ret = 0;

		for(double value : queue) {
			if(value >= 0) {
				ret += value;
			}
		}
		return ret;
	}

	/**
	 * Returns the sum of the positive values in the list
	 *   
	 * @return the sum
	 */
	public double getNegNumSum() {
		double ret = 0;

		for(double value : queue) {
			if(value < 0) {
				ret += value;
			}
		}
		return ret;
	}

	/**
	 * Returns the number of values in the window
	 * 
	 * @return
	 */
	public int getNumvalues() {
		int c = 0;
		for(Double f : queue) {
			c++;
		}
		return c;
	}
	
	/**
	 * 
	 * @param i
	 * @return
	 */
	public double getValue(int i) {
		return this.queue.get(i);
	}

	/**
	 * Returns the number of values under a certain value
	 *   
	 * @return the sum
	 */
	public double getNumValuesUnder(double value) {
		double ret = 0;

		for(double val : queue) {
			if(val < value) {
				ret += val;
			}
		}
		return ret;
	}

	/**
	 * Returns the number of values over a certain value
	 *   
	 * @return the sum
	 */
	public double getNumValuesOver(double value) {
		double ret = 0;

		for(double val : queue) {
			if(val >= value) {
				ret += val;
			}
		}
		return ret;
	}

	/**
	 * Returns the number of values in the window
	 * 
	 * @return
	 */
	public String toString() {
		return queue.toString();
	}
	
	/**
	 * 
	 * @return
	 */
	public long size() {
		return this.size;
	}
}
