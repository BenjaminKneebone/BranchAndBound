package entities;

public class Engine {

	private int id;
	private int length;
	private int weight;
	private int[] speedProfile = new int[10];
	
	public Engine(int id, int length, int weight, int[] speedProfile) {
		this.id = id;
		this.length = length;
		this.weight = weight;
		this.speedProfile = speedProfile;
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
	
}
