package algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import entities.BlockOccupation;

import entities.Block;
import entities.Connection;
import entities.Engine;
import entities.Join;
import entities.Network;
import exceptions.NoOutsFromInException;
import exceptions.RouteNotFoundException;

public class Dijkstra {

	private ArrayList<Block> blocks;
	private ArrayList<ArrayList<Join>> joins;
	private ArrayList<Connection> prevConn;
	private Map<Join, Integer> minDistance = new HashMap<Join, Integer>();
	private Map<Join, Integer> currentIn = new HashMap<Join, Integer>();
	
	public Dijkstra(Network network){
		blocks = network.getBlocks();
		joins = network.getJoins();
		prevConn = new ArrayList<Connection>(blocks.size());
		
		
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
			
		//Set high distance for each join (As some joins will appear in 
		//multiple lists, will perform unnecessary puts
		for(ArrayList<Join> joinList: joins)
			for(Join j: joinList)
				minDistance.put(j, 1000000);
		
		prevConn = new ArrayList<Connection>(blocks.size());
		for(int x = 0; x < blocks.size(); x++)
			prevConn.add(null);
		
		currentIn = new HashMap<Join, Integer>();
		
		//Large number so found routes are smaller
		int globalMin = 10000000;
		boolean destFound = false;
		
		//The "Front"
		ArrayList<Join> activeJoins = new ArrayList<Join>();
				
		//Get the joins associated with the first block
		activeJoins.addAll(joins.get(sourceID));
		
		for(Join j: activeJoins){
			//Set distance to first join
			minDistance.put(j, blocks.get(sourceID).getLength());
			currentIn.put(j, sourceID);
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
					for(Connection c: oldJ.getConnections(blocks.get(currentIn.get(oldJ)))){

						//See if better route found to destination
						if(c.getOut().getID() == destID){
							destFound = true;
							if(minDistance.get(oldJ) + c.getOut().getLength() < globalMin){
								globalMin = minDistance.get(oldJ) +  c.getOut().getLength();
								prevConn.set(c.getOut().getID(), c);
							}
						}else{
							for(Join newJ : joins.get(c.getOut().getID())){
								
								//Do not check join in direction we've arrived from
								if(newJ != oldJ){
									//Check if quicker route has been found to this join
									if(minDistance.get(newJ) > minDistance.get(oldJ) + c.getOut().getLength()){
										//Set new minimum distance values and add join to the "Front"
										minDistance.put(newJ, minDistance.get(oldJ) + c.getOut().getLength());
										currentIn.put(newJ, c.getOut().getID());
										prevConn.set(c.getOut().getID(), c);
										newActive.add(newJ);
									}
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
				
		if(globalMin == 10000000){
			throw new RouteNotFoundException(sourceID, destID);
		}else{
			//Solution found - create route
			ArrayList<BlockOccupation> route = new ArrayList<BlockOccupation>();
			
			//Get join of the destination
			Connection first = prevConn.get(destID);
			Connection second = prevConn.get(first.getIn().getID()); 
								
			route.add(new BlockOccupation(train, blocks.get(destID), first));
			//Loop through adding blocks to root
			while(second != null){
				route.add(new BlockOccupation(train, first.getIn(), second));
				first = second;
				second = prevConn.get(second.getIn().getID());	
			}
			
			//Add source block
			route.add(new BlockOccupation(train, blocks.get(sourceID), null));
			
			//reverse route (Now source to destination)
			Collections.reverse(route);
						
			return route;
		}
	}
	
	private boolean checkComplete(ArrayList<Join> active, int globalMin){
		
		for(Join j: active){
			if(minDistance.get(j) < globalMin)
				return false;
		}
		
		return true;
	}
}
