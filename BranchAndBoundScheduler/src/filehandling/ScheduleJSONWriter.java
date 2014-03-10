package filehandling;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import entities.BlockOccupation;
import entities.Journey;
import branchandboundcontrol.Node;

public class ScheduleJSONWriter {

	public static void writeJSONSchedule(Node node){
		
		JSONObject schedule = new JSONObject();
		
		JSONArray journeys = new JSONArray();
		
		for(Journey j: node.getJourneys()){
			JSONObject manifest = new JSONObject();
			manifest.put("trainName", j.getTrain().getName());
			manifest.put("trainID", j.getTrain().getID());
			
			JSONArray blocks = new JSONArray();
			JSONArray stations = new JSONArray();
			for(BlockOccupation b : j.getBlockOccupations()){
				JSONObject bo = new JSONObject();
				bo.put("block", b.getBlock().getID());
				bo.put("arrTime", b.getArrTime());
				bo.put("arrSpeed", b.getArrSpeed());
				bo.put("depTime", b.getDepTime());
				bo.put("depSpeed", b.getDepSpeed());
				blocks.add(bo);
				
				if(b.isStation()){
					JSONObject station = new JSONObject();
					station.put("stationID", b.getBlock().getID());
					station.put("arrival", b.getStationArrivalTime());
					station.put("stopLength", b.getStationStopTime());
					station.put("departure", b.getStationArrivalTime() + b.getStationStopTime());
					stations.add(station);
				}
			}
			manifest.put("blocks", blocks);
			manifest.put("stations", stations);
			journeys.add(manifest);
		}
		
		schedule.put("journeys", journeys);
		
		StringWriter out = new StringWriter();
		try {
			schedule.writeJSONString(out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String jsonText = out.toString();
		System.out.print(jsonText);
		
		File sch = new File("schedule/schedule.json");

		FileWriter write = null;
		PrintWriter print = null;
		try {
			write = new FileWriter(sch, false);

			print = new PrintWriter(write);

			print.write(jsonText);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (print != null) {
				print.close();
			}
		}
		
	}
}
