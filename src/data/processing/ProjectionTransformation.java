package data.processing;

import java.io.FileReader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.osgeo.proj4j.CRSFactory;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.osgeo.proj4j.CoordinateTransform;
import org.osgeo.proj4j.CoordinateTransformFactory;
import org.osgeo.proj4j.ProjCoordinate;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

/**
 * The ProjectionTransformation class implements an coordinates projection by
 * Proj4j. Specifically, the example below demonstrate the projection from WGS84
 * to UTM18N. The New York Taxi Trip data (yellow cab trips from 2009 to 2015)
 * adopted in this example.
 */
public class ProjectionTransformation {

	public static void main(String[] args) throws Exception {

		CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
		CRSFactory csFactory = new CRSFactory();

		// create input WGS84
		final String WGS84_PARAM = "+title=long/lat:WGS84 +proj=longlat +ellps=WGS84 +datum=WGS84 +units=degrees";
		CoordinateReferenceSystem WGS84 = csFactory.createFromParameters("WGS84", WGS84_PARAM);

		// create output UTM18N
		CoordinateReferenceSystem UTM18N = csFactory.createFromName("EPSG:32618");

		// create transformer
		CoordinateTransform trans = ctFactory.createTransform(WGS84, UTM18N);

		ProjCoordinate p1 = new ProjCoordinate();
		ProjCoordinate p2 = new ProjCoordinate();

		/*
		 * Transform point p1 to p2;
		 * 
		 * double[] coordinates = {-73.995917,40.728576}; p1.x = coordinates[0]; p1.y =
		 * coordinates[1]; trans.transform(p1, p2); System.out.println(p2.x);
		 * System.out.println(p2.y;);
		 */

		// traversal the file list.
		String[] years = { "2009", "2010", "2011", "2012", "2013", "2014", "2015" };
		String[] months = { "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12" };
		
		//Data Source Link: can be local file or cloud storage
		String folder = "";

		for (int y = 0; y < years.length; y++) {
			for (int m = 0; m < months.length; m++) {
				String year = years[y];
				String month = months[m];
				String filename = folder + "yellow_tripdata_" + year + "-" + month + ".csv";
				String filename2 = folder + "yellow_tripdata_" + year + "-" + month + ".csv";
				CSVReader reader = new CSVReader(new FileReader(filename));
				reader.readNext();
				String[] nextLine;

				 Writer fileWriter = Files.newBufferedWriter(Paths.get(filename2));
				CSVWriter writer = new CSVWriter(fileWriter);
				// feed in your array (or convert your data to an array)
				String[] entries = new String[7];

				entries[0] = "Trip_Pickup_DateTime";
				entries[1] = "Trip_Dropoff_DateTime";
				entries[2] = "Trip_Distance";
				entries[3] = "UTM_x1";
				entries[4] = "UTM_y1";
				entries[5] = "UTM_x2";
				entries[6] = "UTM_y2";

				writer.writeNext(entries);

				while ((nextLine = reader.readNext()) != null) {
					try {
						String[] dataitem = new String[7];

						// read pickup date, drop off date, trip distance ,and write into new file.
						dataitem[0] = nextLine[1];
						dataitem[1] = nextLine[2];
						dataitem[2] = nextLine[4];

						// read WGS coordinates from the file.
						double oX = Double.valueOf(nextLine[5]);
						double oY = Double.valueOf(nextLine[6]);
						double dX = Double.valueOf(nextLine[9]);
						double dY = Double.valueOf(nextLine[10]);

						// project transformation
						p1.x = oX;
						p1.y = oY;
						trans.transform(p1, p2);
						dataitem[3] = String.valueOf(p2.x);
						dataitem[4] = String.valueOf(p2.y);

						p1.x = dX;
						p1.y = dY;
						trans.transform(p1, p2);

						dataitem[5] = String.valueOf(p2.x);
						dataitem[6] = String.valueOf(p2.y);
						writer.writeNext(dataitem);
					} catch (Exception e) {

					}
				}
				writer.close();
			}

		}
	}

}
