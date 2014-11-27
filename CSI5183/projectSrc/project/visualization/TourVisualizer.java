package project.visualization;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import javax.swing.JPanel;

import project.problem.types.NodeInfo;

public class TourVisualizer extends JPanel {

	/**
	 * Create the panel.
	 */
	private NodeInfo[] nodes;	
	private ArrayList<Integer> sensingHoles;	
	private Integer[] solutionVector;
	
	private double nodeWidth;
	private Insets insets;
	
	//Panel Bounds
	private int displayWidth;
	private int displayHeight;
	private double scaleX;
	private double scaleY;
	private double scale;
	private double offsetX;
	private double offsetY;
	
	double left = Double.POSITIVE_INFINITY;
	double right = Double.NEGATIVE_INFINITY;
	double bottom = Double.POSITIVE_INFINITY;
	double top = Double.NEGATIVE_INFINITY;
	
	
	public TourVisualizer() {
		super();
		
		nodeWidth = 4.0;
		insets = new Insets((int)nodeWidth, (int)nodeWidth, (int)nodeWidth, (int)nodeWidth);
		
		setBackground(Color.WHITE);
		setForeground(Color.BLACK);
	}
	
	public void updateContent(NodeInfo[] nodes, ArrayList<Integer> sensingHoles) {
		this.nodes = nodes;
		this.sensingHoles = sensingHoles;
	}
	
	public void reset() {
		this.nodes = new NodeInfo[0];
		this.sensingHoles = new ArrayList<Integer>();
	}
	
	public void updateSelection(int[] tour) {
		ArrayList<Integer> vector = new ArrayList<Integer>();
		
		vector.add(0);
		
		for (int i=0; i < tour.length; i++){
			if (tour[i] > 0) {
				vector.add(tour[i]);
			}
		}
		
		vector.add(0);
		
		this.solutionVector = vector.toArray(new Integer[0]);
	}
	
	public void paintNodes(Graphics g) {
		//Remove anything pre-existing
		super.paintComponent(g);
		
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		// first determine bounds of the data
//		boolean isGeographical = EdgeWeightType.GEO.equals(problem.getEdgeWeightType());
		left = Double.POSITIVE_INFINITY;
		right = Double.NEGATIVE_INFINITY;
		bottom = Double.POSITIVE_INFINITY;
		top = Double.NEGATIVE_INFINITY;

		for (int i = 0; i < nodes.length; i++) {
			NodeInfo node = nodes[i];
			double[] position = new double[]{node.getLocation().getX() + 500, node.getLocation().getY() + 500};

			left = Math.min(left, position[0]);
			right = Math.max(right, position[0]);
			bottom = Math.min(bottom, position[1]);
			top = Math.max(top, position[1]);
		}

		// calculate the bounds of the drawing
		displayWidth = getWidth();
		displayHeight = getHeight();
		scaleX = (displayWidth - insets.right - insets.left) / (right - left);
		scaleY = (displayHeight - insets.top - insets.bottom) / (top - bottom);
		scale = Math.min(scaleX, scaleY);
		offsetX = (displayWidth - insets.right - insets.left - scale * (right - left)) / 2.0;
		offsetY = (displayHeight - insets.top - insets.bottom - scale * (top - bottom)) / 2.0;

		// draw the nodes
		for (int i = 0; i < nodes.length; i++) {
			g2.setColor(Color.BLACK);
			NodeInfo node = nodes[i];
			double[] position = {node.getLocation().getX()+ 500, node.getLocation().getY()+ 500};
			
			if (sensingHoles.contains(i)) {
				g2.setColor(Color.RED);
			} else if (i == 0) {
				g2.setColor(Color.GREEN);
			}

			Ellipse2D point = new Ellipse2D.Double(
					displayWidth - (offsetX + scale * (position[0] - left) + insets.left) - (nodeWidth / 2.0),
					displayHeight - (offsetY + scale * (position[1] - bottom) + insets.bottom) - (nodeWidth / 2.0),
					nodeWidth,
					nodeWidth);

			g2.fill(point);
			g2.draw(point);
		}
	}
	
	public void paintSolution(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		// draw the tours
		g2.setPaint(Color.BLUE);
		g2.setStroke(new BasicStroke());

		for (int i = 0; i < solutionVector.length; i++) {
			NodeInfo node1 = nodes[solutionVector[i]];
			NodeInfo node2;
			if (i == solutionVector.length-1) {
				node2 = nodes[solutionVector[0]];
			} else {
				node2 = nodes[solutionVector[i+1]];
			}
			double[] position1 = {node1.getLocation().getX()+ 500, node1.getLocation().getY()+ 500};
			double[] position2 = {node2.getLocation().getX()+ 500, node2.getLocation().getY()+ 500};

			Line2D line = new Line2D.Double(
					displayWidth - (offsetX + scale * (position1[0] - left) + insets.left), 
					displayHeight - (offsetY + scale * (position1[1] - bottom) + insets.bottom),
					displayWidth - (offsetX + scale * (position2[0] - left) + insets.left),
					displayHeight - (offsetY + scale * (position2[1] - bottom) + insets.bottom));

			g2.draw(line);
		}
	}
	
	@Override
	protected synchronized void paintComponent(Graphics g) {
		super.paintComponent(g);
		
//		if (nodes.length < 1 || solutionVector.length < 1 || sensingHoles.size() < 1) {
//			return;
//		}
//		Graphics2D g2 = (Graphics2D)g;
//		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
//		paintNodes();
	}

}
