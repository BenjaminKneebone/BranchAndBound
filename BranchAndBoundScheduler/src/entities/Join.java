package entities;

import java.util.ArrayList;

import exceptions.NoOutsFromInException;

public class Join {

	private ArrayList<Connection> connections = new ArrayList<Connection>();
	//Large number (Initialisation of shortest route calculation)
	private int minDistance = 10000000;

	private int currentIn;
	
	public int getCurrentIn() {
		return currentIn;
	}

	public void setCurrentIn(int currentIn) {
		this.currentIn = currentIn;
	}

	public Join(){	
	}
	
	/**
	 * @param in The entry block into this join 
	 * @return A list of the blocks where valid connections exist from the in block across this join.
	 * @throws NoOutsFromInException
	 */
	public ArrayList<Block> getOuts(Block in) throws NoOutsFromInException{
		ArrayList<Block> outs = new ArrayList<Block>();
		
		for(Connection c: connections){
			if(c.getIn() == in)
				outs.add(c.getOut());
		}
		
		if(outs.size() == 0){
			throw new NoOutsFromInException(in.getID());
		}else{
			return outs;
		}
	}
	
	/**
	 * @return All the blocks that a train can be in before traversing this join. 
	 */
	public ArrayList<Block> getIns(){
		ArrayList<Block> ins = new ArrayList<Block>();
		
		for(Connection c: connections){
			//Avoid duplicating blocks that have multiple connections across join
			if(!ins.contains(c.getIn()))
				ins.add(c.getIn());
		}
			
		return ins;
	}
	
	public void printConnections(int in){
		for(Connection c: connections){
			if(c.getIn().getID() == in)
				System.out.println(in  + " connects to " + c.getOut().getID());
		}
	}
	
	public void addConnection(Connection c){
		connections.add(c);
	}
	
	/**
	 * @param dist Set the minimum distance to this Join (In metres) (Used in Dijkstra)
	 */
	public void setMinDistance(int dist){
		minDistance = dist;
	}
	
	/**
	 * @return Get the minimum distance found to this Join (In metres)
	 */
	public int getMinDistance(){
		return minDistance;
	}
	
	/**
	 * Set routing variables (previous Join and minimum distance to initial state). Does not
	 * change source or destination Blocks
	 */
	public void resetJoin(){
		minDistance = 10000000;
	}	
}
