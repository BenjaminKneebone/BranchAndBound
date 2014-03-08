package algorithms;

import java.util.ArrayList;
import java.util.Collections;
import entities.BlockOccupation;

import entities.Block;
import entities.Engine;
import entities.Join;
import entities.Network;
import exceptions.NoOutsFromInException;
import exceptions.RouteNotFoundException;

public class Dijkstra {

	private ArrayList<Block> blocks;
	private ArrayList<ArrayList<Join>> joins;
	private ArrayList<Join> prevJoins;
	
	public Dijkstra(Network network){
		blocks = network.getBlocks();
		joins = network.getJoins();
		prevJoins = new ArrayList<Join>(blocks.size());
		for(int x = 0; x < blocks.size(); x++)
			prevJoins.add(null);
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
				
		//Get the joins associated with the first block
		activeJoins.addAll(joins.get(sourceID));
		
		for(Join j: activeJoins){
			//Set distance to first join
			j.setMinDistance(blocks.get(sourceID).getLength());
			j.setCurrentIn(sourceID);
		}
		
		//Whilst we have active joins
		while(activeJoins.size() > 0){
			
			//If we have encountered the destination and no shorter possible - Finished
			if(destFound && checkComplete(activeJoins, globalMin))
				break;
			
			ArrayList<Join> newActive = new ArrayList<Join>();
			
			//For each join on the "front"
			for(Join oldJ: activeJoins){
				
				//Get dest blocks
				try {
					for(Block b: oldJ.getOuts(blocks.get(oldJ.getCurrentIn()))){

						//See if better route found to destination
						if(b.getID() == destID){
							destFound = true;
							if(oldJ.getMinDistance() + b.getLength() < globalMin){
								globalMin = oldJ.getMinDistance() + b.getLength();
								prevJoins.set(destID, oldJ);
							}
						}else{
							for(Join newJ : joins.get(b.getID())){
								
								//Check if quicker route has been found to this join
								if(newJ.getMinDistance() > oldJ.getMinDistance() + b.getLength()){
									//Set new minimum distance values and add join to the "Front"
									newJ.setMinDistance(oldJ.getMinDistance() + b.getLength());
									newJ.setCurrentIn(b.getID());
									prevJoins.set(b.getID(), oldJ);
									newActive.add(newJ);
								}
							}
						}
					}
				} catch (NoOutsFromInException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		
				
			}
		
			activeJoins = newActive;
		}
		
		//Reset min distance for the joins
		for(ArrayList<Join> aj: joins)
			for(Join j: aj)
				j.setMinDistance(10000000);
		
		if(globalMin == 10000000){
			throw new RouteNotFoundException(sourceID, destID);
		}else{
			//Solution found - create route
			ArrayList<BlockOccupation> route = new ArrayList<BlockOccupation>();
			
			//Get join of the destination
			Join last = prevJoins.get(destID);
			
			route.add(new BlockOccupation(train, blocks.get(destID)));
			
			//Loop through adding blocks to root
			while(last.getCurrentIn() != sourceID){
				route.add(new BlockOccupation(train, blocks.get(last.getCurrentIn())));
				last = prevJoins.get(last.getCurrentIn());
			}
			
			//Add source block
			route.add(new BlockOccupation(train, blocks.get(sourceID)));
			
			//reverse route (Now source to destination)
			Collections.reverse(route);
						
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
