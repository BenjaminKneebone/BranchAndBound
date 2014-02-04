package bbentities;

import entities.Block;
import entities.Engine;

public class BlockOccupation {

	private Engine train;
	private Block block;
	private int depTime = 0;
	private int arrTime = 0;
	
	public BlockOccupation(Engine train, Block block){
		this.train = train;
		this.block = block;
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
	
	
}
