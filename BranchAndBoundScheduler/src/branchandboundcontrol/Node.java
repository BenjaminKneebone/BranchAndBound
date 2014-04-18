package branchandboundcontrol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import entities.Block;
import entities.Engine;
import entities.Journey;
import entities.Train;

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
	
	private Map<Block, ArrayList<Train>> occupied = new HashMap<Block, ArrayList<Train>>();
	private Map<Block, ArrayList<Train>> occupiedCopy = new HashMap<Block, ArrayList<Train>>();
	private Map<Block, Double> lastEntry = new HashMap<Block, Double>();
	private Map<Block, Double> lastEntryCopy = new HashMap<Block, Double>();
	private Map<Block, Double> nextPossibleEntry = new HashMap<Block, Double>();
	private Map<Block, Double> nextPossibleEntryCopy = new HashMap<Block, Double>();
	
	public Node(ArrayList<Journey> journeys, ArrayList<Block> blocks,
			ArrayList<Engine> trains) {

		this.journeys = journeys;
		this.blocks = blocks;
		this.trains = trains;
		this.id = "0";
		
		for(Block b: blocks){
			occupied.put(b, new ArrayList<Train>());
			occupiedCopy.put(b, new ArrayList<Train>());
			lastEntry.put(b, 0.0);
			lastEntryCopy.put(b, 0.0);
			nextPossibleEntry.put(b, 0.0);
			nextPossibleEntryCopy.put(b, 0.0);
		}

		// Clone journeys
		ArrayList<Journey> newJournies = new ArrayList<Journey>();
		for (Journey j : journeys)
			newJournies.add(j.clone(this.blocks));

		this.journeyCopy = newJournies;
	}
	
	public Node(ArrayList<Journey> journeys, ArrayList<Block> blocks,
			ArrayList<Engine> trains, Journey alteredJourney, int index,
			String id, HashMap<Block, ArrayList<Train>> occupied, HashMap<Block, Double> lastEntry, HashMap<Block, Double> nextPossibleEntry) {

		System.out.println("---NODE" + id + "---");

		this.blocks = blocks;

		for(Block b: this.blocks){
			ArrayList<Train> newOccupied = new ArrayList<Train>();
			ArrayList<Train> newOccupiedCopy = new ArrayList<Train>();
			for(Train t: occupied.get(blocks.get(b.getID()))){
					newOccupied.add(t);
					newOccupiedCopy.add(t);
			}
			
			this.occupied.put(b, newOccupied);
			this.occupiedCopy.put(b, newOccupiedCopy);
			this.lastEntry.put(b, lastEntry.get(blocks.get(b.getID())));
			this.lastEntryCopy.put(b, lastEntry.get(blocks.get(b.getID())));
			this.nextPossibleEntry.put(b, nextPossibleEntry.get(blocks.get(b.getID())));
			this.nextPossibleEntryCopy.put(b, nextPossibleEntry.get(blocks.get(b.getID())));
		}
		
		alteredJourney = alteredJourney.clone(this.blocks);

		// Clone journeys
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
	
	public HashMap<Block, ArrayList<Train>> getOccupied(){
		return (HashMap<Block, ArrayList<Train>>) occupied;
	}
	
	public HashMap<Block, ArrayList<Train>> getOccupiedCopy(){
		return (HashMap<Block, ArrayList<Train>>) occupiedCopy;
	}
	
	public HashMap<Block, Double> getLastEntry(){
		return (HashMap<Block, Double>) lastEntry;
	}
	
	public HashMap<Block, Double> getLastEntryCopy(){
		return (HashMap<Block, Double>) lastEntryCopy;
	}
	
	public HashMap<Block, Double> getNextPossibleEntry(){
		return (HashMap<Block, Double>) nextPossibleEntry;
	}
	
	public HashMap<Block, Double> getNextPossibleEntryCopy(){
		return (HashMap<Block, Double>) nextPossibleEntryCopy;
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
