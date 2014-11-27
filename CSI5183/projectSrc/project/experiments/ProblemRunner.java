package project.experiments;

import java.io.File;

import org.moeaframework.Executor;
import org.moeaframework.Instrumenter;
import org.moeaframework.analysis.collector.Accumulator;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;

import project.problem.RRASRMOO;


public class ProblemRunner {

	public static void main(String[] args) {
		String directory = "Instances/Nodes/";
		File nodeList = new File(directory + "200n_3s_15d_instance.tsp");
		
		int popSize = 200;
		final int generations = 500;
		
		int evaluations = popSize * generations;

		Instrumenter instrumenter = new Instrumenter()
		.withProblemClass(RRASRMOO.class, nodeList)
		.attachElapsedTimeCollector()
		.attachApproximationSetCollector();
//		.withFrequency(100);
		
		long beforeTime = System.currentTimeMillis();

		// solve using a Genetic Algorithm
		final NondominatedPopulation result = new Executor()
				.withProblemClass(RRASRMOO.class, nodeList)
				.withAlgorithm("NSGAIII")
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
		Accumulator accumulator = instrumenter.getLastAccumulator();
				
		System.out.println("Time Elapsed: " + (afterTime-beforeTime)/1000 + "s");
		System.out.println();
				
		for (int i = 0; i < result.size(); i++) {
			Solution solution = result.get(i);
			double[] objectives = solution.getObjectives();

			System.out.println("Solution " + (i + 1) + ":");
			int[] permutation = EncodingUtils.getPermutation(solution
					.getVariable(0));

			System.out.print("  Path: ");
			for (int j = 0; j < permutation.length; j++) {
				System.out.print(permutation[j] + " ");
			}
			System.out.println();

			System.out.println("  length = " + objectives[2]);
			System.out.println("  robust = " + -objectives[1]);
			System.out.println("  lifeti = " + -objectives[0]);
		}		
	}	
}
