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
	
	//When journey is first initialised
	public Journey(Engine train, int[] stations, Dijkstra d) throws RouteNotFoundException{
		this.train = train;
		
				
		//Get separate parts of the route (between stations)
		for(int x = 0; x < stations.length - 1; x++)
			journey.addAll(d.shortestRoute(stations[x], stations[x+1], train));
		
		//Remove duplicate stops in same block.
		for(int x = 0; x < journey.size() - 1; x++)
			if(journey.get(x).getBlock() == journey.get(x + 1).getBlock())
				journey.remove(x + 1);
		
	}
	
	//When journey is cloned
	public Journey(ArrayList<BlockOccupation> blockOccupation, Engine train){
		this.train = train;
		this.journey = blockOccupation;
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
	
	public Journey clone(){
		ArrayList<BlockOccupation> cloneBO = new ArrayList<BlockOccupation>();
		for(BlockOccupation bo: journey)
			cloneBO.add(bo.clone());
		
		Journey j = new Journey(cloneBO, train);
		
		return j;
	}
	
	
}
