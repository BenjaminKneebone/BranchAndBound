package entities;

public class Block{

	private int id = -1;
	private int length = -1;
	
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
	}
}
