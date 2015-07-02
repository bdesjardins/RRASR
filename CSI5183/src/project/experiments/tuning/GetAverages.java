package project.experiments.tuning;

public class GetAverages {

	public static void main(String[] args){
		String[] algorithms = new String[]{"NSGAII","NSGAIII","SPEA2","PESA2","AGEI","AGEII"};
		
		ParametricTuner.getAvgs(algorithms);
	}
	
}
