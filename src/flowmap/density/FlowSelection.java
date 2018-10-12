package flowmap.density;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import de.biomedical_imaging.edu.wlu.cs.levy.CG.KDTree;
import de.biomedical_imaging.edu.wlu.cs.levy.CG.KeyDuplicateException;
import de.biomedical_imaging.edu.wlu.cs.levy.CG.KeySizeException;

public class FlowSelection {

	static KDTree<Integer> kd;
	static HashMap<Integer, double[]> flow_data;
	static HashSet<Integer> selected_flow;

	public static void main(String[] args) throws Exception {

		int h = 400;
		String filename = "D:\\Projects\\MultiscaleFlowMap\\Data2\\subset_139_smoothed.csv";
		String outfilename = "D:\\Projects\\MultiscaleFlowMap\\Data2\\subset_139_selected3.csv";

		loadDataWithDensity(filename);

		flowSelection(h);

		writeFile(outfilename);

	}

	// flow density selection based on KD tree index
	static void flowSelection(double h) {

		double x1, y1, x2, y2, density;

		selected_flow = new HashSet<Integer>();

		// a flag to record visited dataitems to improve calculation speed
		HashSet<Integer> visited = new HashSet<Integer>();

		
		//a sorted array to record flow 
		double[][] flowlist = new double[flow_data.keySet().size()][2];
		int index = 0;
		for (Integer flowid : flow_data.keySet()) {
			
			flowlist[index][0] = flowid;
			flowlist[index][1] = flow_data.get(flowid)[4];
			index++;
		}
		
		// sort the flowlsit according to density value by descending order;
		java.util.Arrays.sort(flowlist, new java.util.Comparator<double[]>() {
			public int compare(double[] a, double[] b) {
				return Double.compare(b[1], a[1]);
			}
		});

		System.out.println("Flow data generalization...");

		for (int i=0;i<flowlist.length;i++) {
			int flowid = (int) flowlist[i][0];
			if (!visited.contains(flowid)) {
				x1 = flow_data.get(flowid)[0];
				y1 = flow_data.get(flowid)[1];
				x2 = flow_data.get(flowid)[2];
				y2 = flow_data.get(flowid)[3];
				density = flow_data.get(flowid)[4];

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

				boolean b = true;

				for (int j = 0; j < nearflow.size(); j++) {
					int id = (int) nearflow.get(j);
					double[] flow = flow_data.get(id);
					double flow_value = flow[4];
					if (flow_value > density) {
						b = false;
						break;
					}
					else
					{
						visited.add(id);
					}
				}
				if (b) {
					selected_flow.add(flowid);
				}
				

			}
		}
		System.out.println(selected_flow.size() + " flow selected");
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
				double[] flow = { x1, y1, x2, y2, density };
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

		double[][] selected = new double[selected_flow.size()][6];
		int index = 0;
		Iterator<Integer> it = selected_flow.iterator();
		while (it.hasNext()) {
			int id = it.next();
			selected[index][0] = id;
			selected[index][1] = flow_data.get(id)[0];
			selected[index][2] = flow_data.get(id)[1];
			selected[index][3] = flow_data.get(id)[2];
			selected[index][4] = flow_data.get(id)[3];
			selected[index][5] = flow_data.get(id)[4];
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
			String[] entries = new String[6];
			entries[0] = "id";
			entries[1] = "x1";
			entries[2] = "y1";
			entries[3] = "x2";
			entries[4] = "y2";
			entries[5] = "density";
			writer.writeNext(entries);

			for (int i = 0; i < selected.length; i++) {
				String[] nextData = new String[6];
				nextData[0] = String.valueOf(selected[i][0]);
				nextData[1] = String.valueOf(selected[i][1]);
				nextData[2] = String.valueOf(selected[i][2]);
				nextData[3] = String.valueOf(selected[i][3]);
				nextData[4] = String.valueOf(selected[i][4]);
				nextData[5] = String.valueOf(selected[i][5]);
				writer.writeNext(nextData);
			}
			writer.close();

		} catch (IOException e) {
			// report IOException in the console
			e.printStackTrace();
		}
	}

}
