package entities;

import java.util.ArrayList;

import filehandling.NetworkJSONParser;

public class Network {

	private ArrayList<Engine> trains = new ArrayList<Engine>();
	private ArrayList<Block> blocks = new ArrayList<Block>();
	
	public Network(){
		NetworkJSONParser js = new NetworkJSONParser();
		trains = js.getTrains();
		blocks = js.getBlocks();
		
		for(Block block : blocks)
			System.out.println(block.getLength());
	}
	
	
}
