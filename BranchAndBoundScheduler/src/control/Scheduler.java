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
		
		//Clone BlockOccupations
		
		
		//Clone journeys, pass in newly cloned blocks
		ArrayList<Journey> newJournies = new ArrayList<Journey>();
		for(Journey j: journeys)
			newJournies.add(j.clone(blocks));
		
		this.journeys = newJournies;		
			
		//No need to clone trains
		this.trains = trains;
	}
	
	public void schedule(){
		
		//Details for earliest block exit for this node
		int index = 0;
		double firstArrival = Integer.MAX_VALUE;
		
		//Schedule next block for each train
		for(Journey j : journeys){
			
			//Get first block to be scheduled on journey
			BlockOccupation firstBlock = j.getNextToBeScheduled();
			//Get second block
			BlockOccupation secondBlock = j.getSecondToBeScheduled();
			Engine train = firstBlock.getTrain();
			BlockExit b = null;
			
			//If second block has enough space for train to enter at full speed
			if(train.canStopInBlock(firstBlock.getBlock())){
				
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
		
		BlockOccupation firstExitBlock = journeys.get(index).getNextToBeScheduled();
		BlockOccupation firstExitNextBlock = journeys.get(index).getSecondToBeScheduled();
		
		firstExitNextBlock.setArrSpeed(firstExitBlock.getDepSpeed());
		firstExitNextBlock.setArrTime(firstExitBlock.getDepTime());
		
		//Indicate BlockOccupation as scheduled
		journeys.get(index).incrementJourney();
		
		//Reset all other BlockOccupations
		for(int x = 0; x < journeys.size(); x++){
			if(x != index){
				BlockOccupation resetBlockOcc = journeys.get(x).getNextToBeScheduled();
				resetBlockOcc.setDepTime(0);
				resetBlockOcc.setDepTime(0);
			}
		}
	}
	
	
	
	public void printJournies(){
		for(Journey j: journeys)
			for(BlockOccupation b: j.getBlockOccupations()){
				b.printBlockDetail();
			}
		
		for(Block b: blocks){
			b.printBlockDetail();
		}
	}
	
	
	
}
