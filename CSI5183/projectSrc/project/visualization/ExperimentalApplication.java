package project.visualization;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JLabel;

import java.awt.Font;

import javax.swing.JSlider;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.eclipse.wb.swing.FocusTraversalOnArray;

import java.awt.Component;

import javax.swing.JComboBox;
import javax.swing.JTree;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;

import java.io.File;

import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

public class ExperimentalApplication {

	//UI Components
	private JFrame frmCsiProject;
	private JTextField populationField;
	private JTextField generationsField;
	private JTextField hypervolumeField;
	private JTextField spacingField;
	private JTextField contributionField;
	private JTextField genDistField;
	private JTextField paretoErrorField;
	private JTextField runtimeField;
	private FileTreeModel model;
	
	
	//Execution variables
	private File instance;
	private int population = 200;
	private int generations = 500;

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
		model = new FileTreeModel(new File("C:/Users/ben/git/CSI5183_F2014/CSI5183/Instances"));
		
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmCsiProject = new JFrame();
		frmCsiProject.setTitle("CSI5183 - Project");
		frmCsiProject.setBounds(100, 100, 1112, 700);
		frmCsiProject.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		
		JLabel lblExperimentalRunner = new JLabel("RRASR Experiment Runner");
		lblExperimentalRunner.setFont(new Font("Tahoma", Font.PLAIN, 19));
		
		JPanel parametersPanel = new JPanel();
		
		JComboBox algorithmSpinner = new JComboBox();
		algorithmSpinner.setModel(new DefaultComboBoxModel(new String[] {"NSGAII", "NSGAIII", "SPEA2", "IBEA"}));
		
		JLabel lblAlgorithm = new JLabel("Algorithm");
		lblAlgorithm.setHorizontalAlignment(SwingConstants.CENTER);
		lblAlgorithm.setFont(new Font("Tahoma", Font.BOLD, 12));
		
		JLabel lblProblemInstance = new JLabel("Problem Instance");
		lblProblemInstance.setHorizontalAlignment(SwingConstants.CENTER);
		lblProblemInstance.setFont(new Font("Tahoma", Font.BOLD, 12));
		
		populationField = new JTextField();
		populationField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				warn();
			}
			public void removeUpdate(DocumentEvent e) {
				warn();
			}
			public void insertUpdate(DocumentEvent e) {
				warn();
			}

			public void warn() {
				try {
					if (Integer.parseInt(populationField.getText())<=0){
						JOptionPane.showMessageDialog(null,
								"Error: Please enter number bigger than 0", "Error Message",
								JOptionPane.ERROR_MESSAGE);
					} else {
						setPopulation(Integer.parseInt(populationField.getText()));
					}
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(null,
							"Error: Please enter number bigger than 0", "Error Message",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		populationField.setText("200");
		populationField.setColumns(10);
		
		JLabel lblPopulationSize = new JLabel("Population Size");
		lblPopulationSize.setHorizontalAlignment(SwingConstants.CENTER);
		lblPopulationSize.setFont(new Font("Tahoma", Font.BOLD, 12));
		
		JLabel lblOfGenerations = new JLabel("# of Generations");
		lblOfGenerations.setHorizontalAlignment(SwingConstants.CENTER);
		lblOfGenerations.setFont(new Font("Tahoma", Font.BOLD, 12));
		
		generationsField = new JTextField();
		generationsField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				warn();
			}
			public void removeUpdate(DocumentEvent e) {
				warn();
			}
			public void insertUpdate(DocumentEvent e) {
				warn();
			}

			public void warn() {
				try {
					if (Integer.parseInt(generationsField.getText())<=0){
						JOptionPane.showMessageDialog(null,
								"Error: Please enter number bigger than 0", "Error Message",
								JOptionPane.ERROR_MESSAGE);
					} else {
						setGenerations(Integer.parseInt(generationsField.getText()));
					}
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(null,
							"Error: Please enter number bigger than 0", "Error Message",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		generationsField.setText("500");
		generationsField.setColumns(10);
				
		JScrollPane scrollPane = new JScrollPane();
		
		JTree tree = new JTree(model);
		tree.setRootVisible(false);
		
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addTreeSelectionListener(new SelectionListener());
		
		scrollPane.setViewportView(tree);
		
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
		
		JSlider genSlider = new JSlider();
		
		JLabel genSliderLabel = new JLabel("Generation Selector");
		genSliderLabel.setHorizontalAlignment(SwingConstants.CENTER);
		genSliderLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		
		JLabel genNumberLabel = new JLabel("NaN");
		genNumberLabel.setHorizontalAlignment(SwingConstants.CENTER);
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
				.addGroup(Alignment.LEADING, gl_resultsPanel.createSequentialGroup()
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
		
		JPanel graphTab = new JPanel();
		tabbedPane.addTab("Pareto Set Viewer", null, graphTab, null);
		
		JPanel tourTab = new JPanel();
		tabbedPane.addTab("Solution Viewer", null, tourTab, null);
		tourTab.setLayout(null);
		
//		TourVisualizer tourPane = new TourVisualizer();
//		tourPane.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
//		tourPane.setBounds(10, 11, 500, 500);
//		tourTab.add(tourPane);
		
		JLabel lblNewLabel_5 = new JLabel("Solution:");
		lblNewLabel_5.setFont(new Font("Tahoma", Font.BOLD, 12));
//		lblNewLabel_5.setBounds(10, 527, 66, 14);
		tourTab.add(lblNewLabel_5);
		
		JComboBox solutionSelection = new JComboBox();
//		solutionSelection.setBounds(86, 522, 424, 20);
		tourTab.add(solutionSelection);
		GroupLayout gl_parametersPanel = new GroupLayout(parametersPanel);
		gl_parametersPanel.setHorizontalGroup(
			gl_parametersPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_parametersPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(12, Short.MAX_VALUE))
				.addComponent(lblProblemInstance, GroupLayout.DEFAULT_SIZE, 362, Short.MAX_VALUE)
				.addGroup(gl_parametersPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblAlgorithm, GroupLayout.DEFAULT_SIZE, 342, Short.MAX_VALUE)
					.addContainerGap())
				.addGroup(gl_parametersPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblPopulationSize, GroupLayout.DEFAULT_SIZE, 342, Short.MAX_VALUE)
					.addContainerGap())
				.addGroup(Alignment.TRAILING, gl_parametersPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblOfGenerations, GroupLayout.DEFAULT_SIZE, 342, Short.MAX_VALUE)
					.addContainerGap())
				.addGroup(gl_parametersPanel.createSequentialGroup()
					.addGap(99)
					.addComponent(populationField, GroupLayout.PREFERRED_SIZE, 152, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(111, Short.MAX_VALUE))
				.addGroup(gl_parametersPanel.createSequentialGroup()
					.addGap(98)
					.addComponent(generationsField, GroupLayout.PREFERRED_SIZE, 152, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(112, Short.MAX_VALUE))
				.addGroup(gl_parametersPanel.createSequentialGroup()
					.addGap(101)
					.addComponent(algorithmSpinner, GroupLayout.PREFERRED_SIZE, 152, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(109, Short.MAX_VALUE))
		);
		gl_parametersPanel.setVerticalGroup(
			gl_parametersPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_parametersPanel.createSequentialGroup()
					.addComponent(lblProblemInstance, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 389, GroupLayout.PREFERRED_SIZE)
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
					.addContainerGap())
		);
		parametersPanel.setLayout(gl_parametersPanel);
		GroupLayout groupLayout = new GroupLayout(frmCsiProject.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(343)
							.addComponent(lblExperimentalRunner))
						.addGroup(groupLayout.createSequentialGroup()
							.addContainerGap()
							.addComponent(parametersPanel, GroupLayout.PREFERRED_SIZE, 362, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, 525, GroupLayout.PREFERRED_SIZE)
							.addGap(6)
							.addComponent(resultsPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
					.addGap(99))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addComponent(lblExperimentalRunner)
					.addGap(13)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(resultsPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(parametersPanel, GroupLayout.PREFERRED_SIZE, 580, GroupLayout.PREFERRED_SIZE)
						.addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, 580, GroupLayout.PREFERRED_SIZE))
					.addGap(46))
		);
		frmCsiProject.getContentPane().setLayout(groupLayout);
		frmCsiProject.getContentPane().setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{tabbedPane, lblExperimentalRunner}));
	}

	private class SelectionListener implements TreeSelectionListener {
		@Override
		public void valueChanged(TreeSelectionEvent se) {
			JTree tree = (JTree) se.getSource();
			File selectedNode = (File) tree.getLastSelectedPathComponent();
			if (selectedNode.isFile()) {
				setInstance((File) selectedNode);
			}
		}
	}
	
	private void setInstance(File newInstance) {
		this.instance = newInstance;
	}
	private void setPopulation(int newPop) {
		this.population = newPop;
	}
	private void setGenerations(int newGen) {
		this.generations = newGen;
	}
}
