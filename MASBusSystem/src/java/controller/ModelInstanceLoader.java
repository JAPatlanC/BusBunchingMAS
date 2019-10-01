package controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import model.Bus;
import model.ModelInstance;
import model.Stop;

/*
 * Classname: ModelInstanceLoader
 * Description: Loads ModelInstance from txt files 
 * Author: Jesús Angel Patlán Castillo
 * Changelog:
 * Date		 *********** Description
 * 28/08/2019			First Version
 * 07/09/2019			Updated the format of the instances
 * 09/09/2019			Bugs Fixed
 * 
 */
public class ModelInstanceLoader {

	/*
	 * Method: solveBusHolding Description: Solves a ModelInstance by using bus
	 * holding strategy Parameters: String - Folder which contains the models
	 * Return: List<ModelInstance> - Instances loaded from the folder
	 */
	public static List<ModelInstance> loadBusHoldingModels(String folderName) {
		List<ModelInstance> listModelInstances = new ArrayList<ModelInstance>();
		// This will reference one line at a time
		String data = null;
		int busCapacity = 0;
		File folder = new File(folderName);
		File[] listOfFiles = folder.listFiles();
		int fils = 0;
		int conta=0;
		for (File file : listOfFiles) {
			System.out.println("Leyendo instancia "+(conta++));
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
							switch (numLinea) {
							// Instance Data
							//Snapshot Time
							case 1:
								mi.t0 = Float.parseFloat(getFileValue(data));
								break;
							//Number of stops
							case 2:
								String numberStops = getFileValue(data);
								for (int i = 0; i < Integer.parseInt(numberStops); i++) {
									mi.listStops.add(new Stop(i + 1));
								}
								break;
							//Number of buses
							case 3:
								String numberBuses = getFileValue(data);
								for (int i = 0; i < Integer.parseInt(numberBuses); i++) {
									Bus b = new Bus(i + 1);
									mi.listBuses.add(b);
								}
								break;
							//Bus capacity
							case 4:
								busCapacity = Integer.parseInt(getFileValue(data));
								break;
							//Max holding time
							case 5:
								mi.maxHold = Integer.parseInt(getFileValue(data));
								break;
							//Abording rate
							case 6:
								mi.dwellPer = Float.parseFloat(getFileValue(data));
								break;
							//Descending rate
							case 7:
								mi.alightPer = Float.parseFloat(getFileValue(data));
								break;
							//Headways
							case 8:
								mi.headways = Float.parseFloat(getFileValue(data));
								break;
							//Arrive rate each stop
							case 9:
								data = bufferedReader.readLine();
								int i=0;
								for(String metadata : data.split("\\|")) {
									mi.listStops.get(i).arriveRate = Float.parseFloat(metadata);
									i++;
								}
								break;
							//Descend rate each stop
							case 10:
								data = bufferedReader.readLine();
								i=0;
								for(String metadata : data.split("\\|")) {
									mi.listStops.get(i).descendRate = Float.parseFloat(metadata);
									i++;
								}
								break;
							//Distances between stops
							case 11:
								data = bufferedReader.readLine();
								i=0;
								for(String metadata : data.split("\\|")) {
									mi.turn += Float.parseFloat(metadata);
									mi.listStops.get(i).distNextStop = Float.parseFloat(metadata);
									mi.listStops.get(i).distDepot = mi.turn;
									if(i!=0)
										mi.listStops.get(i).distPreviousStop = mi.listStops.get(i - 1).distNextStop;
									if(i==mi.listStops.size()-1) {
										mi.listStops.get(0).distPreviousStop = mi.listStops.get(i).distNextStop;
									}
									i++;
								}
								break;
							//People waiting at each stop
							case 12:
								data = bufferedReader.readLine();
								i=0;
								for(String metadata : data.split("\\|")) {
									mi.listStops.get(i).passengersLeft = Integer.parseInt(metadata);
									i++;
								}
								break;
							//Indexes of buses
							case 13:
								break;
							//Buses current positions
							case 14:
								data = bufferedReader.readLine();
								i=0;
								for(String metadata : data.split("\\|")) {
									mi.listBuses.get(i).position = Float.parseFloat(metadata);
									i++;
								}
								break;
							//Buses current passengers
							case 15:
								data = bufferedReader.readLine();
								i=0;
								for(String metadata : data.split("\\|")) {
									mi.listBuses.get(i).passengers = Integer.parseInt(metadata);
									i++;
								}
								break;
							//Last stop visited of each stop
							case 16:
								data = bufferedReader.readLine();
								i=0;
								for(String metadata : data.split("\\|")) {
									mi.listBuses.get(i).capacity = busCapacity;
									mi.listBuses.get(i).previousStop = mi.listStops.get(Integer.parseInt(metadata)-1);
									i++;
								}
								break;
							}
							numLinea++;
					}
				}

				// Closing file.
				bufferedReader.close();
				listModelInstances.add(mi);
			} catch (FileNotFoundException ex) {
				System.out.println("Unable to open file '" + fileName + "'");
			} catch (IOException ex) {
				System.out.println("Error reading file '" + fileName + "'");
			}
		}
		return listModelInstances;
	}

	public static String getFileValue(String data) {
		String value = data.substring(data.lastIndexOf("=") + 1).strip();
		return value;
	}
}
