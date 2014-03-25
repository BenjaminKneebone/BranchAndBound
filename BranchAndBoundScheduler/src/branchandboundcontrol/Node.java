package branchandboundcontrol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import entities.Block;
import entities.Engine;
import entities.Journey;

public class Node {
	
	private ArrayList<Block> blocks;
	/*
	 * A copy of the journeys passed into this node. All potential alterations
	 * to Journeys are stored in this list (Altered BlockOccupations)
	 */
	private ArrayList<Journey> journeys;
	/*
	 * A copy of the journeys passed into this node. This should not be altered
	 * and is passed into new nodes along with one altered journey
	 */
	private ArrayList<Journey> journeyCopy;
	private ArrayList<Engine> trains;
	private String id;

	// Details for earliest block exit for this node
	private Block firstArrivalBlock = null;
	private double firstArrivalTime = Integer.MAX_VALUE;
	
	private Map<Block, Boolean> occupied = new HashMap<Block, Boolean>();
	private Map<Block, Boolean> occupiedCopy = new HashMap<Block, Boolean>();
	
	
	public Node(ArrayList<Journey> journeys, ArrayList<Block> blocks,
			ArrayList<Engine> trains) {

		this.journeys = journeys;
		this.blocks = blocks;
		this.trains = trains;
		this.id = "0";
		
		for(Block b: blocks){
			occupied.put(b, false);
			occupiedCopy.put(b, false);
		}

		// Clone journeys
		ArrayList<Journey> newJournies = new ArrayList<Journey>();
		for (Journey j : journeys)
			newJournies.add(j.clone(this.blocks));

		this.journeyCopy = newJournies;
	}
	
	public Node(ArrayList<Journey> journeys, ArrayList<Block> blocks,
			ArrayList<Engine> trains, Journey alteredJourney, int index,
			String id, HashMap<Block, Boolean> occupied) {

		System.out.println("---NODE" + id + "---");

		// Clone blocks
		ArrayList<Block> newBlocks = new ArrayList<Block>();
		for (Block b : blocks)
			newBlocks.add(b.clone());

		this.blocks = newBlocks;

		for(Block b: this.blocks){
			this.occupied.put(b, occupied.get(blocks.get(b.getID())));
			this.occupiedCopy.put(b, occupied.get(blocks.get(b.getID())));
		}
		
		alteredJourney = alteredJourney.clone(this.blocks);

		// Clone journeys, pass in newly cloned blocks
		ArrayList<Journey> newJournies = new ArrayList<Journey>();
		for (Journey j : journeys)
			newJournies.add(j.clone(this.blocks));

		this.journeys = newJournies;

		// Add the altered journey that this node was created for
		this.journeys.set(index, alteredJourney);

		// Create another copy of the journeys

		alteredJourney = alteredJourney.clone(this.blocks);

		// Clone journeys, pass in newly cloned blocks
		newJournies = new ArrayList<Journey>();
		for (Journey j : journeys)
			newJournies.add(j.clone(this.blocks));

		this.journeyCopy = newJournies;
		journeyCopy.set(index, alteredJourney);

		this.id = id;

		// No need to clone trains
		this.trains = trains;
	}
	
	public HashMap<Block, Boolean> getOccupied(){
		return (HashMap<Block, Boolean>) occupied;
	}
	
	public HashMap<Block, Boolean> getOccupiedCopy(){
		return (HashMap<Block, Boolean>) occupiedCopy;
	}

	public Block getFirstArrivalBlock() {
		return firstArrivalBlock;
	}

	public void setFirstArrivalBlock(Block firstArrivalBlock) {
		this.firstArrivalBlock = firstArrivalBlock;
	}

	public double getFirstArrivalTime() {
		return firstArrivalTime;
	}

	public void setFirstArrivalTime(double firstArrivalTime) {
		this.firstArrivalTime = firstArrivalTime;
	}

	public ArrayList<Block> getBlocks() {
		return blocks;
	}

	public ArrayList<Journey> getJourneys() {
		return journeys;
	}

	public ArrayList<Journey> getJourneyCopy() {
		return journeyCopy;
	}

	public ArrayList<Engine> getTrains() {
		return trains;
	}

	public String getId() {
		return id;
	}
	
	
	
	
	
	
	
	
	
	
	
	
}
