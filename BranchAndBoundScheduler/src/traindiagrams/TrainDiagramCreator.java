package traindiagrams;

import java.awt.Shape;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.util.ShapeUtilities;

import entities.BlockOccupation;
import entities.Journey;

public class TrainDiagramCreator {

	/**
	 * Creates a train diagram
	 * @param journeys The journeys the schedule consists of
	 * @param id File will be stored in files/chartid.jpg
	 */
	public void drawDiagram(ArrayList<Journey> journeys, String id){
		File f = new File("schedule/chart" + id + ".jpg");
		try {
			f.createNewFile();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		XYSeriesCollection dataset = new XYSeriesCollection();
		
		for(Journey j: journeys){
			XYSeries series = new XYSeries(j.getTrain().getName());
			for(BlockOccupation b: j.getBlockOccupations()){
				
				//Draws diagonal line (transit across block) 
				series.add(b.getArrTime(), b.getBlock().getID());
				
				if(b.isStation())
					series.add(b.getStationArrivalTime(), b.getBlock().getID() + 1);
				
				series.add(b.getDepTime(), b.getBlock().getID() + 1);
			}
			dataset.addSeries(series);
		}
		
		// Generate the graph
		JFreeChart chart = ChartFactory.createXYLineChart(
		"XY Chart",
		// Title
		"x-axis",
		// x-axis Label
		"y-axis",
		// y-axis Label
		dataset,
		// Dataset
		PlotOrientation.VERTICAL, // Plot Orientation
		true,
		// Show Legend
		true,
		// Use tooltips
		false
		// Configure chart to generate URLs?
		);
		
		//Show diamond at each block entry/exit
		Shape cross = ShapeUtilities.createDiagonalCross(1, 1);
		
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		
		for(int x = 0; x < dataset.getSeriesCount(); x++)
			renderer.setSeriesShape(x, cross);
		
		chart.getXYPlot().setRenderer(renderer);
		
		try {
		ChartUtilities.saveChartAsJPEG(f, chart, 500, 300);
		} catch (IOException e) {
		System.err.println("Problem occurred creating chart.");
		e.printStackTrace();
		}
		
		
	}
	

}
