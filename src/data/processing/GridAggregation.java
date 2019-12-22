package data.processing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.strtree.STRtree;


//aggregate the hourly data by grids.
public class GridAggregation {
	
	static STRtree tree;
	static HashMap<Integer,double[]> points;

	static String dataFolder = "D:\\Data\\NY_Taxi_tripdata2\\";
	static String dataFolder2 = "D:\\Data\\NY_Taxi_tripdata3\\";
	
	public static void main(String[] args) throws Exception
	{
		

		
		//load the grid centroids
		loadSamplingPoints();
		System.out.println(tree.size());
		
		File folder = new File(dataFolder);
		
		for (final File fileEntry : folder.listFiles()) {

			String fileName = fileEntry.getName();
			String timeId = fileName.substring(0, fileName.lastIndexOf('.'));
			System.out.println(timeId);
			processData(timeId);
	    }
		
//		for(int i=0;i<2;i++)
//		{
//			loadData(i);
//			System.out.println(i);
//		}
		
	}
	
	
	private static void processData(String timeId) throws Exception {
		// TODO Auto-generated method stub
		String filename = dataFolder+timeId+".csv";
		CSVReader reader = new CSVReader(new FileReader(filename));
		
		String outputfile = dataFolder2 + timeId + ".csv";
		CSVWriter writer = new CSVWriter(new FileWriter(outputfile));
		// feed in your array (or convert your data to an array)
		String[] entries = new String[5];
		
		entries[0] = "HourId";
		entries[1] = "Trip_Duration";
		entries[2] = "Trip_Distance";
		entries[3] = "OriginId";
		entries[4] = "DestinationId";
		writer.writeNext(entries);
		
		reader.readNext();
		String[] nextLine;
		double x,y;
		Envelope itemEnv;;
		List result;
		while ((nextLine = reader.readNext()) != null) {
			
			x = Double.valueOf(nextLine[2]);
			y = Double.valueOf(nextLine[3]);
			itemEnv = new Envelope(x-5,x+5,y-5,y+5);
			result = tree.query(itemEnv);
			if(result.size()==1)
			{
				int oid = (int) result.get(0);
				
				x = Double.valueOf(nextLine[4]);
				y = Double.valueOf(nextLine[5]);
				itemEnv = new Envelope(x-5,x+5,y-5,y+5);
				result = tree.query(itemEnv);
				if(result.size()==1)
				{
					int did = (int) result.get(0);
					String[] dataitem = new String[5];
					dataitem[0] = timeId;
					dataitem[1] = nextLine[0];
					dataitem[2] = nextLine[1];
					dataitem[3] = String.valueOf(oid);
					dataitem[4] = String.valueOf(did);
					writer.writeNext(dataitem);
				}
				
			}
			

		}
		
		writer.close();
	}


	static void loadSamplingPoints() throws Exception
	{
		String filename = "D:\\Data\\Grids\\GridPoints.csv";
		CSVReader reader = new CSVReader(new FileReader(filename));
		reader.readNext();
		String[] nextLine;

		int id;
		double x,y;
		tree = new STRtree();
		points = new HashMap<Integer,double[]>();
		Envelope itemEnv;;
		while ((nextLine = reader.readNext()) != null) {
			
			id = Integer.valueOf(nextLine[0]);
			x = Double.valueOf(nextLine[1]);
			y = Double.valueOf(nextLine[2]);
			double[] point = {x,y};
			points.put(id, point);
			itemEnv = new Envelope(x,x,y,y);
			tree.insert(itemEnv, id);
		}
	}

}
