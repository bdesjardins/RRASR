package project.problem.types;

/**
 * 
 * @author bdesjardins
 *
 *
 * Contains all the information about a sensor node for the RRASR and RMRASR problems
 */
public class NodeInfo {
	
	private int activeDegree;
	private int demand;
	private double score;
	
	private Coordinate location;
	private double battery;
	
	/**
	 * Creates a new NodeInfo 
	 * 
	 * 
	 * @param demand (-1 if delivery, 0 if base station, 1 if pickup)
	 * @param location Coordinate location of the node
	 * @param battery Battery level of the node
	 */
	public NodeInfo(int demand, Coordinate location, double battery) {
		this.demand = demand;
		this.location = location;
		this.battery = battery;
		this.activeDegree = 0;
	}
	
	/**
	 * Creates a new NodeInfo. This node is used to determine the scores
	 * for the trajectory robustness objective
	 * 
	 * @param demand (-1 if delivery, 0 if base station, 1 if pickup)
	 * @param location Coordinate location of the node
	 * @param battery Battery level of the node
	 * @param degree Graph degree of the node (how many adjacent nodes)
	 */
	public NodeInfo(int demand, Coordinate location, double battery, int degree) {
		this.demand = demand;
		this.location = location;
		this.battery = battery;
		this.activeDegree=degree;
	}
	
	/**
	 * Creates a new NodeInfo. This is used for sensing hole nodes
	 * 
	 * @param demand (-1 if delivery, 0 if base station, 1 if pickup)
	 * @param location Coordinate location of the node
	 * @param battery Battery level of the node
	 * @param score The score of the node for calculating the Trajectory Robustness
	 */
	public NodeInfo(int demand, Coordinate location, double battery, double score) {
		this.demand = demand;
		this.location = location;
		this.battery = battery;
		this.score  = score;
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
