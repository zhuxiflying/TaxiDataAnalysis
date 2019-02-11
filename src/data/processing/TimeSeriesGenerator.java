package data.processing;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

/**
 * The TimeSeriesGenerator class aggregate taxi trip data into location
 * specified time series. The New York Taxi Trip data (cab trips from June 2016
 * to June 2018) use pickup unit id instead of coordinates to demonstrate the
 * location.
 */
public class TimeSeriesGenerator {

	private static DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
	private static long origin_time;
	private static HashMap<Integer, HashMap<Integer, Integer>> inflow_TimeSeries;
	private static String ts_folder = "F:\\data\\NYC_taxi\\TimeSeries\\";

	public static void main(String[] args) throws Exception {

		// the start time of the dataset.
		Calendar origin_date = Calendar.getInstance();
		origin_date.setTime(formatter.parse("2009-01-01 00:00"));
		origin_time = origin_date.getTimeInMillis();

		inflow_TimeSeries = new HashMap<Integer, HashMap<Integer, Integer>>();

		// the time duration of data files use
		String[][] years_months = { { "2016", "07" }, { "2016", "08" }, { "2016", "09" }, { "2016", "10" },
				{ "2016", "11" }, { "2016", "12" }, { "2017", "01" }, { "2017", "02" }, { "2017", "03" },
				{ "2017", "04" }, { "2017", "05" }, { "2017", "06" }, { "2017", "07" }, { "2017", "08" },
				{ "2017", "09" }, { "2017", "10" }, { "2017", "11" }, { "2017", "12" }, { "2018", "01" },
				{ "2018", "02" }, { "2018", "03" }, { "2018", "04" }, { "2018", "05" }, { "2018", "06" } };

		for (int i = 0; i < years_months.length; i++) {

			String year = years_months[i][0];
			String month = years_months[i][1];
			String filePath = "F:\\data\\NY_Taxi_tripdata\\yellow_tripdata_" + year + "-" + month + ".csv";
			System.out.println(filePath);
			loadData(filePath);
		}

		for (Integer id : inflow_TimeSeries.keySet()) {
			writeTimeSeries(id);
		}

	}

	private static void loadData(String filePath) throws Exception {

		Reader fileReader = Files.newBufferedReader(Paths.get(filePath));
		CSVReader reader = new CSVReader(fileReader);
		reader.readNext();
		String[] nextLine;
//		Calendar cal_departure = Calendar.getInstance();
		Calendar cal_arrival = Calendar.getInstance();

		while ((nextLine = reader.readNext()) != null) {
			try {
				// read pickup date, drop off date and convert it to time index

//				cal_departure.setTime(formatter.parse(nextLine[0]));
				cal_arrival.setTime(formatter.parse(nextLine[1]));
//				int departure_timeIndex = (int) ((cal_departure.getTimeInMillis() - origin_time) / 3600000);
				int arrival_timeIndex = (int) ((cal_arrival.getTimeInMillis() - origin_time) / 3600000);

				// read pickup location and drop off location
//				int pickup = Integer.valueOf(nextLine[7]);
				int dropoff = Integer.valueOf(nextLine[8]);
				HashMap<Integer, Integer> timeseries = null;

				
				// aggregate taxi trip data by units into time series
				if (inflow_TimeSeries.get(dropoff) != null) {
					timeseries = inflow_TimeSeries.get(dropoff);
					if (timeseries.get(arrival_timeIndex) != null) {
						int inflow = timeseries.get(arrival_timeIndex);
						inflow++;
						timeseries.put(arrival_timeIndex, inflow);
					} else {
						timeseries.put(arrival_timeIndex, 1);
					}
					inflow_TimeSeries.put(dropoff, timeseries);
				} else {
					timeseries = new HashMap<Integer, Integer>();
					timeseries.put(arrival_timeIndex, 1);
					inflow_TimeSeries.put(dropoff, timeseries);
				}

			} catch (Exception e) {

			}
		}

	}

	private static void writeTimeSeries(int id) throws Exception {
		String url = ts_folder + id + ".csv";
		Writer fileWriter = Files.newBufferedWriter(Paths.get(url));
		CSVWriter writer = new CSVWriter(fileWriter);
		// feed in your array (or convert your data to an array)

		HashMap<Integer, Integer> ts = inflow_TimeSeries.get(id);
		int start_index = 65711;
		int end_index = 83230;

		for (int index = start_index; index <= end_index; index++) {
			String[] dataitem = new String[2];
			dataitem[0] = String.valueOf(index);
			dataitem[1] = String.valueOf(ts.get(index) == null ? 0 : ts.get(index));
			writer.writeNext(dataitem);
		}
		writer.close();
	}

}
