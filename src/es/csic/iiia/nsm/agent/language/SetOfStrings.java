package es.csic.iiia.nsm.agent.language;

import java.util.ArrayList;

/**
 * A set of strings, implemented by means of an {@code ArrayList<String>}.
 * The set of strings cannot contain duplicate strings.
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
public class SetOfStrings extends ArrayList<String> {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------
	
	private static final long serialVersionUID = 8835836609572123951L;

	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------
	
	/**
	 * Adds a {@code String} to this set of strings
	 * 
	 * @return <tt>true</tt> if the {@code string} was added 
	 * 
	 */
	public boolean add(String string)  {
		if(!this.contains(string)) {
			super.add(string);
			return true;
		}
		return false;
	}	
	
	/**
	 * Returns <tt>true</tt> if the set contains each one of the
	 * {@code strings} received by parameter
	 * 
	 * @param strings the strings to search
	 * @return <tt>true</tt> if the set contains all the {@code strings}
	 */
	public boolean contains(SetOfStrings strings) {
		for(String t : strings) {
			if(!this.contains(t)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Returns <tt>true</tt> if this set and the {@code otherSet}
	 * contain the same strings
	 * 
	 * @param otherSet the other set to compare this with
	 * @return <tt>true</tt> if this set and the {@code otherSet}
	 * 					contain the same strings
	 */
	public boolean equals(SetOfStrings otherSet) {
		int numStringsHere = this.size();
		int numStringsThere = otherSet.size();
		
		/* Check that the two sets have the same number of strings */
		if(numStringsHere != numStringsThere) {
			return false;
		}
		/* Check that all the strings in this set exist in the other set */
		for(int i=0; i<numStringsHere; i++) {
			String term = this.get(i);
			if(!otherSet.contains(term)) {
				return false;
			}
		}
		/* Check that all the strings in the other set exist in this set */
		for(int i=0; i<numStringsThere; i++) {
			String term = this.get(i);
			if(!this.contains(term)) {
				return false;
			}
		}
		return true;
	}
}
