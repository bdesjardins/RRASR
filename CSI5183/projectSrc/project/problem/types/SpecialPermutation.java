package project.problem.types;

import org.moeaframework.core.variable.Permutation;

public class SpecialPermutation extends Permutation {

	private static final long serialVersionUID = 7517485952358420330L;
	
	private int maxSensors = 2;
	
	public SpecialPermutation(int size) {
		super(size);
		repair();
	}
	
	public SpecialPermutation(int[] permutation) {
		super(permutation);
		repair();
	}
		
	@Override
	public void insert(int i, int j) {
		super.insert(i, j);
		repair();
	}
	
	@Override
	public void swap(int i, int j) {
		super.swap(i, j);
		repair();
	}
	
	@Override
	public void fromArray(int[] permutation) {
		super.fromArray(permutation);
		repair();
	}
	
	//Permutation Repair function
	private void repair() {
		
	}
	
	//Check to see if valid SpecialPermutation
	public static boolean isPermutation(int[] permutation) {
		for (int i = 0; i < permutation.length; i++) {
			boolean contains = false;
			for (int j = 0; j < permutation.length; j++) {
				if (permutation[j] == i) {
					contains = true;
					break;
				}
			}

			if (!contains) {
				return false;
			}
		}

		return true;
	}

}
