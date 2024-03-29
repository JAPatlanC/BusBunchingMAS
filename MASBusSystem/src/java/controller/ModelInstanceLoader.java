package controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import model.Bus;
import model.ModelInstance;
import model.Stop;

// TODO: Auto-generated Javadoc
/**
 * Description: Loads ModelInstance from txt files
 * 
 */
public class ModelInstanceLoader {

	/**
	 * Load bus holding models.
	 *
	 * @param folderName the folder name
	 * @return the list of model instance
	 */
	public static List<ModelInstance> loadBusHoldingModels(String folderName) {
		List<ModelInstance> listModelInstances = new ArrayList<ModelInstance>();
		// This will reference one line at a time
		String data = null;
		int busCapacity = 0;
		File folder = new File(folderName);
		File[] listOfFiles = folder.listFiles();
		int fils = 0;
		int conta = 0;
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		Date today = new Date();
		for (File file : listOfFiles) {
			ModelInstance mi = new ModelInstance();
			String fileName = file.getName();
			try {
				// FileReader reads text files in the default encoding.
				FileReader fileReader = new FileReader(file);

				// Always wrap FileReader in BufferedReader.
				BufferedReader bufferedReader = new BufferedReader(fileReader);
				int numLinea = 1;
				while ((data = bufferedReader.readLine()) != null) {
					if (data.contains("#")) {
						// System.out.println(getFileValue(data));
						switch (numLinea) {
						// Instance Data
						// Snapshot Time
						case 1:
							mi.t0 = Float.parseFloat(getFileValue(data));
							break;
						// Number of stops
						case 2:
							String numberStops = getFileValue(data);
							for (int i = 0; i < Integer.parseInt(numberStops); i++) {
								mi.listStops.add(new Stop(i + 1));
							}
							mi.lastStopId = mi.listStops.size();

							break;
						// Number of buses
						case 3:
							String numberBuses = getFileValue(data);
							for (int i = 0; i < Integer.parseInt(numberBuses); i++) {
								Bus b = new Bus(i + 1);
								mi.listBuses.add(b);
							}
							break;
						// Bus capacity
						case 4:
							busCapacity = Integer.parseInt(getFileValue(data));
							break;
						// Max holding time
						case 5:
							mi.maxHold = Integer.parseInt(getFileValue(data));
							break;
						// Abording rate
						case 6:
							mi.dwellPer = Float.parseFloat(getFileValue(data));
							break;
						// Descending rate
						case 7:
							mi.alightPer = Float.parseFloat(getFileValue(data));
							break;
						// Release time
						case 8:
							mi.releaseTime = Float.parseFloat(getFileValue(data));
							break;
						// Instance end time
						case 9:
							mi.tn = Float.parseFloat(getFileValue(data));
							break;
						// Bus Holding Calls
						case 10:
							mi.busHoldingCalls = Integer.parseInt(getFileValue(data));
							mi.busHoldingPeriod = (int) Math.round(mi.tn * 0.9 / (mi.busHoldingCalls + 1));
							break;
						// Buses overtake
						case 11:
							mi.busesOvertake = getFileValue(data).equals("true") ? true : false;
							break;
						// Circular route
						case 12:
							mi.circularRoute = getFileValue(data).equals("true") ? true : false;
							break;
						// Bus holding method
						case 13:
							mi.busHoldingMethod = getFileValue(data);
							break;
						// Headway tolerance range
						case 14:
							mi.headwayToleranceRange = Float.parseFloat(getFileValue(data));
							break;
						// Arrive rate each stop
						case 15:
							data = bufferedReader.readLine();
							int i = 0;
							for (String metadata : data.split("\\|")) {
								mi.listStops.get(i).arriveRate = Float.parseFloat(metadata);
								i++;
							}
							break;
						// Descend rate each stop
						case 16:
							data = bufferedReader.readLine();
							i = 0;
							for (String metadata : data.split("\\|")) {
								mi.listStops.get(i).descendRate = Float.parseFloat(metadata);
								i++;
							}
							break;
						// Distances between stops
						case 17:
							data = bufferedReader.readLine();
							i = 0;
							for (String metadata : data.split("\\|")) {
								mi.turn += Float.parseFloat(metadata);
								mi.listStops.get(i).distNextStop = Float.parseFloat(metadata);
								mi.listStops.get(i).distDepot = mi.turn;
								if (i != 0)
									// mi.listStops.get(i).distPreviousStop = mi.listStops.get(i - 1).distNextStop;
									if (i == mi.listStops.size() - 1) {
										// mi.listStops.get(0).distPreviousStop = mi.listStops.get(i).distNextStop;
									}
								i++;
							}
							break;
						// People waiting at each stop
						case 18:
							data = bufferedReader.readLine();
							i = 0;
							for (String metadata : data.split("\\|")) {
								mi.listStops.get(i).passengersLeft = Integer.parseInt(metadata);
								i++;
							}
							break;
						// Speed limit between stops
						case 19:
							data = bufferedReader.readLine();
							i = 0;
							for (String metadata : data.split("\\|")) {
								mi.listStops.get(i).speedLimit = Float.parseFloat(metadata);
								i++;
							}
							break;
						// Indexes of buses
						case 20:
							break;
						// Buses current positions
						case 21:
							data = bufferedReader.readLine();
							i = 0;
							for (String metadata : data.split("\\|")) {
								mi.listBuses.get(i).position = Float.parseFloat(metadata);
								i++;
							}
							break;
						// Buses current passengers
						case 22:
							data = bufferedReader.readLine();
							i = 0;
							for (String metadata : data.split("\\|")) {
								mi.listBuses.get(i).passengers = Integer.parseInt(metadata);
								i++;
							}
							break;
						// Last stop visited of each stop
						case 23:
							data = bufferedReader.readLine();
							i = 0;
							for (String metadata : data.split("\\|")) {
								mi.listBuses.get(i).capacity = busCapacity;
								if (Integer.parseInt(metadata) == 0)
									mi.listBuses.get(i).previousStop = mi.listStops.get(0);
								else {
									if (Integer.parseInt(metadata) - 2 > 0)
										mi.listBuses.get(i).previousStop = mi.listStops
												.get(Integer.parseInt(metadata) - 2);
									else
										mi.listBuses.get(i).previousStop = mi.listStops.get(mi.listStops.size() - 1);
								}
								i++;
							}
							break;
						}
						numLinea++;
					}
				}
				mi.id = mi.listStops.size() + "-" + mi.listBuses.size()+"-"+mi.headwayToleranceRange;
				// Closing file.
				bufferedReader.close();
				mi.h = new int[mi.listBuses.size()][mi.listStops.size()];
				listModelInstances.add(mi);
			} catch (FileNotFoundException ex) {
				System.out.println("Unable to open file '" + fileName + "'");
			} catch (IOException ex) {
				System.out.println("Error reading file '" + fileName + "'");
			}
		}
		return listModelInstances;
	}

	/**
	 * Gets the file value.
	 *
	 * @param data the data
	 * @return the file value
	 */
	public static String getFileValue(String data) {
		String value = data.substring(data.lastIndexOf("=") + 1).strip();
		return value;
	}
}
