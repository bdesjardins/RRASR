package assignment1;

public class Runner {

	public static void main(String[] args) {
		int generations = 50;
		int popSize = 150;
		
		BasicGA genetic = new BasicGA(new OneMaxSolution());
		
		for (int i = 0; i < popSize; i++) {
			genetic.addSolution(new OneMaxSolution());
		}
		
		genetic.runGeneration(generations);
	}
	
	
}
