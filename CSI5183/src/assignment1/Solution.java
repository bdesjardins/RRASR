package assignment1;

public interface Solution {
	
	abstract double getFitness();
	
	public abstract void printRepresentation();
	
	abstract void evaluate();
	
	void mutate (double mutationChance);
	
	Solution crossover (Solution parent2);

	abstract Object getRepresentation();
	
}
