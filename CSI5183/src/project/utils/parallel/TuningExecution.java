package project.utils.parallel;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Callable;

import org.moeaframework.analysis.sensitivity.MetricFileWriter;
import org.moeaframework.analysis.sensitivity.OutputWriter;
import org.moeaframework.analysis.sensitivity.ParameterFile;
import org.moeaframework.analysis.sensitivity.ResultEntry;
import org.moeaframework.analysis.sensitivity.SampleReader;
import org.moeaframework.core.Algorithm;
import org.moeaframework.core.FrameworkException;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.PRNG_local;
import org.moeaframework.core.PopulationIO;
import org.moeaframework.core.Problem;
import org.moeaframework.core.indicator.QualityIndicator;
import org.moeaframework.core.spi.AlgorithmFactory;
import org.moeaframework.problem.TimingProblem;


/**
 * 
 * @author bdesjardins
 *
 * Callable object containing the methods for running
 * a parameter tuning execution.
 * 
 * Used by {@link MRParametricTuner} and {@link ParametricTuner}
 */
public class TuningExecution implements Callable {

	String algorithmName;
	Problem problem;
	File instanceFile;
	ParameterFile parameterFile;
	File inputFile;
	File outputFile;
	long seed;
	
	public TuningExecution(String algorithmName,Problem problem, File instanceFile, ParameterFile parameterFile, File inputFile, File outputFile, long seed){
		this.algorithmName = algorithmName;
		this.problem = problem;
		this.instanceFile = instanceFile;
		this.parameterFile = parameterFile;
		this.inputFile = inputFile;
		this.outputFile = outputFile;
		this.seed = seed;
	}
	
	@Override
	public Object call() throws Exception {
		runAlgorithm(algorithmName, problem, instanceFile, parameterFile, inputFile, outputFile, seed);
		return outputFile.getName();
	}
	
	private void runAlgorithm(String algorithmName,Problem problem, File instanceFile, ParameterFile parameterFile, File inputFile, File outputFile, long seed) throws IOException {		
		SampleReader input = null;
		OutputWriter output = null;
		PRNG_local random = new PRNG_local();
		
		File referenceFile = new File(instanceFile.getParentFile().getPath() + "/References/" + instanceFile.getName() + ".ref");

		// open the resources and begin processing
		try {
			input = new SampleReader(new FileReader(inputFile),parameterFile);
			
			NondominatedPopulation referenceSet = new NondominatedPopulation(PopulationIO.readObjectives(referenceFile));			
			QualityIndicator indicator = new QualityIndicator(problem, referenceSet);

			output = new MetricFileWriter(indicator, outputFile);

			// resume at the last good output
			for (int i = 0; i < output.getNumberOfEntries(); i++) {
				if (input.hasNext()) {
					input.next();
				} else {
					throw new FrameworkException("output has more entries than input");
				}
			}
			// setup any default parameters
			Properties defaultProperties = new Properties();

			// seed the pseudo-random number generator
			random.setSeed(seed);

			// process the remaining runs
			while (input.hasNext()) {
				Properties properties = input.next();
				properties.putAll(defaultProperties);

				process(algorithmName,properties, problem, output);
			}
		} finally {
			if (output != null) {
				output.close();
			}
			if (input != null) {
				input.close();
			}
			if (problem != null) {
				problem.close();
			}
		}
	}
	
	/**
	 * Performs a single run of the specified algorithm using the parameters.
	 * 
	 * @param algorithmName the algorithm name
	 * @param properties the parameters stored in a properties object
	 * @throws IOException if an I/O error occurred
	 */
	private void process(String algorithmName, Properties properties, Problem problem, OutputWriter output)
			throws IOException {
		// instrument the problem to record timing information
		TimingProblem timingProblem = new TimingProblem(problem);

		Algorithm algorithm = AlgorithmFactory.getInstance().getAlgorithm(
				algorithmName, properties, timingProblem);

		int maxEvaluations = 100000;
		// find the maximum NFE to run
		if (properties.containsKey("maxEvaluations")) {
			maxEvaluations = (int) Double.parseDouble(properties.getProperty("maxEvaluations"));
//			throw new FrameworkException("maxEvaluations not defined");
		}

		// run the algorithm
		long startTime = System.nanoTime();
		while (!algorithm.isTerminated()
				&& (algorithm.getNumberOfEvaluations() < maxEvaluations)) {
			algorithm.step();
		}
		long endTime = System.nanoTime();

		// extract the result and free any resources
		NondominatedPopulation result = algorithm.getResult();
		algorithm.terminate();

		// record instrumented data
		Properties timingData = new Properties();
		timingData.setProperty("EvaluationTime",
				Double.toString(timingProblem.getTime()));
		timingData.setProperty("TotalTime",
				Double.toString((endTime - startTime) / 1e9));

		// write result to output
		output.append(new ResultEntry(result, timingData));
	}

}
