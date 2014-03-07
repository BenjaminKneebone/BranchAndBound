package entities;

import java.util.ArrayList;

import exceptions.RouteNotFoundException;

import algorithms.Dijkstra;

public class JourneyCreator {

	/**
	 * Schedule a repeated journey
	 * @param train The train performing these journies
	 * @param stations List of stations this journey will stop at
	 * @param d A dijkstra instance holding the network the journey is to be scheduled in
	 * @param journeys Newly created journeys will be added to this list
	 * @throws RouteNotFoundException 
	 */
	public static void createRepeatedJourneys(Engine train, ArrayList<Integer> stations, Dijkstra d, ArrayList<Journey> journeys, int interval, int num) throws RouteNotFoundException{
		
		for(int x = 0; x < num; x++){
			
			Journey j = new Journey(train, stations, d, journeys);
			j.getBlockOccupations().get(0).setArrTime(x * interval);
			journeys.add(j);
		}
	}
	
	/**
	 * Schedule a repeated journey
	 * @param train The train performing these journies
	 * @param stations List of stations this journey will stop at
	 * @param d A dijkstra instance holding the network the journey is to be scheduled in
	 * @param journeys Newly created journeys will be added to this list
	 * @throws RouteNotFoundException 
	 */
	public static void createSingleJourney(Engine train, ArrayList<Integer> stations, Dijkstra d, ArrayList<Journey> journeys) throws RouteNotFoundException{
		Journey j = new Journey(train, stations, d, journeys);
		journeys.add(j);
	}
}
