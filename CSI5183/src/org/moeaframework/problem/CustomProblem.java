package org.moeaframework.problem;

import org.moeaframework.core.Solution;

public abstract class CustomProblem extends AbstractProblem {

	public CustomProblem(int numberOfVariables, int numberOfObjectives) {
		super(numberOfVariables, numberOfObjectives);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void evaluate(Solution solution) {
		// TODO Auto-generated method stub
		
	}
	
	public void evaluateJmetal(jmetal.core.Solution solution) {}

	@Override
	public Solution newSolution() {
		// TODO Auto-generated method stub
		return null;
	}

}
