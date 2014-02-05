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
	
	/**Calculate traversal providing the mimumum amount of the time the train must spend in the block
	 * @param block - block to be traversed
	 * @param speed - entry speed
	 * @param time - minimum time to spend in bloc
	 * @return BlockExit object containing time to traverse block and exit speed
	 */
	public BlockExit delayTraversal(Block block, double speed, double time){
			
		double acc = (2 * (block.getLength() - ((speed/3.6) * time))) / Math.pow(time,2);
		System.out.println("acc: " + acc);
		
		double finalVel = ((2 * block.getLength())/time) - (speed/3.6);
		
		return new BlockExit(time, finalVel);
		
	}
	
	/**Returns information about a train traversing a block if at full power
	 * @param block - block to be traversed
	 * @param speed - entry speed (km/h)
	 * @return BlockExit object containing time to traverse block and exit speed (km/h)
	 */
	public BlockExit timeToTraverse(Block block, double speed){
		
		//Not enough time to reach full speed
		if(distanceToFullSpeed(speed) > block.getLength()){
			
			//The final velocity achievable over the distance (sqrt(u^2 - 2as))
			double finalVel = Math.sqrt(Math.pow((speed / 36), 2) + (2 * acceleration * block.getLength()));
	
			//The time it will take to reach this velocity (hence time in the block) ((v-u) / a)
			double time = (finalVel - (speed/ 3.6)) / acceleration;
			
			//convert to km
			finalVel = (finalVel * 3.6);
			
			return new BlockExit(time, finalVel);
		}else{
			
			//distance left after getting to full speed
			double distanceAtFullSpeed = block.getLength() - distanceToFullSpeed(speed);
			
			//Time to accelerate to full speed and to cover remaining distance at full speed
			double totalTime = timeToFullSpeed(speed) + (distanceAtFullSpeed/ ((double) speedProfile[9] / 3.6));
			
			//Leaving at full speed
			return new BlockExit(totalTime, speedProfile[9]);
		}	
	}
	
	/**
	 * 
	 * @param speed - Speed of train
	 * @return time taken to reach full speed (in seconds)
	 */
	public double timeToFullSpeed(double speed){
		//Difference in speed (m/s) divided by acceleration (m/s/s)
		return ((speedProfile[9]/3.6) - (speed/3.6)) / acceleration;
	}

	/**
	 * @param speed - Speed of train
	 * @return distance needed to reach full speed (in metres)
	 */
	public double distanceToFullSpeed(double speed){
		
		//Final speed m/s
		double vsquared = Math.pow((double) (speedProfile[9] / 3.6), 2);
		
		//Initial speed m/s
		double usquared = Math.pow((double) (speed / 3.6), 2);
		
		//Distance in m (v2 - u2 / 2a)
		return (((double) vsquared - usquared) / ((double) 2 * acceleration));
	}
	
	/**
	 * @param speed - Speed of train (Km/h)
	 * @return time needed to stop train (in metres)
	 */
	public double timeToStop(double speed){
		//Difference in speed (m/s) divided by acceleration (m/s/s)
		return (speed/3.6) / deceleration;
	}
	
	/**
	 * @param speed - Speed of train
	 * @return distance needed to stop train (in metres)
	 */
	public double distanceToStop(double speed){
		
		//Initial speed m/s
		double vsquared = Math.pow((double) (speed / 3.6), 2);
								
		//Distance in m (v2 - u2 / 2a) - u = 0
		return ((double) vsquared / ((double) 2 * deceleration));
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
