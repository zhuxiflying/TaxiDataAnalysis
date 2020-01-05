package data.processing;

import java.io.IOException;

import org.apache.commons.math3.analysis.interpolation.LoessInterpolator;

/*
 * locally weighted regression and smoothing scatterplots references: (Cleveland, W. S. (1979))
 * time series smoothing using loess interpolation implemented by apache math package. 
 * produce the same result as the package lowess in gplots v3.0.1.1  in R 
 */
public class Lowess {
	
	//the smoother span. This gives the proportion of points in the plot which influence the smooth at each value. Larger values give more smoothness.
	private static double bandwidth = 0.1;
	
	//the number of robustifying iterations which should be performed.
	private static int iterative = 4;
	
	public static void main(String[] args) throws IOException
	{
		//data load and preprocess
		TimeSeriesAnalysis ta = new TimeSeriesAnalysis();
		int[][] ts = ta.loadInTimeSeries(627);
		int[] ts2 = ta.sumTSByRow(ts);

		double[] trend = lowess(ts2);
	}
	
	private static double[] lowess(int[] ts)
	{
		LoessInterpolator ls = new LoessInterpolator(bandwidth,iterative);
		
		double[] xval = new double[ts.length];
		double[] yval = new double[ts.length];
		
		for(int i=0;i<ts.length;i++)
		{
			xval[i] = Double.valueOf(i);
			yval[i] = Double.valueOf(ts[i]);
		}
		
	    double[] values = ls.smooth(xval, yval);
	    return values;
	}

}
