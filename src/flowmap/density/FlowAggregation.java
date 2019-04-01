package flowmap.density;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

import org.geotools.data.shapefile.files.ShpFiles;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import com.opencsv.CSVReader;

public class FlowAggregation {
	
	// folder in office computer
	private static String shapeFolder = "F:\\data\\MultiscaleFlowmap\\NewYork_shape\\";	
	private static String dataFolder = "F:\\data\\MultiscaleFlowmap\\data\\";
	private static String outputFolder = "F:\\data\\MultiscaleFlowmap\\output\\";

	private static ArrayList<Geometry> shapes;
	private static HashMap<Integer,double[]> id_centroid;
	private static HashMap<String, Integer> flowids_volume;

	public static void main(String[] args) {

		// load partition areas
		loadArea();
		System.out.println(shapes.size());
		
		flowids_volume = new HashMap<String, Integer>();

		String filename = dataFolder+"subset_31.csv";
		aggregateFlow(filename);

		String outfilename = outputFolder + "zipcode_flow.js";
		writeSelectedJSFile(outfilename);


	}
	
	
	/**
	 * This method output JS file 
	 * 
	 *@param output file path
	 *@exception this method throw file not found exception
	 */
	static void writeSelectedJSFile(String fileName){

		double[][] selected = new double[flowids_volume.keySet().size()][6];

		
		String filepath = shapeFolder+"ManhattanBoundarySimplified.shp";
		Geometry shape = null;
		try {
		ShpFiles f = new ShpFiles(filepath);
		GeometryFactory gf = new GeometryFactory();
		ShapefileReader r = new ShapefileReader(f, false, false, gf);
		shape = (Geometry) r.nextRecord().shape();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		double minx = shape.getEnvelopeInternal().getMinX();
		double miny = shape.getEnvelopeInternal().getMinY();
		double maxx = shape.getEnvelopeInternal().getMaxX();
		double maxy = shape.getEnvelopeInternal().getMaxY();
		
		int index = 0;
		
		for(String flowid:flowids_volume.keySet())
		{
			String[] ids = flowid.split(",");
			int id1 = Integer.valueOf(ids[0]);
			int id2 = Integer.valueOf(ids[1]);
			selected[index][0] = index;
			selected[index][1] = id_centroid.get(id1)[0];
			selected[index][2] = id_centroid.get(id1)[1];
			selected[index][3] = id_centroid.get(id2)[0];
			selected[index][4] = id_centroid.get(id2)[1];
			selected[index][5] = flowids_volume.get(flowid);
			index++;
		}

		// sort the flow data according to density value by descending order;
		java.util.Arrays.sort(selected, new java.util.Comparator<double[]>() {
			public int compare(double[] a, double[] b) {
				return Double.compare(b[5], a[5]);
			}
		});
		
		try
		{
	    BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
	    StringBuffer sb = new StringBuffer();
	    sb.append("var flows = [];");
	    
		double px1,py1,px2,py2;
		for (int i = 0; i < 300; i++) {
			px1 = (selected[i][1]-minx)/(maxx-minx)*1000;
			py1 = (maxy-selected[i][2])/(maxx-minx)*1000;
			px2 = (selected[i][3]-minx)/(maxx-minx)*1000;
			py2 = (maxy-selected[i][4])/(maxx-minx)*1000;	
			sb.append("flows["+i+"] = ["+px1+","+py1+","+px2+","+py2+","+selected[i][5]+"];");
		}
		
	    writer.write(sb.toString());
	    writer.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}

	/**
	 * This method loads spatial units, which used to aggregate flow dataset
	 * 
	 *@param specify the shapes to aggregate data, including shapefile and corresponding centroids.
	 *@exception this method throw file not found exception
	 */
	private static void loadArea() {
		
	//	String filepath = shapeFolder+"ManhattanBoundary_tracks_utm.shp";
		String filepath = shapeFolder+"ManhattanBoundary_zipcode.shp";
		shapes = new ArrayList<Geometry>();
		ShpFiles f;
		try {
			f = new ShpFiles(filepath);
			GeometryFactory gf = new GeometryFactory();
			ShapefileReader r = new ShapefileReader(f, false, false, gf);
			while (r.hasNext()) {
				Geometry shape = (Geometry) r.nextRecord().shape();
				shapes.add(shape);
			}
			r.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		id_centroid = new HashMap<Integer,double[]>();
		
//		String filename = shapeFolder+"Tracts_centroids.csv";
		String filename = shapeFolder+"Zipcode_centroids.csv";
		String[] nextLine;

		try {
			CSVReader reader = new CSVReader(new FileReader(filename));
			reader.readNext();
			
			int cid;
			double x, y;
			while ((nextLine = reader.readNext()) != null) {
				cid = Integer.valueOf(nextLine[0]);
				x = Double.valueOf(nextLine[1]);
				y = Double.valueOf(nextLine[2]);
				double[] centriod = {x,y};	
				id_centroid.put(cid, centriod);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method loads flow data, this method should be called after loadArea.
	 * 
	 *@param specify the fileName of flow data.
	 *@exception this method throw file not found exception
	 */
	private static void aggregateFlow(String fileName)
	{
		
		System.out.println("Data loading...");
		GeometryFactory gf = new GeometryFactory();
		String[] nextLine;
		int index = 0;
		int id;
		double x1, y1, x2, y2;
		
		try {
			CSVReader reader = new CSVReader(new FileReader(fileName));
			reader.readNext();
			Point origin, destination;

			while ((nextLine = reader.readNext()) != null) {

				id = Integer.valueOf(nextLine[2]);
				x1 = Double.valueOf(nextLine[3]);
				y1 = Double.valueOf(nextLine[4]);
				x2 = Double.valueOf(nextLine[5]);
				y2 = Double.valueOf(nextLine[6]);
				origin = gf.createPoint(new Coordinate(x1, y1));
				destination = gf.createPoint(new Coordinate(x2, y2));

				for (int i = 0; i < shapes.size(); i++) {
					if (shapes.get(i).contains(origin)) {
						for (int j = 0; j < shapes.size(); j++) {
							if (shapes.get(j).contains(destination)) {
								index++;
								String flowids = i + "," + j;
								if (flowids_volume.containsKey(flowids)) {
									int volume = flowids_volume.get(flowids);
									volume++;
									flowids_volume.put(flowids, volume);
								} else {
									flowids_volume.put(flowids, 1);
								}

							}
						}
					}
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * This method write flow list to csv file
	 * 
	 *@param specify the shapes to aggregate data, including shapefile and corresponding centroids.
	 *@exception this method throw file not found exception
	 */
	static void writeFlowCSVFile(String fileName){

		double[][] selected = new double[flowids_volume.keySet().size()][6];

		int index = 0;
		
		for(String flowid:flowids_volume.keySet())
		{
			String[] ids = flowid.split(",");
			int id1 = Integer.valueOf(ids[0]);
			int id2 = Integer.valueOf(ids[1]);
			selected[index][0] = index;
			selected[index][1] = id_centroid.get(id1)[0];
			selected[index][2] = id_centroid.get(id1)[1];
			selected[index][3] = id_centroid.get(id2)[0];
			selected[index][4] = id_centroid.get(id2)[1];
			selected[index][5] = flowids_volume.get(flowid);
			index++;
		}

		// sort the flow data according to density value by descending order;
		java.util.Arrays.sort(selected, new java.util.Comparator<double[]>() {
			public int compare(double[] a, double[] b) {
				return Double.compare(b[5], a[5]);
			}
		});
		
		
		

		
	}

}
