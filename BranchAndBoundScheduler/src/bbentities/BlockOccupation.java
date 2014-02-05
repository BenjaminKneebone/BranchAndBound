package bbentities;

import java.util.ArrayList;

import entities.Block;
import entities.Engine;

public class BlockOccupation implements Cloneable{

	private Engine train;
	private Block block;
	private int depTime = 0;
	private int arrTime = 0;
	private int arrSpeed = 0;
	private int depSpeed = 0;
	
	public BlockOccupation(Engine train, Block block, int depTime, int arrTime){
		this.train = train;
		this.block = block;
		this.depTime = depTime;
		this.arrTime = arrTime;
	}
	
	public int getArrSpeed() {
		return arrSpeed;
	}

	public void setArrSpeed(int arrSpeed) {
		this.arrSpeed = arrSpeed;
	}

	public int getDepSpeed() {
		return depSpeed;
	}

	public void setDepSpeed(int depSpeed) {
		this.depSpeed = depSpeed;
	}

	public int getDepTime() {
		return depTime;
	}

	public void setDepTime(int depTime) {
		this.depTime = depTime;
	}

	public int getArrTime() {
		return arrTime;
	}

	public void setArrTime(int arrTime) {
		this.arrTime = arrTime;
	}
	
	public Block getBlock() {
		return block;
	}
	
	public Engine getTrain(){
		return train;
	}
	
	public void printBlockDetail(){
		System.out.println("Train " + train.getID() + " using block " + block.getID());
		System.out.println("Departing " + depTime + " Arriving " + arrTime);
	}
	
	public BlockOccupation clone(ArrayList<Block> blocks){
		//Clone BlockOccupation using reference to block in list provided
		BlockOccupation bo = new BlockOccupation(train, blocks.get(block.getID()), depTime, arrTime);
		return bo;
	}
	
	
}
