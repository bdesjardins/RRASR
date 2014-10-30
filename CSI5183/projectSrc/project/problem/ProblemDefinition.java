package project.problem;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;

import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.Permutation;
import org.moeaframework.problem.AbstractProblem;

import project.problem.types.Coordinate;
import project.problem.types.NodeInfo;
import project.problem.types.SpecialPermutation;

public class ProblemDefinition extends AbstractProblem {

	final static Charset ENCODING = StandardCharsets.UTF_8;
	
//	private Coordinate[] activeNodes;
	private NodeInfo[] nodes;
	private NodeInfo depot;
	private ArrayList<Integer> sensingHoles;
		
	public ProblemDefinition() {
		super(1,3);
	}
	
	public ProblemDefinition(File nodeList/*, File activeNodeList*/) {
		//Populate the nodes and activeNodes arrays so we can evaluate the solutions
		//This will be done from generated instance files
		super(1,3);
		try {
			this.sensingHoles = new ArrayList<Integer>();
			
			Scanner scanner = new Scanner(nodeList,ENCODING.name());			
			while(!scanner.nextLine().equals("DATASTART")) {}
			
			//NODE	X_LOC	Y_LOC	DEMAND	BATTERY
			String[] nodeString = scanner.nextLine().split("\t");
			
			int demand = Integer.parseInt(nodeString[3]);
			double battery = Double.parseDouble(nodeString[4]);
			Coordinate location = new Coordinate(Double.parseDouble(nodeString[1]), Double.parseDouble(nodeString[2]));
			
		
			
			ArrayList<NodeInfo> tempNodes = new ArrayList<NodeInfo>();
			this.depot = new NodeInfo(demand, location, battery);
			tempNodes.add(this.depot);
			
			int nodeCounter = 1;
			
			while(!scanner.next().equals("EODATA")) {
				nodeString = scanner.nextLine().split("\t");
				
				demand = Integer.parseInt(nodeString[3]);
				battery = Double.parseDouble(nodeString[4]);
				location = new Coordinate(Double.parseDouble(nodeString[1]), Double.parseDouble(nodeString[2]));
				
				tempNodes.add(new NodeInfo(demand, location, battery));
				
				nodeCounter++;
				
				if (demand == -1) {
					this.sensingHoles.add(Integer.parseInt(nodeString[0]));
				}
			}
			
			while(!scanner.nextLine().equals("HOLESTART")) {}
			
			int degree = 0;
			while(!scanner.next().equals("EOHOLE")) {
				//NODE	X_LOC	Y_LOC	DEMAND	DEGREE
				nodeString = scanner.nextLine().split("\t");
				
				demand = Integer.parseInt(nodeString[3]);
				degree = Integer.parseInt(nodeString[4]);
				location = new Coordinate(Double.parseDouble(nodeString[1]), Double.parseDouble(nodeString[2]));
				
				tempNodes.add(new NodeInfo(demand, location, 0, degree));
				
				
				
				if (demand == -1) {
					this.sensingHoles.add(nodeCounter);
				}
				nodeCounter++;
			}
			
			scanner.close();
			
			nodes = tempNodes.toArray(new NodeInfo[0]);
			
		} catch (FileNotFoundException e) {
			System.out.println("Error generating problem from file");
			e.printStackTrace();
		}	
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
			if (vector.get(j).getDemand() == -1) {
				pathRobustness += batteries.pop()/vector.get(j).getDegree();
			}
			
			tourLength += vector.get(j).getLocation().distanceBetween( vector.get(j+1).getLocation());
		}
		
		solution.setObjective(2, tourLength);
		//MOEA only minimizes, therefore change maximization problems to minimization problems
		solution.setObjective(1, -Math.round(Math.round(pathRobustness))); 
		solution.setObjective(0, -Math.round(Math.round(pathLifetime)));		
	}

	@Override
	public Solution newSolution() {
		// TODO Auto-generated method stub
		Solution solution = new Solution(1,3);		
		Permutation newSolution = new Permutation(this.nodes.length, this.sensingHoles);		
		
		solution.setVariable(0, newSolution);
		
		return solution;
	}
	
}
