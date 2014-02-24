package algorithms;

import java.util.ArrayList;
import java.util.Collections;

import bbentities.BlockOccupation;

import entities.Block;
import entities.Engine;
import entities.Join;
import entities.Network;
import exceptions.RouteNotFoundException;

public class Dijkstra {

	private ArrayList<Block> blocks;
	private ArrayList<Join> joins;
	
	public Dijkstra(Network network){
		blocks = network.getBlocks();
		joins = network.getJoins();
	}
	
	/**
	 * 
	 * @param sourceID Id of first block
	 * @param destID Id of final block
	 * @param train train that will conduct this route
	 * @return A List of BlockOccupations representing the quickest route between destination and source
	 * @throws RouteNotFoundException
	 */
	public ArrayList<BlockOccupation> shortestRoute(int sourceID, int destID, Engine train) throws RouteNotFoundException{
			
		//Large number so found routes are smaller
		int globalMin = 10000000;
		boolean destFound = false;
		
		//The "Front"
		ArrayList<Join> activeJoins = new ArrayList<Join>();
				
		//Get the first join
		activeJoins.add(joins.get(sourceID));
		
		//Set distance to first join
		joins.get(sourceID).setMinDistance(blocks.get(sourceID).getLength());
		//Set this join to beginning of the chain
		joins.get(sourceID).setPrevJoin(null);	
		
		//Whilst we have active joins
		while(activeJoins.size() > 0){
			
			//If we have encountered the destination and no shorter possible - Finished
			if(destFound && checkComplete(activeJoins, globalMin))
				break;
			
			ArrayList<Join> newActive = new ArrayList<Join>();
			
			//For each join on the "front"
			for(Join oldJ: activeJoins){
				
				//If not a terminal
				if(oldJ.getDest() != null){
					
					//Get dest blocks
					for(Block b: oldJ.getDest()){
						
						//See if better route found to destination
						if(b.getID() == destID){
							destFound = true;
							if(oldJ.getMinDistance() + b.getLength() < globalMin){
								globalMin = oldJ.getMinDistance() + b.getLength();
								joins.get(destID).setPrevJoin(oldJ);
							}
						}else{
							Join newJ = joins.get(b.getID());
							
							//Check if quicker route has been found to this join
							if(newJ.getMinDistance() > oldJ.getMinDistance() + b.getLength()){
								//Set new minimum distance values and add join to the "Front"
								newJ.setMinDistance(oldJ.getMinDistance() + b.getLength());
								newJ.setPrevJoin(oldJ);
								newActive.add(newJ);
							}
						}
					}		
				}
			}
		
			activeJoins = newActive;
		}
		
		//Reset min distance for the joins
		for(Join j: joins)
			j.setMinDistance(10000000);
		
		if(globalMin == 10000000){
			//No solution found - reset joins
			for(Join j: joins)
				j.setPrevJoin(null);
			
			throw new RouteNotFoundException(sourceID, destID);
		}else{
			//Solution found - create route
			ArrayList<BlockOccupation> route = new ArrayList<BlockOccupation>();
			
			//Get join of the destination
			Join last = joins.get(destID);
			
			//Loop through adding blocks to root
			while(last.getPrevJoin() != null){
				route.add(new BlockOccupation(train, last.getSource()));
				last = last.getPrevJoin();
			}
			
			//Add source block
			route.add(new BlockOccupation(train, blocks.get(sourceID)));
			
			//reverse route (Now source to destination)
			Collections.reverse(route);
			
			//Reset joins
			for(Join j: joins)
				j.setPrevJoin(null);
			
			return route;
		}
	}
	
	private boolean checkComplete(ArrayList<Join> active, int globalMin){
		
		for(Join j: active){
			if(j.getMinDistance() < globalMin)
				return false;
		}
		
		return true;
	}
}
