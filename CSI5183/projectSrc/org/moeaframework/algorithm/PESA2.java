//  PESA2.java
//
//  Author:
//       Antonio J. Nebro <antonio@lcc.uma.es>
//       Juan J. Durillo <durillo@lcc.uma.es>
//
//  Copyright (c) 2011 Antonio J. Nebro, Juan J. Durillo
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
// 
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

package org.moeaframework.algorithm;



import java.util.HashMap;

import org.moeaframework.core.AdaptiveGridArchive;
import org.moeaframework.core.Initialization;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.NondominatedSortingPopulation;
import org.moeaframework.core.Population;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Selection;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

/**
 * This class implements the PESA2 algorithm. 
 */
public class PESA2 extends AbstractEvolutionaryAlgorithm{


	private Variation variation;
	private Selection selection;


	public PESA2(Problem problem, NondominatedSortingPopulation population,
			NondominatedPopulation archive, Selection selection,
			Variation variation, Initialization initialization) {
		super(problem, population, archive, initialization);
		this.variation = variation;
		this.selection = selection;

//		int capacity = 20;
//		int numberOfDivisions = 20;

//		this.gridArchive = new AdaptiveGridArchive(capacity, problem, numberOfDivisions);
	}
    
	@Override
	protected void iterate() {
		// TODO Auto-generated method stub
		Population population = getPopulation();
		int populationSize = population.size();
		
		AdaptiveGridArchive archive = getArchive();
		
		// Clear the solutionSet
		population.clear();

		// Create a new offspringPopulation   		
		while (population.size() < populationSize) {
			Solution[] parents = selection.select(variation.getArity(),	archive);
			Solution[] children = variation.evolve(parents);

			population.addAll(children);
		}

		for (int i = 0; i < population.size(); i++)
			archive.add(population.get(i));

	}

	public NondominatedSortingPopulation getPopulation() {
		return (NondominatedSortingPopulation)super.getPopulation();
	}
	
	public AdaptiveGridArchive getArchive() {
		return (AdaptiveGridArchive)super.getArchive();
	}

} // PESA2
