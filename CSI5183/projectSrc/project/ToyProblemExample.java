package project;

import java.io.File;
import java.io.IOException;

import org.moeaframework.Analyzer;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;

public class ToyProblemExample {

	public static void main(String[] args) {		

//		Instrumenter instrumenter = new Instrumenter()
//			.withProblemClass(ToyProblem.class)
//			.withFrequency(50)
//			.attachApproximationSetCollector()
//			.attachHypervolumeCollector()
//			.withReferenceSet(new File("C:/Users/ben/git/CSI5183_F2014/CSI5183/ToyProblemSet"));

		

		
		// solve using NSGA-II
		NondominatedPopulation result = new Executor()
				.withProblemClass(ToyProblem.class)
				.withAlgorithm("NSGAII")
				.withMaxEvaluations(50)
				.withProperty("populationSize", 10)
//				.withInstrumenter(instrumenter)
				.run();

		try {
			Analyzer analyzer = new Analyzer()
			.add("ToyProblem", result)
			.saveReferenceSet(new File("C:/Users/ben/git/CSI5183_F2014/CSI5183/ToyProblemSet"));
			
			System.out.println("Created a reference set");
		} catch (IOException e) {
			//do nothing
		} finally {
			//print the results
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
			
			System.out.println();
	
//		Accumulator accumulator = instrumenter.getLastAccumulator();
//		
//		for (int i = 0; i < accumulator.size("NFE"); i++) {
//			System.out.println(accumulator.get("NFE", i) + "\t" +
//					accumulator.get("Hypervolume", i));
//		}
	}
}
