package project.generation;

import java.util.ArrayList;

public class ActiveNetworkGenerator {

	//Basic non-random generation. Assumes some sort of grid deployment has taken place
	public static InstanceNode[] gridGeneration(int min, int max, int sensingRadius) {
		ArrayList<InstanceNode> activeNetwork = new ArrayList<InstanceNode>();
		
		
		int count = 0;
		
		for (int x = min; x <= max; x+=sensingRadius) {
			for (int y = min; y <= max; y+=sensingRadius) {
				activeNetwork.add(new InstanceNode(count,x,y,0,0));
				count++;
			}
		}
		
		return activeNetwork.toArray(new InstanceNode[0]);
	}
}
