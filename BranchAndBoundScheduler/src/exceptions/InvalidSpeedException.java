package exceptions;

public class InvalidSpeedException extends Exception {

	public InvalidSpeedException(int speed, String name, int maxSpeed){
		super("The speed " + speed + "km/h is not possible for " + name + " (0-" + maxSpeed + "km/h)");
	}
	
	
	
	
}
