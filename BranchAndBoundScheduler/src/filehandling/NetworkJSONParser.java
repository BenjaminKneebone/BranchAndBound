package filehandling;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import entities.Block;
import entities.Engine;

public class NetworkJSONParser {

	JSONObject network;
	
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
		
		System.out.println(network.toString());
	}
	
	public ArrayList<Engine> getTrains(){
		
		ArrayList<Engine> trains = new ArrayList<Engine>();
		
		JSONArray engines = (JSONArray) network.get("engines");
		
		@SuppressWarnings("unchecked")
		Iterator<JSONObject> iterator = engines.iterator();
		
		while (iterator.hasNext()) {
			JSONObject train = iterator.next();
			
			JSONArray speed = (JSONArray) train.get("speed");
			int[] speedProfile = new int[10];
			for(int x = 0; x < speed.size(); x++)
				speedProfile[x] = Integer.parseInt(speed.get(x).toString());
			
			Engine temp = new Engine(Integer.parseInt(train.get("id").toString()), Integer.parseInt(train.get("id").toString()), Integer.parseInt(train.get("id").toString()), speedProfile);
			trains.add(temp);
		}
		
		Collections.sort(trains, new Comparator<Engine>() {
			@Override
			public int compare(Engine a, Engine b) {
				return a.getID() < b.getID() ? -1 : a.getID() == b.getID() ? 0: 1;
			}
		});
		
		
		
		return trains;
	}
	
	public ArrayList<Block> getBlocks(){
		
		JSONArray blockObjects = (JSONArray) network.get("blocks");
		ArrayList<Block> blocks = new ArrayList<Block>(blockObjects.size());
		
		@SuppressWarnings("unchecked")
		Iterator<JSONObject> iterator = blockObjects.iterator();
		
		while (iterator.hasNext()) {
			JSONObject block = iterator.next();
			Block temp = new Block(Integer.parseInt(block.get("id").toString()), Integer.parseInt(block.get("length").toString()));
			blocks.add(temp);
		}
		
		Collections.sort(blocks, new Comparator<Block>() {
			@Override
			public int compare(Block a, Block b) {
				return a.getID() < b.getID() ? -1 : a.getID() == b.getID() ? 0: 1;
			}
		});
		
		return blocks;
	}
	
	
	
	
}
