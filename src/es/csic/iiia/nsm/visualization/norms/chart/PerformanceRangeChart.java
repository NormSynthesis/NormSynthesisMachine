package es.csic.iiia.nsm.visualization.norms.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeriesCollection;

import es.csic.iiia.nsm.NormSynthesisMachine;
import es.csic.iiia.nsm.config.Dimension;
import es.csic.iiia.nsm.net.norm.NetworkNode;
import es.csic.iiia.nsm.net.norm.NormSynthesisNetwork;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.evaluation.PerformanceRange;
import es.csic.iiia.nsm.norm.group.NormGroup;
import es.csic.iiia.nsm.visualization.norms.chart.PerformanceRangeChartSeries.UtilityChartSeriesType;

/**
 * A performance range chart shows the performance range of a norm or a 
 * norm group in terms of a system {@code Goal}. In the case of a {@code Norm},
 * the chart shows the performance range of the norm in terms of two dimension: 
 * effectiveness and necessity. In the case of a {@code NormGroup}, the chart
 * shows the performance range in terms of the effectiveness of the group
 * of norms to avoid conflicts
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 * @see PerformanceRange
 */
public class PerformanceRangeChart {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------

	private final int ACTIVE_NORM_WIDTH = 2;

	private NormSynthesisMachine nsm;
	private NormSynthesisNetwork network; 
	private Dimension dim;

	private JFreeChart chart;
	private XYPlot plot;
	private List<PerformanceRangeChartSeries> series;
	private XYSeriesCollection dataset;
	private XYLineAndShapeRenderer renderer;

	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------

	/**
	 * Constructor with parameters
	 * 
	 * @param nsm the norm synthesis machine
	 * @param dim the norm evaluation dimension
	 * @param goal the system goal
	 * @param node the node of which to show the utility
	 */
	public PerformanceRangeChart(NormSynthesisMachine nsm, Dimension dim, 
			NetworkNode node) {

		this.series = new ArrayList<PerformanceRangeChartSeries>();
		this.dim = dim;
		this.nsm = nsm;

		if(node instanceof Norm) {
			this.network = nsm.getNormativeNetwork();
		}
		else if (node instanceof NormGroup) {
			this.network = nsm.getNormGroupNetwork();
		}
		this.createChart(node);
	}

	/**
	 * Creates the chart that shows the performance range
	 * of the given {@code node}
	 * 
	 * @param node the given node
	 */
	private void createChart(NetworkNode node) {
		String xLabel, yLabel;
		xLabel = "Num Evaluation";
		yLabel = "Score";

		PerformanceRangeChartSeries nwSeries = 
				new PerformanceRangeChartSeries(nsm, network, node, dim);

		this.dataset = new XYSeriesCollection(nwSeries);
		this.series.add(nwSeries);

		this.chart = ChartFactory.createXYLineChart(dim.toString(),
				xLabel, yLabel, dataset, 
				PlotOrientation.VERTICAL,   
				true,    
				true,
				false);		

		this.chart.setBackgroundPaint(Color.white);
		this.plot = chart.getXYPlot();
		this.plot.setBackgroundPaint(Color.lightGray);
		this.plot.setDomainGridlinePaint(Color.white);
		this.plot.setRangeGridlinePaint(Color.white);

		this.renderer = new XYLineAndShapeRenderer(true, false);
		this.plot.setRenderer(renderer);

		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();   
		rangeAxis.setAutoRangeIncludesZero(false);

		NumberAxis axis = (NumberAxis) plot.getDomainAxis();
		axis.setFixedAutoRange(this.nsm.getNormSynthesisSettings().
				getNormsPerformanceRangesSize());

		/* Add also series for average and standard deviation */
		this.addSeries(node, UtilityChartSeriesType.Average);
		this.addSeries(node, UtilityChartSeriesType.TopBoundary);
		this.addSeries(node, UtilityChartSeriesType.BottomBoundary);
		this.addSeries(node, UtilityChartSeriesType.AlphaSpec);
		this.addSeries(node, UtilityChartSeriesType.AlphaGen);

		/* Set all series stroke */
		for(int i=0; i<4; i++) {
			renderer.setSeriesStroke(i, new BasicStroke(ACTIVE_NORM_WIDTH), false);
		}

		for(int i=4; i<5; i++) {
			renderer.setSeriesStroke(i, new BasicStroke(
					ACTIVE_NORM_WIDTH + 3,BasicStroke.CAP_BUTT, 
					BasicStroke.JOIN_MITER, 1.0f, new float[] {2f, 100f}, 0.0f), false);
			renderer.setSeriesPaint(i, Color.RED);
		}

		for(int i=5; i<6; i++) {
			renderer.setSeriesStroke(i, new BasicStroke(
					ACTIVE_NORM_WIDTH + 2,BasicStroke.CAP_BUTT, 
					BasicStroke.JOIN_MITER, 1.0f, new float[] {2f, 100f}, 0.0f), false);
			renderer.setSeriesPaint(i, new Color(0,100,0));
		}
	}

	/**
	 * Adds a new series to the chart
	 * 
	 * @param node the node
	 * @param type the series type
	 */
	public void addSeries(NetworkNode node,
			UtilityChartSeriesType type) {
		PerformanceRangeChartSeries nwSeries = 
				new PerformanceRangeChartSeries(nsm, network, node, dim, type);

		this.series.add(nwSeries);
		this.dataset.addSeries(nwSeries);
	}

	/**
	 * Refreshes the chart, updating the last values of each series in it,
	 * and showing these values in the GUI
	 */
	public void refresh() {
		for(PerformanceRangeChartSeries s : series) {
			s.update();
		}
	}

	/**
	 * Returns the chart
	 * 
	 * @return the chart
	 * @see JFreeChart
	 */
	public JFreeChart getChart() {
		return this.chart;
	}
}
