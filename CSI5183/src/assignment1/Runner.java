package assignment1;

import java.util.ArrayList;
import java.util.Scanner;

public class Runner {

	public static void main(String[] args) {
		Scanner input = new Scanner(System.in);
		
		int popSize;
		int generations;
		double mutationChance;
		double reproductionChance;
		String mutType = "";
		String xType = "";
		String fileLocation = "";
		
		
		System.out.println("Please select problem: 1 for OneMax, 2 for SimpleMax, 3 for TSP, 4 for Crossover Tester");		
		int problem = input.nextInt();
		
		if (problem == 5) {
			System.out.println("Running TSP");
			System.out.println("-----------------------------------------------------------------");
			runTSP(2000, 7000, 0.015, 0.80, "berlin52.txt", "n-append","order");
			//runTSP(popSize, generations, mutationChance, reproductionChance, fileLocation);
			
			input.close();			
			System.exit(0);
		}
		
		if (problem == 3) {
			System.out.println("Input a TSP file:");
			fileLocation = input.next();
			
			System.out.println("Please select a crossover type (order or partially-mapped):");		
			xType = input.next();
			
			System.out.println("Please select a mutation type (insertion or n-append):");		
			mutType = input.next();
		}
		
		if (problem == 4) {
			String raw1 = "";
			String raw2 = "";
			input.nextLine();
			
			System.out.println("Please input the first parent (1 2 3 4 etc...):");
			raw1 = input.nextLine();
			
			System.out.println("Please input the second parent:");
			raw2 = input.nextLine();
			
			System.out.println("Please select a crossover type (order or partially-mapped):");
			xType = input.next();
			
			crossoverTester(raw1, raw2, xType);
			input.close();
			System.exit(0);
		}
		
		System.out.println("Please input a population size:");		
		popSize = input.nextInt();
		
		System.out.println("Please input how many generations to run:");		
		generations = input.nextInt();
		
		System.out.println("Please input a mutation chance (0.001):");		
		mutationChance = input.nextDouble();
		
		System.out.println("Input a fitness reproduction chance (0.8):");		
		reproductionChance = input.nextDouble();
						

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
			runTSP(popSize, generations, mutationChance, reproductionChance, fileLocation, mutType,xType);
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
		
		genetic.runGeneration(generations, "","");
	}
	
	private static void runSimpleMax(int popSize, int generations, double mutation, double reproduction) {
		BasicGA genetic = new BasicGA(new SimpleMaxSolution(), mutation, reproduction);
		
		for (int i = 0; i < popSize; i++) {
			genetic.addSolution(new SimpleMaxSolution());
		}
		
		genetic.runGeneration(generations,"","");
	}
	
	private static void runTSP(int popSize, int generations, double mutation, double reproduction, String fileLocation, String mutType, String xType) {
		BasicGA genetic = new BasicGA(new TSPSolution(fileLocation), mutation, reproduction);
		
		for (int i = 0; i < popSize; i++) {
			genetic.addSolution(new TSPSolution());
		}
		
		genetic.runGeneration(generations,mutType,xType);
	}
	
	private static void crossoverTester(String raw1, String raw2, String type) {
		String[] temp1 = raw1.split(" ");
		String[] temp2 = raw2.split(" ");
		
		ArrayList<Integer> rep1 = new ArrayList<Integer>();
		ArrayList<Integer> rep2 = new ArrayList<Integer>();
		
		for (int i = 0; i < temp1.length; i++) {
			rep1.add(Integer.parseInt(temp1[i]));
			rep2.add(Integer.parseInt(temp2[i]));
		}
		
		TSPSolution parent1 = new TSPSolution(rep1);
		TSPSolution parent2 = new TSPSolution(rep2);
		
		TSPSolution child1 = (TSPSolution) parent1.crossover(parent2, type);
		TSPSolution child2 = (TSPSolution) parent2.crossover(parent1, type);
		
		System.out.println("Parent 1:	" + parent1.printRepresentation());
		System.out.println("Parent 2:	" + parent2.printRepresentation());
		
		System.out.println("Child 1 (P1 + P2):	" + child1.printRepresentation());
		System.out.println("Child 2 (P2 + P1):	" + child2.printRepresentation());		
	}
	
	
}
