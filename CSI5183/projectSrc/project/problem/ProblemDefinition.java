package project.problem;

import java.io.File;
import java.util.ArrayList;
import java.util.Stack;

import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.Permutation;
import org.moeaframework.problem.AbstractProblem;

import project.problem.types.Coordinate;
import project.problem.types.NodeInfo;
import project.problem.types.SpecialPermutation;

public class ProblemDefinition extends AbstractProblem {

	private Coordinate[] activeNodes;
	private NodeInfo[] nodes;
	private NodeInfo depot;
	private ArrayList<Integer> sensingHoles;
		
	public ProblemDefinition() {
		super(1,3);
	}
	
	public ProblemDefinition(File nodeList, File activeNodeList) {
		//Populate the nodes and activeNodes arrays so we can evaluate the solutions
		//This will be done from generated instance files
		
		super(1,3);
	}

	@Override
	public void evaluate(Solution solution) {
		// TODO Auto-generated method stub
		ArrayList<NodeInfo> vector = new ArrayList<NodeInfo>();
		
		double tourLength = 0;
		double pathRobustness = 0;
		double pathLifetime = 100;
		
		int[] perm = ((Permutation) solution.getVariable(0)).toArray();
		
		vector.add(depot);
				
		for (int i = 0; i < perm.length; i++) {
			if (perm[i] > 0) {
				vector.add(nodes[perm[i]]);
			}
		}		
		vector.add(depot);
		
		Stack<Double> batteries = new Stack<Double>();
		
		for (int j = 0; j < vector.size() - 1; j++) {
			if (vector.get(j).getDemand() == 1) {
				batteries.push(vector.get(j).getBattery());
				
				if (batteries.peek() < pathLifetime) {
					pathLifetime = batteries.peek();
				}
			}
			if (vector.get(j).getDemand() == 1) {
				pathRobustness += batteries.pop()/vector.get(j).getDegree();
			}
			
			tourLength += vector.get(j).getLocation().distanceBetween( vector.get(j+1).getLocation());
		}
		
		solution.setObjective(0, tourLength);
		//MOEA only minimizes, therefore change maximization problems to minimization problems
		solution.setObjective(1, -pathRobustness); 
		solution.setObjective(2, -pathLifetime);		
	}

	@Override
	public Solution newSolution() {
		// TODO Auto-generated method stub
		Solution solution = new Solution(1,3);
		
		SpecialPermutation newSolution = new SpecialPermutation(nodes.length);
		
		int[] perm = newSolution.toArray();
		
		for (int i = 0; i <perm.length; i++) {
			if (!sensingHoles.contains(perm[i])) {
				if (Math.random() > 0.5) {
					perm[i] = -1 * perm[i];
				}
			}
		}
		
		newSolution.fromArray(perm);
		
		solution.setVariable(0, newSolution);
		
		return solution;
	}
	
}
