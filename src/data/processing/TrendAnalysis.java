package data.processing;

import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.math3.special.Erf;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;


public class TrendAnalysis {

	private static String InflowDataFolder = "";
	private static String resultFolder = "";
	private static HashMap<Integer, Integer> polygon_density;
	private static HashMap<Integer, double[]> polygon_results;

	public static void main(String[] args) throws IOException {

		loadDensity();

		//Time range for the experiments.
		int startIndex = 286;
		int endIndex = 338;
		
		//bandwidth parameter (h = 52 in the manuscript); the portion of the whole dataset; 
		double bandwidth = 0.142;

		int[] ts_all = loadAllWeeklyTS();
		double[] factors = loadAdjustFactor();

		polygon_results = new HashMap<Integer, double[]>();

		for (int pid = 1; pid < 6661; pid++) {

			double[] ts = TrendAnalysis.loadWeeklyInTimeSeries(pid);
			Lowess low1 = new Lowess(bandwidth);
			double[] trend = low1.smooth(ts);

			double[] segments = Arrays.copyOfRange(ts, startIndex, endIndex);
			double pvalue = MannKendallTest(segments);
			double[] dataItem = new double[3];

			dataItem[0] = trend[endIndex - 1] - trend[startIndex];
			dataItem[1] = (trend[endIndex - 1] - trend[startIndex]) / trend[startIndex];
			dataItem[2] = pvalue;
			polygon_results.put(pid, dataItem);
		}

		writeTrendTest(startIndex, endIndex);

	}

	// write trend test results
	private static void writeTrendTest(int startIndex, int endIndex) throws IOException {
		// TODO Auto-generated method stub
		String url = resultFolder + "trendTest" + startIndex + "_" + endIndex + "noNorm.csv";
		Writer fileWriter = Files.newBufferedWriter(Paths.get(url));
		CSVWriter writer = new CSVWriter(fileWriter);
		// feed in your array (or convert your data to an array)

		String[] columName = { "id", "differ", "ratio", "pvalue" };
		writer.writeNext(columName);
		for (int id : polygon_results.keySet()) {
			String[] dataitem = new String[4];
			dataitem[0] = String.valueOf(id);
			dataitem[1] = String.valueOf(polygon_results.get(id)[0]);
			dataitem[2] = String.valueOf(polygon_results.get(id)[1]);
			dataitem[3] = String.valueOf(polygon_results.get(id)[2]);
			writer.writeNext(dataitem);
		}
		writer.close();
	}

	// local trend detection with global trend normalization
	private static double[] loadAdjustFactor() throws IOException {
		int[] ts_all = loadAllWeeklyTS();
		double[] factor = new double[ts_all.length];

		int sum = 0;
		for (int i = 0; i < ts_all.length; i++) {
			sum += ts_all[i];
		}

		double mean = (double) sum / (double) ts_all.length;

		for (int i = 0; i < ts_all.length; i++) {
			factor[i] = ts_all[i] / mean;
		}

		return factor;

	}

	// The Mann-Kendall test implementation, p value returned
	public static double MannKendallTest(double[] ts) {
		int s = 0;
		int n = ts.length;
		for (int i = 0; i < n - 1; i++) {
			for (int j = i + 1; j < n; j++) {
				if (ts[j] < ts[i]) {
					s--;
				} else if (ts[j] > ts[i]) {
					s++;
				} else {

				}
			}
		}

		double variance = n * (n - 1) * (2 * n + 5) / 18.0;
		double zscore = 0;
		if (s < 0)
			zscore = (s - 1) / Math.sqrt(variance);

		if (s > 0)
			zscore = (s + 1) / Math.sqrt(variance);
		zscore = zscore / Math.sqrt(2.0);
		double Pvalue = 0.0;
		Pvalue = 1 - Math.abs(Erf.erf(zscore));
		return Pvalue;
	}

	// load weekly inflow time series
	private static int[] loadAllWeeklyTS() throws IOException {
		String filename = "D:\\Projects\\TimeSeriesSignature\\data\\weeklyTS_all.csv";
		CSVReader reader = new CSVReader(new FileReader(filename));
		reader.readNext();
		int[] ts = new int[365];
		int index = 0;
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {

			if (index < ts.length) {
				ts[index] = Integer.valueOf(nextLine[0]);
			}
			index++;
		}
		return ts;
	}

	// read inflow weekly time series
	public static int[] loadDailyInTimeSeries(int pid) throws IOException {
		String filename = InflowDataFolder + pid + ".csv";
		CSVReader reader = new CSVReader(new FileReader(filename));
		int[] ts = new int[365];
		int index = 0;
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			if (index < 365) {
				for (int j = 0; j < 168; j++) {
					ts[index] += Double.valueOf(nextLine[j]);
				}
			}
			index++;
		}
		return ts;
	}

	// read inflow weekly time series
	public static double[] loadWeeklyInTimeSeries(int pid) throws IOException {
		String filename = InflowDataFolder + pid + ".csv";
		CSVReader reader = new CSVReader(new FileReader(filename));
		double[] ts = new double[365];
		int index = 0;
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			if (index < 365) {
				for (int j = 0; j < 168; j++) {
					ts[index] += Double.valueOf(nextLine[j]);
				}
			}
			index++;
		}
		return ts;
	}

	// read outflow weekly time series
	public static int[] loadWeeklyOutTimeSeries(int pid) throws IOException {
		String filename = InflowDataFolder + pid + ".csv";
		CSVReader reader = new CSVReader(new FileReader(filename));
		int[] ts = new int[365];
		int index = 0;
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			if (index < 365) {
				for (int j = 0; j < 168; j++) {
					ts[index] += Double.valueOf(nextLine[j]);
				}
			}
			index++;
		}
		return ts;
	}

	// read inflow weekly time series for the entire data set
	public static int[] loadWeeklyAllInTimeSeries(int pid) throws IOException {
		String filename = InflowDataFolder + pid + ".csv";
		CSVReader reader = new CSVReader(new FileReader(filename));
		int[] ts = new int[365];
		int index = 0;
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			if (index < 365) {
				for (int j = 0; j < 168; j++) {
					ts[index] += Double.valueOf(nextLine[j]);
				}
			}
			index++;
		}
		return ts;
	}

	// Normalize inflow time series by global trend
	public static double[] normalizeInflowTimeSeries(int[] ts) throws IOException {
		double[] ts_norm = new double[ts.length];
		int[] all_ts = loadAllInflowTimeSeries();
		for (int i = 0; i < ts.length; i++) {
			ts_norm[i] = ts[i] / (double) all_ts[i];
		}
		return ts_norm;
	}

	// read global time series
	public static int[] loadAllInflowTimeSeries() throws IOException {
		String filename = "Global Time Series File";
		CSVReader reader = new CSVReader(new FileReader(filename));
		reader.readNext();
		int[] ts = new int[365];
		int index = 0;
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			if (index < 365) {
				ts[index] = Integer.valueOf(nextLine[0]);
			}
			index++;
		}
		return ts;
	}

	// load spatial density
	private static void loadDensity() throws IOException {
		polygon_density = new HashMap<Integer, Integer>();
		String filename = "Spatial Density File";
		CSVReader reader = new CSVReader(new FileReader(filename));
		reader.readNext();
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			int id = Integer.valueOf(nextLine[0]);
			int inflow_density = Integer.valueOf(nextLine[1]);
			polygon_density.put(id, inflow_density);
		}
	}
}
