package entities;

import java.util.ArrayList;

public class Block implements Cloneable {

	private int id;
	private int length;
	private double nextPossibleEntry = Integer.MAX_VALUE;
	private double lastEntry = 0;
	private ArrayList<Engine> sequence = new ArrayList<Engine>();
	
	public Block(int id, int length){
		this.id = id;
		this.length = length;
	}
	
	public Block(int id, int length, double nextPossibleEntry){
		this.id = id;
		this.length = length;
		this.nextPossibleEntry = nextPossibleEntry;
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
	
	public void setNextPossibleEntry(double arrTime){
		this.nextPossibleEntry = arrTime;
	}
	
	public double getNextPossibleEntry(){
		return nextPossibleEntry;
	}
	
	public double getLastEntry(){
		return lastEntry;
	}
	
	public void setLastEntry(double lastEntry){
		this.lastEntry = lastEntry;
	}
	
	public void printBlockDetail(){
		System.out.println("Block " + id + " Last Arrival: " + nextPossibleEntry);
	}
	
	public Block clone(){
		Block b = new Block(id, length, nextPossibleEntry);
		return b;
	}
	
}
