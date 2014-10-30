package project.generation;

public class InstanceNode {
	public int node;
	public int x_coord;
	public int y_coord;
	public int demand;
	public int battery;
	
	public InstanceNode(int node, int x, int y, int demand, int battery) {
		this.node = node;
		this.x_coord = x;
		this.y_coord = y;
		this.demand = demand;
		this.battery = battery;
	}
	
	public InstanceNode() {
		this.node = 0;
		this.x_coord = 0;
		this.y_coord = 0;
		this.demand = 0;
		this.battery = 0;
	}

	
	public String toString() {
		return (node + "\t" + x_coord + "\t" + y_coord + "\t" + demand + "\t" + battery);
	}
	
	public double getDistance(InstanceNode other) {
		double x1 = this.x_coord;
		double y1 = this.y_coord;
		
		double x2 = other.x_coord;
		double y2 = other.y_coord;
		
		double distance = Math.sqrt(Math.pow((x2-x1),2) + Math.pow((y2-y1),2));
		
		return distance;
	}
	
}
