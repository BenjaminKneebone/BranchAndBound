package entities;

public class Engine {

	private int id;
	private int length;
	private int weight;
	private int driForce;
	private int braForce;
	private String name;
	private int[] speedProfile = new int[10];
	
	public Engine(int id, int length, int weight, int driForce, int breForce, String name, int[] speedProfile) {
		this.id = id;
		this.length = length;
		this.weight = weight;
		this.driForce = driForce;
		this.braForce = breForce;
		this.name = name;
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
	
	public void printDetails(){
		System.out.println(name + " profile:");
		System.out.println("Length: " + length);
		System.out.println("Weight: " + weight);
		System.out.println("Driving Force: " + driForce);
		System.out.println("Breaking Force: " + braForce);
		System.out.print("Speed Profile: ");
		for(int x = 0; x < 10; x++)
			System.out.print(speedProfile[x] + " ");
	}

}
