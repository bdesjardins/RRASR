package project.problem.types;

import java.util.ArrayList;

import org.moeaframework.core.variable.Permutation;

public class SpecialPermutation extends Permutation {

	private static final long serialVersionUID = 7517485952358420330L;
	
	private static int  maxSensors = 2;
	private static int  startingSensors = 0;
	
	private static ArrayList<Integer> sensingHoles;
	
	@SuppressWarnings("static-access")
	public SpecialPermutation(int size, ArrayList<Integer> sensingHoles) {
		super(size);
		this.sensingHoles = sensingHoles;
		repair();
	}
	
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
		if (!isPermutation(permutation)) {
//			throw new IllegalArgumentException("invalid permutation");
			repair();
		}
		
		if (this.permutation.length != permutation.length) {
			throw new IllegalArgumentException("invalid permutation length");
		}

		this.permutation = permutation.clone();
		repair();
	}
	
	//Permutation Repair function
	@SuppressWarnings("static-access")
	private void repair() {
		int pickups = startingSensors;
		int dropoffs = this.sensingHoles.size();
		
		int[] permutation = this.toArray();
		
		//Make sure that all dropoffs are included
		for (int i = 0; i < permutation.length; i++) {
			if (sensingHoles.contains(Math.abs(permutation[i])) && permutation[i] < 0) {
				permutation[i] = -permutation[i];
			} else if (permutation[i] > 0 && !sensingHoles.contains(Math.abs(permutation[i]))) {
				pickups++;
			}
		}
		
		//If there are too many pickups, remove pickups from right to left until they balance with the dropoffs
//		if (pickups > dropoffs)
		for (int i = permutation.length-1; i >= 0; i--) {
			if (pickups <= dropoffs) {
				break;
			}

			if (permutation[i] > 0 && !sensingHoles.contains(permutation[i])) {
				permutation[i] = -permutation[i];
				pickups--;
			}
		}
		//If there are too few pickups, add pickups from left to right until they balance with the dropoffs
//		while (dropoffs > pickups)
		for (int i = 0; i < permutation.length; i++) {
			if (dropoffs <= pickups) {
				break;
			}

			if (permutation[i] < 0 && !sensingHoles.contains(Math.abs(permutation[i]))) {
				permutation[i] = -permutation[i];
				pickups++;
			}
		}
		
		boolean valid = false;		
		
		while (!valid) {
			int sensors = startingSensors;
			
			for (int i = 0; i < permutation.length; i++) {
				if (permutation[i] > 0 && !sensingHoles.contains(permutation[i])) {
					sensors++;
					
					if (sensors > this.maxSensors) {
						sensors--;
						
						int nextHole = findNextHole(permutation, i);
						
						int temp = permutation[i];
						permutation[i] = permutation[nextHole];
						permutation[nextHole] = temp;
						
						i--;
						continue;
					}
				} else if (permutation[i] > 0 && sensingHoles.contains(permutation[i])) {
					sensors--;
					
					if (sensors < 0) {
						sensors++;
						
						int nextSensor = findNextSensor(permutation,i);
						
						int temp = permutation[i];
						permutation[i] = permutation[nextSensor];
						permutation[nextSensor] = temp;
						
						i--;
						continue;
					}
				}
			}			
			
			valid = isPermutation(permutation);
		}
		
		
		
		this.permutation = permutation;
	}
	
	private int findNextHole(int[] permutation, int startingPoint) {
		for (int i = startingPoint; i < permutation.length; i++) {
			if (sensingHoles.contains(permutation[i])) {
				return i;
			}
		}
		
		return -1;
	}
	
	private int findNextSensor(int[] permutation, int startingPoint) {
		for (int i = startingPoint; i < permutation.length; i++) {
			if (permutation[i] > 0 && !sensingHoles.contains(permutation[i])) {
				return i;
			}
		}
		
		return -1;
	}
	
	//Check to see if valid SpecialPermutation
	public static boolean isPermutation(int[] permutation) {
		int sensors = startingSensors;
		
		for (int i = 0; i < permutation.length; i++) {
			for (int j = i+1; j < permutation.length; j++) {
				if (permutation[j] == permutation[i]) {
					return false;
				}
			}
			if (permutation[i] > 0 && !sensingHoles.contains(permutation[i])) {
				sensors++;				
				if (sensors > maxSensors) {
					return false;
				}
			} else if (permutation[i] > 0 && sensingHoles.contains(permutation[i])) {
				sensors--;
				
				if (sensors < 0) {
					return false;
				}
			}
		}
		return true;
	}

}
