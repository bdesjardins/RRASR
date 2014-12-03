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
import java.util.Set;

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

import javax.swing.JCheckBox;

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
	private static String algorithm = "";
	private static int selectedGeneration = 1;
	private static boolean withReferenceSet = false;
	private static ArrayList<String> algorithmsToRun = new ArrayList<String>();

	//Where we hold the results of our run
	private static ArrayList<Accumulator> accumulators = new ArrayList<Accumulator>();
	private static Accumulator selectedAccumulator = null;
	private JTextField numNodesField;
	private JTextField distributionField;
	private JTextField sparsityField;
	private JTextField xoverField;
	private JTextField mutationField;

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

		JLabel lblAlgorithm = new JLabel("Algorithm(s) to Run");
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

		final JComboBox algorithmSpinner = new JComboBox();
		algorithmSpinner.setModel(new DefaultComboBoxModel(new String[] {}));

		JLabel lblAlgorithm_1 = new JLabel("Algorithm");
		lblAlgorithm_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblAlgorithm_1.setFont(new Font("Tahoma", Font.BOLD, 12));
		GroupLayout gl_resultsPanel = new GroupLayout(resultsPanel);
		gl_resultsPanel.setHorizontalGroup(
			gl_resultsPanel.createParallelGroup(Alignment.TRAILING)
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
				.addGroup(Alignment.LEADING, gl_resultsPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblAlgorithm_1, GroupLayout.PREFERRED_SIZE, 152, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addGroup(Alignment.LEADING, gl_resultsPanel.createSequentialGroup()
					.addGap(28)
					.addComponent(algorithmSpinner, GroupLayout.PREFERRED_SIZE, 117, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(27, Short.MAX_VALUE))
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
					.addPreferredGap(ComponentPlacement.RELATED, 40, Short.MAX_VALUE)
					.addComponent(lblAlgorithm_1, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(algorithmSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
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
										
												list.addActionListener(new ActionListener() {
													public void actionPerformed(ActionEvent arg0) {
														printROI();
														printSolutionPath(list.getSelectedIndex());
													}
												});


		paretoViewer = new Plot3DPanel();
		tabbedPane.addTab("Pareto Set Viewer", null, paretoViewer, null);

		JTabbedPane instanceTabs = new JTabbedPane(JTabbedPane.TOP);

		JLabel lblCrossoverRate = new JLabel("Crossover Rate");
		lblCrossoverRate.setHorizontalAlignment(SwingConstants.CENTER);
		lblCrossoverRate.setFont(new Font("Tahoma", Font.BOLD, 12));

		JLabel lblMutationRate = new JLabel("Mutation Rate");
		lblMutationRate.setHorizontalAlignment(SwingConstants.CENTER);
		lblMutationRate.setFont(new Font("Tahoma", Font.BOLD, 12));

		xoverField = new JTextField();
		xoverField.setText("0.75");
		xoverField.setColumns(10);

		mutationField = new JTextField();
		mutationField.setText("0.25");
		mutationField.setColumns(10);

		final JCheckBox checkNSGAII = new JCheckBox("NSGAII");
		checkNSGAII.setFont(new Font("Tahoma", Font.BOLD, 12));

		final JCheckBox checkNSGAIII = new JCheckBox("NSGAIII");
		checkNSGAIII.setFont(new Font("Tahoma", Font.BOLD, 12));

		final JCheckBox checkSPEA2 = new JCheckBox("SPEA2");
		checkSPEA2.setFont(new Font("Tahoma", Font.BOLD, 12));

		final JCheckBox checkPAES = new JCheckBox("PAES");
		checkPAES.setFont(new Font("Tahoma", Font.BOLD, 12));

		GroupLayout gl_parametersPanel = new GroupLayout(parametersPanel);
		gl_parametersPanel.setHorizontalGroup(
				gl_parametersPanel.createParallelGroup(Alignment.TRAILING)
				.addComponent(lblProblemInstance, GroupLayout.DEFAULT_SIZE, 362, Short.MAX_VALUE)
				.addGroup(Alignment.LEADING, gl_parametersPanel.createSequentialGroup()
						.addGap(152)
						.addComponent(runButton)
						.addContainerGap(153, Short.MAX_VALUE))
						.addGroup(Alignment.LEADING, gl_parametersPanel.createSequentialGroup()
								.addContainerGap()
								.addComponent(lblCrossoverRate, GroupLayout.PREFERRED_SIZE, 152, GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(ComponentPlacement.RELATED, 38, Short.MAX_VALUE)
								.addComponent(lblMutationRate, GroupLayout.PREFERRED_SIZE, 152, GroupLayout.PREFERRED_SIZE)
								.addContainerGap())
								.addGroup(Alignment.LEADING, gl_parametersPanel.createSequentialGroup()
										.addContainerGap()
										.addComponent(xoverField, GroupLayout.PREFERRED_SIZE, 152, GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.RELATED, 38, Short.MAX_VALUE)
										.addComponent(mutationField, GroupLayout.PREFERRED_SIZE, 152, GroupLayout.PREFERRED_SIZE)
										.addContainerGap())
										.addGroup(Alignment.LEADING, gl_parametersPanel.createSequentialGroup()
												.addGroup(gl_parametersPanel.createParallelGroup(Alignment.LEADING)
														.addGroup(gl_parametersPanel.createSequentialGroup()
																.addContainerGap()
																.addGroup(gl_parametersPanel.createParallelGroup(Alignment.LEADING, false)
																		.addComponent(lblPopulationSize, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
																		.addComponent(populationField, GroupLayout.PREFERRED_SIZE, 152, GroupLayout.PREFERRED_SIZE))
																		.addGap(38))
																		.addGroup(gl_parametersPanel.createSequentialGroup()
																				.addGap(15)
																				.addGroup(gl_parametersPanel.createParallelGroup(Alignment.LEADING)
																						.addGroup(gl_parametersPanel.createSequentialGroup()
																								.addComponent(checkNSGAIII, GroupLayout.PREFERRED_SIZE, 84, GroupLayout.PREFERRED_SIZE)
																								.addPreferredGap(ComponentPlacement.RELATED))
																								.addGroup(gl_parametersPanel.createSequentialGroup()
																										.addComponent(checkNSGAII)
																										.addPreferredGap(ComponentPlacement.RELATED)))
																										.addPreferredGap(ComponentPlacement.RELATED)))
																										.addGroup(gl_parametersPanel.createParallelGroup(Alignment.LEADING)
																												.addComponent(checkSPEA2, GroupLayout.PREFERRED_SIZE, 69, GroupLayout.PREFERRED_SIZE)
																												.addGroup(gl_parametersPanel.createParallelGroup(Alignment.TRAILING, false)
																														.addComponent(lblOfGenerations, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
																														.addComponent(generationsField, GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE))
																														.addComponent(checkPAES, GroupLayout.PREFERRED_SIZE, 69, GroupLayout.PREFERRED_SIZE))
																														.addContainerGap())
																														.addGroup(gl_parametersPanel.createSequentialGroup()
																																.addGroup(gl_parametersPanel.createParallelGroup(Alignment.TRAILING)
																																		.addComponent(lblAlgorithm, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
																																		.addGroup(gl_parametersPanel.createSequentialGroup()
																																				.addContainerGap()
																																				.addComponent(instanceTabs, GroupLayout.DEFAULT_SIZE, 342, Short.MAX_VALUE)))
																																				.addContainerGap())
				);
		gl_parametersPanel.setVerticalGroup(
				gl_parametersPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_parametersPanel.createSequentialGroup()
						.addComponent(lblProblemInstance, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(instanceTabs, GroupLayout.PREFERRED_SIZE, 311, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(lblAlgorithm, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addGroup(gl_parametersPanel.createParallelGroup(Alignment.BASELINE)
								.addComponent(checkNSGAII)
								.addComponent(checkSPEA2))
								.addPreferredGap(ComponentPlacement.UNRELATED)
								.addGroup(gl_parametersPanel.createParallelGroup(Alignment.BASELINE)
										.addComponent(checkNSGAIII)
										.addComponent(checkPAES))
										.addGap(28)
										.addGroup(gl_parametersPanel.createParallelGroup(Alignment.BASELINE)
												.addComponent(lblPopulationSize)
												.addComponent(lblOfGenerations, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE))
												.addPreferredGap(ComponentPlacement.RELATED)
												.addGroup(gl_parametersPanel.createParallelGroup(Alignment.BASELINE)
														.addComponent(populationField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
														.addComponent(generationsField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
														.addPreferredGap(ComponentPlacement.UNRELATED)
														.addGroup(gl_parametersPanel.createParallelGroup(Alignment.BASELINE)
																.addComponent(lblCrossoverRate, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE)
																.addComponent(lblMutationRate, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE))
																.addPreferredGap(ComponentPlacement.RELATED)
																.addGroup(gl_parametersPanel.createParallelGroup(Alignment.BASELINE)
																		.addComponent(xoverField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																		.addComponent(mutationField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
																		.addGap(18)
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
				
				algorithmSpinner.setModel(new DefaultComboBoxModel(algorithmsToRun.toArray(new String[0])));
				algorithmSpinner.setSelectedIndex(0);
				selectedAccumulator = accumulators.get(0);

				genSlider.setMinimum(1);
				genSlider.setMaximum(generations);
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
					accumulators = new ArrayList<Accumulator>();
					selectedAccumulator = null;
					paretoViewer.removeAllPlots();
					//End of item reset

					instance = ((File) selectedNode);					
					if (tabbedPane.getSelectedIndex() == 0) {
						loadInstanceInformation();
						printROI();
					}
					
					runButton.setEnabled(checkNSGAII.isSelected() || checkNSGAIII.isSelected() || checkSPEA2.isSelected() || checkPAES.isSelected());
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

		tabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				int selected = tabbedPane.getSelectedIndex();
				//0 is Pareto Set Viewer
				//1 is Solution Viewer
				//2 is Metric Graphs

				if (selected == 0 && instance != null) {
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
				accumulators = new ArrayList<Accumulator>();
				selectedAccumulator = null;
				//End of item reset

				generateCustomInstance(numNodes, sparsity, distribution); 					
				if (tabbedPane.getSelectedIndex() == 1) {
					loadInstanceInformation();
					printROI();
				}
				runButton.setEnabled(true);		
			}
		});
		
		checkNSGAII.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (checkNSGAII.isSelected()) {
					algorithmsToRun.add("NSGAII");
				} else {
					algorithmsToRun.remove("NSGAII");
				}
				
				runButton.setEnabled((checkNSGAII.isSelected() || checkNSGAIII.isSelected() || checkSPEA2.isSelected() || checkPAES.isSelected()) 
						&& instance != null);
			}
		});
		
		checkNSGAIII.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (checkNSGAIII.isSelected()) {
					algorithmsToRun.add("NSGAIII");
				} else {
					algorithmsToRun.remove("NSGAIII");
				}
				
				runButton.setEnabled((checkNSGAII.isSelected() || checkNSGAIII.isSelected() || checkSPEA2.isSelected() || checkPAES.isSelected()) 
						&& instance != null);
			}
		});

		checkSPEA2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (checkSPEA2.isSelected()) {
					algorithmsToRun.add("SPEA2");
				} else {
					algorithmsToRun.remove("SPEA2");
				}
				
				runButton.setEnabled((checkNSGAII.isSelected() || checkNSGAIII.isSelected() || checkSPEA2.isSelected() || checkPAES.isSelected()) 
						&& instance != null);
			}
		});

		checkPAES.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (checkPAES.isSelected()) {
					algorithmsToRun.add("PAES");
				} else {
					algorithmsToRun.remove("PAES");
				}
				
				runButton.setEnabled((checkNSGAII.isSelected() || checkNSGAIII.isSelected() || checkSPEA2.isSelected() || checkPAES.isSelected()) 
						&& instance != null);
			}
		});
		
		algorithmSpinner.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				algorithm = algorithmSpinner.getSelectedItem().toString();							
				selectedAccumulator = accumulators.get(algorithmSpinner.getSelectedIndex());
				displayGenerationInfo();
				updateMetricGraphs();
				
				if (tabbedPane.getSelectedIndex() == 0) {
					printROI();
				}
			}
		});


		//END ACTION LISTENERS -------------------------------------------------------------------------------------------------------------------------
	}

	@SuppressWarnings("unused")
	private void runExperiment() {		
		File referenceFile = null;

		double xover = Double.parseDouble(xoverField.getText());
		double mutation = Double.parseDouble(mutationField.getText());

		try {
			String temp = instance.getCanonicalPath().replace('\\', '/');			
			String[] fileString = temp.split("/");

			String refFilePath = "";

			for (int i = 0; i < fileString.length; i++) {
				if (i == fileString.length-1) {
					refFilePath += "References\\" + fileString[i] + ".ref";
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

		for (int i = 0; i < algorithmsToRun.size(); i++) {

			if (withReferenceSet) {
				instrumenter = new Instrumenter()
				.withProblemClass(RRASRMOO.class, instance)
				.withReferenceSet(referenceFile)
				.attachContributionCollector()
				.attachGenerationalDistanceCollector()
				.attachHypervolumeCollector()
				.attachApproximationSetCollector()
				.attachElapsedTimeCollector()
				.attachSpacingCollector()
				.attachAllMetricCollectors()
				.withFrequency(population);

				// solve using a Genetic Algorithm
				final NondominatedPopulation result = new Executor()
				.withProblemClass(RRASRMOO.class, instance)
				.withAlgorithm(algorithmsToRun.get(i))
				.withMaxEvaluations(evaluations)
				.withProperty("populationSize", population)
				.withProperty("swap.rate", mutation) // swap mutation
				.withProperty("insertion.rate", mutation) // insertion mutation
				.withProperty("pmx.rate", xover) // partially mapped crossover
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
				.withAlgorithm(algorithmsToRun.get(i))
				.withMaxEvaluations(evaluations)
				.withProperty("populationSize", population)
				.withProperty("swap.rate", mutation) // swap mutation
				.withProperty("insertion.rate", mutation) // insertion mutation
				.withProperty("pmx.rate", xover) // partially mapped crossover
//				.withEpsilon(5)
//				.distributeOnAllCores()
				.withInstrumenter(instrumenter)
				.run();


			}
			accumulators.add(instrumenter.getLastAccumulator());
		}
	}

	public void displayGenerationInfo() {
		if (selectedAccumulator != null) {
			updateMetricFields();
			updateParetoViewer();
			if (tabbedPane.getSelectedIndex() == 0) {
				updatePathSelector();
				printROI();
			}
		}
	}

	private void updateMetricFields() {
		int gen = selectedGeneration-1;
		
		if (selectedAccumulator.size("Approximation Set") == 1) {
			gen = 0;
		} 
		
		if (selectedAccumulator.size("Approximation Set") == 1 && selectedGeneration != generations) {
			hypervolumeField.setText("N/A");
			spacingField.setText("N/A");
			contributionField.setText("N/A");
			genDistField.setText("N/A");
			paretoErrorField.setText("N/A");
			runtimeField.setText("N/A");
			
			return;
		} else if (withReferenceSet) {
			DecimalFormat df = new DecimalFormat("#.########");
			Set<String> keys = selectedAccumulator.keySet();
			
			hypervolumeField.setText((df.format(((Double) selectedAccumulator.get("Hypervolume", gen)))));
			spacingField.setText((df.format(((Double) selectedAccumulator.get("Spacing", gen)))));
			contributionField.setText((df.format(((Double) selectedAccumulator.get("Contribution", gen)))));
			genDistField.setText((df.format(((Double) selectedAccumulator.get("GenerationalDistance", gen)))));
//			paretoErrorField.setText("" + selectedAccumulator.get("Maximum Pareto Front Error", gen));
		} else {
			hypervolumeField.setText("No Reference Set");
			spacingField.setText("No Reference Set");
			contributionField.setText("No Reference Set");
			genDistField.setText("No Reference Set");
			paretoErrorField.setText("No Reference Set");		
		}
		DecimalFormat df = new DecimalFormat("#.###");

		runtimeField.setText(df.format(((Double) selectedAccumulator.get("Elapsed Time", gen))));
	}

	private void updateParetoViewer() {
		
		paretoViewer.removeAllPlots();
		
		for (int j = 0; j < algorithmsToRun.size(); j++) {
			int gen = selectedGeneration-1;
			
			if (accumulators.get(j).size("Approximation Set") == 1) {
				if (selectedGeneration != generations) {
					continue;
				}
				gen = 0;
			} 
			ArrayList<Solution> result = (ArrayList<Solution>) accumulators.get(j).get("Approximation Set", gen);		

			double[] X = new double[result.size()];
			double[] Y = new double[result.size()];
			double[] Z = new double[result.size()];

			for (int i = 0; i < result.size(); i++) {
				X[i] = -result.get(i).getObjective(0); //Lifetime
				Y[i] = -result.get(i).getObjective(1); //Robustness
				Z[i] = result.get(i).getObjective(2); //Length
			}

			paretoViewer.addScatterPlot(algorithmsToRun.get(j), X, Y, Z);
		}

		paretoViewer.setAxisLabels("Path Lifetime","Path Robustness","Path Length");
		paretoViewer.setAutoBounds();
	}

	private void updatePathSelector() {
		int gen = selectedGeneration-1;
		
		if (selectedAccumulator.size("Approximation Set") == 1) {
			if (selectedGeneration != generations) {
				
				list.setModel(new DefaultComboBoxModel(new String[0]));
				return;
			}
			gen = 0;
		} 
		
		ArrayList<Solution> result = (ArrayList<Solution>) selectedAccumulator.get("Approximation Set", gen);

		String[] solutionStrings = new String[result.size()];

		DecimalFormat df = new DecimalFormat("#.##");

		for (int i = 0; i < result.size(); i++) {
			solutionStrings[i] = "Length: " + df.format(result.get(i).getObjective(2)) + " Robustness: " + -result.get(i).getObjective(1) + " Lifetime: " + -result.get(i).getObjective(0);
		}

		list.setModel(new DefaultComboBoxModel(solutionStrings));
	}

	private void updateMetricGraphs() {		
		metricGraphViewer.removeAllPlots();
		
		selectedAccumulator.size("NFE");

		double[] X = new double[selectedAccumulator.size("NFE")];

		for (int i = 0; i < selectedAccumulator.size("NFE"); i++) {
			X[i] = ((Integer)selectedAccumulator.get("NFE", i))/population;
		}

		String name = "Elapsed Time";
		double[] Y = new double[selectedAccumulator.size("Elapsed Time")];		
		for (int i = 0; i<selectedAccumulator.size("Elapsed Time"); i++) {
			Y[i] = (Double) selectedAccumulator.get("Elapsed Time", i);
		}
		metricGraphViewer.addScatterPlot(name, X, Y);

		if (withReferenceSet) {
			name = "Hypervolume";
			Y = new double[selectedAccumulator.size(name)];		
			for (int i = 0; i<selectedAccumulator.size(name); i++) {
				Y[i] = (Double) selectedAccumulator.get(name, i);
			}
			metricGraphViewer.addScatterPlot(name, X, Y);

			name = "Spacing";
			Y = new double[selectedAccumulator.size(name)];		
			for (int i = 0; i<selectedAccumulator.size(name); i++) {
				Y[i] = (Double) selectedAccumulator.get(name, i);
			}
			metricGraphViewer.addScatterPlot(name, X, Y);

			name = "Contribution";
			Y = new double[selectedAccumulator.size(name)];		
			for (int i = 0; i<selectedAccumulator.size(name); i++) {
				Y[i] = (Double) selectedAccumulator.get(name, i);
			}
			metricGraphViewer.addScatterPlot(name, X, Y);

			name = "GenerationalDistance";
			Y = new double[selectedAccumulator.size(name)];		
			for (int i = 0; i<selectedAccumulator.size(name); i++) {
				Y[i] = (Double) selectedAccumulator.get(name, i);
			}
			metricGraphViewer.addScatterPlot(name, X, Y);

//			name = "Maximum Pareto Front Error";
//			Y = new double[selectedAccumulator.size(name)];		
//			for (int i = 0; i<selectedAccumulator.size(name); i++) {
//				Y[i] = (Double) selectedAccumulator.get(name, i);
//			}
//			metricGraphViewer.addScatterPlot(name, X, Y);
		}

		metricGraphViewer.setAxisLabels("Generation","Metric");
	}

	private void printSolutionPath(int solutionIndex) {
		int gen = selectedGeneration-1;
		
		if (selectedAccumulator.size("Approximation Set") == 1) {
			if (selectedGeneration != generations) {				
				tourVisualizer.paintNodes(tourVisualizer.getGraphics());
				return;
			}
			gen = 0;
		} 
		
		ArrayList<Solution> result = (ArrayList<Solution>) selectedAccumulator.get("Approximation Set", gen);
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
