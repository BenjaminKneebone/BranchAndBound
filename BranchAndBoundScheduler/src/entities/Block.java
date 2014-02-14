package entities;

import java.util.ArrayList;

public class Block implements Cloneable {

	private int id;
	private int length;
	private double nextPossibleEntry;
	private double lastEntry = 0;
	public boolean occupied = false;
	private ArrayList<Engine> sequence = new ArrayList<Engine>();
	
	public Block(int id, int length){
		this.id = id;
		this.length = length;
	}
	
	public Block(int id, int length, double lastEntry, double nextPossibleEntry, boolean occupied){
		this.id = id;
		this.length = length;
		this.lastEntry = lastEntry;
		this.nextPossibleEntry = nextPossibleEntry;
		this.occupied = occupied;
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
	
	public boolean isOccupied(){
		return occupied;
	}
	
	public void setOccupied(boolean occupied){
		this.occupied = occupied;
	}
	
	public void printBlockDetail(){
		System.out.println("Block " + id + " Last Arrival: " + lastEntry + " Last Departure: " + nextPossibleEntry + " occupied: " + occupied);
	}
	
	public Block clone(){
		Block b = new Block(id, length, lastEntry, nextPossibleEntry, occupied);
		return b;
	}
	
}
