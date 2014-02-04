package control;

import bbentities.Journey;
import entities.Network;
import exceptions.RouteNotFoundException;

public class Test {

	public static void main(String[] args){
		Network n = new Network();
		n.printNetworkInfo();
		
		
		int[] stations = {0, 5,4,0,17};
		
	
		Journey j;
		try {
			j = new Journey(n.getTrains().get(0), stations, n);
			j.printJourney();
		} catch (RouteNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
}
