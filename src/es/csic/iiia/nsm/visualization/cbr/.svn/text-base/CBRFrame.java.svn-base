package es.csic.iiia.nsm.visualization.cbr;

import javax.swing.JFrame;

import es.csic.iiia.nsm.NormSynthesisMachine;

/**
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 *
 */
public class CBRFrame extends JFrame {

  //---------------------------------------------------------------------------
  // Static attributes
  //---------------------------------------------------------------------------

	private static final long serialVersionUID = 2344965818344529036L;

  //---------------------------------------------------------------------------
  // Attributes
  //---------------------------------------------------------------------------
  
  private CBRPanel panel;
  
  //---------------------------------------------------------------------------
  // Methods
  //---------------------------------------------------------------------------
  
  /**
   * 
   * @param iron
   */
  public CBRFrame(NormSynthesisMachine nsm) {
  	panel = new CBRPanel();
  	this.add(panel);
  	
  	this.setSize(panel.getSize());
  	this.pack();
  }
}
