package project.problem.types;

public class NodeInfo {
	
	private int activeDegree;
	private int demand;
	
	private Coordinate location;
	private double battery;
	
	//TODO set communication radius
	private static double communicationRadius = 0;
	
	public NodeInfo(int demand, Coordinate location, double battery, Coordinate[] activeNodes) {
		this.demand = demand;
		this.location = location;
		this.battery = battery;
		this.activeDegree = 0;
			
		if (this.demand == -1) {
			int degree = 0;
		
			for (int i = 0; i < activeNodes.length; i++) {
				if (this.location.distanceBetween(activeNodes[i]) <= this.communicationRadius) {
					degree++;
				}			
			}
			
			this.activeDegree=degree;
		}
	}
	
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
