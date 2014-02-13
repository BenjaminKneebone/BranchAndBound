package control;

import java.util.ArrayList;

import algorithms.Dijkstra;
import bbentities.BlockOccupation;
import bbentities.Journey;
import entities.BlockExit;
import entities.Engine;
import entities.Network;
import exceptions.RouteNotFoundException;

public class Test {
	
	static ArrayList<Journey> journies = new ArrayList<Journey>();
	
	
	

	public static void main(String[] args){
		Network n = new Network();
		n.printNetworkInfo();
		
		Dijkstra d = new Dijkstra(n);

		Engine t = n.getTrains().get(0);
		t.printDetails();
			
		int[] stations = {0, 5,4,0,17};
		int[] stations2 = {0, 5,0,17};
		
		Journey j;
		try {
			j = new Journey(n.getTrains().get(0), stations, d, 0);
			journies.add(j);
			j.printJourney();
			j = new Journey(n.getTrains().get(1), stations2, d, 0);
			journies.add(j);
			j.printJourney();
		} catch (RouteNotFoundException e) {
			e.printStackTrace();
		}
			
		//Create a scheduler with original configuration
		Scheduler s = new Scheduler(journies, n.getBlocks(), n.getTrains());
		s.schedule();
		
		
		
		
	}
	
}
