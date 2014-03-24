package branchandboundcontrol;

import java.util.ArrayList;

import algorithms.Dijkstra;
import entities.BlockOccupation;
import entities.Journey;
import entities.Block;
import entities.BlockExit;
import entities.Engine;
import entities.JourneyCreator;
import entities.Network;
public class Test {
	
	private static ArrayList<Journey> journeys = new ArrayList<Journey>();
	
	public static void main(String[] args){
		Network n = new Network("files/8BlockLoopNetwork.json");
		n.printNetworkInfo();
		
		Dijkstra d = new Dijkstra(n);

		Engine t = n.getTrains().get(0);
			
		ArrayList<Integer> stations = new ArrayList<Integer>();
		stations.add(4);
		stations.add(7);
		stations.add(4);
		stations.add(7);
		stations.add(4);
		stations.add(7);
		stations.add(4);
	
		
		ArrayList<Integer> stations1 = new ArrayList<Integer>();
		stations1.add(0);
		stations1.add(6);
		
		ArrayList<Integer> stations2 = new ArrayList<Integer>();
		stations2.add(0);
		stations2.add(10);
		
		ArrayList<Integer> stations4 = new ArrayList<Integer>();
		stations4.add(0);
		stations4.add(12);
		
		ArrayList<Integer> stations3 = new ArrayList<Integer>();
		stations3.add(0);
		stations3.add(13);
		
		//System.out.println("---INITIAL STATE---");
		//System.out.println("---BLOCKS---");
		//for(Block b: n.getBlocks())
			//b.printBlockDetail();
		System.out.println("---JOURNEYS---");
			JourneyCreator.createSingleJourney(n.getTrains().get(0), stations, d, journeys);
			JourneyCreator.createSingleJourney(n.getTrains().get(1), stations, d, journeys);
			JourneyCreator.createSingleJourney(n.getTrains().get(2), stations, d, journeys);
			//JourneyCreator.createSingleJourney(n.getTrains().get(0), stations, d, journeys);
			//JourneyCreator.createSingleJourney(n.getTrains().get(0), stations, d, journeys);
			//JourneyCreator.createSingleJourney(n.getTrains().get(0), stations, d, journeys);
			//JourneyCreator.createSingleJourney(n.getTrains().get(0), stations4, d, journeys);
		
		//System.out.println("---END INITIAL STATE---");
		
		for(Journey j: journeys)
			j.printSimpleJourney();
		
		
		//Create a scheduler with original configuration
		new NodeControl(journeys, n.getBlocks(), n.getTrains());	
		
	}
	
}
