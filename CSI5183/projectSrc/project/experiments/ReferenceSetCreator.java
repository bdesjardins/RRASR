package project.experiments;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.moeaframework.Analyzer;
import org.moeaframework.Executor;
import org.moeaframework.analysis.sensitivity.ResultEntry;
import org.moeaframework.analysis.sensitivity.ResultFileReader;
import org.moeaframework.analysis.sensitivity.ResultFileWriter;
import org.moeaframework.core.EpsilonBoxDominanceArchive;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Problem;
import org.moeaframework.util.io.FileUtils;

import project.problem.ProblemDefinition;

public class ReferenceSetCreator {

	public static void main(String[] args) {
		String directory = "C:/Users/ben/git/CSI5183_F2014/CSI5183/Instances";
		
		String[] problems = new String[]{"Nodes", "Sparsity", "Distribution"};
		
		for (int i = 0; i <problems.length; i++) {
			File folder = new File(directory + "/" + problems[i]);
			File[] listOfFiles = folder.listFiles();
			
			for (int j = 0; j < listOfFiles.length; j++) {
				createApproximationSets(listOfFiles[j]);
				mergeApproximationSets(directory + "/temp", directory + "/" + problems[i] + "/References/" + listOfFiles[j].getName() + ".ref");
				
				try {
					FileUtils.delete(new File(directory + "/temp"));
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				System.out.println("Created reference set for: " + listOfFiles[j].getName());
			}
		}
	}
	
	private static void createApproximationSets (File instanceFile) {
		int popSize = 100;
		int generations = 500;

		int evaluations = popSize * generations;

		String[] algorithms = new String[]{"NSGAII", "NSGAIII", "SPEA2", "PAES"};

		// solve using Genetic Algorithms
		for (int j = 0; j < algorithms.length; j++) {
			final List<NondominatedPopulation> result = new Executor()
			.withProblemClass(ProblemDefinition.class, instanceFile)
			.withAlgorithm(algorithms[j])
			.withMaxEvaluations(evaluations)
			.withProperty("populationSize", popSize)
			.withProperty("swap.rate", 0.25) // mutation
			.withProperty("insertion.rate", 0.25) // mutation
			.withProperty("pmx.rate", 0.75) // crossover
//			.withEpsilon(5)
			.distributeOnAllCores()
			.runSeeds(5);
//			.run();


			try {
				for (int i = 0; i < result.size(); i++) {
					Analyzer analyzer = new Analyzer()
					.add("RRASR", result.get(i))
					.saveReferenceSet(new File("C:/Users/ben/git/CSI5183_F2014/CSI5183/Instances/temp/" + algorithms[j] + i + ".set"));
				}

				System.out.println("Created a reference set");
			} catch (IOException e) {
				//do nothing
			} 
		}
	}
	
	private static void mergeApproximationSets(String folderLocation, String destinationName) {
		File folder = new File(folderLocation);
		File[] listOfFiles = folder.listFiles();

		    for (int i = 0; i < listOfFiles.length; i++) {
		      if (listOfFiles[i].isFile()) {
		        System.out.println("File " + listOfFiles[i].getName());
		      } else if (listOfFiles[i].isDirectory()) {
		        System.out.println("Directory " + listOfFiles[i].getName());
		      }
		    }		
		
		Problem problem = new ProblemDefinition();
		NondominatedPopulation mergedSet = null;
		ResultFileReader reader = null;
		
		String destination = destinationName;
		
		double[] epsilon = null;

		// setup the merged non-dominated population
		if (epsilon != null) {
			mergedSet = new EpsilonBoxDominanceArchive(epsilon);
		} else {
			mergedSet = new NondominatedPopulation();
		}

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

			writer = new ResultFileWriter(problem, output);
			writer.append(new ResultEntry(mergedSet));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (writer != null) {
				writer.close();
			}
			if (problem != null) {
				problem.close();
			}
		}
	}
}
