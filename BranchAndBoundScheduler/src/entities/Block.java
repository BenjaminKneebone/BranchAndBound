package entities;

import java.util.ArrayList;

public class Block implements Cloneable {

	private int id;
	private int length;
	private int lastArrival;
	private ArrayList<Engine> sequence = new ArrayList<Engine>();
	
	public Block(int id, int length, int lastArrival){
		this.id = id;
		this.length = length;
		this.lastArrival = lastArrival;
	}
	
	public int getID(){
		return id;
	}
	
	public int getLength(){
		return length;
	}
	
	public void addToSequence(Engine eng){
		sequence.add(eng);
	}
	
	public Block clone(){
		Block b = new Block(id, length, lastArrival);
		return b;
	}
	
}
