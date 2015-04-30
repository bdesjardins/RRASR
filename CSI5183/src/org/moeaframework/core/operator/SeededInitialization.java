/* Copyright 2009-2014 David Hadka
 *
 * This file is part of the MOEA Framework.
 *
 * The MOEA Framework is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * The MOEA Framework is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the MOEA Framework.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.moeaframework.core.operator;

import org.moeaframework.core.Initialization;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variable;
import org.moeaframework.core.variable.BinaryVariable;
import org.moeaframework.core.variable.Grammar;
import org.moeaframework.core.variable.Permutation;
import org.moeaframework.core.variable.Program;
import org.moeaframework.core.variable.RealVariable;
import org.moeaframework.util.tree.Node;
import org.moeaframework.util.tree.Rules;

/**
 * Initializes all built-in decision variables.
 */
public class SeededInitialization implements Initialization {

	/**
	 * The problem.
	 */
	private final Problem problem;

	/**
	 * The initial population size.
	 */
	private final int populationSize;

	/**
	 * Constructs an initialization operator.
	 * 
	 * @param problem the problem
	 * @param populationSize the initial population size
	 */
	public SeededInitialization(Problem problem, int populationSize) {
		super();
		this.problem = problem;
		this.populationSize = populationSize;
	}

	@Override
	public Solution[] initialize() {
		Solution[] initialPopulation = new Solution[populationSize];

		for (int i = 0; i < populationSize; i++) {
			Solution solution = problem.newSolution();
			initialPopulation[i] = solution;
		}

		return initialPopulation;
	}

	/**
	 * Unused in this implementation
	 * 
	 * @param variable the variable to be initialized
	 */
	protected void initialize(Variable variable) {
		
	}

}
