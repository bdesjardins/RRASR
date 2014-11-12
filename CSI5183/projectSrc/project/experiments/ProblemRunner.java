package project.experiments;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import net.ericaro.surfaceplotter.JSurfacePanel;
import net.ericaro.surfaceplotter.surface.ArraySurfaceModel;

import org.moeaframework.Executor;
import org.moeaframework.Instrumenter;
import org.moeaframework.analysis.collector.Accumulator;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;

import project.problem.RRASRMOO;


public class ProblemRunner {
	
	private static Accumulator accumulator = null;
	private static int current = 1;
	private static ArraySurfaceModel sm = null;

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		String directory = "C:/Users/ben/git/CSI5183_F2014/CSI5183/Instances/Nodes/";
		File nodeList = new File(directory + "15n_3s_15d_instance.tsp");
		
		int popSize = 100;
		final int generations = 500;
		
		int evaluations = popSize * generations;

		Instrumenter instrumenter = new Instrumenter()
		.withProblemClass(RRASRMOO.class, nodeList)
		.attachElapsedTimeCollector()
		.attachApproximationSetCollector()
		.withFrequency(100);
		
		long beforeTime = System.currentTimeMillis();

		// solve using a Genetic Algorithm
		final NondominatedPopulation result = new Executor()
				.withProblemClass(RRASRMOO.class, nodeList)
				.withAlgorithm("SPEA2")
				.withMaxEvaluations(evaluations)
				.withProperty("populationSize", popSize)
				.withProperty("swap.rate", 0.25) // mutation
				.withProperty("insertion.rate", 0.25) // mutation
				.withProperty("pmx.rate", 0.75) // crossover
//				.withEpsilon(5)
//				.distributeOnAllCores()
				.withInstrumenter(instrumenter)
				.run();
		
		long afterTime = System.currentTimeMillis();
		accumulator = instrumenter.getLastAccumulator();
				
		System.out.println("Time Elapsed: " + (afterTime-beforeTime)/1000 + "s");
		System.out.println();
				
//		for (int i = 0; i < result.size(); i++) {
//			Solution solution = result.get(i);
//			double[] objectives = solution.getObjectives();
//
//			System.out.println("Solution " + (i + 1) + ":");
//			int[] permutation = EncodingUtils.getPermutation(solution
//					.getVariable(0));
//
//			System.out.print("  Path: ");
//			for (int j = 0; j < permutation.length; j++) {
//				System.out.print(permutation[j] + " ");
//			}
//			System.out.println();
//
//			System.out.println("  length = " + objectives[2]);
//			System.out.println("  robust = " + -objectives[1]);
//			System.out.println("  lifeti = " + -objectives[0]);
//		}
//		
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				testSomething(generations);
			}
		});
	}

	public static void testSomething(final int generations) {
		final JSurfacePanel jsp = new JSurfacePanel();
		jsp.setTitleText("Generation " + current);
		
		
		
		JButton nextButton = new JButton("Next Generation");
		nextButton.setBounds(0, 0, 80, 30);
		nextButton.setPreferredSize(new Dimension(80,30));
		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (current == generations) {
					jsp.setTitleText("Generation MAX (" + current + ")");
				} else {
					current++;
					updateGraph();
					jsp.setTitleText("Generation " + current);
				}
			}
		});
		JButton previousButton = new JButton("Previous Generation");
		previousButton.setBounds(0, 0, 80, 30);
		previousButton.setPreferredSize(new Dimension(80,30));
		previousButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {				
				if (current == 1) {
					jsp.setTitleText("Generation MIN (" + current + ")");
				} else {
					current--;
					updateGraph();
					jsp.setTitleText("Generation " + current);
				}
			}
		});
		
		JButton lastButton = new JButton("Last Generation");
		lastButton.setBounds(0, 0, 80, 30);
		lastButton.setPreferredSize(new Dimension(80,30));
		lastButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				current = generations;
				updateGraph();
				jsp.setTitleText("Generation MAX (" + current + ")");
			}
		});
		JButton firstButton = new JButton("First Generation");
		firstButton.setBounds(0, 0, 80, 30);
		firstButton.setPreferredSize(new Dimension(80,30));
		firstButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {				
				current = 1;
				updateGraph();
				jsp.setTitleText("Generation MIN (" + current + ")");
			}
		});
		
		JSplitPane buttons = new JSplitPane(0, nextButton, previousButton);
		JSplitPane buttons2 = new JSplitPane(0, firstButton, lastButton);

		JFrame jf = new JFrame("Test");
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.getContentPane().add(jsp, BorderLayout.CENTER);
		jf.getContentPane().add(buttons, BorderLayout.SOUTH);
		jf.getContentPane().add(buttons2, BorderLayout.NORTH);
		jf.pack();
		jf.setVisible(true);

		sm = new ArraySurfaceModel();		
		updateGraph();

		jsp.setModel(sm);
		
		jsp.setSize(800, 800);
	}
	
	private static void updateGraph() {
				
		ArrayList<Solution> result = (ArrayList<Solution>) accumulator.get("Approximation Set", current-1);
		
		float xMin = 0;
		float xMax = 100;
		float yMin = 20;
		float yMax = 200;
		
		int max = (int) yMax;
		
		float[][] z1 = new float[max][max];
		for (int i = 0; i < result.size(); i++) {
			Solution solution = (Solution) result.get(i);
			double[] objectives = solution.getObjectives();
			
			int x = (int) -objectives[0]; //lifetime
			int y = (int) -objectives[1]; //robustness
			float z = (float) objectives[2]; //path length

			z1[x][y] = z;
		}
				
		sm.setValues(xMin,xMax,yMin,yMax,max, z1, null);
		sm.autoScale();
		
		System.out.println("Generation: " + accumulator.get("NFE", current-1) + " " + accumulator.get("Elapsed Time", current-1));
	}
}
