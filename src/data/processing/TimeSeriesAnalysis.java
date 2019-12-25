package data.processing;

import java.io.FileReader;
import java.io.IOException;

import com.opencsv.CSVReader;


/*
 * load time series from file
 * provide basic functions to manipulate the time series
 */
public class TimeSeriesAnalysis {
	
	
	private static String InflowDataFolder = "D:\\Data\\NY_Taxi_tripdata6_in\\";
	private static String OutflowDataFolder = "D:\\Data\\NY_Taxi_tripdata6_out\\";
	private static int rowNum = 366;
	private static int colNum = 168;
	private static int polyNum = 6662;
	
	public static void main(String[] args) throws IOException
	{
		
		for(int pid=1;pid<polyNum;pid++)
		{
			int[][] ts = loadInTimeSeries(pid);
//			int sumIn = sumTS(ts);
//			int[][] ts2 = loadOutTimeSeries(pid);
//			int sumOut = sumTS(ts2);
//			System.out.println(pid+","+sumIn+","+sumOut+","+(sumIn-sumOut));
			
			int[] sum = sumTSByRow(ts);
			System.out.print(pid);
			for(int i=0;i<sum.length;i++)
			{
				System.out.print(","+sum[i]);
			}
			System.out.println();
			
		}

	}
	
	private static int[][] loadInTimeSeries(int pid) throws IOException
	{
		String filename = InflowDataFolder+pid+".csv";
		CSVReader reader = new CSVReader(new FileReader(filename));
		int[][] ts = new int[rowNum][colNum];
		int index = 0;
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null&&index<rowNum) {
			
			for(int j=0;j<colNum;j++)
			{
				ts[index][j] = Integer.valueOf(nextLine[j]);
			}
			index++;
		}
		return ts;
	}
	
	private static int[][] loadOutTimeSeries(int pid) throws IOException
	{
		String filename = OutflowDataFolder+pid+".csv";
		CSVReader reader = new CSVReader(new FileReader(filename));
		int[][] ts = new int[rowNum][colNum];
		int index = 0;
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null&&index<rowNum) {
			
			for(int j=0;j<colNum;j++)
			{
				ts[index][j] = Integer.valueOf(nextLine[j]);
			}
			index++;
		}
		return ts;
	}
	
	private static int[] sumTSByRow(int[][] ts)
	{
		int[] sum = new int[rowNum];
		for(int i=0;i<rowNum;i++)
		{
			for(int j=0;j<colNum;j++)
			{
				sum[i] += ts[i][j];
			}
		}
		return sum;
	}
	
	private static int sumTS(int[][] ts)
	{
		int sum = 0;
		for(int i=0;i<rowNum;i++)
		{
			for(int j=0;j<colNum;j++)
			{
				sum += ts[i][j];
			}
		}
		return sum;
	}

}
