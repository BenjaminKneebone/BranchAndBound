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

	JSONObject network;
	
	ArrayList<Engine> trains;
	ArrayList<Block> blocks;
	ArrayList<Join> joins;
	
	public NetworkJSONParser(){
		JSONParser jp = new JSONParser();
		
		try {
			Object obj = jp.parse(new FileReader("files/Network.json"));
			network = (JSONObject) obj;
		} catch (FileNotFoundException e) {
			System.out.println("Network.json not found");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			System.out.println("Error parsing Network.json");
		}
		
		createTrains();
		createBlocks();
		createJoins();
		
	}
	
	private void createTrains(){
		
		trains = new ArrayList<Engine>();
		
		JSONArray engines = (JSONArray) network.get("engines");
		//Initialize adjacency arraylist
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
			
			Engine temp = new Engine(id, Integer.parseInt(train.get("length").toString()), Integer.parseInt(train.get("weight").toString()), 
					Integer.parseInt(train.get("driving force").toString()), Integer.parseInt(train.get("braking force").toString()), 
					train.get("name").toString(), speedProfile);
			
			trains.set(id,temp);
		}
	}
	
	private void createBlocks(){
		
		blocks = new ArrayList<Block>();
		
		JSONArray blockObjects = (JSONArray) network.get("blocks");
		
		//Initialize adjacency arraylist
		for(int x = 0; x < blockObjects.size(); x++)
			blocks.add(new Block(0, 0, 0));
		
		
		@SuppressWarnings("unchecked")
		Iterator<JSONObject> blockIterator = blockObjects.iterator();
		
		int sourceID;
		
		while (blockIterator.hasNext()) {
			JSONObject block = blockIterator.next();
			sourceID = Integer.parseInt(block.get("id").toString());
			
			Block temp = new Block(sourceID, Integer.parseInt(block.get("length").toString()), 0);
			blocks.set(sourceID, temp);
		}
	}
	
	private void createJoins(){
		
		joins = new ArrayList<Join>();
		
		JSONArray blockObjects = (JSONArray) network.get("blocks");
		
		//Initialize adjacency arraylist
		for(int x = 0; x < blockObjects.size(); x++)
			joins.add(new Join(blocks.get(x), null));
		
		
		JSONArray joinObjects = (JSONArray) network.get("joins");
		
		@SuppressWarnings("unchecked")
		Iterator<JSONObject> joinIterator = joinObjects.iterator();
		
		
		
		//For each join
		while(joinIterator.hasNext()){
			//Get the blocks involved
			JSONArray ids = (JSONArray) joinIterator.next().get("ids");
			
			//Block leading into the join
			JSONObject sourceBlock = (JSONObject) ids.get(0);
			int sourceID = Integer.parseInt(sourceBlock.get("id").toString());
			Block source = blocks.get(sourceID);
			
			ArrayList<Block> dest = new ArrayList<Block>();
			
			//Block leading out of block - add to adjacency for source block
			JSONObject destBlock1 = (JSONObject) ids.get(1);
			int dest1ID = Integer.parseInt(destBlock1.get("id").toString());
			dest.add(blocks.get(dest1ID));
			
			//Possible 2nd lead out - add to adjacency for source block
			if(ids.size() == 3){
				JSONObject destBlock2 = (JSONObject) ids.get(2);
				int dest2ID = Integer.parseInt(destBlock2.get("id").toString());
				dest.add(blocks.get(dest2ID));
			}	
			
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
