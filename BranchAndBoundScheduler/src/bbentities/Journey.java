package bbentities;

import java.util.ArrayList;

import entities.Block;
import entities.Engine;
import entities.Network;
import exceptions.RouteNotFoundException;

import algorithms.Dijkstra;

public class Journey {

	private Engine train;
	private ArrayList<BlockOccupation> journey = new ArrayList<BlockOccupation>();
	
	public Journey(Engine train, int[] stations, Network n) throws RouteNotFoundException{
		this.train = train;
		
		Dijkstra d = new Dijkstra(n);
				
		//Get separate parts of the route (between stations)
		for(int x = 0; x < stations.length - 1; x++)
			journey.addAll(d.shortestRoute(stations[x], stations[x+1], train));
		
		//Remove duplicate stops in same block.
		for(int x = 0; x < journey.size() - 1; x++)
			if(journey.get(x).getBlock() == journey.get(x + 1).getBlock())
				journey.remove(x + 1);
		
	}
	
	public void printJourney(){
		System.out.println("Train: " + train.getID());
		System.out.println("Passing block: ");
		for(BlockOccupation j: journey){
			System.out.println(j.getBlock().getID());
			System.out.println("Arriving: " + j.getDepTime());
			System.out.println("Departing: " + j.getArrTime());
		}
	}
	
	
}
