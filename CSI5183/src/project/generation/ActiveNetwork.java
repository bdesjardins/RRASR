package project.generation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import agape.algos.Separators;
import agape.visu.Visualization;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

public class ActiveNetwork {

	InstanceNode[] nodes;
	ArrayList<ArrayList<Integer>> adjacencyList;
	
	int min;
	int max;
	int sensingRadius;
	int communicationRadius;
	
	public ActiveNetwork(int min, int max, int sensingRadius){
		this.min = min;
		this.max = max;
		this.sensingRadius = sensingRadius;
		this.communicationRadius = 2 * sensingRadius;
		
		this.nodes = gridGeneration(min, max, sensingRadius);
		buildAdjacencyList();
		findScores();
//		scoreTest();
	}
	
	private void scoreTest(){
		double[] scores = new double[nodes.length];
		double maxScore = 0;
		
		for(int i = 0; i < nodes.length; i++){
			scores[i] = nodes[i].score;
			if(scores[i] > maxScore){
				maxScore = scores[i];
			}
		}
		
//		Arrays.sort(scores);
		
		for(int i = scores.length-1; i>=0; i--){
			System.out.println(i + " " + scores[i]);
		}
		
		System.out.println("Max Score: " + maxScore);
	}
	
	//Basic non-random generation. Assumes some sort of grid deployment has taken place
	public static InstanceNode[] gridGeneration(int min, int max, int sensingRadius) {
		ArrayList<InstanceNode> activeNetwork = new ArrayList<InstanceNode>();
		
		//Add the base station
		activeNetwork.add(new InstanceNode(0,0,0,0,0));
		
		int count = 1;
		for (double x = min; x <= max; x+=(Math.sqrt(2)*sensingRadius)) {
			for (double y = min; y <= max; y+=(Math.sqrt(2)*sensingRadius)) {
				activeNetwork.add(new InstanceNode(count,x,y,0,1.0));
				count++;
			}
		}
		
		return activeNetwork.toArray(new InstanceNode[0]);
	}
	
	private void buildAdjacencyList(){
		this.adjacencyList = new ArrayList<ArrayList<Integer>>();

		for (int i=0; i<nodes.length;i++) {
			ArrayList<Integer> temp = new ArrayList<Integer>();
			for (int j=0; j<nodes.length;j++) {
				if (i == j) {
					continue;
				}
				if (nodes[i].getDistance(nodes[j]) <= communicationRadius) {
					temp.add(j);
				}
			}
			adjacencyList.add(i, temp);
		}
	}
	
	private void findScores(){
		Graph<Integer, Integer> graph = new UndirectedSparseGraph<Integer, Integer>();
		for(int i = 0; i < nodes.length; i++){
			graph.addVertex(i);
		}
				
		Integer edgeCount = 0;
		for (int i = 0; i < adjacencyList.size(); i++){
			ArrayList<Integer> edgeList = adjacencyList.get(i);
			
			while(edgeList.size() > 0) {
//			for(int j = 0; j < edgeList.size(); j++){
//				graph.addEdge(edgeCount, i, edgeList.get(j));
				graph.addEdge(edgeCount, i, edgeList.get(0));
				edgeCount++;
				
				adjacencyList.get(edgeList.get(0)).remove((Integer) i);
				adjacencyList.get(i).remove((Integer) edgeList.get(0));
				
//				adjacencyList.get(adjacencyList.get(edgeList.get(0))).remove((Integer) i);
//				adjacencyList.get(keySet[i]).remove((Integer) edgeList.get(0));				
			}
			
		}

//		Visualization.showGraph(graph);
		
		Integer baseStation = nodes[0].node;
		Separators<Integer, Integer> algorithm = new Separators<Integer, Integer>();
		
		//Create the scores!
		for(Integer i = 1; i < nodes.length; i++){
			Set<Set<Integer>> separators = algorithm.getABSeparators(graph, i, baseStation);
			Iterator<Set<Integer>> iter1 = separators.iterator();			
			
			while(iter1.hasNext()){
				Set<Integer> abSeparator = (Set<Integer>) iter1.next();
				Iterator<Integer> iter2 = abSeparator.iterator();
				
				int cardinality = abSeparator.size();
						
				while(iter2.hasNext()){					
					nodes[(Integer) iter2.next()].score += 1.0/cardinality;
					
				}
			}
		}	
	}
	
	public InstanceNode[] getSensingHoles(int numHoles){
		ArrayList<Integer> holeIndex = new ArrayList<Integer>();

		while (holeIndex.size() < numHoles) {
			int index = (int) (Math.random()*nodes.length);

			if (holeIndex.contains(index) || index==0) {
				continue;
			}

			boolean willDisconnect = false;
			for (int i = 0; i < nodes.length; i++) {
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

			for (int i = 0; i < nodes.length; i++) {
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

			sensingHoles.add(new InstanceNode(holeCount++, nodes[index].x_coord, nodes[index].y_coord,-1, nodes[index].score));
		}

		return sensingHoles.toArray(new InstanceNode[0]);
	}
	
	public InstanceNode[] getSensingHolesOld(int numHoles){
		ArrayList<Integer> holeIndex = new ArrayList<Integer>();

		while (holeIndex.size() < numHoles) {
			int index = (int) (Math.random()*nodes.length);

			if (holeIndex.contains(index)) {
				continue;
			}

			boolean willDisconnect = false;
			for (int i = 0; i < nodes.length; i++) {
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

			for (int i = 0; i < nodes.length; i++) {
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

			sensingHoles.add(new InstanceNode(holeCount++, nodes[index].x_coord, nodes[index].y_coord,-1, adjacencyList.get(index).size()*1.0));
		}

		return sensingHoles.toArray(new InstanceNode[0]);
	}
}
