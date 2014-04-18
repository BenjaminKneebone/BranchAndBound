package entities;

import java.util.ArrayList;

import filehandling.NetworkJSONParser;

public class Network {

	private ArrayList<Engine> trains;
	private ArrayList<Block> blocks;
	private ArrayList<ArrayList<Join>> joins;
	
	/**
	 * @param filename File to parse network data from
	 */
	public Network(String filename){
		NetworkJSONParser js = new NetworkJSONParser(filename);
		trains = js.getTrains();
		blocks = js.getBlocks();
		joins = js.getJoins();
	}
	
	public ArrayList<Block> getBlocks(){
		return blocks;
	}
	
	public ArrayList<Engine> getTrains(){
		return trains;
	}
	
	public ArrayList<ArrayList<Join>> getJoins(){
		return joins;
	}
	
	public void printNetworkInfo(){
		for(Block b: blocks)
			b.printBlockDetail();
		
		
		for(Engine t: trains)
			System.out.println("train: " + t.getID() + " length: " + t.getLength() + " weight: " + t.getWeight() + " Speed: " + t.getSpeedProfile()[0]);
		
		for(int x = 0; x < joins.size(); x++){
			System.out.println("Block " + x + " has the connections:");
			
			for(Join j: joins.get(x)){
				j.printConnections(x);
			}
			
		}
	}
	
}
