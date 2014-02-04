package entities;

import java.util.ArrayList;

public class Join {

	private Block source;
	//Null if no connection from source
	private ArrayList<Block> dest;
	int minDistance = 0;
	private Join prevJoin;
	
	public Join(Block source, ArrayList<Block> dest){
		this.source = source;
		this.dest = dest;
	}
	
	public Block getSource(){
		return source;
	}
	
	public ArrayList<Block> getDest(){
		return dest;
	}
	
	public void setMinDistance(int dist){
		minDistance = dist;
	}
	
	public int getMinDistance(){
		return minDistance;
	}
	
	public void setPrevJoin(Join prev){
		prevJoin = prev;
	}
	
	public Join getPrevJoin(){
		return prevJoin;
	}
	
	
}
