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
		
		for(Journey j: journeys){
			
			sortOccupied();
			
			
			System.out.println("---JOURNEY " + j.getTrain().getName());
			
			nextBlock = j.getNextToBeScheduled();
			
			nextBlockOccupied = occupied.get(nextBlock.getBlock().getID());
			
			//Set arrival in first block to departure time
			nextBlock.setArrTime(j.getPreviousBlock().getDepTime());
			
			if(nextBlockOccupied.isEmpty()){
				System.out.println("No occupations");
				
				//First block has no occupations - Retain original arrival time
				if(!scheduleBlock(j))
					return false;
			}else{
			
				System.out.println("Size: " + nextBlockOccupied.size());
				
				//Find valid arrival time in first block to be traversed			
				for(int x = 0; x <= nextBlockOccupied.size(); x++){
					System.out.println(x);
					
					System.out.println("Entry time: " +  nextBlock.getArrTime());
					
					if(x == nextBlockOccupied.size()){
						
						System.out.println("Last occupation");
						
						//Last occupation, make or break
						if(!scheduleBlock(j)){
							//Journey cannot be scheduled
							return false;
						}else{
							//Schedule next journey
							break;
						}
						
					}
					
					if(isTimeCollision(nextBlock.getArrTime(), nextBlockOccupied.get(x))){
						System.out.println("Time collision");
						j.getPreviousBlock().setDepTime(nextBlockOccupied.get(x).getDepTime());
						nextBlock.setArrTime(nextBlockOccupied.get(x).getDepTime());
						continue;
					}
				
					//Attempt to schedule
					if(!scheduleBlock(j)){
						System.out.println("Try next block");
						//Arrival time is after the last occupation
						j.getPreviousBlock().setDepTime(nextBlockOccupied.get(x).getDepTime());
						nextBlock.setArrTime(nextBlockOccupied.get(x).getDepTime());
						continue;
					}else{
						break;
					}
				}
				
			}
		
		}
		
		//All journeys scheduled
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
	
	
	
	
	
	
	
	
}
