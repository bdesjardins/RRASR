package project.generation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class InstanceGenerator {

	public static void main(String[] args){
		String directory = "C:/Users/ben/git/CSI5183_F2014/CSI5183/Instances";
		int areaSizeMin = -500;
		int areaSizeMax = 500;
		int maxCapacity = 3;
		int filesPerTuple = 10;
		double deliveryPerPickup = 0.25;

		int[] numberOfNodes = new int[]{20,30,40,50,60,100,200,300,400,500};

		for (int n = 0; n < numberOfNodes.length; n++) {
			for (int q = 1; q <= maxCapacity; q++) {
				for (int fileNo = 1; fileNo <= filesPerTuple; fileNo++) {
					try {
						generateInstance(areaSizeMin, areaSizeMax, numberOfNodes[n], q, fileNo, deliveryPerPickup, directory);
					} catch (FileNotFoundException e) {
						System.out.println("Directory not found");
						System.exit(0);
					}
				}
			}
		}

		System.out.println("Done!");		
	}

	private static void generateInstance(int areaSizeMin, int areaSizeMax, int n, int q, int fileNo, double deliveryPerPickup, String directory) 
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
		
		InstanceNode[] nodes = generateNodes(areaSizeMin, areaSizeMax, n, deliveryPerPickup);
		
		for (int i = 0; i < nodes.length; i++) {
			writer.println(nodes[i].toString());
		}
		
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
			
			newNode.battery = (int) (Math.random()*100);
			
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

}
