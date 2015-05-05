package project.experiments.tuning;

import java.io.File;

public class RunVectorSelection {

	public static void main(String[] args){
		File tuningResults = new File("Tuning/Results");
		File vectorList = new File("Tuning/parameterList.txt");
		VectorSelector selector = new VectorSelector(vectorList, tuningResults);
		
		selector.getVectors();
	}
	
}
