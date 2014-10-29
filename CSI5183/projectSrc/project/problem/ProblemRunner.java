package project.problem;

import java.io.File;
import java.util.Set;

import org.moeaframework.Executor;
import org.moeaframework.Instrumenter;
import org.moeaframework.analysis.collector.Accumulator;
import org.moeaframework.core.NondominatedPopulation;

public class ProblemRunner {

	public static void main(String[] args) {
		//TODO remove nulls
		File nodeList = null;
		File activeNodeList = null;
		
		Instrumenter instrumenter = new Instrumenter()
			.withProblemClass(ProblemDefinition.class, nodeList, activeNodeList)
			.withFrequency(50)
			.attachApproximationSetCollector()
			.attachAll();

		// solve using NSGA-II
		NondominatedPopulation result = new Executor()
			.withProblemClass(ProblemDefinition.class, nodeList, activeNodeList)
			.withAlgorithm("NSGAII")
			.withMaxEvaluations(10000)
			.withProperty("populationSize", 2500)
			.withProperty("pm.rate", 0.01) //mutation
			.withProperty("sbx.rate", 0.8) //crossover
//			.withEpsilon(15)
			.withInstrumenter(instrumenter)
			.run();

		Accumulator accumulator = instrumenter.getLastAccumulator();
		
		Set<String> temp = accumulator.keySet();
		String temp2 = "01010";
		
		//includeApproximationSet
		


	}


}
