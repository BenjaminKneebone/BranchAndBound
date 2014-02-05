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
	

	
	public double distanceToFullSpeed(int speed){
		
		//Final speed m/s
		double vsquared = Math.pow((double) (speedProfile[9] * 1000) / 3600, 2);
		
		//Initial speed m/s
		double usquared = Math.pow((double) (speed * 1000) / 3600, 2);
		
		//Distance in m
		return (((double) vsquared - usquared) / ((double) 2 * acceleration));
	}
	
	public double distanceToStop(int speed){
		
		//Final speed m/s
		double vsquared = Math.pow((double) (speed * 1000) / 3600, 2);
				
		//Initial speed m/s
		double usquared = Math.pow(0, 2);
				
		//Distance in m
		return (((double) vsquared - usquared) / ((double) 2 * deceleration));
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
