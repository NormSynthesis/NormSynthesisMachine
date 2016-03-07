/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.csic.iiia.nsm.visualization.norms;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import es.csic.iiia.nsm.NormSynthesisMachine;
import es.csic.iiia.nsm.config.Dimension;
import es.csic.iiia.nsm.config.Goal;
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
public class NormsInspectorPanel_ORIGINAL extends javax.swing.JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4850532986703932668L;
	
	private NormSynthesisMachine nsm;
	private NormativeNetwork normativeNetwork;
	private NormGroupNetwork normGroupNetwork;
	private Norm selectedNorm;
	private NormGroup selectedNormGroup;
	
	/**
	 * Creates new form NormsTracerPanel
	 */
	public NormsInspectorPanel_ORIGINAL(NormSynthesisInspector nsInspector) {
		initComponents();

		this.nsm = nsInspector.getNormSynthesisMachine();
		this.normativeNetwork = this.nsm.getNormativeNetwork();
		this.normGroupNetwork = this.nsm.getNormGroupNetwork();
		this.selectedNorm = null;
		this.selectedNormGroup = null;
		
		this.updateTreeNormsInUse();
		this.updateTreeDiscardedNorms();
		this.updateTreeNormGroupsInUse();
		this.updateTreeDiscardedNormGroups();
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
	private void treeDiscardedNormsChanged(TreeSelectionEvent evt) {
		if(treeDiscardedNorms.getLastSelectedPathComponent() 
				instanceof DefaultMutableTreeNode) {

			DefaultMutableTreeNode srcNode = 
					(DefaultMutableTreeNode)treeDiscardedNorms.getLastSelectedPathComponent();
			Object src = srcNode.getUserObject();

			if(src instanceof Norm) {
				this.selectedNorm = (Norm)src;
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
	private void updateTreeDiscardedNorms() {
		List<Norm> notRepresented = normativeNetwork.getNotRepresentedNorms();
		List<Norm> notRepresentedGeneralNorms = new ArrayList<Norm>();
		List<Norm> notRepresentedLeaves = new ArrayList<Norm>();
		TreePath focus = null;

		this.treeDiscardedNorms.removeAll();

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
		
		this.treeDiscardedNorms.setModel(tmodel);
		this.treeDiscardedNorms.setSelectionPath(focus);
		this.treeDiscardedNorms.validate();
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

		s += "\n\nParent norms\n";
		s += "-------";

		for(Norm parent : this.normativeNetwork.getParents(n)) {
			s += "\n" + parent.toString();
		}

		s += "\n\nChild norms\n";
		s += "--------";

		for(Norm child : this.normativeNetwork.getChildren(n)) {
			s += "\n" + child.toString();
		}
		
//		this.textNormDescription.setText(s);
	}

	
	/*
	 * NORM GROUPS
	 */
	
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
	private void treeDiscardedNormGroupsChanged(TreeSelectionEvent evt) {
		if(treeDiscardedNormGroups.getLastSelectedPathComponent() 
				instanceof DefaultMutableTreeNode) {

			DefaultMutableTreeNode srcNode = 
					(DefaultMutableTreeNode)treeDiscardedNormGroups.getLastSelectedPathComponent();
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
	private void updateTreeDiscardedNormGroups() {
		List<NormGroup> nonRepresented = normGroupNetwork.getNotRepresentedNormGroups();
		TreePath focus = null;

		Collections.sort(nonRepresented);
		
		this.treeDiscardedNormGroups.removeAll();

		/* Update tree of norms in use */
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(
				"Norm groups in use (" + nonRepresented.size() + ")");
		TreeModel tmodel = new DefaultTreeModel(rootNode);

		// Add norm groups that are being evaluated
		for(NormGroup normGroup : nonRepresented) {
			this.fillTree(rootNode, normGroup);
		}

		this.treeDiscardedNormGroups.setModel(tmodel);
		this.treeDiscardedNormGroups.setSelectionPath(focus);
		this.treeDiscardedNormGroups.validate();
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
		String s = "";
		NormGroup n = this.selectedNormGroup;
		

		for(NormGroup parent : this.normGroupNetwork.getParents(n)) {
			s += "\n" + parent.toString();
		}

		s += "\n\nChild norms\n";
		s += "--------";

		for(NormGroup child : this.normGroupNetwork.getChildren(n)) {
			s += "\n" + child.toString();
		}
		
//		this.textNormGroupDescription.setText(s);
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
		lblNSStrategyInfo.setText(this.nsm.getNormSynthesisSettings().
				getNormSynthesisStrategy().toString());
		lblGenModeInfo.setText(this.nsm.getNormSynthesisSettings().getNormGeneralisationMode().toString());
		lblGenStepInfo.setText(Integer.toString(this.nsm.getNormSynthesisSettings().getNormGeneralisationStep()));
		
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
		List<Norm> nsNorms = normativeNetwork.getNormativeSystem();

		/* Update norm synthesis metrics */
		this.lblNSCardinalityInfo.setText(String.valueOf(nsNorms.size()));
		this.lblNNCardinalityInfo.setText(String.valueOf(normativeNetwork.getCardinality()));
		
		this.updateTreeNormsInUse();
		this.updateTreeDiscardedNorms();
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
	 * 
	 * @param evt
	 */
	private void btnUpdateNormGroupsActionPerformed(java.awt.event.ActionEvent evt) {                                                    
		List<NormGroup> activeNormGroups = normGroupNetwork.getActiveNodes();

		/* Update norm synthesis metrics */
		this.lblNSCardinalityInfoNormGroup.setText(String.valueOf(activeNormGroups.size()));

		this.updateTreeNormGroupsInUse();
		this.updateTreeDiscardedNormGroups();
	}                                                   

	
	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")                          
	private void initComponents() {

		lblTitle = new javax.swing.JLabel();
		tabbedPanel = new javax.swing.JTabbedPane();
		panelNorms = new javax.swing.JPanel();
		panelSynthNorms = new javax.swing.JPanel();
		lblNormsInUse = new javax.swing.JLabel();
		panelNormsInUse = new javax.swing.JScrollPane();
		treeNormsInUse = new javax.swing.JTree();
		lblDiscardedNorms = new javax.swing.JLabel();
		panelDiscardedNorms = new javax.swing.JScrollPane();
		treeDiscardedNorms = new javax.swing.JTree();
		panelInspectedNorm = new javax.swing.JPanel();
		pbEffectiveness = new javax.swing.JProgressBar();
		pbNecessity = new javax.swing.JProgressBar();
		lblEffectiveness = new javax.swing.JLabel();
		lblNecessity = new javax.swing.JLabel();
		panelNormDescPanel = new javax.swing.JPanel();
		lblPerfRange = new javax.swing.JLabel();
		btnPerfRange = new javax.swing.JButton();
		panelNSMethod = new javax.swing.JPanel();
		lblNSStrategy = new javax.swing.JLabel();
		lblNSStrategyInfo = new javax.swing.JLabel();
		lblGenMode = new javax.swing.JLabel();
		lblGenModeInfo = new javax.swing.JLabel();
		lblGenStep = new javax.swing.JLabel();
		lblGenStepInfo = new javax.swing.JLabel();
		panelNSMetrics = new javax.swing.JPanel();
		lblNScardinality = new javax.swing.JLabel();
		lblNNCardinality = new javax.swing.JLabel();
		lblNSCardinalityInfo = new javax.swing.JLabel();
		lblNNCardinalityInfo = new javax.swing.JLabel();
		btnUpdate = new javax.swing.JButton();
		panelNormsGroups = new javax.swing.JPanel();
		panelSynthNormsGroups = new javax.swing.JPanel();
		lblNormGroupsInUse = new javax.swing.JLabel();
		panelNormGroupsInUse = new javax.swing.JScrollPane();
		treeNormGroupsInUse = new javax.swing.JTree();
		lblDiscardedNormsGroups = new javax.swing.JLabel();
		panelDiscardedNormsGroups = new javax.swing.JScrollPane();
		treeDiscardedNormGroups = new javax.swing.JTree();
		panelInspectedNormGroup = new javax.swing.JPanel();
		pbNormGroupEffectiveness = new javax.swing.JProgressBar();
		lblNormGroupEffectiveness = new javax.swing.JLabel();
		panelNormGroupDescPanel = new javax.swing.JPanel();
		lblNormGroupPerfRange = new javax.swing.JLabel();
		btnNormGroupPerfRange = new javax.swing.JButton();
		panelNSMethodNormGroup = new javax.swing.JPanel();
		lblNSStrategyNormGroup = new javax.swing.JLabel();
		lblNSStrategyInfoNormGroup = new javax.swing.JLabel();
		lblGenModeNormGroup = new javax.swing.JLabel();
		lblGenModeInfoNormGroup = new javax.swing.JLabel();
		lblGenStepNormGroup = new javax.swing.JLabel();
		lblGenStepInfoNormGroup = new javax.swing.JLabel();
		panelNSMetricsNormGroup = new javax.swing.JPanel();
		lblNScardinalityNormGroup = new javax.swing.JLabel();
		lblNNCardinalityNormGroup = new javax.swing.JLabel();
		lblNSCardinalityInfoNormGroup = new javax.swing.JLabel();
		lblNNCardinalityInfoNormGroup = new javax.swing.JLabel();
		btnUpdateNormGroups = new javax.swing.JButton();

		setBackground(java.awt.Color.lightGray);

		lblTitle.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
		lblTitle.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/magnifGlass.png")); // NOI18N
		lblTitle.setText("Norms Inspector");
		lblTitle.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		lblTitle.setOpaque(true);

		tabbedPanel.setBackground(java.awt.Color.lightGray);
		tabbedPanel.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N

		panelNorms.setBackground(java.awt.Color.lightGray);

		panelSynthNorms.setBackground(java.awt.Color.lightGray);
		panelSynthNorms.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Synthesised norms", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 1, 13), java.awt.Color.blue)); // NOI18N

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
		lblDiscardedNorms.setText("Discarded norms");

		treeDiscardedNorms.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		treeDiscardedNorms.setFont(new java.awt.Font("Arial", 0, 13)); // NOI18N
		panelDiscardedNorms.setViewportView(treeDiscardedNorms);
		treeDiscardedNorms.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
			public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
				treeDiscardedNormsChanged(evt);
			}
		});
		
		javax.swing.GroupLayout panelSynthNormsLayout = new javax.swing.GroupLayout(panelSynthNorms);
		panelSynthNorms.setLayout(panelSynthNormsLayout);
		panelSynthNormsLayout.setHorizontalGroup(
				panelSynthNormsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelSynthNormsLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelSynthNormsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(panelNormsInUse, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(lblNormsInUse))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
								.addGroup(panelSynthNormsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addGroup(panelSynthNormsLayout.createSequentialGroup()
												.addComponent(lblDiscardedNorms)
												.addGap(0, 0, Short.MAX_VALUE))
												.addComponent(panelDiscardedNorms))
												.addContainerGap())
				);
		panelSynthNormsLayout.setVerticalGroup(
				panelSynthNormsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelSynthNormsLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelSynthNormsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(lblDiscardedNorms)
								.addComponent(lblNormsInUse))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(panelSynthNormsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
										.addComponent(panelDiscardedNorms, javax.swing.GroupLayout.DEFAULT_SIZE, 308, Short.MAX_VALUE)
										.addComponent(panelNormsInUse, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
										.addGap(12, 12, 12))
				);

		panelInspectedNorm.setBackground(java.awt.Color.lightGray);
		panelInspectedNorm.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Inspected norm", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 1, 13), java.awt.Color.blue)); // NOI18N

		pbEffectiveness.setToolTipText("");
		pbEffectiveness.setStringPainted(true);

		pbNecessity.setStringPainted(true);

		lblEffectiveness.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
		lblEffectiveness.setText("Effectiveness");

		lblNecessity.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
		lblNecessity.setText("Necessity");

		panelNormDescPanel.setBackground(java.awt.Color.lightGray);
		panelNormDescPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

		javax.swing.GroupLayout panelNormDescPanelLayout = new javax.swing.GroupLayout(panelNormDescPanel);
		panelNormDescPanel.setLayout(panelNormDescPanelLayout);
		panelNormDescPanelLayout.setHorizontalGroup(
				panelNormDescPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGap(0, 0, Short.MAX_VALUE)
				);
		panelNormDescPanelLayout.setVerticalGroup(
				panelNormDescPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGap(0, 209, Short.MAX_VALUE)
				);

		lblPerfRange.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
		lblPerfRange.setText("Performance ranges");

		btnPerfRange.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/utility.png")); // NOI18N
		btnPerfRange.setText("Show");
		btnPerfRange.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnPerfRangeActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout panelInspectedNormLayout = new javax.swing.GroupLayout(panelInspectedNorm);
		panelInspectedNorm.setLayout(panelInspectedNormLayout);
		panelInspectedNormLayout.setHorizontalGroup(
				panelInspectedNormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelInspectedNormLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelInspectedNormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(panelNormDescPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addGroup(panelInspectedNormLayout.createSequentialGroup()
										.addComponent(lblPerfRange)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
						.addContainerGap()
						.addComponent(panelNormDescPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addGroup(panelInspectedNormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(pbEffectiveness, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(lblEffectiveness))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(panelInspectedNormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(lblNecessity)
										.addComponent(pbNecessity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGap(12, 12, 12)
										.addGroup(panelInspectedNormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(btnPerfRange)
												.addComponent(lblPerfRange))
												.addContainerGap(20, Short.MAX_VALUE))
				);

		panelNSMethod.setBackground(java.awt.Color.lightGray);
		panelNSMethod.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Norm synthesis method", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 1, 13), java.awt.Color.blue)); // NOI18N

		lblNSStrategy.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
		lblNSStrategy.setText("Norm synthesis strategy:");

		lblNSStrategyInfo.setFont(new java.awt.Font("Arial", 0, 13)); // NOI18N

		lblGenMode.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
		lblGenMode.setText("Norm generalisation mode: ");

		lblGenStep.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
		lblGenStep.setText("Generalisation step:");

		lblGenModeInfo.setFont(new java.awt.Font("Arial", 0, 13)); // NOI18N
		lblGenStepInfo.setFont(new java.awt.Font("Arial", 0, 13)); // NOI18N
		
		
		javax.swing.GroupLayout panelNSMethodLayout = new javax.swing.GroupLayout(panelNSMethod);
		panelNSMethod.setLayout(panelNSMethodLayout);
		panelNSMethodLayout.setHorizontalGroup(
				panelNSMethodLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelNSMethodLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelNSMethodLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(lblNSStrategy, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(lblGenMode))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(panelNSMethodLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addGroup(panelNSMethodLayout.createSequentialGroup()
												.addComponent(lblGenModeInfo)
												.addGap(18, 18, 18)
												.addComponent(lblGenStep)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(lblGenStepInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
												.addComponent(lblNSStrategyInfo))
												.addContainerGap(62, Short.MAX_VALUE))
				);
		panelNSMethodLayout.setVerticalGroup(
				panelNSMethodLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelNSMethodLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelNSMethodLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(lblNSStrategy)
								.addComponent(lblNSStrategyInfo))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(panelNSMethodLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(lblGenMode)
										.addComponent(lblGenModeInfo)
										.addComponent(lblGenStep)
										.addComponent(lblGenStepInfo))
										.addContainerGap(13, Short.MAX_VALUE))
				);

		panelNSMetrics.setBackground(java.awt.Color.lightGray);
		panelNSMetrics.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Norm synthesis metrics", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 1, 13), java.awt.Color.blue)); // NOI18N

		lblNScardinality.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
		lblNScardinality.setText("Normative system cardinality:");

		lblNNCardinality.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
		lblNNCardinality.setText("Normative network cardinality:");

		lblNSCardinalityInfo.setFont(new java.awt.Font("Arial", 0, 13)); // NOI18N
		lblNSCardinalityInfo.setText("20");

		lblNNCardinalityInfo.setFont(new java.awt.Font("Arial", 0, 13)); // NOI18N
		lblNNCardinalityInfo.setText("20");

		javax.swing.GroupLayout panelNSMetricsLayout = new javax.swing.GroupLayout(panelNSMetrics);
		panelNSMetrics.setLayout(panelNSMetricsLayout);
		panelNSMetricsLayout.setHorizontalGroup(
				panelNSMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelNSMetricsLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelNSMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(lblNNCardinality)
								.addComponent(lblNScardinality))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(panelNSMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addComponent(lblNSCardinalityInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(lblNNCardinalityInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
										.addContainerGap())
				);
		panelNSMetricsLayout.setVerticalGroup(
				panelNSMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelNSMetricsLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelNSMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(lblNScardinality)
								.addComponent(lblNSCardinalityInfo))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(panelNSMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(lblNNCardinality)
										.addComponent(lblNNCardinalityInfo))
										.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);

		btnUpdate.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
		btnUpdate.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/update.png")); // NOI18N
		btnUpdate.setText("Update");
		btnUpdate.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnUpdateActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout panelNormsLayout = new javax.swing.GroupLayout(panelNorms);
		panelNorms.setLayout(panelNormsLayout);
		panelNormsLayout.setHorizontalGroup(
				panelNormsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelNormsLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelNormsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(panelNormsLayout.createSequentialGroup()
										.addGroup(panelNormsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
												.addComponent(panelNSMethod, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
												.addComponent(panelSynthNorms, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
												.addGroup(panelNormsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(panelNSMetrics, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
														.addComponent(panelInspectedNorm, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
														.addGroup(panelNormsLayout.createSequentialGroup()
																.addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
																.addGap(0, 0, Short.MAX_VALUE)))
																.addContainerGap())
				);
		panelNormsLayout.setVerticalGroup(
				panelNormsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelNormsLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelNormsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
								.addComponent(panelNSMethod, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(panelNSMetrics, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(panelNormsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
										.addComponent(panelSynthNorms, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(panelInspectedNorm, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(btnUpdate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addGap(12, 12, 12))
				);

		tabbedPanel.addTab("Norms", panelNorms);

		panelNormsGroups.setBackground(java.awt.Color.lightGray);

		panelSynthNormsGroups.setBackground(java.awt.Color.lightGray);
		panelSynthNormsGroups.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Synthesised norm groups", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 1, 13), java.awt.Color.blue)); // NOI18N

		lblNormGroupsInUse.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
		lblNormGroupsInUse.setText("Norms in use");

		treeNormGroupsInUse.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		treeNormGroupsInUse.setFont(new java.awt.Font("Arial", 0, 13)); // NOI18N
		panelNormGroupsInUse.setViewportView(treeNormGroupsInUse);
		treeNormGroupsInUse.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
			public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
				treeNormGroupsInUseChanged(evt);
			}
		});	
		
		lblDiscardedNormsGroups.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
		lblDiscardedNormsGroups.setText("Discarded norms");

		treeDiscardedNormGroups.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		treeDiscardedNormGroups.setFont(new java.awt.Font("Arial", 0, 13)); // NOI18N
		panelDiscardedNormsGroups.setViewportView(treeDiscardedNormGroups);
		treeDiscardedNormGroups.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
			public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
				treeDiscardedNormGroupsChanged(evt);
			}
		});	

		javax.swing.GroupLayout panelSynthNormsGroupsLayout = new javax.swing.GroupLayout(panelSynthNormsGroups);
		panelSynthNormsGroups.setLayout(panelSynthNormsGroupsLayout);
		panelSynthNormsGroupsLayout.setHorizontalGroup(
				panelSynthNormsGroupsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelSynthNormsGroupsLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelSynthNormsGroupsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(panelNormGroupsInUse, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(lblNormGroupsInUse))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
								.addGroup(panelSynthNormsGroupsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addGroup(panelSynthNormsGroupsLayout.createSequentialGroup()
												.addComponent(lblDiscardedNormsGroups)
												.addGap(0, 0, Short.MAX_VALUE))
												.addComponent(panelDiscardedNormsGroups))
												.addContainerGap())
				);
		panelSynthNormsGroupsLayout.setVerticalGroup(
				panelSynthNormsGroupsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelSynthNormsGroupsLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelSynthNormsGroupsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(lblDiscardedNormsGroups)
								.addComponent(lblNormGroupsInUse))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(panelSynthNormsGroupsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
										.addComponent(panelDiscardedNormsGroups, javax.swing.GroupLayout.DEFAULT_SIZE, 308, Short.MAX_VALUE)
										.addComponent(panelNormGroupsInUse, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
										.addGap(12, 12, 12))
				);

		panelInspectedNormGroup.setBackground(java.awt.Color.lightGray);
		panelInspectedNormGroup.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Inspected norm group", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 1, 13), java.awt.Color.blue)); // NOI18N

		pbNormGroupEffectiveness.setToolTipText("");
		pbNormGroupEffectiveness.setStringPainted(true);

		lblNormGroupEffectiveness.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
		lblNormGroupEffectiveness.setText("Effectiveness");

		panelNormGroupDescPanel.setBackground(java.awt.Color.lightGray);
		panelNormGroupDescPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

		javax.swing.GroupLayout panelNormGroupDescPanelLayout = new javax.swing.GroupLayout(panelNormGroupDescPanel);
		panelNormGroupDescPanel.setLayout(panelNormGroupDescPanelLayout);
		panelNormGroupDescPanelLayout.setHorizontalGroup(
				panelNormGroupDescPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGap(0, 0, Short.MAX_VALUE)
				);
		panelNormGroupDescPanelLayout.setVerticalGroup(
				panelNormGroupDescPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGap(0, 209, Short.MAX_VALUE)
				);

		lblNormGroupPerfRange.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
		lblNormGroupPerfRange.setText("Performance ranges");

		btnNormGroupPerfRange.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/utility.png")); // NOI18N
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
								.addComponent(panelNormGroupDescPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addGroup(panelInspectedNormGroupLayout.createSequentialGroup()
										.addComponent(lblNormGroupPerfRange)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(btnNormGroupPerfRange))
										.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelInspectedNormGroupLayout.createSequentialGroup()
												.addComponent(lblNormGroupEffectiveness)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(pbNormGroupEffectiveness, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)))
												.addContainerGap())
				);
		panelInspectedNormGroupLayout.setVerticalGroup(
				panelInspectedNormGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelInspectedNormGroupLayout.createSequentialGroup()
						.addContainerGap()
						.addComponent(panelNormGroupDescPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addGroup(panelInspectedNormGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(pbNormGroupEffectiveness, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(lblNormGroupEffectiveness))
								.addGap(36, 36, 36)
								.addGroup(panelInspectedNormGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(btnNormGroupPerfRange)
										.addComponent(lblNormGroupPerfRange))
										.addContainerGap(21, Short.MAX_VALUE))
				);

		panelNSMethodNormGroup.setBackground(java.awt.Color.lightGray);
		panelNSMethodNormGroup.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Norm synthesis method", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 1, 13), java.awt.Color.blue)); // NOI18N

		lblNSStrategyNormGroup.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
		lblNSStrategyNormGroup.setText("Norm synthesis strategy:");

		lblNSStrategyInfoNormGroup.setFont(new java.awt.Font("Arial", 0, 13)); // NOI18N
		lblNSStrategyInfoNormGroup.setText("SIMON");

		lblGenModeNormGroup.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
		lblGenModeNormGroup.setText("Norm generalisation mode: ");

		lblGenModeInfoNormGroup.setFont(new java.awt.Font("Arial", 0, 13)); // NOI18N
		lblGenModeInfoNormGroup.setText("Deep");

		lblGenStepNormGroup.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
		lblGenStepNormGroup.setText("Generalisation step:");

		lblGenStepInfoNormGroup.setFont(new java.awt.Font("Arial", 0, 13)); // NOI18N
		lblGenStepInfoNormGroup.setText("1");

		javax.swing.GroupLayout panelNSMethodNormGroupLayout = new javax.swing.GroupLayout(panelNSMethodNormGroup);
		panelNSMethodNormGroup.setLayout(panelNSMethodNormGroupLayout);
		panelNSMethodNormGroupLayout.setHorizontalGroup(
				panelNSMethodNormGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelNSMethodNormGroupLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelNSMethodNormGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(lblNSStrategyNormGroup, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(lblGenModeNormGroup))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(panelNSMethodNormGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addGroup(panelNSMethodNormGroupLayout.createSequentialGroup()
												.addComponent(lblGenModeInfoNormGroup)
												.addGap(18, 18, 18)
												.addComponent(lblGenStepNormGroup)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(lblGenStepInfoNormGroup, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
												.addComponent(lblNSStrategyInfoNormGroup))
												.addContainerGap(62, Short.MAX_VALUE))
				);
		panelNSMethodNormGroupLayout.setVerticalGroup(
				panelNSMethodNormGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelNSMethodNormGroupLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelNSMethodNormGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(lblNSStrategyNormGroup)
								.addComponent(lblNSStrategyInfoNormGroup))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(panelNSMethodNormGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(lblGenModeNormGroup)
										.addComponent(lblGenModeInfoNormGroup)
										.addComponent(lblGenStepNormGroup)
										.addComponent(lblGenStepInfoNormGroup))
										.addContainerGap(13, Short.MAX_VALUE))
				);

		panelNSMetricsNormGroup.setBackground(java.awt.Color.lightGray);
		panelNSMetricsNormGroup.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Norm synthesis metrics", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 1, 13), java.awt.Color.blue)); // NOI18N

		lblNScardinalityNormGroup.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
		lblNScardinalityNormGroup.setText("Normative system cardinality:");

		lblNNCardinalityNormGroup.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
		lblNNCardinalityNormGroup.setText("Normative network cardinality:");

		lblNSCardinalityInfoNormGroup.setFont(new java.awt.Font("Arial", 0, 13)); // NOI18N
		lblNSCardinalityInfoNormGroup.setText("20");

		lblNNCardinalityInfoNormGroup.setFont(new java.awt.Font("Arial", 0, 13)); // NOI18N
		lblNNCardinalityInfoNormGroup.setText("20");

		javax.swing.GroupLayout panelNSMetricsNormGroupLayout = new javax.swing.GroupLayout(panelNSMetricsNormGroup);
		panelNSMetricsNormGroup.setLayout(panelNSMetricsNormGroupLayout);
		panelNSMetricsNormGroupLayout.setHorizontalGroup(
				panelNSMetricsNormGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelNSMetricsNormGroupLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelNSMetricsNormGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(lblNNCardinalityNormGroup)
								.addComponent(lblNScardinalityNormGroup))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(panelNSMetricsNormGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addComponent(lblNSCardinalityInfoNormGroup, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(lblNNCardinalityInfoNormGroup, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
										.addContainerGap())
				);
		panelNSMetricsNormGroupLayout.setVerticalGroup(
				panelNSMetricsNormGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelNSMetricsNormGroupLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelNSMetricsNormGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(lblNScardinalityNormGroup)
								.addComponent(lblNSCardinalityInfoNormGroup))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(panelNSMetricsNormGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(lblNNCardinalityNormGroup)
										.addComponent(lblNNCardinalityInfoNormGroup))
										.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);

		btnUpdateNormGroups.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
		btnUpdateNormGroups.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/update.png")); // NOI18N
		btnUpdateNormGroups.setText("Update");
		btnUpdateNormGroups.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnUpdateNormGroupsActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout panelNormsGroupsLayout = new javax.swing.GroupLayout(panelNormsGroups);
		panelNormsGroups.setLayout(panelNormsGroupsLayout);
		panelNormsGroupsLayout.setHorizontalGroup(
				panelNormsGroupsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelNormsGroupsLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelNormsGroupsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(panelNormsGroupsLayout.createSequentialGroup()
										.addGroup(panelNormsGroupsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
												.addComponent(panelNSMethodNormGroup, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
												.addComponent(panelSynthNormsGroups, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
												.addGroup(panelNormsGroupsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(panelNSMetricsNormGroup, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
														.addComponent(panelInspectedNormGroup, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
														.addGroup(panelNormsGroupsLayout.createSequentialGroup()
																.addComponent(btnUpdateNormGroups, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
																.addGap(0, 0, Short.MAX_VALUE)))
																.addContainerGap())
				);
		panelNormsGroupsLayout.setVerticalGroup(
				panelNormsGroupsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelNormsGroupsLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelNormsGroupsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
								.addComponent(panelNSMethodNormGroup, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(panelNSMetricsNormGroup, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(panelNormsGroupsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
										.addComponent(panelSynthNormsGroups, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(panelInspectedNormGroup, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(btnUpdateNormGroups, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addGap(12, 12, 12))
				);

		panelInspectedNormGroup.getAccessibleContext().setAccessibleName("Inspected norm group");

		tabbedPanel.addTab("Norm Groups", panelNormsGroups);

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
						.addContainerGap()
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(tabbedPanel)
								.addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
								.addContainerGap())
				);
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblTitle)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(tabbedPanel)
						.addContainerGap())
				);

		tabbedPanel.getAccessibleContext().setAccessibleName("Norms");
		
		panelNormsGroups.setVisible(false);
	}// </editor-fold>                        


	// Variables declaration - do not modify                     
	private javax.swing.JButton btnNormGroupPerfRange;
	private javax.swing.JButton btnPerfRange;
	private javax.swing.JButton btnUpdate;
	private javax.swing.JButton btnUpdateNormGroups;
	private javax.swing.JLabel lblDiscardedNorms;
	private javax.swing.JLabel lblDiscardedNormsGroups;
	private javax.swing.JLabel lblEffectiveness;
	private javax.swing.JLabel lblGenMode;
	private javax.swing.JLabel lblGenModeInfo;
	private javax.swing.JLabel lblGenModeInfoNormGroup;
	private javax.swing.JLabel lblGenModeNormGroup;
	private javax.swing.JLabel lblGenStep;
	private javax.swing.JLabel lblGenStepInfo;
	private javax.swing.JLabel lblGenStepInfoNormGroup;
	private javax.swing.JLabel lblGenStepNormGroup;
	private javax.swing.JLabel lblNNCardinality;
	private javax.swing.JLabel lblNNCardinalityInfo;
	private javax.swing.JLabel lblNNCardinalityInfoNormGroup;
	private javax.swing.JLabel lblNNCardinalityNormGroup;
	private javax.swing.JLabel lblNSCardinalityInfo;
	private javax.swing.JLabel lblNSCardinalityInfoNormGroup;
	private javax.swing.JLabel lblNSStrategy;
	private javax.swing.JLabel lblNSStrategyInfo;
	private javax.swing.JLabel lblNSStrategyInfoNormGroup;
	private javax.swing.JLabel lblNSStrategyNormGroup;
	private javax.swing.JLabel lblNScardinality;
	private javax.swing.JLabel lblNScardinalityNormGroup;
	private javax.swing.JLabel lblNecessity;
	private javax.swing.JLabel lblNormGroupEffectiveness;
	private javax.swing.JLabel lblNormGroupPerfRange;
	private javax.swing.JLabel lblNormGroupsInUse;
	private javax.swing.JLabel lblNormsInUse;
	private javax.swing.JLabel lblPerfRange;
	private javax.swing.JLabel lblTitle;
	private javax.swing.JScrollPane panelNormsInUse;
	private javax.swing.JScrollPane panelDiscardedNorms;
	private javax.swing.JScrollPane panelDiscardedNormsGroups;
	private javax.swing.JPanel panelInspectedNorm;
	private javax.swing.JPanel panelInspectedNormGroup;
	private javax.swing.JPanel panelNSMethod;
	private javax.swing.JPanel panelNSMethodNormGroup;
	private javax.swing.JPanel panelNSMetrics;
	private javax.swing.JPanel panelNSMetricsNormGroup;
	private javax.swing.JPanel panelNormDescPanel;
	private javax.swing.JPanel panelNormGroupDescPanel;
	private javax.swing.JScrollPane panelNormGroupsInUse;
	private javax.swing.JPanel panelNorms;
	private javax.swing.JPanel panelNormsGroups;
	private javax.swing.JPanel panelSynthNorms;
	private javax.swing.JPanel panelSynthNormsGroups;
	private javax.swing.JProgressBar pbEffectiveness;
	private javax.swing.JProgressBar pbNecessity;
	private javax.swing.JProgressBar pbNormGroupEffectiveness;
	private javax.swing.JTabbedPane tabbedPanel;
	private javax.swing.JTree treeDiscardedNorms;
	private javax.swing.JTree treeDiscardedNormGroups;
	private javax.swing.JTree treeNormsInUse;
	private javax.swing.JTree treeNormGroupsInUse;
	// End of variables declaration                   
}
