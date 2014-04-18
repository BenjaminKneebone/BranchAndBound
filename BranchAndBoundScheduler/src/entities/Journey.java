package entities;

import java.util.ArrayList;

import entities.Block;
import entities.Engine;
import exceptions.RouteNotFoundException;

import algorithms.Dijkstra;
import algorithms.Dijkstra;

public class Journey {

	private Engine train;
	private ArrayList<BlockOccupation> journey = new ArrayList<BlockOccupation>();
	//Start at 2nd block. Train starts journey at end of first block
	private int nextToBeScheduled = 0;
	private int id;
	private int length = 0;
	private int noConnLength;
	
	public Journey(Engine train, ArrayList<Stop> stations, Dijkstra d, ArrayList<Journey> journeys) throws RouteNotFoundException{
		this.train = train;
		this.id = journeys.size();	
		
		//Get separate parts of the route (between stations)
		for(int x = 0; x < stations.size() - 1; x++)
				journey.addAll(d.shortestRoute(stations.get(x).getBlockID(), stations.get(x + 1).getBlockID(), train));
			
		//Ignore stopping time in first block (index 0)
		int stationIndex = 0;
		
		
		for(int x = 0; x < journey.size() - 1; x++){
			
			//Remove duplicate stops in same block.
			if(journey.get(x).getBlock() == journey.get(x + 1).getBlock()){
				journey.remove(x + 1);	
			}
			
			//Add to journey total length
			length += journey.get(x).getLength();
			noConnLength += journey.get(x).noConnLength();
			
			
			//For loops, the second station will match the first block also, we must ignore this
			if(!(stations.get(0) == stations.get(1) && x == 0))
				
				//Set station stop times
				if(stations.get(stationIndex).getBlockID() == journey.get(x).getBlock().getID()){
						journey.get(x).setStationStopTime(stations.get(stationIndex).getStopTime());
						stationIndex++;
				}
			
		}
		
		if(journey.size() >= 3){
			System.out.println(journey.size());
			for(int x = 1; x < journey.size() - 1; x++){
				System.out.println(x);
				if(journey.get(x).getConnection().getJoin() == journey.get(x+1).getConnection().getJoin())
					journey.get(x).setTurnaround(true);
			}
			
		}
		
		
		
		//Set stopping time in last block
		journey.get(journey.size() - 1).setStationStopTime(120);
		
		length += journey.get(journey.size() - 1).getLength();
		noConnLength += journey.get(journey.size() - 1).noConnLength();
		
		//Set start of journey to time 0
		journey.get(0).setArrTime(0);
	}
	
	//When journey is cloned
	public Journey(ArrayList<BlockOccupation> blockOccupation, Engine train, int nextToBeScheduled, int id){
		this.train = train;
		this.journey = blockOccupation;
		this.nextToBeScheduled = nextToBeScheduled;
		this.id = id;
	}
	
	public void printJourney(){
		System.out.println("Train " + train.getName() + " journey. Length: " + length);
		for(BlockOccupation j: journey)
			System.out.printf("Train: %-3d Block: %-3d Arriving: %-15.2f Speed: %-4dkm/h Departing: %-15.2f Speed: %-4dkm/h Station: %s\n", train.getID(), j.getBlock().getID(), j.getArrTime(), j.getArrSpeed(), j.getDepTime(), j.getDepSpeed(), j.isStation());
	}
	
	public void printSimpleJourney(){
		System.out.println(" \\begin{center} \n \\begin{tabular}{| c | c | c | c |} \n" +
							"\\hline \n" +
							"\\multicolumn{4}{|c|}{Train " + train.getName() + " manifest - Journey Length: " + (double) length/1000 + "km}\\\\ \n" + 
							"\\hline \n" +
							"\\textbf{Block/Conn.} & \\textbf{Distance to traverse} & \\textbf{Station} & \\textbf{Turnaround}\\\\ \n" +
							"\\hline ");
		
		for(BlockOccupation j: journey)
			j.printOccupationDetail();
		System.out.println(" \\end{tabular}\n \\end{center}");
	}
	
	public void printNoConnectionJourney(){
		System.out.println("\\begin{center} \n \\begin{tabular}{| c | c | c |} \n" +
							"\\hline \n" +
							"\\multicolumn{3}{|c|}{Train " + train.getName() + " manifest - Journey Length: " + (double) noConnLength/1000 + "km}\\\\ \n" + 
							"\\hline \n" +
							"\\textbf{Block.} & \\textbf{Distance to traverse} & \\textbf{Station}\\\\ \n" +
							"\\hline \n ");
		
		for(BlockOccupation j: journey)
			j.printNoConnOccupationDetail();
		System.out.println(" \\end{tabular} \n \\end{center}");
	}
	
	public int getLength(){
		return length;
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
	
	/**Increment counter pointing at next block to be scheduled*/
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
	
	public int getID(){
		return id;
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
		
		Journey j = new Journey(cloneBO, train, nextToBeScheduled, id);
		
		return j;
	}
	
	public boolean isFirstBlock(){
		return nextToBeScheduled == 0;
	}
	
	public boolean isSecondBlock(){
		return nextToBeScheduled == 1;
	}
	

	public ArrayList<BlockOccupation> getRemainingBlocks(){
		ArrayList<BlockOccupation> remJourney = new ArrayList<BlockOccupation>();
		for(int x = nextToBeScheduled + 1; x < journey.size(); x++)
			remJourney.add(journey.get(x));
		
		return remJourney;
		
	}
	
	public ArrayList<BlockOccupation> getPreviousBlocks(){
		ArrayList<BlockOccupation> remJourney = new ArrayList<BlockOccupation>();
		for(int x = nextToBeScheduled - 1; x >= 0; x--)
			remJourney.add(journey.get(x));
		
		return remJourney;
		
	}
	
	
}
