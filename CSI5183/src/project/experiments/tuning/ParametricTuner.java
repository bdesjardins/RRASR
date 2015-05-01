package project.experiments.tuning;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.moeaframework.analysis.sensitivity.ParameterFile;

import project.problem.RRASRMOO;
import project.utils.parallel.FindAverages;
import project.utils.parallel.TuningExecution;

public class ParametricTuner {
	
	public static void main(String[] args){
		String[] algorithms = new String[]{"NSGAII","NSGAIII","SPEA2","PESA2","AGEI","AGEII"}; //TODO
//		String[] algorithms = new String[]{"NSGAII"};
		File tuningDir = new File("Tuning/Instances");
		int runs = 10;
		
		int nrOfProcessors = Runtime.getRuntime().availableProcessors();
		ExecutorService eservice = Executors.newFixedThreadPool(nrOfProcessors);
		CompletionService <Object> cservice = new ExecutorCompletionService <Object> (eservice);
		
		long beforeTime = System.currentTimeMillis();
		
		//File containing parameters and their bounds
		ParameterFile parameterFile = null;
		try {
			parameterFile = new ParameterFile(new File("Tuning/parameters.txt"));
		} catch (IOException e1) {
			e1.printStackTrace();
		} 
		
		File inputFile = new File("Tuning/parameterList.txt"); //File containing parameter vectors
		
		int counter = 0;		
		for(String algorithm : algorithms){	
			File algFile = new File("Tuning/Results/" + algorithm);
			algFile.mkdir();
			
			for(File instance: tuningDir.listFiles()){
				if(!instance.isFile()){
					continue;
				}
				File instFile = new File(algFile.getPath() + "/" + instance.getName().replace(".tsp", ""));
				instFile.mkdir();
				
				for(int i = 1; i <= runs; i++){
					//Algorithm execution
					
					File outputFile = new File(instFile.getPath() + "/" + instance.getName().replace(".tsp", "") + "_r" + i + ".met");
					
					counter++;
					cservice.submit(new TuningExecution(algorithm,new RRASRMOO(instance),instance, parameterFile, inputFile, outputFile, i));
				}				
			}
		}
		
		//wait for parallel actions to complete	
		Object taskResult;
		for(int index = 0; index < counter; index++) {
			try {
				taskResult = cservice.take().get();
				System.out.println(taskResult);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		
		eservice = Executors.newFixedThreadPool(nrOfProcessors);
		cservice = new ExecutorCompletionService <Object> (eservice);
		
		counter = 0;
		for(String algorithm : algorithms){
			File algFile = new File("Tuning/Results/" + algorithm);
			
			for(File instFolder : algFile.listFiles()){
				counter++;
				cservice.submit(new FindAverages(instFolder,"Tuning/Results/" + algorithm + "_" + instFolder.getName() + ".avg")); //File dataFolder, String outputLocation
			}
		}

		//wait for parallel actions to complete	
		for(int index = 0; index < counter; index++) {
			try {
				taskResult = cservice.take().get();
				System.out.println(taskResult);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		
		long afterTime = System.currentTimeMillis();

		System.out.println();
		System.out.println("Total Time Elapsed: " + (afterTime-beforeTime)/1000 + "s");
		System.exit(0);
	}
}
