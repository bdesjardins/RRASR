package project.experiments;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.moeaframework.Analyzer;
import org.moeaframework.Executor;
import org.moeaframework.Instrumenter;
import org.moeaframework.analysis.collector.Accumulator;
import org.moeaframework.analysis.collector.IndicatorCollector;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.indicator.MaximumParetoFrontError;

import project.problem.RRASRMOO;

public class ExperimentRunner {

	public static void main(String[] args) {
		long beforeTime = System.currentTimeMillis();

		File results = new File("results.csv");

		try {
			CSVPrinter printer = new CSVPrinter(new FileWriter(results, true), CSVFormat.EXCEL);

			String directory = "Instances";

			String[] algorithms = new String[]{"NSGAII", "NSGAIII", "SPEA2"};
			String[] problems = new String[]{"Nodes", "Sparsity", "Distribution"};

			for (int counter = 1; counter <= 20; counter++) {
				for (int k = 0; k < algorithms.length; k++) {
					for (int i = 0; i <problems.length; i++) {
						File folder = new File(directory + "/" + problems[i]);
						File[] listOfFiles = folder.listFiles();
						File newFolder = new File(directory + "/" + problems[i] + "/" + "Run" + counter);
						folder.mkdir();

						for (int j = 0; j < listOfFiles.length; j++) {
							File approxFolder = new File(newFolder.getCanonicalPath() + "/" + listOfFiles[j].getName());
							approxFolder.mkdir();

							File referenceSet = new File(directory + "/" + problems[i] + "/Reference/" + listOfFiles[j].getName() + ".ref");

							doTheThing(listOfFiles[j], referenceSet, printer, algorithms[k], approxFolder);
						}
					}
				}
			}

			printer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long afterTime = System.currentTimeMillis();

		System.out.println("Time Elapsed: " + (afterTime-beforeTime)/1000 + "s");
	}

	private static void doTheThing(File instanceFile, File referenceSet, CSVPrinter printer, String algorithm, File approxFolder) {
		int popSize = 200;
		final int generations = 500;

		//TODO Some tricksy metrics are here
		Analyzer test = new Analyzer();	
		test.withReferenceSet(referenceSet).withProblemClass(RRASRMOO.class, instanceFile);


		Instrumenter instrumenter = new Instrumenter()
		.withProblemClass(RRASRMOO.class, instanceFile)
		.withReferenceSet(referenceSet)
		.attachGenerationalDistanceCollector()
		.attachInvertedGenerationalDistanceCollector()
		.attachHypervolumeCollector()
		.attachApproximationSetCollector()
		.attachElapsedTimeCollector()
		.attachSpacingCollector()
		.attach(new IndicatorCollector(new MaximumParetoFrontError(new RRASRMOO(), test.getReferenceSet())))
		.withFrequency(popSize);

		// solve using a Genetic Algorithm
		final NondominatedPopulation result = new Executor()
		.withProblemClass(RRASRMOO.class, instanceFile)
		.withAlgorithm(algorithm)
		.withMaxEvaluations(popSize*generations)
		.withProperty("populationSize", popSize)
		.withProperty("swap.rate", "0.25") // swap mutation
		.withProperty("insertion.rate", "0.25") // insertion mutation
		.withProperty("pmx.rate", "0.75") // partially mapped crossover
		.withInstrumenter(instrumenter)
		.run();

		Accumulator accumulator = instrumenter.getLastAccumulator();
		//#nodes,sparsity,distribution,HYPERVOLUME,CONTRIBUTION,GENDISTANCE,MPFERROR,RUNTIME,algorithm
		try {

			CSVPrinter writer = new CSVPrinter(new FileWriter(new File(approxFolder + "/" + instanceFile.getName() + ".csv"), true), CSVFormat.EXCEL);

			for (int i = 0; i < accumulator.size("NFE"); i++) {
				printer.print(""); //#nodes
				printer.print(""); //sparsity
				printer.print(""); // distribution
				printer.print(accumulator.get("NFE", i));
				printer.print(accumulator.get("Hypervolume", i));
				printer.print(accumulator.get("Spacing", i));
				printer.print(accumulator.get("MaximumParetoFrontError", i));
				printer.print(accumulator.get("InvertedGenerationalDistance", i));
				printer.print(accumulator.get("GenerationalDistance", i));
				printer.print(accumulator.get("Elapsed Time", i));
				printer.print(algorithm);
				printer.println();
				printer.flush();

				ArrayList<Solution> sols = (ArrayList<Solution>) accumulator.get("Approximation Set", i);				


				writer.print("Generation");
				writer.print("Length");
				writer.print("Robustness");
				writer.print("Lifetime");
				writer.println();

				for (int j = 0; j < sols.size(); j++) {
					writer.print(i);
					writer.print(result.get(i).getObjective(2));
					writer.print(-result.get(i).getObjective(1));
					writer.print(-result.get(i).getObjective(0));
					writer.println();					
				}

				writer.flush();			
			}

			writer.close();
		}catch (IOException e) {

		}

	}

}


