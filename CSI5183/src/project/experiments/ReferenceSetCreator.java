package project.experiments;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import org.moeaframework.Analyzer;
import org.moeaframework.Executor;
import org.moeaframework.analysis.sensitivity.ResultEntry;
import org.moeaframework.analysis.sensitivity.ResultFileReader;
import org.moeaframework.analysis.sensitivity.ResultFileWriter;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Problem;
import org.moeaframework.util.io.FileUtils;

import project.problem.RRASRMOO;

public class ReferenceSetCreator {

	public static void main(String[] args) {
		Scanner input = new Scanner(System.in);
		
		System.out.println("Please input the desired number of runs:");		
		int runs = input.nextInt();				
		System.out.println("Starting!");
		
		long beforeTime = System.currentTimeMillis();
		
		String directory = "Instances";
		
		String[] problems = new String[]{"Nodes","Distribution","Sparsity"};
		
		for (int i = 0; i <problems.length; i++) {
			File folder = new File(directory + "/" + problems[i]);
			
			if (!folder.exists()) {
				continue;
			}
			
			File[] listOfFiles = folder.listFiles();
			
			for (int j = 0; j < listOfFiles.length; j++) {
				if (!listOfFiles[j].isFile()) {
					continue;
				}
				
				createApproximationSets(listOfFiles[j], runs);
				mergeApproximationSets(directory + "/temp", directory + "/" + problems[i] + "/References/" + listOfFiles[j].getName() + ".ref");
				
				System.out.println("Created reference set for: " + listOfFiles[j].getName() + " " + (new Date(System.currentTimeMillis())).toString());
			}
		}
		long afterTime = System.currentTimeMillis();

		System.out.println("Time Elapsed: " + (afterTime-beforeTime)/1000 + "s");
	}
	
	private static void createApproximationSets (File instanceFile, int runs) {
		int popSize = 200;
		int generations = 500;

		int evaluations = popSize * generations;

		String[] algorithms = new String[]{"NSGAII", "NSGAIII", "SPEA2","PESA2","AGEI","AGEII"};

		// solve using Genetic Algorithms
		for (int j = 0; j < algorithms.length; j++) {
			final List<NondominatedPopulation> result = new Executor()
			.withProblemClass(RRASRMOO.class, instanceFile)
			.withAlgorithm(algorithms[j])
			.withMaxEvaluations(evaluations)
			.withProperty("populationSize", popSize)
			.withProperty("swap.rate", 0.25) // mutation
			.withProperty("insertion.rate", 0.25) // mutation
			.withProperty("pmx.rate", 0.75) // crossover
//			.withEpsilon(5)
//			.distributeOnAllCores()
			.runSeeds(runs);
//			.run();
				
			try {
				for (int i = 0; i < result.size(); i++) {
					Analyzer analyzer = new Analyzer()
					.add("RRASR", result.get(i))
					.saveReferenceSet(new File("Instances/temp/" + algorithms[j] + i + ".set"));
				}
				
				System.out.println("Created " + algorithms[j] + " set for " + instanceFile.getName());

//				System.out.println("Created a reference set");
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			} 
		}
	}
	
	private static void mergeApproximationSets(String folderLocation, String destinationName) {
		File folder = new File(folderLocation);
		File[] listOfFiles = folder.listFiles();
		
		Problem problem = new RRASRMOO();
		NondominatedPopulation mergedSet = null;
		ResultFileReader reader = null;
		
		String destination = destinationName;
		
		mergedSet = new NondominatedPopulation();

		try {
			// read in result files
			for (File file : listOfFiles) {
				try {
					reader = new ResultFileReader(problem, file);

					while (reader.hasNext()) {
						mergedSet.addAll(reader.next().getPopulation());
					}
				} finally {
					if (reader != null) {
						reader.close();
					}
				}
			}
		} catch (IOException e) {
			//TODO Do nothing?
		}

		File output = new File(destination);

		ResultFileWriter writer = null;
		
		try {
			//delete the file to avoid appending
			FileUtils.delete(output);
		} catch (IOException e) {
			//Do nothing
		}
		try {
			writer = new ResultFileWriter(problem, output);
			writer.append(new ResultEntry(mergedSet));
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (writer != null) {
				writer.close();
			}
			if (problem != null) {
				problem.close();
			}
		}
	}
}
