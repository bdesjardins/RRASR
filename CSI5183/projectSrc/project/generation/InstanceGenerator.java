package project.generation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class InstanceGenerator {

	public static void main(String[] args){
		String directoryRoot = "C:/Users/ben/git/CSI5183_F2014/CSI5183/Instances";
		int areaSizeMin = -500;
		int areaSizeMax = 500;
		
		int sensingRadius = 20;
		int communicationRadius = 2*sensingRadius;
		
		/*Default Values:
		 * 
		 * Number of Nodes = 250
		 * % sensing holes = 0.15
		 * sparsity		   = 20
		*/
		int defaultNodes = 400;
		double defaultHoles = 0.15;
		int defaultSparsity = 3; //We don't care about sparsity by default
		
		int[] numberOfNodes = new int[]{10,15,20,25,30,35,40,45,50,60,70,80,90,100,110,120,130,140,150,160,170,180,190,200,225,250,275,300,
				325,350,375,400,425,450,475,500,525,550,575,600,625,650,675,700,750,800,850,900,950,1000}; //50 Entries
		
		
		//TESTING
//		try {
//			String directory = directoryRoot + "/Old";
//			generateInstance(areaSizeMin, areaSizeMax, defaultNodes, defaultSparsity, defaultHoles, directory, sensingRadius, communicationRadius);
//		} catch (FileNotFoundException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		//--------------------------------------------------
	
		String directory = directoryRoot + "/Nodes";
		for (int n = 0; n < numberOfNodes.length; n++) {
			try {
				generateInstance(areaSizeMin, areaSizeMax, numberOfNodes[n], defaultSparsity, defaultHoles, directory, sensingRadius, communicationRadius);
			} catch (FileNotFoundException e) {
				System.out.println("Directory not found");
				System.exit(0);
			}
		}
		
		directory = directoryRoot + "/Distribution";

		for (double n = 1; n <= 50; n++) {
			try {
				generateInstance(areaSizeMin, areaSizeMax, defaultNodes, defaultSparsity, n/100, directory, sensingRadius, communicationRadius);
			} catch (FileNotFoundException e) {
				System.out.println("Directory not found");
				System.exit(0);
			}
		}
		directory = directoryRoot + "/Sparsity";

		for (int n = 1; n <= 10; n++) {
			try {
				generateInstance(areaSizeMin, areaSizeMax, defaultNodes, n, defaultHoles, directory, sensingRadius, communicationRadius);
			} catch (FileNotFoundException e) {
				System.out.println("Directory not found");
				System.exit(0);
			}
		}

		System.out.println("Done!");		
	}

	private static void generateInstance(int areaSizeMin, int areaSizeMax, int n, int sparsity, double deliveryPerPickup, String directory, int sensingRadius, int communicationRadius) 
			throws FileNotFoundException{
		
		String fileName = directory + "/" + n + "n_" + sparsity + "s_" + (int) (deliveryPerPickup*100) + "d_instance.tsp";
		File instance = new File(fileName);

		PrintWriter writer = new PrintWriter(instance);

		writer.println("NAME: " + fileName);
		writer.println("COMMENT: Ben Desjardins (bdesj038@uottawa.ca)");
		writer.println("DIMENSION: " + n);
		writer.println("SPARCITY: " + sparsity);
		writer.println("%HOLES: " + deliveryPerPickup*100 + "%");
		writer.println("EDGE_WEIGHT_TYPE: EUC_2D");
		
		writer.println();
		writer.println();
		
		writer.println("NODE\tX_LOC\tY_LOC\tDEMAND\tBATTERY");
		writer.println("DATASTART");
		writer.println("0\t0\t0\t0\t0");
		
//		InstanceNode[] nodes = generateNodes2(areaSizeMin, areaSizeMax, n, deliveryPerPickup, sparcity);
		
		InstanceNode[] nodes = max_dpa(Math.round(Math.round(n - n*deliveryPerPickup))-1, sparsity, areaSizeMax, sparsity+3, 1, communicationRadius);
		
		for (int i = 0; i < nodes.length; i++) {
			writer.println(nodes[i].toString());
		}
		
		writer.println("EODATA");
		writer.println("NODE\tX_LOC\tY_LOC\tDEMAND\tDEGREE");
		writer.println("HOLESTART");
		
		InstanceNode[] holes = generateSensingHoles(areaSizeMin, areaSizeMax, Math.round(Math.round(n*deliveryPerPickup)), deliveryPerPickup, sensingRadius, communicationRadius);
		
		for (int i = 0; i < holes.length; i++) {
			writer.println(holes[i].toString());
		}
		writer.println("EOHOLE");
		writer.println("EOF");
		writer.close();
	}
	
	private static InstanceNode[] generateNodes(int areaSizeMin, int areaSizeMax, int n, double deliveryPerPickup) {
		InstanceNode[] nodes = new InstanceNode[n-1];
		
		int deliveries = (int) (n*deliveryPerPickup);
		int deliveryCounter = 0;
		
		for (int i = 0; i < nodes.length; i++) {
			int x = (int) (Math.random() * (areaSizeMax - areaSizeMin) + areaSizeMin);
			int y = (int) (Math.random() * (areaSizeMax - areaSizeMin) + areaSizeMin);
			
			InstanceNode newNode = new InstanceNode();
			newNode.x_coord = x;
			newNode.y_coord = y;
			newNode.node = i+1;
			newNode.demand = 1;
			
			newNode.battery = (int) Math.floor((Math.random()*100 + 1));
			
			nodes[i] = newNode;
		}
		
		while (deliveryCounter < deliveries) {
			int node = (int) (Math.random()*nodes.length);
			
			if (nodes[node].demand != -1) {
				if (Math.random() > 0.5) {
					nodes[node].demand = -1;
					nodes[node].battery = 0;
					deliveryCounter++;
				}
			}
		}
				
		return nodes;
	}
	
	private static InstanceNode[] generateNodes2(int areaSizeMin, int areaSizeMax, int n, double deliveryPerPickup, int sparsity) {		
		int pickups = Math.round(Math.round(n - n*deliveryPerPickup));
		
		InstanceNode[] nodes = new InstanceNode[pickups-1];
		
		//Pick a random drop location, if #inCommunicationDistance > sparsity, record#, record distance to closest other node. 
		//Repeat X times. If no suitable location has been found after X times, place at the best chosen location
		
		//Is this minimum sparsity, maximum sparsity, or absolute sparsity?
		//Is this the real graph, is this just sparsity
			
		for (int i = 0; i < nodes.length; i++) {
			int x = (int) (Math.random() * (areaSizeMax - areaSizeMin) + areaSizeMin);
			int y = (int) (Math.random() * (areaSizeMax - areaSizeMin) + areaSizeMin);
			
			InstanceNode newNode = new InstanceNode();
			newNode.x_coord = x;
			newNode.y_coord = y;
			newNode.node = i+1;
			newNode.demand = 1;
			
			newNode.battery = (int) Math.floor(((Math.random()*100 + 1)));
			
			nodes[i] = newNode;
		}
						
		return nodes;
	}
	
	private static InstanceNode[] generateSensingHoles(int areaSizeMin, int areaSizeMax, int numHoles, double deliveryPerPickup, int sensingRadius, int communicationRadius) {
		InstanceNode[] active = ActiveNetworkGenerator.gridGeneration(areaSizeMin, areaSizeMax, sensingRadius);
		
		HashMap<Integer, ArrayList<Integer>> adjacencyList = new HashMap<Integer, ArrayList<Integer>>();
		
		for (int i=0; i<active.length;i++) {
			ArrayList<Integer> temp = new ArrayList<Integer>();
			for (int j=0; j<active.length;j++) {
				if (i == j) {
					continue;
				}
				if (active[i].getDistance(active[j]) <= communicationRadius) {
					temp.add(j);
				}
			}
			adjacencyList.put(i, temp);
		}
		
		ArrayList<Integer> holeIndex = new ArrayList<Integer>();
		
		while (holeIndex.size() < numHoles) {
			int index = (int) (Math.random()*active.length);
			
			if (holeIndex.contains(index)) {
				continue;
			}
			
			boolean willDisconnect = false;
			for (int i = 0; i < active.length; i++) {
				if (i == index) {
					continue;
				}
				
				ArrayList<Integer> temp = adjacencyList.get(i);
				
				if (temp.contains(index) && temp.size() == 1) {
					willDisconnect = true;
					break;
				}
			}
			if (willDisconnect) {
				continue;
			}
			
			for (int i = 0; i < active.length; i++) {
				if (i == index) {
					continue;
				}
				
				adjacencyList.get(i).remove((Integer) index);
			}
						
			holeIndex.add(index);
		}
		
		ArrayList<InstanceNode> sensingHoles = new ArrayList<InstanceNode>();
		
		int holeCount = 0;
		
		while (holeIndex.size() > 0) {
			int index = holeIndex.remove(0);
			
			sensingHoles.add(new InstanceNode(holeCount++, active[index].x_coord, active[index].y_coord,-1, adjacencyList.get(index).size()));
		}
		
		return sensingHoles.toArray(new InstanceNode[0]);
	}

	private static InstanceNode[] max_dpa(int numNodes, int degree, int maxValue, int maxDegree, int minDistance, int communicationRadius) {
		double r = Math.sqrt((degree * Math.pow((maxValue*2), 2))/((numNodes-1)*Math.PI)) + 15;		
		boolean connected = false;
		
		InstanceNode[] nodes = new InstanceNode[0];
		
//		while (!connected) {
		int x = (int) ((Math.random() * (maxValue*2)) - maxValue);
		int y = (int) ((Math.random() * (maxValue*2)) - maxValue);

		ArrayList<InstanceNode> set = new ArrayList<InstanceNode>();

		InstanceNode p1 = new InstanceNode();
		p1.node = 1;
		p1.x_coord = x;
		p1.y_coord = y;
		p1.battery = 0; //Temporarily use to record degree
		p1.demand = 1;

		set.add(p1);

		for (int k = 2; k<=numNodes; k++) {
			boolean placed  = false;

			while (!placed) {
				x = (int) ((Math.random() * (maxValue*2)) - maxValue);
				y = (int) ((Math.random() * (maxValue*2)) - maxValue);

				InstanceNode newNode = new InstanceNode();
				newNode.node = k;
				newNode.x_coord = x;
				newNode.y_coord = y;
				newNode.demand = 1;
				newNode.battery = 0;

				ArrayList<Integer> degreeUp = new ArrayList<Integer>();

				boolean proximityTest = false;
				boolean degreeTest = true;
				for (int i = 0; i< set.size(); i++) {
					if (newNode.getDistance(set.get(i)) <= r && newNode.getDistance(set.get(i)) > minDistance) {
						proximityTest = true;
					}
					if (newNode.getDistance(set.get(i)) <= communicationRadius && set.get(i).battery < maxDegree) {
						if (set.get(i).battery < maxDegree) {
							degreeUp.add(i);
						} else {
							degreeTest = false;
							break;
						}
					} 
				}

				placed = proximityTest && degreeTest;

				if (placed) {
					for(int i = 0; i < degreeUp.size(); i++) {
						set.get(degreeUp.get(i)).battery++;
					}

					newNode.battery = degreeUp.size();
					set.add(newNode);
				}
			}
		}
		nodes = set.toArray(new InstanceNode[0]);
		//			connected = true;
		//			//Other stuff in the original algorithm goes here. Seeing how this performs without the check at the end
		//		}

		for (int i = 0; i < nodes.length; i++) {
			nodes[i].battery = (int) Math.floor(((Math.random()*100 + 1)));
		}
		
		return nodes;
	}
	
}
