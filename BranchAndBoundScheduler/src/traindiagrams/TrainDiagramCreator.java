package traindiagrams;

import java.awt.Shape;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.util.ShapeUtilities;

import entities.BlockOccupation;
import entities.Journey;

public class TrainDiagramCreator {

	int maxRange = 0;
	int maxBlock = 0;
	
	
	/**
	 * Creates a train diagram
	 * @param journeys The journeys the schedule consists of
	 * @param id File will be stored in files/chartid.jpg
	 */
	public void drawDiagram(ArrayList<Journey> journeys, String id){
		File f = new File("schedule/chart.jpg");
		try {
			f.createNewFile();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		XYSeriesCollection dataset = new XYSeriesCollection();
		
		for(Journey j: journeys){
			XYSeries series = new XYSeries(j.getTrain().getName());
			ArrayList<BlockOccupation> bo = j.getBlockOccupations();
			for(int x = 0; x < bo.size(); x++){
				
				//Draws diagonal line (transit across block) 
				if(x == 0)
					series.add(bo.get(x).getArrTime(), bo.get(x).getBlock().getID() - 1);
				else
					series.add(bo.get(x).getArrTime(), bo.get(x - 1).getBlock().getID());
				
				if(bo.get(x).isStation())
					series.add(bo.get(x).getStationArrivalTime(), bo.get(x).getBlock().getID());
				
				series.add(bo.get(x).getDepTime(), bo.get(x).getBlock().getID());
				
				if(bo.get(x).getDepTime() > maxRange){
					maxRange = (int) (bo.get(x).getDepTime() + 5);
				}
				
				if(bo.get(x).getBlock().getID() > maxBlock){
					maxBlock = bo.get(x).getBlock().getID();
				}
				
			}
			dataset.addSeries(series);
		}
		
		// Generate the graph
		JFreeChart chart = ChartFactory.createXYLineChart(
		"Train Diagram",
		// Title
		"Time (Seconds)",
		// x-axis Label
		"Block",
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
		
		NumberAxis xAxis = new NumberAxis();
		xAxis.setAutoRange(false);
		xAxis.setRange(0, maxRange);
		chart.getXYPlot().setDomainAxis(xAxis);
		NumberAxis yAxis = new NumberAxis();
		yAxis.setAutoRange(false);
		yAxis.setRange(-1, maxBlock+1);
		yAxis.setTickUnit(new NumberTickUnit(1));
		chart.getXYPlot().setRangeAxis(yAxis);
		
		try {
		ChartUtilities.saveChartAsJPEG(f, chart, 500, 300);
		} catch (IOException e) {
		System.err.println("Problem occurred creating chart.");
		e.printStackTrace();
		}
		
		
	}
	

}
