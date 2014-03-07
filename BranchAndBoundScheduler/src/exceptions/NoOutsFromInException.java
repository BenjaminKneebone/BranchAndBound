package exceptions;

public class NoOutsFromInException extends Exception {

	public NoOutsFromInException(int out){
		super("This join has not valid connections from block " + out);
	}
	
}
