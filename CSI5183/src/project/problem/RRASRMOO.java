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
import org.moeaframework.problem.CustomProblem;

import project.problem.types.Coordinate;
import project.problem.types.NodeInfo;

public class RRASRMOO extends CustomProblem {

	final static Charset ENCODING = StandardCharsets.UTF_8;
	
	protected NodeInfo[] nodes;
	protected ArrayList<Integer> sensingHoles;
	protected double[][] distances;
	
	protected int startingSensors = 0;
	protected int maxSensors = 3;
	
	protected int initCounter;
		
	public RRASRMOO() {
		super(1,3);
	}
	
	public RRASRMOO(File nodeList) {
		//Populate the nodes and activeNodes arrays so we can evaluate the solutions
		//This will be done from generated instance files
		super(1,3);
		this.initCounter = 0;
		
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
			
			double score = 0;	
			
			double scoreMax = 0;
			
			while(!scanner.next().equals("EOHOLE")) {
				//NODE	X_LOC	Y_LOC	DEMAND	DEGREE
				nodeString = scanner.nextLine().split("\t");
				
				demand = Integer.parseInt(nodeString[3]);
				score = Double.parseDouble(nodeString[4]);
				location = new Coordinate(Double.parseDouble(nodeString[1]), Double.parseDouble(nodeString[2]));
				
				tempNodes.add(new NodeInfo(demand, location, 0, score));
				
				if (score > scoreMax){
					scoreMax = score;
				}
				
						
				if (demand == -1) {
					this.sensingHoles.add(nodeCounter);
				}
				nodeCounter++;
			}			
			scanner.close();
			
			nodes = tempNodes.toArray(new NodeInfo[0]);
			
			//Normalize scores
			for(int i = 0; i < sensingHoles.size(); i++){
				double tempScore = nodes[sensingHoles.get(i)].getScore();
				
				//This is done due to a pipeline error and time constraints. Hard-coding is bad, kids.
				scoreMax = 192.62499999999775;
				nodes[sensingHoles.get(i)].setScore(tempScore/scoreMax);
			}
			
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
		
		repair(solution);
		
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
				pathRobustness += batteries.pop()*nodeA.getScore(); //New objective is a multiplier
			}
			
			tourLength += distances[vector.get(j)][vector.get(j+1)];
		}
		double roundpathRobustness = Math.round(pathRobustness * 1000.0) / 1000.0;
		
		solution.setObjective(2, Math.round(Math.round(tourLength)));
		//MOEA only minimizes, therefore change maximization problems to minimization problems
//		solution.setObjective(1, -Math.round(Math.round(pathRobustness))); 
		solution.setObjective(1, -roundpathRobustness); 
		solution.setObjective(0, -Math.round(Math.round(pathLifetime)));		
	}

	public void evaluateJmetal(jmetal.core.Solution solution) {
		ArrayList<Integer> vector = new ArrayList<Integer>();
		
		double tourLength = 0;
		double pathRobustness = 0;
		double pathLifetime = 100;
		
		int[] perm = ((jmetal.encodings.variable.Permutation) solution.getDecisionVariables()[0]).vector_;
		
		if (!Permutation.isPermutation(perm)) {
			perm = (new Permutation(perm)).toArray();
			jmetal.encodings.variable.Permutation sol = new jmetal.encodings.variable.Permutation(perm);
			solution.setDecisionVariables(new jmetal.core.Variable[]{ sol });
		}
		
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
			newSolution = new Permutation(this.nodes.length);
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
		default: newSolution = new Permutation(this.nodes.length); break;
		}		
		
		
		solution.setVariable(0, newSolution);		
		return solution;	
	}
	
	protected Permutation pathHeuristic() {
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
	
	protected Permutation robustnessHeuristic() {
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
	
	protected Permutation healthHeuristic() {
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
	
	protected Permutation buildCustomPermutation(ArrayList<Integer> vector) {
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

	protected void repair(Solution solution){
		Permutation variable = (Permutation) solution.getVariable(0);
		
		int pickups = startingSensors;
		int dropoffs = sensingHoles.size();
		
		int[] permutation = variable.toArray();
				
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

			if (permutation[i] > 0 && !sensingHoles.contains(Math.abs(permutation[i]))) {
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
				if (permutation[i] > 0 && !sensingHoles.contains(Math.abs(permutation[i]))) {					
					if (sensors >= this.maxSensors) {
						
						int nextHole = findNextHole(permutation, i);
						
						int temp = permutation[i];
						permutation[i] = permutation[nextHole];
						permutation[nextHole] = temp;
						
						i--;
						continue;
					} else {
						sensors++;
					}
				} else if (permutation[i] > 0 && sensingHoles.contains(Math.abs(permutation[i]))) {					
					if (sensors <= 0) {						
						int nextSensor = findNextSensor(permutation,i);
						
						int temp = permutation[i];
						permutation[i] = permutation[nextSensor];
						permutation[nextSensor] = temp;
						
						i--;
						continue;
					} else {
						sensors--;
					}
				}
			}			
			
			valid = this.isPermutation(permutation);
		}		
		variable.fromArray(permutation);
		solution.setVariable(0, variable);
	}
	
	protected int findNextHole(int[] permutation, int startingPoint) {
		for (int i = startingPoint; i < permutation.length; i++) {
			if (sensingHoles.contains(Math.abs(permutation[i]))) {
				return i;
			}
		}
		
		return -1;
	}
	
	protected int findNextSensor(int[] permutation, int startingPoint) {
		for (int i = startingPoint; i < permutation.length; i++) {
			if (permutation[i] > 0 && !sensingHoles.contains(Math.abs(permutation[i]))) {
				return i;
			}
		}
		
		return -1;
	}
	
	protected boolean isPermutation(int[] permutation) {				;
		int sensors = this.startingSensors;
		
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
}
