package entities;

public class Stop {

	private int blockID = -1;
	private int stopTime = -1;
	
	public Stop(int blockID, int stopTime){
		this.blockID = blockID;
		this.stopTime = stopTime;
	}

	public int getBlockID() {
		return blockID;
	}

	public int getStopTime() {
		return stopTime;
	}
	
	
	
}
