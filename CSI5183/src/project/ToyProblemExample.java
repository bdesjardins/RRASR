package project;

import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;

public class ToyProblemExample {

	public static void main(String[] args) {
		// solve using NSGA-II
		NondominatedPopulation result = new Executor()
				.withProblemClass(ToyProblem.class)
				.withAlgorithm("NSGAII")
				.withMaxEvaluations(5000)
				.withProperty("populationSize", 50)
				.run();

		// print the results
		for (int i = 0; i < result.size(); i++) {
			Solution solution = result.get(i);
			double[] objectives = solution.getObjectives();
										
			System.out.println("Solution " + (i+1) + ":");
			System.out.println("   x = " + EncodingUtils.getInt(solution.getVariable(0)));
			System.out.println("   y = " + EncodingUtils.getInt(solution.getVariable(1)));
			System.out.println("  f1 = " + objectives[0]);
			System.out.println("  f2 = " + objectives[1]);
		}			
	}
}
