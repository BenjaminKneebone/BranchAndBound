package entities;

/**
 * Object used to hold details of a train's exit
 * from a block.
 * @author bk8g11
 *
 */
public class BlockExit {

	private double time;
	private int speed;
	private double timeToTraverseLengthOfTrain;
	private String message;
	
	/**
	 * @param time Time leaving block
	 * @param speed Speed leaving block
	 * @param message Message to display information about train in block
	 */
	public BlockExit(double time, int speed, String message, double trainLengthTraversalTime){
		this.time = time;
		this.speed = speed;
		this.message = message;
		this.timeToTraverseLengthOfTrain = trainLengthTraversalTime;
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
	
	public double getTimeToEnterBlock(){
		return timeToTraverseLengthOfTrain;
	}
}
