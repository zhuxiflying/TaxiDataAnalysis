package data.processing;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import com.opencsv.CSVReader;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.strtree.STRtree;

/*
 * select points with local maximum density, these local maximum points are used to generate thiessen polygons
 */
public class LocalMaximaSelection {

	static STRtree tree;
	static HashMap<Integer, double[]> points;
	static HashMap<Integer, Integer> pointID_Density;

	public static void main(String[] args) throws IOException {

		// load the grid centroids
		loadGridPoints();
		loadPointDensity();

		double searchRadius = 30;
		for (Integer id : pointID_Density.keySet()) {
			if (isLocalMaximum(id, searchRadius)) {
				double[] coordinates = points.get(id);
				int density = pointID_Density.get(id) == null ? 0 : pointID_Density.get(id);
				if (density >= 100)
					System.out.println(id + "," + coordinates[0] + "," + coordinates[1] + "," + density);
			}
		}

	}

	private static void loadPointDensity() throws IOException {
		String filename = "D:\\Data\\Inflow_Density.csv";
		CSVReader reader = new CSVReader(new FileReader(filename));
		reader.readNext();
		String[] nextLine;

		int id;
		int density;

		pointID_Density = new HashMap<Integer, Integer>();
		while ((nextLine = reader.readNext()) != null) {

			id = Integer.valueOf(nextLine[0]);
			density = Integer.valueOf(nextLine[1]);
			pointID_Density.put(id, density);
		}
		reader.close();
	}

	private static void loadGridPoints() throws IOException {
		// TODO Auto-generated method stub
		String filename = "D:\\Data\\Grids\\GridPoints.csv";
		CSVReader reader = new CSVReader(new FileReader(filename));
		reader.readNext();
		String[] nextLine;

		int id;
		double x, y;
		tree = new STRtree();
		points = new HashMap<Integer, double[]>();
		Envelope itemEnv;
		;
		while ((nextLine = reader.readNext()) != null) {

			id = Integer.valueOf(nextLine[0]);
			x = Double.valueOf(nextLine[1]);
			y = Double.valueOf(nextLine[2]);
			double[] point = { x, y };
			points.put(id, point);
			itemEnv = new Envelope(x, x, y, y);
			tree.insert(itemEnv, id);
		}
		reader.close();
	}

	private static boolean isLocalMaximum(Integer pid, double searchRadius) {
		double[] coordinates = points.get(pid);
		int density = pointID_Density.get(pid) == null ? 0 : pointID_Density.get(pid);
		Envelope itemEnv;
		;
		List<Integer> result;
		itemEnv = new Envelope(coordinates[0] - searchRadius, coordinates[0] + searchRadius,
				coordinates[1] - searchRadius, coordinates[1] + searchRadius);
		result = tree.query(itemEnv);
		for (Integer nid : result) {
			double[] coordinates2 = points.get(nid);
			double dist = calculateDistanceBetweenPoints(coordinates, coordinates2);
			if (dist <= searchRadius) {
				double density_near = pointID_Density.get(nid) == null ? 0 : pointID_Density.get(nid);
				if (density_near > density)
					return false;
			}
		}
		return true;
	}

	private static double calculateDistanceBetweenPoints(double[] point1, double[] point2) {
		return Math.sqrt(
				(point2[1] - point1[1]) * (point2[1] - point1[1]) + (point2[0] - point1[0]) * (point2[0] - point1[0]));
	}

}
