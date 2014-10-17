package assignment1;

public abstract interface GeneticAlgorithm {
		
	public void runGeneration(int num);
	
	public Solution getBest();
	
//	abstract void mutate(Solution solution);
	
//	abstract void crossover(Solution parent1, Solution parent2);

	void newGeneration();

	void reproduce(Solution parent1, Solution parent2);
}
