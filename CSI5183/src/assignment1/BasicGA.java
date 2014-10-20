package assignment1;

import java.util.ArrayList;

public class BasicGA implements GeneticAlgorithm {

	private ArrayList<Solution> population;
	private ArrayList<Solution> nextPopulation;
	private Solution best;
	
	private double mutationChance;
	private double crossoverChance;
	
	private static int generationNumber = 1;
	
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
	public void runGeneration(int num, String mutType, String xType) {
		for (int i = 0; i < num; i++) {
			newGeneration(mutType, xType);
			System.out.println(this.generationNumber + "	" + this.best.printRepresentation());
			generationNumber++;
		}		
	}

	@Override
	public Solution getBest() {
		return this.best;
	}

	@Override
	public void reproduce(Solution parent1, Solution parent2, String mutType, String xType) {
		//TODO use Solution instead of OneMaxSolution in every case
		Solution child = parent1.crossover(parent2, xType);
		
		child.mutate(this.mutationChance, mutType);
		
		if (child.getFitness() > this.best.getFitness()) {
			this.best = child;
		}
		
		nextPopulation.add(child);		
	}

	@Override
	public void newGeneration(String mutType, String xType) {
		//Add best and 1 random to provide some elitist selection
		this.nextPopulation.add(this.best);
		
		while (this.nextPopulation.size() < this.population.size()) {
			reproduce(tournamentSelection(), tournamentSelection(), mutType, xType);
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
