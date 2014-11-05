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
	
	private NodeInfo[] nodes;
	private ArrayList<Integer> sensingHoles;
	private double[][] distances;
	
	private static int initCounter = 0;
		
	public ProblemDefinition() {
		super(1,3);
	}
	
	public ProblemDefinition(File nodeList) {
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
			tempNodes.add(new NodeInfo(demand, location, battery));
			
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
			
			distances = new double[nodes.length][nodes.length];
			
			int start = 0;
			for (int i = 0; i < distances[0].length; i++) {
				for (int j = start++; j < distances[0].length; j++) {
					if (i == j) {
						distances[i][j] = 0;
						continue;
					}
					NodeInfo nodeA = nodes[i];					
					NodeInfo nodeB = nodes[j];
					
					double distance = nodeA.getLocation().distanceBetween(nodeB.getLocation());
					distances[i][j] = distance;
					distances[j][i] = distance;
				}
			}
			
		} catch (FileNotFoundException e) {
			System.out.println("Error generating problem from file");
			e.printStackTrace();
		}	
	}

	@Override
	public void evaluate(Solution solution) {
		ArrayList<Integer> vector = new ArrayList<Integer>();
		
		double tourLength = 0;
		double pathRobustness = 0;
		double pathLifetime = 100;
		
		int[] perm = ((Permutation) solution.getVariable(0)).toArray();
		
		vector.add(0);
				
		for (int i = 0; i < perm.length; i++) {
			if (perm[i] > 0) {
				vector.add(perm[i]);
			}
		}		
		vector.add(0);
		
		Stack<Double> batteries = new Stack<Double>();
		
		for (int j = 0; j < vector.size() - 1; j++) {
			NodeInfo nodeA = nodes[vector.get(j)];
			
			if (nodeA.getDemand() == 1) {
				batteries.push(nodeA.getBattery());
				
				if (batteries.peek() < pathLifetime) {
					pathLifetime = batteries.peek();
				}
			}
			if (nodes[vector.get(j)].getDemand() == -1) {
				pathRobustness += batteries.pop()/nodeA.getDegree();
			}
			
			tourLength += distances[vector.get(j)][vector.get(j+1)];
		}
		
		solution.setObjective(2, tourLength);
		//MOEA only minimizes, therefore change maximization problems to minimization problems
		solution.setObjective(1, -Math.round(Math.round(pathRobustness))); 
		solution.setObjective(0, -Math.round(Math.round(pathLifetime)));		
	}

	@Override
	public Solution newSolution() {
		Solution solution = new Solution(1,3);
		Permutation newSolution;
		
		switch (initCounter) {
		case 0:
			newSolution = new Permutation(this.nodes.length, this.sensingHoles);
			initCounter++;break;
		case 1:
			newSolution = healthHeuristic();
			initCounter++;break;
		case 2:
			newSolution = pathHeuristic();
			initCounter++;break;
		case 3:
			newSolution = robustnessHeuristic();
			initCounter++;break;
		default: newSolution = new Permutation(this.nodes.length, this.sensingHoles); break;
		}		
		
//		newSolution = new Permutation(this.nodes.length, this.sensingHoles);
		
		solution.setVariable(0, newSolution);		
		return solution;
	}
	
	private Permutation pathHeuristic() {
		ArrayList<Integer> path = new ArrayList<Integer>();
		int sensorCount = 0;
		int maxSensors = 2;
		int currentLocation = 0;
		
		while (path.size() < 2*sensingHoles.size()) {
			int bestNext = -1;
			if (sensorCount <= maxSensors && sensorCount != 0) { //Visit closest node
				for (int i = 0; i < nodes.length; i++) {
					if (currentLocation == i) {
						continue;
					}
					if (!path.contains(i) && (bestNext == -1 ||distances[currentLocation][i] < distances[currentLocation][bestNext])) {
						bestNext = i;
					}
				}
				path.add(bestNext);
			} else if (sensorCount == maxSensors) { //Visit nearest hole
				for (int i = 0; i < sensingHoles.size(); i++) {
					if (!path.contains(i) && (bestNext == -1 || distances[currentLocation][sensingHoles.get(i)] < distances[currentLocation][bestNext])) {
						bestNext = sensingHoles.get(i);
					}
				}
				path.add(bestNext);
			} else if (sensorCount == 0) { //visit nearest passive sensor
				for (int i = 0; i < nodes.length; i++) {
					if (currentLocation == i) {
						continue;
					}
					if (!path.contains(i) && !sensingHoles.contains(i) && (bestNext == -1 ||distances[currentLocation][i] < distances[currentLocation][bestNext])) {
						bestNext = i;
					}
				}
				path.add(bestNext);
			}
			currentLocation = bestNext;
		}
		
		return buildCustomPermutation(path);
	}
	
	private Permutation robustnessHeuristic() {
		ArrayList<Integer> path = new ArrayList<Integer>();
		int sensorCount = 0;

		int currentLocation = 0;
		
		while (path.size() < 2*sensingHoles.size()) {
			int bestNext = -1;
			if (sensorCount > 0) { //Visit lowest degree hole
				for (int i = 0; i < sensingHoles.size(); i++) {
					if (currentLocation == sensingHoles.get((Integer) i)) {
						continue;
					}
					if (!path.contains(i) && (bestNext == -1 ||nodes[sensingHoles.get(i)].getDegree() < nodes[bestNext].getDegree())) {
						bestNext = sensingHoles.get(i);
					}
				}
				path.add(bestNext);
			} else if (sensorCount == 0) { //visit best passive sensor
				for (int i = 0; i < nodes.length; i++) {
					if (currentLocation == i) {
						continue;
					}
					if (!path.contains(i) && !sensingHoles.contains(i) && (bestNext == -1 ||nodes[i].getBattery() > nodes[bestNext].getBattery())) {
						bestNext = i;
					}
				}
				path.add(bestNext);
			}
			currentLocation = bestNext;
		}
		
		return buildCustomPermutation(path);
	}
	
	private Permutation healthHeuristic() {
		ArrayList<Integer> path = new ArrayList<Integer>();
		int sensorCount = 0;
		int maxSensors = 2;
		int currentLocation = 0;
		
		while (path.size() < 2*sensingHoles.size()) {
			int bestNextBattery = -1;
			int bestNextHole = -1;
			if (sensorCount <= maxSensors && sensorCount != 0) { //Visit best battery or hole, whichever is closer
				for (int i = 0; i < nodes.length; i++) {
					if (currentLocation == i) {
						continue;
					}
					if (!path.contains(i) && sensingHoles.contains(i) && (bestNextHole == -1 ||distances[currentLocation][i] < distances[currentLocation][bestNextHole])) {
						bestNextHole = i;
					} else if (!path.contains(i) && (bestNextBattery == -1 ||nodes[i].getBattery() > nodes[bestNextBattery].getBattery())) {
						bestNextBattery = i;
					}
				}
				
				if (distances[currentLocation][bestNextBattery] < distances[currentLocation][bestNextHole]) {
					path.add(bestNextBattery);
					currentLocation = bestNextBattery;
				} else {
					path.add(bestNextBattery);
					currentLocation = bestNextHole;
				}				
			} else if (sensorCount == maxSensors) { //Visit nearest hole
				for (int i = 0; i < sensingHoles.size(); i++) {
					if (!path.contains(i) && (bestNextHole == -1 || distances[currentLocation][sensingHoles.get(i)] < distances[currentLocation][bestNextHole])) {
						bestNextHole = sensingHoles.get(i);
					}
				}
				path.add(bestNextHole);
				currentLocation = bestNextHole;
			} else if (sensorCount == 0) { //visit best passive sensor
				for (int i = 0; i < nodes.length; i++) {
					if (currentLocation == i) {
						continue;
					}
					if (!path.contains(i) && !sensingHoles.contains(i) && (bestNextBattery == -1 ||nodes[i].getBattery() < nodes[bestNextBattery].getBattery())) {
						bestNextBattery = i;
					}
				}
				path.add(bestNextBattery);
				currentLocation = bestNextBattery;
			}
		}
		
		return buildCustomPermutation(path);
	}
	
	private Permutation buildCustomPermutation(ArrayList<Integer> vector) {
		ArrayList<Integer> temp = new ArrayList<Integer>();
		int size = this.nodes.length;
		
		for (int i = 0; i < size; i++) {
			temp.add(i);
		}
		
		int[] permutation = new int[size];
		
		for (int i = 0; i < permutation.length && temp.size() > 0; i++){
			int next = (temp.remove((int) Math.floor(Math.random() * temp.size())));
			
			if (vector.contains(next)) {
				int trueNext = vector.remove(0);
				
				temp.add((Integer) next);
				temp.remove((Integer) trueNext);
				
				permutation[i] = trueNext;
			} else {
				permutation[i] = -next;
			}
		}
		
		return new Permutation(permutation);
	}
}
