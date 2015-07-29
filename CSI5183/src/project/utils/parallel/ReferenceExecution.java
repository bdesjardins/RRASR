package project.utils.parallel;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import org.moeaframework.Analyzer;
import org.moeaframework.Executor;
import org.moeaframework.analysis.sensitivity.ResultEntry;
import org.moeaframework.analysis.sensitivity.ResultFileReader;
import org.moeaframework.analysis.sensitivity.ResultFileWriter;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Problem;
import org.moeaframework.util.io.FileUtils;

import project.problem.RRASRMOO;

public class ReferenceExecution implements Callable {

	File instanceFile;
	int runs;
	String folderLocation;
	String destinationName;
	
	public ReferenceExecution(File instanceFile, int runs, String folderLocation, String destinationName){
		this.instanceFile = instanceFile;
		this.runs = runs;
		this.folderLocation = folderLocation;
		this.destinationName = destinationName;
	}
	
	@Override
	public Object call() throws Exception {
		//Create temp folder for files
		File tempDir = new File("Instances/temp_" + Thread.currentThread().getId());
		tempDir.mkdir();		

		createApproximationSets(instanceFile, runs, tempDir);
		mergeApproximationSets(tempDir, destinationName);
		
		org.apache.commons.io.FileUtils.deleteQuietly(tempDir);
		
		return "Created reference set for: " + instanceFile.getName() + " " + (new Date(System.currentTimeMillis())).toString();
	}
	
	private void createApproximationSets (File instanceFile, int runs, File tempDir) {
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
					.saveReferenceSet(new File(tempDir.getPath() + "/" + algorithms[j] + i + ".set"));
				}
				
				System.out.println("Created " + algorithms[j] + " set for " + instanceFile.getName());

//				System.out.println("Created a reference set");
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			} 
		}
	}
	
	private void mergeApproximationSets(File folder, String destinationName) {
//		File folder = new File(folderLocation);
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
