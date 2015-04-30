package project.problem.types;

public class NodeInfo {
	
	private int activeDegree;
	private int demand;
	
	private Coordinate location;
	private double battery;
	
	public NodeInfo(int demand, Coordinate location, double battery) {
		this.demand = demand;
		this.location = location;
		this.battery = battery;
		this.activeDegree = 0;
	}
	
	public NodeInfo(int demand, Coordinate location, double battery, int degree) {
		this.demand = demand;
		this.location = location;
		this.battery = battery;
		this.activeDegree=degree;
	}
	
	public int getDegree() {
		return this.activeDegree;
	}
	
	public void setDegree(int degree) {
		this.activeDegree = degree;
	}
	
	public int getDemand() {
		return this.demand;
	}
	
	public Coordinate getLocation() {
		return this.location;
	}
	
	public double getBattery() {
		return this.battery;
	}
}
