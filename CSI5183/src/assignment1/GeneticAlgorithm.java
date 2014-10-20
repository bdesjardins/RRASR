package assignment1;

public abstract interface GeneticAlgorithm {
		
	public void runGeneration(int num, String mutType, String xType);
	
	public Solution getBest();

	void newGeneration(String mutType, String xType);

	void reproduce(Solution parent1, Solution parent2, String mutType, String xType);
}
