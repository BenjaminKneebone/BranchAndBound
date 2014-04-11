package ownalgorithm;

import java.util.ArrayList;

import entities.BlockOccupation;
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
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
