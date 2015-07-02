package project.experiments.tuning;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class GeneralistVectorSelector extends VectorSelector {
	
	HashMap<String, Double[]> rankLists;
	
//	String[] algorithms;
	File vectorList;
	File resultsFile;
	
	public GeneralistVectorSelector(File vectorList, File resultsFile){
		super(vectorList, resultsFile);
		
		rankLists = new HashMap<String, Double[]>();
//		this.algorithms = algorithms;
		this.vectorList = vectorList;
		this.resultsFile = resultsFile;
	}
	
	public static void main(String[] args){
		File tuningResults = new File("Tuning/Results");
		File vectorList = new File("Tuning/parameterList.txt");
		VectorSelector selector = new VectorSelector(vectorList, tuningResults);
		
		selector.getVectors();
	}
	
	@Override
	public void getVectors(){		
		for(File file: resultsFiles){
			if(file.isDirectory()){
				continue;
			}
			
			System.out.println(file.getName() + ":");
			
			String[] results = rankLists.keySet().toArray(new String[0]);
			
			for(int i = 0; i < results.length; i++){
				System.out.println(results[i]);
			}
			System.out.println();
		}
	}
	
	private void getRanks(){
		File[] algResults = resultsFile.listFiles();
		
		for(int i = 0; i < algResults.length; i++){
			ArrayList<Double[]> runRanks = new ArrayList<Double[]>();
			
			File[] runs = algResults[i].listFiles();		
			
			for(int j = 0; j < runs.length; j++){
				VectorSelector selector = new VectorSelector(vectorList, runs[i]);
				runRanks.add(selector.getVectorRanks());
			}
			
			Double[] avgRanks = new Double[runRanks.get(0).length];
			
			for (int j = 0; j < avgRanks.length; j++){
				Double rankTotal = 0.0;
				for(int k = 0; k < runRanks.size(); k++){
					rankTotal += runRanks.get(k)[j];
				}
				avgRanks[j] = rankTotal/runRanks.size();
			}			
			rankLists.put(algResults[i].getName(), avgRanks);
		}
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
		
		String[] info = new String[12];
		info[0] = "Vector: " + (bestVector+1);
		info[1] = "Rank: "+ value;
		info[2] = "-------------------------------------";
		info[3] = "HV	GD	IGD	SPC	MPE";
//		info[4] =  rankArray[bestVector][0] + "	" + rankArray[bestVector][1] + "	" + 
//				rankArray[bestVector][2] + "	" + rankArray[bestVector][3] + "	" + rankArray[bestVector][4];
		info[4] = "";
		info[5] = "-------------------------------------";
		info[6] = "Max Evaluations: " + paramValues[0];
		info[7] = "Population Size: " + paramValues[1];
		info[8] = "Swap Rate: " + paramValues[2];
		info[9] = "Insertion Rate: " + paramValues[3];
		info[10] = "Crossover Rate: " + paramValues[4];
		info[11] = "-------------------------------------";
		
		return info;
	}
	
	
	
}
