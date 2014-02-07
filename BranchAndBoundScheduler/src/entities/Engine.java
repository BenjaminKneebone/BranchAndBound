package entities;

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
	
	/**Calculate traversal providing the minimum amount of the time the train must spend in the block
	 * @param block - block to be traversed
	 * @param speed - entry speed
	 * @param time - minimum time to spend in bloc
	 * @return BlockExit object containing time to traverse block and exit speed
	 */
	public BlockExit delayTraversal(Block block, int speed, double time){
		
		double newSpeed = speed;
		
		//Time traversing block at entry speed
		double timeAtInputSpeed = timeToTraverseSetSpeed(block.getLength(), speed);
		System.out.println(timeAtInputSpeed);
		
		//Should train slow down or accelerate in block?
		if(timeAtInputSpeed < time){
			
			//Too quick - slow down
			double decelerationTime = 0;
			double decelerationDist = 0;
			double constantTime = 0;
			
			/* Iterate down through speeds. When the total time is shorter than the minimum time 
			 * that speed is the fastest possible and exiting the block after minimum time given */
			
			//Until speed is quicker than minimum time
			while(decelerationTime + constantTime < time){
				
				//Test new speed
				newSpeed--;

				if(newSpeed == 0){
					
					double stopTime = timeToStop(speed);
					double stopDist = distanceToStop(speed);
					
					constantTime = timeToTraverseSetSpeed(block.getLength() - stopDist, speed);
					
					String message = name + " took " + (stopTime + constantTime) + " to traverse block " + id + ", entering at " + speed + ", decelerating at " + (block.getLength() - stopDist) + " leaving at 0km/h";
					
					return new BlockExit(stopTime + constantTime, 0, message);
				}else{
					//Time & distance changing speed
					decelerationTime = timeToReachSpeed(speed, newSpeed);
					decelerationDist = distanceToReachSpeed(speed, newSpeed);
					
					//Time at constant speed
					constantTime = timeToTraverseSetSpeed(block.getLength() - decelerationDist, newSpeed);
				}
			}
			
			System.out.println("Acc time " + decelerationTime);
			System.out.println("Acc dist " + decelerationDist);
			System.out.println("Con time " + constantTime);
			System.out.println("Con dist " + (block.getLength() - decelerationDist));
			
			String message = name + " took " + (decelerationTime + constantTime) + " to traverse block " + id + ", entering at " + speed + ", leaving at " + newSpeed + "km/h";
			
			return new BlockExit(decelerationTime + constantTime, newSpeed, message);
		}else{
			
			//Too slow - can speed up
			double accelerationTime = 100000;
			double accelerationDist = 100000;
			double constantTime = 0;
			
			/* Iterate up through speeds. When the total time is shorter than the minimum time it is too fast so 
			 * the previous speed tested is the fastest possible and exiting the block after minimum time given */
			
			//Until time is longer than minimum possible
			while(accelerationTime + constantTime > time){
				
				//Test new speed
				newSpeed++;
				
				if(newSpeed == 0 || newSpeed == speedProfile[9]){
					System.out.println("Train has come to a halt or reached full speed");
				}else{
					//Time & distance changing speed
					accelerationTime = timeToReachSpeed(speed, newSpeed);
					accelerationDist = distanceToReachSpeed(speed, newSpeed);
					
					//Time at constant speed
					constantTime = timeToTraverseSetSpeed(block.getLength() - accelerationDist, newSpeed);
				}
			}
			
			newSpeed--;
			
			//NewSpeed set to fastest speed possible (Highest speed giving longer time than minimum time)
			accelerationTime = timeToReachSpeed(speed, newSpeed);
			accelerationDist = distanceToReachSpeed(speed, newSpeed);
			constantTime = timeToTraverseSetSpeed(block.getLength() - accelerationDist, newSpeed);
			
			System.out.println("Acc time " + accelerationTime);
			System.out.println("Acc dist " + accelerationDist);
			System.out.println("Con time " + constantTime);
			System.out.println("Con dist " + (block.getLength() - accelerationDist));
			
			String message = name + " took " + (accelerationTime + constantTime) + " to traverse block " + id + ", entering at " + speed + "kmh, leaving at " + newSpeed + "km/h";
			
			return new BlockExit(accelerationTime + constantTime, newSpeed, message);
		}
			
		
	}
	
	
	private double timeToReachSpeed(double initialSpeed, double finalSpeed){
		
		if(finalSpeed == initialSpeed)
			return 0;
		
		
		if(finalSpeed > initialSpeed)
			return (kmhToMs(finalSpeed) - kmhToMs(initialSpeed)) / acceleration;	
		else
			return (kmhToMs(initialSpeed) - kmhToMs(finalSpeed)) / deceleration;
	}
	
	private double distanceToReachSpeed(double initialSpeed, double finalSpeed){
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
	
	private double timeToTraverseSetSpeed(double distance, double speed){
		return distance / kmhToMs(speed);
	}
	
	/**Returns information about a train traversing a block if at full power
	 * @param block - block to be traversed
	 * @param speed - entry speed (km/h)
	 * @return BlockExit object containing time to traverse block and exit speed (km/h)
	 */
	public BlockExit timeToTraverse(Block block, double speed){
		
		//Not enough time to reach full speed
		if(distanceToFullSpeed(speed) > block.getLength()){
			
			//The final velocity achievable over the distance (sqrt(u^2 + 2as))
			double finalVel = Math.sqrt(Math.pow(kmhToMs(speed), 2) + (2 * acceleration * block.getLength()));
	
			//The time it will take to reach this velocity (hence time in the block) ((v-u) / a)
			double time = (finalVel - kmhToMs(speed)) / acceleration;
			
			//convert to km
			finalVel = msToKmh(finalVel);
			
			String message = name + " took " + time + " to traverse block " + id + ", entering at " + speed + "kmh, leaving at " + finalVel + "km/h -- Full Power";
			
			return new BlockExit(time, finalVel, message);
		}else{
			
			//distance left after getting to full speed
			double distanceAtFullSpeed = block.getLength() - distanceToFullSpeed(speed);
			
			//Time to accelerate to full speed and to cover remaining distance at full speed
			double time = timeToFullSpeed(speed) + (distanceAtFullSpeed / kmhToMs(speedProfile[9]));
			
			String message = name + " took " + time + " to traverse block " + id + ", entering at " + speed + "kmh, leaving at " + speedProfile[9] + "km/h -- Full Power";
			
			//Leaving at full speed
			return new BlockExit(time, speedProfile[9], message);
		}	
	}
	
	/**
	 * 
	 * @param speed - Speed of train
	 * @return time taken to reach full speed (in seconds)
	 */
	public double timeToFullSpeed(double speed){
		//(v - u) / a  
		return (kmhToMs(speedProfile[9]) - kmhToMs(speed)) / acceleration;
	}

	/**
	 * @param speed - Speed of train
	 * @return distance needed to reach full speed (in metres)
	 */
	public double distanceToFullSpeed(double speed){
		
		//Final speed m/s
		double vsquared = Math.pow((double) kmhToMs(speedProfile[9]), 2);
		
		//Initial speed m/s
		double usquared = Math.pow((double) kmhToMs(speed), 2);
		
		//Distance in m (v2 - u2 / 2a)
		return (((double) vsquared - usquared) / ((double) 2 * acceleration));
	}
	
	/**
	 * @param speed - Speed of train (Km/h)
	 * @return time needed to stop train (in metres)
	 */
	public double timeToStop(double speed){
		//Difference in speed (m/s) divided by acceleration (m/s/s)
		return kmhToMs(speed) / deceleration;
	}
	
	/**
	 * @param speed - Speed of train
	 * @return distance needed to stop train (in metres)
	 */
	public double distanceToStop(double speed){
		
		//Initial speed m/s
		double vsquared = Math.pow((double) kmhToMs(speed), 2);
								
		//Distance in m (v2 - u2 / 2a) --> u = 0
		return ((double) vsquared / ((double) 2 * deceleration));
	}
	
	private double kmhToMs(double kmSpeed){
		return kmSpeed / 3.6;
	}
	
	private double msToKmh(double msSpeed){
		return msSpeed * 3.6;
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

}
