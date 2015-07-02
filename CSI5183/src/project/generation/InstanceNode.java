package project.generation;

import java.text.DecimalFormat;

public class InstanceNode {
	public int node;
	public double x_coord;
	public double y_coord;
	public int demand;
	public int battery;
	
	public double score;
	
	private static final DecimalFormat df = new DecimalFormat("#.00");
	
	public InstanceNode(int node, int x, int y, int demand, int battery) {
		this.node = node;
		this.x_coord = x;
		this.y_coord = y;
		this.demand = demand;
		this.battery = battery;
		this.score = 1;
	}
	
	public InstanceNode(int node, double x, double y, int demand, int battery) {
		this.node = node;
		this.x_coord = x;
		this.y_coord = y;
		this.demand = demand;
		this.battery = battery;
		this.score = 1;
	}
	
	public InstanceNode(int node, int x, int y, int demand, double score) {
		this.node = node;
		this.x_coord = x;
		this.y_coord = y;
		this.demand = demand;
		this.battery = -1;
		this.score = score;
	}
	
	public InstanceNode(int node, double x, double y, int demand, double score) {
		this.node = node;
		this.x_coord = x;
		this.y_coord = y;
		this.demand = demand;
		this.battery = -1;
		this.score = score;
	}
	
	public InstanceNode() {
		this.node = 0;
		this.x_coord = 0;
		this.y_coord = 0;
		this.demand = 0;
		this.battery = 0;
		this.score = -1;
	}

	
	public String toString() {		
		return (node + "\t" + df.format(x_coord) + "\t" + df.format(y_coord) + "\t" + demand + "\t" + battery);
	}
	
	public String toStringHole(){		
		return (node + "\t" + df.format(x_coord) + "\t" + df.format(y_coord) + "\t" + demand + "\t" + df.format(score));
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
