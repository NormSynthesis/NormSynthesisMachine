package es.csic.iiia.nsm;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import es.csic.iiia.nsm.agent.language.PredicatesDomains;
import es.csic.iiia.nsm.config.Dimension;
import es.csic.iiia.nsm.config.DomainFunctions;
import es.csic.iiia.nsm.config.NormSynthesisSettings;
import es.csic.iiia.nsm.metrics.DefaultNormSynthesisMetrics;
import es.csic.iiia.nsm.metrics.NormSynthesisMetrics;
import es.csic.iiia.nsm.net.norm.DefaultOmegaFunction;
import es.csic.iiia.nsm.net.norm.NetworkNodeState;
import es.csic.iiia.nsm.net.norm.NormativeNetwork;
import es.csic.iiia.nsm.net.norm.OmegaFunction;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.NormativeSystem;
import es.csic.iiia.nsm.norm.group.net.NormGroupNetwork;
import es.csic.iiia.nsm.norm.reasoning.NormReasoner;
import es.csic.iiia.nsm.perception.Monitor;
import es.csic.iiia.nsm.perception.Sensor;
import es.csic.iiia.nsm.strategy.NormSynthesisStrategy;
import es.csic.iiia.nsm.strategy.iron.IRONStrategy;
import es.csic.iiia.nsm.strategy.lion.LIONStrategy;
import es.csic.iiia.nsm.strategy.simon.SIMONStrategy;
import es.csic.iiia.nsm.visualization.NormSynthesisInspector;

/**
 * The Norm Synthesis Machine (NSM), containing:
 * <ol>
 * <li>	The norm evaluation dimensions (effectiveness and necessity).
 * 			During the norm evaluation phase, norms are evaluated in
 * 			terms of their: (i) effectiveness, based on the outcome of
 * 			their compliances, and (ii) necessity, based on the outcome
 * 			of their infringements;
 * <li>	The configuration <tt>settings</tt> of the norm synthesis machine; 
 * <li>	The monitor, containing sensors to perceive the scenario; 
 * <li>	The normative network, whose nodes stand for norms and whose edges
 * 			stand for relationships between norms; 
 * <li>	The omega function, that computes the normative system from the 
 * 			normative network; 
 * <li>	The norm synthesis <tt>strategy</tt>, which contains a method 
 * 			{@code execute()} that performs the norm synthesis cycle; 
 * <li>	The domain functions <tt>dmFunctions</tt> that allow to perform
 * 			norm synthesis for a specific domain. 
 * <li>	The NSM metrics, that contains information about the metrics of
 * 			different elements in the norm	synthesis process.
 * </ol>
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
public class NormSynthesisMachine {

	/**
	 * Generalisation mode. Mode <tt>Shallow<tt> performs shallow
	 * generalisations, while mode <tt>Deep<tt> performs deep generalisations
	 * 
	 * @author "Javier Morales (jmorales@iiia.csic.es)"
	 */
	public enum NormGeneralisationMode {
		None, Shallow, Deep;
	}

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------

	private Random randomizer;								// Randomiser
	private List<Dimension> normEvDims;				// Norm evaluation dimensions
	private NormSynthesisSettings settings;		// Norm synthesis settings
	private Monitor monitor;									// Monitor to perceive the scenario
	private NormativeNetwork nNetwork;				// The normative network
	private NormGroupNetwork nGroupNetwork; 	// The network of norm groups
	private OmegaFunction omegaFunction;			// Function to compute the NS
	private NormSynthesisStrategy strategy;		// The norm synthesis strategy
	private PredicatesDomains predDomains;		// Predicates and their domains
	private DomainFunctions dmFunctions;			// Domain functions 
	private NormSynthesisMetrics metrics;			// Norm synthesis metrics
	private NormSynthesisInspector tracer; 		// GUI
	
	private NormReasoner normReasoner; // The norms reasoner
	private NormReasoner normEvaluationReasoner;				// The norms reasoner

	private boolean gui;											// Use GUI?
	private boolean firstExecution;						// First execution of the strategy?

	//---------------------------------------------------------------------------
	// Constructors 
	//---------------------------------------------------------------------------

	/**
	 * The Norm Synthesis Machine constructor.
	 * 
	 * @param 	settings basic settings of the norm synthesis machine
	 * @param 	predDomains the predicates and terms to specify norms
	 * 					for the given domain
	 * @param 	dmFunctions the domain functions, that allow to perform 
	 * 					norm synthesis for a specific domain
	 * @param 	gui indicates if the user requires a GUI or not
	 * @see 		PredicatesDomains
	 * @see			DomainFunctions
	 */
	public NormSynthesisMachine(NormSynthesisSettings settings, 
			PredicatesDomains predDomains, DomainFunctions dmFunctions, 
			boolean gui, long randomSeed) {

		this.settings = settings;
		this.predDomains = predDomains;
		this.dmFunctions = dmFunctions;
		this.gui = gui;
		this.firstExecution = true;

		this.randomizer = new Random(randomSeed);

		/* Add norm evaluation dimensions */
		this.normEvDims = new ArrayList<Dimension>();
		this.normEvDims.add(Dimension.Effectiveness);
		this.normEvDims.add(Dimension.Necessity);

		/* Create the normative network (norms and relationships between norms) */
		this.nNetwork = new NormativeNetwork(this);
		this.nGroupNetwork = new NormGroupNetwork(this);
		
		this.omegaFunction = new DefaultOmegaFunction();
		this.nNetwork.setOmegaFunction(this.omegaFunction);
		
		/* Create the monitor to perform system sensing */
		this.monitor = new Monitor();
	}

	//---------------------------------------------------------------------------
	// Public methods 
	//---------------------------------------------------------------------------

	public void setup(NormSynthesisStrategy.Option option,
			NormGeneralisationMode genMode, int genStep, 
			NormSynthesisMetrics nsMetrics, OmegaFunction oFunction, 
			List<Norm> poolOfNorms) {

		this.metrics = nsMetrics;
		this.omegaFunction = oFunction;
		
		/* Create omega function to retrieve the normative system
		 * from the normative network */
		if(this.omegaFunction == null) {
			this.omegaFunction = new DefaultOmegaFunction();
			this.nNetwork.setOmegaFunction(this.omegaFunction);
		}
		
		/* Create default metrics */
		if(this.metrics == null) {
			this.metrics = new DefaultNormSynthesisMetrics(this);
		}

		/* Create norms reasoners */
		this.normReasoner = new NormReasoner(this.settings.getSystemGoals(), 
				this.predDomains, this.dmFunctions);
		
		this.normEvaluationReasoner = new NormReasoner(this.settings.getSystemGoals(), 
				this.predDomains, this.dmFunctions);
		
		/* Create norm synthesis strategy */
		switch(option) {
			
		case IRON:
			strategy = new IRONStrategy(this);
			break;

		case SIMON:
			strategy = new SIMONStrategy(this,genMode,genStep);
			break;
			
		case LION:
			strategy = new LIONStrategy(this,genMode,genStep);
			break;
		}
		
		/* Add default pool of norms */
		if(poolOfNorms != null) {
			this.addDefaultNormativeSystem(poolOfNorms);
		}
	}

	/**
	 * 
	 * @param strategy
	 * @param nsMetrics
	 * @param poolOfNorms
	 */
	public void setup(NormSynthesisStrategy strategy, 
			NormSynthesisMetrics nsMetrics, OmegaFunction oFunction, 
			List<Norm> poolOfNorms) {

		this.strategy = strategy;
		
		/* Create default metrics */
		if(nsMetrics != null) {
			this.metrics = nsMetrics;
		}
		else {
			new DefaultNormSynthesisMetrics(this);
		}
		
		/* Create omega function to retrieve the normative system
		 * from the normative network */
		if(oFunction != null) {
			this.omegaFunction = oFunction;
			this.nNetwork.setOmegaFunction(this.omegaFunction);
		}
		
		/* Create norms reasoners */
		this.normReasoner = 
				new NormReasoner(this.settings.getSystemGoals(), 
				this.predDomains, this.dmFunctions);
		
		this.normEvaluationReasoner = 
				new NormReasoner(this.settings.getSystemGoals(), 
				this.predDomains, this.dmFunctions);
		
		/* Add default pool of norms */
		if(poolOfNorms != null) {
			this.addDefaultNormativeSystem(poolOfNorms);
		}
	}

	/**
	 * Adds a sensor to the monitor of the norm synthesis machine
	 * 
	 * @param sensor the sensor to add to monitor
	 * @see Monitor
	 * @see Sensor
	 */
	public void addSensor(Sensor sensor) {
		this.monitor.addSensor(sensor);
	}

	/**
	 * Performs the norm synthesis cycle by executing
	 * the norm synthesis strategy
	 * 
	 * @return the {@code NormativeSystem} resulting from 
	 * 					the norm synthesis cycle. The normative system
	 * 					is computed by the omega function, from the
	 * 					normative network
	 * @see NormSynthesisStrategy
	 * @see NormativeSystem
	 */
	public NormativeSystem executeStrategy(double timeStep) 
			throws IncorrectSetupException {

		/* First, check that the NSM has been correctly setup */
		if(this.firstExecution) {
			this.firstExecution= false;
			this.checkSetup();

			/* Create the GUI if required) */
			if(this.gui) {
				tracer = new NormSynthesisInspector(this);
				tracer.show();	
			}
		}

		/* Executes the strategy and get the resulting normative system */
		NormativeSystem ns = this.strategy.execute();
		this.metrics.update(timeStep);

		if(this.gui) {
			tracer.refresh();
		}
		return ns;
	}

	//---------------------------------------------------------------------------
	// Private methods
	//---------------------------------------------------------------------------

	/**
	 * Checks the initial setup of the norm synthesis machine, ensuring
	 * that the user has added {@code sensors} to perceive the scenario,
	 * an {@code omega function} to compute the normative system from the
	 * normative network, and a norm synthesis {@code strategy}
	 * 
	 * @throws IncorrectSetupException if one of the following conditions hold:
	 * 					(1) no sensors have been added to the monitor; or
	 * 					(2) no omega function has been set; or
	 * 					(3) no strategy has been set
	 * @see IncorrectSetupException
	 */
	private void checkSetup() throws IncorrectSetupException {
//		if(this.monitor.getNumSensors() == 0) {
//			throw new IncorrectSetupException("No sensors have been added yet");
//		}
//		if(this.omegaFunction == null) {
//			throw new IncorrectSetupException("Omega function not defined yet");
//		}
		if(this.strategy == null) {
			throw new IncorrectSetupException("Strategy not defined yet. Use method"
					+ " \"setup()\" to set a strategy");
		}
	}

	/** 
	 * Adds a default normative system (i.e., a pool of norms
	 * that will be active in the normative network)
	 * 
	 * @param poolOfNorms the pool of active norms in the normative network
	 */
	private void addDefaultNormativeSystem(List<Norm> poolOfNorms) {
		for(Norm norm : poolOfNorms) {
			this.nNetwork.add(norm);
			this.nNetwork.setState(norm, NetworkNodeState.ACTIVE);
		}
	}
	
	//---------------------------------------------------------------------------
	// Getters
	//---------------------------------------------------------------------------

	/**
	 * Returns the norm synthesis settings
	 * 
	 * @return the norm synthesis settings
	 * @see NormSynthesisSettings
	 */
	public NormSynthesisSettings getNormSynthesisSettings() {
		return this.settings;
	}

	/**
	 * Returns an object {@code PredicatesDomains} that contains:
	 * 
	 * (1) 	all the possible predicates in the agents' contexts; and
	 * (2) 	the domain of each possible predicate, represented as
	 * 			a {@code Taxonomy}.
	 * 
	 * @return an object {@code PredicatesDomains} that contains all the
	 * 					possible predicates in the agents' contexts, and the domain
	 * 					of each possible predicate, represented as a {@code Taxonomy}.
	 * @see PredicatesDomains
	 */
	public PredicatesDomains getPredicatesDomains() {
		return this.predDomains;
	}

	/**
	 * Returns the domain functions that allow to perform
	 * norm synthesis for a specific domain
	 * 
	 * @return the domain functions
	 * @see DomainFunctions
	 */
	public DomainFunctions getDomainFunctions() {
		return this.dmFunctions;
	}

	/**
	 * Returns the norm synthesis metrics
	 * 
	 * @return the norm synthesis metrics
	 * @see NormSynthesisMetrics
	 */
	public NormSynthesisMetrics getNormSynthesisMetrics() {
		return this.metrics;
	}

	/**
	 * Returns the monitor that perceives the environment
	 * 
	 * @return the monitor that perceives the environment
	 * @see Monitor
	 */
	public Monitor getMonitor() {
		return this.monitor;
	}

	/**
	 * Returns the normative network
	 * 
	 * @return the normative network
	 * @see NormativeNetwork
	 */
	public NormativeNetwork getNormativeNetwork() {
		return nNetwork;
	}

	/**
	 * Returns the norm groups network
	 * 
	 * @return the norm groups network
	 * @see NormGroupNetwork
	 */
	public NormGroupNetwork getNormGroupNetwork() {
		return nGroupNetwork;
	}

	/**
	 * Returns the omega function, that computes a normative 
	 * system from a normative network
	 * 
	 * @return the omega function
	 * @see OmegaFunction
	 */
	public OmegaFunction getOmegaFunction() {
		return this.omegaFunction;
	}

	/**
	 * Returns the norm synthesis strategy
	 * 
	 * @return the norm synthesis strategy
	 * @see NormSynthesisStrategy
	 */
	public NormSynthesisStrategy getStrategy() {
		return strategy;
	}

	/**
	 * Returns the norm evaluation dimensions
	 * 
	 * @return the norm evaluation dimensions
	 * @see Dimension
	 */
	public List<Dimension> getNormEvaluationDimensions() {
		return this.normEvDims;
	}

	/**
	 * Returns the norm reasoner
	 * 
	 * @return the norm reasoner
	 */
	public NormReasoner getNormReasoner() {
		return this.normReasoner;
	}
	
	/**
	 * 
	 * @return
	 */
	public NormReasoner getNormEvaluationReasoner() {
		return this.normEvaluationReasoner;
	}

	/**
	 * Use Graphical User Interface (norms tracer)?
	 * 
	 * @return <tt>true</tt> if the NSM must use GUI 
	 */
	public boolean isGUI() {
		return this.gui;
	}

	/**
	 * Returns the norms tracer
	 * 
	 * @return the norms tracer
	 */
	public NormSynthesisInspector getTracer() {
		return this.tracer;
	}

	/**
	 * Returns the random values generator
	 * 
	 * @return the random values generator
	 */
	public Random getRandom() {
		return this.randomizer;
	}
}
