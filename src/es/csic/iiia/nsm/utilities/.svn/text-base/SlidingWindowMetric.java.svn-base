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
	private int size = 0;
	private LinkedList<Float> queue = null;

	/**
	 * Constructor
	 * 
	 * @param size
	 */
	public SlidingWindowMetric(int size){
		queue = new LinkedList<Float>();
		this.size = size;
		this.newValue = false;
	}

	/**
	 * Adds a value to the window
	 * 
	 * @param value
	 */
	public void  addValue(float value){
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
	public float getLastValue() {
		return this.queue.getLast();
	}

	/**
	 * Returns the total sum of the values in the window
	 * 
	 * @return
	 */
	public float getSum(){
		float sum = 0;

		if(this.getNumvalues() == 0)
			return 0f;

		Iterator<Float> it = queue.listIterator();
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
	public float getSumUntil(int idx){
		float sum = 0;
		int i = 0;
		
		if(this.getNumvalues() == 0)
			return 0f;

		Iterator<Float> it = queue.listIterator();
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
	public float getStdDev() {
		double stdDev = Math.sqrt(this.getVar());
		return (float) stdDev;
	}
	
	/**
	 * 
	 * @return
	 */
	public float getStdDevUntil(int idx) {
		double stdDev = Math.sqrt(this.getVarUntil(idx));
		return (float) stdDev;
	}
	
	/**
	 * 
	 * @return
	 */
	public float getVar() {
		int numValues = this.getNumvalues();
		float avg = this.getAvg();
		float var = 0f;

		for(Float num : queue) {
			var += Math.pow((num - avg), 2);
		}
		var /= numValues;
		return var;
	}

	/**
	 * 
	 * @return
	 */
	public float getVarUntil(int idx) {
		int numValues = idx+1;
		int i =0 ;
		float avg = this.getAvgUntil(idx);
		float var = 0f;

		for(Float num : queue) {
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
	public float getAvg(){
		float sum = this.getSum();
		int numValues = this.getNumvalues();

		if(numValues == 0)
			return 0f;

		float ret = sum / (float) numValues;
		return ret;
	}
	
	/**
	 * 
	 * @return
	 */
	public float getAvgUntil(int idx){
		float sum = this.getSumUntil(idx);
		int numValues = idx+1;

		if(numValues == 0)
			return 0f;

		float ret = sum / (float) numValues;
		return ret;
	}

	/**
	 * 
	 * @return
	 */
	public int getSize() {
		return size;
	}

	/**
	 * Returns the sum of the positive values in the list
	 *   
	 * @return the sum
	 */
	public float getPosNumSum() {
		float ret = 0;

		for(float value : queue) {
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
	public float getNegNumSum() {
		float ret = 0;

		for(float value : queue) {
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
		for(Float f : queue) {
			c++;
		}
		return c;
	}
	
	/**
	 * 
	 * @param i
	 * @return
	 */
	public float getValue(int i) {
		return this.queue.get(i);
	}

	/**
	 * Returns the number of values under a certain value
	 *   
	 * @return the sum
	 */
	public float getNumValuesUnder(float value) {
		float ret = 0;

		for(float val : queue) {
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
	public float getNumValuesOver(float value) {
		float ret = 0;

		for(float val : queue) {
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
	public int size() {
		return this.size;
	}
}
