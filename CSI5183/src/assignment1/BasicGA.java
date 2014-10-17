package assignment1;

import java.util.ArrayList;

public class BasicGA implements GeneticAlgorithm {

	private ArrayList<Solution> population;
	private ArrayList<Solution> nextPopulation;
	private Solution best;
	
	private  double mutationChance;
	private  double crossoverChance;
	
	public BasicGA(Solution type){
		this.population = new ArrayList<Solution>();
		this.nextPopulation = new ArrayList<Solution>(); 
		 
		this.best = type;
		this.mutationChance = 0.001;
		this.crossoverChance = 0.8;
	}
	
	public BasicGA(Solution type, double mutationChance, double crossoverChance){
		this.population = new ArrayList<Solution>();
		this.nextPopulation = new ArrayList<Solution>(); 
		 
		this.best = type;
		this.mutationChance = mutationChance;
		this.crossoverChance = crossoverChance;
	}
	
	@Override
	public void runGeneration(int num) {
		for (int i = 0; i < num; i++) {
			newGeneration();
			this.best.printRepresentation();
		}		
	}

	@Override
	public Solution getBest() {
		return this.best;
	}

	@Override
	public void reproduce(Solution parent1, Solution parent2) {
		//TODO use Solution instead of OneMaxSolution in every case
		Solution child1 = parent2.crossover(parent1);
		Solution child2 = parent1.crossover(parent2);
		
		child1.mutate(this.mutationChance);
		child2.mutate(this.mutationChance);
		
		if (child1.getFitness() > this.best.getFitness()) {
			this.best = child1;
		}
		if (child2.getFitness() > this.best.getFitness()) {
			this.best = child2;
		}
		
		nextPopulation.add(child1);
		nextPopulation.add(child2);			
	}

	@Override
	public void newGeneration() {
		//Add best and 1 random to provide some elitist selection
		this.nextPopulation.add(this.best);
		this.nextPopulation.add(this.population.get((int) Math.floor((Math.random() * this.population.size()))));
		
		while (this.nextPopulation.size() < this.population.size()) {
			reproduce(tournamentSelection(), tournamentSelection());
		}
		
		this.population = this.nextPopulation;
		this.nextPopulation = new ArrayList<Solution>();
	}
	
	//5-man Tournament
	private Solution tournamentSelection() {
		Solution currentBest;
		
		Solution sol1 = this.population.get((int) (Math.random() * this.population.size()));
		Solution sol2 = this.population.get((int) (Math.random() * this.population.size()));
		Solution sol3 = this.population.get((int) (Math.random() * this.population.size()));
		Solution sol4 = this.population.get((int) (Math.random() * this.population.size()));
		Solution sol5 = this.population.get((int) (Math.random() * this.population.size()));
				
		if (Math.random() > crossoverChance) {
			if (Math.random() < 0.2) {
				return sol1;
			} else if (Math.random() < 0.4) {
				return sol2;
			} else if (Math.random() < 0.6) {
				return sol3;
			} else if (Math.random() < 0.8) {
				return sol4;
			} else {
				return sol5;
			}
		} else {
			currentBest = sol1;
			
			if (currentBest.getFitness() < sol2.getFitness()) {
				currentBest = sol2;
			}
			if (currentBest.getFitness() < sol3.getFitness()) {
				currentBest = sol3;
			}
			if (currentBest.getFitness() < sol4.getFitness()) {
				currentBest = sol4;
			}
			if (currentBest.getFitness() < sol5.getFitness()) {
				currentBest = sol5;
			}
		}
		
		return currentBest;
	}

	public void addSolution(Solution sol) {
		this.population.add(sol);
	}
}
