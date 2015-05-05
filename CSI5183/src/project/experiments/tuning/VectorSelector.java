package project.experiments.tuning;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;

public class VectorSelector {

	final static Charset ENCODING = StandardCharsets.UTF_8;
	
	File parameterFile;
	File[] resultsFiles;

	Map<Integer, Double> hypervolume; //0
	Map<Integer, Double> genDistance; //1
	Map<Integer, Double> invGenDistance; //2
	Map<Integer, Double> spacing; //3
	Map<Integer, Double> maxParetoError; //5

	String[] parameterVectors;
	
	double[][] rankArray;

	public VectorSelector(File parameterFile, File resultsDirectory){
		this.parameterFile = parameterFile;

		//Create the file array
		this.resultsFiles = resultsDirectory.listFiles();
		loadParameters();
	}
	
	public void getVectors(){
		for(File file: resultsFiles){
			if(file.isDirectory()){
				continue;
			}
			
			System.out.println(file.getName() + ":");
			
			String[] results = findBest(file);
			
			for(int i = 0; i < results.length; i++){
				System.out.println(results[i]);
			}
			System.out.println();
		}
	}
	
	private String[] findBest(File result){
		hypervolume = new HashMap<Integer, Double>();
		genDistance = new HashMap<Integer, Double>();
		invGenDistance = new HashMap<Integer, Double>();
		spacing = new HashMap<Integer, Double>();
		maxParetoError = new HashMap<Integer, Double>();
		
		loadResults(result);
		rankArray = new double[hypervolume.size()][6];
		rankResults();
		
		return findBestVector();	
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
				genDistance.put(counter, Double.parseDouble(values[1]));
				invGenDistance.put(counter, Double.parseDouble(values[2]));
				spacing.put(counter, Double.parseDouble(values[3]));
				maxParetoError.put(counter, Double.parseDouble(values[5]));
				counter++;
			}			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			scanner.close();
		}
	}
	
	private void rankResults(){		
		hypervolume = sortByValues(hypervolume);
		genDistance = sortByValues(genDistance);
		invGenDistance = sortByValues(invGenDistance);
		spacing = sortByValues(spacing);
		maxParetoError = sortByValues(maxParetoError);
		
		
		
	    Integer[] hypervolume_key = hypervolume.keySet().toArray(new Integer[0]);
		Integer[] genDistance_key = genDistance.keySet().toArray(new Integer[0]);
		Integer[] invGenDistance_key = invGenDistance.keySet().toArray(new Integer[0]);
		Integer[] spacing_key = spacing.keySet().toArray(new Integer[0]);
		Integer[] maxParetoError_key = maxParetoError.keySet().toArray(new Integer[0]);
		
		//Fill the ranks
		for(int i = 0; i < hypervolume_key.length; i++){
			rankArray[hypervolume_key[i]][0] = i+1;
			rankArray[genDistance_key[i]][1] = i+1;
			rankArray[invGenDistance_key[i]][2] = i+1;
			rankArray[spacing_key[i]][3] = i+1;
			rankArray[maxParetoError_key[i]][4] = i+1;
		}
		
		//Average the ranks
		for(int i = 0; i < rankArray.length; i++){
			rankArray[i][5] = (rankArray[i][0] + rankArray[i][1] + rankArray[i][2] + rankArray[i][3] + rankArray[i][4])/5;
		}
	}
	
	private String[] findBestVector(){
		int bestVector = -1;
		double value = 99999999;
		
		for(int i = 0; i < rankArray.length; i++){
			if(rankArray[i][5] < value){
				value = rankArray[i][5];
				bestVector = i;
			}
		}
		
		String[] paramValues = parameterVectors[bestVector].split(" ");
		
		String[] info = new String[9];
		info[0] = "Vector: " + (bestVector+1);
		info[1] = "Rank: "+ value;
		info[2] = "-----------------------------------";
		info[3] = "Max Evaluations: " + paramValues[0];
		info[4] = "Population Size: " + paramValues[1];
		info[5] = "Swap Rate: " + paramValues[2];
		info[6] = "Insertion Rate: " + paramValues[3];
		info[7] = "Crossover Rate: " + paramValues[4];
		info[8] = "-----------------------------------";
		
		return info;
	}
	
	private void loadParameters(){
		Scanner scanner = null;
		ArrayList<String> vectors = new ArrayList<String>();
		
		try {
			scanner = new Scanner(this.parameterFile ,ENCODING.name());
			
			while (scanner.hasNextLine()){
				vectors.add(scanner.nextLine());
			}			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			scanner.close();
		}
		
		this.parameterVectors = vectors.toArray(new String[0]);
	}

	//Use iterator on KeySet from resulting map
	
	public static <K extends Comparable,V extends Comparable> Map<K,V> sortByValues(Map<K,V> map){
		List<Map.Entry<K,V>> entries = new LinkedList<Map.Entry<K,V>>(map.entrySet());

		Collections.sort(entries, new Comparator<Map.Entry<K,V>>() {

			@Override
			public int compare(Entry<K, V> o1, Entry<K, V> o2) {
				return o1.getValue().compareTo(o2.getValue());
			}
		});

		//LinkedHashMap will keep the keys in the order they are inserted
		//which is currently sorted on natural ordering
		Map<K,V> sortedMap = new LinkedHashMap<K,V>();

		for(Map.Entry<K,V> entry: entries){
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		return sortedMap;
	}
}

