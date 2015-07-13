package project.experiments.tuning;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class GeneralistVectorSelector extends VectorSelector {
	
	HashMap<String, Double[]> rankLists;
	
	String[] algorithms;
	File vectorList;
	File resultsFile;
	
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
	
	private void loadResults(File result){
		Scanner scanner = null;
		try {
			scanner = new Scanner(result ,ENCODING.name());
			int counter = 0;
			
			while (scanner.hasNextLine()){
				String line = scanner.nextLine();
				
				String[] values = line.split(" ");
				
				hypervolume.put(counter, Double.parseDouble(values[0]));
				genDistance.put(counter, -1*Double.parseDouble(values[1]));
				invGenDistance.put(counter, -1*Double.parseDouble(values[2]));
				spacing.put(counter, Double.parseDouble(values[3]));
				maxParetoError.put(counter, -1*Double.parseDouble(values[5]));
				counter++;
			}			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			scanner.close();
		}
	}
	
}
