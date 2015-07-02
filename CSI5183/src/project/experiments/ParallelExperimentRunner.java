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

import project.utils.parallel.ExperimentExecution;
import project.utils.parallel.ParallelCSVPrinter;

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
					if (!instances[j].isFile()) {
						continue;
					}
					
					instanceFiles.add(instances[j]);
					referenceFiles.add(new File(directory + "/" + instances[j].getParentFile().getName() + "/References/" + instances[j].getName() + ".ref"));
				}
			}

			int nrOfProcessors = Runtime.getRuntime().availableProcessors();
			ExecutorService eservice = Executors.newFixedThreadPool(nrOfProcessors);
			CompletionService <Object> cservice = new ExecutorCompletionService <Object> (eservice);
			
			int counter = 0;
			for (int instCount = 0; instCount < instanceFiles.size(); instCount++) {
				for (int alg = 0; alg < algorithms.length; alg++) {
					ParameterVector params = new ParameterVector(algorithms[alg]);
					for(int count = 1; count <= runs; count++){
						counter++;
						cservice.submit(new ExperimentExecution(instanceFiles.get(instCount), referenceFiles.get(instCount), 
								printer, algorithms[alg], count, params.nfe, params.popSize, params.xover, params.swap, params.insert));
					}
				}		
			}
			
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
			printer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		long afterTime = System.currentTimeMillis();

		System.out.println();
		System.out.println("Total Time Elapsed: " + (afterTime-beforeTime)/1000 + "s");
		System.exit(0);
	}
	
	private static class ParameterVector{
		
		public int popSize;
		public int nfe;
		public double xover;
		public double swap;
		public double insert;
		
		public ParameterVector(){
			popSize = 200;
			nfe = 100000;
			xover = 0.75;
			swap = 0.25;
			insert = 0.25;
		}
		
		//TODO - Update with tuned values
		public ParameterVector(String algorithm){
			if(algorithm.equalsIgnoreCase("NSGAII")){
				popSize = 200;
				nfe = 100000;
				xover = 0.75;
				swap = 0.25;
				insert = 0.25;				
			} else if (algorithm.equalsIgnoreCase("NSGAIII")) {
				popSize = 200;
				nfe = 100000;
				xover = 0.75;
				swap = 0.25;
				insert = 0.25;				
			} else if (algorithm.equalsIgnoreCase("SPEA2")) {
				popSize = 200;
				nfe = 100000;
				xover = 0.75;
				swap = 0.25;
				insert = 0.25;				
			} else if (algorithm.equalsIgnoreCase("PESA2")) {
				popSize = 200;
				nfe = 100000;
				xover = 0.75;
				swap = 0.25;
				insert = 0.25;				
			} else if (algorithm.equalsIgnoreCase("AGEI")) {
				popSize = 200;
				nfe = 100000;
				xover = 0.75;
				swap = 0.25;
				insert = 0.25;				
			} else if (algorithm.equalsIgnoreCase("AGEII")) {
				popSize = 200;
				nfe = 100000;
				xover = 0.75;
				swap = 0.25;
				insert = 0.25;				
			} else {
				popSize = 200;
				nfe = 100000;
				xover = 0.75;
				swap = 0.25;
				insert = 0.25;
			}
		}
		
	}
}
