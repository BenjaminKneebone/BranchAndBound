package entities;

import java.util.ArrayList;

import filehandling.NetworkJSONParser;

public class Network {

	private ArrayList<Engine> trains;
	private ArrayList<Block> blocks;
	private ArrayList<Join> joins;
	
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
	
	public ArrayList<Join> getJoins(){
		return joins;
	}
	
	public void printNetworkInfo(){
		for(Block b: blocks)
			System.out.println("block" + b.getID() + " length: " + b.getLength());
		
		
		for(Engine t: trains)
			System.out.println("train: " + t.getID() + " length: " + t.getLength() + " weight: " + t.getWeight() + " Speed: " + t.getSpeedProfile()[0]);
		
		for(Join j: joins){
			System.out.print("Source: " + j.getSource().getID());
	
			if(j.getDest() == null)
				System.out.println(" has no attached blocks");
			else{
				for(Block b: j.getDest())
					System.out.print(" connects to " + b.getID());
				System.out.print("\n");
			}
		}
	}
	
}
