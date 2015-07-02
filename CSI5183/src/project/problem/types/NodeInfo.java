package project.problem.types;

public class NodeInfo {
	
	private int activeDegree;
	private int demand;
	private double score;
	
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
	
	public NodeInfo(int demand, Coordinate location, double battery, double score) {
		this.demand = demand;
		this.location = location;
		this.battery = battery;
		this.setScore(score);
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

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}
}
