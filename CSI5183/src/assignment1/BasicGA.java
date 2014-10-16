package assignment1;

import java.util.ArrayList;

public class BasicGA implements GeneticAlgorithm {

	private ArrayList<Solution> population;
	private ArrayList<Solution> nextPopulation;
	private Solution best;
	
	private final double mutationChance = 0.001;
	
	public BasicGA(){
		this.population = new ArrayList<Solution>();
		this.nextPopulation = new ArrayList<Solution>(); 
	}
	
	@Override
	public void runGeneration(int num) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Solution getBest() {
		return this.best;
	}

	private Solution mutate(Solution solution) {
		if (Math.random() < this.mutationChance) {
			return OneMaxSolution.mutate(solution);
		} else {
			return solution;
		}
	}

	@Override
	public void crossover(Solution parent1, Solution parent2) {
		//TODO use Solution instead of OneMaxSolution in every case
		Solution child1 = OneMaxSolution.crossover(parent1, parent2);
		Solution child2 = OneMaxSolution.crossover(parent2, parent1);
		
		nextPopulation.add(mutate(child1));
		nextPopulation.add(mutate(child2));			
	}

	@Override
	public void reproduce() {
		// TODO Auto-generated method stub
		
		this.population = this.nextPopulation;
		this.nextPopulation = new ArrayList<Solution>();
	}

}
