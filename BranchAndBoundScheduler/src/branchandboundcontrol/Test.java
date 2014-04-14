package branchandboundcontrol;

import java.util.ArrayList;
import algorithms.Dijkstra;
import entities.Journey;
import entities.JourneyCreator;
import entities.Network;
import entities.Stop;
public class Test {
	
	private static ArrayList<Journey> journeys = new ArrayList<Journey>();
	
	public static void main(String[] args){
		Network n = new Network("files/bidirectionalstraight.json");
		n.printNetworkInfo();
		
		Dijkstra d = new Dijkstra(n);

		ArrayList<Stop> stations = new ArrayList<Stop>();
		stations.add(new Stop(0,20));
		stations.add(new Stop(4,120));

		
		ArrayList<Stop> stations1 = new ArrayList<Stop>();
		stations1.add(new Stop(4,20));
		stations1.add(new Stop(0,120));
		
		ArrayList<Integer> stations2 = new ArrayList<Integer>();
		stations2.add(0);
		stations2.add(10);
		
		ArrayList<Integer> stations4 = new ArrayList<Integer>();
		stations4.add(0);
		stations4.add(12);
		
		ArrayList<Integer> stations3 = new ArrayList<Integer>();
		stations3.add(0);
		stations3.add(13);
		
		System.out.println("---JOURNEYS---");
		JourneyCreator.createSingleJourney(n.getTrains().get(0), stations, d, journeys);
		JourneyCreator.createSingleJourney(n.getTrains().get(1), stations1, d, journeys);
		
		if(journeys.size() > 0){
			
			for(Journey j: journeys)
				j.printSimpleJourney();
		
			//Create a scheduler with original configuration
			new NodeControl(journeys, n.getBlocks(), n.getTrains());	
		}else
			System.out.println("No Journeys could be routed");
		}
	
}
