package control;

import java.util.ArrayList;

import entities.Block;
import entities.BlockExit;
import entities.Engine;
import exceptions.InvalidSpeedException;

import bbentities.BlockOccupation;
import bbentities.Journey;

public class Scheduler {

	private ArrayList<Journey> journies;
	private ArrayList<Block> blocks;
	private ArrayList<Engine> trains;
	
	public Scheduler(ArrayList<Journey> journies, ArrayList<Block> blocks, ArrayList<Engine> trains){
		
		//Clone blocks
		ArrayList<Block> newBlocks = new ArrayList<Block>();
		for(Block b: blocks)
			newBlocks.add(b.clone());
		
		this.blocks = newBlocks;
		
		//Clone journies, pass in newly cloned blocks
		ArrayList<Journey> newJournies = new ArrayList<Journey>();
		for(Journey j: journies)
			newJournies.add(j.clone(blocks));
		
		this.journies = newJournies;		
			
		//No need to clone trains
		this.trains = trains;
	}
	
	public void schedule(){
		
		for(Journey j : journies){
			
			BlockOccupation currentBlock = j.getBlockOccupations().get(0);
			BlockOccupation nextBlock = j.getBlockOccupations().get(1);
			Engine train = currentBlock.getTrain();
			
			boolean canStopInNextBlock = train.canStopInBlock(currentBlock.getBlock());
			
			if(canStopInNextBlock){
				
				//Full speed ahead
				try {
					BlockExit b = train.timeToTraverse(currentBlock.getBlock(), currentBlock.getArrSpeed());
					//Time leaving block
					currentBlock.setDepTime(currentBlock.getArrTime() + b.getTime());
					//Speed leaving block
					currentBlock.setDepSpeed(b.getSpeed());
				} catch (InvalidSpeedException e) {
					e.printStackTrace();
				}
			}else{
				
				Block nextBlockcocklotsandlotsofcock = null;
				int depSpeed = train.highestBlockEntrySpeed(nextBlockcocklotsandlotsofcock);
				
				
				
				
				
				
				
				
				
			}
			
			
			
			
			
		}
		
		
		
		
		
		
		
		
		
		
		
	}
	
	
	
	public void printJournies(){
		for(Journey j: journies)
			for(BlockOccupation b: j.getBlockOccupations()){
				b.printBlockDetail();
			}
		
		for(Block b: blocks){
			b.printBlockDetail();
		}
	}
	
	
	
}
