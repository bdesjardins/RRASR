package project.problem.types;

public class Coordinate {

	private double x;
	private double y;
	
	public Coordinate(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public Coordinate(Coordinate copy) {
		this.x = copy.x;
		this.y = copy.y;
	}
	
	public double getX() {
		return this.x;
	}
	
	public double getY() {
		return this.y;
	}
	
	public double distanceBetween(Coordinate other) {
		double x1 = this.x;
		double y1 = this.y;
		
		double x2 = other.x;
		double y2 = other.y;
		
		double distance = Math.sqrt(Math.pow((x2-x1),2) + Math.pow((y2-y1),2));
		
		return distance;
	}
	
}
