package control;

import java.util.ArrayList;

import algorithms.Dijkstra;
import bbentities.BlockOccupation;
import bbentities.Journey;
import entities.Block;
import entities.BlockExit;
import entities.Engine;
import entities.Network;
import exceptions.RouteNotFoundException;

public class Test {
	
	static ArrayList<Journey> journies = new ArrayList<Journey>();
	
	
	

	public static void main(String[] args){
		Network n = new Network("files/Network.json");
		
		Dijkstra d = new Dijkstra(n);

		Engine t = n.getTrains().get(0);
			
		ArrayList<Integer> stations = new ArrayList<Integer>();
		stations.add(0);
		stations.add(4);
		stations.add(8);
		
		ArrayList<Integer> stations2 = new ArrayList<Integer>();
		stations2.add(2);
		stations2.add(4);
		stations2.add(8);
		
		System.out.println("---INITIAL STATE---");
		System.out.println("---BLOCKS---");
		for(Block b: n.getBlocks())
			b.printBlockDetail();
		System.out.println("---JOURNEYS---");
		Journey j;
		try {
			j = new Journey(n.getTrains().get(0), stations, d, 0, 0);
			journies.add(j);
			j.printJourney();
			j = new Journey(n.getTrains().get(1), stations, d, 0, 1);
			journies.add(j);
			j.printJourney();
			j = new Journey(n.getTrains().get(2), stations2, d, 0, 2);
			journies.add(j);
			j.printJourney();
		} catch (RouteNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println("---END INITIAL STATE---");
		
		
		//Create a scheduler with original configuration
		Scheduler s = new Scheduler(journies, n.getBlocks(), n.getTrains());
		s.schedule();
		
		
		
		
	}
	
}
