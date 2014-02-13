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
		
		//Clone blocks
		ArrayList<Block> newBlocks = new ArrayList<Block>();
		for(Block b: blocks)
			newBlocks.add(b.clone());
		
		this.blocks = newBlocks;		
		
		//Clone journeys, pass in newly cloned blocks
		ArrayList<Journey> newJournies = new ArrayList<Journey>();
		for(Journey j: journeys)
			newJournies.add(j.clone(blocks));
		
		this.journeys = newJournies;	
		
		//Wipe all arrival times (Last line of step 2)
		for(Journey j: journeys){
			j.getNextToBeScheduled().setArrSpeed(0);
			j.getNextToBeScheduled().setArrTime(0);
		}
			
		//No need to clone trains
		this.trains = trains;
	}
	
	public void schedule(){
		
		System.out.println("Scheduling");
		
		//Details for earliest block exit for this node
		int index = 0;
		double firstArrival = Integer.MAX_VALUE;
		
		//Schedule next block for each train
		for(Journey j : journeys){
			
			if(!j.isScheduled()){
				//Journey not completed
				
				//Get first block to be scheduled on journey
				BlockOccupation firstBlock = j.getNextToBeScheduled();
				System.out.println("Arrival Speed " + firstBlock.getArrSpeed());
				Engine train = firstBlock.getTrain();
				BlockExit b = null;
				
				System.out.println("Scheduling " + train.getName() + " in block " + firstBlock.getBlock().getID());
				
				if(j.lastBlock()){
					System.out.println("Last block");
					
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
					
					System.out.println("The next block for " + train.getName() + " in block " + secondBlock.getBlock().getID());
					
					//If second block has enough space for train to enter at full speed
					if(train.canStopInBlock(secondBlock.getBlock())){
						
						//Full speed ahead
						try {
							b = train.timeToTraverse(firstBlock.getBlock(), firstBlock.getArrSpeed());
						} catch (InvalidSpeedException e) {
							e.printStackTrace();
						}
					}else{
						
						//Train must leave block at reduced speed
						int depSpeed = train.highestBlockEntrySpeed(secondBlock.getBlock());
						
						try {
							b = train.exitBlockAtSetSpeed(firstBlock.getBlock(), firstBlock.getArrSpeed(), depSpeed);
						} catch (InvalidSpeedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				
					//If train would get to next block while its still occupied
					if(firstBlock.getArrTime() + b.getTime() < secondBlock.getBlock().getNextPossibleEntry()){
						
						//Train needs to be slowed down
			
						//minimum time train needs to spend in block
						double timeInBlock = secondBlock.getBlock().getNextPossibleEntry() - firstBlock.getArrTime();
						
						//calculate speed and time so train arrives when next block is unoccupied
						try {
							b = train.minimumTimeTraversal(firstBlock.getBlock(), firstBlock.getArrSpeed(), timeInBlock);
						} catch (InvalidSpeedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				
				//Time leaving block
				firstBlock.setDepTime(firstBlock.getArrTime() + b.getTime());
				//Speed leaving block
				firstBlock.setDepSpeed(b.getSpeed());
				
				System.out.println("Train " + train.getName() + " takes " + b.getTime() + " in block " + firstBlock.getBlock().getID() + " leaving at " + firstBlock.getDepTime() + " at " + b.getSpeed() + "km/h");
				
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
		
		System.out.println("The first arrival is " + firstArrival + " within journey " + index + " at block " + j.getID());
		
		for(Journey jou: journeys){
			
			BlockOccupation bo = jou.getNextToBeScheduled();
			Block jstar = bo.getBlock();
			
			System.out.println(j);
			System.out.println(jstar);
			System.out.println(bo.getArrTime());
			System.out.println(j.getNextPossibleEntry());
			
			//If next block is that of earliest arrival and depij < mav
			if(j == jstar && bo.getArrTime() <= firstArrival){
			
				System.out.println("Same block");
				
				//Update last arrival time for the block
				j.setNextPossibleEntry(bo.getDepTime());
				
				if(!jou.lastBlock()){
					System.out.println("Update next block");
					//Not the last block so update entry into next block
					BlockOccupation bo2 = jou.getSecondToBeScheduled();
					bo2.setArrTime(bo.getDepTime());
					bo2.setArrSpeed(bo.getDepSpeed());
					System.out.println(bo2.getArrSpeed());
					bo2.getBlock().setLastEntry(bo.getDepTime());
				}
				
				jou.incrementJourney();
				
				
				if(allJourneysScheduled()){
					System.out.println("Schedule complete");
					
				}else{
					Scheduler s = new Scheduler(journeys, blocks, trains);
					s.schedule();
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
