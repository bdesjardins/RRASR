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
package org.moeaframework.core.operator.permutation;

import org.moeaframework.core.PRNG;
import org.moeaframework.core.PRNG_local;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variable;
import org.moeaframework.core.Variation;
import org.moeaframework.core.variable.Permutation;

/**
 * Insertion mutation operator. Randomly selects an entry in the permutation and
 * inserts it at some other position in the permutation.
 * <p>
 * This operator is type-safe.
 */
public class Insertion implements Variation {

	/**
	 * The probability of mutating a variable.
	 */
	private final double probability;
	private PRNG_local random;

	/**
	 * Constructs an insertion mutation operator with the specified
	 * probability of mutating a variable.
	 * 
	 * @param probability the probability of mutating a variable
	 */
	public Insertion(double probability) {
		super();
		this.probability = probability;
		this.random = new PRNG_local();
	}

	@Override
	public Solution[] evolve(Solution[] parents) {
		Solution result = parents[0].copy();

		for (int i = 0; i < result.getNumberOfVariables(); i++) {
			Variable variable = result.getVariable(i);

			if ((random.nextDouble() <= probability)
					&& (variable instanceof Permutation)) {
				evolve((Permutation)variable);
			}
		}

		return new Solution[] { result };
	}

	/**
	 * Evolves the specified permutation using the insertion mutation operator.
	 * 
	 * @param permutation the permutation to be mutated
	 */
	public void evolve(Permutation permutation) {
		int i = random.nextInt(permutation.size());
		int j = random.nextInt(permutation.size() - 1);

		if (i == j) {
			j = permutation.size() - 1;
		}

		permutation.insert(i, j);
	}

	@Override
	public int getArity() {
		return 1;
	}

}
