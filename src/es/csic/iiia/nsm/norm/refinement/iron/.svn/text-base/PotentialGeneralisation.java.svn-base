package es.csic.iiia.nsm.norm.refinement.iron;

import java.util.List;

import es.csic.iiia.nsm.agent.AgentAction;
import es.csic.iiia.nsm.agent.language.SetOfPredicatesWithTerms;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.NormModality;

/**
 * An potential generalisation of IRON is a generalisation that the NSM 
 * may potentially perform for a particular group of norms. Each potential
 * generalisation contains:
 * <ol>
 * <li> a potential <tt>parent</tt> norm that may be created if a set of child
 * 			norms fulfil certain conditions to be generalised to the parent; and
 * <li>	a {@code List} of <tt>children</tt>, namely a list of child norms that
 * 			must exist and perform well to be generalised to a parent norm.
 * </ol>
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 * @see GeneralisationReasoner
 * @see GeneralisationTrees
 */
public class PotentialGeneralisation {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------
	
	private Norm parent;
	private List<Norm> children;
	private boolean performed;

	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * 
	 * @param parent the potential parent
	 * @param children the children that are necessary to generalise
	 */
	public PotentialGeneralisation(Norm parent, List<Norm> children) {
		this.parent = parent;
		this.children = children;
		this.performed = false;
	}

	/**
	 * Returns the parent of this potential generalisation
	 * 
	 * @return the parent of this potential generalisation
	 */
	public Norm getParent() {
		return this.parent;
	}
	
	/**
	 * Returns a {@code List} of the children in this potential generalisation
	 * 
	 * @return a {@code List} of the children in this potential generalisation
	 */
	public List<Norm> getChildren() {
		return this.children;
	}
	
	/**
	 * Returns <tt>true</tt> if this potential generalisation has been performed,
	 * namely the parent has been added to the normative network and activated, 
	 * and its children have been deactivated in the normative network
	 * 
	 * @return <tt>true</tt> if this potential generalisation has been performed,
	 * 					namely the parent has been added to the normative network 
	 * 					and activated, and its children have been deactivated in the
	 * 					normative network
	 */
	public boolean isPerformed() {
		return this.performed;
	}
	
	/**
	 * Sets the {@code performed} attribute of this potential generalisation
	 * 
	 * @param performed the boolean flag
	 */
	public void setPerformed(boolean performed)	{
		this.performed = performed;
	}
	
	/**
	 * Returns <tt>true</tt> if this potential generalisation has the same
	 * parent and children than the other potential generalisation
	 * {@code otherPotGen}
	 * 
	 * @param otherPotGen the other potential generalisation
	 * @return <tt>true</tt> if this potential generalisation has the same
	 * parent and children than the other potential generalisation
	 * {@code otherPotGen}
	 */
	public boolean equals(PotentialGeneralisation otherPotGen) {
		SetOfPredicatesWithTerms pPrecond = otherPotGen.getParent()
				.getPrecondition();
		NormModality pModality = otherPotGen.getParent().getModality();
		AgentAction pAction = otherPotGen.getParent().getAction();
		
		if(!this.parent.getPrecondition().equals(pPrecond) ||
				this.parent.getModality() != pModality ||
				this.parent.getAction() != pAction) {
			return false;
		}
			
		for(Norm cNode : otherPotGen.getChildren()) {	
			if(!this.containsChild(cNode)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Returns <tt>true</tt> if this potential generalisation contains 
	 * a given {@code norm} in the list of {@code children}
	 * 
	 * @param norm the norm to search for in the child norms list
	 * @return <tt>true</tt> if this potential generalisation contains 
	 * 					a given {@code norm} in the list of {@code children}
	 */
	public boolean containsChild(Norm norm) {		
		for(Norm child : this.children) {			
			SetOfPredicatesWithTerms cPrecond = norm.getPrecondition();
			NormModality cModality = norm.getModality();
			AgentAction cAction = norm.getAction();
			
			if(child.getPrecondition().equals(cPrecond) &&
					child.getModality() == cModality &&
					child.getAction() == cAction) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns a string describing the potential generalisation
	 */
	@Override
	public String toString() {
		String s = parent + ":\t[[ ";
		int c = 0;
		
		for(Norm child : children) {
			c++;
			s += child;
			if(c < children.size())
				s += ", ";
		}
		s += " ]]";
		
		return s;
	}
}
