package org.moeaframework.algorithm;

import java.util.HashSet;
import java.util.Iterator;

import org.moeaframework.algorithm.util.Spea2Fitness;
import org.moeaframework.core.Initialization;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.NondominatedSortingPopulation;
import org.moeaframework.core.Population;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Selection;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;
import org.moeaframework.core.comparator.FitnessComparator;
import org.moeaframework.core.comparator.ParetoObjectiveComparator;
import org.moeaframework.core.operator.TournamentSelection;

public class Lifecycle extends AbstractEvolutionaryAlgorithm{
	
	private int algorithm = 1;
	
	NondominatedPopulation previousGeneration;
	
	int concurrentPoorPerformace = 0;

	//Algorithm specific parameters
	int idealPopulationSize = -1;	
	Variation variation;
	Selection selection;	
	private Population spea2Archive;
	private NondominatedPopulation ageIArchive;
	private ReferencePointNondominatedSortingPopulation NSGAIII_pop;
	private TournamentSelection spea2Selection;
	
	public Lifecycle(Problem problem, Population population,
			NondominatedPopulation archive, Variation variation, Initialization initialization, Selection selection) {
		super(problem, population, archive, initialization);
		
		this.variation = variation;
		this.selection = selection;
		
		this.previousGeneration = this.getResult();
		
		//Starting with NSGAIII
		NSGAIII_pop = new ReferencePointNondominatedSortingPopulation(problem.getNumberOfObjectives(), 4);
		NSGAIII_pop.addAll(getPopulation());
		
		spea2Selection = new TournamentSelection(2, new FitnessComparator());
	}


	@Override
	protected void iterate() {
		// TODO implement iterate methods for each algorithm
		switch (algorithm){
			case 1: iterateAGEI(); break;
			case 2: iterateNSGAIII(); break;
			case 3: iterateSPEA2(); break;		
		}
		
		if(!enoughImprovement()){
			this.concurrentPoorPerformace++;
			
			if (this.concurrentPoorPerformace == 5){
				switchAlgorithms();
				this.concurrentPoorPerformace = 0;
			}
		} else {
			this.concurrentPoorPerformace = 0;
		}
		
		//Each iterate should be one generation
//		if (genCounter >= popSize) {
//			if(!enoughImprovement()){
//				switchAlgorithms();
//			}
//			genCounter = 0;
//		} else {
//			genCounter++;
//		}
	}
	
	@Override
	public NondominatedPopulation getArchive() {
		return (NondominatedPopulation) super.getArchive();
	}

	@Override
	public NondominatedSortingPopulation getPopulation() {
		return (NondominatedSortingPopulation)super.getPopulation();
	}
	
	public boolean enoughImprovement(){
		
		NondominatedPopulation currentGeneration = null;
		
		if (algorithm == 1){
			currentGeneration = this.getResult();
		} else if (algorithm == 2){
			currentGeneration = new NondominatedPopulation(NSGAIII_pop);
		} else if (algorithm == 3){
//			currentGeneration = new NondominatedPopulation(spea2Archive);
			currentGeneration = this.getResult();
		}
		int counter = 0;		
		
		for(int i = 0; i < currentGeneration.size(); i++){
			for(int j = 0; j < previousGeneration.size(); j++){
				boolean same = true;
				for (int k = 0; k < 4; k++) {					
					if (currentGeneration.get(i).getObjective(k) != previousGeneration.get(j).getObjective(k)) {
						same = false;
					}
				}
				if (same) {
					counter++;
				}
			}
		}
		
		double ratio = (counter * 1.0) / previousGeneration.size();
		previousGeneration = currentGeneration;
		
		 //If more than 75% of the new generation is old, move to the next algorithm
		if (ratio > 0.9) { //This value likely needs to be played with
			return false;
		}
		
		return true;
	}

	private void switchAlgorithms(){
		if (algorithm == 2) { //NSGAIII to SPEA2
			NondominatedSortingPopulation population = getPopulation();
			population.truncate(0);
			population.addAll(NSGAIII_pop);
			
			this.spea2Archive = new Population();
			
			algorithm = 3;
			return;			
		} else if (algorithm == 3) { // SPEA2 to AGEI
			this.idealPopulationSize = -1;
			
			algorithm = 1;
			return;
		} else if (algorithm == 1) { // AGEI to NSGAIII
			int divisions = 4;
			
			NSGAIII_pop = new ReferencePointNondominatedSortingPopulation(problem.getNumberOfObjectives(), divisions);
			NSGAIII_pop.addAll(getPopulation());
			
			algorithm = 2;
			return;
		}		
	}
	
	////////////////////////////////////////////////////////////////////////////////////
	////////							AGEI
	////////////////////////////////////////////////////////////////////////////////////
	protected void iterateAGEI(){
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
//		           problem_.evaluateConstraints(offSpring[0]);
		        	 
		        	 /* START check if new offSpring is not (epsilon) dominated by an archive point */
		        	 boolean newPointIsDominatedByOldArchive = false;
		        	 for (int i = 0; i<ageIArchive.size(); i++) {
		        		 /*
		        		  * COMPARE: return -1, or 0, or 1 if solution1 dominates solution2, both are
		        		  *          non-dominated, or solution1 is dominated by solution2, respectively.
		        		  */
		        		 int result = cNormal.compare(ageIArchive.get(i), offSpring[0]);
		        		 if (result==-1) {
		        			 // break if an archive point dominates the new point
		        			 newPointIsDominatedByOldArchive = true;
		        			 break;
		        		 }
		        		 if (result==1) {
		        			 // remove archive point if new point dominates that one
		        			 ageIArchive.remove(i);
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
//		         ageIArchive = archive.union(offSpringSolutionSetForArchive);
		         ageIArchive.addAll(offSpringSolutionSetForArchive);
		           /* merge population with offSpringSolutionSet */
//		         solutionSet = solutionSet.union(offSpringSolutionSet);              // would it be neccessary to take just the first subfront?
		           solutionSet.addAll(offSpringSolutionSet);


		         /* START select mu auf of mu+lambda */
		         reducePopulationToSize(solutionSet, ageIArchive, populationSize);
		         /* END select mu auf of mu+lambda */		
	}
	
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
                double deltaForThisSolutionCurrent = AGEI.computeApproximationSolutionForSolution(p, s); // population element p, archiveFront element s

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
        			double deltaForThisSolutionCurrent = AGEI.computeApproximationSolutionForSolution(p, s); // population element p, archiveFront element s

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
		ageIArchive = new NondominatedPopulation();	
		ageIArchive.addAll(solutionSet);
	}
	
	////////////////////////////////////////////////////////////////////////////////////
	////////							NSGAIII
	////////////////////////////////////////////////////////////////////////////////////
	
	public void iterateNSGAIII() {
		NondominatedSortingPopulation population = NSGAIII_pop;
		Population offspring = new Population();
		int populationSize = population.size();

		while (offspring.size() < populationSize) {
			Solution[] parents = selection.select(variation.getArity(),
					population);
			Solution[] children = variation.evolve(parents);

			offspring.addAll(children);
		}

		evaluateAll(offspring);

		population.addAll(offspring);
		population.truncate(populationSize);
	}
	
	////////////////////////////////////////////////////////////////////////////////////
	////////							SPEA2
	////////////////////////////////////////////////////////////////////////////////////
	protected void iterateSPEA2() {
		Population population = getPopulation();
		
		int populationSize = population.size();		
		int archiveSize = (int) Math.round(population.size()*0.5);
				
		Population union = new Population(population);
		union.addAll(spea2Archive);
		Spea2Fitness spea = new Spea2Fitness(union);
		spea.fitnessAssign();
		spea2Archive = spea.environmentalSelection(archiveSize); 
		
		// Create a new offspringPopulation
		Population offSpringSolutionSet= new Population();    		
		while (offSpringSolutionSet.size() < populationSize) {
			Solution[] parents = spea2Selection.select(variation.getArity(),spea2Archive);
			Solution[] children = variation.evolve(parents);

			offSpringSolutionSet.addAll(children);
		}

		// End Create a offSpring solutionSet
		evaluateAll(offSpringSolutionSet);
		
		population.clear();
		population.addAll(offSpringSolutionSet);                  		
	}
}
