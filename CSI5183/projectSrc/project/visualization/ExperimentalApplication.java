package project.visualization;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JLabel;

import java.awt.Font;

import javax.swing.JSlider;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.eclipse.wb.swing.FocusTraversalOnArray;
import org.moeaframework.Executor;
import org.moeaframework.Instrumenter;
import org.moeaframework.analysis.collector.Accumulator;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.Permutation;

import project.generation.InstanceGenerator;
import project.problem.RRASRMOO;
import project.problem.types.Coordinate;
import project.problem.types.NodeInfo;

import java.awt.Component;

import javax.swing.JComboBox;
import javax.swing.JTree;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JScrollPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeSelectionModel;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import org.math.plot.Plot3DPanel;

import javax.swing.border.LineBorder;

import java.awt.Color;

import org.math.plot.Plot2DPanel;

@SuppressWarnings(value = { "rawtypes", "unchecked" })
public class ExperimentalApplication {

	//UI Components
	private JFrame frmCsiProject;
	private JTextField populationField;
	private JTextField generationsField;
	private static JTextField hypervolumeField;
	private static JTextField spacingField;
	private static JTextField contributionField;
	private static JTextField genDistField;
	private static JTextField paretoErrorField;
	private static JTextField runtimeField;
	private static JComboBox list;
	private FileTreeModel model;
	private static TourVisualizer tourVisualizer;
	private static JTabbedPane tabbedPane;
	
	public static Plot3DPanel paretoViewer;
	public static Plot2DPanel metricGraphViewer;

	
	//Execution variables
	private static File instance;
	private static int population = 200;
	private static int generations = 1;
	private static String algorithm = "NSGAII";
	private static int selectedGeneration = 1;
	private static boolean withReferenceSet = false;
	
	//Where we hold the results of our run
	private static Accumulator accumulator = null;
	private JTextField numNodesField;
	private JTextField distributionField;
	private JTextField sparsityField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ExperimentalApplication window = new ExperimentalApplication();
					window.frmCsiProject.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ExperimentalApplication() {
		model = new FileTreeModel(new File("Instances"));
		
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmCsiProject = new JFrame();
		frmCsiProject.setTitle("CSI5183 - Project");
		frmCsiProject.setBounds(100, 100, 1160, 700);
		frmCsiProject.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				
		JLabel lblExperimentalRunner = new JLabel("RRASR Experiment Runner");
		lblExperimentalRunner.setFont(new Font("Tahoma", Font.PLAIN, 19));
		
		JPanel parametersPanel = new JPanel();
		
		final JComboBox algorithmSpinner = new JComboBox();
		algorithmSpinner.setModel(new DefaultComboBoxModel(new String[] {"NSGAII", "NSGAIII", "SPEA2", "PAES"}));
		
		JLabel lblAlgorithm = new JLabel("Algorithm");
		lblAlgorithm.setHorizontalAlignment(SwingConstants.CENTER);
		lblAlgorithm.setFont(new Font("Tahoma", Font.BOLD, 12));
		
		JLabel lblProblemInstance = new JLabel("Problem Instance");
		lblProblemInstance.setHorizontalAlignment(SwingConstants.CENTER);
		lblProblemInstance.setFont(new Font("Tahoma", Font.BOLD, 12));
		
		populationField = new JTextField();
		populationField.setText("200");
		populationField.setColumns(10);
		
		JLabel lblPopulationSize = new JLabel("Population Size");
		lblPopulationSize.setHorizontalAlignment(SwingConstants.CENTER);
		lblPopulationSize.setFont(new Font("Tahoma", Font.BOLD, 12));
		
		JLabel lblOfGenerations = new JLabel("# of Generations");
		lblOfGenerations.setHorizontalAlignment(SwingConstants.CENTER);
		lblOfGenerations.setFont(new Font("Tahoma", Font.BOLD, 12));
		
		generationsField = new JTextField();
		generationsField.setText("500");
		generationsField.setColumns(10);
		
		final JLabel genNumberLabel = new JLabel("NaN");
		
		final JSlider genSlider = new JSlider();
		genSlider.setMinimum(1);
		genSlider.setMaximum(1);
		genSlider.setValue(1);
		
		
		final JButton runButton = new JButton("Run");
		runButton.setEnabled(false);
		runButton.setFont(new Font("Tahoma", Font.BOLD, 12));
		
		JPanel resultsPanel = new JPanel();
		
		JLabel lblHypervolume = new JLabel("Hypervolume");
		lblHypervolume.setHorizontalAlignment(SwingConstants.CENTER);
		lblHypervolume.setFont(new Font("Tahoma", Font.BOLD, 12));
		
		hypervolumeField = new JTextField();
		hypervolumeField.setEditable(false);
		hypervolumeField.setColumns(10);
		
		JLabel lblNewLabel = new JLabel("Spacing");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		
		spacingField = new JTextField();
		spacingField.setEditable(false);
		spacingField.setColumns(10);
		
		JLabel lblNewLabel_1 = new JLabel("Contribution");
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1.setFont(new Font("Tahoma", Font.BOLD, 12));
		
		contributionField = new JTextField();
		contributionField.setEditable(false);
		contributionField.setColumns(10);
		
		JLabel lblNewLabel_2 = new JLabel("Generational Distance");
		lblNewLabel_2.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_2.setFont(new Font("Tahoma", Font.BOLD, 12));
		
		genDistField = new JTextField();
		genDistField.setEditable(false);
		genDistField.setColumns(10);
		
		JLabel lblNewLabel_3 = new JLabel("Max Pareto Front Error");
		lblNewLabel_3.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_3.setFont(new Font("Tahoma", Font.BOLD, 12));
		
		paretoErrorField = new JTextField();
		paretoErrorField.setEditable(false);
		paretoErrorField.setColumns(10);
		
		JLabel lblNewLabel_4 = new JLabel("Runtime (s)");
		lblNewLabel_4.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_4.setFont(new Font("Tahoma", Font.BOLD, 12));
		
		runtimeField = new JTextField();
		runtimeField.setEditable(false);
		runtimeField.setColumns(10);		
		
		JButton lastGenButton = new JButton("Last Generation");		
		JButton firstGenButton = new JButton("First Generation");
		
		JLabel genSliderLabel = new JLabel("Generation Selector");
		genSliderLabel.setHorizontalAlignment(SwingConstants.CENTER);
		genSliderLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		

		genNumberLabel.setHorizontalAlignment(SwingConstants.CENTER);
		GroupLayout gl_resultsPanel = new GroupLayout(resultsPanel);
		gl_resultsPanel.setHorizontalGroup(
			gl_resultsPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_resultsPanel.createSequentialGroup()
					.addGroup(gl_resultsPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_resultsPanel.createSequentialGroup()
							.addContainerGap()
							.addGroup(gl_resultsPanel.createParallelGroup(Alignment.LEADING)
								.addComponent(lblNewLabel_1, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
								.addComponent(lblNewLabel, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
								.addComponent(lblHypervolume, GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)))
						.addGroup(gl_resultsPanel.createSequentialGroup()
							.addContainerGap()
							.addComponent(lblNewLabel_2, GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE))
						.addGroup(gl_resultsPanel.createSequentialGroup()
							.addContainerGap()
							.addComponent(lblNewLabel_3, GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE))
						.addGroup(gl_resultsPanel.createSequentialGroup()
							.addContainerGap()
							.addComponent(lblNewLabel_4, GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE))
						.addGroup(gl_resultsPanel.createSequentialGroup()
							.addGap(43)
							.addComponent(paretoErrorField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_resultsPanel.createSequentialGroup()
							.addGap(43)
							.addComponent(genDistField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_resultsPanel.createSequentialGroup()
							.addGap(43)
							.addComponent(contributionField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_resultsPanel.createSequentialGroup()
							.addGap(43)
							.addComponent(runtimeField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addGap(33)))
					.addContainerGap())
				.addGroup(gl_resultsPanel.createSequentialGroup()
					.addContainerGap(43, Short.MAX_VALUE)
					.addComponent(spacingField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(43))
				.addGroup(gl_resultsPanel.createSequentialGroup()
					.addContainerGap(43, Short.MAX_VALUE)
					.addComponent(hypervolumeField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(43))
				.addGroup(gl_resultsPanel.createSequentialGroup()
					.addGap(32)
					.addGroup(gl_resultsPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(firstGenButton)
						.addComponent(lastGenButton))
					.addContainerGap(31, Short.MAX_VALUE))
				.addGroup(gl_resultsPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(genSliderLabel, GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
					.addContainerGap())
				.addGroup(gl_resultsPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(genNumberLabel, GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
					.addContainerGap())
				.addGroup(gl_resultsPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(genSlider, GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
					.addContainerGap())
		);
		gl_resultsPanel.setVerticalGroup(
			gl_resultsPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_resultsPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblHypervolume)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(hypervolumeField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(11)
					.addComponent(lblNewLabel)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(spacingField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(11)
					.addComponent(lblNewLabel_1)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(contributionField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(11)
					.addComponent(lblNewLabel_2)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(genDistField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblNewLabel_3)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(paretoErrorField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(11)
					.addComponent(lblNewLabel_4)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(runtimeField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED, 99, Short.MAX_VALUE)
					.addComponent(genSliderLabel)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(genSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(genNumberLabel)
					.addGap(25)
					.addComponent(firstGenButton)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(lastGenButton)
					.addGap(23))
		);
		resultsPanel.setLayout(gl_resultsPanel);
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);

		
		paretoViewer = new Plot3DPanel();
		tabbedPane.addTab("Pareto Set Viewer", null, paretoViewer, null);
		
		JPanel tourTab = new JPanel();
		tabbedPane.addTab("Solution Viewer", null, tourTab, null);
		tourTab.setLayout(null);
		
		JLabel solutionSelectLabel = new JLabel("Solution:");
		solutionSelectLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		solutionSelectLabel.setBounds(10, 520, 56, 21);
		tourTab.add(solutionSelectLabel);
		
		list = new JComboBox();
		list.setModel(new DefaultComboBoxModel(new String[] {}));
		list.setBounds(76, 521, 434, 20);
		tourTab.add(list);
		
		tourVisualizer = new TourVisualizer();
		tourVisualizer.setBorder(new LineBorder(new Color(0, 0, 0)));
		tourVisualizer.setBounds(10, 10, 500, 500);
		tourTab.add(tourVisualizer);
		
		JTabbedPane instanceTabs = new JTabbedPane(JTabbedPane.TOP);
		
		GroupLayout gl_parametersPanel = new GroupLayout(parametersPanel);
		gl_parametersPanel.setHorizontalGroup(
			gl_parametersPanel.createParallelGroup(Alignment.LEADING)
				.addComponent(lblProblemInstance, GroupLayout.DEFAULT_SIZE, 362, Short.MAX_VALUE)
				.addGroup(gl_parametersPanel.createSequentialGroup()
					.addGap(153)
					.addComponent(runButton)
					.addContainerGap(152, Short.MAX_VALUE))
				.addGroup(gl_parametersPanel.createSequentialGroup()
					.addGap(105)
					.addComponent(generationsField, GroupLayout.PREFERRED_SIZE, 152, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(105, Short.MAX_VALUE))
				.addGroup(gl_parametersPanel.createSequentialGroup()
					.addGap(105)
					.addComponent(populationField, GroupLayout.PREFERRED_SIZE, 152, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(105, Short.MAX_VALUE))
				.addGroup(gl_parametersPanel.createSequentialGroup()
					.addGap(105)
					.addComponent(algorithmSpinner, GroupLayout.PREFERRED_SIZE, 152, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(105, Short.MAX_VALUE))
				.addGroup(Alignment.TRAILING, gl_parametersPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_parametersPanel.createParallelGroup(Alignment.TRAILING)
						.addComponent(instanceTabs, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 342, Short.MAX_VALUE)
						.addComponent(lblPopulationSize, GroupLayout.DEFAULT_SIZE, 342, Short.MAX_VALUE)
						.addComponent(lblOfGenerations, GroupLayout.DEFAULT_SIZE, 342, Short.MAX_VALUE))
					.addContainerGap())
				.addGroup(gl_parametersPanel.createSequentialGroup()
					.addGap(15)
					.addComponent(lblAlgorithm, GroupLayout.DEFAULT_SIZE, 332, Short.MAX_VALUE)
					.addGap(15))
		);
		gl_parametersPanel.setVerticalGroup(
			gl_parametersPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_parametersPanel.createSequentialGroup()
					.addComponent(lblProblemInstance, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(instanceTabs, GroupLayout.PREFERRED_SIZE, 311, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblAlgorithm, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE)
					.addGap(2)
					.addComponent(algorithmSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(lblPopulationSize)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(populationField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(lblOfGenerations)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(generationsField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(41)
					.addComponent(runButton)
					.addContainerGap())
		);
		
		JScrollPane scrollPane = new JScrollPane();
		instanceTabs.addTab("Pre-Generated Instances", null, scrollPane, null);
		
		JTree tree_1 = new JTree(model);
		scrollPane.setViewportView(tree_1);
		tree_1.setRootVisible(false);
		
		JPanel customInstancePanel = new JPanel();
		instanceTabs.addTab("Custom Instance", null, customInstancePanel, null);
		
		JLabel lblNewLabel_5 = new JLabel("Number of Nodes");
		lblNewLabel_5.setFont(new Font("Tahoma", Font.BOLD, 12));
		
		JLabel lblNumberOfSensing = new JLabel("Number of Sensing Holes (% of nodes)");
		lblNumberOfSensing.setFont(new Font("Tahoma", Font.BOLD, 12));
		
		JLabel lblSparcityOfNodes = new JLabel("Sparsity of Passive Nodes (Higher is sparser)");
		lblSparcityOfNodes.setFont(new Font("Tahoma", Font.BOLD, 12));
		
		JButton genInstanceButton = new JButton("Generate Instance");
		genInstanceButton.setFont(new Font("Tahoma", Font.BOLD, 12));
		
		numNodesField = new JTextField();
		numNodesField.setText("200");
		numNodesField.setColumns(10);
		
		distributionField = new JTextField();
		distributionField.setText("15");
		distributionField.setColumns(10);
		
		sparsityField = new JTextField();
		sparsityField.setText("10");
		sparsityField.setColumns(10);
		GroupLayout gl_customInstancePanel = new GroupLayout(customInstancePanel);
		gl_customInstancePanel.setHorizontalGroup(
			gl_customInstancePanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_customInstancePanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_customInstancePanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_customInstancePanel.createSequentialGroup()
							.addGroup(gl_customInstancePanel.createParallelGroup(Alignment.LEADING)
								.addComponent(lblNewLabel_5)
								.addGroup(gl_customInstancePanel.createSequentialGroup()
									.addGap(93)
									.addComponent(genInstanceButton)))
							.addContainerGap(89, Short.MAX_VALUE))
						.addGroup(gl_customInstancePanel.createSequentialGroup()
							.addComponent(lblNumberOfSensing, GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
							.addGap(69))
						.addGroup(gl_customInstancePanel.createSequentialGroup()
							.addComponent(numNodesField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addContainerGap(241, Short.MAX_VALUE))
						.addGroup(gl_customInstancePanel.createSequentialGroup()
							.addComponent(distributionField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addContainerGap(241, Short.MAX_VALUE))
						.addGroup(gl_customInstancePanel.createSequentialGroup()
							.addComponent(sparsityField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addContainerGap(241, Short.MAX_VALUE))
						.addGroup(gl_customInstancePanel.createSequentialGroup()
							.addComponent(lblSparcityOfNodes, GroupLayout.PREFERRED_SIZE, 284, GroupLayout.PREFERRED_SIZE)
							.addContainerGap())))
		);
		gl_customInstancePanel.setVerticalGroup(
			gl_customInstancePanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_customInstancePanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblNewLabel_5)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(numNodesField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(20)
					.addComponent(lblNumberOfSensing, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(distributionField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(19)
					.addComponent(lblSparcityOfNodes, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(sparsityField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED, 76, Short.MAX_VALUE)
					.addComponent(genInstanceButton)
					.addContainerGap())
		);
		customInstancePanel.setLayout(gl_customInstancePanel);
		
		tree_1.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		
		parametersPanel.setLayout(gl_parametersPanel);
		GroupLayout groupLayout = new GroupLayout(frmCsiProject.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(parametersPanel, GroupLayout.PREFERRED_SIZE, 362, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED, 50, Short.MAX_VALUE)
					.addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, 525, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(resultsPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(19))
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(435)
					.addComponent(lblExperimentalRunner)
					.addContainerGap(482, Short.MAX_VALUE))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblExperimentalRunner)
					.addGap(13)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(parametersPanel, GroupLayout.PREFERRED_SIZE, 580, GroupLayout.PREFERRED_SIZE)
						.addComponent(resultsPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, 580, GroupLayout.PREFERRED_SIZE))
					.addGap(35))
		);
		
		metricGraphViewer = new Plot2DPanel();
		tabbedPane.addTab("Metric Graphs", null, metricGraphViewer, null);
		

		frmCsiProject.getContentPane().setLayout(groupLayout);
		frmCsiProject.getContentPane().setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{tabbedPane, lblExperimentalRunner}));
		
		//ACTION LISTENERS -------------------------------------------------------------------------------------------------------------------------
		runButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				population = (Integer.parseInt(populationField.getText()));
				generations = (Integer.parseInt(generationsField.getText()));				
				runExperiment();
				
				genSlider.setMinimum(1);
				genSlider.setMaximum(accumulator.size("Approximation Set"));
				genSlider.setValue(generations);
				
				//Enable the metrics graph if we have a reference set
				if (withReferenceSet) {
					tabbedPane.setEnabledAt(2, true);
				}
				
				displayGenerationInfo();
				updateMetricGraphs();
			}
		});
		
		tree_1.addTreeSelectionListener(new SelectionListener() {
			public void valueChanged(TreeSelectionEvent se) {
				JTree tree = (JTree) se.getSource();
				File selectedNode = (File) tree.getLastSelectedPathComponent();
				if (selectedNode.isFile()) {
					//Reset all items
					reset();
					genNumberLabel.setText("NaN");
					genSlider.setMinimum(1);
					genSlider.setMaximum(1);
					genSlider.setValue(1);
					tourVisualizer.reset();
					accumulator = null;
					//End of item reset
					
					instance = ((File) selectedNode);					
					if (tabbedPane.getSelectedIndex() == 1) {
						loadInstanceInformation();
						printROI();
					}
					runButton.setEnabled(true);
				}				
			}			
		});
		
		genSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				selectedGeneration = genSlider.getValue();
				genNumberLabel.setText(Integer.toString(selectedGeneration));
				displayGenerationInfo();
			}
		});
		
		algorithmSpinner.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				algorithm = algorithmSpinner.getSelectedItem().toString();
			}
		});
		
		firstGenButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedGeneration = 1;
				genSlider.setValue(1);
				genNumberLabel.setText(Integer.toString(1));
				displayGenerationInfo();
			}
		});
		
		lastGenButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedGeneration = generations;
				genSlider.setValue(generations);
				genNumberLabel.setText(Integer.toString(generations));
				displayGenerationInfo();
			}
		});
		
		list.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				printROI();
				printSolutionPath(list.getSelectedIndex());
			}
		});
		
		tabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				int selected = tabbedPane.getSelectedIndex();
				//0 is Pareto Set Viewer
				//1 is Solution Viewer
				//2 is Metric Graphs
				
				if (selected == 1 && instance != null) {
					loadInstanceInformation();
					printROI();
				} 
			}
		});
		
		genInstanceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int numNodes = Integer.parseInt(numNodesField.getText());
				int sparsity = Integer.parseInt(sparsityField.getText());
				int distribution = Integer.parseInt(distributionField.getText());
				
				//Reset all items
				reset();
				genNumberLabel.setText("NaN");
				genSlider.setMinimum(1);
				genSlider.setMaximum(1);
				genSlider.setValue(1);
				tourVisualizer.reset();
				accumulator = null;
				//End of item reset
				
				generateCustomInstance(numNodes, sparsity, distribution); 					
				if (tabbedPane.getSelectedIndex() == 1) {
					loadInstanceInformation();
					printROI();
				}
				runButton.setEnabled(true);		
			}
		});
		
		//END ACTION LISTENERS -------------------------------------------------------------------------------------------------------------------------
	}

	@SuppressWarnings("unused")
	private void runExperiment() {		
		File referenceFile = null;
		
		try {
			String temp = instance.getCanonicalPath().replace('\\', '/');			
			String[] fileString = temp.split("/");
			
			String refFilePath = "";
			
			for (int i = 0; i < fileString.length; i++) {
				if (i == fileString.length-1) {
					refFilePath += "Reference\\" + fileString[i];
				} else {
					refFilePath += fileString[i] + "\\";
				}
			}			
			referenceFile =  new File(refFilePath);
			
			if (referenceFile.exists() && referenceFile.isFile()) {
				withReferenceSet = true;
			} else {
				withReferenceSet = false;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		int evaluations = population * generations;
		Instrumenter instrumenter;
		
		if (withReferenceSet) {
			instrumenter = new Instrumenter()
			.withProblemClass(RRASRMOO.class, instance)
			.withReferenceSet(referenceFile)
			.attachAll()
			.withFrequency(population);

			// solve using a Genetic Algorithm
			final NondominatedPopulation result = new Executor()
					.withProblemClass(RRASRMOO.class, instance)
					.withAlgorithm(algorithm)
					.withMaxEvaluations(evaluations)
					.withProperty("populationSize", population)
					.withProperty("swap.rate", 0.25) // swap mutation
					.withProperty("insertion.rate", 0.25) // insertion mutation
					.withProperty("pmx.rate", 0.75) // partially mapped crossover
					.withInstrumenter(instrumenter)
					.run();
			
		} else {
			instrumenter = new Instrumenter()
			.withProblemClass(RRASRMOO.class, instance)
			.attachElapsedTimeCollector()
			.attachApproximationSetCollector()
			.withFrequency(population);
			
			// solve using a Genetic Algorithm
			final NondominatedPopulation result = new Executor()
				.withProblemClass(RRASRMOO.class, instance)
				.withAlgorithm(algorithm)
				.withMaxEvaluations(evaluations)
				.withProperty("populationSize", population)
				.withProperty("swap.rate", 0.25) // swap mutation
				.withProperty("insertion.rate", 0.25) // insertion mutation
				.withProperty("pmx.rate", 0.75) // partially mapped crossover
//				.withEpsilon(5)
//				.distributeOnAllCores()
				.withInstrumenter(instrumenter)
				.run();

			
		}
		accumulator = instrumenter.getLastAccumulator();
	}
	
	public void displayGenerationInfo() {
		if (accumulator != null) {
			updateMetricFields();
			updateParetoViewer();
			if (tabbedPane.getSelectedIndex() == 1) {
				updatePathSelector();
				printROI();
			}
		}
	}
	
	private void updateMetricFields() {
		if (withReferenceSet) {
			hypervolumeField.setText("" + accumulator.get("Hypervolume", selectedGeneration-1));
			spacingField.setText("" + accumulator.get("Spacing", selectedGeneration-1));
			contributionField.setText("" + accumulator.get("Contribution", selectedGeneration-1));
			genDistField.setText("" + accumulator.get("Generational Distance", selectedGeneration-1));
			paretoErrorField.setText("" + accumulator.get("Maximum Pareto Front Error", selectedGeneration-1));
		} else {
			hypervolumeField.setText("No Reference Set");
			spacingField.setText("No Reference Set");
			contributionField.setText("No Reference Set");
			genDistField.setText("No Reference Set");
			paretoErrorField.setText("No Reference Set");		
		}
		DecimalFormat df = new DecimalFormat("#.###");
		
		runtimeField.setText(df.format(((Double) accumulator.get("Elapsed Time", selectedGeneration-1))));
	}
	
	private void updateParetoViewer() {
		ArrayList<Solution> result = (ArrayList<Solution>) accumulator.get("Approximation Set", selectedGeneration-1);
		
		paretoViewer.removeAllPlots();
		
		double[] X = new double[result.size()];
		double[] Y = new double[result.size()];
		double[] Z = new double[result.size()];
		
		for (int i = 0; i < result.size(); i++) {
			X[i] = -result.get(i).getObjective(0); //Lifetime
			Y[i] = -result.get(i).getObjective(1); //Robustness
			Z[i] = result.get(i).getObjective(2); //Length
		}
		
		paretoViewer.addScatterPlot("Non-Dominated Solution Set", X, Y, Z);
		paretoViewer.setAxisLabels("Path Lifetime","Path Robustness","Path Length");
		paretoViewer.setAutoBounds();	
	}
	
	private void updatePathSelector() {
		ArrayList<Solution> result = (ArrayList<Solution>) accumulator.get("Approximation Set", selectedGeneration-1);
		
		String[] solutionStrings = new String[result.size()];
		
		DecimalFormat df = new DecimalFormat("#.##");
		
		for (int i = 0; i < result.size(); i++) {
			solutionStrings[i] = "Length: " + df.format(result.get(i).getObjective(2)) + " Robustness: " + -result.get(i).getObjective(1) + " Lifetime: " + -result.get(i).getObjective(0);
		}
		
		list.setModel(new DefaultComboBoxModel(solutionStrings));
	}
	
	private void updateMetricGraphs() {		
		accumulator.size("NFE");
		
		double[] X = new double[accumulator.size("NFE")];
		
		for (int i = 0; i < accumulator.size("NFE"); i++) {
			X[i] = ((Integer)accumulator.get("NFE", i))/population;
		}
		
		String name = "Elapsed Time";
		double[] Y = new double[accumulator.size("Elapsed Time")];		
		for (int i = 0; i<accumulator.size("Elapsed Time"); i++) {
			Y[i] = (Double) accumulator.get("Elapsed Time", i);
		}
		metricGraphViewer.addScatterPlot(name, X, Y);
		
		if (withReferenceSet) {
			name = "Hypervolume";
			Y = new double[accumulator.size(name)];		
			for (int i = 0; i<accumulator.size(name); i++) {
				Y[i] = (Double) accumulator.get(name, i);
			}
			metricGraphViewer.addScatterPlot(name, X, Y);
			
			name = "Spacing";
			Y = new double[accumulator.size(name)];		
			for (int i = 0; i<accumulator.size(name); i++) {
				Y[i] = (Double) accumulator.get(name, i);
			}
			metricGraphViewer.addScatterPlot(name, X, Y);
			
			name = "Contribution";
			Y = new double[accumulator.size(name)];		
			for (int i = 0; i<accumulator.size(name); i++) {
				Y[i] = (Double) accumulator.get(name, i);
			}
			metricGraphViewer.addScatterPlot(name, X, Y);
			
			name = "Generational Distance";
			Y = new double[accumulator.size(name)];		
			for (int i = 0; i<accumulator.size(name); i++) {
				Y[i] = (Double) accumulator.get(name, i);
			}
			metricGraphViewer.addScatterPlot(name, X, Y);
			
			name = "Maximum Pareto Front Error";
			Y = new double[accumulator.size(name)];		
			for (int i = 0; i<accumulator.size(name); i++) {
				Y[i] = (Double) accumulator.get(name, i);
			}
			metricGraphViewer.addScatterPlot(name, X, Y);
		}
		
		metricGraphViewer.setAxisLabels("Generation","Metric");
	}
	
	private void printSolutionPath(int solutionIndex) {
		ArrayList<Solution> result = (ArrayList<Solution>) accumulator.get("Approximation Set", selectedGeneration-1);
		Solution selected = result.get(solutionIndex);
		tourVisualizer.updateSelection(((Permutation) selected.getVariable(0)).toArray());
		tourVisualizer.paintNodes(tourVisualizer.getGraphics());
		tourVisualizer.paintSolution(tourVisualizer.getGraphics());
	}
	
	private void printROI(){
		tourVisualizer.paintNodes(tourVisualizer.getGraphics());
	}
	
	private void loadInstanceInformation() {
		try {
			ArrayList<Integer> sensingHoles = new ArrayList<Integer>();

			Scanner scanner;
			scanner = new Scanner(instance, StandardCharsets.UTF_8.name());

			while(!scanner.nextLine().equals("DATASTART")) {}

			//NODE	X_LOC	Y_LOC	DEMAND	BATTERY
			String[] nodeString = scanner.nextLine().split("\t");

			int demand = Integer.parseInt(nodeString[3]);
			double battery = Double.parseDouble(nodeString[4]);
			Coordinate location = new Coordinate(Double.parseDouble(nodeString[1]), Double.parseDouble(nodeString[2]));

			ArrayList<NodeInfo> tempNodes = new ArrayList<NodeInfo>();			
			tempNodes.add(new NodeInfo(demand, location, battery));

			int nodeCounter = 1;

			while(!scanner.next().equals("EODATA")) {
				nodeString = scanner.nextLine().split("\t");

				demand = Integer.parseInt(nodeString[3]);
				battery = Double.parseDouble(nodeString[4]);
				location = new Coordinate(Double.parseDouble(nodeString[1]), Double.parseDouble(nodeString[2]));

				tempNodes.add(new NodeInfo(demand, location, battery));

				nodeCounter++;

				if (demand == -1) {
					sensingHoles.add(Integer.parseInt(nodeString[0]));
				}
			}

			while(!scanner.nextLine().equals("HOLESTART")) {}

			int degree = 0;
			while(!scanner.next().equals("EOHOLE")) {
				//NODE	X_LOC	Y_LOC	DEMAND	DEGREE
				nodeString = scanner.nextLine().split("\t");

				demand = Integer.parseInt(nodeString[3]);
				degree = Integer.parseInt(nodeString[4]);
				location = new Coordinate(Double.parseDouble(nodeString[1]), Double.parseDouble(nodeString[2]));

				tempNodes.add(new NodeInfo(demand, location, 0, degree));



				if (demand == -1) {
					sensingHoles.add(nodeCounter);
				}
				nodeCounter++;
			}

			scanner.close();

			NodeInfo[] nodeList = tempNodes.toArray(new NodeInfo[0]);
			
			tourVisualizer.updateContent(nodeList, sensingHoles);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}	
	}
	
	private void reset() {
		hypervolumeField.setText("");
		spacingField.setText("");
		contributionField.setText("");
		genDistField.setText("");
		paretoErrorField.setText("");
		runtimeField.setText("");
		list.setModel(new DefaultComboBoxModel(new String[0]));
	}
	
	private void generateCustomInstance(int numNodes, int sparsity, int distribution) {
		instance = InstanceGenerator.generateCustomInstance(numNodes, sparsity, distribution);
	}
	
	private class SelectionListener implements TreeSelectionListener {
		@Override
		public void valueChanged(TreeSelectionEvent se) {
			JTree tree = (JTree) se.getSource();
			File selectedNode = (File) tree.getLastSelectedPathComponent();
			if (selectedNode.isFile()) {
				instance = ((File) selectedNode);
			}
		}
	}
}
