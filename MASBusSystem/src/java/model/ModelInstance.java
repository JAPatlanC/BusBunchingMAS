package model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * Description: Instance of the model, represented by a snapshot of the environment. 
 */
public class ModelInstance {

	/** The instance id. */
	public String id;
	
	/** The list buses. */
	public List<Bus> listBuses;
	
	/** The list stops. */
	public List<Stop> listStops;
	
	/** The maximum holding time. */
	public float maxHold;
	
	/** The time for a passenger to descend from the bus. */
	public float dwellPer;
	
	/** The time for a passenger to get inside the bus */
	public float alightPer; 
	
	/** The instance initial time. */
	public float t0;
	
	/** The Instance current time. */
	public float ti;
	
	/** The instance end time. */
	public float tn;
	
	/** The time for the doors to open/close. */
	public int doors;
	
	/** The Distances between buses. */
	public float headways;
	
	/** The Release time of the buses at first stop. */
	public float releaseTime;
	
	/** The Total route length. */
	public float turn;
	
	/** If every bus in the route is already on the System. */
	public boolean allFree;
	
	/** The Number of bus holding solver calls. */
	public int busHoldingCalls;
	
	/** The Period of every bus holding solver call. */
	public int busHoldingPeriod;
	
	/** The Bus holding method to use as a solver. */
	public String busHoldingMethod;
	
	/** The Holding times for every bus at every stop (filled when the model is solved by busholding). */
	public int[][] h;
						
	/** The mean distance. */
	public List<Float> meanDistance;
	
	/** The max headway. */
	public float maxHeadway;
	
	/** The min headway. */
	public float minHeadway;
	
	/** If buses can overtake or not in the instance. */
	public boolean busesOvertake;
	
	/** Allow the buses to keep circulating at the end of the route. */
	public boolean circularRoute;
	
	/** The last stop id. */
	public int lastStopId;

	/** The folder stops rate name. */
	private String folderStopsRateName = "C:\\Users\\ja_pa\\git\\BusBunchingMAS\\MASBusSystem\\StopsRate";

	/**
	 * Instantiates a new model instance.
	 */
	public ModelInstance() {
		listBuses = new ArrayList<Bus>();
		listStops = new ArrayList<Stop>();
		meanDistance = new ArrayList<Float>();
		allFree = false;
	}

	/**
	 * To string.
	 *
	 * @return the string
	 */
	@Override
	public String toString() {
		System.out.println("***ModelInstance BEGIN***");
		System.out.println("[Instance time=" + t0 + ", InstanceCurrentTime=" + ti + ", Instance end time=" + tn
				+ ", maxHold=" + maxHold + ", dwellPer=" + dwellPer + ", alightPer=" + alightPer + ", doors=" + doors
				+ ", releaseTime=" + releaseTime + ", turn=" + turn + ", busHoldingCalls=" + busHoldingCalls
				+ ", busHoldingPeriod=" + busHoldingPeriod + "]");
		System.out.println("---Stops (" + listStops.size() + ")---");
		for (Stop s : listStops) {
			//System.out.println(s);
		}
		List<Bus> activeBuses = getActiveBuses();
		System.out.println("---Buses (" + activeBuses.size() + ")---");
		for (Bus b : activeBuses) {
			System.out.println(b);
		}
		return "***ModelInstance END***";
	}
	

	/**
	 * Update headway metric.
	 */
	public void updateMetric() {
		float dist = 0;
		List<Float> distances = new ArrayList<Float>();
		for(Bus b : getActiveBuses()) {
			for(Bus b2 : getActiveBuses()) {
				if(b!=b2) {
					distances.add((float) Math.abs(b.position-b2.position));
				}
			}
			dist+=distances.stream().min(Comparator.naturalOrder()).get();
		}
		dist/=getActiveBuses().size();
		meanDistance.add(dist);
	}
	
	/**
	 * Update position txt.
	 */
	public void updatePositionTxt() {
		/*
		try (FileWriter writer = new FileWriter(id+".log");
				 BufferedWriter bw = new BufferedWriter(writer)) {
				bw.write(positions);

			} catch (IOException e) {
				System.err.format("IOException: %s%n", e);
			}
			*/
		String positions = "";
		for(Bus b : listBuses) {
			positions += b.position +",";
		}
		positions = removeLastChar(positions); 
		List<String> list = Arrays.asList(positions);
	    try {
			Files.write(Paths.get(id+".log"), list,StandardOpenOption.APPEND);
		} catch (Exception e) {
			try {
			Files.write(Paths.get(id+".log"), list);
			}catch(Exception ex) {
				ex.printStackTrace();
				
			}
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}
	
	/**
	 * Removes the last char of String.
	 *
	 * @param str the str
	 * @return the string
	 */
	private static String removeLastChar(String str) {
	    return str.substring(0, str.length() - 1);
	}
	
	/**
	 * Gets the active buses.
	 *
	 * @return the active buses
	 */
	public List<Bus> getActiveBuses() {
		List<Bus> listBus = new ArrayList<Bus>();
		for (Bus b : listBuses) {
			if (b.isActive)
				listBus.add(b);
		}
		return listBus;
	}

	/**
	 * Checks if all buses are free or not.
	 *
	 * @return true, if is all free
	 */
	public boolean isAllFree() {
		if (allFree)
			return allFree;
		for (Bus b : listBuses) {
			if (!b.isActive)
				return allFree;
		}
		allFree = true;
		return allFree;
	}

	/**
	 * Active next bus.
	 */
	public void activeNextBus() {
		for (Bus b : listBuses) {
			if (!b.isActive && !b.isAtEndRouteState) {
				b.isActive = true;
				b.position = 0;
				break;
			}
		}
	}

	/**
	 * Simulate stop arrives.
	 */
	public void simulateStopArrives() {
		/*
		File folder = new File(folderStopsRateName);
		File[] listOfFiles = folder.listFiles();
		File file = listOfFiles[0];
		FileReader fileReader;
		try {
			String[] line = Files.readAllLines(Paths.get("stopsRate.log")).get((int) ti).split(",");
			for(int i =0;i<line.length;i++) {
				listStops.get(i).passengersLeft+=Integer.valueOf(line[i]);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		*/
		String positions = "";
		for (Stop s : listStops) {
			positions += s.simulateArrive() +",";
		}
		positions = removeLastChar(positions); 
		List<String> list = Arrays.asList(positions);
	    /*try {
			Files.write(Paths.get("stopsRate.log"), list,StandardOpenOption.APPEND);
		} catch (Exception e) {
			try {
			Files.write(Paths.get("stopsRate.log"), list);
			}catch(Exception ex) {
				ex.printStackTrace();
				
			}
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}*/
	}

	/**
	 * Simulate bus holding.
	 */
	public void simulateBusHolding() {
		for (Bus b : listBuses) {
			if (!b.isOnStop)
				continue;
			for (Stop s : listStops) {
				if (s.distDepot == b.position) {
					if (h[b.id - 1][s.id - 1] > 0) {
						b.isBusHolding = true;
						h[b.id - 1][s.id - 1]--;
					} else
						b.isBusHolding = false;
					break;
				}
			}
		}
	}

	/**
	 * Simulate bus position.
	 */
	public void simulateBusPosition() {
		for (Bus b : listBuses) {

			if (b.isBusHolding || isOccupiedNextPosition(b))
				continue;
			if(b.speed<1)
				b.speed+=0.1;
			b.position+=b.speed;
			if(b.position > turn && circularRoute) {
				b.previousStop = listStops.get(0);
				b.isOnStop=false;
				b.position=0;
			}else if(b.position > turn && !circularRoute) {
				b.isAtEndRouteState=true;
				b.isActive=false;
			}
			for (Stop s : listStops) {
				if (s.distDepot == Math.floor(b.position) && !s.isBusOnStop && !b.mustSkipStop && b.previousStop!=s) {
					b.speed+=0;
					b.position = (float) Math.floor(b.position);
					b.isOnStop = true;
					b.previousStop = s;
					s.isBusOnStop = true;
					break;
				}
				else if (s.distDepot == Math.floor(b.position) && !s.isBusOnStop && b.mustSkipStop && b.previousStop!=s) {
					b.mustSkipStop=false;
					break;
				}
			}
		}
	}

	/**
	 * Checks if is occupied next position.
	 *
	 * @param b the b
	 * @return true, if is occupied next position
	 */
	public boolean isOccupiedNextPosition(Bus b) {
		if (!b.isOnStop && !busesOvertake) {
			for (Bus b2 : getActiveBuses()) {
				if (b2.position - b.position <= 1 &&b2.position - b.position>0) {
					return true;
				}
			}
		}
		if(!b.isActive || b.isOnStop)
			return true;
		return false;
	}

	/**
	 * Simulate bus descend.
	 */
	public void simulateBusDescend() {
		for (Bus b : listBuses) {
			if (!b.isOnStop || b.isOnAboardState || b.isBusHolding)
				continue;
			if (b.isOnStop && !b.isOnDescendState) {
				b.isOnDescendState = true;
				for (Stop s : listStops) {
					if (s.distDepot == b.position) {
						int passengersDescend = Math.round(b.passengers * (1 - s.descendRate));
						b.passengers = passengersDescend;
						b.h += Math.round(passengersDescend * dwellPer);
						break;
					}
				}
			}
			if (b.isOnStop && b.isOnDescendState) {
				if (b.h == 0) {
					b.isOnDescendState = false;
				} else
					b.h -= 1;
			}
		}
	}

	/**
	 * Simulate bus aboard.
	 */
	public void simulateBusAboard() {
		for (Bus b : listBuses) {
			if (!b.isOnStop || b.isOnDescendState || b.isBusHolding)
				continue;
			if (b.isOnStop && !b.isOnAboardState) {
				b.isOnAboardState = true;
				for (Stop s : listStops) {
					if (s.distDepot == b.position) {
						if (b.passengers + s.passengersLeft > b.capacity) {
							int passengersLeft = b.passengers + s.passengersLeft - b.capacity;
							b.h += Math.round((s.passengersLeft - passengersLeft) * alightPer);
							b.passengers = s.passengersLeft - passengersLeft;
							s.passengersLeft = passengersLeft;
						} else {
							b.h += Math.round(s.passengersLeft * alightPer);
							b.passengers = s.passengersLeft;
							s.passengersLeft = 0;
						}
						break;
					}
				}
			}
			if (b.isOnStop && b.isOnAboardState) {
				if (b.h == 0) {
					b.isOnAboardState = false;
					b.isOnStop = false;
					for (Stop s : listStops) {
						if (s.distDepot == b.position) {
							s.isBusOnStop = false;
							break;
						}
					}
				} else
					b.h -= 1;
			}
		}
	}

	/**
	 * Checks if all buses agents made a decision
	 */
	public boolean allReady(){
		boolean ban = true;
		for(Bus b : listBuses) {
			if(!b.isReady)
				ban=false;
		}
			
		return ban;
	}
	/**
	 * Prepares the buses for the next action
	 */
	public void prepareBuses(){
		for(Bus b : listBuses) {
			b.isReady=false;
		}
	}

}
