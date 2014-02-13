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
	//The index of the first unscheduled BlockOccupation
	private int nextToBeScheduled;
	
	//When journey is first initialised
	public Journey(Engine train, int[] stations, Dijkstra d, int nextToBeScheduled) throws RouteNotFoundException{
		this.train = train;
			
		//Get separate parts of the route (between stations)
		for(int x = 0; x < stations.length - 1; x++)
			journey.addAll(d.shortestRoute(stations[x], stations[x+1], train));
		
		//Remove duplicate stops in same block.
		for(int x = 0; x < journey.size() - 1; x++)
			if(journey.get(x).getBlock() == journey.get(x + 1).getBlock())
				journey.remove(x + 1);
		
		//Set start of journey to time 0, speed 0
		journey.get(0).setArrTime(0);
		journey.get(0).setArrSpeed(0);
	
	}
	
	//When journey is cloned
	public Journey(ArrayList<BlockOccupation> blockOccupation, Engine train, int nextToBeScheduled){
		this.train = train;
		this.journey = blockOccupation;
		
		this.nextToBeScheduled = nextToBeScheduled;
		System.out.println("Next to be scheduled value = " + nextToBeScheduled);
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
	
	public Engine getTrain(){
		return train;
	}
	
	public ArrayList<BlockOccupation> getBlockOccupations(){
		return journey;
	}
	
	public BlockOccupation getNextToBeScheduled(){
		return journey.get(nextToBeScheduled);
	}
	
	public BlockOccupation getSecondToBeScheduled(){
		return journey.get(nextToBeScheduled+1);
	}
	
	/**Increment counter pointing at next block to be scheduled
	 */
	public void incrementJourney(){
		nextToBeScheduled++;
		System.out.println("incremented " + nextToBeScheduled);
	}
	
	/**
	 * @return true if the journey has been completely scheduled
	 */
	public boolean isScheduled(){
		return nextToBeScheduled > journey.size() - 1;
	}
	
	/**
	 * @return true if the next block to be scheduled is the last block in the journey
	 */
	public boolean lastBlock(){
		return nextToBeScheduled == journey.size() - 1;
	}
	
	public Journey clone(ArrayList<Block> blocks){
		ArrayList<BlockOccupation> cloneBO = new ArrayList<BlockOccupation>();
		for(BlockOccupation bo: journey)
			cloneBO.add(bo.clone(blocks));
		
		Journey j = new Journey(cloneBO, train, nextToBeScheduled);
		System.out.println("Constructor value " + nextToBeScheduled);
		
		return j;
	}
	
	
}
