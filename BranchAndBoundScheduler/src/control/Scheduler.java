package control;

import java.util.ArrayList;

import entities.Block;
import entities.BlockExit;
import entities.Engine;
import exceptions.InvalidSpeedException;

import bbentities.BlockOccupation;
import bbentities.Journey;

public class Scheduler {

	private ArrayList<Journey> journeys;
	private ArrayList<Block> blocks;
	private ArrayList<Engine> trains;
	
	public Scheduler(ArrayList<Journey> journeys, ArrayList<Block> blocks, ArrayList<Engine> trains){
		
		System.out.println("---NODE---");
		
		//Clone blocks
		ArrayList<Block> newBlocks = new ArrayList<Block>();
		for(Block b: blocks)
			newBlocks.add(b.clone());
		
		this.blocks = newBlocks;		
		
		for(Block b: this.blocks)
			b.printBlockDetail();
		
		//Clone journeys, pass in newly cloned blocks
		ArrayList<Journey> newJournies = new ArrayList<Journey>();
		for(Journey j: journeys)
			newJournies.add(j.clone(this.blocks));
		
		this.journeys = newJournies;	
		
		//Wipe all arrival times (Last line of step 2)
		for(Journey j: journeys){
			if(!j.isScheduled() && j.toBeWiped()){
				j.getNextToBeScheduled().setArrSpeed(0);
				j.getNextToBeScheduled().setArrTime(0);
			}else{
				j.setToBeWiped(true);
			}
		}
			
		//No need to clone trains
		this.trains = trains;
	}
	
	public void schedule(){
		
		//Details for earliest block exit for this node
		int index = 0;
		double firstArrival = Integer.MAX_VALUE;
		
		//Schedule next block for each train
		for(Journey j : journeys){
			if(!j.isScheduled() && !(j.firstBlock() && j.getNextToBeScheduled().getBlock().isOccupied())){
				//Journey to be scheduled & we're not looking at the first block and its occupied
							
				//Get first block to be scheduled on journey
				BlockOccupation firstBlock = j.getNextToBeScheduled();
				System.out.println("Occupied: " + firstBlock.getBlock().isOccupied());

				if(firstBlock.getArrTime() < firstBlock.getBlock().getNextPossibleEntry()){
					firstBlock.setArrTime(firstBlock.getBlock().getNextPossibleEntry());
				}
				
					Engine train = firstBlock.getTrain();
					BlockExit b = null;
						
					if(j.lastBlock()){
						
						System.out.println("Scheduling last block " + firstBlock.getBlock().getID() + " for " + train.getName());
						
						//Last block of the journey
						try {
							//Train coming to a stop at the end of the block
							b = train.exitBlockAtSetSpeed(firstBlock.getBlock(), firstBlock.getArrSpeed(), 0);
						} catch (InvalidSpeedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}else{
						//Not the last block of the journey
					
						//Get second block
						BlockOccupation secondBlock = j.getSecondToBeScheduled();
						
						//If second block has enough space for train to enter at full speed
						if(train.canStopInBlock(secondBlock.getBlock())){
							
							System.out.println("Scheduling block " + firstBlock.getBlock().getID() + " for " + train.getName() + " at full speed");
							
							//Full speed ahead
							try {
								b = train.timeToTraverse(firstBlock.getBlock(), firstBlock.getArrSpeed());
							} catch (InvalidSpeedException e) {
								e.printStackTrace();
							}
						}else{
							
							System.out.println("Scheduling block " + firstBlock.getBlock().getID() + " for " + train.getName() + " leaving at reduced speed");
							
							//Train must leave block at reduced speed
							int depSpeed = train.highestBlockEntrySpeed(secondBlock.getBlock());
							
							try {
								b = train.exitBlockAtSetSpeed(firstBlock.getBlock(), firstBlock.getArrSpeed(), depSpeed);
							} catch (InvalidSpeedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
										
						//If next block is occupied or we arrive before the last train has cleared
						if(secondBlock.getBlock().isOccupied() || ((firstBlock.getArrTime() + b.getTime()) < secondBlock.getBlock().getNextPossibleEntry())){
							
							System.out.println("Next block still occupied - extend time in block");
							
							//Train needs to stop at the end of the block
							try {
								b = train.exitBlockAtSetSpeed(firstBlock.getBlock(), firstBlock.getArrSpeed(), 0);
							} catch (InvalidSpeedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					
					System.out.println("Arriving at " + firstBlock.getArrTime() + " at " + firstBlock.getArrSpeed());
					System.out.println("Leaving at " + (firstBlock.getArrTime() + b.getTime()) + " at " + b.getSpeed());
					
					//Time leaving block
					firstBlock.setDepTime(firstBlock.getArrTime() + b.getTime());
					//Speed leaving block
					firstBlock.setDepSpeed(b.getSpeed());
					
					//If this is the BlockOccupation with the earliest exit
					if(firstBlock.getDepTime() < firstArrival){
						firstArrival = firstBlock.getDepTime();
						index = journeys.indexOf(j);
					}
				
			}			
		}
		
		//----STEP 3
		
		//Get block corresponding to earliest arrival
		Block j = journeys.get(index).getNextToBeScheduled().getBlock();
		
		for(Journey jou: journeys){
			if(!jou.isScheduled()  && !(jou.firstBlock() && jou.getNextToBeScheduled().getBlock().isOccupied())){
				
				BlockOccupation bo = jou.getNextToBeScheduled();
				Block jstar = bo.getBlock();
				
				
				//If next block is that of earliest arrival and depij < mav
				if(j == jstar && bo.getArrTime() <= firstArrival){
				
					double prevNextEntry = j.getNextPossibleEntry();
					double prevLastEntry = 0;
					
					
					System.out.println("---CREATING NODE---");
					System.out.println("Updating block " + bo.getBlock().getID() + " for " + bo.getTrain().getName());
					
					//If not the first block
					if(!jou.firstBlock())
						//If arrived at 0 (I.e. Waited in last block)
						if(bo.getArrSpeed() == 0)
							//Unoccupy previous block
							jou.getPreviousBlock().getBlock().setOccupied(false);
					
					
					//Update last arrival time for the block
					j.setNextPossibleEntry(bo.getDepTime());
					
					//Set this block to occupied if we halt at end of block
					if(bo.getDepSpeed() != 0)
						bo.getBlock().setOccupied(false);
					else
						bo.getBlock().setOccupied(true);
					
					if(!jou.lastBlock()){
						
						//Not the last block so update entry into next block
						BlockOccupation bo2 = jou.getSecondToBeScheduled();
						prevLastEntry = bo2.getBlock().getLastEntry();
						
						System.out.println("Updating next block " + bo2.getBlock().getID());
						bo2.setArrTime(bo.getDepTime());
						bo2.setArrSpeed(bo.getDepSpeed());
						bo2.getBlock().setLastEntry(bo.getDepTime());
						
						//If we enter the next block, occupy it
						if(bo.getDepSpeed() != 0)
							bo2.getBlock().setOccupied(true);
					}
					
					//Move to next block of this journey
					jou.incrementJourney();
					
					//Indicate not to reset arrival details in new node for this journey
					jou.setToBeWiped(false);
					
					if(allJourneysScheduled()){
						for(Block b: this.blocks)
							b.printBlockDetail();
						System.out.println("-----COMPLETE SCHEDULE-----");
						
						for(Journey journey : journeys){
							System.out.println("\n" + journey.getTrain().getName() + " schedule");
							for(BlockOccupation b : journey.getBlockOccupations())
								b.printBlockDetail();
						}
							
					}else{
						Scheduler s = new Scheduler(journeys, blocks, trains);
						s.schedule();
					}
					
					//undo alterations
					jou.decrementJourney();
					jou.setToBeWiped(true);
					j.setNextPossibleEntry(prevNextEntry);
					
					if(!jou.lastBlock()){
						//Not the last block so update entry into next block
						BlockOccupation bo2 = jou.getSecondToBeScheduled();
						System.out.println("Resetting block " + bo2.getBlock().getID());
						bo2.setArrTime(Integer.MAX_VALUE);
						bo2.setArrSpeed(0);
						bo2.getBlock().setLastEntry(prevLastEntry);
						
						//If we entered the next block, indicate that it is actually unoccupied
						if(bo.getDepSpeed() != 0)
						bo2.getBlock().setOccupied(false);
					}
				}
			}
					
			
		}
		
		
		
		
		
	}
	
	private boolean allJourneysScheduled(){
		
		//Return false if any journey is not fully scheduled
		for(Journey j: journeys)
			if(!j.isScheduled())
				return false;
		
		return true;
	}
	
	public void printJourneys(){
		for(Journey j: journeys)
			for(BlockOccupation b: j.getBlockOccupations()){
				b.printBlockDetail();
			}
		
		for(Block b: blocks){
			b.printBlockDetail();
		}
	}
	
	
	
}
