package entities;

import exceptions.InvalidSpeedException;

public class Engine {

	private int id;
	private int length;         //mm
	private int weight;         //kilograms
	private int driForce;		//Newtons
	private int braForce;       //Newtons
	private double acceleration;//m/s^2
	private double deceleration;//m/s^2
	private String name;
	private int[] speedProfile = new int[10];
	
	public Engine(int id, int length, int weight, int driForce, int breForce, String name, int[] speedProfile) {
		this.id = id;
		this.length = length;
		this.weight = weight;
		this.driForce = driForce;
		this.braForce = breForce;
		this.name = name;
		this.acceleration = (double) driForce / weight;
		this.deceleration = (double) braForce / weight;
		this.speedProfile = speedProfile;
	}
	
	public String getName(){
		return name;
	}
	
	public int getID(){
		return id;
	}
	
	public int getLength() {
		return length;
	}
	
	public int getWeight() {
		return weight;
	}
	
	public int[] getSpeedProfile() {
		return speedProfile;
	}
	
	public void setSpeedProfile(int[] speedProfile) {
		this.speedProfile = speedProfile;
	}
	
	/**Use if train can proceed at full power (Next block is long enough and unoccupied)
	 * @param block - block to be traversed
	 * @param speed - entry speed (km/h)
	 * @return BlockExit object containing time to traverse block and exit speed (km/h)
	 * @throws InvalidSpeedException 
	 */
	public BlockExit timeToTraverse(Block block, int speed) throws InvalidSpeedException{
		
		if(speed < 0 || speed > speedProfile[9])
			throw new InvalidSpeedException(speed, name, speedProfile[9]);
		
		//Not enough time to reach full speed
		if(distanceToFullSpeed(speed) > block.getLength()){
			
			//The final velocity achievable over the distance (sqrt(u^2 + 2as))
			double finalVel = Math.sqrt(Math.pow(kmhToMs(speed), 2) + (2 * acceleration * block.getLength()));
			
			//Round down to nearest km/h
			int newVel = (int) Math.floor(msToKmh(finalVel));
			
			//Convert back to m/s for time calculations
			finalVel = kmhToMs(newVel);
			
			//The time it will take to reach this velocity (hence time in the block) ((v-u) / a)
			double time = (finalVel - kmhToMs(speed)) / acceleration;
			
			String message = name + " took " + time + " to traverse block " + id + ", entering at " + speed + "kmh, leaving at " + finalVel + "km/h -- Full Power";
			
			return new BlockExit(time, newVel, message);
		}else{
			
			double distanceAtFullSpeed = block.getLength() - distanceToFullSpeed(speed);
			
			//Time to accelerate to full speed and to cover remaining distance at full speed
			double time = timeToFullSpeed(speed) + (distanceAtFullSpeed / kmhToMs(speedProfile[9]));
			
			String message = name + " took " + time + " to traverse block " + id + ", entering at " + speed + "kmh, leaving at " + speedProfile[9] + "km/h -- Full Power";
			
			//Leaving at full speed
			return new BlockExit(time, speedProfile[9], message);
		}	
	}
	
	/**Use if train must exit the block at a set speed (Next block cannot support full speed stopping)
	 * @param block block to be traversed
	 * @param speed entry speed (km/h)
	 * @param finalSpeed exit speed (km/h)
	 * @return Block Exit item
	 * @throws InvalidSpeedException 
	 */
	public BlockExit exitBlockAtSetSpeed(Block block, int speed, int finalSpeed) throws InvalidSpeedException{
			
		System.out.println("Method speed: " + speed);
		System.out.println("Method finalspeed: " + finalSpeed);
		
		if(speed < 0 || speed > speedProfile[9])
			throw new InvalidSpeedException(speed, name, speedProfile[9]);
		
		if(finalSpeed < 0 || finalSpeed > speedProfile[9])
			throw new InvalidSpeedException(finalSpeed, name, speedProfile[9]);
		
		if(finalSpeed == speed){
			//Stay at constant speed
			String message = name + " traversed block " + block.getID() + " at " + speed + "km/h, taking " + timeToTraverseSetSpeed(block.getLength(), speed) + " seconds";
			return new BlockExit(timeToTraverseSetSpeed(block.getLength(), speed), speed, message);
		}
		
		
		if(finalSpeed > speed){
			//Accelerates over the block
			double accelerationTime = timeToChangeSpeed(speed, finalSpeed);
			double accelerationDist = distanceToChangeSpeed(speed, finalSpeed);
			
			//Time at constant speed
			double constantTime = timeToTraverseSetSpeed(block.getLength() - accelerationDist, finalSpeed);
			
			String message = name + " entered " + block.getID() + " at " + speed + ", begin acceleration at " + (block.getLength() - accelerationDist) + " exiting at final speed after " + (accelerationTime + constantTime) + " seconds";
			
			return new BlockExit(accelerationTime + constantTime, speed, message);
		}else{
			//Decelerates over the block
			double decelerationTime = timeToChangeSpeed(speed, finalSpeed);
			double decelerationDist = distanceToChangeSpeed(speed, finalSpeed);
			
			//Time at constant speed
			double constantTime = timeToTraverseSetSpeed(block.getLength() - decelerationDist, speed);
			System.out.println("Time in block: " + constantTime );
			
			String message = name + " entered " + block.getID() + " at " + speed + ", begin deceleration at " + (block.getLength() - decelerationDist) + " exiting at final speed after " + (decelerationTime + constantTime) + " seconds";
			
			return new BlockExit(decelerationTime + constantTime, speed, message);
		}
	}

	/**Use if train must spend certain amount of time in the block (If next block is occupied)
	 * @param block - block to be traversed
	 * @param speed - entry speed
	 * @param time - minimum time to spend in bloc
	 * @return BlockExit object containing time to traverse block and exit speed
	 * @throws InvalidSpeedException 
	 */
	public BlockExit minimumTimeTraversal(Block block, int speed, double time) throws InvalidSpeedException{
		
		if(speed < 0 || speed > speedProfile[9])
			throw new InvalidSpeedException(speed, name, speedProfile[9]);
		
		int newSpeed = speed;
		
		//Time taken if speed does not change
		double timeAtInputSpeed = timeToTraverseSetSpeed(block.getLength(), speed);
		
		//Should train slow down or accelerate in block?
		if(timeAtInputSpeed < time){
			
			//Too quick - slow down
			double decelerationTime = 0;
			double decelerationDist = 0;
			double constantTime = 0;
			
			/* Iterate down through speeds. When the total time is shorter than the minimum time 
			 * that speed is the fastest possible and exiting the block after minimum time given */
			
			//Until time taken is longer than minimum time
			while(decelerationTime + constantTime < time){
				
				//Decrement speed
				newSpeed--;

				if(newSpeed == 0){
					//Train must stop at the end of the block
					double stopTime = timeToStop(speed);
					double stopDist = distanceToStop(speed);
					
					//Amount of time to travel at entry speed before decelerating
					constantTime = timeToTraverseSetSpeed(block.getLength() - stopDist, speed);
					
					String message = name + " took " + (stopTime + constantTime) + " to traverse block " + id + ", entering at " + speed + ", decelerating at " + (block.getLength() - stopDist) + " leaving at 0km/h";
					
					return new BlockExit(stopTime + constantTime, 0, message);
				}else{
					//Time & distance changing speed
					decelerationTime = timeToChangeSpeed(speed, newSpeed);
					decelerationDist = distanceToChangeSpeed(speed, newSpeed);
					
					//Time at constant speed
					constantTime = timeToTraverseSetSpeed(block.getLength() - decelerationDist, newSpeed);
				}
			}
				
			String message = name + " took " + (decelerationTime + constantTime) + " to traverse block " + id + ", entering at " + speed + ", leaving at " + newSpeed + "km/h";
			
			return new BlockExit(decelerationTime + constantTime, newSpeed, message);
		}else{
			
			//Too slow - can speed up
			double accelerationTime = 100000;
			double accelerationDist = 100000;
			double constantTime = 0;
			
			/* Iterate up through speeds. When the total time is shorter than the minimum time it is too fast so 
			 * the previous speed tested is the fastest possible*/
			
			//Until time is shorter than minimum possible
			while(accelerationTime + constantTime > time){
				
				//Test new speed
				newSpeed++;
				
				if(newSpeed == 0 || newSpeed == speedProfile[9]){
					System.out.println("Train has come to a halt or reached full speed");
				}else{
					//Time & distance changing speed
					accelerationTime = timeToChangeSpeed(speed, newSpeed);
					accelerationDist = distanceToChangeSpeed(speed, newSpeed);
					
					//Time at constant speed
					constantTime = timeToTraverseSetSpeed(block.getLength() - accelerationDist, newSpeed);
				}
			}
			
			newSpeed--;
			
			//NewSpeed set to fastest speed possible (Highest speed giving longer time than minimum time)
			accelerationTime = timeToChangeSpeed(speed, newSpeed);
			accelerationDist = distanceToChangeSpeed(speed, newSpeed);
			constantTime = timeToTraverseSetSpeed(block.getLength() - accelerationDist, newSpeed);
			
			System.out.println("Acc time " + accelerationTime);
			System.out.println("Acc dist " + accelerationDist);
			System.out.println("Con time " + constantTime);
			System.out.println("Con dist " + (block.getLength() - accelerationDist));
			
			String message = name + " took " + (accelerationTime + constantTime) + " to traverse block " + id + ", entering at " + speed + "kmh, leaving at " + newSpeed + "km/h";
			
			return new BlockExit(accelerationTime + constantTime, newSpeed, message);
		}
	}
	
	/**
	 * Whether a train can stop in the given block
	 * @param block block in question
	 * @return true is block is long enough for train to stop at full speed
	 */
	public boolean canStopInBlock(Block block){
		return block.getLength() > distanceToStop(speedProfile[9]);
	}
	
	/**
	 * @param block block in question
	 * @return The highest speed the train can arrive in the block and stop in the block
	 */
	public int highestBlockEntrySpeed(Block block){
		//u = rt(v^2 - 2as), with v = 0 
		return (int) Math.floor(Math.sqrt(2 * block.getLength() * deceleration));
	}
	
	public void printDetails(){
		System.out.println(name + " profile:");
		System.out.println("Length: " + length);
		System.out.println("Weight: " + weight);
		System.out.println("Driving Force: " + driForce);
		System.out.println("Braking Force: " + braForce);
		System.out.println("Acceleration: " + acceleration);
		System.out.println("Deceleration: " + deceleration);
		System.out.print("Speed Profile: ");
		for(int x = 0; x < 10; x++)
			System.out.print(speedProfile[x] + " ");
		System.out.println();
	}
	
	
	//***HELPER FUNCTIONS
	
	/**
	 * Time taken for train to accelerate/decelerate from initialSpeed to finalSpeed 
	 * @param initialSpeed initial speed (km/h)
	 * @param finalSpeed final speed (km/h)
	 * @return time (seconds)
	 */
	private double timeToChangeSpeed(int initialSpeed, int finalSpeed){
		
		if(finalSpeed == initialSpeed)
			return 0;
		
		if(finalSpeed > initialSpeed)
			return (kmhToMs(finalSpeed) - kmhToMs(initialSpeed)) / acceleration;	
		else
			return (kmhToMs(initialSpeed) - kmhToMs(finalSpeed)) / deceleration;
	}
	
	/**
	 * Distance for train to accelerate/decelerate from initial speed to final speed
	 * @param initialSpeed initial speed (km/h)
	 * @param finalSpeed final speed (km/h)
	 * @return distance (m)
	 */
	private double distanceToChangeSpeed(int initialSpeed, int finalSpeed){
		if(finalSpeed == initialSpeed)
			return 0;
		
		double vsquared;
		double usquared;
		
		if(finalSpeed > initialSpeed){
		
			//Final speed m/s
			vsquared = Math.pow((double) kmhToMs(finalSpeed), 2);
					
			//Initial speed m/s
			usquared = Math.pow((double) kmhToMs(initialSpeed), 2);
					
			//Distance in m (v2 - u2 / 2a)
			return (((double) vsquared - usquared) / ((double) 2 * acceleration));
			
		}else{
			
			//Final speed m/s
			vsquared = Math.pow((double) kmhToMs(initialSpeed), 2);
					
			//Initial speed m/s
			usquared = Math.pow((double) kmhToMs(finalSpeed), 2);
					
			//Distance in m (v2 - u2 / 2a)
			return (((double) vsquared - usquared) / ((double) 2 * deceleration));
			
		}
	}
	
	/**
	 * Time to traverse distance at given speed
	 * @param distance distance to be traversed (m)
	 * @param speed speed to traverse at (km/h)
	 * @return time (seconds)
	 */
	private double timeToTraverseSetSpeed(double distance, int speed){
		return distance / kmhToMs(speed);
	}
	
	/**time for train to accelerate to full speed
	 * @param speed - Speed of train (km/h)
	 * @return time taken to reach full speed (in seconds)
	 */
	private double timeToFullSpeed(int speed){
		return timeToChangeSpeed(speed, speedProfile[9]);
	}

	/**distance required for train to reach full speed
	 * @param speed - Speed of train (km/h)
	 * @return distance needed to reach full speed (in metres)
	 */
	private double distanceToFullSpeed(int speed){
		return distanceToChangeSpeed(speed, speedProfile[9]);
	}
	
	/**
	 * @param speed - Speed of train (Km/h)
	 * @return time needed to stop train (in metres)
	 */
	private double timeToStop(int speed){
		return timeToChangeSpeed(speed, 0);
	}
	
	/**Train stopping distance
	 * @param speed - Speed of train
	 * @return distance needed to stop train (in metres)
	 */
	private double distanceToStop(int speed){
		return distanceToChangeSpeed(speed, 0);
	}
	
	private double kmhToMs(double kmSpeed){
		return kmSpeed / 3.6;
	}
	
	private double msToKmh(double msSpeed){
		return msSpeed * 3.6;
	}
	
	
	
	
	
	
	

}
