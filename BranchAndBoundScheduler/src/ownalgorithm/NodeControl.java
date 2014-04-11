package ownalgorithm;

import java.util.ArrayList;

import entities.BlockOccupation;
import entities.Engine;
import entities.Journey;

public class NodeControl {

	private ArrayList<ArrayList<BlockOccupation>> occupied;
	private ArrayList<Journey> journeys;
	
	
	
	
	public NodeControl(ArrayList<Journey> journeys, int numBlocks){
		occupied = new ArrayList<ArrayList<BlockOccupation>>(numBlocks);
		this.journeys = journeys;
		
		//Add empty occupation lists for each block
		for(int x = 0; x < numBlocks; x++)
			occupied.add(new ArrayList<BlockOccupation>());		
		
		
		schedule();
		
		
	}
	
	
	private void schedule(){
		
		Engine train;
		
		for(Journey j: journeys){
		
			train = j.getTrain();
			
			for(BlockOccupation b: j.getBlockOccupations()){
			
				ArrayList<BlockOccupation> blockOccupied = occupied.get(b.getBlock().getID());
				
				int x = 0;
				boolean scheduled = false;
				
				while(!scheduled){
				
					while(true){
						//If time is during occupation x (Invalid arrival time)
						if(isTimeCollision(b.getArrTime(), blockOccupied.get(x))){
							//Not last block, back to back occupations in block
							if(x < blockOccupied.size() - 1 &&
									b.getArrTime() == blockOccupied.get(x+1).getArrTime()){
										//Iterate
										x++;
										continue;
							}else
									//Push back arrival time
									b.setArrTime(blockOccupied.get(x).getDepTime());
						}
				
						break;
					}
				
					b = 
		
					//Do scheduling
		
				
					
					
					
					
					
					
				}
				
			}
			
		}
		
	}
	
	private boolean isTimeCollision(double time, BlockOccupation blockOccupation){
		return time >= blockOccupation.getArrTime() && time < blockOccupation.getDepTime();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
}
