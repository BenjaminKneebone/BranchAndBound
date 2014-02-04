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
		
		ArrayList<Block> route = new ArrayList<Block>();
		
		//Get separate parts of the route
		for(int x = 0; x < stations.length - 1; x++){
			
			ArrayList<Block> journeySegment;
			
			journeySegment = d.shortestRoute(stations[x], stations[x+1]);
			route.addAll(journeySegment);
		}
		
		//Remove duplicate stops in same block.
		for(int x = 0; x < route.size() - 1; x++){
			if(route.get(x) == route.get(x + 1))
				route.remove(x + 1);
			
			journey.add(new BlockOccupation(train, route.get(x)));
		}
		
		
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
