package project.experiments.tuning;

/**
 * 
 * @author bdesjardins
 * 
 * Experimental application used create *.avg files for parametric tuning
 */
public class GetAverages {

	public static void main(String[] args){
		String[] algorithms = new String[]{"NSGAII","NSGAIII","SPEA2","PESA2","AGEI","AGEII"};
		
		ParametricTuner.getAvgs(algorithms);
	}
	
}
