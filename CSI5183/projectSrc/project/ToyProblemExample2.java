package project;

import java.io.File;
import java.io.IOException;

import org.moeaframework.Analyzer;
import org.moeaframework.Executor;
import org.moeaframework.Instrumenter;
import org.moeaframework.analysis.collector.Accumulator;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.core.variable.Permutation;

public class ToyProblemExample2 {

	public static void main(String[] args) {		

		Instrumenter instrumenter = new Instrumenter()
			.withProblemClass(ToyProblem2.class)
			.withFrequency(50)
			.attachAll()
			.withReferenceSet(new File("C:/Users/ben/git/CSI5183_F2014/CSI5183/ToyProblemSet2"));
	
		// solve using NSGA-II
		NondominatedPopulation result = new Executor()
				.withProblemClass(ToyProblem2.class)
				.withAlgorithm("eNSGAII")
				.withMaxEvaluations(10000)
				.withProperty("populationSize", 500)
				.withEpsilon(0.9)
				.withInstrumenter(instrumenter)
				.run();

		try {
			Analyzer analyzer = new Analyzer()
			.add("ToyProblem", result)
			.saveReferenceSet(new File("C:/Users/ben/git/CSI5183_F2014/CSI5183/ToyProblemSet2"));
			
			System.out.println("Created a reference set");
		} catch (IOException e) {
			//do nothing
		} finally {
			//print the results
			for (int i = 0; i < result.size(); i++) {
				Solution solution = result.get(i);
				double[] objectives = solution.getObjectives();
				
				Permutation x = (Permutation) solution.getVariable(0);
				Permutation y = (Permutation) solution.getVariable(1);
				
				String stringX = "";
				String stringY = "";
											
				System.out.println("Solution " + (i+1) + ":");
				for (int j = 0; j < x.size(); j++) {
					stringX = stringX + x.get(j) + " ";
					stringY = stringY + y.get(j) + " ";
				}
				
				System.out.println("   x = " + stringX);
				System.out.println("   y = " + stringY);
				System.out.println("  t1 = " + objectives[0]);
				System.out.println("  t2 = " + objectives[1]);
				System.out.println("  ma = " + objectives[2]);
			}
		}
			
			System.out.println();
	
		Accumulator accumulator = instrumenter.getLastAccumulator();
		
		int temp = 101;
		
		for (int i = 0; i < accumulator.size("NFE"); i++) {
			System.out.println(accumulator.get("NFE", i) + "\t" +
					accumulator.get("Hypervolume", i));
		}
	}
}
