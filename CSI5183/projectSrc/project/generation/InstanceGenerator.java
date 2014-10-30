package project.generation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class InstanceGenerator {

	public static void main(String[] args){
		String directory = "C:/Users/ben/git/CSI5183_F2014/CSI5183/Instances";
		int areaSizeMin = -500;
		int areaSizeMax = 500;
		int maxCapacity = 1;
		int filesPerTuple = 2;
		double deliveryPerPickup = 0.1;
		
		int sensingRadius = 50;
		int communicationRadius = 2*sensingRadius;

		int[] numberOfNodes = new int[]{20,30,40,50,60,100,200,300,400,500};

		for (int n = 0; n < numberOfNodes.length; n++) {
			for (int q = 1; q <= maxCapacity; q++) {
				for (int fileNo = 1; fileNo <= filesPerTuple; fileNo++) {
					try {
						generateInstance(areaSizeMin, areaSizeMax, numberOfNodes[n], q, fileNo, deliveryPerPickup, directory, sensingRadius, communicationRadius);
					} catch (FileNotFoundException e) {
						System.out.println("Directory not found");
						System.exit(0);
					}
				}
			}
		}

		System.out.println("Done!");		
	}

	private static void generateInstance(int areaSizeMin, int areaSizeMax, int n, int q, int fileNo, double deliveryPerPickup, String directory, int sensingRadius, int communicationRadius) 
			throws FileNotFoundException{
		
		String fileName = directory + "/" + n + "_" + q + "_instance" + fileNo + ".tsp";
		File instance = new File(fileName);

		PrintWriter writer = new PrintWriter(instance);

		writer.println("NAME: " + fileName);
		writer.println("COMMENT: Ben Desjardins (bdesj038@uottawa.ca)");
		writer.println("DIMENSION: " + n);
		writer.println("CAPACITY: " + q);
		writer.println("EDGE_WEIGHT_TYPE: EUC_2D");
		
		writer.println();
		writer.println();
		
		writer.println("NODE\tX_LOC\tY_LOC\tDEMAND\tBATTERY");
		writer.println("DATASTART");
		writer.println("0\t0\t0\t0\t0");
		
		InstanceNode[] nodes = generateNodes2(areaSizeMin, areaSizeMax, n, deliveryPerPickup);
		
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
	
	private static InstanceNode[] generateNodes2(int areaSizeMin, int areaSizeMax, int n, double deliveryPerPickup) {		
		int pickups = Math.round(Math.round(n - n*deliveryPerPickup));
		
		InstanceNode[] nodes = new InstanceNode[pickups-1];
		
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

}
