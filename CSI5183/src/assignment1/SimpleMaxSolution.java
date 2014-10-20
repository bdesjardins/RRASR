package assignment1;

public class SimpleMaxSolution implements Solution {

	private int[] rep; //representation. Used "rep" for brevity in code
	private double fitness;
	
	
	public SimpleMaxSolution() {
		this.rep = new int[]{getRand(),getRand(),getRand(),getRand(),getRand(),getRand(),getRand(),getRand(),getRand(),getRand()};
	}
	
	public SimpleMaxSolution(int[] rep) {
		this.rep = rep;
		evaluate();
	}
	
	@Override
	public double getFitness() {
		return this.fitness;
	}

	@Override
	public String printRepresentation() {
		return "(" + rep[0] + "*" + rep[1] + "*" + rep[2] + "*" + rep[3] + "*" + rep[4] + ")/(" +
				rep[5] + "*" + rep[6] + "*" + rep[7] + "*" + rep[8] + "*" + rep[9] + ")" + "	" + this.fitness;

	}

	@Override
	public void evaluate() {
		double fit = (rep[0] * rep[1] * rep[2] * rep[3] * rep[4]) / (rep[5] * rep[6] * rep[7] * rep[8] * rep[9]);
		this.fitness = fit;
	}

	@Override
	public void mutate(double mutationChance, String type) {
		//Using a random replacement mutation		
		for (int i = 0; i < rep.length; i++) {
			if (Math.random() < mutationChance) {
				rep[i] = getRand();
			}
		}
	}
	
//	@Override
//	public void mutate(double mutationChance) {
//		//Using a swap mutation
//		if (Math.random() < mutationChance) {
//			int i =  (int)Math.floor((Math.random() * 10));
//			int j =  (int)Math.floor((Math.random() * 10));
//			
//			int temp = rep[i];
//			rep[i] = rep[j];
//			rep[j] = temp;
//		}
//	}s

	@Override
	public Solution crossover(Solution parent2, String type) {
		//uniform crossover: a b c d / e f g h i / j
		
		int[] rep2 = (int[]) parent2.getRepresentation();
		
		int[] child = new int[]{rep[0],rep[1],rep[2],rep[3],rep2[4],rep2[5],rep2[6],rep2[7],rep2[8],rep[9]};
		
		return new SimpleMaxSolution(child);
	}

	@Override
	public Object getRepresentation() {
		return rep;
	}
	
	private int getRand() {
		return (int) Math.floor((Math.random() * 10) + 1);
	}

}
