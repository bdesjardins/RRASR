package project.experiments.tuning;

import java.io.File;

public class RunVectorSelection {

	public static void main(String[] args){
		File tuningResults = new File("Tuning/Results");
		File vectorList = new File("Tuning/parameterList.txt");
		String[] algorithms = new String[]{"AGEI","AGEII","NSGAII","NSGAIII","PESA2","SPEA2"};
		
		//Non-averaged
//		VectorSelector selector = new VectorSelector(vectorList, tuningResults);		
//		selector.getVectors();
		
		//Averaged
		GeneralistVectorSelector selector =  new GeneralistVectorSelector(vectorList, tuningResults, algorithms);
		selector.getVectors();
	}
	
}
