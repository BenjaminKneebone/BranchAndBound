package entities;

import java.util.ArrayList;

public class Block {

	private int id;
	private int length;
	private ArrayList<Engine> sequence = new ArrayList<Engine>();
	
	public Block(int id, int length){
		this.id = id;
		this.length = length;
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
	
}
