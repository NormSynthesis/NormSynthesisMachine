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
import org.jfree.util.ShapeUtilities;

import es.csic.iiia.nsm.NormSynthesisMachine;
import es.csic.iiia.nsm.config.Dimension;
import es.csic.iiia.nsm.config.Goal;
import es.csic.iiia.nsm.net.norm.NetworkNode;
import es.csic.iiia.nsm.net.norm.NormSynthesisNetwork;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.evaluation.PerformanceRange;
import es.csic.iiia.nsm.norm.evaluation.Utility;
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
	private PerformanceRange perfRange;
	private Dimension dim;
	private Goal goal;
	private NetworkNode node;
	
	private BasicStroke dottedStroke;
	private BasicStroke continuousStroke;
	private BasicStroke thresholdStroke;
	
	private JFreeChart chart;
	private XYPlot plot;
	private List<PerformanceRangeChartSeries> series;
	private XYSeriesCollection dataset;
	private XYLineAndShapeRenderer renderer;
	private List<Color> seriesColors;
	
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
		this.node = node;
		
		if(node instanceof Norm) {
			this.network = nsm.getNormativeNetwork();
		}
		else if (node instanceof NormGroup) {
			this.network = nsm.getNormGroupNetwork();
		}
		
		Utility utility = network.getUtility(node);
		
		this.goal = nsm.getNormSynthesisSettings().getSystemGoals().get(0);
		this.perfRange = utility.getPerformanceRange(dim, goal);
		
		this.dottedStroke = new BasicStroke(ACTIVE_NORM_WIDTH + 3,
				BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, 
				new float[] {2f, 100f}, 0.0f);
		
		this.continuousStroke = new BasicStroke(ACTIVE_NORM_WIDTH);
		
		this.thresholdStroke = new BasicStroke(ACTIVE_NORM_WIDTH + 3,
				BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, 
				new float[] {4f, 50f}, 0.0f);
		
		this.seriesColors = new ArrayList<Color>();
		this.seriesColors.add(Color.RED);
		this.seriesColors.add(Color.BLUE);
		this.seriesColors.add(Color.YELLOW);
		this.seriesColors.add(Color.GREEN);
		this.seriesColors.add(Color.BLACK);
		
		/* Create chart's elements */
		this.createChart();
	}

	/**
	 * Creates the chart that shows the performance range
	 * of the given {@code node}
	 * 
	 * @param node the given node
	 */
	private void createChart() {
		String xLabel, yLabel;
		xLabel = "Num Evaluation";
		yLabel = "Score";
		
		PerformanceRangeChartSeries punctualValuesSeries = 
				this.createSeries(UtilityChartSeriesType.PunctualValue);
		
		/* Add punctual values series */
		this.dataset = new XYSeriesCollection(punctualValuesSeries);
		this.series.add(punctualValuesSeries);
		
		this.chart = ChartFactory.createXYLineChart(dim.toString(), xLabel, yLabel,
				dataset, PlotOrientation.VERTICAL, true, true, false);		

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
		
		/* Add extra series depending on the norm synthesis method */
		String strategy = this.nsm.getNormSynthesisSettings().getNormSynthesisStrategy();
		if(strategy.equals("IRON") || strategy.equals("SIMON")) {
			this.addIRONSeries(node);
		}
		else {
			this.addLIONSeries(node);
		}
	}

	/**
	 * @param punctualvalue
	 * @return
	 */
  private PerformanceRangeChartSeries createSeries(
      UtilityChartSeriesType type) {

  	PerformanceRangeChartSeries series;
  	if(type == UtilityChartSeriesType.PunctualValue) {
  		series = new PerformanceRangeChartSeries(nsm, node.toString(),
  				perfRange, dim, goal, type);
  	}
  	else {
  		series = new PerformanceRangeChartSeries(nsm, type.toString(),
  				perfRange, dim, goal, type);
  	}
  	return series;
  }

	/**
	 * 
	 */
  private void addIRONSeries(NetworkNode node) {
  	this.addSeries(this.createSeries(UtilityChartSeriesType.Average));
		this.addSeries(this.createSeries(UtilityChartSeriesType.TopBoundary));
		this.addSeries(this.createSeries(UtilityChartSeriesType.BottomBoundary));
		this.addSeries(this.createSeries(UtilityChartSeriesType.AlphaSpec));

		/* Set punctual values series style */
		renderer.setSeriesStroke(0, this.dottedStroke, false);
		renderer.setSeriesPaint(0, Color.RED);
		
		/* Set average and bollinger bands series style */
		for(int i=1; i<=3; i++) {
			renderer.setSeriesStroke(i, this.continuousStroke, false);
			renderer.setSeriesPaint(i, this.seriesColors.get(i));
		}

		/* Set threshold stroke style */
		renderer.setSeriesStroke(4, this.thresholdStroke, false);
		renderer.setSeriesPaint(4, Color.BLACK);
  }
  
	/**
	 * 
	 */
  private void addLIONSeries(NetworkNode node) {
  	this.addSeries(this.createSeries(UtilityChartSeriesType.Average));
  	this.addSeries(this.createSeries(UtilityChartSeriesType.AlphaSpecTopBand));
  	this.addSeries(this.createSeries(UtilityChartSeriesType.AlphaSpec));
  	this.addSeries(this.createSeries(UtilityChartSeriesType.AlphaSpecBottomBand));
  	
		/* Set punctual values series style */
		renderer.setSeriesStroke(0, this.dottedStroke, false);
		renderer.setSeriesPaint(1, Color.RED);
		
		/* Set average series style */
		renderer.setSeriesStroke(1, this.continuousStroke, false);
		renderer.setSeriesPaint(1, Color.BLUE);

		/* Set threshold stroke style */
		renderer.setSeriesStroke(2, this.thresholdStroke, false);
		renderer.setSeriesPaint(2, Color.BLACK);
		renderer.setSeriesStroke(3, this.continuousStroke, false);
		renderer.setSeriesPaint(3, Color.GREEN);
		renderer.setSeriesStroke(4, this.thresholdStroke, false);
		renderer.setSeriesPaint(4, Color.BLACK);
  }



	/**
	 * Adds a new series to the chart
	 * 
	 * @param node the node
	 * @param type the series type
	 */
	public void addSeries(PerformanceRangeChartSeries series) {
		this.dataset.addSeries(series);	
		this.series.add(series);
	}

	/**
	 * Refreshes the chart, updating the last values of each series in it,
	 * and showing these values in the GUI
	 */
	public void refresh() {
		for(PerformanceRangeChartSeries s : series) {
			s.update();
		}
		perfRange.setNewValue(false);
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
