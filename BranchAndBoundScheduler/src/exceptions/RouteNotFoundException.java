package exceptions;

public class RouteNotFoundException extends Exception{
	
	public RouteNotFoundException(int source, int dest){
		super("There is no route between " + source + " and " + dest);
	}

}
