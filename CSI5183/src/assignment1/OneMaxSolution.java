package assignment1;

public class OneMaxSolution implements Solution {

	private double fitness;
	
	private String representation;
	
	private final int solutionSize = 50;
	
	public OneMaxSolution() {
		String rep = "";
		
		for (int i = 0; i < this.solutionSize; i++) {
			if (Math.random() < 0.5) {
				rep += '1';
			}
			else {
				rep += '0';
			}
		}
		
		this.representation = rep;
		this.fitness = evaluate();
	}
	
	public OneMaxSolution(String representation){
		this.representation = representation;
		this.fitness = evaluate();
	}
	
	
	@Override
	public double getFitness() {
		return this.fitness;
	}


	@Override
	public void printRepresentation() {
		System.out.println(this.representation + "	" + this.fitness);
	}
	
	public String getRepresentation() {
		return representation;
	}

	@Override
	public double evaluate() {
		int fitness = 0;
		
		for(int i = 0; i < representation.length(); i++) {
			if (representation.charAt(i) == '1') {
				fitness++;
			}
		}
		
		return (double) fitness;		
	}

	public static Solution mutate(Solution solution) {
		
		return null;
	}

	public static Solution crossover(Solution parent1, Solution parent2) {
		
		return null;
	}
}
