package es.csic.iiia.nsm.utilities;

import java.util.List;

/**
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 *
 */
public class Minkowski {

	//---------------------------------------------------------------------------
	// Attributes 
	//---------------------------------------------------------------------------

	private double P;

	//---------------------------------------------------------------------------
	// Methods 
	//---------------------------------------------------------------------------

	/** Constructor for Minkowski metric.
	 *
	 * @param q Power of component wise absolute difference; must be at least 1
	 */
	public Minkowski(double P) {
		if (P<1) {
			throw new IllegalArgumentException("Argument q must be at least 1.");
		}
		this.P = P;
	}

	/** 
	 * Gives the Minkowski distance between two performance ranges
	 * <p>
	 * For 1 <= q < infinity:
	 * distance(x,y) := \left( \Sum_i=0^d-1 \left| x_i - y_i \right|^q \right)^\frac{1}{q}
	 * <p>
	 * For q = infinity
	 * distance(x,y) := max_i \left| x_i - y_i \right|
	 * 
	 * @param a the first performance range
	 * @param b the second performance range
	 */
	public double distance(List<Double> a, List<Double> b, int numValues)
	throws IllegalArgumentException {
		
		double dist = 0;
		double diff;

		if (a==null || b==null) {
			throw new IllegalArgumentException("Distance from a null vector is undefined.");
		}
		
		for (int i=0 ; i< numValues ; i++) {
			diff = Math.abs( a.get(i) - b.get(i));

			if (P==1) {
				dist += diff;
			}
			else if (P==2) {
				dist += diff*diff;
			}
			else if (P==Double.POSITIVE_INFINITY) {
				if (diff > dist) {
					dist = diff;
				}
				else {
					dist += Math.pow(diff, P);
				}
			}
		}

		/* */
		if (P==1 || P==Double.POSITIVE_INFINITY) {
			return dist;
		}
		else if (P==2) {
			return Math.sqrt(dist);
		}
		else {
			return Math.pow( dist, 1/P);
		}
	}
}
