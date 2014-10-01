package project;

import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.core.variable.RealVariable;
import org.moeaframework.problem.*;
import java.lang.Math;

public class ToyProblem extends AbstractProblem {

	public ToyProblem() {
		super(2,2);
	}
	
	/*
	public ToyProblem(int numberOfVariables, int numberOfObjectives) {
		super(numberOfVariables, numberOfObjectives);
	}
	
	public ToyProblem(int numberOfVariables, int numberOfObjectives, int numberOfConstraints) {
		super(numberOfVariables, numberOfObjectives, numberOfConstraints);
	}
	*/

	@Override
	public void evaluate(Solution solution) {
		int x = EncodingUtils.getInt(solution.getVariable(0));
		int y = EncodingUtils.getInt(solution.getVariable(1));
		
		double opt1 = 2 + Math.pow((x-2), 2) + Math.pow((y-1), 2);
		double opt2 = 9 * x + Math.pow((y-1), 2);
		
		solution.setObjective(0, opt1);
		solution.setObjective(1, opt2);
	}

	@Override
	public Solution newSolution() {		
		//Solution(numberOfVariables, numberOfObjectives);
		Solution solution = new Solution(2,2);
		
		solution.setVariable(0, new RealVariable(-7.0, 13.0));
		solution.setVariable(1, new RealVariable(4.0, 24.0));
		
		return solution;
	}

}
