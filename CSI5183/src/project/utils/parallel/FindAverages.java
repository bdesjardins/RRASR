package project.utils.parallel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.moeaframework.analysis.sensitivity.MatrixReader;

/**
 * 
 * @author bdesjardins
 * 
 * Callable function to be used by multi-threaded executor
 * 
 * Creates *.avg files for parametric tuning.
 * 
 * Used by {@link ParametricTuner} and {@link MRParametricTuner}
 * 
 */
public class FindAverages implements Callable {

	File dataFolder;
	String outputLocation;
	
	public FindAverages(File dataFolder, String outputLocation){
		this.dataFolder = dataFolder;
		this.outputLocation = outputLocation;
	}
	
	@Override
	public Object call() throws Exception {
		findAverages();
		return dataFolder;
	}

	private void findAverages(){
		String mode = "average";
		PrintStream out = null;
		List<double[][]> entries = new ArrayList<double[][]>();
		SummaryStatistics statistics = new SummaryStatistics();
//		OptionCompleter completer = new OptionCompleter("minimum", "maximum", "average", "stdev", "count");
		
		try {
			//load data from all input files
			for (File filename : dataFolder.listFiles()) {
				entries.add(load(filename));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//validate the inputs
		if (entries.isEmpty()) {
			throw new IllegalArgumentException("requires at least one file");
		}
		
		int numberOfRows = -1;
		int numberOfColumns = -1;
		
		for (int i=0; i<entries.size(); i++) {
			if (numberOfRows == -1) {
				numberOfRows = entries.get(i).length;
				
				if (numberOfRows == 0) {
					throw new IllegalArgumentException("empty file");
				}
			} else if (numberOfRows != entries.get(i).length) {
				throw new IllegalArgumentException("unbalanced rows");
			}
			
			if (numberOfColumns == -1) {
				numberOfColumns = entries.get(i)[0].length;
			} else if (numberOfColumns != entries.get(i)[0].length) {
				throw new IllegalArgumentException("unbalanced columns:");
			}
		}
		
		try {
			//instantiate the writer
			out = new PrintStream(outputLocation);	
		
			//compute the statistics
			for (int i=0; i<numberOfRows; i++) {
				for (int j=0; j<numberOfColumns; j++) {
					statistics.clear();
					
					for (int k=0; k<entries.size(); k++) {
						double value = entries.get(k)[i][j];
						
						statistics.addValue(value);
					}
					
					if (j > 0) {
						out.print(' ');
					}
					
					if (mode.equals("minimum")) {
						out.print(statistics.getMin());
					} else if (mode.equals("maximum")) {
						out.print(statistics.getMax());
					} else if (mode.equals("average")) {
						out.print(statistics.getMean());
					} else if (mode.equals("stdev")) {
						out.print(statistics.getStandardDeviation());
					} else if (mode.equals("count")) {
						out.print(statistics.getN());
					} 
				}
				
				out.println();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if ((out != null) && (out != System.out)) {
				out.close();
			}
		}
	}
	
	/**
	 * Loads the data from the specified file.
	 * 
	 * @param file the file containing numeric data
	 * @return the data from the specified file
	 * @throws IOException if an I/O error occurred
	 */
	private double[][] load(File file) throws IOException {
		MatrixReader reader = null;
		
		try {
			reader = new MatrixReader(file);
			List<double[]> data = new ArrayList<double[]>();
			
			while (reader.hasNext()) {
				data.add(reader.next());
			}
			
			return data.toArray(new double[0][]);
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}
}
