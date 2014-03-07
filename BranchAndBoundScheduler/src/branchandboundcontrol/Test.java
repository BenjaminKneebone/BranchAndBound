package branchandboundcontrol;

import java.util.ArrayList;

import algorithms.Dijkstra;
import entities.BlockOccupation;
import entities.Journey;
import entities.Block;
import entities.BlockExit;
import entities.Engine;
import entities.JourneyCreator;
import entities.Network;
import exceptions.RouteNotFoundException;

public class Test {
	
	private static ArrayList<Journey> journeys = new ArrayList<Journey>();
	
	public static void main(String[] args){
		Network n = new Network("files/8BlockLoop1siding.json");
		n.printNetworkInfo();
		
		Dijkstra d = new Dijkstra(n);

		Engine t = n.getTrains().get(0);
			
		ArrayList<Integer> stations = new ArrayList<Integer>();
		stations.add(0);
		stations.add(4);
		
		
		
		
		System.out.println("---INITIAL STATE---");
		System.out.println("---BLOCKS---");
		for(Block b: n.getBlocks())
			b.printBlockDetail();
		System.out.println("---JOURNEYS---");
		try {
			JourneyCreator.createSingleJourney(n.getTrains().get(0), stations, d, journeys);
		} catch (RouteNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println("---END INITIAL STATE---");
		
		journeys.get(0).printJourney();
		
		
		//Create a scheduler with original configuration
		//new NodeControl(journeys, n.getBlocks(), n.getTrains());	
		
	}
	
}
