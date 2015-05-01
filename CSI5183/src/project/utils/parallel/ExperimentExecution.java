package project.utils.parallel;

import java.io.File;
import java.util.concurrent.Callable;

import org.moeaframework.Analyzer;
import org.moeaframework.Executor;
import org.moeaframework.Instrumenter;
import org.moeaframework.analysis.collector.Accumulator;
import org.moeaframework.analysis.collector.IndicatorCollector;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.indicator.MaximumParetoFrontError;

import project.problem.RRASRMOO;

public class ExperimentExecution implements Callable {

	File instanceFile;
	File referenceSet;
	ParallelCSVPrinter printer;
	String algorithm;
	int run;
	int popSize;
	int generations;
	double xover;
	double mutation;
	String problem;
	
	public ExperimentExecution(File instanceFile, File referenceSet, ParallelCSVPrinter printer, String algorithm, int run, int popSize, int gens,
			double xover, double mutation){
		this.instanceFile = instanceFile;
		this.referenceSet = referenceSet;
		this.printer = printer;
		this.algorithm = algorithm;
		this.run = run;
		this.popSize = popSize;
		this.generations = gens;
		this.xover = xover;
		this.mutation = mutation;
		
		this.problem = instanceFile.getParentFile().getName();
	}

	@Override
	public Object call() {	
		long beforeTime = System.currentTimeMillis();

		Analyzer test = new Analyzer();	
		test.withReferenceSet(referenceSet).withProblemClass(RRASRMOO.class, instanceFile);

		Instrumenter instrumenter = new Instrumenter()
		.withProblemClass(RRASRMOO.class, instanceFile)
		.withReferenceSet(referenceSet)
		.attachGenerationalDistanceCollector()
		.attachInvertedGenerationalDistanceCollector()
		.attachHypervolumeCollector()
		.attachElapsedTimeCollector()
		.attachSpacingCollector()
		.attach(new IndicatorCollector(new MaximumParetoFrontError(new RRASRMOO(), test.getReferenceSet())))
		.withFrequency(popSize);

		// solve using a Genetic Algorithm
		@SuppressWarnings("unused")
		final NondominatedPopulation result = new Executor()
		.withProblemClass(RRASRMOO.class, instanceFile)
		.withAlgorithm(algorithm)
		.withMaxEvaluations(popSize*generations)
		.withProperty("populationSize", popSize)
		.withProperty("swap.rate", ""+mutation) // swap mutation
		.withProperty("insertion.rate", ""+mutation) // insertion mutation
		.withProperty("pmx.rate", ""+xover) // partially mapped crossover
		.withInstrumenter(instrumenter)
		.run();

		Accumulator accumulator = instrumenter.getLastAccumulator();
		for (int i = 0; i < accumulator.size("NFE"); i++) {
			String[] results = new String[11];
			
			results[0] = instanceFile.getName().replaceFirst("_instance.tsp", "");
			results[1] = problem;
			results[2] = algorithm;
			results[3] = ""+run;
			results[4] = ""+ accumulator.get("NFE", i);
			results[5] = ""+ accumulator.get("Hypervolume", i);
			results[6] = ""+ accumulator.get("Spacing", i);
			results[7] = ""+ accumulator.get("MaximumParetoFrontError", i);
			results[8] = ""+ accumulator.get("InvertedGenerationalDistance", i);
			results[9] = ""+ accumulator.get("GenerationalDistance", i);
			results[10] = ""+ accumulator.get("Elapsed Time", i);
			
			printer.writeEntry(results);
			
//				printer.print(instanceFile.getName().replaceFirst("_instance.tsp", "")); //#File
//				printer.print(problem);
//				printer.print(algorithm);
//				printer.print(run); 
//				printer.print(accumulator.get("NFE", i));
//				printer.print(accumulator.get("Hypervolume", i));
//				printer.print(accumulator.get("Spacing", i));
//				printer.print(accumulator.get("MaximumParetoFrontError", i));
//				printer.print(accumulator.get("InvertedGenerationalDistance", i));
//				printer.print(accumulator.get("GenerationalDistance", i));
//				printer.print(accumulator.get("Elapsed Time", i));
//				printer.println();
//				printer.flush();					
		}
		long afterTime = System.currentTimeMillis();
		
		return instanceFile.getName() + " " + algorithm + "/" + problem + "/" + run + " - " + (afterTime-beforeTime)/1000 + "s";
	}
}
