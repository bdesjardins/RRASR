package project.experiments.tuning;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * 
 * @author bdesjardins
 * 
 * Selects the best parameter vector for each specified algorithm.
 * Less verbose than {@link VectorSelector}
 * 
 * Works on an algorithm by algorithm basis. Could likely be consolidated
 * with {@link VectorSelector} rather than being a subclass.
 */
public class GeneralistVectorSelector extends VectorSelector {
	
	HashMap<String, Double[]> rankLists;
	
	String[] algorithms;
	File vectorList;
	File resultsFile;
	
	/**
	 * Evaluator to determine the best parameter vectors for each algorithm
	 * based on a parametric tuning execution
	 * 
	 * Uses a set of *.avg files to determine the best parameter vector
	 * from the ones available
	 * 
	 * @param vectorList File containing the list of vectors
	 * @param resultsFile File containing results of parametric tuning
	 * @param algorithms List of algorithms to get parameters for
	 */
	public GeneralistVectorSelector(File vectorList, File resultsFile, String[] algorithms){
		super(vectorList, resultsFile);
		
		rankLists = new HashMap<String, Double[]>();
		this.algorithms = algorithms;
		this.vectorList = vectorList;
		this.resultsFile = resultsFile;
	}
	
//	public static void main(String[] args){
//		File tuningResults = new File("Tuning/Results");
//		File vectorList = new File("Tuning/parameterList.txt");
//		VectorSelector selector = new VectorSelector(vectorList, tuningResults);
//		
//		selector.getVectors();
//	}
	
	/**
	 * Finds the best vectors for each algorithm and prints
	 * them to console
	 */
	@Override
	public void getVectors(){	
		
		for(String algorithm: algorithms){
			System.out.println(algorithm + ":");
			getRanks(algorithm);
			findBestVector(algorithm);
			
			String[] results = findBestVector(algorithm);

			for(int i = 0; i < results.length; i++){
				System.out.println(results[i]);
			}

//			String[] results = rankLists.keySet().toArray(new String[0]);
//
//			for(int i = 0; i < results.length; i++){
//				System.out.println(results[i]);
//			}
			System.out.println();

		}
	}
	
	private void getRanks(String algorithm){
		
		ArrayList<File> temp = new ArrayList<File>();
		
		for(File file: resultsFiles){
			if(file.isDirectory()){
				continue;
			}
			if(!file.getName().startsWith(algorithm+"_")){
				continue;
			}
			
			temp.add(file);
		}
		
		File[] algResults = temp.toArray(new File[0]);
		
		ArrayList<Double[]> runRanks = new ArrayList<Double[]>();
		for(int i = 0; i < algResults.length; i++){
			
			runRanks.add(getVectorRanks(algResults[i]));
			
//			VectorSelector selector = new VectorSelector(vectorList, algResults[i]);
//			runRanks.add(selector.getVectorRanks());

		}
		Double[] avgRanks = new Double[runRanks.get(0).length];
		
		for (int j = 0; j < avgRanks.length; j++){
			Double rankTotal = 0.0;
			for(int k = 0; k < runRanks.size(); k++){
				rankTotal += runRanks.get(k)[j];
			}
			avgRanks[j] = rankTotal/runRanks.size();
		}			
		rankLists.put(algorithm, avgRanks);
	}
	
	/**
	 * Finds the best vector for an algorithm and build
	 * a descriptive string for it
	 * 
	 * @param algorithm Algorithm to find parameters for
	 * @return Descriptive string representation of vector
	 */
	private String[] findBestVector(String algorithm){
		int bestVector = -1;
		double value = 99999999;
		
		Double[] rankArray = rankLists.get(algorithm);
		
		for(int i = 0; i < rankArray.length; i++){
			if(rankArray[i] < value){
				value = rankArray[i];
				bestVector = i;
			}
		}
		
		String[] paramValues = parameterVectors[bestVector].split(" ");
		
		ArrayList<String> info = new ArrayList<String>();
		info.add("Vector: " + (bestVector+1));
		info.add("Rank: "+ value);
		info.add("-------------------------------------");
		info.add("Max Evaluations: " + paramValues[0]);
		info.add("Population Size: " + paramValues[1]);
		info.add("Swap Rate: " + paramValues[2]);
		info.add("Insertion Rate: " + paramValues[3]);
		info.add("Crossover Rate: " + paramValues[4]);
		info.add("-------------------------------------");
		
		return info.toArray(new String[0]);
	}
	
	private Double[] getVectorRanks(File file){
		findBest(file);
		
		Double[] ranks = new Double[hypervolume.size()];
		
		for(int i = 0; i < ranks.length; i++){
			ranks[i] = rankArray[i][5];
		}
		
		return ranks;
	}
	
	private void findBest(File result){
		hypervolume = new HashMap<Integer, Double>();
		genDistance = new HashMap<Integer, Double>();
		invGenDistance = new HashMap<Integer, Double>();
		spacing = new HashMap<Integer, Double>();
		maxParetoError = new HashMap<Integer, Double>();
		
		loadResults(result);
		rankArray = new double[hypervolume.size()][6];
		rankResults();	
	}

	
}
