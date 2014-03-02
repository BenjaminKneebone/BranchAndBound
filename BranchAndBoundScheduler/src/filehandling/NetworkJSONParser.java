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
import entities.Engine;
import entities.Join;

public class NetworkJSONParser {

	private JSONObject network;
	private ArrayList<Engine> trains;
	private ArrayList<Block> blocks;
	private ArrayList<Join> joins;
	
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
			blocks.add(new Block(0, 0, 0 , 0, false));
		
		
		@SuppressWarnings("unchecked")
		Iterator<JSONObject> blockIterator = blockObjects.iterator();
		
		int sourceID;
		
		while (blockIterator.hasNext()) {
			JSONObject block = blockIterator.next();
			sourceID = Integer.parseInt(block.get("id").toString());
			
			//create block
			Block temp = new Block(sourceID, Integer.parseInt(block.get("length").toString()), 0, 0, false);
			
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
		
		joins = new ArrayList<Join>();
		JSONArray blockObjects = (JSONArray) network.get("blocks");
		
		//Initialise adjacency arraylist
		for(int x = 0; x < blockObjects.size(); x++)
			joins.add(new Join(blocks.get(x), null));
		
		
		JSONArray joinObjects = (JSONArray) network.get("joins");
		
		@SuppressWarnings("unchecked")
		Iterator<JSONObject> joinIterator = joinObjects.iterator();
		
		//For each join
		while(joinIterator.hasNext()){
			
			//Get all blocks either side of the join
			JSONArray ids = (JSONArray) joinIterator.next().get("ids");
			
			//Block leading into the join
			JSONObject sourceBlock = (JSONObject) ids.get(0);
			int sourceID = Integer.parseInt(sourceBlock.get("id").toString());
			Block source = blocks.get(sourceID);
			
			//Blocks leading from the join
			ArrayList<Block> dest = new ArrayList<Block>();
			
			//Add Blocks leading out of the join
			for(int x = 1; x < ids.size(); x++){
				//Block leading out of block - add to adjacency for source block
				dest.add(blocks.get(Integer.parseInt(((JSONObject) ids.get(x)).get("id").toString())));
			}
			
			//Position based on the source Blocks ID
			joins.set(sourceID, new Join(source, dest));	
		}
	}
	
	public ArrayList<Engine> getTrains(){
		return trains;
	}
	
	public ArrayList<Join> getJoins(){
		return joins;
	}
	
	public ArrayList<Block> getBlocks(){
		return blocks;
	}
	
	
	
	
}
