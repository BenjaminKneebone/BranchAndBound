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
	private double depTime = Integer.MAX_VALUE;
	//Time entering block
	private double arrTime = Integer.MAX_VALUE;
	
	private int stationStopTime = 0;
	private double stationArrivalTime = -1;
	
	private String message;
	
	public BlockOccupation(Engine train, Block block){
		this.train = train;
		this.block = block;
	}
	
	private BlockOccupation(Engine train, Block block, double depTime, double arrTime, int arrSpeed, int depSpeed, double stationArrivalTime, int stationStopTime, String message){
		this.train = train;
		this.block = block;
		this.depTime = depTime;
		this.arrTime = arrTime;
		this.depSpeed = depSpeed;
		this.arrSpeed = arrSpeed;
		this.stationStopTime = stationStopTime;
		this.stationArrivalTime = stationArrivalTime;
		this.message = message;
	}
	
	public BlockOccupation clone(ArrayList<Block> blocks){
		//Clone BlockOccupation using reference to block in list provided
		BlockOccupation bo = new BlockOccupation(train, blocks.get(block.getID()), depTime, arrTime, arrSpeed, depSpeed, stationArrivalTime, stationStopTime, message);
		return bo;
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
		this.stationArrivalTime = depTime;
		this.depTime = depTime + stationStopTime;
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
	
	public boolean isStation(){
		return stationStopTime > 0;
	}
	
	public void setStationStopTime(int time){
		this.stationStopTime = time;
	}
		
	public double getStationArrivalTime(){
		return stationArrivalTime;
	}
	
	public void printBlockDetail(){
		System.out.println(arrTime + "/" + depTime + " in block " + block.getID() + " Arr Speed: " + arrSpeed + " Dep Speed: " + depSpeed);
	}
	
	public String getBlockOccupationDetail(){
		if(!isStation())
			return String.format("Block %d Arriving %-8.4f (%-3dkm/h) Departing %-8.4f (%-3dkm/h) \n", block.getID(), arrTime, arrSpeed,  depTime, depSpeed); 
		else 
			return String.format("Block %d Arriving %-8.4f (%-3dkm/h) Departing %-8.4f (%-3dkm/h) Station Arrival: %-8.4f \n", block.getID(), arrTime, arrSpeed,  depTime, depSpeed, stationArrivalTime); 
	}
	
	public void setMessage(String msg){
		this.message = msg;
	}
	
	public String getMessage(){
		return message;
	}
	
}
