package project.problem;

import java.io.File;
import java.util.Set;
import java.util.Timer;

import org.moeaframework.Executor;
import org.moeaframework.Instrumenter;
import org.moeaframework.analysis.collector.Accumulator;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;

public class ProblemRunner {

	public static void main(String[] args) {
		// TODO remove nulls
		String directory = "C:/Users/ben/git/CSI5183_F2014/CSI5183/Instances/";
		File nodeList = new File(directory + "200_1_instance1.tsp");
		// File activeNodeList = null;

//		 Instrumenter instrumenter = new Instrumenter()
//		 .withProblemClass(ProblemDefinition.class, nodeList)
//		 .withFrequency(50)
////		 .attachApproximationSetCollector()
//		 .attachAll();
		
		long beforeTime = System.currentTimeMillis();

		// solve using NSGA-II
		NondominatedPopulation result = new Executor()
				.withProblemClass(ProblemDefinition.class, nodeList)
				.withAlgorithm("NSGAII").withMaxEvaluations(5000)
				.withProperty("populationSize", 2500)
				.withProperty("pm.rate", 0.33) // mutation
				.withProperty("sbx.rate", 0.75) // crossover
				.withEpsilon(15)
				// .withInstrumenter(instrumenter)
				.run();
		
		long afterTime = System.currentTimeMillis();
		
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
		
		System.out.println();
//		 Accumulator accumulator = instrumenter.getLastAccumulator();
//		
//		 //TODO Temp variables to see values while debugging
//		 Set<String> temp = accumulator.keySet();
//		 String temp2 = "01010";

		// includeApproximationSet

	}

}
