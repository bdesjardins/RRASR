package org.moeaframework.core.comparator;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;

import org.moeaframework.core.Solution;

public class SPEA2FitnessComparator implements DominanceComparator,
Comparator<Solution>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3506478457195104870L;

	public SPEA2FitnessComparator() {
		super();
	}

	@Override
	public int compare(Solution solution1, Solution solution2) {					
		double fitness1 = (double) solution1.getAttribute("fitness");			
		double fitness2 = (double) solution2.getAttribute("fitness");

		if (fitness1 > fitness2) {
			return -1;
		} else if (fitness1 < fitness2) {
			return 1;
		} else {
			return 0;
		}

	}

}
