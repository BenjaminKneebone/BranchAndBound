package filehandling;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import entities.Block;
import entities.Connection;
import entities.Engine;
import entities.Join;

public class NetworkJSONParser {

	private JSONObject network;
	private ArrayList<Engine> trains;
	private ArrayList<Block> blocks;
	private ArrayList<ArrayList<Join>> joins;
	
	/**
	 * @param filename File to parse network data from
	 */
	public NetworkJSONParser(String filename){
		JSONParser jp = new JSONParser();
		
		try {
			Object obj = jp.parse(new FileReader(filename));
			network = (JSONObject) obj;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		createTrains();
		createBlocks();
		createJoins();
		
	}
	
	private void createTrains(){
		
		/*Empty list set up so that trains can be added to the list in the
		 * index represented by their ID. Before being performed this way, 
		 * the originally created ArrayList was not always ordered by train
		 * ID. Ordering is now guaranteed.
		 */
		
		trains = new ArrayList<Engine>();
		
		JSONArray engines = (JSONArray) network.get("engines");
		//Initialise adjacency ArrayList
		for(int x = 0; x < engines.size(); x++)
			trains.add(new Engine(0, 0, 0, 0, 0, "", null));
		
		@SuppressWarnings("unchecked")
		Iterator<JSONObject> iterator = engines.iterator();
		
		int id;
		
		while (iterator.hasNext()) {
			JSONObject train = iterator.next();
			
			id = Integer.parseInt(train.get("id").toString());
			
			//Create speed profile
			JSONArray speed = (JSONArray) train.get("speed");
			int[] speedProfile = new int[10];
			for(int x = 0; x < speed.size(); x++)
				speedProfile[x] = Integer.parseInt(speed.get(x).toString());
			
			//Create Engine
			Engine temp = new Engine(id, Integer.parseInt(train.get("length").toString()), Integer.parseInt(train.get("weight").toString()), 
					Integer.parseInt(train.get("driving force").toString()), Integer.parseInt(train.get("braking force").toString()), 
					train.get("name").toString(), speedProfile);
			
			//Add Engine
			trains.set(id,temp);
		}
	}
	
	private void createBlocks(){
		
		/*Empty list set up so that blocks can be added to the list in the
		 * index represented by their ID. Before being performed this way, 
		 * the originally created ArrayList was not always ordered by block
		 * ID. Ordering is now guaranteed.
		 */
		
		blocks = new ArrayList<Block>();
		
		JSONArray blockObjects = (JSONArray) network.get("blocks");
		
		//Initialise adjacency ArrayList
		for(int x = 0; x < blockObjects.size(); x++)
			blocks.add(new Block(0, 0));
		
		
		@SuppressWarnings("unchecked")
		Iterator<JSONObject> blockIterator = blockObjects.iterator();
		
		int sourceID;
		
		while (blockIterator.hasNext()) {
			JSONObject block = blockIterator.next();
			sourceID = Integer.parseInt(block.get("id").toString());
			
			//create block
			Block temp = new Block(sourceID, Integer.parseInt(block.get("length").toString()));
			
			//add in correct place
			blocks.set(sourceID, temp);
		}
	}
	
	private void createJoins(){
		
		/*Empty list set up so that joins can be added to the list in the
		 * index represented by their sourceID. Before being performed this way, 
		 * the originally created ArrayList was not always ordered by source
		 * ID. Ordering is now guaranteed.
		 */
		
		joins = new ArrayList<ArrayList<Join>>();
		JSONArray blockObjects = (JSONArray) network.get("blocks");
		
		ArrayList<Join> joinList;
		
		//Initialise adjacency arraylist
		for(int x = 0; x < blockObjects.size(); x++){
			joinList = new ArrayList<Join>();
			joins.add(joinList);
		}
		
		JSONArray joinObjects = (JSONArray) network.get("joins");
		
		@SuppressWarnings("unchecked")
		Iterator<JSONObject> joinIterator = joinObjects.iterator();
		
		//For each join
		while(joinIterator.hasNext()){
			
			//Get all blocks either side of the join
			@SuppressWarnings("unchecked")
			Iterator<JSONObject> allowedIterator = ((JSONArray) joinIterator.next().get("allowed")).iterator();
			
			Join newJoin = new Join();

			Block in;
			Block out;
			int length;
			
			while(allowedIterator.hasNext()){
				JSONObject nextConnection = (JSONObject) allowedIterator.next();
				
				in = blocks.get(Integer.parseInt(nextConnection.get("in").toString()));
				out = blocks.get(Integer.parseInt(nextConnection.get("out").toString()));
				length = Integer.parseInt(nextConnection.get("length").toString());
				System.out.println("Connection length: " + length);
				newJoin.addConnection(new Connection(in, out, length));
			}
			
			for(Block b: newJoin.getIns()){
				//System.out.println("Adding to " + b.getID());
				joins.get(b.getID()).add(newJoin);
			}
			
		}
	}
	
	public ArrayList<Engine> getTrains(){
		return trains;
	}
	
	public ArrayList<ArrayList<Join>> getJoins(){
		return joins;
	}
	
	public ArrayList<Block> getBlocks(){
		return blocks;
	}
	
	
	
	
}
