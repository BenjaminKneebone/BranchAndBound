package entities;

import java.util.ArrayList;

public class Block{

	private int id = -1;
	private int length = -1;
	private ArrayList<Block> restrict = new ArrayList<Block>();
	private boolean bidirectional = false;
	
	public boolean isBidirectional() {
		return bidirectional;
	}

	public void setBidirectional(boolean bidirectional) {
		this.bidirectional = bidirectional;
	}

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
	
	public void printBlockDetail(){
		System.out.println("Block " + id);
		System.out.println("Bidirectional " + bidirectional);
		System.out.print("Restricted Blocks: ");
		for(Block b: restrict)
			System.out.print(b.getID() + " ");
		
		System.out.print("\n");
		
	}
	
	public void addRestrictedBlock(Block b){
		restrict.add(b);
	}
	
	public ArrayList<Block> getRestrictedBlocks(){
		return restrict;
	}
}
