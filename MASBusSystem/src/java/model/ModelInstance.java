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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import controller.ModelSolver;
import jason.asSyntax.Literal;
import utils.Utils;

// TODO: Auto-generated Javadoc
/**
 * Description: Instance of the model, represented by a snapshot of the
 * environment.
 */
public class ModelInstance {

	/** The instance id. */
	public String id;

	/** The list buses. */
	public List<Bus> listBuses;

	/** The list stops. */
	public List<Stop> listStops;
	

	/** The list active passengers. */
	public List<Passenger> listActivePassengers;
	
	/** The list inactive passengers. */
	public List<Passenger> listInactivePassengers;

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

	/**
	 * The Holding times for every bus at every stop (filled when the model is
	 * solved by busholding).
	 */
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
	private String folderStopsRateName = "StopsRate/";

	/** The headway tolerance range. */
	public float headwayToleranceRange;

	/** The number of passengers that aboarded through the simulation. */
	public int passengersAboarding;

	/** The number of passengers that descended through the simulation. */
	public int passengersDescended;

	/** The number of passengers waiting at the current time. */
	public int passengersWaiting;

	/** The total distances. */
	private List<Float> totalDistances = new ArrayList<Float>();

	private Thread thread = null;

	private ModelSolver ms;
	
	private boolean isPrintResultsActive;
	
	private boolean isDebugActive;

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
			// System.out.println(s);
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
		for (Bus b : getActiveBuses()) {
			for (Bus b2 : getActiveBuses()) {
				if (b != b2) {
					distances.add((float) Math.abs(b.position - b2.position));
				}
			}
			if (!distances.isEmpty())
				dist += distances.stream().min(Comparator.naturalOrder()).get();
		}
		dist /= getActiveBuses().size();
		meanDistance.add(dist);
	}

	/**
	 * Update position txt.
	 */
	public void updatePositionTxt() {
		/*
		 * try (FileWriter writer = new FileWriter(id+".log"); BufferedWriter bw = new
		 * BufferedWriter(writer)) { bw.write(positions);
		 * 
		 * } catch (IOException e) { System.err.format("IOException: %s%n", e); }
		 */
		String positions = "";
		for (Bus b : listBuses) {
			positions += b.position + ",";
		}
		positions = removeLastChar(positions);
		List<String> list = Arrays.asList(positions);
		try {
			Files.write(Paths.get(id + ".log"), list, StandardOpenOption.APPEND);
		} catch (Exception e) {
			try {
				Files.write(Paths.get(id + ".log"), list);
			} catch (Exception ex) {
				ex.printStackTrace();

			}
			// TODO Auto-generated catch block
			// e.printStackTrace();
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
		File folder = new File(folderStopsRateName);
		File[] listOfFiles = folder.listFiles();
		File file = listOfFiles[0];
		FileReader fileReader;
		try {
			String[] line = Files.readAllLines(Paths.get(folderStopsRateName + "\\stopsRateEcovia.log")).get((int) ti)
					.split(",");
			for (int i = 0; i < line.length; i++) {
				listStops.get(i).passengersLeft += Integer.valueOf(line[i]);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		 * 
		 * String positions = ""; for (Stop s : listStops) { positions +=
		 * s.simulateArrive() +","; }
		 */
		/*
		 * positions = removeLastChar(positions); List<String> list =
		 * Arrays.asList(positions);try { Files.write(Paths.get("stopsRate.log"),
		 * list,StandardOpenOption.APPEND); } catch (Exception e) { try {
		 * Files.write(Paths.get("stopsRate.log"), list); }catch(Exception ex) {
		 * ex.printStackTrace();
		 * 
		 * } // TODO Auto-generated catch block //e.printStackTrace(); }
		 */
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
	 * Determines if the bus should increase or decrease its speed
	 */
	public void determineBusSpeedRegulation(Bus b) {
		b.distPreviousBus = 0;
		b.distNextBus = 0;
		if (b.position > b.previousBus.position) {
			b.distPreviousBus = b.position - b.previousBus.position;
		} else {
			b.distPreviousBus = Math.abs((b.previousBus.position - this.turn) + (b.position));
		}
		if (b.position < b.nextBus.position) {
			b.distNextBus = b.nextBus.position - b.position;
		} else {
			b.distNextBus = Math.abs((b.nextBus.position - this.turn) + (b.position));
		}
		double range = b.distPreviousBus + b.distNextBus;
		double centralPoint = range / 2 + b.previousBus.position;
		b.speedUp = false;
		b.slowDown = false;
		System.out.println("Bus speed regulation: " + b.id);
		/*
		 * System.out.println("CP:"+centralPoint); System.out.println("Range: "+range);
		 * System.out.println(b.distNextBus); System.out.println(b.distPreviousBus);
		 */
		if (b.position > centralPoint + (range * this.headwayToleranceRange)) {
			// System.out.println("Slow down!");
			b.slowDown = true;
		} else if (b.position < centralPoint - (range * this.headwayToleranceRange)) {
			// System.out.println("Speed up!");
			b.speedUp = true;
			b.mustSkipStop = true;
		} else {
			// System.out.println("OK!");
			b.speedUp = true;
			b.mustSkipStop = false;
		}
	}

	/**
	 * Simulate bus position.
	 */
	public void simulateBusPosition() {
		if (getActiveBuses().size() > 2) {
			for (Bus b : getActiveBuses()) {
				determineBusSpeedRegulation(b);

			}
		}
		for (Bus b : listBuses) {

			if (b.isBusHolding || isOccupiedNextPosition(b))
				continue;
			if (b.speed < b.previousStop.speedLimit && b.speedUp)
				b.speed += 0.1;
			if (b.speed > b.previousStop.speedLimit && b.slowDown)
				b.speed -= 0.1;
			b.position += b.speed;
			if (b.position > turn && circularRoute) {
				b.previousStop = listStops.get(0);
				b.isOnStop = false;
				b.position = 0;
			} else if (b.position > turn && !circularRoute) {
				b.isAtEndRouteState = true;
				b.isActive = false;
			}
			for (Stop s : listStops) {
				if (s.distDepot == Math.floor(b.position) && !s.isBusOnStop && !b.mustSkipStop && b.previousStop != s) {
					b.speed = 0;
					b.position = (float) Math.floor(b.position);
					b.isOnStop = true;
					b.previousStop = s;
					s.isBusOnStop = true;
					break;
				} else if (s.distDepot == Math.floor(b.position) && !s.isBusOnStop && b.mustSkipStop
						&& b.previousStop != s) {
					b.position = Math.floor(b.position) + 1;
					b.previousStop = s;
					b.mustSkipStop = false;
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
				if (b2.position - (b.position + b.speed) <= 1 && b2.position - b.position > 0) {
					return true;
				}
			}
		}
		if (!b.isActive || b.isOnStop)
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
						passengersDescended += b.passengers - passengersDescend;
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
							passengersAboarding += s.passengersLeft - passengersLeft;
							b.h += Math.round((s.passengersLeft - passengersLeft) * alightPer);
							b.passengers = s.passengersLeft - passengersLeft;
							s.passengersLeft = passengersLeft;
						} else {
							b.h += Math.round(s.passengersLeft * alightPer);
							b.passengers += s.passengersLeft;
							passengersAboarding += s.passengersLeft;
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
	public boolean allReady() {
		boolean ban = true;
		for (Bus b : listBuses) {
			if (!b.isReady)
				ban = false;
		}

		return ban;
	}

	/**
	 * Prepares the buses for the next action
	 */
	public void prepareBuses() {
		for (Bus b : listBuses) {
			b.isReady = false;
		}
	}

	public void printResults() {
		if(!this.isPrintResultsActive)
			return;
		float averageHeadway = (float) this.meanDistance.stream().mapToInt(a -> Math.round(a)).average().getAsDouble();
		int maxHeadway = this.meanDistance.stream().mapToInt(a -> Math.round(a)).max().getAsInt();
		int minHeadway = this.meanDistance.stream().mapToInt(a -> Math.round(a)).min().getAsInt();
		double stdDevHeadway = Utils.stdDev(this.meanDistance);
		double varianceHeadway = Math.pow(stdDevHeadway, 2);
		totalDistances.add(averageHeadway);
		System.out.println("*******************************************");

		System.out.println("Stops: " + this.listStops.size());
		System.out.println("Buses: " + this.listBuses.size());
		System.out.println("Bus holding solver calls: " + this.busHoldingCalls);
		System.out.println("Bus Alight: " + this.alightPer);
		System.out.println("Bus Dwell: " + this.dwellPer);
		System.out.println("HTR: " + this.headwayToleranceRange);
		System.out.println("Free of buses: " + this.busHoldingCalls);
		System.out.println("Overtake: " + this.busesOvertake);
		System.out.println("Circular: " + this.circularRoute);
		System.out.println("Average Headway: " + averageHeadway);
		System.out.println("Max Headway: " + maxHeadway);
		System.out.println("Min Headway: " + minHeadway);
		System.out.println("Std Deviation: " + stdDevHeadway);
		System.out.println("Variance: " + varianceHeadway);
		System.out.println("Passengers who aboarded: " + this.passengersAboarding);
		System.out.println("Passengers who descended: " + this.passengersDescended);

		System.out.println("" + this.listStops.size());
		System.out.println("" + this.listBuses.size());
		System.out.println("" + this.busHoldingCalls);
		System.out.println("" + this.alightPer);
		System.out.println("" + this.dwellPer);
		System.out.println("" + this.busesOvertake);
		System.out.println("" + this.circularRoute);
		System.out.println("" + this.headwayToleranceRange);
		System.out.println("" + averageHeadway);
		// System.out.println("" + maxHeadway);
		// System.out.println("" + minHeadway);
		// System.out.println("" + stdDevHeadway);
		// System.out.println("" + varianceHeadway);
		System.out.println("*******************************************");
	}

	public void updateInstanceEnvironment() {
		if(this.isDebugActive)
			this.printDebug();
		this.updateBusesNeighborhood();
		// Update stop arrive
		this.simulateStopArrives();
		// Simulate possible bus holding
		this.simulateBusHolding();
		// Update bus position
		this.simulateBusPosition();
		// Update bus passengers descending
		this.simulateBusDescend();
		// Update bus passengers aboarding
		this.simulateBusAboard();

		this.updatePositionTxt();
		this.updateMetric();
	}

	public void printDebug() {
		System.out.println(this);
		System.out.println("Passengers who aboarded: " + this.passengersAboarding);
		System.out.println("Passengers who descended: " + this.passengersDescended);
	}

	public void updateBusesNeighborhood() {
		// Update buses neighborhood
		if (this.getActiveBuses().size() > 2) {
			List<Bus> orderedBus = new ArrayList<Bus>(this.getActiveBuses());
			Collections.sort(orderedBus, (o1, o2) -> Double.compare(o1.position, o2.position));
			for (int count = 0; count < orderedBus.size(); count++) {
				Bus b = this.listBuses.get(orderedBus.get(count).id - 1);
				if (count == 0) {
					b.nextBus = orderedBus.get(count + 1);
					b.previousBus = orderedBus.get(orderedBus.size() - 1);
				} else if (count == orderedBus.size() - 1) {
					b.nextBus = orderedBus.get(0);
					b.previousBus = orderedBus.get(count - 1);
				} else {
					b.nextBus = orderedBus.get(count + 1);
					b.previousBus = orderedBus.get(count - 1);
				}
			}
		}
	}

	public ArrayList<Literal> getBusHoldingValues() {
		ArrayList<Literal> listPercepts = null;
		// Call bus holding solver each period, instances a thread for the mathematical
		// model to be solved in parallel
		if (this.ti % this.busHoldingPeriod == 0 && this.ti != 0) {
			thread = new Thread(ms);
			ms = new ModelSolver(this);
			thread.start();
		}

		// Checks if the model solver has been called
		if (ms != null) {
			listPercepts = new ArrayList<Literal>();
			// If the model is already solved, then it analyzes the holding times of each
			// bus
			// at each stop and that information is added to the C-agent beliefs
			if (ms.isReady()) {
				this.h = ms.getMi().h;
				for (int i = 0; i < this.h.length; i++) {
					for (int j = 0; j < this.h[i].length; j++) {
						if (this.h[i][j] > 0) {
							listPercepts.add(Literal
									.parseLiteral("tellBH(" + (i + 1) + "," + (j + 1) + "," + this.h[i][j] + ")"));
						}
					}
				}
				ms = null;
				thread = null;
			}
		}
		return listPercepts;
	}
}
