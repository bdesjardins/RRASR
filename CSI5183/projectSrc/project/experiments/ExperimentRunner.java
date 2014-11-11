package project.experiments;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.moeaframework.Executor;
import org.moeaframework.Instrumenter;
import org.moeaframework.analysis.collector.Accumulator;
import org.moeaframework.core.NondominatedPopulation;
import project.problem.ProblemDefinition;

public class ExperimentRunner {

	public static void main(String[] args) {
		File results = new File("C:/Users/ben/git/CSI5183_F2014/CSI5183/Files/results.csv");

		try {
			CSVPrinter printer = new CSVPrinter(new FileWriter(results, true), CSVFormat.EXCEL);
			
			String directory = "C:/Users/ben/git/CSI5183_F2014/CSI5183/Instances";

			String[] algorithms = new String[]{"NSGAII", "NSGAIII", "SPEA2", "IBEA"};
			String[] problems = new String[]{"Nodes", "Sparsity", "Distribution"};

			for (int k = 0; k < algorithms.length; k++) {
				for (int i = 0; i <problems.length; i++) {
					File folder = new File(directory + "/" + problems[i]);
					File[] listOfFiles = folder.listFiles();

					for (int j = 0; j < listOfFiles.length; j++) {
						File referenceSet = new File(directory + "/" + problems[i] + "/Reference/" + listOfFiles[j].getName() + ".ref");

						doTheThing(listOfFiles[j], referenceSet, printer, algorithms[k]);
					}
				}
			}
			
			printer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void doTheThing(File instanceFile, File referenceSet, CSVPrinter printer, String algorithm) {
		int popSize = 100;
		final int generations = 500;
		
		int evaluations = popSize * generations;

		Instrumenter instrumenter = new Instrumenter()
		.withProblemClass(ProblemDefinition.class, instanceFile)
//		.attachElapsedTimeCollector()
//		.attachApproximationSetCollector()
		.withReferenceSet(referenceSet)
		.attachAll()
		.withFrequency(popSize);

		// solve using a Genetic Algorithm
		final NondominatedPopulation result = new Executor()
				.withProblemClass(ProblemDefinition.class, instanceFile)
				.withAlgorithm(algorithm)
				.withMaxEvaluations(evaluations)
				.withProperty("populationSize", popSize)
				.withProperty("swap.rate", 0.25) // mutation
				.withProperty("insertion.rate", 0.25) // mutation
				.withProperty("pmx.rate", 0.75) // crossover
//				.withEpsilon(5)
				.distributeOnAllCores()
				.withInstrumenter(instrumenter)
				.run();
		
		Accumulator accumulator = instrumenter.getLastAccumulator();
		
		//#nodes,sparsity,distribution,HYPERVOLUME,CONTRIBUTION,GENDISTANCE,MPFERROR,RUNTIME,algorithm
		
	}

}
