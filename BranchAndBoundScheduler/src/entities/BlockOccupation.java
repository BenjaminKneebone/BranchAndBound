package entities;

import java.util.ArrayList;

import entities.Block;
import entities.Engine;

public class BlockOccupation implements Cloneable{

	private Engine train;
	private Block block;
	private Connection connection;
	
	//Speed entering block
	private int arrSpeed = 0;
	//Speed leaving block
	private int depSpeed = 0;
	
	//Time entering block
	private double arrTime = Integer.MAX_VALUE;
	//Time leaving block
	private double depTime = Integer.MAX_VALUE;
	
	//Specified time to stop at station
	private int stationStopTime = 0;
	//Arrival Time at station
	private double stationArrivalTime = -1;
	
	private double timeToEnterBlock = 0;
	
	//Some information about BlockOccupation (Acceleration/Deceleration etc.)
	private String message;
	
	public BlockOccupation(Engine train, Block block, Connection conn){
		this.train = train;
		this.block = block;
		this.connection = conn;
	}
	
	private BlockOccupation(Engine train, Block block, Connection conn, double depTime, double arrTime, int arrSpeed, int depSpeed, double stationArrivalTime, int stationStopTime, double timeToEnterBlock, String message){
		this.train = train;
		this.block = block;
		this.depTime = depTime;
		this.arrTime = arrTime;
		this.depSpeed = depSpeed;
		this.arrSpeed = arrSpeed;
		this.stationStopTime = stationStopTime;
		this.stationArrivalTime = stationArrivalTime;
		this.message = message;
		this.connection = conn;
		this.timeToEnterBlock = timeToEnterBlock;
	}
	
	/**Clones this BlockOccupation. Clone will reference Block in the 
	 * list of blocks passed in.
	 * @param blocks
	 * @return
	 */
	public BlockOccupation clone(ArrayList<Block> blocks){
		//Clone BlockOccupation using reference to block in list provided
		BlockOccupation bo = new BlockOccupation(train, blocks.get(block.getID()), connection, depTime, arrTime, arrSpeed, depSpeed, stationArrivalTime, stationStopTime, timeToEnterBlock, message);
		return bo;
	}
	
	public double getTimeToEnterBlock(){
		return timeToEnterBlock;
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

	/**
	 * Sets station arrival time to depTime and sets Block departure time
	 * after time at station passes.
	 */
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
	
	public boolean isStation(){
		return stationStopTime > 0;
	}
	
	public void setStationStopTime(int time){
		this.stationStopTime = time;
	}
	
	public int getStationStopTime(){
		return stationStopTime;
	}
		
	public double getStationArrivalTime(){
		return stationArrivalTime;
	}
	
	public int getLength(){
		if(connection != null){
			return connection.getLength() + block.getLength();
		}else{
			return block.getLength();
		}
	}
	
	public void setStationArrivalTime(double time){
		this.stationArrivalTime = time;
	}
	
	public void setTimeToEnterBlock(double time){
		this.timeToEnterBlock = time;
	}
		
	public String getBlockOccupationDetail(){
		if(!isStation())
			return String.format("Block %d Arriving %-8.4f (%-3dkm/h) Departing %-8.4f (%-3dkm/h) \n", block.getID(), arrTime, arrSpeed,  depTime, depSpeed); 
		else 
			return String.format("Block %d Arriving %-8.4f (%-3dkm/h) Departing %-8.4f (%-3dkm/h) Station Arrival: %-8.4f \n", block.getID(), arrTime, arrSpeed,  depTime, depSpeed, stationArrivalTime); 
	}
	
	public void printOccupationDetail(){
		if(connection != null){
			System.out.println("Connection " + connection.getIn().getID() + " to " + connection.getOut().getID() + " length: " + connection.getLength());
		}
		System.out.printf("Train: %-3d Block: %-3d Length: %-5d Station: %s\n", train.getID(), block.getID(), block.getLength(), isStation());
	}
	
	public void setMessage(String msg){
		this.message = msg;
	}
	
	public String getMessage(){
		return message;
	}
	
	public double getTimeInBlock(){
		return depTime - stationStopTime - arrTime;
	}
	
}
