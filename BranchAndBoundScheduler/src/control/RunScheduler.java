package control;

import java.util.ArrayList;

import ownalgorithm.OANodeControl;

import branchandboundcontrol.BBNodeControl;
import algorithms.Dijkstra;
import entities.Journey;
import entities.JourneyCreator;
import entities.Network;
import entities.Stop;

public class RunScheduler {
	
	//1 - Branch and Bound
	//2 - Author's Alg
	private static int scheduler = 2;
	
	
	private static ArrayList<Journey> journeys = new ArrayList<Journey>();
	
	public static void main(String[] args){
		Network n = new Network("files/unidirectionalstraight.json");
		n.printNetworkInfo();
		
		
		Dijkstra d = new Dijkstra(n);

		ArrayList<Stop> stations = new ArrayList<Stop>();
		stations.add(new Stop(0,120));
		stations.add(new Stop(4,120));
		
		ArrayList<Stop> stations1 = new ArrayList<Stop>();
		stations1.add(new Stop(4,120));
		stations1.add(new Stop(0,120));

		
		ArrayList<Stop> stations2 = new ArrayList<Stop>();
		stations2.add(new Stop(0,120));
		stations2.add(new Stop(3,120));
		stations2.add(new Stop(0,120));
		
		ArrayList<Stop> stations3 = new ArrayList<Stop>();
		stations3.add(new Stop(0,120));
		stations3.add(new Stop(0,120));
		
		ArrayList<Stop> stations4 = new ArrayList<Stop>();
		stations4.add(new Stop(0,120));
		stations4.add(new Stop(0,120));
		stations4.add(new Stop(0,120));
		
		
		ArrayList<Stop> stations5 = new ArrayList<Stop>();
		stations5.add(new Stop(3,120));
		stations5.add(new Stop(3,120));
		stations5.add(new Stop(3,120));
	
		ArrayList<Stop> stations6 = new ArrayList<Stop>();
		stations6.add(new Stop(0,120));
		stations6.add(new Stop(3,120));
		stations6.add(new Stop(0,120));
		stations6.add(new Stop(3,120));


		
		
		
		System.out.println("---JOURNEYS---");
		JourneyCreator.createSingleJourney(n.getTrains().get(0), stations, d, journeys);
		JourneyCreator.createSingleJourney(n.getTrains().get(0), stations1, d, journeys);
		JourneyCreator.createSingleJourney(n.getTrains().get(0), stations2, d, journeys);
		
		
		if(journeys.size() > 0){
			
			for(Journey j: journeys)
				j.printSimpleJourney();
			
			System.out.println("\n\n\n --------------NO CONNECTION ROUTE------------");
			
			
			for(Journey j: journeys)
				j.printNoConnectionJourney();
		
			
			//if(scheduler == 1)
				//new BBNodeControl(journeys, n.getBlocks(), n.getTrains());	
			
			//if(scheduler == 2)
				//new OANodeControl(journeys, n.getBlocks());
			
		}else
			System.out.println("No Journeys could be routed");
	}
		
	
}
