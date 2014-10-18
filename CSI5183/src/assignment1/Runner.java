package assignment1;

import java.util.Scanner;

public class Runner {

	public static void main(String[] args) {
		Scanner input = new Scanner(System.in);
		
		System.out.println("Please select problem: 1 for OneMax, 2 for SimpleMax, 3 for TSP");		
		int problem = input.nextInt();
		
		if (problem == 4) {
			System.out.println("Running TSP");
			System.out.println("-----------------------------------------------------------------");
			runTSP(100, 1000, 0.001, 0.80, "berlin52.txt");
			//runTSP(popSize, generations, mutationChance, reproductionChance, fileLocation);
			
			input.close();			
			System.exit(0);
		}
		
		System.out.println("Please input a population size:");		
		int popSize = input.nextInt();
		
		System.out.println("Please input how many generations to run:");		
		int generations = input.nextInt();
		
		System.out.println("Please input a mutation chance (0.001):");		
		double mutationChance = input.nextDouble();
		
		System.out.println("Input a fitness reproduction chance (0.8):");		
		double reproductionChance = input.nextDouble();
						
		String fileLocation = "";
		if (problem == 3) {
			System.out.println("Input a TSP file:");
			fileLocation = input.nextLine();
		}
		System.out.println("");
				
//		int generations = 50;
//		int popSize = 150;
		
		if (problem == 1) {
			System.out.println("Running OneMax");
			System.out.println("-----------------------------------------------------------------");
			
			//runOneMax(100, 50, 0.001, 0.8);
			runOneMax(popSize, generations, mutationChance, reproductionChance);
		} else if (problem ==2) {
			System.out.println("Running SimpleMax");
			System.out.println("-----------------------------------------------------------------");
			//runSimpleMax(50, 1, 0.005, 0.70);
			runSimpleMax(popSize, generations, mutationChance, reproductionChance);
		} else if (problem == 3){
			System.out.println("Running TSP");
			System.out.println("-----------------------------------------------------------------");
			//runSimpleMax(50, 1, 0.005, 0.70);
			runTSP(popSize, generations, mutationChance, reproductionChance, fileLocation);
		} else {
			System.out.println("Selected problem doesn't exist. GET OUT NOW");
		}
		
		input.close();
	}
	
	private static void runOneMax(int popSize, int generations, double mutation, double reproduction) {
		BasicGA genetic = new BasicGA(new OneMaxSolution(), mutation, reproduction);
		
		for (int i = 0; i < popSize; i++) {
			genetic.addSolution(new OneMaxSolution());
		}
		
		genetic.runGeneration(generations);
	}
	
	private static void runSimpleMax(int popSize, int generations, double mutation, double reproduction) {
		BasicGA genetic = new BasicGA(new SimpleMaxSolution(), mutation, reproduction);
		
		for (int i = 0; i < popSize; i++) {
			genetic.addSolution(new SimpleMaxSolution());
		}
		
		genetic.runGeneration(generations);
	}
	
	private static void runTSP(int popSize, int generations, double mutation, double reproduction, String fileLocation) {
		BasicGA genetic = new BasicGA(new TSPSolution(fileLocation), mutation, reproduction);
		
		for (int i = 0; i < popSize; i++) {
			genetic.addSolution(new TSPSolution());
		}
		
		genetic.runGeneration(generations);
	}
	
	
}
