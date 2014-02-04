package control;

import java.util.ArrayList;

import algorithms.Dijkstra;
import bbentities.BlockOccupation;
import bbentities.Journey;
import entities.Network;
import exceptions.RouteNotFoundException;

public class Test {
	
	static ArrayList<Journey> journies = new ArrayList<Journey>();
	
	
	

	public static void main(String[] args){
		Network n = new Network();
		n.printNetworkInfo();
		
		Dijkstra d = new Dijkstra(n);

		int[] stations = {0, 5,4,0,17};
		
		Journey j;
		try {
			j = new Journey(n.getTrains().get(0), stations, d);
			journies.add(j);
		} catch (RouteNotFoundException e) {
			e.printStackTrace();
		}
		
		
		/*
		//Test Cloning
		
		//Create a scheduler with original configuration
		Scheduler s = new Scheduler(journies, n.getBlocks(), n.getTrains());
		
		//Set arrival time for all block occupations and change last arrival for all blocks
		for(BlockOccupation b: journies.get(0).getBlockOccupations()){
			b.setArrTime(2000);
			b.getBlock().setArrivalTime(2000);
		}
		
		//Print out details for changed objects
		for(BlockOccupation b: journies.get(0).getBlockOccupations()){
			b.printBlockDetail();
			b.getBlock().printBlockDetail();
		}
		
		System.out.println("-----------");
		
		//Print out the state of the scheduler - should not be affected by changes
		s.printJournies();
		*/
	}
	
}
