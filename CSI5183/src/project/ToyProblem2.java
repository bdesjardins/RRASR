package project;

import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.core.variable.Permutation;
import org.moeaframework.core.variable.RealVariable;
import org.moeaframework.problem.AbstractProblem;

public class ToyProblem2 extends AbstractProblem {
	public ToyProblem2() {
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
		Permutation x = (Permutation) solution.getVariable(0);
		Permutation y = (Permutation) solution.getVariable(1);
		
		double opt1 = 0;
		
		for (int i = 0; i < x.size(); i++) {
			if (i == x.size()-1) {
				opt1 += x.get(i) * x.get(0);
			} else {			
				opt1 += x.get(i) * x.get(i+1);
			}
		}
		
		double opt2 = 0;
		
		for (int i = 0; i < y.size(); i++) {
			if (i == y.size()-1) {
				opt2 += y.get(i) * y.get(0);
			} else {			
				opt2 += y.get(i) * y.get(i+1);
			}
		}
		
		double opt3 = 0;
		
		for (int i = 0; i < y.size(); i++) {
			if (x.get(i) == y.get(i)) {
				opt3++;
			}
		}
		
		
		solution.setObjective(0, opt1);
		solution.setObjective(1, opt2);
		solution.setObjective(2, opt3);
	}

	@Override
	public Solution newSolution() {		
		//Solution(numberOfVariables, numberOfObjectives);
		Solution solution = new Solution(2,3);
		
		solution.setVariable(0, new Permutation(25));
		solution.setVariable(1, new Permutation(25));
		
		return solution;
	}

}
