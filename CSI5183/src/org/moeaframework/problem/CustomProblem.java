package org.moeaframework.problem;

import org.moeaframework.core.Solution;

public abstract class CustomProblem extends AbstractProblem {

	public CustomProblem(int numberOfVariables, int numberOfObjectives) {
		super(numberOfVariables, numberOfObjectives);
	}

	@Override
	public void evaluate(Solution solution) {
		
	}
	
	public void evaluateJmetal(jmetal.core.Solution solution) {}

	@Override
	public Solution newSolution() {
		return null;
	}

	protected void repair(Solution solution) {}

}
