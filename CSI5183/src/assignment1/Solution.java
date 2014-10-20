package assignment1;

public interface Solution {
	
	abstract double getFitness();
	
	public abstract String printRepresentation();
	
	abstract void evaluate();
	
	void mutate (double mutationChance, String type);
	
	Solution crossover (Solution parent2, String type);

	abstract Object getRepresentation();
	
}
