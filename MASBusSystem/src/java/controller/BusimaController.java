package controller;

/**
 * Description: Environment of the MAS Bus route
 * 
 */
import jason.asSyntax.*;
import jason.environment.*;
import jason.infra.repl.mi;
import jason.stdlib.abolish;
import model.Bus;
import model.ModelInstance;
import model.Stop;
import utils.Utils;

import java.util.logging.*;

import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.distribution.PoissonDistributionTest;

import gurobi.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

// TODO: Auto-generated Javadoc
/**
 * The Class VCWorld.
 */
public class BusimaController extends Environment {

	/** The ban. */
	private boolean ban = true;

	private Thread thread = null;

	/** The model instance. */
	private ModelInstance modelInstance;

	/** The logger. */
	private Logger logger = Logger.getLogger("MAS-BusSystem." + BusimaController.class.getName());

	/** The folder name. */
	private String folderName = "instances/";

	/** The i. */
	private int i = 0;

	/** The samples. */
	private int samples = 1;

	/** The total distances. */
	private List<Float> totalDistances;

	private ModelSolver ms;

	/**
	 * Initializes the MAS Bus System
	 *
	 * @param args the args
	 */
	@Override
	public void init(String[] args) {
		modelInstance = ModelInstanceLoader.loadBusHoldingModels(folderName).get(0);
		logger.info("Initializing");
		totalDistances = new ArrayList<Float>();
		ModelSolver ms = null;
		updatePercepts("control", "start");
	}

	/**
	 * Update percepts.
	 */
	private void updatePercepts(String agName, String action) {
		clearPercepts();
		if (agName.equals("control")) {
			addPercept(Literal.parseLiteral("start"));
		}
		if (action.equals("start")) {
			ban = true;
			updateEnvironment();
		}
	}

	/**
	 * Method to execute an action of an agent.
	 *
	 * @param agName Agent who executes the action
	 * @param action the action
	 * @return true, if successful
	 */
	@Override
	public boolean executeAction(String agName, Structure action) {
		System.out.println(agName);
		System.out.println(action.getFunctor());
		//C-Agent actions
		if (agName.equals("control")) {
			if (action.getFunctor().equals("skipStop") && modelInstance.ti != modelInstance.tn) {
				String params = action.getTerm(0).toString();
				List<String> listAgId = Arrays.asList(params.substring(1, params.length()-1).split(","));
				for (String agId : listAgId) {
					Bus bus = modelInstance.listBuses.get(Integer.parseInt(agId)-1);
					for (Bus b : modelInstance.getActiveBuses()) {
						if (b.position - bus.position > -5 && b.position - bus.position < 0) {
							bus.mustSkipStop = true;
						}
					}
				}

				removePercept(agName, Literal.parseLiteral("validateSkipStop("+params+")"));
			}
		} 
		
		//Bus Agent actions
		else {
			int agId = Integer.valueOf(agName.replaceFirst(".*?(\\d+).*", "$1")) - 1;
			System.out.println(agId);
			modelInstance.listBuses.get(agId).isReady = true;
			if (action.getFunctor().equals("doBusHold") && modelInstance.ti != modelInstance.tn) {
				System.out.println("Do BUSHOLDING");
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
				}
			}
		}
		updatePercepts(agName, action.getFunctor());
		return true;
	}

	/**
	 * Method to stop the MAS Bus System.
	 */
	@Override
	public void stop() {
		super.stop();
	}

	/**
	 * Update environment.
	 */
	public void updateEnvironment() {
		while (ban) {
			//System.out.println(modelInstance);
			  System.out.println("Passengers who aboarded: "+modelInstance.passengersAboarding);
			  System.out.println("Passengers who descended: "+modelInstance.passengersDescended);
			try {
				Thread.sleep(0);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//Allows the C-agent to analyze the data sent by the bus agents
			if (modelInstance.ti % 3 == 0 && modelInstance.ti != 0) {
				String busesPosition ="[";
				String busesPassengers ="[";
				String busesSpeed ="[";
				String busesNextStop ="[";
				for (Bus b : modelInstance.getActiveBuses()) {
					busesPosition+=b.position+",";
					busesPassengers+=b.passengers+",";
					busesSpeed+=b.speed+",";
					int nextStop = 1;
					if (b.previousStop.id != modelInstance.lastStopId)
						nextStop = b.previousStop.id + 1;
					busesNextStop+=nextStop+",";
					addPercept(Literal.parseLiteral("bcBusPosition(" + b.id + "," + b.position + ")"));
					addPercept(Literal.parseLiteral("bcPassengers(" + b.id + "," + b.passengers + ")"));
					addPercept(Literal.parseLiteral("bcBusSpeed(" + b.id + "," + b.speed + ")"));
					addPercept(Literal.parseLiteral("bcBusNextStop(" + b.id + "," + nextStop + ")"));
					//addPercept("control", Literal.parseLiteral("start"));
				}
				//Analyzes if the buses might do a skip stop based on their position
				String busesOnRoute = "[";
				for (Bus b : modelInstance.getActiveBuses()) {
					String busName = "Bus_" + b.id;
					if (b.isOnStop) {
						addPercept(busName, Literal.parseLiteral("onStop"));
					} else {
						addPercept(busName, Literal.parseLiteral("onRoute"));
						busesOnRoute += b.id + ",";
					}
				}
				busesOnRoute = busesOnRoute.substring(0, busesOnRoute.length() - 1) + "]";
				addPercept("control", Literal.parseLiteral("validateSkipStop(" + busesOnRoute + ")"));
				
				ban = false;
			}

			
			// Free bus at first stop if there is a bus not active
			if (!modelInstance.isAllFree() && modelInstance.ti % modelInstance.releaseTime == 0) {
				modelInstance.activeNextBus();
				try {
					String busName = getEnvironmentInfraTier().getRuntimeServices().createAgent(
							"Bus_" + modelInstance.getActiveBuses().size(), // agent name
							"bus.asl", // AgentSpeak source
							null, // default agent class
							null, // default architecture class
							null, // default belief base parameters
							null, null);
					getEnvironmentInfraTier().getRuntimeServices().startAgent(busName);
					addPercept(busName, Literal.parseLiteral("onRoute"));
				} catch (Exception e) {
					System.out.println("Can't add agent");
					e.printStackTrace();
				}
			}
			// Update buses neighborhood
			if (modelInstance.getActiveBuses().size() > 2) {
				List<Bus> orderedBus = new ArrayList<Bus>(modelInstance.getActiveBuses());
				Collections.sort(orderedBus, (o1, o2) -> Double.compare(o1.position, o2.position));
				for (int count = 0; count < orderedBus.size(); count++) {
					Bus b = modelInstance.listBuses.get(orderedBus.get(count).id - 1);
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
			// Call bus holding solver each period, instances a thread for the mathematical model to be solved in parallel
			if (modelInstance.ti % modelInstance.busHoldingPeriod == 0 && modelInstance.ti != 0) {
				thread = new Thread(ms);
				ms = new ModelSolver(modelInstance);
				thread.start();
			}
			
			//Checks if the model solver has been called
			if (ms != null) {
				//If the model is already solved, then it analyzes the holding times of each bus
				//at each stop and that information is added to the C-agent beliefs
				if (ms.isReady()) {
					modelInstance.h = ms.getMi().h;
					for (int i = 0; i < modelInstance.h.length; i++) {
						for (int j = 0; j < modelInstance.h[i].length; j++) {
							if (modelInstance.h[i][j] > 0) {
								addPercept("control", Literal.parseLiteral(
										"tellBH(" + (i + 1) + "," + (j + 1) + "," + modelInstance.h[i][j] + ")"));
							}
						}
					}
					ms = null;
					thread = null;
				}
			}
			// Update stop arrive
			modelInstance.simulateStopArrives();
			// Simulate possible bus holding
			modelInstance.simulateBusHolding();
			// Update bus position
			modelInstance.simulateBusPosition();
			// Update bus passengers descending
			modelInstance.simulateBusDescend();
			// Update bus passengers aboarding
			modelInstance.simulateBusAboard();

			// Update file positions
			modelInstance.updatePositionTxt();
			try {
				modelInstance.updateMetric();
			} catch (Exception e) {

			}
			modelInstance.ti += 1;
			if (modelInstance.ti == modelInstance.tn) {
				clearPercepts("start");
				addPercept("control", Literal.parseLiteral("finish"));
				i++;
				float averageHeadway = (float) modelInstance.meanDistance.stream().mapToInt(a -> Math.round(a))
						.average().getAsDouble();
				int maxHeadway = modelInstance.meanDistance.stream().mapToInt(a -> Math.round(a)).max().getAsInt();
				int minHeadway = modelInstance.meanDistance.stream().mapToInt(a -> Math.round(a)).min().getAsInt();
				double stdDevHeadway = Utils.stdDev(modelInstance.meanDistance);
				double varianceHeadway = Math.pow(stdDevHeadway, 2);
				totalDistances.add(averageHeadway);
				System.out.println("*******************************************");
			
			  System.out.println("Stops: "+modelInstance.listStops.size());
			  System.out.println("Buses: "+modelInstance.listBuses.size());
			  System.out.println("Bus holding solver calls: "+modelInstance.busHoldingCalls);
			  System.out.println("Bus Alight: "+modelInstance.alightPer);
			  System.out.println("Bus Dwell: "+modelInstance.dwellPer);
			  System.out.println("HTR: "+modelInstance.headwayToleranceRange);
			  System.out.println("Free of buses: "+modelInstance.busHoldingCalls);
			  System.out.println("Overtake: "+modelInstance.busesOvertake);
			  System.out.println("Circular: "+modelInstance.circularRoute);
			  System.out.println("Average Headway: "+averageHeadway);
			  System.out.println("Max Headway: "+maxHeadway);
			  System.out.println("Min Headway: "+minHeadway);
			  System.out.println("Std Deviation: "+stdDevHeadway);
			  System.out.println("Variance: "+varianceHeadway);
			  System.out.println("Passengers who aboarded: "+modelInstance.passengersAboarding);
			  System.out.println("Passengers who descended: "+modelInstance.passengersDescended);
				
				System.out.println("" + modelInstance.listStops.size());
				System.out.println("" + modelInstance.listBuses.size());
				System.out.println("" + modelInstance.busHoldingCalls);
				System.out.println("" + modelInstance.alightPer);
				System.out.println("" + modelInstance.dwellPer);
				System.out.println("" + modelInstance.busesOvertake);
				System.out.println("" + modelInstance.circularRoute);
				System.out.println(""+modelInstance.headwayToleranceRange);
				System.out.println("" + averageHeadway);
				//System.out.println("" + maxHeadway);
				//System.out.println("" + minHeadway);
				//System.out.println("" + stdDevHeadway);
				//System.out.println("" + varianceHeadway);
				System.out.println("*******************************************");
				if (i < samples) {
					modelInstance = ModelInstanceLoader.loadBusHoldingModels(folderName).get(0);
					modelInstance.busHoldingCalls += 2 * (i);
					modelInstance.busHoldingPeriod = (int) Math
							.round(modelInstance.tn * 0.9 / (modelInstance.busHoldingCalls + 1));
				}
			}
		}

	}

}
