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

/**
 * 
 * @author bdesjardins
 *
 * Implementation of the Reliable Multiple Robot-Assisted Sensor Relocation problem
 */
public class RMRASR extends CustomProblem {

	final static Charset ENCODING = StandardCharsets.UTF_8;
	
	protected NodeInfo[] nodes;
	protected ArrayList<Integer> sensingHoles;
	
	//Store this so we only have to calculate distances once
	protected double[][] distances;
	
	protected int startingSensors = 0;
	protected int maxSensors = 3;
	
	protected int initCounter;
	
	public static long repairTime_total = 0;
	public static long repairTime_balance = 0;
	public static long repairTime_order = 0;
	
	protected int numberOfRobots;
	
	protected ArrayList<Integer> duplicateDepots;
	
	/**
	 * Create a simple RMRASR instance
	 */
	public RMRASR() {
		super(1,4);
	}
	
	/**
	 * Creates a RMRASR instance using a nodeList (instance file)
	 * Defaults to 3 robots
	 * @param nodeList Instance file
	 */
	public RMRASR(File nodeList){
		this(nodeList,3);			
	}
	
	/**
	 * Creates a RMRASR instance using a nodeList (instance file)
	 *
	 * @param nodeList Instance file
	 * @param numberOfRobots The number of robots to use
	 */
	public RMRASR (File nodeList, int numberOfRobots){		
		super(1,4);	
		this.numberOfRobots = numberOfRobots;
		
		this.initCounter = 0;		
		
		try {
			this.duplicateDepots = new ArrayList<Integer>();
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
			
			//Add in duplicate depots
			if (this.numberOfRobots > sensingHoles.size()){
				this.numberOfRobots = sensingHoles.size();
			}
			
			for(int i = 0; i < this.numberOfRobots-1; i++){
				tempNodes.add(new NodeInfo(0, new Coordinate(0,0),0,0));
				this.duplicateDepots.add(nodeCounter);
				nodeCounter++;
			}
			
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

	/**
	 * Evaluates the fitness values of a given solution
	 */
	@Override //- Original evaluate function may suffice. We shall see
	public void evaluate(Solution solution){		
		
		ArrayList<ArrayList<Integer>> vectors = new ArrayList<ArrayList<Integer>>();

		double totalLength = 0;
		double pathRobustness = 0;
		double pathLifetime = 100;
		double loadBalance = 0;

		repair(solution);

		int[] perm = ((Permutation) solution.getVariable(0)).toArray();
		
		ArrayList<Integer> vector = new ArrayList<Integer>();
		vector.add(0);

		for (int i = 0; i < perm.length; i++) {
			if (duplicateDepots.contains(perm[i])){
				vector.add(0);
				vectors.add(vector);
				
				vector = new ArrayList<Integer>();
				vector.add(0);
				
				continue;
			}
			
			if (perm[i] > 0) {
				vector.add(perm[i]);
			}
		}		
		vector.add(0);
		vectors.add(vector);

		ArrayList<Double> vectorLoads = new ArrayList<Double>();
		
		for(int i = 0; i < vectors.size(); i++) {
			Stack<Double> batteries = new Stack<Double>();

			ArrayList<Integer> pathVector = vectors.get(i);
			double tourLength = 0;
			for (int j = 0; j < pathVector.size() - 1; j++) {
				NodeInfo nodeA = nodes[pathVector.get(j)];
				

				if (nodeA.getDemand() == 1) {
					batteries.push(nodeA.getBattery());

					if (batteries.peek() < pathLifetime) {
						pathLifetime = batteries.peek();
					}
				}
				if (nodes[pathVector.get(j)].getDemand() == -1) {
					pathRobustness += batteries.pop()*nodeA.getScore(); //New objective is a multiplier
				}
				tourLength += distances[pathVector.get(j)][pathVector.get(j+1)];
				totalLength += distances[pathVector.get(j)][pathVector.get(j+1)];
			}
			vectorLoads.add(tourLength);
		}
		double roundpathRobustness = Math.round(pathRobustness * 10.0) / 10.0;

		for(double tour: vectorLoads){
			double load = Math.round((tour/totalLength)*100) / 100.0;
			
			if(load > loadBalance){
				loadBalance = load;
			}
		}
		
		solution.setObjective(2, Math.round(Math.round(totalLength)));
		//MOEA only minimizes, therefore change maximization problems to minimization problems
//		solution.setObjective(1, -Math.round(Math.round(pathRobustness))); 
		solution.setObjective(1, -roundpathRobustness); 
		solution.setObjective(0, -Math.round(Math.round(pathLifetime)));
		
		solution.setObjective(3, loadBalance*100);
	}

	/**
	 * Creates a new randomly initialized solution
	 * 
	 * @return A new solution
	 */
	@Override //TODO newSolution
	public Solution newSolution() {
		Solution solution = new Solution(1,4);
		
		solution.setVariable(0, new Permutation(this.nodes.length));		
		return solution;
	}

	//Due to the amount of reordering occurring it may be better to 
	//re-implement this using linked-lists instead
	@Override
	protected void repair(Solution solution){
		long startTime = System.currentTimeMillis();
		
		Permutation variable = (Permutation) solution.getVariable(0);		
		int[] permutation = balancePermutation(variable.toArray());		
			
		//Fix the ordering as necessary - ArrayList Version v2
		ArrayList<Integer> locations = new ArrayList<Integer>();
		ArrayList<Integer> vector = new ArrayList<Integer>();
		for (int i = 0; i < permutation.length; i++){
			if(permutation[i] > 0) {
				vector.add(permutation[i]);
				locations.add(i);
			}
		}
		vector = orderVector(vector);
		
		for (int i = 0; i < locations.size(); i++) {		
			permutation[locations.get(i)] = vector.get(i);
		}	
		variable.fromArray(permutation);
		solution.setVariable(0, variable);
		
		RMRASR.repairTime_total += (System.currentTimeMillis() - startTime);		
	}
	
	protected boolean isPermutation(int[] permutation) {				
		int sensors = this.startingSensors;
		
		for (int i = 0; i < permutation.length; i++) {
			for (int j = i+1; j < permutation.length; j++) {
				if (permutation[j] == permutation[i]) {
					return false;
				}
			}
			if (permutation[i] > 0 && !sensingHoles.contains(permutation[i]) && !duplicateDepots.contains(permutation[i])) {
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


	private int[] balancePermutation(int[] permutation){	
		long startTime = -System.currentTimeMillis();
		int pickups = startingSensors;
		int dropoffs = sensingHoles.size();
		
		//Make sure that all dropoffs and depots are included
		for (int i = 0; i < permutation.length; i++) {
			if (sensingHoles.contains(Math.abs(permutation[i])) && permutation[i] < 0) {
				permutation[i] = -permutation[i];
			} else if (duplicateDepots.contains(Math.abs(permutation[i])) && permutation[i] < 0) { //NEW
				permutation[i] = -permutation[i];
			} else if (permutation[i] > 0 && (!sensingHoles.contains(Math.abs(permutation[i])) && !duplicateDepots.contains(Math.abs(permutation[i])))) {
				pickups++;
			}
		}

		//If there are too many pickups, remove pickups from right to left until they balance with the dropoffs
//		if (pickups > dropoffs)
		for (int i = permutation.length-1; i >= 0; i--) {
			if (pickups <= dropoffs) {
				break;
			}

			if (permutation[i] > 0 && !sensingHoles.contains(Math.abs(permutation[i])) && !duplicateDepots.contains(Math.abs(permutation[i]))) {
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

		RMRASR.repairTime_balance += (startTime + System.currentTimeMillis());
		return permutation;
	}
	
	private ArrayList<Integer> orderVector(ArrayList<Integer> vector){
		long startTime = -System.currentTimeMillis();
		
		boolean valid = false;	
		while (!valid) {						
			//Make sure there is at least one hole between each depot
			boolean delivery = false;
			for (int i = 0; i < vector.size(); i++){				
				if(duplicateDepots.contains(vector.get(i))){
					if (i == vector.size()-1) {
						vector.add(0, vector.remove(i));
						i = -1;
						delivery = false;
					} else if (delivery == false){
						int holeLoc = findNextHole(vector, i);
						
						if (holeLoc == -1) { //Need to set i = -1 in this case
							vector.add(0, vector.remove(i));
							i = -1;
							delivery = false;
							continue;
						}
						
						int holeValue = vector.remove(holeLoc);
						vector.add(i, holeValue);
						i--;
					}
					else {
						delivery = false;
					}
				} else if(sensingHoles.contains(vector.get(i))) {
					delivery = true;
				}
			}
									
			//Balance ordering by moving the pickups around until properly balanced
			int sensors = startingSensors;
			for(int i = 0; i < vector.size(); i++){
				 //If a sensor	
				if (!sensingHoles.contains(vector.get(i)) && !duplicateDepots.contains(vector.get(i))) {				
					if (sensors >= this.maxSensors) {
						int nextHole = findNextHole(vector, i);
						
						vector.add(nextHole, vector.remove(i));
						i--;
					} else {
						sensors++;
					}
				} else if(duplicateDepots.contains(vector.get(i))){ //TODO try and avoid setting i = -1
					if (sensors > 0) {
						int lastSensor = findLastSensor(vector, i);							
						int nextHole = findNextHole(vector, i);
						
						vector.add(nextHole-1, vector.remove(lastSensor));
						i = -1;
						sensors = 0;
					}
				} else if(sensingHoles.contains(vector.get(i))){
					if (sensors > 0) {
						sensors--;
					} else {
						int nextSensor = findNextSensor(vector, i);
						
						vector.add(i, vector.remove(nextSensor));
						i--;
					}
				}
			}			
			try {
				valid = this.isValidVector(vector);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		RMRASR.repairTime_order += (startTime + System.currentTimeMillis());
		return vector;
	}
	
	protected int findNextHole(ArrayList<Integer> permutation, int startingPoint) {
		for (int i = startingPoint; i < permutation.size(); i++) {
			if (sensingHoles.contains(permutation.get(i))) {
				return i;
			}
		}
		return -1;
	}


	protected int findNextSensor(ArrayList<Integer> permutation, int startingPoint) {
		for (int i = startingPoint; i < permutation.size(); i++) {
			if (!sensingHoles.contains(permutation.get(i)) && !duplicateDepots.contains(permutation.get(i))) {
				return i;
			}
		}
		return -1;
	}
	
	
	protected int findLastSensor(ArrayList<Integer> permutation, int startingPoint) {
		for (int i = startingPoint; i >= 0; i--) {
			if (!sensingHoles.contains(permutation.get(i)) && !duplicateDepots.contains(permutation.get(i))) {
				return i;
			}
		}
		return -1;
	}


	protected boolean isPermutation(ArrayList<Integer> permutation) {
		int sensors = this.startingSensors;
		boolean delivery = false;

		for (int i = 0; i < permutation.size(); i++) {
			for (int j = i+1; j < permutation.size(); j++) {
				if (permutation.get(j) == permutation.get(i)) {
					return false;
				}
			}
		}
		
		for (int i = 0; i < permutation.size(); i++) {
			if (permutation.get(i) > 0 && !sensingHoles.contains(permutation.get(i)) && !duplicateDepots.contains(permutation.get(i))) {
				sensors++;				
				if (sensors > maxSensors) {
					return false;
				}
			} else if (permutation.get(i) > 0 && sensingHoles.contains(permutation.get(i))) {
				sensors--;

				if (sensors < 0) {
					return false;
				}
				delivery = true;

			} else if (permutation.get(i) > 0 && duplicateDepots.contains(permutation.get(i))){
				if (sensors > 0 || !delivery) {
					return false;
				}
				delivery = false;
			}
		}

		if (sensors == 0 && delivery == true) {
			return true;
		} else {
			return false;
		}
	}

	private boolean isValidVector(ArrayList<Integer> vector) throws Exception{
		int sensors = this.startingSensors;
		boolean delivery = false;

		for (int i = 0; i < vector.size(); i++) {
			for (int j = i+1; j < vector.size(); j++) {
				if (vector.get(j) == vector.get(i)) {
					throw new Exception("Permutation is broken - repair impossible");
				}
			}
		}
		
		for (int i = 0; i < vector.size(); i++) {
			if (!sensingHoles.contains(vector.get(i)) && !duplicateDepots.contains(vector.get(i))) {
				sensors++;				
				if (sensors > maxSensors) {
					return false;
				}
			} else if (sensingHoles.contains(vector.get(i))) {
				sensors--;

				if (sensors < 0) {
					return false;
				}
				delivery = true;

			} else if (duplicateDepots.contains(vector.get(i))){
				if (sensors > 0 || !delivery) {
					return false;
				}
				delivery = false;
			}
		}

		if (sensors == 0 && delivery == true) {
			return true;
		} else {
			return false;
		}
	}

}


