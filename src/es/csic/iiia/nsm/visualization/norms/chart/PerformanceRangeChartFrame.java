package es.csic.iiia.nsm.visualization.norms.chart;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.ui.ApplicationFrame;

import es.csic.iiia.nsm.NormSynthesisMachine;
import es.csic.iiia.nsm.config.Dimension;
import es.csic.iiia.nsm.config.Goal;
import es.csic.iiia.nsm.net.norm.NetworkNode;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.group.NormGroup;

/**
 * A frame that shows the utility of a norm or a norm group
 * with respect to a goal
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 * @see PerformanceRange
 */
public class PerformanceRangeChartFrame extends ApplicationFrame {

	//---------------------------------------------------------------------------
	// Static attributes
	//---------------------------------------------------------------------------
	
	private static final long serialVersionUID = 8286435099181955453L;

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------
	
	private final int CHART_WIDTH = 550;
	private final int CHART_HEIGHT = 400;

	private List<PerformanceRangeChart> charts;

	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * 
	 * @param nsm the norm synthesis machine
	 * @param goal the goal from which to show the utility
	 * @param node the node from which to show the utility
	 */
	public PerformanceRangeChartFrame(NormSynthesisMachine nsm, NetworkNode node) {
		
		super("Norm scores for goal GCols");
		this.charts = new ArrayList<PerformanceRangeChart>();

		if(node instanceof Norm) {
			for(Dimension dim : nsm.getNormEvaluationDimensions()) {
				this.charts.add(new PerformanceRangeChart(nsm, dim, node));
			}	
		}
		else if (node instanceof NormGroup) {
			this.charts.add(new PerformanceRangeChart(
					nsm, Dimension.Effectiveness, node));
		}
		
		this.initComponents();
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	/**
	 * Initialises the chart's components
	 */
	private void initComponents() {
		JPanel content = new JPanel(new GridLayout());
		ChartPanel chartPanel;

		for(PerformanceRangeChart chart : this.charts) {
			chartPanel = new ChartPanel(chart.getChart());
			content.add(chartPanel);
			chartPanel.setPreferredSize(new java.awt.Dimension(CHART_WIDTH, CHART_HEIGHT));
		}
		setContentPane(content);
	}

	/**
	 * Refreshes the chart's components
	 */
	public void refresh() {
		for(PerformanceRangeChart chart : this.charts) {
			chart.refresh();
		}
	}
}
