/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jmetal.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import jmetal.core.*;
import jmetal.metaheuristics.bfnw.BFNW;
import jmetal.qualityIndicator.*;

/**
 *
 * @author mwagner
 */
public class InfoPrinter1 {
    boolean firstPrintScreen = true;
    boolean firstPrintFile = true;
    File mpiClusterFile = null;
    File mpiClusterFileVAR = null;
    File mpiClusterFileFUN = null;
//    long lastPrintFile = 0;                                   // timestamp of last print(toFile/toScreen) in ms
//    long lastPrintScreen = 0;                                   // timestamp of last print(toFile/toScreen) in ms

//    int evExp = 3;    // if evals > 10^evExp print every 10^(evExp-1)
    int lastPrintedEvalsFile = 1000;
//    int lastPrintedEvalsScreen = 1000;
    int lastPrintedEvalsScreen = 100;

    public boolean waitedLongEnough(int ce, int lastEvals) {
//        int current10exp = (int)Math.floor(Math.log10(ce));
        int last10exp = (int)Math.floor(Math.log10(lastEvals));

//        if (current10exp == last10exp) {
        double stepsize = Math.pow(10, last10exp-1);
        if (ce >= lastEvals + stepsize) {
//            lastPrintedEvals = ce;
            return true;
        } else {
            return false;
        }

        
    }

//    public boolean waitedLongEnough(long now, long reference) {
////        if ( (now-reference)/1000.0 > (3 ) ) {
//        if ( (now-reference)/1000.0 > 3 + (Math.random()*6.0) ) {
////        if ( (now-reference)/1000.0 > 210 + (Math.random()*60.0) ) {             // if waited more than about 60s return true
////            System.out.println("waited long enough");
////            this.lastPrint = c;
//            return true;
//        }
//        return false;
//    }

    public InfoPrinter1(Algorithm algorithm, Problem problem, String subdir) {

        if (algorithm.getClass().getSimpleName().equalsIgnoreCase("NSGAII")
                || algorithm.getClass().getSimpleName().equalsIgnoreCase("SPEA2")
                || algorithm.getClass().getSimpleName().equalsIgnoreCase("IBEA")
                ) {
            this.lastPrintedEvalsFile = 10000;
            this.lastPrintedEvalsScreen = 10000;
        }

        if (algorithm.getClass().getSimpleName().equalsIgnoreCase("SMSEMOA") &&
                problem.getNumberOfObjectives() > 4) {
            this.lastPrintedEvalsFile = 100;
            this.lastPrintedEvalsScreen = 100;
        }

        System.out.println("InfoPrinter - printing at '0 evaluations' and then again at '"+
                this.lastPrintedEvalsFile+" evaluations' (stepsize: smaller order of magnitude)");

        File currentDir = new File(".");
        String base = "";
        try {
            base = currentDir.getCanonicalPath() + "/";
            base = base + subdir + "/";
        } catch (IOException ex) {
            System.out.println("exception: InfoPrinter() 1");
            Logger.getLogger(InfoPrinter1.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("InfoPrinter - current dir: "+base);
    
        // important: replace backslashes, as R complains about "escaped characters"
        base = base.replace("\\", "/");

        // create a unique directory name by adding a timestamp and the experiment's name
        base = base +
                problem.getClass().getSimpleName() + "-" +
                algorithm.getClass().getSimpleName() + "-" +
//                problem.getClass().getSimpleName() + "-" +
                System.currentTimeMillis();

        mpiClusterFile = new File(base+".log");
        mpiClusterFileVAR = new File(base+".VAR");
        mpiClusterFileFUN = new File(base+".FUN");
        try {
            System.out.println("InfoPrinter - current file:" + mpiClusterFile.getCanonicalPath() + " (created next time)");

            if (!mpiClusterFile.exists() && !mpiClusterFileVAR.exists() && !mpiClusterFileFUN.exists()) {
//                mpiClusterFile.createNewFile();
            } else {
                System.out.println("error: file exists already. EXIT");
                System.exit(0);
            }
            //        System.out.println("exp.experimentBaseDirectory_="+exp.experimentBaseDirectory_);
        } catch (IOException ex) {
            System.out.println("exception: InfoPrinter() 2");
            Logger.getLogger(InfoPrinter1.class.getName()).log(Level.SEVERE, null, ex);
        }


        
    }

    /** Prints a bunch of indicator values, based on the solutionSet and the archive.
     * @param evaluations
     * @param solutionSet
     * @param archive
     * @param indicators
     */
    public void printLotsOfValues(Algorithm algorithm, Problem problem,
            Map<String,Operator> operators, Map<String,Object> inputParameters,
            int evaluations, SolutionSet solutionSet, SolutionSet archive, QualityIndicator indicators, boolean doNotCalculateAnything) {
//System.out.println("printLotsOfValues"+doNotCalculateAnything);


        // if not waited long enough: early return
//        long c = System.currentTimeMillis();
//        if ( !waitedLongEnough(c, this.lastPrintScreen)  ) {
//            return;
//        } else {
//            this.lastPrintScreen = c;
//            System.out.println("screen");
//        }
        if (!waitedLongEnough(evaluations, lastPrintedEvalsScreen)) {
            return;
        } else {
            this.lastPrintedEvalsScreen = evaluations;
//            System.out.println("screen");
        }

        if (firstPrintScreen) {
//        if (evaluations== ((Integer)inputParameters.get("populationSize")).intValue()) {
            Operator crossoverOperator, mutationOperator, selectionOperator;
            crossoverOperator = operators.get("crossover");
            mutationOperator = operators.get("mutation");
            selectionOperator = operators.get("selection");
            System.out.println(
                DateUtils1.now() + "\n"
                + " " + algorithm.toString() + "\n"
                + " " + problem.toString() + " vars:"+problem.getNumberOfVariables() + " objs:" + problem.getNumberOfObjectives() + "\n"
                + " " + selectionOperator.toString());

            Object o = inputParameters.get("doCrossover");
            if (o==null) {
                System.out.println(" no flag \"doCrossover\" specified");
            } else {
                boolean b = ((Boolean)o).booleanValue();
                System.out.println((b?"+":"-") + crossoverOperator.toString());
            }

            o = inputParameters.get("doMutation");
            if (o==null) {
                System.out.println(" no flag \"doMutation\" specified");
            } else {
                boolean b = ((Boolean)o).booleanValue();
                System.out.println((b?"+":"-") + mutationOperator.toString());
            }
            
            firstPrintScreen = false;
        }
//System.out.println(doNotCalculateAnything);


        if (!doNotCalculateAnything) {

//            String print =
//    //        System.out.print(
//                    algorithm.getClass().getSimpleName() + " " +
//                    problem.getClass().getSimpleName() + " " +
//                    evaluations
//                    + " mu:" + solutionSet.size()
//                    + " arc:" + archive.size();
//    //        System.out.flush();
//    //        System.out.print(
//
//            print += " (HYPpop:" + indicators.getHypervolume(solutionSet);
//            print += ",HYPpopFPRAS:" + indicators.getHypervolumeFPRAS(solutionSet);
//    //        print += ",HYParc:" + indicators.getHypervolume(archive);
//            print += ",HYPreal:" + indicators.getTrueParetoFrontHypervolume()
//                    + ",EPSpop:" + indicators.getEpsilon(solutionSet)
//                    + ",SPREADpop:" + indicators.getSpread(solutionSet)
//                    + ",IGDpop:" + indicators.getIGD(solutionSet)
//                    + ",GDpop:" + indicators.getGD(solutionSet)
//                    + ")";
//            double approximation = BFNW.computeApproximation(solutionSet, archive)[0];
//            System.out.println(print + " approximation(pop app arc):" + approximation);
//            System.out.flush();

                        String print =
    //        System.out.print(
                    algorithm.getClass().getSimpleName() + " " +
                    problem.getClass().getSimpleName() + " " +
                    evaluations
                    + " mu:" + solutionSet.size();
//                    + " arc:" + archive.size();
    //        System.out.flush();
    //        System.out.print(

//            print += " (HYPpop:" + indicators.getHypervolume(solutionSet);
//            print += ",HYPpopFPRAS:" + indicators.getHypervolumeFPRAS(solutionSet);
    //        print += ",HYParc:" + indicators.getHypervolume(archive);
//            print += ",HYPreal:" + indicators.getTrueParetoFrontHypervolume()
                    print += " EPSpop:" + indicators.getEpsilon(solutionSet);
//                    + ",SPREADpop:" + indicators.getSpread(solutionSet)
//                    + ",IGDpop:" + indicators.getIGD(solutionSet)
//                    + ",GDpop:" + indicators.getGD(solutionSet)
//                    + ")";
//            double approximation = BFNW.computeApproximation(solutionSet, archive)[0];
//            System.out.println(print + " approximation(pop app arc):" + approximation);
            System.out.println(print);
            System.out.flush();


        } else {

            String print =
    //        System.out.print(
                    algorithm.getClass().getSimpleName() + " " +
                    problem.getClass().getSimpleName() + " " +
                    evaluations
                    + " mu:" + solutionSet.size()
                    + " arc:" + archive.size();
            System.out.println(print);
            System.out.flush();
        }
    }


    public void printLotsOfValuesToFile(Algorithm algorithm, Problem problem,
            Map<String,Operator> operators, Map<String,Object> inputParameters,
            int evaluations, SolutionSet solutionSet, SolutionSet archive, QualityIndicator indicators, boolean doNotCalculateAnything) {

//        System.out.println("printLotsOfValuesToFile"+doNotCalculateAnything);

//        System.out.println("printLotsOfValuesToFile");

        // if not waited long enough: early return
//        long c = System.currentTimeMillis();
//        if ( !waitedLongEnough(c, this.lastPrintFile)  ) {
//            return;
//        } else {
//            this.lastPrintFile = c;
//            System.out.println("file");
//        }

        if (firstPrintFile) {
            firstPrintFile = false;
        } else {
        
            if (!waitedLongEnough(evaluations, lastPrintedEvalsFile)) {
                return;
            } else {
                this.lastPrintedEvalsFile = evaluations;
    //            System.out.println("file");
            }

        }
 

        String print =
                problem.getClass().getSimpleName() + " " +
                algorithm.getClass().getSimpleName() + " " +
                evaluations
                + " mu:" + solutionSet.size()
                + " arc:" + archive.size();

        if (!doNotCalculateAnything) {
            print += " (HYPpop:" + indicators.getHypervolume(solutionSet);
            print += ",HYPreal:" + indicators.getTrueParetoFrontHypervolume()
                    + ",EPSpop:" + indicators.getEpsilon(solutionSet)
                    + ",SPREADpop:" + indicators.getSpread(solutionSet)
                    + ",IGDpop:" + indicators.getIGD(solutionSet)
                    + ",GDpop:" + indicators.getGD(solutionSet)
                    + ")";
            double approximation = BFNW.computeApproximation(solutionSet, archive)[0];
            print = print + " approximation(pop app arc):" + approximation;
        }


        try {
            //todo: write to file
//            System.out.println("INFOPRINTER1:"+mpiClusterFile.getCanonicalPath());

//            if (!mpiClusterFile.exists()) {
            mpiClusterFile.getParentFile().mkdir();

                mpiClusterFile.createNewFile();
                mpiClusterFileFUN.createNewFile();
                mpiClusterFileVAR.createNewFile();
//            } else {
//                System.out.println("error: file exists already. EXIT");
//                System.exit(0);
//            }
//            System.out.println("INFOPRINTER2:"+mpiClusterFile.getCanonicalPath());

            BufferedWriter out = new BufferedWriter(new FileWriter(mpiClusterFile, false));  // use false to NOT APPEND
            out.write(print + "\n");
            out.close();



//            logger_.info("Variables values have been writen to file VAR");
    solutionSet.printVariablesToFile(mpiClusterFileVAR.getCanonicalPath());
//    logger_.info("Objectives values have been writen to file FUN");
    solutionSet.printObjectivesToFile(mpiClusterFileFUN.getCanonicalPath());
        } catch (IOException ex) {
            System.out.println("exception: InfoPrinter.printLosOfValuesToFile");
            Logger.getLogger(InfoPrinter1.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.flush();
        
    }


}


class DateUtils1 {
  public static String now() {
    Calendar cal = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    return sdf.format(cal.getTime());
  }
}