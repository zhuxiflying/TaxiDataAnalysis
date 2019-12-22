package data.processing.local;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;

import com.opencsv.CSVReader;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.strtree.STRtree;

public class GridAggregation {
	
	static STRtree tree;
	static HashMap<Integer,double[]> points;
	
	public static void main(String[] args) throws Exception
	{
		
		HashMap<Integer,int[]> grids = new HashMap<Integer,int[]>();
		
		loadSamplingPoints();
		System.out.println(tree.size());
		String filename = "D:\\Projects\\MultiscaleFlowMap\\Data2\\raw\\subset_31.csv";
		CSVReader reader = new CSVReader(new FileReader(filename));
		reader.readNext();
		String[] nextLine;

		int id;
		double x,y;
//		tree = new STRtree();
		Envelope itemEnv;;
		List result;
		while ((nextLine = reader.readNext()) != null) {
			
			x = Double.valueOf(nextLine[3]);
			y = Double.valueOf(nextLine[4]);
			itemEnv = new Envelope(x-5,x+5,y-5,y+5);
			result = tree.query(itemEnv);
			if(result.size()==1)
			{
				int idd = (int) result.get(0);
				if(grids.get(idd)!=null)
				{
					int[] flow_volume = grids.get(idd);
					flow_volume[1]++;
					grids.put(idd,flow_volume);
				}
				else
				{
					int[] flow_volume = new int[2];
					flow_volume[1]=1;
					grids.put(idd,flow_volume);
				}
			}
			
			x = Double.valueOf(nextLine[5]);
			y = Double.valueOf(nextLine[6]);
			itemEnv = new Envelope(x-5,x+5,y-5,y+5);
			result = tree.query(itemEnv);
			if(result.size()==1)
			{
				int idd = (int) result.get(0);
				if(grids.get(idd)!=null)
				{
					int[] flow_volume = grids.get(idd);
					flow_volume[0]++;
					grids.put(idd,flow_volume);
				}
				else
				{
					int[] flow_volume = new int[2];
					flow_volume[0]=1;
					grids.put(idd,flow_volume);
				}
			}
		}
		
		for(Integer idd:grids.keySet())
		{
			System.out.println(idd+","+points.get(idd)[0]+","+points.get(idd)[1]+","+grids.get(idd)[0]+","+grids.get(idd)[1]+","+(grids.get(idd)[0]-grids.get(idd)[1]));
		}
	}
	
	
	static void loadSamplingPoints() throws Exception
	{
		String filename = "D:\\Projects\\TimeSeriesSignature\\data\\Grids_points.csv";
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
