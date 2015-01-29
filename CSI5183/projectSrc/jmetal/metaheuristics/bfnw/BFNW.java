/**
 * BFNW.java (IBEA.java based)
 *
 *
 * @author Markus Wagner
 * @version 1.1
 */
package jmetal.metaheuristics.bfnw;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

import jmetal.util.comparators.*;
import jmetal.core.*;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.*;

/**
 * This class representing the SPEA2 algorithm
 */
public class BFNW extends Algorithm {

    static boolean debugPrintGlobal = !true;
    boolean debugPrintBFNW = !true;
    
    public InfoPrinter1 infoPrinter = null;
    
    /**
     * Defines the number of tournaments for creating the mating pool
     */
    public static final int TOURNAMENTS_ROUNDS = 1;
    /**
     * Stores the problem to solve
     */
//    private Problem problem_;

    /**
     * Constructor.
     * Create a new BFNW instance
     * @param problem Problem to solve
     */
    public BFNW(Problem problem) {
    	super (problem);
//    	this.problem_ = problem;
    }

    /**
     * Runs the BFNW algorithm.
     * @return a <code>SolutionSet</code> that is a set of non dominated solutions
     * as a result of the algorithm execution
     * @throws JMException
     */
    public SolutionSet execute() throws JMException, ClassNotFoundException {

        QualityIndicator indicators = (QualityIndicator) getInputParameter("indicators"); // this line had to be added (mw)
        boolean doMutation = ((Boolean)getInputParameter("doMutation")).booleanValue();
        boolean doCrossover = ((Boolean)getInputParameter("doCrossover")).booleanValue();
        int infoPrinterHowOften;
        if (getInputParameter("infoPrinterHowOften")==null) infoPrinterHowOften=1000;
            else infoPrinterHowOften = ((Integer)getInputParameter("infoPrinterHowOften")).intValue();
        String infoPrinterSubDir = (String)getInputParameter("infoPrinterSubDir");
        boolean doOnMPICluster;
        if (getInputParameter("doOnMPICluster")==null) doOnMPICluster = false;
            else doOnMPICluster = ((Boolean) getInputParameter("doOnMPICluster")).booleanValue();

        int populationSize, archiveSize, maxEvaluations, evaluations;
        Operator crossoverOperator, mutationOperator, selectionOperator;
        SolutionSet solutionSet, archive, offSpringSolutionSet;

        //Read the params
        populationSize = ((Integer) getInputParameter("populationSize")).intValue();
        maxEvaluations = ((Integer) getInputParameter("maxEvaluations")).intValue();

        //Read the operators
        crossoverOperator = operators_.get("crossover");
        mutationOperator = operators_.get("mutation");
        selectionOperator = operators_.get("selection");

        //Initialize the variables
        solutionSet = new SolutionSet(populationSize);
        evaluations = 0;

        //-> Create the initial solutionSet
        Solution newSolution;
        for (int i = 0; i < populationSize; i++) {
            newSolution = new Solution(problem_);
            problem_.evaluate(newSolution);
            problem_.evaluateConstraints(newSolution);
            evaluations++;
            solutionSet.add(newSolution);
        }

        /* Initialize the archive with the solutionSet. In subsequent iterations, 
         * the newly constructed point will first be added to the archive, and then
         * the best mu points out of mu+1 are selected that approximate the new 
         * archive best.
         */
        archive = (SolutionSet)DeepCopy.copy(solutionSet);

        DominanceComparator cNormal = new DominanceComparator();


//        File mpiClusterFile = new File(".");


        // main loop starts...
        while (evaluations <= maxEvaluations) {

            /* START debug printouts */
            if (infoPrinter==null) infoPrinter = new InfoPrinter1(this, problem_, infoPrinterSubDir);
            if (doOnMPICluster) {
                infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators, true);
                infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators, true);
            } else {
                infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators, false);
                infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators, false);
            }
            if (evaluations>=maxEvaluations) break;
            /* END debug printouts */

            


            if (debugPrintBFNW && evaluations > populationSize) {
                double approximation = BFNW.computeApproximation(solutionSet, archive)[0];
                System.out.println("[eval:" + evaluations + "] " + "LOOP: OLD approximation " + approximation + " " + solutionSet
//                        + " (HYPmy:" + indicators.getHypervolume(solutionSet)
                        + ",HYPreal:" + indicators.getTrueParetoFrontHypervolume() + ")");
            }
            

            /* START bfnw block */
            if (debugPrintBFNW) {
                double approximation = BFNW.computeApproximation(solutionSet, archive)[0];
                System.out.println("BFNW: OLD approximation " + approximation + " " + solutionSet);
            }


            /* BFNW 1. step: generate one new solution */
            offSpringSolutionSet = new SolutionSet(populationSize);                          // generate mu solutions
            SolutionSet offSpringSolutionSetForArchive = new SolutionSet(populationSize);    // generate mu solutions
            Solution[] parents = new Solution[2];
            Solution[] offSpring = null;

              for (int kk = 0; kk<populationSize; kk++){                     // loop condition: generate lambda inividuals
     
                    int j = 0;
                    do {
                        j++;
                        parents[0] = (Solution) selectionOperator.execute(solutionSet); // carefull! the operator may work on the fitness values (which we did not really have in the beginning)
                    } while (j < BFNW.TOURNAMENTS_ROUNDS); // do-while                  // alternatively: choose a point at random and mutate that
                    int k = 0;
                    do {
                        k++;
                        parents[1] = (Solution) selectionOperator.execute(solutionSet);
                    } while (k < BFNW.TOURNAMENTS_ROUNDS); // do-while

     
                    //make the crossover and generate a single child
                    if (doCrossover) offSpring = (Solution [])crossoverOperator.execute(parents);    // 2 parents are XOed
                        else offSpring = parents;                                                    // no XO
                    if (doMutation) mutationOperator.execute(offSpring[0]);                          // mutation

                    // FITNESS EVALUATION - note: this does not set fitness, just runs the problem functions
                    problem_.evaluate(offSpring[0]);
                    problem_.evaluateConstraints(offSpring[0]);
                    evaluations++;



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
            archive = archive.union(offSpringSolutionSetForArchive);
            /* merge population with offSpringSolutionSet */
            solutionSet = solutionSet.union(offSpringSolutionSet);              // would it be neccessary to take just the first subfront?


            /* START select mu auf of mu+lambda */
            reducePopulationToSize(solutionSet, archive, populationSize);
            /* END select mu auf of mu+lambda */

            
        } // end of main loop

        // preparation of the result of the optimization process
        Ranking ranking = new Ranking(solutionSet);
        return ranking.getSubfront(0);
    } // execute

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



    /** Compute how well the population approximates the archive
     *
     * @param population
     * @param archive
     * @return a sorted double[] where the maximal approximation (best approximation of the
     *   worstly approximated archive point) is in the first field
     */
    public static double[] computeApproximation(SolutionSet population, SolutionSet archive) {
        boolean debugPrint = true && debugPrintGlobal;
        if (debugPrint) System.out.println("computeApproximation/2: ");

        SolutionSet archiveFront = null;

        archiveFront = archive;
        
        if (debugPrint) System.out.println("population.size=" + population.size() + " archive.size=" + archive.size() + " front.size=" + archiveFront.size());

        /* store all the "pop app arc" in the following array:
         * for each archive point it is stored how well a population point approximates it
         */

        /* store all minimums of approximations per archive point in this array */
        double[] results = new double[archiveFront.size()];

        // compute approximation for each non-dominated point of the archive
        // to find out how well it is approximated by the population points
        for (int i = 0; i < archiveFront.size(); i++) {
            Solution s = archiveFront.get(i);   // non-dominated archive-point

            double deltaForThisSolution = Double.MAX_VALUE; //minimize this value
            for (int j = 0; j < population.size(); j++) {
                Solution p = population.get(j);

                // compute how an element p of the front(population) approximates an element s of the archive in domain and image
                double deltaForThisSolutionCurrent = computeApproximationSolutionForSolution(p, s); // population element p, archiveFront element s


                if (deltaForThisSolutionCurrent < deltaForThisSolution) {
                    deltaForThisSolution = deltaForThisSolutionCurrent;
                }

            }

            // save the minimal approximation
            results[i] = deltaForThisSolution;

            if (debugPrint) {
                System.out.printf("-deltaForThisSolution: %8.5f ", deltaForThisSolution);
                System.out.println("(=" + deltaForThisSolution + ")"); // same output as line before, just more precise
            }
        }

        if (debugPrint) System.out.println("sorted deltas: " + Arrays.toString(results));

        Arrays.sort(results);        // biggest approximation is now at the end
        reverse(results);

        if (debugPrint) System.out.println("->deltaForThisSolutionSet(approximation): " + results[0]);

        return results;              // maximal approximation is now in results[0];
    }


    public static void reverse(double[] b) {
       int left  = 0;          // index of leftmost element
       int right = b.length-1; // index of rightmost element

       while (left < right) {
          // exchange the left and right elements
          double temp = b[left];
          b[left]  = b[right];
          b[right] = temp;

          // move the bounds toward the center
          left++;
          right--;
       }
    }



    /** Compute how well a solution p approximates a solution s. This is done by
     * determining the approximation for all decision variables and objective
     * variables and then taking the maximum of these approximations (i.e. the worst).
     * @param p
     * @param s
     * @return max(all approximations "p app s")
     */
    public static double computeApproximationSolutionForSolution(Solution p, Solution s) {
        boolean debugPrint = true && debugPrintGlobal;
      if (debugPrint) System.out.print("computeApproximationSolutionForSolution/2: ");

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
            if (debugPrint) System.out.printf(temp + " ");
        }

        if (debugPrint) System.out.print(" maxDelta:" + delta + "\n");
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
    public void reducePopulationToSize(SolutionSet population, SolutionSet archive, int targetSize) {
//        boolean debugPrint = true;
        boolean debugPrint = true && debugPrintGlobal;
        boolean debugPrintAdditional = false;
        if (debugPrint) System.out.println("computeFitnesses/2: ");

        // the following array stores the maximum approximations for which a population point is responsible
        int[] whichPopPointIsResponsible = new int[archive.size()];
        int[] whichPopPointIsResponsibleSecondBest = new int[archive.size()];

        SolutionSet archiveFront = archive;

        double[] results = new double[archiveFront.size()];

        if (debugPrint) System.out.println("population.size=" + population.size() + " archive.size=" + archive.size() + " front.size=" + archiveFront.size());

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

            if (debugPrint) {
                System.out.printf("-deltaForThisSolution: %8.5f ", deltaForThisSolution);
                System.out.println("(=" + deltaForThisSolution + ")"); // same output as line before, just more precise
            }
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
        if (debugPrintAdditional) System.out.println("popCounter"+popCounter);
        pIsInCurrentPop[minValIndex] = false;
        popCounter--;
        if (debugPrintAdditional) System.out.println("popCounter"+popCounter);


        if (debugPrintAdditional) if (true) {
            System.out.println("unsorted eps1:    " + Arrays.toString(eps1));
            System.out.println("responsible:      " + Arrays.toString(whichPopPointIsResponsible));
            System.out.println("unsorted eps2:    " + Arrays.toString(eps2));
            System.out.println("responsible:      " + Arrays.toString(whichPopPointIsResponsibleSecondBest));
            System.out.println("val        :      " + Arrays.toString(val));
            System.out.println("pIsInCurrentPop:  " + Arrays.toString(pIsInCurrentPop));
//            System.out.println("pIsP1a     :      " + Arrays.toString(pIsP1a));
            System.out.println("minVal="+minVal + " index=" +minValIndex + " population.size()now="+ (population.size()));
//            System.out.println("maxAppForPopPoint:" + Arrays.toString(maxAppForPopPoint));
        }




        while (popCounter > targetSize) {
            HashSet whichEpsToUpdate = new HashSet();

            for (int i = 0; i<whichPopPointIsResponsible.length; i++) {
                if (minValIndex == whichPopPointIsResponsible[i]) whichEpsToUpdate.add(i);
                if (minValIndex == whichPopPointIsResponsibleSecondBest[i]) whichEpsToUpdate.add(i);
            }


            Iterator it = whichEpsToUpdate.iterator();
            while (it.hasNext()) {
                int i = (Integer)it.next();
                if (debugPrintAdditional) System.out.print(i+" ");
            }
            if (debugPrintAdditional) System.out.println("");

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

            if (debugPrint) {
                System.out.printf("-deltaForThisSolution: %8.5f ", deltaForThisSolution);
                System.out.println("(=" + deltaForThisSolution + ")"); // same output as line before, just more precise
            }




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
            if (pIsInCurrentPop[i])
                if (val[i]<minVal) {
                        minVal = val[i];
                        minValIndex = i;
    //                }
                }

        }

        popCounter--;
        if (debugPrintAdditional) System.out.println("popCounter"+popCounter);
        pIsInCurrentPop[minValIndex] = false;

        if (debugPrintAdditional) if (true) {
            System.out.println("unsorted eps1:    " + Arrays.toString(eps1));
            System.out.println("responsible:      " + Arrays.toString(whichPopPointIsResponsible));
            System.out.println("unsorted eps2:    " + Arrays.toString(eps2));
            System.out.println("responsible:      " + Arrays.toString(whichPopPointIsResponsibleSecondBest));
            System.out.println("val        :      " + Arrays.toString(val));
            System.out.println("pIsInCurrentPop:  " + Arrays.toString(pIsInCurrentPop));
//            System.out.println("pIsP1a     :      " + Arrays.toString(pIsP1a));
            System.out.println("minVal="+minVal + " index=" +minValIndex + " population.size()now="+ (population.size()));
//            System.out.println("maxAppForPopPoint:" + Arrays.toString(maxAppForPopPoint));
        }
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
                            population.get(popPointResponsible).setFitness(Math.pow( 1d/results[i], this.problem_.getNumberOfObjectives()  ) );
                        }
                    }
                

        }



        // form new population:
        for (int i = pIsInCurrentPop.length - 1; i>=0; i--) {
            if (!pIsInCurrentPop[i]) population.remove(i);
            if (debugPrintAdditional) System.out.println("population.size()"+population.size());
        }

    }
}