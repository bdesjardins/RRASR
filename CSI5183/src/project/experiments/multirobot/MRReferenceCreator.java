package project.experiments.multirobot;

import java.io.File;
import java.util.Scanner;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import project.utils.parallel.MRReferenceExecution;

/**
 * 
 * @author bdesjardins
 * 
 * Creates reference sets for all available instance files
 * 
 * Each run (instance file/ algorithm/# of robots) is done in a separate thread. 
 * This application will create a number of threads equal to the number
 * of available processing threads on the executing machine.
 */
public class MRReferenceCreator {

	/**
	 * Self contained class for creating reference sets.
	 *
	 * Assumes that all reference folders will be in the same location
	 * as the jar for this program.
	 * 
	 * Folders to target are hard coded as "problems"
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
//		Scanner input = new Scanner(System.in);
//		
//		System.out.println("Please input the desired number of runs:");	//30 runs	
//		int runs = input.nextInt();
//		input.close();
		
		int runs = 30;
		System.out.println("Starting!");
		
		long beforeTime = System.currentTimeMillis();
		
		String directory = "Instances";
		
		String[] problems = new String[]{"Nodes","Distribution"};
//		String[] problems = new String[]{"Test"};
		
		int nrOfProcessors = Runtime.getRuntime().availableProcessors();
		ExecutorService eservice = Executors.newFixedThreadPool(nrOfProcessors);
		CompletionService <Object> cservice = new ExecutorCompletionService <Object> (eservice);
		
		int counter = 0;
		
		for (int i = 0; i <problems.length; i++) {
			File folder = new File(directory + "/" + problems[i]);
			
			if (!folder.exists()) {
				continue;
			}
			
			File[] listOfFiles = folder.listFiles();
			
			for (int robots = 2; robots <= 5; robots++) { //For 2,3,4, and 5 robot tests				
				File refDir = new File(directory + "/" + problems[i] + "/References_" + robots);
				refDir.mkdir();
				for (int j = 0; j < listOfFiles.length; j++) {
					if (!listOfFiles[j].isFile()) {
						continue;
					}
					counter++;						
					cservice.submit(new MRReferenceExecution(listOfFiles[j], runs, 
							(directory + "/temp"), (refDir.getAbsolutePath() + "/" + listOfFiles[j].getName() + ".ref"), robots));
				}
				
				System.out.println("Reference sets for " + robots + "robots created");
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
		long afterTime = System.currentTimeMillis();

		System.out.println("Time Elapsed: " + (afterTime-beforeTime)/1000 + "s");
		System.exit(0);
	}
}
