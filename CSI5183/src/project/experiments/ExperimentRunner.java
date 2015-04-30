package project.experiments;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;
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
		Scanner input = new Scanner(System.in);
		
		System.out.println("Input desired number of runs");
		int runs = input.nextInt();
		
		long beforeTime = System.currentTimeMillis();

		File results = new File("Files/results.csv");

		try {
			CSVPrinter printer = new CSVPrinter(new FileWriter(results, true), CSVFormat.EXCEL);
			
//			printer.print("instanceFile"); //#File
//			printer.print("algorithm");
//			printer.print("run"); 
//			printer.print("NFE");
//			printer.print("Hypervolume");
//			printer.print("Spacing");
//			printer.print("MaximumParetoFrontError");
//			printer.print("InvertedGenerationalDistance");
//			printer.print("GenerationalDistance");
//			printer.print("Elapsed Time");
//			printer.println();
//			printer.flush();

			String directory = "Instances";

//			String[] algorithms = new String[]{"NSGAII", "NSGAIII", "SPEA2"};
//			String[] algorithms = new String[]{"AGEII"};
//			String[] problems = new String[]{"Nodes", "Sparsity", "Distribution"};
			
			String[] algorithms = new String[]{"AGEII"};
			String[] problems = new String[]{"Nodes"};

			for (int counter = 1; counter <= runs; counter++) {
				for (int k = 0; k < algorithms.length; k++) {
					for (int i = 0; i <problems.length; i++) {
						File folder = new File(directory + "/" + problems[i]);
						
						if (!folder.exists()) {
							continue;
						}
						
						File[] listOfFiles = folder.listFiles();

						for (int j = 0; j < listOfFiles.length; j++) {
							File newFolder = new File("Files/" + problems[i] + "/" + algorithms[k] + "/" +
									listOfFiles[j].getName().replaceFirst("_instance.tsp", ""));
							newFolder.mkdirs();
							
							if (listOfFiles[j].isDirectory()) {
								continue;
							}
							
							File referenceSet = new File(directory + "/" + problems[i] + "/References/" + listOfFiles[j].getName() + ".ref");

							System.out.print("Starting: " + listOfFiles[j].getName() + " - " + algorithms[k] + "...");
							
							doTheThing(listOfFiles[j], referenceSet, printer, algorithms[k], newFolder, counter, problems[i]);
							
							System.out.println("Done!");
														
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

	private static void doTheThing(File instanceFile, File referenceSet, CSVPrinter printer, String algorithm, File folder, int run, String problem) {
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
		try {

//			CSVPrinter writer = new CSVPrinter(new FileWriter(new File(folder.getCanonicalPath() + "/" +
//					instanceFile.getName().replaceFirst("instance.tsp", "run" + run + ".csv")), true), CSVFormat.EXCEL);
			
//			writer.print("Generation");
//			writer.print("Length");
//			writer.print("Robustness");
//			writer.print("Lifetime");
//			writer.println();

			for (int i = 0; i < accumulator.size("NFE"); i++) {
				printer.print(instanceFile.getName().replaceFirst("_instance.tsp", "")); //#File
				printer.print(problem);
				printer.print(algorithm);
				printer.print(run); 
				printer.print(accumulator.get("NFE", i));
				printer.print(accumulator.get("Hypervolume", i));
				printer.print(accumulator.get("Spacing", i));
				printer.print(accumulator.get("MaximumParetoFrontError", i));
				printer.print(accumulator.get("InvertedGenerationalDistance", i));
				printer.print(accumulator.get("GenerationalDistance", i));
				printer.print(accumulator.get("Elapsed Time", i));
				printer.println();
				printer.flush();

				ArrayList<Solution> sols = (ArrayList<Solution>) accumulator.get("Approximation Set", i);				

//				for (int j = 0; j < sols.size(); j++) {
//					writer.print(i);
//					writer.print(sols.get(j).getObjective(2));
//					writer.print(-sols.get(j).getObjective(1));
//					writer.print(-sols.get(j).getObjective(0));
//					writer.println();					
//				}
//
//				writer.flush();			
			}

//			writer.close();
		}catch (IOException e) {

		}
	}
	
	
}


