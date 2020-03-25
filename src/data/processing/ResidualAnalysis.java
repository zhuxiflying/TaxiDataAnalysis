package data.processing;

import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

/*
 * this class implement the methods calculate the residual (time series - trend) and the corresponding statistical metrics  
 */
public class ResidualAnalysis {

	private static String InflowDataFolder = "Time Series Data Folder";
	private static String TSDataFolder = "Temporal Signature Data Folder";
	private static String ResidualFolder = "Residual Data Folder";

	private static HashMap<Integer, HashMap<Integer, double[]>> time_data;

	public static void main(String[] args) throws IOException {

		for (int time = 0; time < 2555; time++) {
			int[] count = residualStatistic(time);
			System.out.println(time + "," + count[0] + "," + count[1]);
		}

		time_data = new HashMap<Integer, HashMap<Integer, double[]>>();

		for (int pid = 1; pid < 6661; pid++) {

			int[] ts = ResidualAnalysis.loadDailyInTimeSeries(pid);
			double[] signature = ResidualAnalysis.loadDailyInTemporalSignature(pid);

			for (int i = 0; i < ts.length; i++) {
				if (time_data.get(i) != null) {
					HashMap<Integer, double[]> pid_data = time_data.get(i);
					double[] data = { ts[i], signature[i] };
					pid_data.put(pid, data);
					time_data.put(i, pid_data);
				} else {
					HashMap<Integer, double[]> pid_data = new HashMap<Integer, double[]>();
					double[] data = { ts[i], signature[i] };
					pid_data.put(pid, data);
					time_data.put(i, pid_data);
				}
			}

		}

		for (Integer time : time_data.keySet()) {
			String url = ResidualFolder + time + ".csv";
			Writer fileWriter = Files.newBufferedWriter(Paths.get(url));
			CSVWriter writer = new CSVWriter(fileWriter);
			// feed in your array (or convert your data to an array)
			HashMap<Integer, double[]> pid_data = time_data.get(time);
			for (int pid : pid_data.keySet()) {
				String[] dataitem = new String[5];
				double[] data = pid_data.get(pid);
				dataitem[0] = String.valueOf(pid);
				dataitem[1] = String.valueOf(data[0]);
				dataitem[2] = String.valueOf(data[1]);
				dataitem[3] = String.valueOf(data[0] - data[1]);
				dataitem[4] = String.valueOf((data[0] - data[1]) / data[1]);
				writer.writeNext(dataitem);
			}
			writer.close();
		}

	}

	public static int[] residualStatistic(int time) throws IOException {
		String url = ResidualFolder + time + ".csv";
		CSVReader reader = new CSVReader(new FileReader(url));
		int index1 = 0;
		int index2 = 0;
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			double ts = Double.valueOf(nextLine[1]);
			double trend = Double.valueOf(nextLine[2]);
			double residual = Math.abs(ts - trend);
			double percentage = (ts - trend) / trend;
			if (residual > 20 && percentage > 0.1)
				index1++;
			if (residual > 20 && percentage < -0.1)
				index2++;
		}
		int[] data = { index1, index2 };
		return data;
	}

	// read inflow weekly time series
	public static int[] loadDailyInTimeSeries(int pid) throws IOException {
		String filename = InflowDataFolder + pid + ".csv";
		CSVReader reader = new CSVReader(new FileReader(filename));
		int[] ts = new int[2555];
		int index = 0;
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			if (index < 2555) {
				for (int j = 0; j < 168; j++) {
					int day = j / 24;
					ts[index + day] += Double.valueOf(nextLine[j]);
				}
			}
			index = index + 7;
		}
		return ts;
	}

	public static double[] loadDailyInTemporalSignature(int pid) throws IOException {
		String filename = TSDataFolder + pid + ".csv";
		CSVReader reader = new CSVReader(new FileReader(filename));
		double[] ts = new double[2555];
		int index = 0;
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			if (index < 2555) {
				for (int j = 0; j < 7; j++) {
					double value = Double.valueOf(nextLine[j]);
					if (Double.isNaN(value)) {
						value = 0;
					}
					ts[index + j] = value;
				}
			}
			index = index + 7;
		}
		return ts;
	}
}
