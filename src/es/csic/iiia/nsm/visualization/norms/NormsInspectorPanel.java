/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.csic.iiia.nsm.visualization.norms;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import jess.Main;
import es.csic.iiia.nsm.NormSynthesisMachine;
import es.csic.iiia.nsm.config.Dimension;
import es.csic.iiia.nsm.config.Goal;
import es.csic.iiia.nsm.config.NormSynthesisSettings;
import es.csic.iiia.nsm.metrics.NormSynthesisMetrics;
import es.csic.iiia.nsm.net.norm.NetworkEdgeType;
import es.csic.iiia.nsm.net.norm.NetworkNodeState;
import es.csic.iiia.nsm.net.norm.NormativeNetwork;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.evaluation.Utility;
import es.csic.iiia.nsm.norm.group.NormGroup;
import es.csic.iiia.nsm.norm.group.net.NormGroupNetwork;
import es.csic.iiia.nsm.visualization.NormSynthesisInspector;

/**
 *
 * @author javi
 */
public class NormsInspectorPanel extends javax.swing.JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4850532986703932668L;

	private NormSynthesisMachine nsm;
	private NormativeNetwork normativeNetwork;
	private NormGroupNetwork normGroupNetwork;
	private NormSynthesisSettings nsmSettings;
	private NormSynthesisMetrics nsMetrics;

	private Norm selectedNorm;
	private NormGroup selectedNormGroup;

	private String miscPath;

	/**
	 * Creates new form NormsTracerPanel
	 */
	public NormsInspectorPanel(NormSynthesisInspector nsInspector) {
		final File f = new File(NormsInspectorPanel.class.getProtectionDomain().
				getCodeSource().getLocation().getPath());
		String path = f.getPath();
		miscPath = path.substring(0,path.length()-3) + "misc/";
		
		initComponents();

		this.nsm = nsInspector.getNormSynthesisMachine();
		this.normativeNetwork = this.nsm.getNormativeNetwork();
		this.normGroupNetwork = this.nsm.getNormGroupNetwork();
		this.nsmSettings = this.nsm.getNormSynthesisSettings();
		this.nsMetrics = this.nsm.getNormSynthesisMetrics();
		this.selectedNorm = null;
		this.selectedNormGroup = null;

		String strategy = this.nsmSettings.getNormSynthesisStrategy();
		String generationMode = "Reactive";
		String generalisationMode = "---";
		String generalisationStep = "---";

		if(strategy.equals("SIMON") || strategy.equals("LION")) {
			generalisationMode = nsmSettings.getNormGeneralisationMode().toString();
			generalisationStep = String.valueOf(nsmSettings.getNormGeneralisationStep());
		}

		this.lblNSStrategyInfo.setText(strategy);
		this.lblGenerationModeInfo.setText(generationMode);
		this.lblGenModeInfo.setText(generalisationMode);
		this.lblGenStepInfo.setText(generalisationStep);

		this.updateTreeNormsInUse();
		this.updateTreeNormsNotInUse();
		this.updateTreeNormGroupsInUse();
		this.updateTreeNormGroupsNotInUse();
		this.updateGUI();
	}

	/**
	 * 
	 * @param evt
	 */
	private void treeNormsInUseChanged(TreeSelectionEvent evt) {
		if(treeNormsInUse.getLastSelectedPathComponent() 
				instanceof DefaultMutableTreeNode) {

			DefaultMutableTreeNode srcNode = 
					(DefaultMutableTreeNode)treeNormsInUse.getLastSelectedPathComponent();
			Object src = srcNode.getUserObject();

			if(src instanceof Norm) {
				this.selectedNorm = (Norm)src;
				this.updateSelectedNorm();
			}
		}
	}

	/**
	 * 
	 * @param evt
	 */
	private void treeNormsNotInUseChanged(TreeSelectionEvent evt) {
		if(treeNormsNotInUse.getLastSelectedPathComponent() 
				instanceof DefaultMutableTreeNode) {

			DefaultMutableTreeNode srcNode = 
					(DefaultMutableTreeNode)treeNormsNotInUse.getLastSelectedPathComponent();
			Object src = srcNode.getUserObject();

			if(src instanceof Norm) {
				this.selectedNorm = (Norm)src;
				this.updateSelectedNorm();
			}
		}
	}

	/**
	 * 
	 * @param evt
	 */
	private void treeNormGroupsInUseChanged(TreeSelectionEvent evt) {
		if(treeNormGroupsInUse.getLastSelectedPathComponent() 
				instanceof DefaultMutableTreeNode) {

			DefaultMutableTreeNode srcNode = 
					(DefaultMutableTreeNode)treeNormGroupsInUse.getLastSelectedPathComponent();
			Object src = srcNode.getUserObject();

			if(src instanceof NormGroup) {
				this.selectedNormGroup = (NormGroup)src;
				this.updateSelectedNormGroup();
			}
		}
	}

	/**
	 * 
	 * @param evt
	 */
	private void treeNormGroupsNotInUseChanged(TreeSelectionEvent evt) {
		if(treeNormGroupsNotInUse.getLastSelectedPathComponent() 
				instanceof DefaultMutableTreeNode) {

			DefaultMutableTreeNode srcNode = 
					(DefaultMutableTreeNode)treeNormGroupsNotInUse.getLastSelectedPathComponent();
			Object src = srcNode.getUserObject();

			if(src instanceof NormGroup) {
				this.selectedNormGroup = (NormGroup)src;
			}
		}
	}

	/**
	 * 
	 * 
	 * @param cSolution
	 */
	private void updateTreeNormsInUse() {
		List<Norm> nsNorms = normativeNetwork.getNormativeSystem();
		TreePath focus = null;

		this.treeNormsInUse.removeAll();

		int f_minimality = this.nsm.getNormSynthesisMetrics().
				getNormativeSystemFitoussiMinimality();

		/* Update tree of norms in use */
		DefaultMutableTreeNode rootNode = 
				new DefaultMutableTreeNode("Norms in use (" + 
						nsNorms.size() + "), w " + f_minimality + " leaves");

		TreeModel tmodel = new DefaultTreeModel(rootNode);

		// Add norms that are being evaluated
		for(Norm norm : nsNorms) {
			this.fillTree(rootNode, norm);
		}

		this.treeNormsInUse.setModel(tmodel);
		this.treeNormsInUse.setSelectionPath(focus);
		this.treeNormsInUse.validate();
	}

	/**
	 * 
	 */
	private void updateTreeNormsNotInUse() {
		List<Norm> notRepresented = normativeNetwork.getNotRepresentedNorms();
		List<Norm> notRepresentedGeneralNorms = new ArrayList<Norm>();
		List<Norm> notRepresentedLeaves = new ArrayList<Norm>();
		TreePath focus = null;

		this.treeNormsNotInUse.removeAll();

		for(Norm norm : notRepresented) {
			if(this.normativeNetwork.getState(norm) != NetworkNodeState.ACTIVE) {
				notRepresentedGeneralNorms.add(norm);
			}
		}
		for(Norm norm : notRepresented) {
			if(this.normativeNetwork.getGeneralisationLevel(norm) == 1) {
				notRepresentedLeaves.add(norm);
			}
		}

		/* Update tree of norms in use */
		DefaultMutableTreeNode rootNode =
				new DefaultMutableTreeNode("Discarded norms and leaves");

		DefaultMutableTreeNode notRepresentedGeneralNormsRoot =
				new DefaultMutableTreeNode("Discarded norms (" + notRepresentedGeneralNorms.size() + ")");

		DefaultMutableTreeNode leavesRootNode =
				new DefaultMutableTreeNode("Discarded leaves (" + notRepresentedLeaves.size() + ")");

		rootNode.add(notRepresentedGeneralNormsRoot);
		rootNode.add(leavesRootNode);

		TreeModel tmodel = new DefaultTreeModel(rootNode);

		// Add norms that are being evaluated
		for(Norm norm : notRepresentedGeneralNorms) {
			this.fillTree(notRepresentedGeneralNormsRoot, norm);
		}		

		// Add norms that are being evaluated
		for(Norm norm : notRepresentedLeaves) {
			this.fillTree(leavesRootNode, norm);
		}

		this.treeNormsNotInUse.setModel(tmodel);
		this.treeNormsNotInUse.setSelectionPath(focus);
		this.treeNormsNotInUse.validate();
	}

	/**
	 * 
	 */
	private void updateSelectedNorm() {
		List<Goal> goals = this.nsm.getNormSynthesisSettings().getSystemGoals();
		Utility utility = this.normativeNetwork.getUtility(selectedNorm);
		float eff = utility.getScore(Dimension.Effectiveness, goals.get(0));
		float nec = utility.getScore(Dimension.Necessity, goals.get(0));

		this.pbEffectiveness.setValue((int)(eff * 100));
		this.pbNecessity.setValue((int)(nec * 100));

		this.setSelectedNormDescription();
	}

	/**
	 * 
	 */
	private void setSelectedNormDescription() {
		String s = "";
		Norm n = this.selectedNorm;

		s += "Pre-condition\n";
		s += "-------\n";
		s += n.getPrecondition().toString() + "\n\n";

		s += "Post-condition\n";
		s += "-------\n";
		s += n.getModality() + "(" + n.getAction() + ")";

		this.jTextAreaInspectedNorm.setText(s);
	}


	/*
	 * NORM GROUPS
	 */


	/**
	 * 
	 * 
	 * @param cSolution
	 */
	private void updateTreeNormGroupsInUse() {
		List<NormGroup> activeNormGroups = normGroupNetwork.getActiveNodes();
		TreePath focus = null;

		Collections.sort(activeNormGroups);

		this.treeNormGroupsInUse.removeAll();

		/* Update tree of norms in use */
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(
				"Norm groups in use (" + activeNormGroups.size() + ")");
		TreeModel tmodel = new DefaultTreeModel(rootNode);

		// Add norm groups that are being evaluated
		for(NormGroup normGroup : activeNormGroups) {
			this.fillTree(rootNode, normGroup);
		}

		this.treeNormGroupsInUse.setModel(tmodel);
		this.treeNormGroupsInUse.setSelectionPath(focus);
		this.treeNormGroupsInUse.validate();
	}

	/**
	 * 
	 */
	private void updateTreeNormGroupsNotInUse() {
		List<NormGroup> nonRepresented = normGroupNetwork.getNotRepresentedNormGroups();
		TreePath focus = null;

		Collections.sort(nonRepresented);

		this.treeNormGroupsNotInUse.removeAll();

		/* Update tree of norms in use */
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(
				"Norm groups in use (" + nonRepresented.size() + ")");
		TreeModel tmodel = new DefaultTreeModel(rootNode);

		// Add norm groups that are being evaluated
		for(NormGroup normGroup : nonRepresented) {
			this.fillTree(rootNode, normGroup);
		}

		this.treeNormGroupsNotInUse.setModel(tmodel);
		this.treeNormGroupsNotInUse.setSelectionPath(focus);
		this.treeNormGroupsNotInUse.validate();
	}

	/**
	 * 
	 */
	private void updateSelectedNormGroup() {
		List<Goal> goals = this.nsm.getNormSynthesisSettings().getSystemGoals();
		Utility utility = this.normGroupNetwork.getUtility(selectedNormGroup);
		float eff = utility.getScore(Dimension.Effectiveness, goals.get(0));

		this.pbNormGroupEffectiveness.setValue((int)(eff * 100));
		this.setSelectedNormGroupDescription();
	}

	/**
	 * 
	 */
	private void setSelectedNormGroupDescription() {
		NormGroup n = this.selectedNormGroup;
		this.jTextAreaInspectedNormGroup.setText(n.toStringDetailed());
	}


	/**
	 * 
	 * @param node
	 * @param children
	 */
	private void fillTree(DefaultMutableTreeNode parentNode, Norm norm) {
		List<Norm> children = normativeNetwork.getChildren(norm);
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(norm);
		parentNode.add(node);

		for(Norm child : children) {
			this.fillTree(node, child);
		}
	}

	/**
	 * 
	 * @param node
	 * @param children
	 */
	private void fillTree(DefaultMutableTreeNode parentNode, NormGroup normGroup) {
		List<NormGroup> children = normGroupNetwork.getChildren(normGroup);
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(normGroup);
		parentNode.add(node);

		for(NormGroup child : children) {
			this.fillTree(node, child);
		}
	}

	/**
	 * 
	 */
	public void updateGUI() {

		/* Normative network metrics */
		int nnCardinality = this.normativeNetwork.getNorms().size();
		int numGenRels = this.normativeNetwork.
				getRelationships(NetworkEdgeType.GENERALISATION).size();
		int numSubsRels = this.normativeNetwork.
				getRelationships(NetworkEdgeType.SUBSTITUTABILITY).size();
		int numComplRels = this.normativeNetwork.
				getRelationships(NetworkEdgeType.COMPLEMENTARITY).size();

		this.lblNNCardinalityInfo.setText(String.valueOf(nnCardinality));
		this.lblNumGenRelsInfo.setText(String.valueOf(numGenRels));
		this.lblNumSubsRelsInfo.setText(String.valueOf(numSubsRels));
		this.lblNumComplRelsInfo.setText(String.valueOf(numComplRels));

		/* Normative system metrics */
		Goal goal = this.nsmSettings.getSystemGoals().get(0);
		Utility u = this.nsMetrics.getNormativeSystemUtility();
		
		int nsMinimality = this.nsMetrics.getNormativeSystemMinimality();
		int nsFMinimality = this.nsMetrics.getNormativeSystemFitoussiMinimality();
		float nsEff = u.getScore(Dimension.Effectiveness, goal);
		float nsNec = u.getScore(Dimension.Necessity, goal);
		
		nsEff = (float) (Math.round( nsEff * 100.0 ) / 100.0);
		nsNec = (float) (Math.round( nsNec * 100.0 ) / 100.0);
		
		this.lblNSMinimalityInfo.setText(String.valueOf(nsMinimality));
		this.lblNSFitoussiMinimalityInfo.setText(String.valueOf(nsFMinimality));
		this.lblNSMedianEffInfo.setText(String.valueOf(nsEff != 0 ? nsEff : "---"));
		this.lblNSMedianNecInfo.setText(String.valueOf(nsNec != 0 ? nsNec : "---"));

		/* Update norm synthesis metrics */
		long numStoredNorms = this.nsMetrics.getNumNodesInMemory(); 
		long numAccToNorms = this.nsMetrics.getNumNodesVisited();
		double medCompTime = this.nsMetrics.getMedianComputationTime() / 1000;
		double totalCompTime = this.nsMetrics.getTotalComputationTime() / 1000;
		long numTicksStability = this.nsMetrics.getNumTicksOfStability();
		boolean converged = this.nsMetrics.hasConverged();

		medCompTime = Math.round( medCompTime * 10000.0 ) / 10000.0;
		totalCompTime = Math.round( totalCompTime * 100.0 ) / 100.0;

		this.lblStoredNormsInfo.setText(String.valueOf(numStoredNorms));
		this.lblNormAccessesInfo.setText(String.valueOf(numAccToNorms));
		this.lblMedianCompTimeInfo.setText(String.valueOf(medCompTime) + " s");
		this.lblTotalCompTimeInfo.setText(String.valueOf(totalCompTime) + " s");
		this.lblNumTicksNSStabilityInfo.setText(String.valueOf(numTicksStability) + " ticks");
		if(converged) {
			this.lblNSConvergedInfo.setText("YES!");
			this.lblNSConvergedInfo.setForeground(new Color(0,150,0));
		}
		else {
			this.lblNSConvergedInfo.setText("Not yet");
			this.lblNSConvergedInfo.setForeground(Color.RED);
		}

		/* Update elements of the UI */
		this.updateUI();
	}

	/**
	 * 
	 * @param evt
	 */
	private void btnPerfRangeActionPerformed(ActionEvent evt) {
		if(this.selectedNorm != null) {
			this.nsm.getTracer().addNormScoreChart(this.selectedNorm);
		}
	}

	/**
	 * 
	 * @param evt
	 */
	private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {
		this.updateTreeNormsInUse();
		this.updateTreeNormsNotInUse();
		this.updateTreeNormGroupsInUse();
		this.updateTreeNormGroupsNotInUse();
		this.updateGUI();
	}

	/**
	 * 
	 * @param evt
	 */
	private void btnNormGroupPerfRangeActionPerformed(java.awt.event.ActionEvent evt) {                                                      
		if(this.selectedNormGroup != null) {
			this.nsm.getTracer().addNormGroupScoreChart(this.selectedNormGroup);
		}
	}                                                     

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed" desc="Generated Code">                          
	private void initComponents() {

		lblTitle = new javax.swing.JLabel();
		tabbedPanel = new javax.swing.JTabbedPane();
		panelNorms = new javax.swing.JPanel();
		panelInspectedNorm = new javax.swing.JPanel();
		pbEffectiveness = new javax.swing.JProgressBar();
		pbNecessity = new javax.swing.JProgressBar();
		lblEffectiveness = new javax.swing.JLabel();
		lblNecessity = new javax.swing.JLabel();
		lblPerfRange = new javax.swing.JLabel();
		btnPerfRange = new javax.swing.JButton();
		jScrollInspectedNorm = new javax.swing.JScrollPane();
		jTextAreaInspectedNorm = new javax.swing.JTextArea();
		lblNormsInUse = new javax.swing.JLabel();
		panelNormsInUse = new javax.swing.JScrollPane();
		treeNormsInUse = new javax.swing.JTree();
		lblDiscardedNorms = new javax.swing.JLabel();
		panelDiscardedNorms = new javax.swing.JScrollPane();
		treeNormsNotInUse = new javax.swing.JTree();
		panelNormsGroups = new javax.swing.JPanel();
		panelInspectedNormGroup = new javax.swing.JPanel();
		pbNormGroupEffectiveness = new javax.swing.JProgressBar();
		lblNormGroupEffectiveness = new javax.swing.JLabel();
		lblNormGroupPerfRange = new javax.swing.JLabel();
		jScrollInspectedNormGroup = new javax.swing.JScrollPane();
		jTextAreaInspectedNormGroup = new javax.swing.JTextArea();
		btnNormGroupPerfRange = new javax.swing.JButton();
		panelNormGroupsInUse = new javax.swing.JScrollPane();
		treeNormGroupsInUse = new javax.swing.JTree();
		lblNormGroupsInUse = new javax.swing.JLabel();
		lblDiscardedNormsGroups = new javax.swing.JLabel();
		panelDiscardedNormsGroups = new javax.swing.JScrollPane();
		treeNormGroupsNotInUse = new javax.swing.JTree();
		panelNSConfig = new javax.swing.JPanel();
		lblNSStrategy = new javax.swing.JLabel();
		lblNSStrategyInfo = new javax.swing.JLabel();
		lblGenMode = new javax.swing.JLabel();
		lblGenModeInfo = new javax.swing.JLabel();
		lblGenStep = new javax.swing.JLabel();
		lblGenStepInfo = new javax.swing.JLabel();
		lblGenerationMode = new javax.swing.JLabel();
		lblGenerationModeInfo = new javax.swing.JLabel();
		panelNNMetrics = new javax.swing.JPanel();
		lblNNCardinality = new javax.swing.JLabel();
		lblNNCardinalityInfo = new javax.swing.JLabel();
		lblNumGenRels = new javax.swing.JLabel();
		lblNumComplRels = new javax.swing.JLabel();
		lblNumSubsRels = new javax.swing.JLabel();
		lblNumSubsRelsInfo = new javax.swing.JLabel();
		lblNumComplRelsInfo = new javax.swing.JLabel();
		lblNumGenRelsInfo = new javax.swing.JLabel();
		panelNormSynthMetrics = new javax.swing.JPanel();
		lblTotalCompTimeInfo = new javax.swing.JLabel();
		lblTotalCompTime = new javax.swing.JLabel();
		lblNormAccessesInfo = new javax.swing.JLabel();
		lblMedianCompTime = new javax.swing.JLabel();
		lblMedianCompTimeInfo = new javax.swing.JLabel();
		lblNormAccesses = new javax.swing.JLabel();
		lblStoredNorms = new javax.swing.JLabel();
		lblStoredNormsInfo = new javax.swing.JLabel();
		lblNumTicksNSStability = new javax.swing.JLabel();
		lblNumTicksNSStabilityInfo = new javax.swing.JLabel();
		lblNSConverged = new javax.swing.JLabel();
		lblNSConvergedInfo = new javax.swing.JLabel();
		panelNSMetrics = new javax.swing.JPanel();
		lblNSMinimality = new javax.swing.JLabel();
		lblNSMinimalityInfo = new javax.swing.JLabel();
		lblNSFitoussiMinimality = new javax.swing.JLabel();
		lblNSMedianNec = new javax.swing.JLabel();
		lblNSMedianEff = new javax.swing.JLabel();
		lblNSMedianEffInfo = new javax.swing.JLabel();
		lblNSMedianNecInfo = new javax.swing.JLabel();
		lblNSFitoussiMinimalityInfo = new javax.swing.JLabel();
		btnUpdate = new javax.swing.JButton();

		setBackground(java.awt.Color.lightGray);

		String loupePath = miscPath + "loupe.png";
		String chartPath = miscPath + "chart.png";
		
		lblTitle.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
		lblTitle.setIcon(new javax.swing.ImageIcon(loupePath)); // NOI18N
		lblTitle.setText("Norms Inspector");
		lblTitle.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		lblTitle.setOpaque(true);

		tabbedPanel.setBackground(java.awt.Color.lightGray);
		tabbedPanel.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N

		panelNorms.setBackground(java.awt.Color.lightGray);
		panelNorms.setPreferredSize(new java.awt.Dimension(605, 358));

		panelInspectedNorm.setBackground(java.awt.Color.lightGray);
		panelInspectedNorm.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Inspected norm", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 1, 13), java.awt.Color.blue)); // NOI18N

		pbEffectiveness.setToolTipText("");
		pbEffectiveness.setStringPainted(true);

		pbNecessity.setStringPainted(true);

		lblEffectiveness.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
		lblEffectiveness.setText("Effectiveness");

		lblNecessity.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
		lblNecessity.setText("Necessity");

		lblPerfRange.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
		lblPerfRange.setText("Performance ranges");

		btnPerfRange.setIcon(new javax.swing.ImageIcon(chartPath)); // NOI18N
		btnPerfRange.setText("Show");
		btnPerfRange.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnPerfRangeActionPerformed(evt);
			}
		});

		jTextAreaInspectedNorm.setColumns(20);
		jTextAreaInspectedNorm.setRows(5);
		jScrollInspectedNorm.setViewportView(jTextAreaInspectedNorm);

		javax.swing.GroupLayout panelInspectedNormLayout = new javax.swing.GroupLayout(panelInspectedNorm);
		panelInspectedNorm.setLayout(panelInspectedNormLayout);
		panelInspectedNormLayout.setHorizontalGroup(
				panelInspectedNormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelInspectedNormLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelInspectedNormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(jScrollInspectedNorm, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
								.addGroup(panelInspectedNormLayout.createSequentialGroup()
										.addComponent(lblPerfRange, javax.swing.GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE)
										.addGap(18, 18, 18)
										.addComponent(btnPerfRange))
										.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelInspectedNormLayout.createSequentialGroup()
												.addComponent(lblEffectiveness)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(pbEffectiveness, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE))
												.addGroup(panelInspectedNormLayout.createSequentialGroup()
														.addComponent(lblNecessity, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
														.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
														.addComponent(pbNecessity, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)))
														.addContainerGap())
				);
		panelInspectedNormLayout.setVerticalGroup(
				panelInspectedNormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelInspectedNormLayout.createSequentialGroup()
						.addGap(10, 10, 10)
						.addComponent(jScrollInspectedNorm, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addGroup(panelInspectedNormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(pbEffectiveness, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(lblEffectiveness))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(panelInspectedNormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(lblNecessity)
										.addComponent(pbNecessity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGap(22, 22, 22)
										.addGroup(panelInspectedNormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(lblPerfRange)
												.addComponent(btnPerfRange))
												.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);

		lblNormsInUse.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
		lblNormsInUse.setText("Norms in use");

		treeNormsInUse.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		treeNormsInUse.setFont(new java.awt.Font("Arial", 0, 13)); // NOI18N
		panelNormsInUse.setViewportView(treeNormsInUse);
		treeNormsInUse.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
			public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
				treeNormsInUseChanged(evt);
			}
		});	

		lblDiscardedNorms.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
		lblDiscardedNorms.setText("Norms not in use");

		treeNormsNotInUse.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		treeNormsNotInUse.setFont(new java.awt.Font("Arial", 0, 13)); // NOI18N
		panelDiscardedNorms.setViewportView(treeNormsNotInUse);
		treeNormsNotInUse.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
			public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
				treeNormsNotInUseChanged(evt);
			}
		});	

		javax.swing.GroupLayout panelNormsLayout = new javax.swing.GroupLayout(panelNorms);
		panelNorms.setLayout(panelNormsLayout);
		panelNormsLayout.setHorizontalGroup(
				panelNormsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelNormsLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelNormsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(lblNormsInUse)
								.addComponent(panelNormsInUse, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(panelNormsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addComponent(lblDiscardedNorms)
										.addComponent(panelDiscardedNorms, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(panelInspectedNorm, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addContainerGap())
				);
		panelNormsLayout.setVerticalGroup(
				panelNormsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelNormsLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelNormsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(panelNormsLayout.createSequentialGroup()
										.addGroup(panelNormsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(lblDiscardedNorms)
												.addComponent(lblNormsInUse))
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addGroup(panelNormsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(panelDiscardedNorms, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
														.addComponent(panelNormsInUse, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
														.addComponent(panelInspectedNorm, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
														.addContainerGap())
				);

		tabbedPanel.addTab("Synthesised norms", panelNorms);

		panelNormsGroups.setBackground(java.awt.Color.lightGray);

		panelInspectedNormGroup.setBackground(java.awt.Color.lightGray);
		panelInspectedNormGroup.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Inspected norm group", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 1, 13), java.awt.Color.blue)); // NOI18N

		pbNormGroupEffectiveness.setToolTipText("");
		pbNormGroupEffectiveness.setStringPainted(true);

		lblNormGroupEffectiveness.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
		lblNormGroupEffectiveness.setText("Effectiveness");

		lblNormGroupPerfRange.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
		lblNormGroupPerfRange.setText("Performance ranges");

		jTextAreaInspectedNormGroup.setColumns(20);
		jTextAreaInspectedNormGroup.setRows(5);
		jScrollInspectedNormGroup.setViewportView(jTextAreaInspectedNormGroup);

		btnNormGroupPerfRange.setIcon(new javax.swing.ImageIcon(chartPath)); // NOI18N
		btnNormGroupPerfRange.setText("Show");
		btnNormGroupPerfRange.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnNormGroupPerfRangeActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout panelInspectedNormGroupLayout = new javax.swing.GroupLayout(panelInspectedNormGroup);
		panelInspectedNormGroup.setLayout(panelInspectedNormGroupLayout);
		panelInspectedNormGroupLayout.setHorizontalGroup(
				panelInspectedNormGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelInspectedNormGroupLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelInspectedNormGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(jScrollInspectedNormGroup, javax.swing.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE)
								.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelInspectedNormGroupLayout.createSequentialGroup()
										.addComponent(lblNormGroupEffectiveness)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(pbNormGroupEffectiveness, javax.swing.GroupLayout.DEFAULT_SIZE, 139, Short.MAX_VALUE))
										.addGroup(panelInspectedNormGroupLayout.createSequentialGroup()
												.addComponent(lblNormGroupPerfRange, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(btnNormGroupPerfRange)))
												.addContainerGap())
				);
		panelInspectedNormGroupLayout.setVerticalGroup(
				panelInspectedNormGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelInspectedNormGroupLayout.createSequentialGroup()
						.addGap(10, 10, 10)
						.addComponent(jScrollInspectedNormGroup, javax.swing.GroupLayout.DEFAULT_SIZE, 171, Short.MAX_VALUE)
						.addGap(13, 13, 13)
						.addGroup(panelInspectedNormGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(pbNormGroupEffectiveness, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(lblNormGroupEffectiveness))
								.addGap(19, 19, 19)
								.addGroup(panelInspectedNormGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(lblNormGroupPerfRange)
										.addComponent(btnNormGroupPerfRange))
										.addContainerGap())
				);

		treeNormGroupsInUse.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		treeNormGroupsInUse.setFont(new java.awt.Font("Arial", 0, 13)); // NOI18N
		panelNormGroupsInUse.setViewportView(treeNormGroupsInUse);
		treeNormGroupsInUse.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
			public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
				treeNormGroupsInUseChanged(evt);
			}
		});	

		lblNormGroupsInUse.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
		lblNormGroupsInUse.setText("Norm groups in use");

		lblDiscardedNormsGroups.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
		lblDiscardedNormsGroups.setText("Norm groups not in use");

		treeNormGroupsNotInUse.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		treeNormGroupsNotInUse.setFont(new java.awt.Font("Arial", 0, 13)); // NOI18N
		panelDiscardedNormsGroups.setViewportView(treeNormGroupsNotInUse);
		treeNormGroupsNotInUse.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
			public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
				treeNormGroupsNotInUseChanged(evt);
			}
		});	

		javax.swing.GroupLayout panelNormsGroupsLayout = new javax.swing.GroupLayout(panelNormsGroups);
		panelNormsGroups.setLayout(panelNormsGroupsLayout);
		panelNormsGroupsLayout.setHorizontalGroup(
				panelNormsGroupsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelNormsGroupsLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelNormsGroupsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(lblNormGroupsInUse)
								.addComponent(panelNormGroupsInUse, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(panelNormsGroupsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addComponent(lblDiscardedNormsGroups)
										.addComponent(panelDiscardedNormsGroups, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(panelInspectedNormGroup, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addContainerGap())
				);
		panelNormsGroupsLayout.setVerticalGroup(
				panelNormsGroupsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelNormsGroupsLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelNormsGroupsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(panelNormsGroupsLayout.createSequentialGroup()
										.addGroup(panelNormsGroupsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(lblDiscardedNormsGroups)
												.addComponent(lblNormGroupsInUse))
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addGroup(panelNormsGroupsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(panelDiscardedNormsGroups, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
														.addComponent(panelNormGroupsInUse, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
														.addComponent(panelInspectedNormGroup, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
														.addContainerGap())
				);

		tabbedPanel.addTab("Synthesised norm groups", panelNormsGroups);

		panelNSConfig.setBackground(java.awt.Color.lightGray);
		panelNSConfig.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Norm synthesis configuration", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 1, 13), java.awt.Color.blue)); // NOI18N

		lblNSStrategy.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
		lblNSStrategy.setText("Strategy:");

		lblNSStrategyInfo.setFont(new java.awt.Font("Arial", 0, 13)); // NOI18N
		lblNSStrategyInfo.setText("SIMON");

		lblGenMode.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
		lblGenMode.setText("Generalisation mode: ");

		lblGenModeInfo.setFont(new java.awt.Font("Arial", 0, 13)); // NOI18N
		lblGenModeInfo.setText("Deep");

		lblGenStep.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
		lblGenStep.setText("Generalisation step:");

		lblGenStepInfo.setFont(new java.awt.Font("Arial", 0, 13)); // NOI18N
		lblGenStepInfo.setText("1");

		lblGenerationMode.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
		lblGenerationMode.setText("Generation mode:");

		lblGenerationModeInfo.setFont(new java.awt.Font("Arial", 0, 13)); // NOI18N
		lblGenerationModeInfo.setText("Reactive");

		javax.swing.GroupLayout panelNSConfigLayout = new javax.swing.GroupLayout(panelNSConfig);
		panelNSConfig.setLayout(panelNSConfigLayout);
		panelNSConfigLayout.setHorizontalGroup(
				panelNSConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelNSConfigLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelNSConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(panelNSConfigLayout.createSequentialGroup()
										.addComponent(lblGenStep, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(lblGenStepInfo, javax.swing.GroupLayout.DEFAULT_SIZE, 16, Short.MAX_VALUE)
										.addGap(72, 72, 72))
										.addGroup(panelNSConfigLayout.createSequentialGroup()
												.addComponent(lblGenMode)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(lblGenModeInfo)
												.addGap(0, 0, Short.MAX_VALUE))
												.addGroup(panelNSConfigLayout.createSequentialGroup()
														.addGroup(panelNSConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
																.addComponent(lblNSStrategy, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
																.addComponent(lblGenerationMode, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 139, Short.MAX_VALUE))
																.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																.addGroup(panelNSConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
																		.addComponent(lblGenerationModeInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
																		.addComponent(lblNSStrategyInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
				);
		panelNSConfigLayout.setVerticalGroup(
				panelNSConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelNSConfigLayout.createSequentialGroup()
						.addGap(5, 5, 5)
						.addGroup(panelNSConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(lblNSStrategy)
								.addComponent(lblNSStrategyInfo))
								.addGap(8, 8, 8)
								.addGroup(panelNSConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(lblGenerationMode)
										.addComponent(lblGenerationModeInfo))
										.addGap(8, 8, 8)
										.addGroup(panelNSConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(lblGenMode)
												.addComponent(lblGenModeInfo))
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, Short.MAX_VALUE)
												.addGroup(panelNSConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(lblGenStep)
														.addComponent(lblGenStepInfo))
														.addContainerGap())
				);

		panelNNMetrics.setBackground(java.awt.Color.lightGray);
		panelNNMetrics.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Normative network metrics", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 1, 13), java.awt.Color.blue)); // NOI18N

		lblNNCardinality.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
		lblNNCardinality.setText("Synthesised norms:");

		lblNNCardinalityInfo.setFont(new java.awt.Font("Arial", 0, 13)); // NOI18N
		lblNNCardinalityInfo.setText("0");

		lblNumGenRels.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
		lblNumGenRels.setText("Generalisation relationships:");

		lblNumComplRels.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
		lblNumComplRels.setText("Complementarity relationships:");

		lblNumSubsRels.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
		lblNumSubsRels.setText("Substitutability relationships:");

		lblNumSubsRelsInfo.setText("0");

		lblNumComplRelsInfo.setText("0");

		lblNumGenRelsInfo.setText("0");

		javax.swing.GroupLayout panelNNMetricsLayout = new javax.swing.GroupLayout(panelNNMetrics);
		panelNNMetrics.setLayout(panelNNMetricsLayout);
		panelNNMetricsLayout.setHorizontalGroup(
				panelNNMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelNNMetricsLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelNNMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
								.addComponent(lblNumComplRels, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(lblNumSubsRels, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(lblNumGenRels, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(lblNNCardinality, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(panelNNMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addGroup(panelNNMetricsLayout.createSequentialGroup()
												.addComponent(lblNNCardinalityInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
												.addGap(0, 8, Short.MAX_VALUE))
												.addComponent(lblNumSubsRelsInfo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
												.addComponent(lblNumComplRelsInfo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
												.addComponent(lblNumGenRelsInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
												.addContainerGap())
				);
		panelNNMetricsLayout.setVerticalGroup(
				panelNNMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelNNMetricsLayout.createSequentialGroup()
						.addGap(5, 5, 5)
						.addGroup(panelNNMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(lblNNCardinality)
								.addComponent(lblNNCardinalityInfo))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(panelNNMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(lblNumGenRels)
										.addComponent(lblNumGenRelsInfo))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(panelNNMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(lblNumSubsRels)
												.addComponent(lblNumSubsRelsInfo))
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addGroup(panelNNMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(lblNumComplRels)
														.addComponent(lblNumComplRelsInfo))
														.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);

		panelNormSynthMetrics.setBackground(java.awt.Color.lightGray);
		panelNormSynthMetrics.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Norm synthesis metrics", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 1, 13), java.awt.Color.blue)); // NOI18N

		lblTotalCompTimeInfo.setText("0");

		lblTotalCompTime.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
		lblTotalCompTime.setText("Total computation time:");

		lblNormAccessesInfo.setText("0");

		lblMedianCompTime.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
		lblMedianCompTime.setText("Median computation time:");

		lblMedianCompTimeInfo.setText("0");

		lblNormAccesses.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
		lblNormAccesses.setText("Norm accesses:");

		lblStoredNorms.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
		lblStoredNorms.setText("Stored norms:");

		lblStoredNormsInfo.setText("0");

		lblNumTicksNSStability.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
		lblNumTicksNSStability.setText("Stability of current NS:");

		lblNumTicksNSStabilityInfo.setText("0 ticks");

		lblNSConverged.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
		lblNSConverged.setText("Convergence:");

		lblNSConvergedInfo.setForeground(java.awt.Color.red);
		lblNSConvergedInfo.setText("Not yet");

		javax.swing.GroupLayout panelNormSynthMetricsLayout = new javax.swing.GroupLayout(panelNormSynthMetrics);
		panelNormSynthMetrics.setLayout(panelNormSynthMetricsLayout);
		panelNormSynthMetricsLayout.setHorizontalGroup(
				panelNormSynthMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelNormSynthMetricsLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelNormSynthMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
								.addComponent(lblNormAccesses, javax.swing.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
								.addComponent(lblStoredNorms, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(panelNormSynthMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addComponent(lblStoredNormsInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(lblNormAccessesInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addGroup(panelNormSynthMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
												.addComponent(lblTotalCompTime, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
												.addComponent(lblMedianCompTime))
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addGroup(panelNormSynthMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
														.addComponent(lblMedianCompTimeInfo, javax.swing.GroupLayout.DEFAULT_SIZE, 76, Short.MAX_VALUE)
														.addComponent(lblTotalCompTimeInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
														.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
														.addGroup(panelNormSynthMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
																.addComponent(lblNSConverged, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
																.addComponent(lblNumTicksNSStability, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
																.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																.addGroup(panelNormSynthMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
																		.addComponent(lblNumTicksNSStabilityInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
																		.addComponent(lblNSConvergedInfo, javax.swing.GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE))
																		.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);
		panelNormSynthMetricsLayout.setVerticalGroup(
				panelNormSynthMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelNormSynthMetricsLayout.createSequentialGroup()
						.addGap(3, 3, 3)
						.addGroup(panelNormSynthMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(lblStoredNorms)
								.addComponent(lblStoredNormsInfo)
								.addComponent(lblMedianCompTime)
								.addComponent(lblMedianCompTimeInfo)
								.addComponent(lblNumTicksNSStability)
								.addComponent(lblNumTicksNSStabilityInfo))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(panelNormSynthMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(lblNormAccesses)
										.addComponent(lblNormAccessesInfo)
										.addComponent(lblTotalCompTime)
										.addComponent(lblTotalCompTimeInfo)
										.addComponent(lblNSConverged)
										.addComponent(lblNSConvergedInfo))
										.addGap(12, 12, 12))
				);

		panelNSMetrics.setBackground(java.awt.Color.lightGray);
		panelNSMetrics.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Normative system metrics", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 1, 13), java.awt.Color.blue)); // NOI18N

		lblNSMinimality.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
		lblNSMinimality.setText("Active norms:");

		lblNSMinimalityInfo.setFont(new java.awt.Font("Arial", 0, 13)); // NOI18N
		lblNSMinimalityInfo.setText("0");

		lblNSFitoussiMinimality.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
		lblNSFitoussiMinimality.setText("Represented norms:");

		lblNSMedianNec.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
		lblNSMedianNec.setText("Necessity:");

		lblNSMedianEff.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
		lblNSMedianEff.setText("Effectiveness:");

		lblNSMedianEffInfo.setText("0");

		lblNSMedianNecInfo.setText("0");

		lblNSFitoussiMinimalityInfo.setText("0");

		javax.swing.GroupLayout panelNSMetricsLayout = new javax.swing.GroupLayout(panelNSMetrics);
		panelNSMetrics.setLayout(panelNSMetricsLayout);
		panelNSMetricsLayout.setHorizontalGroup(
				panelNSMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelNSMetricsLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelNSMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
								.addComponent(lblNSMedianNec, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(lblNSMedianEff, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(lblNSFitoussiMinimality, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(lblNSMinimality, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(panelNSMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addGroup(panelNSMetricsLayout.createSequentialGroup()
												.addComponent(lblNSMinimalityInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
												.addGap(0, 0, Short.MAX_VALUE))
												.addComponent(lblNSMedianEffInfo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
												.addComponent(lblNSMedianNecInfo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
												.addComponent(lblNSFitoussiMinimalityInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
												.addGap(8, 8, 8))
				);
		panelNSMetricsLayout.setVerticalGroup(
				panelNSMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelNSMetricsLayout.createSequentialGroup()
						.addGap(5, 5, 5)
						.addGroup(panelNSMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(lblNSMinimality)
								.addComponent(lblNSMinimalityInfo))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(panelNSMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(lblNSFitoussiMinimality)
										.addComponent(lblNSFitoussiMinimalityInfo))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(panelNSMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(lblNSMedianEff)
												.addComponent(lblNSMedianEffInfo))
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addGroup(panelNSMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(lblNSMedianNec)
														.addComponent(lblNSMedianNecInfo))
														.addContainerGap())
				);

		btnUpdate.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
		btnUpdate.setText("Update");
		btnUpdate.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnUpdateActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addContainerGap()
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(layout.createSequentialGroup()
										.addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
										.addGap(0, 0, Short.MAX_VALUE))
										.addGroup(layout.createSequentialGroup()
												.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
														.addComponent(panelNormSynthMetrics, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
														.addComponent(tabbedPanel)
														.addGroup(layout.createSequentialGroup()
																.addComponent(panelNSConfig, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
																.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																.addComponent(panelNNMetrics, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
																.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																.addComponent(panelNSMetrics, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
																.addContainerGap())))
				);
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblTitle)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
								.addComponent(panelNNMetrics, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(panelNSMetrics, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(panelNSConfig, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(panelNormSynthMetrics, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
								.addComponent(tabbedPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(btnUpdate)
								.addContainerGap())
				);

		tabbedPanel.getAccessibleContext().setAccessibleName("Norms");
	}// </editor-fold>        

	// Variables declaration - do not modify                     
	private javax.swing.JButton btnNormGroupPerfRange;
	private javax.swing.JButton btnPerfRange;
	private javax.swing.JButton btnUpdate;
	private javax.swing.JScrollPane jScrollInspectedNorm;
	private javax.swing.JScrollPane jScrollInspectedNormGroup;
	private javax.swing.JTextArea jTextAreaInspectedNorm;
	private javax.swing.JTextArea jTextAreaInspectedNormGroup;
	private javax.swing.JLabel lblDiscardedNorms;
	private javax.swing.JLabel lblDiscardedNormsGroups;
	private javax.swing.JLabel lblEffectiveness;
	private javax.swing.JLabel lblGenMode;
	private javax.swing.JLabel lblGenModeInfo;
	private javax.swing.JLabel lblGenStep;
	private javax.swing.JLabel lblGenStepInfo;
	private javax.swing.JLabel lblGenerationMode;
	private javax.swing.JLabel lblGenerationModeInfo;
	private javax.swing.JLabel lblMedianCompTime;
	private javax.swing.JLabel lblMedianCompTimeInfo;
	private javax.swing.JLabel lblNNCardinality;
	private javax.swing.JLabel lblNNCardinalityInfo;
	private javax.swing.JLabel lblNSConverged;
	private javax.swing.JLabel lblNSConvergedInfo;
	private javax.swing.JLabel lblNSFitoussiMinimality;
	private javax.swing.JLabel lblNSFitoussiMinimalityInfo;
	private javax.swing.JLabel lblNSMedianEff;
	private javax.swing.JLabel lblNSMedianEffInfo;
	private javax.swing.JLabel lblNSMedianNec;
	private javax.swing.JLabel lblNSMedianNecInfo;
	private javax.swing.JLabel lblNSMinimality;
	private javax.swing.JLabel lblNSMinimalityInfo;
	private javax.swing.JLabel lblNSStrategy;
	private javax.swing.JLabel lblNSStrategyInfo;
	private javax.swing.JLabel lblNecessity;
	private javax.swing.JLabel lblNormAccesses;
	private javax.swing.JLabel lblNormAccessesInfo;
	private javax.swing.JLabel lblNormGroupEffectiveness;
	private javax.swing.JLabel lblNormGroupPerfRange;
	private javax.swing.JLabel lblNormGroupsInUse;
	private javax.swing.JLabel lblNormsInUse;
	private javax.swing.JLabel lblNumComplRels;
	private javax.swing.JLabel lblNumComplRelsInfo;
	private javax.swing.JLabel lblNumGenRels;
	private javax.swing.JLabel lblNumGenRelsInfo;
	private javax.swing.JLabel lblNumSubsRels;
	private javax.swing.JLabel lblNumSubsRelsInfo;
	private javax.swing.JLabel lblNumTicksNSStability;
	private javax.swing.JLabel lblNumTicksNSStabilityInfo;
	private javax.swing.JLabel lblPerfRange;
	private javax.swing.JLabel lblStoredNorms;
	private javax.swing.JLabel lblStoredNormsInfo;
	private javax.swing.JLabel lblTitle;
	private javax.swing.JLabel lblTotalCompTime;
	private javax.swing.JLabel lblTotalCompTimeInfo;
	private javax.swing.JScrollPane panelDiscardedNorms;
	private javax.swing.JScrollPane panelDiscardedNormsGroups;
	private javax.swing.JPanel panelInspectedNorm;
	private javax.swing.JPanel panelInspectedNormGroup;
	private javax.swing.JPanel panelNNMetrics;
	private javax.swing.JPanel panelNSConfig;
	private javax.swing.JPanel panelNSMetrics;
	private javax.swing.JScrollPane panelNormGroupsInUse;
	private javax.swing.JPanel panelNormSynthMetrics;
	private javax.swing.JPanel panelNorms;
	private javax.swing.JPanel panelNormsGroups;
	private javax.swing.JScrollPane panelNormsInUse;
	private javax.swing.JProgressBar pbEffectiveness;
	private javax.swing.JProgressBar pbNecessity;
	private javax.swing.JProgressBar pbNormGroupEffectiveness;
	private javax.swing.JTabbedPane tabbedPanel;
	private javax.swing.JTree treeNormGroupsInUse;
	private javax.swing.JTree treeNormGroupsNotInUse;
	private javax.swing.JTree treeNormsInUse;
	private javax.swing.JTree treeNormsNotInUse;
	// End of variables declaration                   
}
