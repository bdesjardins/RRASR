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

import project.problem.RMRASR;

public class MRExperimentExecution implements Callable {

	File instanceFile;
	File referenceSet;
	ParallelCSVPrinter printer;
	String algorithm;
	int run;
	int popSize;
	int evaluations;
	double xover;
	double swap;
	double insert;
	String problem;
	int robots;
	
	public MRExperimentExecution(File instanceFile, File referenceSet, ParallelCSVPrinter printer, String algorithm, int run, int evaluations, int popSize,
			double xover, double swap, double insert, int robots){
		this.instanceFile = instanceFile;
		this.referenceSet = referenceSet;
		this.printer = printer;
		this.algorithm = algorithm;
		this.run = run;
		this.popSize = popSize;
		this.evaluations = evaluations;
		this.xover = xover;
		this.swap = swap;
		this.insert = insert;
		this.robots = robots;
		
		this.problem = instanceFile.getParentFile().getName();
	}

	@Override
	public Object call() {	
		long beforeTime = System.currentTimeMillis();

		Analyzer test = new Analyzer();	
		test.withReferenceSet(referenceSet).withProblemClass(RMRASR.class, instanceFile);

		Instrumenter instrumenter = new Instrumenter()
		.withProblemClass(RMRASR.class, instanceFile, robots)
		.withReferenceSet(referenceSet)
		.attachGenerationalDistanceCollector()
		.attachInvertedGenerationalDistanceCollector()
		.attachHypervolumeCollector()
		.attachElapsedTimeCollector()
		.attachSpacingCollector()
		.attach(new IndicatorCollector(new MaximumParetoFrontError(new RMRASR(), test.getReferenceSet())))
		.withFrequency(popSize);

		// solve using a Genetic Algorithm
		@SuppressWarnings("unused")
		final NondominatedPopulation result = new Executor()
		.withProblemClass(RMRASR.class, instanceFile)
		.withAlgorithm(algorithm)
		.withMaxEvaluations(evaluations)
		.withProperty("populationSize", popSize)
		.withProperty("swap.rate", ""+swap) // swap mutation
		.withProperty("insertion.rate", ""+insert) // insertion mutation
		.withProperty("pmx.rate", ""+xover) // partially mapped crossover
		.withInstrumenter(instrumenter)
		.run();

		Accumulator accumulator = instrumenter.getLastAccumulator();
		for (int i = 0; i < accumulator.size("NFE"); i++) {
			String[] results = new String[12];
			
			results[0] = instanceFile.getName().replaceFirst("_instance.tsp", "");
			results[1] = problem;
			results[2] = algorithm;
			results[3] = ""+robots;
			results[4] = ""+run;
			results[5] = ""+ accumulator.get("NFE", i);
			results[6] = ""+ accumulator.get("Hypervolume", i);
			results[7] = ""+ accumulator.get("Spacing", i);
			results[8] = ""+ accumulator.get("MaximumParetoFrontError", i);
			results[9] = ""+ accumulator.get("InvertedGenerationalDistance", i);
			results[10] = ""+ accumulator.get("GenerationalDistance", i);
			results[11] = ""+ accumulator.get("Elapsed Time", i);
			
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
		
		return instanceFile.getName() + " " + algorithm + "/" + problem + "/" + "r"+run+"/"+ robots + " - " + (afterTime-beforeTime)/1000 + "s";
	}
}
