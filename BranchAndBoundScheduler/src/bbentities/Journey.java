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
	private int index;
	
	//When journey is first initialised
	public Journey(Engine train, ArrayList<Integer> stations, Dijkstra d, int nextToBeScheduled, int index) throws RouteNotFoundException{
		this.train = train;
			
		//Get separate parts of the route (between stations)
		for(int x = 0; x < stations.size() - 1; x++)
			journey.addAll(d.shortestRoute(stations.get(x), stations.get(x + 1), train));
		
		int stationIndex = 0;
		
		//Remove duplicate stops in same block.
		for(int x = 0; x < journey.size() - 1; x++){
			if(journey.get(x).getBlock() == journey.get(x + 1).getBlock())
				journey.remove(x + 1);
			
			if(stations.get(stationIndex) == journey.get(x).getBlock().getID()){
					journey.get(x).setStation(true);
					stationIndex++;
			}
			
		}
		
		journey.get(journey.size() - 1).setStation(true);
		
		System.out.println("Journey Size: " + journey.size());
		
		//Set start of journey to time 0, speed 0
		journey.get(0).setArrTime(0);
		journey.get(0).setArrSpeed(0);
		
		this.index = index;	
	}
	
	//When journey is cloned
	public Journey(ArrayList<BlockOccupation> blockOccupation, Engine train, int nextToBeScheduled, int index){
		this.train = train;
		this.journey = blockOccupation;
		this.index = index;
		
		this.nextToBeScheduled = nextToBeScheduled;
	}
	
	public void printJourney(){
		System.out.println("Train " + train.getName() + " journey");
		for(BlockOccupation j: journey)
			System.out.println("Train: " + train.getID() + " Passing block: " + j.getBlock().getID() + " Arriving: " + j.getArrTime() + " at " + j.getArrSpeed() + "km/h Departing at: " + j.getDepTime() + " at " + j.getDepSpeed() + "km/h + Station: " + j.isStation());
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
		
	public int getIndex(){
		return index;
	}
	
	/**Increment counter pointing at next block to be scheduled
	 */
	public void incrementJourney(){
		nextToBeScheduled++;
	}
	
	/**Decrement counter pointing at next block to be scheduled
	 */
	public void decrementJourney(){
		nextToBeScheduled--;
	}
	
	/**
	 * @return true if the journey has been completely scheduled
	 */
	public boolean isScheduled(){
		return nextToBeScheduled > journey.size() - 1;
	}
	
	public boolean firstBlock(){
		return nextToBeScheduled == 0;
	}
	
	public BlockOccupation getPreviousBlock(){
		return journey.get(nextToBeScheduled - 1);
	}
	
	/**
	 * @return true if the next block to be scheduled is the last block in the journey
	 */
	public boolean lastBlock(){
		return nextToBeScheduled >= journey.size() - 1;
	}
	
	public Journey clone(ArrayList<Block> blocks){
		ArrayList<BlockOccupation> cloneBO = new ArrayList<BlockOccupation>();
		for(BlockOccupation bo: journey)
			cloneBO.add(bo.clone(blocks));
		
		Journey j = new Journey(cloneBO, train, nextToBeScheduled, index);
		
		return j;
	}
	
	
}
