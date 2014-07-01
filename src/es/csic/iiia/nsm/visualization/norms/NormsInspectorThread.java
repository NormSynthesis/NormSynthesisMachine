package es.csic.iiia.nsm.visualization.norms;

import es.csic.iiia.nsm.net.norm.NormativeNetwork;
import es.csic.iiia.nsm.visualization.NormSynthesisInspector;

/**
 * The thread that creates and updates the NSM norms tracer
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
public class NormsInspectorThread extends Thread {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------
	
//	private NormsTracerFrame traceFrame;
	private NormsInspectorFrame inspectorFrame;
	private boolean refresh;
	
	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * 
	 * @param trafficInstitutions
	 */
	public NormsInspectorThread(NormSynthesisInspector manager,
			NormativeNetwork normativeNetwork) {
		
//		this.traceFrame = new NormsTracerFrame(manager);
		this.inspectorFrame = new NormsInspectorFrame(manager);
		this.refresh = false;
	}

	/**
	 * Allow the GUI to be updated
	 */
	public void allowRefreshing() {
		synchronized(this) {
			this.refresh = true;
		}
	}
	
	/**
	 * Updates the trace frame to the infinite
	 */
	@Override
	public void run() {
//		this.traceFrame.setVisible(true);
		this.inspectorFrame.setVisible(true);
		
		while(true) {
			if(this.refresh) {
				this.inspectorFrame.refresh();
			}
			
			try {
	      Thread.currentThread();
				Thread.sleep(Integer.MAX_VALUE);

      } catch (InterruptedException e) {
      }
		}
	}
}
