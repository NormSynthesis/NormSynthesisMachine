/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.csic.iiia.nsm.visualization.norms;

import javax.swing.JFrame;

import es.csic.iiia.nsm.visualization.NormSynthesisInspector;

/**
 *
 * @author javi
 */
public class NormsInspectorFrame extends JFrame {

	private NormsInspectorPanel inspectorPanel;
	/**
	 * 
	 * @param nsInspector
	 */
	public NormsInspectorFrame(NormSynthesisInspector nsInspector) {
		inspectorPanel = new NormsInspectorPanel(nsInspector);
		this.add(inspectorPanel);
		this.setSize(inspectorPanel.getSize());
		this.pack();
	}
	
	/**
	 * Refreshes the window if auto update is activated
	 */
	public void refresh() {
//		this.inspectorPanel
	}
}
