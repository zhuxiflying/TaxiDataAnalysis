package flowmap.density;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import de.biomedical_imaging.edu.wlu.cs.levy.CG.KDTree;
import de.biomedical_imaging.edu.wlu.cs.levy.CG.KeyDuplicateException;
import de.biomedical_imaging.edu.wlu.cs.levy.CG.KeyMissingException;
import de.biomedical_imaging.edu.wlu.cs.levy.CG.KeySizeException;


//calculate the distance of the nearest flow, which density value greater than itself.
public class FlowSelection2 {

	static KDTree<Integer> kd;
	static HashMap<Integer, double[]> flow_data;

	public static void main(String[] args) throws Exception {

		String filename = "D:\\Projects\\MultiscaleFlowMap\\Data2\\subset_109_smoothed.csv";
		String outfilename = "D:\\Projects\\MultiscaleFlowMap\\Data2\\subset_109_selected.csv";

		loadDataWithDensity(filename);

		calculateGeneralizationThreshold();

		writeFile(outfilename);

	}

	// flow density selection based on KD tree index
	static void calculateGeneralizationThreshold() throws KeySizeException, KeyMissingException {

		double h = 100;

		double x1, y1, x2, y2, density, radius;


		System.out.println("Flow data generalization...");
		boolean b = false;

		
		while (b == false) {
	
			for (Integer flowid : flow_data.keySet()) {
				double[] flow_item = flow_data.get(flowid);
				radius = flow_item[5];
				if (Double.isNaN(radius)) {
					x1 = flow_data.get(flowid)[0];
					y1 = flow_data.get(flowid)[1];
					x2 = flow_data.get(flowid)[2];
					y2 = flow_data.get(flowid)[3];
					density = flow_data.get(flowid)[4];

					double[] flow1 = { x1, y1, x2, y2 };
					double[] point_low = { x1 - h, y1 - h, x2 - h, y2 - h };
					double[] point_up = { x1 + h, y1 + h, x2 + h, y2 + h };
					List<Integer> nearflow = null;
					try {
						nearflow = kd.range(point_low, point_up);
					} catch (KeySizeException e) {
						// TODO Auto-generated catch block
						System.err.println(
								"KeySizeException is thrown when a KDTree method is invoked on a key whose size (array length) mismatches the one used in the that KDTree's constructor.");
					}

					double minDist = Double.MAX_VALUE;

					for (int j = 0; j < nearflow.size(); j++) {
						int id = (int) nearflow.get(j);
						double[] flow = flow_data.get(id);
						double flow_value = flow[4];
						if (flow_value > density) {
							double[] flow2 = { flow[0], flow[1], flow[2], flow[3] };
							double dis = distance(flow1, flow2);
							if (dis < minDist)
								minDist = dis;
						}
					}
					if(minDist<h)
					{
					flow_item[5] = minDist;
					flow_data.put(flowid, flow_item);
					kd.delete(flow1);
					}
				}
			}
			System.out.println(h+","+kd.size());
			
			if(kd.size()==1)
			{
				b = true;
			}
			else
			{
				//increase the search radius
				h = h * 2;
			}
		}
	}

	// load the flow data with density value into hash map and kd index tree
	// IO exception reported but not handled in this method
	// KeyDuplicateException for kd tree reported but not handled
	static void loadDataWithDensity(String filename) throws KeySizeException, IOException {

		flow_data = new HashMap<Integer, double[]>();
		kd = new KDTree<Integer>(4);

		int id = 0;
		double x1, y1, x2, y2, density;
		String[] nextLine;

		System.out.println("Data loading...");

		CSVReader reader = new CSVReader(new FileReader(filename));
		reader.readNext();

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
				double[] flow = new double[6];
				flow[0] = x1;
				flow[1] = y1;
				flow[2] = x2;
				flow[3] = y2;
				flow[4] = density;
				flow[5] = Double.NaN;
				flow_data.put(id, flow);
			} catch (KeyDuplicateException e) {
				// not handle duplicate flow data in this version, there is limited number of
				// duplicate flows
				System.err.println(
						"KeyDuplicateException is thrown when the KDTree.insert method is invoked on a key already in the KDTree.");
			}
		}

		System.out.println(kd.size() + " flow data items loaded.");
	}

	// write selected flow data into file
	// IO exception reported but not handled in this method
	static void writeFile(String outputfile) {

		double[][] selected = new double[flow_data.keySet().size()][7];
		int index = 0;
		Iterator<Integer> it = flow_data.keySet().iterator();
		while (it.hasNext()) {
			int id = it.next();
			selected[index][0] = id;
			selected[index][1] = flow_data.get(id)[0];
			selected[index][2] = flow_data.get(id)[1];
			selected[index][3] = flow_data.get(id)[2];
			selected[index][4] = flow_data.get(id)[3];
			selected[index][5] = flow_data.get(id)[4];
			selected[index][6] = flow_data.get(id)[5];
			index++;
		}

		// sort the flow data according to density value by descending order;
		java.util.Arrays.sort(selected, new java.util.Comparator<double[]>() {
			public int compare(double[] a, double[] b) {
				return Double.compare(b[5], a[5]);
			}
		});

		System.out.println("Save generalization result into file...");
		CSVWriter writer;
		try {
			writer = new CSVWriter(new FileWriter(outputfile));

			// the header of the output file
			String[] entries = new String[7];
			entries[0] = "id";
			entries[1] = "x1";
			entries[2] = "y1";
			entries[3] = "x2";
			entries[4] = "y2";
			entries[5] = "density";
			entries[6] = "select_radius";
			writer.writeNext(entries);

			for (int i = 0; i < selected.length; i++) {
				String[] nextData = new String[7];
				nextData[0] = String.valueOf(selected[i][0]);
				nextData[1] = String.valueOf(selected[i][1]);
				nextData[2] = String.valueOf(selected[i][2]);
				nextData[3] = String.valueOf(selected[i][3]);
				nextData[4] = String.valueOf(selected[i][4]);
				nextData[5] = String.valueOf(selected[i][5]);
				nextData[6] = String.valueOf(selected[i][6]);
				writer.writeNext(nextData);
			}
			writer.close();

		} catch (IOException e) {
			// report IOException in the console
			e.printStackTrace();
		}
	}

	// calculate the Euclidean distance between two flows
	private static double distance(double[] flow1, double[] flow2) {
		return Math.sqrt((flow1[0] - flow2[0]) * (flow1[0] - flow2[0]) + (flow1[1] - flow2[1]) * (flow1[1] - flow2[1])
				+ (flow1[2] - flow2[2]) * (flow1[2] - flow2[2]) + (flow1[3] - flow2[3]) * (flow1[3] - flow2[3]));
	}

}
