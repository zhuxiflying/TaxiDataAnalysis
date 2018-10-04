package flowmap.density;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;



import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.vividsolutions.jts.geom.Envelope;

import de.biomedical_imaging.edu.wlu.cs.levy.CG.KDTree;
import de.biomedical_imaging.edu.wlu.cs.levy.CG.KeyDuplicateException;

public class FlowDensityEstimation {

	private static HashMap<Integer, Double> flow_density;
	static KDTree<Integer> kd;
	static HashMap<Integer, double[]> flow_data;

	public static void main(String[] args) throws Exception {

		double h = 300;

		int hour = 139;
		String filename = "D:\\Projects\\MultiscaleFlowMap\\Data\\subset_" + hour + ".csv";

		loadData(filename);

		flow_density = new HashMap<Integer, Double>();

		CSVReader reader = new CSVReader(new FileReader(filename));
		String[] nextLine;
		reader.readNext();

		int id1;
		double x1, y1, x2, y2;

		while ((nextLine = reader.readNext()) != null) {
            
			id1 = Integer.valueOf(nextLine[2]);
			x1 = Double.valueOf(nextLine[3]);

			y1 = Double.valueOf(nextLine[4]);

			x2 = Double.valueOf(nextLine[5]);

			y2 = Double.valueOf(nextLine[6]);

			double[] point_low = { x1 - h, y1 - h, x2 - h, y2 - h };
			double[] point_up = { x1 + h, y1 + h, x2 + h, y2 + h };
			List<Integer> nearflow = kd.range(point_low, point_up);

			double density = 0;
			for (Integer id : nearflow) {
				double[] sp = flow_data.get(id);
				double distance1 = Math.abs(x1 - sp[0]);
				double distance2 = Math.abs(y1 - sp[1]);
				double distance3 = Math.abs(x2 - sp[2]);
				double distance4 = Math.abs(y2 - sp[3]);
			
				//The multivariate Epanechnikov (multiplicative):
				double weight1 = 0.75*(1 - Math.pow(distance1 / h, 2));
				double weight2 = 0.75*(1 - Math.pow(distance2 / h, 2));
				double weight3 = 0.75*(1 - Math.pow(distance3 / h, 2));
				double weight4 = 0.75*(1 - Math.pow(distance4 / h, 2));
				density += weight1 * weight2 * weight3 * weight4;
				
			}
				flow_density.put(id1, density);
		}
		String outputfile = "D:\\Projects\\MultiscaleFlowMap\\Data2\\subset_" + hour + "_smoothed.csv";
		CSVWriter writer = new CSVWriter(new FileWriter(outputfile));
		String[] entries = new String[6];
		entries[0] = "id";
		entries[1] = "x1";
		entries[2] = "y1";
		entries[3] = "x2";
		entries[4] = "y2";
		entries[5] = "density";
		writer.writeNext(entries);

		for (Integer flow : flow_density.keySet()) {
			try {
				double density = flow_density.get(flow);
				String[] nextData = new String[6];
				nextData[0] = String.valueOf(flow);
				nextData[1] = String.valueOf(flow_data.get(flow)[0]);
				nextData[2] = String.valueOf(flow_data.get(flow)[1]);
				nextData[3] = String.valueOf(flow_data.get(flow)[2]);
				nextData[4] = String.valueOf(flow_data.get(flow)[3]);
				nextData[5] = String.valueOf(density);
				writer.writeNext(nextData);
			} catch (Exception e) {

			}

		}

		writer.close();

	}

	static void loadData(String filename) throws Exception {

		flow_data = new HashMap<Integer, double[]>();
		kd = new KDTree<Integer>(4);

		CSVReader reader = new CSVReader(new FileReader(filename));
		reader.readNext();
		String[] nextLine;

		int id;
		double x1, y1, x2, y2, density;
		Envelope itemEnv;

		while ((nextLine = reader.readNext()) != null) {

			id = Integer.valueOf(nextLine[2]);
			x1 = Double.valueOf(nextLine[3]);
			y1 = Double.valueOf(nextLine[4]);
			x2 = Double.valueOf(nextLine[5]);
			y2 = Double.valueOf(nextLine[6]);

			double[] point = { x1, y1, x2, y2 };
			flow_data.put(id, point);
			try {
				kd.insert(point, id);
			} catch (KeyDuplicateException e) {
				
				//not handle duplicate flow data in this version, there is limited number of duplicate flows
			}
		}
	}

}
