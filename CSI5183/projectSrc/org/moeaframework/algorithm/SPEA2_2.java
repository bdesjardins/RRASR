package org.moeaframework.algorithm;

import jmetal.core.SolutionSet;
import jmetal.metaheuristics.spea2.SPEA2;

import org.moeaframework.algorithm.util.Spea2Fitness;
import org.moeaframework.core.EpsilonBoxDominanceArchive;
import org.moeaframework.core.Initialization;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.NondominatedSortingPopulation;
import org.moeaframework.core.Population;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Selection;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

public class SPEA2_2 extends AbstractEvolutionaryAlgorithm {

	/**
	 * The selection operator.
	 */
	private final Selection selection;

	/**
	 * The variation operator.
	 */
	private final Variation variation;
	
	private Population spea2Archive;
	
	public SPEA2_2(Problem problem, NondominatedSortingPopulation population,
			NondominatedPopulation archive, Selection selection,
			Variation variation, Initialization initialization) {
		super(problem, population, archive, initialization);
		this.variation = variation;
		this.selection = selection;
		this.spea2Archive = new Population();
	}

	@Override
	protected void iterate() {
		Population population = getPopulation();
		
		int populationSize = population.size();		
		int archiveSize = (int) Math.round(population.size()*0.5);
				
		Population union = new Population(population);
		union.addAll(spea2Archive);
		Spea2Fitness spea = new Spea2Fitness(union);
		spea.fitnessAssign();
		spea2Archive = spea.environmentalSelection(archiveSize); 
		
		// Create a new offspringPopulation
		Population offSpringSolutionSet= new Population();    		
		while (offSpringSolutionSet.size() < populationSize) {
			Solution[] parents = selection.select(variation.getArity(),	spea2Archive);
			Solution[] children = variation.evolve(parents);

			offSpringSolutionSet.addAll(children);
		}

		// End Create a offSpring solutionSet
		evaluateAll(offSpringSolutionSet);
		
		population.clear();
		population.addAll(offSpringSolutionSet);                  		
	}

	@Override
	public NondominatedSortingPopulation getPopulation() {
		return (NondominatedSortingPopulation)super.getPopulation();
	}
	
}
