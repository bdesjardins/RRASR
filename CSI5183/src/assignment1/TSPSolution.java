package assignment1;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class TSPSolution implements Solution {
	
	private static class Coordinate {
		private double x;
		private double y;
		
		public Coordinate(double x, double y) {
			this.x = x;
			this.y = y;
		}
		
		public double x() {
			return this.x;
		}
		
		public double y() {
			return this.y;
		}
	}
		
	private ArrayList<Integer> representation;
	private static HashMap<Integer, Coordinate> cities;
	private double fitness;
	
	//Used to instantiate the GA and build the cities map
	public TSPSolution(String filename) {
		cities = new  HashMap<Integer, Coordinate>();

		try {
			Scanner fileInput = new Scanner(new File(filename));

			while (fileInput.hasNextLine()) {
				String[] line = (fileInput.nextLine()).split(" ");

				int city = Integer.parseInt(line[0]);
				Coordinate coord = new Coordinate(Double.parseDouble(line[1]), Double.parseDouble(line[2]));

				cities.put(city, coord);
			}

			fileInput.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		ArrayList<Integer> cityList = getCityList();
		ArrayList<Integer> newRep = new ArrayList<Integer>();
		
		while (cityList.size() > 0) {
			int choice = (int) Math.floor((Math.random() * cityList.size()));
			newRep.add(cityList.remove(choice));			
		}
		
		this.representation = newRep;
		evaluate();
	}
	
	public TSPSolution() {
		ArrayList<Integer> cityList = getCityList();
		ArrayList<Integer> newRep = new ArrayList<Integer>();
		
		while (cityList.size() > 0) {
			int choice = (int) Math.floor((Math.random() * cityList.size()));
			newRep.add(cityList.remove(choice));			
		}
		
		this.representation = newRep;
		evaluate();
	}
	
	public TSPSolution(ArrayList<Integer> child) {
		this.representation = child;
		evaluate();
	}
	
	@Override
	public double getFitness() {
		return this.fitness;
	}

	@Override
	public void printRepresentation() {
		for (int i = 0; i < representation.size(); i++) {
			System.out.print(representation.get(i));
			if (i == representation.size()-1) {
				break;
			}
			System.out.print("-");			
		}
		
		System.out.println("	" + this.fitness);
	}

	@Override
	public void evaluate() {
		double fit = 0;
		
		for (int i = 0; i < representation.size(); i++) {
			if (i+1 == representation.size()) {
				fit += getDistance(cities.get(representation.get(i)), cities.get(representation.get(0)));				
				break;
			}
			
			fit += getDistance(cities.get(representation.get(i)), cities.get(representation.get(i+1)));
		}
		
		this.fitness = -1*fit;
	}

	@Override
	public void mutate(double mutationChance) {
		if (Math.random() < mutationChance) {
			int choice = (int) Math.floor((Math.random() * representation.size()));			
			int city = representation.remove(choice);
			
			choice = (int) Math.floor((Math.random() * representation.size()));
			representation.add(choice, city);			
		}
	}

	@Override
	public Solution crossover(Solution parent2) {
		String type = "order";
		
		ArrayList<Integer> childHolder = new ArrayList<Integer>();
				
		if (type.equalsIgnoreCase("order")) {
			int[] child = new int[representation.size()];
			
			int a = (int) Math.floor((Math.random() * representation.size()));
			int b = (int) Math.floor((Math.random() * representation.size()));
			ArrayList<Integer> par2 = (ArrayList<Integer>) parent2.getRepresentation();
			
			if (b < a) {
				int temp = a;
				a = b;
				b = temp;
			}
			
			for (int i = a; i <= b; i++) {
//				child.add(i, this.representation.get(i));
				child[i] = this.representation.get(i);
			}
			
			int j = 0;
			
			for (int i = 0; i < par2.size(); i++) {
				if (j == a) {
					j = b+1;
				}
				
//				if (child.contains(par2.get(i))) {
				if (contains(par2.get(i), child)) {
					continue;
				} else {
					child[j] = par2.get(i);
//					child.add(j, par2.get(i));
					j++;
				}
			}
			
			for (int i = 0; i < child.length; i++) {
				childHolder.add(child[i]);
			}
		}
		
		else if (type.equalsIgnoreCase("partially-mapped")) {}
	
		return new TSPSolution(childHolder);
	}

	@Override
	public Object getRepresentation() {
		return this.representation;
	}
	
	private double getDistance(Coordinate location1, Coordinate location2) {
		double x1 = location1.x();
		double y1 = location1.y();
		
		double x2 = location2.x();
		double y2 = location2.y();
		
		double distance = Math.sqrt(Math.pow((x2-x1),2) + Math.pow((y2-y1),2));
		
		return distance;
	}

	private ArrayList<Integer> getCityList() {
		return new ArrayList<Integer>(cities.keySet());
	}
	
	private boolean contains(int contain, int[] arr) {
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] == contain) {
				return true;
			}
		}
		
		return false;
	}
}
