/**
 * AGE.java 
 *
 * @author Markus Wagner
 * @version 1.1 (GECCO 2013 version)
 */
package org.moeaframework.algorithm;

import java.util.HashSet;
import java.util.Iterator;

import org.moeaframework.core.Initialization;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.NondominatedSortingPopulation;
import org.moeaframework.core.Population;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Selection;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;
import org.moeaframework.core.comparator.ParetoObjectiveComparator;

/**
 * This class represents the AGE algorithm (GECCO 2013 version)
 * 
 * "A Fast Approximation-Guided Evolutionary Multi-Objective Algorithm"
 * Markus Wagner and Frank Neumann
 * Genetic and Evolutionary Computation Conference 2013
 * http://cs.adelaide.edu.au/~markus/pub/2013gecco-age2.pdf
 * 
 * Creator: Markus Wagner (wagner@acrocon.com)
 * Feel free to contact me. There are many gems hidden in this code.
 */
public class AGEII extends AbstractEvolutionaryAlgorithm {

	Variation variation;
	Selection selection;
	
	double epsilonGridWidth = 0.5;
	int idealPopulationSize = -1;

	/**
	 * Constructor.
	 * Create a new AGE instance
	 * @param problem Problem to solve
	 */
	public AGEII(Problem problem, NondominatedSortingPopulation population,
			NondominatedPopulation archive, Variation variation, Initialization initialization, Selection selection) {
		super(problem, population, archive, initialization);
		this.variation = variation;
		this.selection = selection;
	}

	public static Solution convertSolutionToEpsilonGridVectorFLOOR(Solution s, double epsilonGridWidth) {
		Solution result = new Solution(s.getNumberOfVariables(), s.getNumberOfObjectives());
		for (int i=0; i<s.getNumberOfObjectives(); i++) {
			double v = s.getObjective(i);
			result.setObjective(i, epsilonGridWidth*Math.floor( v/epsilonGridWidth)  );
		}
		for (int j=0; j<s.getNumberOfVariables(); j++){
			result.setVariable(j, s.getVariable(j));
		}
		return result;
	}
	
	public static Solution moveEpsilonGridVectorOnceFurtherAway(Solution s, double epsilonGridWidth) {
		Solution result = new Solution(s.getNumberOfVariables(), s.getNumberOfObjectives());
		for (int i=0; i<s.getNumberOfObjectives(); i++) {
			double v = s.getObjective(i);
			result.setObjective(i, (v/epsilonGridWidth + 1) * epsilonGridWidth  );
		}
		for (int j=0; j<s.getNumberOfVariables(); j++){
			result.setVariable(j, s.getVariable(j));
		}
		return result;
	}

	/**
	 * Runs the AGE algorithm.
	 * @return a <code>SolutionSet</code> that is a set of non dominated solutions
	 * as a result of the algorithm execution
	 */
	protected void iterate() {

//		QualityIndicator indicators = (QualityIndicator) getInputParameter("indicators"); // this line had to be added (mw)
//		double epsilonGridWidth = ((Double) getInputParameter("epsilonGridWidth")).doubleValue();
//		double epsilonGridWidth = 0.5; //Default used in the experiments  ==== class Variable Now

		//Read the params
		Population solutionSet, offSpringSolutionSet;
		solutionSet = super.getPopulation();
		int populationSize = solutionSet.size();
		
		if(this.idealPopulationSize == -1){
			this.idealPopulationSize = populationSize;
			setupArchive();
		}
	

		/* Initialize the archive with the solutionSet. In subsequent iterations, 
		 * the newly constructed point will first be added to the archive, and then
		 * the best mu points out of mu+1 are selected that approximate the new 
		 * archive best.
		 */
		/* or: initialise with epsilonboxes, where a point is in the box */
		boolean useEpsilonBoxesArchive;
		if (epsilonGridWidth==0) useEpsilonBoxesArchive = false;
		else useEpsilonBoxesArchive = true;

		
		//Replaced with setupArchive()
//		if (useEpsilonBoxesArchive) {
//			archive = new Population();
//			for (int i = 0; i<populationSize; i++) {
//				Solution converted = convertSolutionToEpsilonGridVectorFLOOR(solutionSet.get(i),epsilonGridWidth);
//				archive.add(converted); 
//			}
//		} else {
//			this.archive.addAll(solutionSet);
//		}

		ParetoObjectiveComparator cNormal = new ParetoObjectiveComparator();

		//TODO everything before this should be initialized once

		/* START AGE block */
		boolean thinningAndCrowding = true;

		// population reduction
		/*14 nov 2012 deactivated*/            
///*!!!*/if (thinningAndCrowding) {
//			// take first front(s) and extreme points            
//			Distance distance = new Distance();
//
//			Ranking tempr = new Ranking(solutionSet);
//			int crowdingDistanceSwitch = -1;                                // for -1 for AGE-specific variants
//
//			Population t = new Population();
//
//			t.addAll(tempr.getSubfront(0));                              // definitely take first front
//			int included = 1;               
//
//			for (int i = included; i<tempr.getNumberOfSubfronts(); i++) {   // from next fronts take probabilistically
//				Population x = tempr.getSubfront(i);
//				distance.crowdingDistanceAssignment(x, super.problem.getNumberOfObjectives(), crowdingDistanceSwitch, archive);
//
//				for (int j = 0; j<x.size(); j++) {
//					if (Math.random()< (1d/(i+1)) ) {                 // decreasing probability depending on the rank number
//						t.add(x.get(j));
//					} else {}
//
//				}
//			}
//			solutionSet = t;
//
//			// crowding distances for each front
//			distance = new Distance();
//			tempr = new Ranking(solutionSet);
//			for (int i = 0; i<tempr.getNumberOfSubfronts(); i++) {
//				distance.crowdingDistanceAssignment(tempr.getSubfront(i), problem_.getNumberOfObjectives(), 0, archive);    // careful: use correct switch!
//			}
//		}    


		/* AGE 1. step: generate one new solution */
		offSpringSolutionSet = new Population(); // generate mu solutions
		Solution[] parents = new Solution[2];
		Solution[] offSpring = null;
			
		
		for (int kk = 0; kk<this.idealPopulationSize; kk++){  // loop condition: generate lambda inividuals
			// carefull! the operator may work on the fitness values (which we did not really have in the beginning)

			//make the crossover and generate a single child
			parents = selection.select(variation.getArity(),population);
			offSpring = variation.evolve(parents);

			// FITNESS EVALUATION - note: this does not set fitness, just runs the problem functions
			super.evaluate(offSpring[0]);

			/* START check if new offSpring is not (epsilon) dominated by an archive point */
			boolean newPointIsDominatedByOldArchive = false;
			boolean newPointIsDominatedByOldArchiveTakeNevertheless = false;

			for (int i = 0; i<this.archive.size(); i++) {
				Solution archiveSolution = this.archive.get(i);
				/*
				 * COMPARE: return -1, or 0, or 1 if solution1 dominates solution2, both are
				 *          non-dominated, or solution1 is dominated by solution2, respectively.
				 */                        
				int result = cNormal.compare(archiveSolution, offSpring[0]);

				int offspringVsArchiveByAtMostEpsilon = Integer.MAX_VALUE;
				if (useEpsilonBoxesArchive) {
					// floor offspring to the "lower left corner"
					result = cNormal.compare(archiveSolution, convertSolutionToEpsilonGridVectorFLOOR(offSpring[0], epsilonGridWidth));
					Solution archivePointMovedAway = moveEpsilonGridVectorOnceFurtherAway(archiveSolution, epsilonGridWidth);
					offspringVsArchiveByAtMostEpsilon = cNormal.compare(archivePointMovedAway, offSpring[0]);
				}


				if (result==-1 || result==2) {
					// break if an archive point dominates the new point
					newPointIsDominatedByOldArchive = true;

					if (useEpsilonBoxesArchive && offspringVsArchiveByAtMostEpsilon==1) {
						newPointIsDominatedByOldArchiveTakeNevertheless = true;
					}
					break;
				}
				if (result==1) {
					// remove archive point if new point dominates that one
					//                            System.out.println("remove i="+i+" of "+archive.size());
					this.archive.remove(i);
					i--;
				}
			}
			/* END check if new offSpring is not (epsilon) dominated by an archive point */

			/* the following can be done unconditionally, as we would have 
                       - stopped if offSpring[0] is dominated by the current population
			 */
			if (newPointIsDominatedByOldArchive) {
				// in case of !useEpsilonBoxesArchive: forget this point

				if (/*useEpsilonBoxesArchive &&*/ newPointIsDominatedByOldArchiveTakeNevertheless) {
					// use if the domination by the archive is not too bad (within the epsilon box)
					offSpringSolutionSet.add(offSpring[0]);
				}

				continue;

			} else {
				offSpringSolutionSet.add(offSpring[0]);

				if (useEpsilonBoxesArchive) {
					offSpring[0] = convertSolutionToEpsilonGridVectorFLOOR(offSpring[0], epsilonGridWidth);
				}

				Population temp = new Population();
				temp.add(offSpring[0]);
				this.archive.addAll(temp);
			}  
		}
		/*END generate lambda invididuals*/

		/* merge population with offSpringSolutionSet */
		solutionSet.addAll(offSpringSolutionSet);

		/* START select mu auf of mu+lambda */
		reducePopulationToSize(solutionSet, this.archive, this.idealPopulationSize);

		/* END select mu auf of mu+lambda */
//		if (true) { 
//			for (int i=0; i<1; i++) {
////				System.out.print("sample archive point: ");
//				Solution s = this.archive.get(i);
//				for (int j = 0; j<s.getNumberOfObjectives(); j++) {
//					System.out.print(s.getObjective(j)+" ");
//				}
////			System.out.println();
//			}
//		}
		
		//		return solutionSet;
	} // execute

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/// Here follow the important AGE functions. For details, contact wagner@acrocon.com ///////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/** Compute how well a solution p approximates a solution s. This is done by
	 * determining the approximation for all decision variables and objective
	 * variables and then taking the maximum of these approximations (i.e. the worst).
	 * @param p
	 * @param s
	 * @return max(all approximations "p app s")
	 */
	public static double computeApproximationSolutionForSolution(Solution p, Solution s) {
		double delta = 0; // in order to maximize delta

		/* DV = decision variable, O = objective variable */
		double[] sOV = solutionObjectivesToDoubleArray(s);
		double[] pOV = solutionObjectivesToDoubleArray(p);


		double temp;

		// 2. compute the approximation of s by p in image (problem-specific)
		for (int i = 0; i < sOV.length; i++) {
			temp = pOV[i] - sOV[i];
			if (temp > delta) delta = temp;
		}

		return delta;
	}

	/** Returns the objective variables of a solution
	 * @param s
	 * @return
	 */
	public static double[] solutionObjectivesToDoubleArray(Solution s) {
		int numberOfObjectives = s.getNumberOfObjectives();
		double[] result = new double[numberOfObjectives];
		for (int j = 0; j < numberOfObjectives; j++) {
			result[j] = s.getObjective(j);
		}
		return result;
	}

	// the following is based on computeFitnesses/2
	/** Compute how well the population approximates the archive, see Karl's email from 07.12.2010
	 *
	 * @param population
	 * @param archive
//     * @return
	 */
	public void reducePopulationToSize(Population population, Population archive, int targetSize) {

		if (population.size()==1) return;

		// the following array stores the maximum approximations for which a population point is responsible
		int[] whichPopPointIsResponsible = new int[archive.size()];
		int[] whichPopPointIsResponsibleSecondBest = new int[archive.size()];

		Population archiveFront = archive;

		double[] results = new double[archiveFront.size()];

		/* store all the "pop app arc" in the following array:
		 * for each archive point it is stored how well a population point approximates it
		 */

		/* store all minimums of approximations per archive point in this array */
		double[] eps1 = new double[archiveFront.size()];
		double[] eps2 = new double[archiveFront.size()];

		// compute approximation for each non-dominated point of the archive
		// to find out how well it is approximated by the population points
		for (int i = 0; i < archiveFront.size(); i++) {
			Solution s = archiveFront.get(i);   // non-dominated archive-point

			double deltaForThisSolution = Double.MAX_VALUE; //minimize this value
			double deltaForThisSolutionSecondBest = Double.MAX_VALUE; //minimize this value
			for (int j = 0; j < population.size(); j++) {
				Solution p = population.get(j);

				// compute how an element p of the front(population) approximates an element s of the archive in domain and image
				double deltaForThisSolutionCurrent = computeApproximationSolutionForSolution(p, s); // population element p, archiveFront element s

				// if better then currentBest
				if (deltaForThisSolutionCurrent < deltaForThisSolution) {
					deltaForThisSolutionSecondBest = deltaForThisSolution;      // old best becomes secondBest
					deltaForThisSolution = deltaForThisSolutionCurrent;         // new best becomes best
					whichPopPointIsResponsibleSecondBest[i] = whichPopPointIsResponsible[i];
					whichPopPointIsResponsible[i] = j;
				} else {
					// if better then currentSecondBest
					if (deltaForThisSolutionCurrent < deltaForThisSolutionSecondBest) {
						deltaForThisSolutionSecondBest = deltaForThisSolutionCurrent;
						whichPopPointIsResponsibleSecondBest[i] = j;
					}
				}

			}

			// save the minimal approximation
			results[i] = deltaForThisSolution;
			eps1[i] = deltaForThisSolution;
			eps2[i] = deltaForThisSolutionSecondBest;
		}

		// keep track whether some point is still in the population
		boolean[] pIsInCurrentPop = new boolean[population.size()];
		for (int i = 0; i<pIsInCurrentPop.length; i++)
			pIsInCurrentPop[i] = true;


		//now determine the val(p)
		double[] val = new double[population.size()];
		double minVal = Double.MAX_VALUE;
		int minValIndex = 0;
		for (int i = 0; i<eps1.length; i++) {
			double eps2a = eps2[i];
			int p = whichPopPointIsResponsible[i];
			if (eps2a>val[p])
				val[p] = eps2a;            
		}

		for (int i=0; i<val.length; i++) {
			if (val[i]<minVal) {
				minVal = val[i];
				minValIndex = i;
			}
		}

		int popCounter=population.size();

		pIsInCurrentPop[minValIndex] = false;
		popCounter--;

		while (popCounter > targetSize) {
			//while (population.size() > targetSize) {
			HashSet whichEpsToUpdate = new HashSet();

			for (int i = 0; i<whichPopPointIsResponsible.length; i++) {
				if (minValIndex == whichPopPointIsResponsible[i]) whichEpsToUpdate.add(i);
				if (minValIndex == whichPopPointIsResponsibleSecondBest[i]) whichEpsToUpdate.add(i);
			}

			Iterator it = whichEpsToUpdate.iterator();
			while (it.hasNext()) {
				int i = (Integer)it.next();
			}

			it = whichEpsToUpdate.iterator();
			while (it.hasNext()) {
				int i = (Integer)it.next();

				Solution s = archiveFront.get(i);   // non-dominated archive-point

				double deltaForThisSolution = Double.MAX_VALUE; //minimize this value
				double deltaForThisSolutionSecondBest = Double.MAX_VALUE; //minimize this value
				for (int j = 0; j < population.size(); j++) {

					// skip those of the old population that are no longer in the current population
					if (!pIsInCurrentPop[j]) continue;

					Solution p = population.get(j);

					// compute how an element p of the front(population) approximates an element s of the archive in domain and image
					double deltaForThisSolutionCurrent = computeApproximationSolutionForSolution(p, s); // population element p, archiveFront element s

					// if better then currentBest
					if (deltaForThisSolutionCurrent < deltaForThisSolution) {
						deltaForThisSolutionSecondBest = deltaForThisSolution;      // old best becomes secondBest
						deltaForThisSolution = deltaForThisSolutionCurrent;         // new best becomes best
						whichPopPointIsResponsibleSecondBest[i] = whichPopPointIsResponsible[i];
						whichPopPointIsResponsible[i] = j;
					} else {
						// if better then currentSecondBest
						if (deltaForThisSolutionCurrent < deltaForThisSolutionSecondBest) {
							deltaForThisSolutionSecondBest = deltaForThisSolutionCurrent;
							whichPopPointIsResponsibleSecondBest[i] = j;
						}
					}

				}

				// save the minimal approximation
				results[i] = deltaForThisSolution;
				eps1[i] = deltaForThisSolution;
				eps2[i] = deltaForThisSolutionSecondBest;
			}

			//now determine the val(p)
			//        double[] val = new double[population.size()];
			minVal = Double.MAX_VALUE;
			minValIndex = 0;
			for (int i = 0; i<eps1.length; i++) {
				double eps2a = eps2[i];
				int p = whichPopPointIsResponsible[i];
				if (eps2a>val[p])
					val[p] = eps2a;
			}

			for (int i=0; i<val.length; i++) {
				if (pIsInCurrentPop[i])
					if (val[i]<minVal) {
						minVal = val[i];
						minValIndex = i;
					}

			}
			//        population.remove(minValIndex);
			popCounter--;
			pIsInCurrentPop[minValIndex] = false;
		}


		// form new population:
		for (int i = pIsInCurrentPop.length - 1; i>=0; i--) {
			if (!pIsInCurrentPop[i]) population.remove(i);
		}
	} //end
	
	private void setupArchive() {
		Population solutionSet = super.getPopulation();
		
		/* or: initialise with epsilonboxes, where a point is in the box */
		boolean useEpsilonBoxesArchive;

		if (epsilonGridWidth==0) useEpsilonBoxesArchive = false;
		else useEpsilonBoxesArchive = true;

		if (useEpsilonBoxesArchive) {
			for (int i = 0; i<this.idealPopulationSize; i++) {
				Solution converted = convertSolutionToEpsilonGridVectorFLOOR(solutionSet.get(i),epsilonGridWidth);
				archive.add(converted); 
			}
		} else {
			this.archive.addAll(solutionSet);
		}
		
	}
	
}