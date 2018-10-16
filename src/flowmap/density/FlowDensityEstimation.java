package flowmap.density;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.vividsolutions.jts.geom.Envelope;

import de.biomedical_imaging.edu.wlu.cs.levy.CG.KDTree;
import de.biomedical_imaging.edu.wlu.cs.levy.CG.KeyDuplicateException;
import de.biomedical_imaging.edu.wlu.cs.levy.CG.KeySizeException;

public class FlowDensityEstimation {

	private static HashMap<Integer, Double> flow_density;
	static KDTree<Integer> kd;
	static HashMap<Integer, double[]> flow_data;

	public static void main(String[] args) throws Exception {
		// kernel bandwidth parameter
		double h=300;

		// input flow data file:
		int hour = 139;
		String filename = "D:\\Projects\\MultiscaleFlowMap\\Data\\subset_" + hour + ".csv";
		String smoothed_flow = "D:\\Projects\\MultiscaleFlowMap\\Data2\\subset_" + hour + "_smoothed.csv";

		loadData(filename);

//		h = getParameterSetting();

		flowSmoothing(h);

		writeFile(smoothed_flow);
	}

	// flow density estimation based on KD tree index
	// The multivariate Epanechnikov (multiplicative) adopted as the kernel model
	static void flowSmoothing(double h) {

		flow_density = new HashMap<Integer, Double>();
		double x1, y1, x2, y2;

		System.out.println("Flow data density estimation...");

		for (Integer flowid : flow_data.keySet()) {
			if(flowid%1000==0)System.out.println(flowid);
			x1 = flow_data.get(flowid)[0];
			y1 = flow_data.get(flowid)[1];
			x2 = flow_data.get(flowid)[2];
			y2 = flow_data.get(flowid)[3];

			double[] point_low = { x1 - h, y1 - h, x2 - h, y2 - h };
			double[] point_up = { x1 + h, y1 + h, x2 + h, y2 + h };
			List<Integer> nearflow = null;
			try {
				nearflow = kd.range(point_low, point_up);
			} catch (KeySizeException e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
				System.err.println(
						"KeySizeException is thrown when a KDTree method is invoked on a key whose size (array length) mismatches the one used in the that KDTree's constructor.");
			}

			double density = 0;
			for (Integer nearflow_id : nearflow) {
				double[] flow = flow_data.get(nearflow_id);
				double volume = flow[4];
				double distance1 = Math.abs(x1 - flow[0]);
				double distance2 = Math.abs(y1 - flow[1]);
				double distance3 = Math.abs(x2 - flow[2]);
				double distance4 = Math.abs(y2 - flow[3]);

				// The multivariate Epanechnikov (multiplicative):
				double weight1 = 0.75 * (1 - Math.pow(distance1 / h, 2));
				double weight2 = 0.75 * (1 - Math.pow(distance2 / h, 2));
				double weight3 = 0.75 * (1 - Math.pow(distance3 / h, 2));
				double weight4 = 0.75 * (1 - Math.pow(distance4 / h, 2));
				density += volume * weight1 * weight2 * weight3 * weight4;
			}
			flow_density.put(flowid, density);
		}
	}

	// flow density estimation based on KD tree index
	// The univariate model adopted as the kernel model
	static void flowSmoothingUniModel(double h) {

		flow_density = new HashMap<Integer, Double>();
		double x1, y1, x2, y2;

		System.out.println("Flow data density estimation...");

		for (Integer flowid : flow_data.keySet()) {

			x1 = flow_data.get(flowid)[0];
			y1 = flow_data.get(flowid)[1];
			x2 = flow_data.get(flowid)[2];
			y2 = flow_data.get(flowid)[3];
			double[] point_low = { x1 - h, y1 - h, x2 - h, y2 - h };
			double[] point_up = { x1 + h, y1 + h, x2 + h, y2 + h };

			List<Integer> nearflow = null;
			try {
				nearflow = kd.range(point_low, point_up);
			} catch (KeySizeException e) {
				// TODO Auto-generated catch block
//					e.printStackTrace();
				System.err.println(
						"KeySizeException is thrown when a KDTree method is invoked on a key whose size (array length) mismatches the one used in the that KDTree's constructor.");
			}

			double density = 0;
			for (Integer nearflow_id : nearflow) {
				double[] flow = flow_data.get(nearflow_id);
				double volume = flow[4];
				double distance1 = Math.abs(x1 - flow[0]);
				double distance2 = Math.abs(y1 - flow[1]);
				double distance3 = Math.abs(x2 - flow[2]);
				double distance4 = Math.abs(y2 - flow[3]);
				double distance = Math.sqrt(
						distance1 * distance1 + distance2 * distance2 + distance3 * distance3 + distance4 * distance4);

				if (distance < h) {
					double weight = 1 - distance / h;
//					double weight = 1;
					density += volume * weight;
				}
			}
			flow_density.put(flowid, density);
		}
	}

	// A rule-of-thumb bandwidth estimator, a data driven bandwidth selection method
	private static double getParameterSetting() {
		// TODO Auto-generated method stub
		double x1, y1, x2, y2, weight;
		double meanX1 = 0, meanY1 = 0, meanX2 = 0, meanY2 = 0;
		int n = 0;

		// calculate the mean of the data distribution
		for (Integer flowid : flow_data.keySet()) {
			x1 = flow_data.get(flowid)[0];
			y1 = flow_data.get(flowid)[1];
			x2 = flow_data.get(flowid)[2];
			y2 = flow_data.get(flowid)[3];
			weight = flow_data.get(flowid)[4];

			meanX1 += x1 * weight;
			meanY1 += y1 * weight;
			meanX2 += x2 * weight;
			meanY2 += y2 * weight;
			n += weight;
		}

		meanX1 = meanX1 / n;
		meanY1 = meanY1 / n;
		meanX2 = meanX2 / n;
		meanY2 = meanY2 / n;

		// calculate the standard deviation of the data distribution
		DescriptiveStatistics ds = new DescriptiveStatistics();

		for (Integer flowid : flow_data.keySet()) {
			x1 = flow_data.get(flowid)[0];
			y1 = flow_data.get(flowid)[1];
			x2 = flow_data.get(flowid)[2];
			y2 = flow_data.get(flowid)[3];
			double distance1 = Math.abs(x1 - meanX1);
			double distance2 = Math.abs(y1 - meanY1);
			double distance3 = Math.abs(x2 - meanX2);
			double distance4 = Math.abs(y2 - meanY2);

			double distance = Math.sqrt(
					distance1 * distance1 + distance2 * distance2 + distance3 * distance3 + distance4 * distance4);
			ds.addValue(distance);
		}

		double std = ds.getStandardDeviation();
		double median = ds.getPercentile(50);

		// The default search radius (bandwidth) is computed specifically to the input
		// dataset using a spatial variant of Silverman's Rule of Thumb that is robust
		// to spatial outliers (that is, points that are far away from the rest of the
		// points). 
		//reference: http://desktop.arcgis.com/en/arcmap/10.3/tools/spatial-analyst-toolbox/how-kernel-density-works.htm
		double h = 0.9 * Math.min(std, Math.sqrt(1.0 / Math.log(2)) * median) * Math.pow(1.0 / n, 0.2);

//		System.out.println("Data item n:" + n);
//		System.out.println("Data center:" + meanX1 + "," + meanY1 + "," + meanX2 + "," + meanY2);
//		System.out.println("Data standard deviation:" + std);
		System.out.println("Suggested bandwidth:" + h);

		return h;

	}

	// load the flow data into hash map and kd index tree
	// an Envelope as a filter to select data within the study area
	// IO exception reported but not handled in this method
	// KeyDuplicateException for kd tree reported but not handled
	static void loadData(String filename) {

		flow_data = new HashMap<Integer, double[]>();
		kd = new KDTree<Integer>(4);

		int id;
		double x1, y1, x2, y2;

		int num = 0; // duplicate number
		// study area envelop in WGS1984_UTM18N projection
//		Envelope itemEnv = new Envelope(582800, 589500, 4506000, 4516000);
		String[] nextLine;

		System.out.println("Data loading...");

		try {
			CSVReader reader = new CSVReader(new FileReader(filename));
			reader.readNext();

			while ((nextLine = reader.readNext()) != null) {

				id = Integer.valueOf(nextLine[2]);
				x1 = Double.valueOf(nextLine[3]);
				y1 = Double.valueOf(nextLine[4]);
				x2 = Double.valueOf(nextLine[5]);
				y2 = Double.valueOf(nextLine[6]);

//				if (itemEnv.contains(x1, y1) && itemEnv.contains(x2, y2)) {

				double[] point = { x1, y1, x2, y2 };
				try {
					kd.insert(point, id);
					double[] flow = { x1, y1, x2, y2, 1 };
					flow_data.put(id, flow);
				} catch (KeyDuplicateException e) {
					// add weight for flow if there is duplicate key

					int did = kd.nearest(point);
					double[] flow = flow_data.get(did);
					flow[4] = 2;
					flow_data.put(did, flow);
					num++;
				}
//				}
			}
		} catch (Exception e) {
			// report IOException in the console
			e.printStackTrace();
		}

		System.out.println(kd.size() + " flow and " + num + "  (duplicate) data items loaded.");

	}

	// write the flow data with density values into file
	// IO exception reported but not handled in this method
	static void writeFile(String outputfile) {

		System.out.println("Save density estimation result into file...");
		CSVWriter writer;
		try {
			writer = new CSVWriter(new FileWriter(outputfile));

			// the header of the output file
			String[] entries = new String[6];
			entries[0] = "id";
			entries[1] = "x1";
			entries[2] = "y1";
			entries[3] = "x2";
			entries[4] = "y2";
			entries[5] = "density";
			writer.writeNext(entries);

			for (Integer flow : flow_density.keySet()) {
				double density = flow_density.get(flow);
				String[] nextData = new String[6];
				nextData[0] = String.valueOf(flow);
				nextData[1] = String.valueOf(flow_data.get(flow)[0]);
				nextData[2] = String.valueOf(flow_data.get(flow)[1]);
				nextData[3] = String.valueOf(flow_data.get(flow)[2]);
				nextData[4] = String.valueOf(flow_data.get(flow)[3]);
				nextData[5] = String.valueOf(density);
				writer.writeNext(nextData);
			}
			writer.close();

		} catch (IOException e) {
			// report IOException in the console
			e.printStackTrace();
		}
	}
}
