package org.moeaframework.problem;

import org.moeaframework.core.Solution;

/**
 * 
 * @author bdesjardins
 *
 * Superclass designed to allow for execution for both MOEA
 * and jMetal algorithms. This class also includes a repair function
 */
public abstract class CustomProblem extends AbstractProblem {

	/**
	 * Constructs an unconstrained problem with the specified number
	 * of decision variables and objectives.
	 * 
	 * @param numberOfVariables the number of decision variables
	 * @param numberOfObjectives the number of objectives
	 */
	public CustomProblem(int numberOfVariables, int numberOfObjectives) {
		super(numberOfVariables, numberOfObjectives);
	}
	
	/**
	 * Allows the jMetal adapter to execute this problem type and still
	 * record metrics at each generation
	 * 
	 * @param solution jMetal solution object
	 */
	public void evaluateJmetal(jmetal.core.Solution solution) {}

	/**
	 * Repairs the solution to make sure it is valid.
	 * 
	 * @param solution Solution to be repaired.
	 */
	protected void repair(Solution solution) {}

}
