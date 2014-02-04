package control;

import java.util.ArrayList;

import entities.Block;
import entities.Engine;

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
