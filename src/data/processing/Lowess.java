package data.processing;

import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.math3.analysis.interpolation.LoessInterpolator;

import com.opencsv.CSVReader;

/*
 * locally weighted regression and smoothing scatterplots references: (Cleveland, W. S. (1979))
 * time series smoothing using loess interpolation implemented by apache math package. 
 * produce the same result as the package lowess in gplots v3.0.1.1  in R 
 */
public class Lowess {

	// the smoother span. This gives the proportion of points in the plot which
	// influence the smooth at each value. Larger values give more smoothness.
	// bandwidth is the h parameter in the paper
	private static double bandwidth = 0.142;

	// the number of robustifying iterations which should be performed.
	private static int iterative = 10;

	public Lowess() {

	}

	public Lowess(double h) {
		this.bandwidth = h;
	}

	public static void main(String[] args) throws IOException {

		int id = 1040;
		// data load and preprocess
		int[][] ts = TimeSeriesAnalysis.loadInTimeSeries(id);
		int[] ts_weekly = TimeSeriesAnalysis.sumTSByRow(ts);
		Lowess lo = new Lowess(0.142);
		double[] trend = lo.smooth(ts_weekly);

		for (int i = 0; i < trend.length; i++) {
			System.out.println(ts_weekly[i]);
		}
	}

	// time series loader
	private static int[] loadTimeSeries() throws IOException {
		String filename = "Time Series File";
		CSVReader reader = new CSVReader(new FileReader(filename));
		int[] ts = new int[365];
		int index = 0;
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			ts[index] = Integer.valueOf(nextLine[0]);
			index++;
		}
		return ts;
	}

	// lowess smoother
	public static double[] smooth(double[] ts) {
		LoessInterpolator ls = new LoessInterpolator(bandwidth, iterative);

		double[] xval = new double[ts.length];
		double[] yval = new double[ts.length];

		for (int i = 0; i < ts.length; i++) {
			xval[i] = Double.valueOf(i);
			yval[i] = Double.valueOf(ts[i]);
		}

		double[] values = ls.smooth(xval, yval);
		return values;
	}

	// lowess smoother
	public double[] smooth(int[] ts) {
		LoessInterpolator ls = new LoessInterpolator(bandwidth, iterative);

		double[] xval = new double[ts.length];
		double[] yval = new double[ts.length];

		for (int i = 0; i < ts.length; i++) {
			xval[i] = Double.valueOf(i);
			yval[i] = Double.valueOf(ts[i]);
		}

		double[] values = ls.smooth(xval, yval);
		return values;
	}

}
