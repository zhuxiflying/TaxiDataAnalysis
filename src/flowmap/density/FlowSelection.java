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

		String filename = "D:\\Projects\\MultiscaleFlowMap\\Data2\\subset_139_smoothed.csv";
		CSVReader reader = new CSVReader(new FileReader(filename));
		String[] nextLine;
		reader.readNext();

		int idd;
		double x1, y1, x2, y2, density;
		System.out.print("var flows = [];");
		int index =0;
	
		while ((nextLine = reader.readNext()) != null) {
			
			if(index%10000==0)System.out.println(index);
            index++;
			idd = Integer.valueOf(nextLine[0]);
			x1 = Double.valueOf(nextLine[1]);
			y1 = Double.valueOf(nextLine[2]);
			x2 = Double.valueOf(nextLine[3]);
			y2 = Double.valueOf(nextLine[4]);
			density = Double.valueOf(nextLine[5]);
			if (density > 100) {
				double[] point = { x1, y1, x2, y2 };
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
					flow[0] = (x1-581800)/10;
					flow[1] = (4525800-y1)/10;
					flow[2] = (x2-581800)/10;
					flow[3] = (4525800-y2)/10;
					if(flow[0]>0&&flow[0]<1000&&flow[1]>0&&flow[1]<2000&&flow[2]>0&&flow[2]<1000&&flow[3]>0&&flow[3]<2000)
					{

					flow[4] = density;
					selected_flow.add(flow);
					}
				}
			}
		}
		
		
		
		double[][] array= new double[selected_flow.size()][5];
		
		for(int i=0;i<selected_flow.size();i++)
		{
			array[i][0] = selected_flow.get(i)[0];
			array[i][1] = selected_flow.get(i)[1];
			array[i][2] = selected_flow.get(i)[2];
			array[i][3] = selected_flow.get(i)[3];
			array[i][4] = selected_flow.get(i)[4];
		}
				java.util.Arrays.sort(array, new java.util.Comparator<double[]>() {
				    public int compare(double[] a, double[] b) {
				        return Double.compare(a[4], b[4]);
				    }
				});
		
		for(int i=0;i<array.length;i++)
		{
		System.out.print("flows["+i+"]=[");
		System.out.print(array[i][0]+","+array[i][1]+","+array[i][2]+","+array[i][3]+","+array[i][4]);
		System.out.print("];");
		System.out.println();
//		System.out.println(array[i][4]);
		}
		
//		System.out.println(selected_flow.size());
//		double minX = Double.MAX_VALUE;
//		double maxX = -Double.MAX_VALUE;
//		double miny = Double.MAX_VALUE;
//		double maxy = -Double.MAX_VALUE;
//		
//		for(int i=0;i<selected_flow.size();i++)
//		{
//			double[] flow = selected_flow.get(i);
//			if(flow[0]<minX)minX=flow[0];
//			if(flow[2]<minX)minX=flow[2];
//			if(flow[0]>maxX)maxX=flow[0];
//			if(flow[2]>maxX)maxX=flow[2];
//			if(flow[1]<miny)miny=flow[1];
//			if(flow[3]<miny)miny=flow[3];
//			if(flow[1]>maxy)maxy=flow[1];
//			if(flow[3]>maxy)maxy=flow[3];
//		}
//		

		

	}

	static void loadData() throws Exception {
		flow_data = new HashMap<Integer, double[]>();

		String filename = "D:\\Projects\\MultiscaleFlowMap\\Data2\\subset_139_smoothed.csv";
		kd = new KDTree(4);
		CSVReader reader = new CSVReader(new FileReader(filename));
		reader.readNext();
		String[] nextLine;

		int id;
		double x1, y1, x2, y2, density;
		Envelope itemEnv;
		while ((nextLine = reader.readNext()) != null) {

			id = Integer.valueOf(nextLine[0]);
			x1 = Double.valueOf(nextLine[1]);
			y1 = Double.valueOf(nextLine[2]);
			x2 = Double.valueOf(nextLine[3]);
			y2 = Double.valueOf(nextLine[4]);
			density = Double.valueOf(nextLine[5]);

			double[] point = { x1, y1, x2, y2 };
			try {
				if(density>100)
				{
				kd.insert(point, id);
				double[] flow = { x1, y1, x2, y2, density };
				flow_data.put(id, flow);
				}
			} catch (KeyDuplicateException e) {
//				double[] flow = flow_data.get(id);
//				flow[4]++;
//				flow_data.put(id, flow);
			}
		}
	}

}
