package algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
	//private ArrayList<Connection> prevConn;
	
	private Map<Connection, Connection> prevConn;
	private Map<Connection, Integer> minDistance = new HashMap<Connection, Integer>();
	
	public Dijkstra(Network network){
		blocks = network.getBlocks();
		joins = network.getJoins();
		prevConn = new HashMap<Connection, Connection>(blocks.size());	
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
		
		//The last connection in a route
		Connection lastConn = null;
		
		//Set high distance for each connection (As some connections will appear in 
		//multiple lists, will perform unnecessary puts
		for(ArrayList<Join> joinList: joins)
			for(Join j: joinList)
				for(Connection c: j.getConnections())
					minDistance.put(c, 1000000);
		
		prevConn = new HashMap<Connection, Connection>(minDistance.size());
		
		//Large number so found routes are smaller
		int globalMin = 10000000;
		boolean destFound = false;
		
		//The "Front"
		ArrayList<Connection> activeConnections = new ArrayList<Connection>();
				
		//Get the joins associated with the first block
		for(Join j: joins.get(sourceID)){
			try {
				for(Connection c: j.getConnections(blocks.get(sourceID))){
					activeConnections.add(c);
					minDistance.put(c, 0);
					prevConn.put(c, null);
				}
			} catch (NoOutsFromInException e) {
				
			}
		}

		//Whilst we have active joins
		while(activeConnections.size() > 0){
			
			//If we have encountered the destination and no shorter possible - Finished
			if(destFound && checkComplete(activeConnections, globalMin))
				break;
			
			ArrayList<Connection> newActive = new ArrayList<Connection>();
			
			//For each join on the "front"
			for(Connection oldC: activeConnections){
				
				//Get dest block
				Block out = oldC.getOut();
				if(out.getID() == destID){
					destFound = true;
					if(minDistance.get(oldC) + out.getLength() < globalMin){
						System.out.println("Best distance found");
						globalMin = minDistance.get(oldC) +  out.getLength();
						lastConn = oldC;
					}
				}else{
					for(Join j: joins.get(out.getID())){
						try {
							for(Connection c: j.getConnections(out)){
								//Check if quicker route has been found to this Connection
								if(minDistance.get(oldC) + out.getLength() < minDistance.get(c)){
									//Set new minimum distance values and add join to the "Front"
									minDistance.put(c, minDistance.get(oldC) + out.getLength());
									prevConn.put(c, oldC);
									newActive.add(c);
								}
							}
						} catch (NoOutsFromInException e) {
						}	
					}
				}
			}
			
		
			activeConnections = newActive;
			
			Collections.sort(activeConnections, new ConnectionComparator());
		}
				
		
		if(lastConn == null){
			throw new RouteNotFoundException(sourceID, destID);
		}else{
			//Solution found - create route
			ArrayList<BlockOccupation> route = new ArrayList<BlockOccupation>();
								
			route.add(new BlockOccupation(train, blocks.get(destID), lastConn, false));
			//Loop through adding blocks to root
			while(prevConn.get(lastConn) != null){
				
				//If same Join, train has turned around in block
				if(lastConn.getJoin() == prevConn.get(lastConn).getJoin()){
					route.add(new BlockOccupation(train, blocks.get(prevConn.get(lastConn).getOut().getID()), prevConn.get(lastConn), true));
				}else{
					route.add(new BlockOccupation(train, blocks.get(prevConn.get(lastConn).getOut().getID()), prevConn.get(lastConn), false));
				}
				
				lastConn = prevConn.get(lastConn);
				
				if(prevConn.get(lastConn) == null)
					break;
				
			}
			
			//Add source block
			route.add(new BlockOccupation(train, blocks.get(sourceID), null, false));
			
			//reverse route (Now source to destination)
			Collections.reverse(route);
						
			return route;
		}
		
	}
	
	private boolean checkComplete(ArrayList<Connection> active, int globalMin){
		
		for(Connection c: active){
			if(minDistance.get(c) < globalMin)
				return false;
		}
		
		return true;
	}
	
	private class ConnectionComparator implements Comparator<Connection>{

		@Override
		public int compare(Connection o1, Connection o2) {
			return minDistance.get(o1) < minDistance.get(o2) ? -1 : minDistance.get(o1) == minDistance.get(o2) ? 0 : 1;
		}
		
	}
}