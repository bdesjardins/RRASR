package project.experiments.multirobot;

import java.io.File;

import org.moeaframework.Executor;
import org.moeaframework.Instrumenter;
import org.moeaframework.analysis.collector.Accumulator;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;

import project.problem.RMRASR;


public class MRTestRunner {

	public static void main(String[] args) {
		String directory = "Instances/Distribution/";
		File nodeList = new File(directory + "200n_10s_15d_instance.tsp");
		
//		String directory = "Instances/";
//		File nodeList = new File(directory + "custom.tsp");
		
		int popSize = 200;
		final int generations = 500;
		
		int evaluations = popSize * generations * 2;

		Instrumenter instrumenter = new Instrumenter()
		.withProblemClass(RMRASR.class, nodeList)
		.attachElapsedTimeCollector()
		.attachApproximationSetCollector()
		.withFrequency(popSize);
		
		long beforeTime = System.currentTimeMillis();

		// solve using a Genetic Algorithm
		final NondominatedPopulation result = new Executor()
				.withProblemClass(RMRASR.class, nodeList, 3)
				.withAlgorithm("Lifecycle")
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
				
		System.out.println("Total Time Elapsed: " + (afterTime-beforeTime)/1000.0 + "s");
		System.out.println("Repair Time Elapsed: " + (RMRASR.repairTime_total)/1000.0 + "s");
//		System.out.println("Repair Percentage: " + ((RMRASR.repairTime_total)*100.0/(afterTime-beforeTime)*1.0) + "%");
//		System.out.println();
//		System.out.println("Repair (Balance) Time Elapsed: " + (RMRASR.repairTime_balance)/1000.0 + "s");
//		System.out.println("Repair (Balance) Percentage: " + ((RMRASR.repairTime_balance)*100.0/(RMRASR.repairTime_total)*1.0) + "%");
//		System.out.println();
//		System.out.println("Repair (Order) Time Elapsed: " + (RMRASR.repairTime_order)/1000.0 + "s");
//		System.out.println("Repair (Order) Percentage: " + ((RMRASR.repairTime_order)*100.0/(RMRASR.repairTime_total)*1.0) + "%");

//		System.out.println();
//		System.out.println("Accumulator Size: " + accumulator.size("NFE"));
				
		for (int i = 0; i < result.size(); i++) {
			Solution solution = result.get(i);
			double[] objectives = solution.getObjectives();

			System.out.println("Solution " + (i + 1) + ":");
//			int[] permutation = EncodingUtils.getPermutation(solution
//					.getVariable(0));
//
//			System.out.print("  Path: ");
//			for (int j = 0; j < permutation.length; j++) {
//				System.out.print(permutation[j] + " ");
//			}
//			System.out.println();

			System.out.println("  length = " + objectives[2]);
			System.out.println("  robust = " + -objectives[1]);
			System.out.println("  lifeti = " + -objectives[0]);
			System.out.println("  balanc = " + objectives[3]);
		}		
	}	
}
