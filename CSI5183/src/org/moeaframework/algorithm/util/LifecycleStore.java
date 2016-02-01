package org.moeaframework.algorithm.util;

import org.moeaframework.core.Initialization;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.NondominatedSortingPopulation;
import org.moeaframework.core.Population;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Selection;
import org.moeaframework.core.Variation;

public class LifecycleStore {

	public Problem problem;
	public Population population;
	public Population archive;
	public Variation variation;
	public Initialization initialization;
	public Selection selection;
	
	public LifecycleStore(Problem problem, NondominatedSortingPopulation population,
			NondominatedPopulation archive, Variation variation, Initialization initialization, Selection selection) {
		this.variation = variation;
		this.selection = selection;
		this.problem = problem;
		this.population = population;
		this.archive = archive;
		this.initialization = initialization;
		this.selection = selection;
	}
	
}
