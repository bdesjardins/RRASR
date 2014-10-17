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
		evaluate();
	}
	
	public OneMaxSolution(String representation){
		this.representation = representation;
		evaluate();
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
	public void evaluate() {
		int fitness = 0;
		
		for(int i = 0; i < representation.length(); i++) {
			if (representation.charAt(i) == '1') {
				fitness++;
			}
		}
		
		this.fitness = fitness;		
	}

	@Override
	public void mutate(double mutationChance) {
		
		char[] chars = this.representation.toCharArray();
		
		for (int i = 0; i < chars.length; i++) {
			if (Math.random() < mutationChance) {
				if (chars[i] == '1') {
					chars[i] = '0';
				} else {
					chars[i] = '1';
				}
			}
		}
		
		String temp = "";
		
		for (int j = 0; j <chars.length; j++) {
			temp += chars[j];
		}
		
		this.representation = temp;
		evaluate();
	}

	@Override
	public Solution crossover(Solution parent2) {
		String part1 = this.representation.substring(0, (int) Math.floor(this.solutionSize/2));
		String part2 = ((String) parent2.getRepresentation()).substring((int) Math.floor(this.solutionSize/2), this.solutionSize);
		
		return new OneMaxSolution(part1 + part2);
	}
}
