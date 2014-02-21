package traindiagrams;

import java.awt.Shape;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.util.ShapeUtilities;

import bbentities.BlockOccupation;
import bbentities.Journey;

public class TrainDiagramCreator {

	public void drawDiagram(ArrayList<Journey> journeys, double id){
		File f = new File("chart" + id + ".jpg");
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
				
				series.add(b.getArrTime(), b.getBlock().getID());
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
		
		Shape cross = ShapeUtilities.createDiagonalCross(1, 1);
		
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		renderer.setSeriesShapesVisible(0, true);
		renderer.setSeriesShape(0, cross);
		renderer.setSeriesShape(1, cross);
		renderer.setSeriesShape(2, cross);
		
		chart.getXYPlot().setRenderer(renderer);
		
		try {
		ChartUtilities.saveChartAsJPEG(f, chart, 500, 300);
		} catch (IOException e) {
		System.err.println("Problem occurred creating chart.");
		e.printStackTrace();
		}
		
		
	}
	

}
