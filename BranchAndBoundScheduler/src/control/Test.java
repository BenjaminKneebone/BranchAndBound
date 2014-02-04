package control;

import algorithms.Dijkstra;
import bbentities.Journey;
import entities.Network;
import exceptions.RouteNotFoundException;

public class Test {

	public static void main(String[] args){
		Network n = new Network();
		n.printNetworkInfo();
		
		Dijkstra d = new Dijkstra(n);

		
		
		int[] stations = {0, 5,4,0,17};
		
	
		Journey j;
		try {
			j = new Journey(n.getTrains().get(0), stations, d);
			j.printJourney();
		} catch (RouteNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
}
