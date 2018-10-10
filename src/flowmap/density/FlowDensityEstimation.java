package flowmap.density;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

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
		double h = 300;

		// input flow data file:
		int hour = 139;
		String filename = "D:\\Projects\\MultiscaleFlowMap\\Data\\subset_" + hour + ".csv";
		String smoothed_flow = "D:\\Projects\\MultiscaleFlowMap\\Data2\\subset_" + hour + "_smoothed.csv";

		loadData(filename);

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
				double distance1 = Math.abs(x1 - flow[0]);
				double distance2 = Math.abs(y1 - flow[1]);
				double distance3 = Math.abs(x2 - flow[2]);
				double distance4 = Math.abs(y2 - flow[3]);

				// The multivariate Epanechnikov (multiplicative):
				double weight1 = 0.75 * (1 - Math.pow(distance1 / h, 2));
				double weight2 = 0.75 * (1 - Math.pow(distance2 / h, 2));
				double weight3 = 0.75 * (1 - Math.pow(distance3 / h, 2));
				double weight4 = 0.75 * (1 - Math.pow(distance4 / h, 2));
				density += weight1 * weight2 * weight3 * weight4;
			}
			flow_density.put(flowid, density);
		}
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
		// study area envelop in WGS1984_UTM18N projection
		Envelope itemEnv = new Envelope(582800, 589500, 4506000, 4516000);
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

				if (itemEnv.contains(x1, y1) && itemEnv.contains(x2, y2)) {

					double[] point = { x1, y1, x2, y2 };
					flow_data.put(id, point);
					try {
						kd.insert(point, id);
					} catch (KeyDuplicateException e) {
						// not handle duplicate flow data in this version, there is limited number of
						// duplicate flows
						//System.err.println(
						//		"KeyDuplicateException is thrown when the KDTree.insert method is invoked on a key already in the KDTree.");
					}
				}
			}
		} catch (Exception e) {
			// report IOException in the console
			e.printStackTrace();
		}
		
		System.out.println(kd.size()+ " flow data items loaded.");

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
