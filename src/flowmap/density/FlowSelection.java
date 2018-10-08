package flowmap.density;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.opencsv.CSVReader;
import com.vividsolutions.jts.geom.Envelope;

import de.biomedical_imaging.edu.wlu.cs.levy.CG.KDTree;
import de.biomedical_imaging.edu.wlu.cs.levy.CG.KeyDuplicateException;

public class FlowSelection {

	static KDTree kd;
	static HashMap<Integer, double[]> flow_data;

	public static void main(String[] args) throws Exception {

		loadData();

		ArrayList<double[]> selected_flow = new ArrayList<double[]>();

		int h = 500;

		String filename = "D:\\Projects\\MultiscaleFlowMap\\Data2\\subset_139_smoothed_sample.csv";
		CSVReader reader = new CSVReader(new FileReader(filename));
		String[] nextLine;
		reader.readNext();

		double x1, y1, x2, y2, density;

		while ((nextLine = reader.readNext()) != null) {

			x1 = Double.valueOf(nextLine[1]);
			y1 = Double.valueOf(nextLine[2]);
			x2 = Double.valueOf(nextLine[3]);
			y2 = Double.valueOf(nextLine[4]);
			density = Double.valueOf(nextLine[5]);
			double[] point_low = { x1 - h, y1 - h, x2 - h, y2 - h };
			double[] point_up = { x1 + h, y1 + h, x2 + h, y2 + h };
			List nearflow = kd.range(point_low, point_up);
			double max_density = -Double.MAX_VALUE;
			for (int i = 0; i < nearflow.size(); i++) {
				int id = (int) nearflow.get(i);
				double[] flow = flow_data.get(id);
				double flow_value = flow[4];
				if (flow_value > max_density)
					max_density = flow_value;
			}

			if (density == max_density) {
				double[] flow = new double[5];
				flow[0] = x1;
				flow[1] = y1;
				flow[2] = x2;
				flow[3] = y2;
				flow[4] = density;
				selected_flow.add(flow);
			}
		}

		for (int i = 0; i < selected_flow.size(); i++) {
			double[] flow = selected_flow.get(i);
			System.out.print(flow[0] + "," + flow[1] + "," + flow[2] + "," + flow[3] + "," + flow[4]);
			System.out.println();
		}
	}

	static void loadData() throws Exception {
		
		flow_data = new HashMap<Integer, double[]>();
		String filename = "D:\\Projects\\MultiscaleFlowMap\\Data2\\subset_139_smoothed_sample.csv";
		kd = new KDTree(4);
		CSVReader reader = new CSVReader(new FileReader(filename));
		reader.readNext();
		String[] nextLine;

		int id;
		double x1, y1, x2, y2, density;
		while ((nextLine = reader.readNext()) != null) {

			id = Integer.valueOf(nextLine[0]);
			x1 = Double.valueOf(nextLine[1]);
			y1 = Double.valueOf(nextLine[2]);
			x2 = Double.valueOf(nextLine[3]);
			y2 = Double.valueOf(nextLine[4]);
			density = Double.valueOf(nextLine[5]);

			double[] point = { x1, y1, x2, y2 };
			try {

				kd.insert(point, id);
				double[] flow = { x1, y1, x2, y2, density };
				flow_data.put(id, flow);

			} catch (KeyDuplicateException e) {
			}
		}
	}

}
