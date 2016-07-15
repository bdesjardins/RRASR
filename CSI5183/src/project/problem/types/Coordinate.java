package project.problem.types;

/**
 * 
 * @author bdesjardins
 *
 * Simple class to model a 2-d coordinate
 */
public class Coordinate {

	private double x;
	private double y;
	
	/**
	 * Create a coordinate of the form (x,y)
	 * 
	 * @param x 
	 * @param y
	 */
	public Coordinate(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Create a copy of another coordinate
	 * 
	 * @param copy
	 */
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
	
	/**
	 * Calculate the distance between this coordinate and another
	 * 
	 * @param other The other coordinate
	 * @return The distance between this coordinate and the other
	 */
	public double distanceBetween(Coordinate other) {
		double x1 = this.x;
		double y1 = this.y;
		
		double x2 = other.x;
		double y2 = other.y;
		
		double distance = Math.sqrt(Math.pow((x2-x1),2) + Math.pow((y2-y1),2));
		
		return distance;
	}
	
}
