package entities;

public class BlockExit {

	private double time;
	private int speed;
	private String message;
	
	public BlockExit(double time, int speed, String message){
		this.time = time;
		this.speed = speed;
		this.message = message;
	}

	public double getTime() {
		return time;
	}

	public int getSpeed() {
		return speed;
	}
	
	public String getMessage(){
		return message;
	}
	
	
	
	
}
