package bbentities;

import java.util.ArrayList;

import entities.Block;
import entities.Engine;

public class BlockOccupation implements Cloneable{

	private Engine train;
	private Block block;
	//Speed entering block
	private int arrSpeed = 0;
	//Speed leaving block
	private int depSpeed = 0;
	
	//Time leaving block
	private double depTime = 0;
	//Time entering block
	private double arrTime = 0;
	
	
	public BlockOccupation(Engine train, Block block, double depTime, double arrTime, int arrSpeed, int depSpeed){
		this.train = train;
		this.block = block;
		this.depTime = depTime;
		this.arrTime = arrTime;
		this.depSpeed = depSpeed;
		this.arrSpeed = arrSpeed;
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

	public double getDepTime() {
		return depTime;
	}

	public void setDepTime(double depTime) {
		this.depTime = depTime;
	}

	public double getArrTime() {
		return arrTime;
	}

	public void setArrTime(double arrTime) {
		this.arrTime = arrTime;
	}
	
	public Block getBlock() {
		return block;
	}
	
	public Engine getTrain(){
		return train;
	}
	
	public void printBlockDetail(){
		/*
		System.out.println("------------------------------------------------------------");
		System.out.println("Train " + train.getID() + " using block " + block.getID());
		System.out.println("Arrives in block at " + arrTime + " at speed " + arrSpeed);
		System.out.println("Departs at " + depTime + " at speed " + depSpeed);
		*/
		
		//System.out.printf("%.1f / %.1f ->", arrTime, depTime);
		System.out.println(arrTime + "/" + depTime);
	}
	
	public BlockOccupation clone(ArrayList<Block> blocks){
		//Clone BlockOccupation using reference to block in list provided
		BlockOccupation bo = new BlockOccupation(train, blocks.get(block.getID()), depTime, arrTime, arrSpeed, depSpeed);
		return bo;
	}
	
	
}
