package data.processing;

import java.io.FileReader;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;


//partition the data file into hour duration.
public class TemporalIndexing {

	public static void main(String[] args) throws Exception {

		String[] years = { "2011", "2012", "2013", "2014", "2015" };
		String[] months = { "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12" };

		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

		// the start time of the dataset.
		Calendar origin_date = Calendar.getInstance();
		origin_date.setTime(formatter.parse("2009-01-01 00:00"));
		long origin_time = origin_date.getTimeInMillis();

		Calendar cal_departure = Calendar.getInstance();
		Calendar cal_arrival = Calendar.getInstance();

		for (int y = 0; y < years.length; y++) {
			for (int m = 0; m < months.length; m++) {

				HashMap<Integer, ArrayList<String[]>> data_segement = new HashMap<Integer, ArrayList<String[]>>();
				String year = years[y];
				String month = months[m];
				String filename = "D:\\Data\\NY_Taxi_tripdata\\yellow_tripdata_" + year + "-" + month + ".csv";
				System.out.println(filename);

				CSVReader reader = new CSVReader(new FileReader(filename));
				reader.readNext();
				String[] nextLine;

				while ((nextLine = reader.readNext()) != null) {

					cal_departure.setTime(formatter.parse(nextLine[0]));
					cal_arrival.setTime(formatter.parse(nextLine[1]));
					int duration = (int) ((cal_arrival.getTimeInMillis() - cal_departure.getTimeInMillis()) / 60000);
					int departure = (int) ((cal_departure.getTimeInMillis() - origin_time) / 3600000);

					if (data_segement.get(departure) != null) {
						ArrayList<String[]> data = data_segement.get(departure);
						String[] data_item = new String[6];
						data_item[0] = String.valueOf(duration);
						data_item[1] = nextLine[2];
						data_item[2] = nextLine[3];
						data_item[3] = nextLine[4];
						data_item[4] = nextLine[5];
						data_item[5] = nextLine[6];
						data.add(data_item);
						data_segement.put(departure, data);
					} else {
						ArrayList<String[]> data = new ArrayList<String[]>();
						String[] data_item = new String[6];
						data_item[0] = String.valueOf(duration);
						data_item[1] = nextLine[2];
						data_item[2] = nextLine[3];
						data_item[3] = nextLine[4];
						data_item[4] = nextLine[5];
						data_item[5] = nextLine[6];
						data.add(data_item);
						data_segement.put(departure, data);
					}

				}

				for (Integer time : data_segement.keySet()) {
					String outputfile = "D:\\Data\\NY_Taxi_tripdata2\\" + time + ".csv";
					CSVWriter writer = new CSVWriter(new FileWriter(outputfile));
					// feed in your array (or convert your data to an array)
					String[] entries = new String[6];

					entries[0] = "Trip_Duration";
					entries[1] = "Trip_Distance";
					entries[2] = "UTM_x1";
					entries[3] = "UTM_y1";
					entries[4] = "UTM_x2";
					entries[5] = "UTM_y2";

					writer.writeNext(entries);
					ArrayList<String[]> data = data_segement.get(time);

					for (int j = 0; j < data.size(); j++) {

						try {
							String[] dataitem = new String[6];
							dataitem[0] = data.get(j)[0];
							dataitem[1] = data.get(j)[1];
							dataitem[2] = data.get(j)[2];
							dataitem[3] = data.get(j)[3];
							dataitem[4] = data.get(j)[4];
							dataitem[5] = data.get(j)[5];
							writer.writeNext(dataitem);
						} catch (Exception e) {

						}
					}
					writer.close();
				}

			}
		}

	}

}
