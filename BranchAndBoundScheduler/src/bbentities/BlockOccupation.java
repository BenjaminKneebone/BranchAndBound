package bbentities;

import entities.Block;
import entities.Engine;

public class BlockOccupation implements Cloneable{

	private Engine train;
	private Block block;
	private int depTime = 0;
	private int arrTime = 0;
	
	public BlockOccupation(Engine train, Block block, int depTime, int arrTime){
		this.train = train;
		this.block = block;
		this.depTime = depTime;
		this.arrTime = arrTime;
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
	
	public BlockOccupation clone(){
		BlockOccupation bo = new BlockOccupation(train, block, depTime, arrTime);
		return bo;
	}
	
	
}
