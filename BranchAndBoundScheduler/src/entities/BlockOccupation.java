package entities;

import java.util.ArrayList;

import entities.Block;
import entities.Engine;

public class BlockOccupation implements Cloneable{

	private Engine train;
	private Block block;
	private Connection connection;
	
	private boolean turnaround = false;
	
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
	
	private boolean startStation = false;
	private boolean endStation = false;
	
	
	//Some information about BlockOccupation (Acceleration/Deceleration etc.)
	private String message = "";
	
	public BlockOccupation(Engine train, Block block, Connection conn, boolean startStation, boolean endStation){
		this.train = train;
		this.block = block;
		this.connection = conn;
		this.startStation = startStation;
		this.endStation = endStation;
	}
	
	private BlockOccupation(Engine train, Block block, Connection conn, double depTime, double arrTime, int arrSpeed, int depSpeed, double stationArrivalTime, int stationStopTime, double timeToEnterBlock, String message, boolean turnaround, boolean startStation, boolean endStation){
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
		this.turnaround = turnaround;
		this.startStation = startStation;
		this.endStation = endStation;
	}
	
	/**Clones this BlockOccupation. Clone will reference Block in the 
	 * list of blocks passed in.
	 * @param blocks
	 * @return
	 */
	public BlockOccupation clone(ArrayList<Block> blocks){
		//Clone BlockOccupation using reference to block in list provided
		BlockOccupation bo = new BlockOccupation(train, blocks.get(block.getID()), connection, depTime, arrTime, arrSpeed, depSpeed, stationArrivalTime, stationStopTime, timeToEnterBlock, message, turnaround, startStation, endStation);
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
			if(turnaround)
				return connection.getLength() + train.getLength();
			else{
				if(endStation)
					return train.getLength();
				
				return connection.getLength() + block.getLength();
			}
				
		}else{
			
			if(startStation)
				return 0;
			
			return block.getLength();
		}
	}
	
	public int noConnLength(){
		if(startStation)
			return 0;
		else
			return block.getLength();
	}
	
	public void setStationArrivalTime(double time){
		this.stationArrivalTime = time;
	}
	
	public void setTimeToEnterBlock(double time){
		this.timeToEnterBlock = time;
	}
		
	public String getBlockOccupationDetail(){
		if(!isStation())
			return String.format("%d Arriving %-8.4f (%-3dkm/h) & Departing %-8.4f (%-3dkm/h) \\\\ \n", block.getID(), arrTime, arrSpeed,  depTime, depSpeed); 
		else 
			return String.format("%d Arriving %-8.4f (%-3dkm/h) & Departing %-8.4f (%-3dkm/h) \\\\ Station Arrival: %-8.4f \\\\ \n", block.getID(), arrTime, arrSpeed,  depTime, depSpeed, stationArrivalTime); 
	}
	
	public boolean isTurnaround(){
		return turnaround;
	}
	
	public void printOccupationDetail(){
		if(connection != null){
			System.out.println(connection.getIn().getID() + " to " + connection.getOut().getID() + " & " + connection.getLength() + "m & & \\\\ \\hline");
		}
		
		if(isStation() && turnaround)
			System.out.printf("%-3d  & %5dm & Yes & Yes \\\\ \\hline \n", block.getID(), this.getLength());
		else
			if(isStation())
				System.out.printf("%-3d  & %5dm & Yes & \\\\ \\hline \n", block.getID(), this.getLength());
			else
				if(turnaround)
					System.out.printf("%-3d  & %5dm &  & Yes \\\\ \\hline \n", block.getID(), this.getLength());
				else
					System.out.printf("%-3d  & %5dm &  & \\\\ \\hline \n", block.getID(), this.getLength());
	}
	
	public void printNoConnOccupationDetail(){
		
		if(isStation())
			System.out.printf("%-3d  & %5dm & Yes \\\\ \\hline \n", block.getID(), this.noConnLength());
		else
			System.out.printf("%-3d  & %5dm & \\\\ \\hline \n", block.getID(), this.noConnLength());
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
	
	public void setTurnaround(boolean turnaround){
		this.turnaround = turnaround;
	}
	
	public Connection getConnection(){
		return connection;
	}
	
}
