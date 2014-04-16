package ownalgorithm;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

import traindiagrams.TrainDiagramCreator;


import entities.Block;
import entities.BlockOccupation;
import entities.Engine;
import entities.Journey;
import exceptions.InvalidSpeedException;

public class NodeControl {

	private ArrayList<ArrayList<BlockOccupation>> occupied;
	private ArrayList<Journey> journeys;
	private OccupationComparator occComp = new OccupationComparator();
	
	private HashMap<BlockOccupation, Integer> nextBlockHash = new HashMap<BlockOccupation, Integer>();
	
	public NodeControl(ArrayList<Journey> journeys, ArrayList<Block> blocks){
		
		this.journeys = journeys;
		
		for(Journey j: journeys){
			for(int x = 0; x < j.getBlockOccupations().size() - 1; x++){
				nextBlockHash.put(j.getBlockOccupations().get(x),
						j.getBlockOccupations().get(x + 1).getBlock().getID());
			}
				
		}
		
		
		occupied = new ArrayList<ArrayList<BlockOccupation>>(blocks.size());
		
		//Add empty occupation lists for each block
		for(int x = 0; x < blocks.size(); x++)
			occupied.add(new ArrayList<BlockOccupation>());		
		
		
		if(schedule()){
			for(Journey j: journeys)
				j.printJourney();
			
			saveOptimal();
		}else{
			System.out.println("No schedule could be produced");
		}
		
		
	}
	
	
	private boolean schedule(){
		
		ArrayList<BlockOccupation> nextBlockOccupied;
		BlockOccupation nextBlock;
		ArrayList<BlockOccupation> originOccupied;
		BlockOccupation origin;
		
		for(Journey j: journeys){
			
			System.out.println("---JOURNEY " + j.getTrain().getName());
			
			sortOccupied();
			
			origin = j.getPreviousBlock();
			nextBlock = j.getNextToBeScheduled();
			nextBlockOccupied = occupied.get(nextBlock.getBlock().getID());
			originOccupied = occupied.get(origin.getBlock().getID());
			
			//The windows in which the train can depart the origin block
			ArrayList<TimeWindow> timeWindows = new ArrayList<TimeWindow>();
			
			int stationStopTime = origin.getStationStopTime();
			
			System.out.println(stationStopTime);
			
			if(originOccupied.isEmpty()){
				timeWindows.add(new TimeWindow(stationStopTime, Integer.MAX_VALUE));
			}else{			
				
				//Can the train be in the station from time 0?
				if(originOccupied.get(0).getArrTime() > stationStopTime)
					timeWindows.add(new TimeWindow(stationStopTime, originOccupied.get(0).getArrTime()));
				
				//Any gaps in the origin block
				for(int x = 0; x < originOccupied.size() - 1; x++){
					if(originOccupied.get(x + 1).getArrTime() != originOccupied.get(x).getDepTime()){
						timeWindows.add(new TimeWindow(originOccupied.get(x + 1).getArrTime(), originOccupied.get(x).getDepTime() + stationStopTime));
					}
				}
			
				//Gap after all occupations
				timeWindows.add(new TimeWindow(originOccupied.get(originOccupied.size() - 1).getDepTime() + stationStopTime, Integer.MAX_VALUE));
			}
			
			for(TimeWindow t : timeWindows){
				System.out.print("Start:" + t.getStart() + " End: " + t.getEnd());
			}
			
			//Attempt to schedule
			origin.setDepTime(timeWindows.get(0).getStart());
			origin.setArrTime(origin.getDepTime() - stationStopTime);
			origin.setStationArrivalTime(origin.getArrTime());
			nextBlock.setArrTime(origin.getDepTime());
			
			System.out.println("Origin dep Time: " + origin.getDepTime());
			System.out.println("Next block arr Time: " + nextBlock.getArrTime());
			
			if(nextBlockOccupied.isEmpty()){
				System.out.println("Next block empty");
				
				//Should be able to schedule
				originOccupied.add(origin);
				if(scheduleBlock(j))
					continue;
				else
					return false;
			}else{
				System.out.println("Next block not empty");
				
				//Find first point when it can enter next block
				for(BlockOccupation b: nextBlockOccupied)
					if(isTimeCollision(origin.getDepTime(), b) && origin.getDepTime() != b.getDepTime()){
						if(nextBlockHash.get(b) != origin.getBlock().getID()){
							origin.setDepTime(b.getDepTime());
							origin.setArrTime(origin.getDepTime() - stationStopTime);
							origin.setStationArrivalTime(origin.getArrTime());
							nextBlock.setArrTime(origin.getDepTime());
							break;
						}else{
							timeWindows.remove(0);
							//Attempt to schedule
							origin.setDepTime(timeWindows.get(0).getStart());
							origin.setArrTime(origin.getDepTime() - stationStopTime);
							origin.setStationArrivalTime(origin.getArrTime());
							nextBlock.setArrTime(origin.getDepTime());
						}
					}
					
				boolean lastAttempt = false;
				
				originOccupied.add(origin);
				while(!scheduleBlock(j)){
					originOccupied.remove(origin);
					
					if(lastAttempt)
						return false;
					
					//Try next departure time
					if(origin.getDepTime() + 1 > timeWindows.get(0).getEnd()){
						timeWindows.remove(0);
						
						System.out.println("New time window");
						
						origin.setDepTime(timeWindows.get(0).getStart());
						origin.setArrTime(origin.getDepTime() - stationStopTime);
						origin.setStationArrivalTime(origin.getArrTime());
						nextBlock.setArrTime(origin.getDepTime());
						
					}else{
						origin.setDepTime(origin.getDepTime() + 1);
						origin.setArrTime(origin.getDepTime() - stationStopTime);
						origin.setStationArrivalTime(origin.getArrTime());
						nextBlock.setArrTime(origin.getDepTime());
					}
					
					
					for(int x = 0; x < nextBlockOccupied.size(); x++){
						if(isTimeCollision(origin.getDepTime(), nextBlockOccupied.get(x))){
							
							if(nextBlockOccupied.get(x).getDepTime() <= timeWindows.get(0).getEnd() && nextBlockHash.get(nextBlockOccupied.get(x)) != origin.getBlock().getID()){
								System.out.println("Same time window");
								origin.setDepTime(nextBlockOccupied.get(x).getDepTime());
								origin.setArrTime(origin.getDepTime() - stationStopTime);
								origin.setStationArrivalTime(origin.getArrTime());
								nextBlock.setArrTime(origin.getDepTime());
							}else{
								timeWindows.remove(0);
								origin.setDepTime(timeWindows.get(0).getStart());
								origin.setArrTime(origin.getDepTime() - stationStopTime);
								origin.setStationArrivalTime(origin.getArrTime());
								nextBlock.setArrTime(origin.getDepTime());
							}
							if(x == nextBlockOccupied.size() - 1)
								lastAttempt = true;
						}
					}
					originOccupied.add(origin);
					
				}
				continue;
			}	
		
		}
		return true;	
			
			
	
		
	
		
	}
	
	private boolean scheduleBlock(Journey journey){
		
		System.out.println("-------------------------------------------------------------------------------");
		
		for(ArrayList<BlockOccupation> al: occupied)
			Collections.sort(al, occComp);
		
		BlockOccupation currBlock = journey.getNextToBeScheduled();
		ArrayList<BlockOccupation> currOccupied = occupied.get(currBlock.getBlock().getID());
		
		BlockOccupation prevBlock = journey.getPreviousBlock();
		ArrayList<BlockOccupation> prevOccupied = occupied.get(prevBlock.getBlock().getID());
		
		System.out.println("NEXT BLOCK: " + currBlock.getBlock().getID());
		
		//Next train arrival time
		double lastExitTime = Integer.MAX_VALUE;
		for(BlockOccupation b: currOccupied)
			if(b.getArrTime() > currBlock.getArrTime()){
				lastExitTime = b.getArrTime();
				break;
			}
	
		if(journey.lastBlock()){
				
			System.out.println("LAST BLOCK");
			
			//Get time required by train in block
			try {
				currBlock = journey.getTrain().exitBlockAtSetSpeed(currBlock, 0);
				System.out.println(currBlock.getBlockOccupationDetail());
				
				//If train leaves after next train arrives, infeasible scheduling
				if(currBlock.getDepTime() >= lastExitTime)
					return false;
				
				currOccupied.add(currBlock);
				System.out.println(currBlock.getBlockOccupationDetail());
				printOccupied();
				return true;
				
			} catch (InvalidSpeedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}else{
			
			System.out.println("NOT LAST BLOCK");
			
			BlockOccupation nextBlock = journey.getSecondToBeScheduled();
			ArrayList<BlockOccupation> nextOccupied = occupied.get(nextBlock.getBlock().getID());
 			
			System.out.println("ENTRY TIME: " + currBlock.getArrTime());
			System.out.println("LAST EXIT TIME: " + lastExitTime);
			
			//Next block is occupied for the duration of the free time in the current block
			for(BlockOccupation b: nextOccupied)
				if(b.getArrTime() <= currBlock.getArrTime() && b.getDepTime() > lastExitTime){
					System.out.println("Next block occupied for duration");
					return false;
				}
			
			//Earliest train can enter next block
			double earliestExitTime = currBlock.getArrTime();
			
			
			for(int x = 0; x < nextOccupied.size(); x++){
				//If current arrival time is occupied in next block
				if(isTimeCollision(currBlock.getArrTime(), nextOccupied.get(x)))
					earliestExitTime = nextOccupied.get(x).getDepTime();
			
				//If current exit time is occupied in next block
				if(isTimeCollision(lastExitTime, nextOccupied.get(x))){
					//If train in next block enters this block - avoid deadlock
					if(nextOccupied.get(x).getDepTime() == lastExitTime && 
							nextBlockHash.get(nextOccupied.get(x)) == currBlock.getBlock().getID())
						lastExitTime = nextOccupied.get(x).getArrTime() - 1;
					
					if(nextOccupied.get(x).getDepTime() != lastExitTime){
						//Bring exit time forward
						lastExitTime = nextOccupied.get(x).getArrTime() - 1;
						break;
					}
						
					
				}	
			}
						
			System.out.println("EARLIEST NEXT BLOCK ENTRY: " + earliestExitTime);
			System.out.println("LAST EXIT TIME: " + lastExitTime);
			
			//STATION
			if(currBlock.isStation()){
				System.out.println("Station");
				try {
					currBlock = journey.getTrain().exitBlockAtSetSpeed(currBlock, 0);
				} catch (InvalidSpeedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//Train can't complete block in time
				if(currBlock.getDepTime() > lastExitTime){
					System.out.println(currBlock.getBlockOccupationDetail());
					System.out.println("REJECTED");
					currOccupied.remove(currBlock);
					return false;
				}else{
					nextBlock.setArrSpeed(0);
					
					//Do we need to keep train in station?
					if(currBlock.getDepTime() < earliestExitTime){
						//Push back departure time
						currBlock.setDepTime(earliestExitTime);
						System.out.println("Station departure delayed");
					}

					nextBlock.setArrTime(currBlock.getDepTime());
					
					currOccupied.add(currBlock);
					
					System.out.println("Station Block Info");
					System.out.println(currBlock.getBlockOccupationDetail());
					
					printOccupied();
					journey.incrementJourney();
					if(!scheduleBlock(journey)){
						journey.decrementJourney();
						
						ArrayList<BlockOccupation> potential = new ArrayList<BlockOccupation>();
						
						//Check other free slots
						for(BlockOccupation b: occupied.get(nextBlock.getBlock().getID())){
							if(b.getArrTime() > earliestExitTime && b.getDepTime() <= lastExitTime){
								potential.add(b);
							}	
						}
						
						//Attempt to schedule the train in the next block after the occupation
						for(BlockOccupation b: potential){					
							currBlock.setDepTime(b.getDepTime());
							nextBlock.setArrTime(b.getDepTime());
							
							currOccupied.add(currBlock);
							journey.incrementJourney();
							
							printOccupied();
							
							if(scheduleBlock(journey)){
								return true;
							}
							
							journey.decrementJourney();
						}	
						
					}else{
						return true;
					}
					System.out.println("REJECTED");
					currOccupied.remove(currBlock);
					return false;
					
				}
			}else{
				
				if (journey.getTrain().canStopInBlock(nextBlock.getLength())) {
					System.out.println("Full Power Block");
					/*CAN ENTER NEXT BLOCK AT ANY SPEED - FULL SPEED AHEAD*/
					try {
						currBlock = journey.getTrain().timeToTraverse(currBlock);
					} catch (InvalidSpeedException e) {
						e.printStackTrace();
					}
					
					
				} else {
					/*MUST ENTER NEXT BLOCK AT REDUCED SPEED*/
					System.out.println("Reduced Exit Speed Block");
					int depSpeed = journey.getTrain().highestBlockEntrySpeed(currBlock.getLength());

					try {
						currBlock = journey.getTrain().exitBlockAtSetSpeed(currBlock, depSpeed);
					} catch (InvalidSpeedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				System.out.println("Dep Time:" + currBlock.getDepTime());
				
				if(currBlock.getDepTime() < earliestExitTime){
					System.out.println("Train arrives too early, push back departure");
					try {
						currBlock = journey.getTrain().minimumTimeTraversal(currBlock, earliestExitTime - currBlock.getArrTime());
					} catch (InvalidSpeedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				//Train needs too long in the block - May not be at an integer speed - Unlikely
				if(currBlock.getDepTime() > lastExitTime)
					return false;
				
				nextBlock.setArrTime(currBlock.getDepTime());
				nextBlock.setArrSpeed(currBlock.getDepSpeed());
				currOccupied.add(currBlock);
				System.out.println(currBlock.getBlockOccupationDetail());
				printOccupied();
				journey.incrementJourney();
				if(!scheduleBlock(journey)){
					currOccupied.remove(currBlock);
					journey.decrementJourney();
					
					ArrayList<BlockOccupation> potential = new ArrayList<BlockOccupation>();
					
					//Check other free slots
					for(BlockOccupation b: nextOccupied){
						//Other occupations in next block
						if(b.getArrTime() > earliestExitTime && b.getDepTime() <= lastExitTime){
							potential.add(b);
						}
					}
					
					for(BlockOccupation b: potential){
							//Enter block after previous occupation
							try {
								currBlock = journey.getTrain().minimumTimeTraversal(currBlock, b.getDepTime() - currBlock.getArrTime());
							} catch (InvalidSpeedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							//Train needs too long in the block - May not be at an integer speed - Unlikely
							if(currBlock.getDepTime() > lastExitTime)
								return false;
							
							nextBlock.setArrTime(currBlock.getDepTime());
							nextBlock.setArrSpeed(currBlock.getDepSpeed());
							
							currOccupied.add(currBlock);
							printOccupied();
							journey.incrementJourney();
							if(scheduleBlock(journey)){
								return true;
							}
							currOccupied.remove(currBlock);
							journey.decrementJourney();
					}		
					
					System.out.println("REJECTED");
					//No possible free blocks
					return false;
				}else{
					return true;
				}
			}
			
		}
		currOccupied.remove(currBlock);
		return false;	
	}
	
	private boolean isTimeCollision(double time, BlockOccupation blockOccupation){
		return time >= blockOccupation.getArrTime() && time <= blockOccupation.getDepTime();
	}
	
	
	/**
	 * Save the optimal schedule found & its train diagram
	 */
	public void saveOptimal(){
		
		//ScheduleJSONWriter.writeJSONSchedule(bestNode);
		
		File sch = new File("schedule/scheduleatnode.txt");

		FileWriter write = null;
		PrintWriter print = null;
		try {
			write = new FileWriter(sch, false);

			print = new PrintWriter(write);

			for (Journey journey : journeys) {
				print.write(journey.getTrain().getName() + " schedule\n");

				for (BlockOccupation b : journey.getBlockOccupations())
					print.write(b.getBlockOccupationDetail());
			}
			
			for (Journey journey : journeys) {
				print.write(journey.getTrain().getName() + " schedule\n");

				print.printf("Departing Station " + journey.getBlockOccupations().get(0).getBlock().getID() + " at " + journey.getBlockOccupations().get(0).getDepTime() + "\n");
				
				for (BlockOccupation b : journey.getBlockOccupations())
					print.printf(b.getMessage()); 
			
				print.write("\n");
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (print != null) {
				print.close();
			}
		}

		TrainDiagramCreator tdc = new TrainDiagramCreator();
		tdc.drawDiagram(journeys, "1");
	}
	
	private void printOccupied(){
		System.out.println("Block Occupations");
		for(int x = 0; x < occupied.size(); x++){
			System.out.println("Block " + x);
			for(BlockOccupation b: occupied.get(x)){
				System.out.print("ARR: " + b.getArrTime() + " DEP: " + b.getDepTime() + "  ");
			}
			System.out.print("\n");
		}
	}
	
	private void sortOccupied(){
		for(ArrayList<BlockOccupation> al: occupied)
			Collections.sort(al, occComp);
	}
	
	private class OccupationComparator implements Comparator<BlockOccupation>{

		@Override
		public int compare(BlockOccupation b0, BlockOccupation b1) {
			if(b0.getArrTime() > b1.getArrTime())
				return 1;
			else
				if(b0.getArrTime() == b1.getArrTime())
					return 0;
				else
					return - 1;
		}
		
	}
	
	private class TimeWindow{
		double start;
		double end;
		
		public TimeWindow(double start, double end){
			this.start = start;
			this.end = end;
		}
		
		public double getStart(){
			return start;
		}
		
		public double getEnd(){
			return end;
		}
		
		
		
	}
	
	
	
	
	
	
	
	
}
