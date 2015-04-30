package project.experiments;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import project.utils.ExperimentExecution;
import project.utils.ParallelCSVPrinter;

public class ParallelExperimentRunner {

	public static void main(String[] args){
		Scanner input = new Scanner(System.in);
		
		System.out.println("Input desired number of runs");
		int runs = input.nextInt();		
		input.close();
		
		long beforeTime = System.currentTimeMillis();

		File results = new File("Files/results.csv");

		try {
//			CSVPrinter printer = new CSVPrinter(new FileWriter(results, true), CSVFormat.EXCEL);			
			ParallelCSVPrinter printer = new ParallelCSVPrinter(new FileWriter(results, true));
			//Print CSV column headers (File is appended to at top it would seem)
			printer.print("instance"); //#File
			printer.print("problem");
			printer.print("algorithm");
			printer.print("run"); 
			printer.print("NFE");
			printer.print("Hypervol");
			printer.print("Spacing");
			printer.print("MaxParetoFrontError");
			printer.print("InvGenDist");
			printer.print("GenDist");
			printer.print("Elapsed Time");
			printer.println();
		
			String directory = "Instances";
			String[] algorithms = new String[]{"NSGAII", "NSGAIII", "SPEA2", "PESA2", "AGEI", "AGEII"};
//			String[] algorithms = new String[]{"NSGAII", "NSGAIII", "PESA2", "AGEI", "AGEII"};
//			String[] algorithms = new String[]{"NSGAII", "NSGAIII", "PESA2"};
//			String[] algorithms = new String[]{"AGEI", "AGEII"};
			
			ArrayList<File> instanceFiles = new ArrayList<File>();
			ArrayList<File> referenceFiles = new ArrayList<File>();
			
			File[] instanceFolders = (new File(directory)).listFiles();
			
			for(int i = 0; i < instanceFolders.length; i++){
				File[] instances = instanceFolders[i].listFiles();
				
				for (int j = 0; j < instances.length; j++){
					if(instances[j].getName().equals("References")){
						continue;
					}
					
					instanceFiles.add(instances[j]);
					referenceFiles.add(new File(directory + "/" + instances[j].getParentFile().getName() + "/References/" + instances[j].getName() + ".ref"));
				}
			}

			int nrOfProcessors = Runtime.getRuntime().availableProcessors(); //TODO change before use
//			int nrOfProcessors = 2;
			ExecutorService eservice = Executors.newFixedThreadPool(nrOfProcessors);
			CompletionService <Object> cservice = new ExecutorCompletionService <Object> (eservice);
			
			//TODO after parametric tuning, add specific values for each algorithm
			int popSize = 200;
			int generations = 500;
			double xover = 0.75;
			double mutation = 0.25;

			for (int instCount = 0; instCount < instanceFiles.size(); instCount++) {
				for (int alg = 0; alg < algorithms.length; alg++) {
					for(int count = 1; count <= runs; count++){
						cservice.submit(new ExperimentExecution(instanceFiles.get(instCount), referenceFiles.get(instCount), 
								printer, algorithms[alg], count, popSize, generations, xover, mutation));
					}
				}		
			}
			
			Object taskResult;
			for(int index = 0; index < instanceFiles.size()* algorithms.length*runs; index++) {
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
			printer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		long afterTime = System.currentTimeMillis();

		System.out.println();
		System.out.println("Total Time Elapsed: " + (afterTime-beforeTime)/1000 + "s");
		System.exit(0);
	}
}
