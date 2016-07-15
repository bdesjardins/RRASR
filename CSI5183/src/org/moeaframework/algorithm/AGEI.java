/**
 * BFNW.java (IBEA.java based)
 *
 *
 * @author Markus Wagner
 * @version 1.1
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
 * This class representing the AGEI algorithm
 * 
 * Ported to MOEA by Ben Desjardins
 */
public class AGEI extends AbstractEvolutionaryAlgorithm {
   
    /**
     * Defines the number of tournaments for creating the mating pool
     */
    public static final int TOURNAMENTS_ROUNDS = 1;
    /**
     * Stores the problem to solve
     */
//    private Problem problem_;
    
	Variation variation;
	Selection selection;
    int idealPopulationSize = -1;

    /**
     * Constructor.
     * Create a new BFNW instance
     * @param problem Problem to solve
     */
	public AGEI(Problem problem, NondominatedSortingPopulation population,
			NondominatedPopulation archive, Variation variation, Initialization initialization, Selection selection) {
		super(problem, population, archive, initialization);
		this.variation = variation;
		this.selection = selection;
	}
	
	@Override
	protected void iterate() {
		//Read the params
		Population solutionSet, offSpringSolutionSet;
		solutionSet = super.getPopulation();
		int populationSize = solutionSet.size();
		
        ParetoObjectiveComparator cNormal = new ParetoObjectiveComparator();
		
		if(this.idealPopulationSize == -1){
			this.idealPopulationSize = populationSize;
			setupArchive();
		}
		
		 offSpringSolutionSet = new Population();                          // generate mu solutions
         Population offSpringSolutionSetForArchive = new Population();    // generate mu solutions
         Solution[] parents = new Solution[2];
         Solution[] offSpring = null;

         for (int kk = 0; kk<idealPopulationSize; kk++){                     // loop condition: generate lambda inividuals

        	 //make the crossover and generate a single child
        	 parents = selection.select(variation.getArity(),population);
        	 offSpring = variation.evolve(parents);

        	 // FITNESS EVALUATION - note: this does not set fitness, just runs the problem functions
        	 super.evaluate(offSpring[0]);
//           problem_.evaluateConstraints(offSpring[0]);
        	 
        	 /* START check if new offSpring is not (epsilon) dominated by an archive point */
        	 boolean newPointIsDominatedByOldArchive = false;
        	 for (int i = 0; i<archive.size(); i++) {
        		 /*
        		  * COMPARE: return -1, or 0, or 1 if solution1 dominates solution2, both are
        		  *          non-dominated, or solution1 is dominated by solution2, respectively.
        		  */
        		 int result = cNormal.compare(archive.get(i), offSpring[0]);
        		 if (result==-1) {
        			 // break if an archive point dominates the new point
        			 newPointIsDominatedByOldArchive = true;
        			 break;
        		 }
        		 if (result==1) {
        			 // remove archive point if new point dominates that one
        			 archive.remove(i);
        			 i--;
        		 }
        	 }
        	 /* END check if new offSpring is not epsilon dominated by an archive point */


        	 // define behavior: add offspring to archive
        	 if (newPointIsDominatedByOldArchive) {
        		 // forget this point
        		 continue;
        	 } else {
        		 offSpringSolutionSet.add(offSpring[0]);
        		 offSpringSolutionSetForArchive.add(offSpring[0]);
        	 }

         }
         /*END generate lambda invididuals*/

           /* technically important: add all non-dominated points to the archive. */
//         archive = archive.union(offSpringSolutionSetForArchive);
           archive.addAll(offSpringSolutionSetForArchive);
           /* merge population with offSpringSolutionSet */
//         solutionSet = solutionSet.union(offSpringSolutionSet);              // would it be neccessary to take just the first subfront?
           solutionSet.addAll(offSpringSolutionSet);


         /* START select mu auf of mu+lambda */
         reducePopulationToSize(solutionSet, archive, populationSize);
         /* END select mu auf of mu+lambda */		
	}

	
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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

//        /* 1. compute the approximation of s by p in domain;
//         * additive:       temp = pDV[i]-sDV[i];
//         * multiplicative: temp = pDV[i]/sDV[i]; // problematic with negative numbers!
//         *
//         * additive approximation, based on Friedrich/Bringmann/Voß/Igel FOGA 2011
//         * -> for the two vectors p,s find the largest distance between bost
//         */
//        for (int i = 0; i < sDV.length; i++) {
//            temp = pDV[i] - sDV[i];
//            if (temp > delta) delta = temp;
//            if (debugPrint) System.out.print(temp + " ");
//        }

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

    /** Reduces the population down to targetSize
     *
     * @param population
     * @param archive
//     * @return
     */
    public void reducePopulationToSize(Population population, Population archive, int targetSize) {
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

//                approximations[i][j] = deltaForThisSolutionCurrent;

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
  
        int popCounter  =population.size();
        pIsInCurrentPop[minValIndex] = false;
        popCounter--;

        while (popCounter > targetSize) {
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
        	minVal = Double.MAX_VALUE;
        	minValIndex = 0;
        	for (int i = 0; i<eps1.length; i++) {
        		double eps2a = eps2[i];
        		int p = whichPopPointIsResponsible[i];
        		if (eps2a>val[p])
        			val[p] = eps2a;
        	}

        	for (int i=0; i<val.length; i++) {
        		if (pIsInCurrentPop[i]) {
        			if (val[i]<minVal) {
        				minVal = val[i];
        				minValIndex = i;
        			}
        		}	
        	}

        	popCounter--;
        	pIsInCurrentPop[minValIndex] = false;

        }

        // set fitness in this function as well
        //now determine the maximum approximation for which a popPoint is responsible:
        double[] maxAppForPopPoint = new double[population.size()];
        for (int i = 0; i<whichPopPointIsResponsible.length; i++) {
        	int popPointResponsible = whichPopPointIsResponsible[i];
        	if (pIsInCurrentPop[popPointResponsible]) {
        		if (maxAppForPopPoint[popPointResponsible] < results[i]) { // update if point is responsible for a "worse approximation"
        			maxAppForPopPoint[popPointResponsible] = results[i];

        			// set fitness here... 
//        			population.get(popPointResponsible).setFitness(Math.pow( 1d/results[i], this.problem.getNumberOfObjectives()  ) );
        		}
        	}
        }

        // form new population:
        for (int i = pIsInCurrentPop.length - 1; i>=0; i--) {
        	if (!pIsInCurrentPop[i]) population.remove(i);
        }
    }
	
	private void setupArchive() {
		Population solutionSet = super.getPopulation();
		this.archive.addAll(solutionSet);		
	}
}