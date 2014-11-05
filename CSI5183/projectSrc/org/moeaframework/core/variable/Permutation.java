/* Copyright 2009-2014 David Hadka
 *
 * This file is part of the MOEA Framework.
 *
 * The MOEA Framework is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * The MOEA Framework is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the MOEA Framework.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.moeaframework.core.variable;

import java.util.ArrayList;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.moeaframework.core.Variable;

/**
 * Decision variable for permutations.
 */
public class Permutation implements Variable {

	private static final long serialVersionUID = 5690584295426235286L;

	/**
	 * The permutation array.
	 */
	protected int[] permutation;
	
	private static int  maxSensors = 2;
	private static int  startingSensors = 0;
	
	private static ArrayList<Integer> sensingHoles;

	/**
	 * Constructs a permutation variable with the specified number of
	 * elements.
	 * 
	 * @param size the number of elements in the permutation
	 */
	public Permutation(int size) {
		super();

		permutation = new int[size];
		
		ArrayList<Integer> temp = new ArrayList<Integer>();
		for (int i = 0; i < size; i++) {
			temp.add(i);
		}
		
		while (temp.size() > 0) {
			permutation[size - temp.size()] = (temp.remove((int) Math.floor(Math.random() * temp.size())));
		}		
		
		repair();
	}

	/**
	 * Constructs a permutation variable using the specified permutation
	 * array.
	 * 
	 * @param permutation the permutation array
	 * @throws IllegalArgumentException if the permutation array is not a valid
	 *         permutation
	 */
	public Permutation(int[] permutation) {
		super();
		this.permutation = permutation;

		//this call is necessary to ensure the invariants hold
		fromArray(permutation);
	}
	
	@SuppressWarnings("static-access")
	public Permutation(int size, ArrayList<Integer> sensingHoles) {
		super();

		permutation = new int[size];

		ArrayList<Integer> temp = new ArrayList<Integer>();
		for (int i = 0; i < size; i++) {
			temp.add(i);
		}
		
		while (temp.size() > 0) {
			permutation[size - temp.size()] = (temp.remove((int) Math.floor(Math.random() * temp.size())));
		}
		
		this.sensingHoles = sensingHoles;
		repair();
	}

	@Override
	public Permutation copy() {
		return new Permutation(permutation);
	}

	/**
	 * Returns the number of elements in this permutation.
	 * 
	 * @return the number of elements in this permutation
	 */
	public int size() {
		return permutation.length;
	}

	/**
	 * Returns the value of the permutation at the specified index.
	 * 
	 * @param index the index of the permutation value to be returned
	 * @return the permutation element at the specified index
	 * @throws ArrayOutOfBoundsException if the index is out of range {@code [0,
	 *         size()-1]}
	 */
	public int get(int index) {
		return permutation[index];
	}

	/**
	 * Swaps the {@code i}th and {@code j}th elements in this permutation.
	 * 
	 * @param i the first index
	 * @param j the second index
	 * @throws ArrayIndexOutOfBoundsException if {@code i} or {@code j} is out
	 *         or range @{code [0, size()-1]}
	 */
	public void swap(int i, int j) {
		int temp = permutation[i];
		permutation[i] = -permutation[j];
		permutation[j] = -temp;
		
		repair();
	}

	/**
	 * Removes the {@code i}th element and inserts it at the {@code j}th
	 * position.
	 * 
	 * @param i the first index
	 * @param j the second index
	 * @throws ArrayIndexOutOfBoundsException if {@code i} or {@code j} is out
	 *         or range @{code [0, size()-1]}
	 */
	public void insert(int i, int j) {
		int temp = permutation[i];

		// shifts entries in the permutation
		if (i < j) {
			for (int k = i + 1; k <= j; k++) {
				permutation[k - 1] = permutation[k];
			}
		} else if (i > j) {
			for (int k = i - 1; k >= j; k--) {
				permutation[k + 1] = permutation[k];
			}
		}

		permutation[j] = -temp;
		
		repair();
	}

	/**
	 * Returns a copy of the permutation array.
	 * 
	 * @return a copy of the permutation array
	 */
	public int[] toArray() {
		return permutation.clone();
	}

	/**
	 * Sets the permutation array.
	 * 
	 * @param permutation the permutation array
	 * @throws IllegalArgumentException if the permutation array is not a valid
	 *         permutation
	 */
	public void fromArray(int[] permutation) {		
		int[] temp = this.permutation.clone();

		this.permutation = permutation.clone();
		
		repair();
		
		if (!isPermutation(this.permutation)) {
			this.permutation = temp;
		}
		
		if (this.permutation.length != permutation.length) {
			this.permutation = temp;
		}
	}

	/**
	 * Returns {@code true} if the specified permutation is valid; {@code false}
	 * otherwise.
	 * 
	 * @param permutation the permutation array
	 * @return {@code true} if the specified permutation is valid; {@code false}
	 *         otherwise
	 */
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
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(permutation).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if ((obj == null) || (obj.getClass() != getClass())) {
			return false;
		} else {
			Permutation rhs = (Permutation)obj;
			
			return new EqualsBuilder().append(permutation, rhs.permutation)
					.isEquals();
		}
	}
	
	//Permutation Repair function
	@SuppressWarnings("static-access")
	public void repair() {
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
}
