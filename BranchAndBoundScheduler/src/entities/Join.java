package entities;

import java.util.ArrayList;

public class Join {

	private Block source;
	//Null if no connection from source
	private ArrayList<Block> dest;
	//Large number (Initialisation of shortest route calculation)
	private int minDistance = 10000000;
	//Used in Dijkstra to indicate previous join in route (forms chain)
	private Join prevJoin;
	
	/**
	 * @param source The Block leading into the Join
	 * @param dest The Blocks leading out of the Join
	 */
	public Join(Block source, ArrayList<Block> dest){
		this.source = source;
		this.dest = dest;
	}
	
	/**
	 * @return The Block leading into this Join
	 */
	public Block getSource(){
		return source;
	}
	
	/**
	 * @return The Blocks leading out of this Join
	 */
	public ArrayList<Block> getDest(){
		return dest;
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
	 * @param prev The Join to be set as the Join before this one in some route
	 */
	public void setPrevJoin(Join prev){
		prevJoin = prev;
	}
	
	/**
	 * @return The Join before this join
	 */
	public Join getPrevJoin(){
		return prevJoin;
	}
	
	/**
	 * Set routing variables (previous Join and minimum distance to initial state). Does not
	 * change source or destination Blocks
	 */
	public void resetJoin(){
		prevJoin = null;
		minDistance = 10000000;
	}	
}
