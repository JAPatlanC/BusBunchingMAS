package controller;

/**
 * Description: Environment of the MAS Bus route
 * 
 */
import jason.asSyntax.*;
import jason.environment.*;
import jason.infra.repl.mi;
import model.Bus;
import model.ModelInstance;
import model.Stop;

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
public class VCWorld extends Environment {

	/** The ban. */
	private boolean ban = true;
	/** The ban. */
	private boolean ban2 = true;

	private Thread thread = null;
	
	/** The model instance. */
	private ModelInstance modelInstance;
	
	/** The logger. */
	private Logger logger = Logger.getLogger("MAS-BusSystem." + VCWorld.class.getName());
	
	/** The folder name. */
	private String folderName = "C:\\Users\\ja_pa\\OneDrive\\Documentos\\Tesis\\instancias\\New Instances\\";
	
	/** The i. */
	private int i=0;
	
	/** The samples. */
	private int samples=1;
	
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
		ModelSolver ms =null;
		// The name of the folder to read.
		// String folderName =
		// "C:\\Users\\ja_pa\\OneDrive\\Documentos\\Tesis\\instancias\\Instances on T
		// time\\";
		/*
		 * int i=0; for (ModelInstance mi :
		 * ModelInstanceLoader.loadBusHoldingModels(folderName)) {
		 * System.out.println("------Resolviendo instancia "+(i++)+"---------");
		 * ModelSolver.solveBusHolding(mi);
		 * System.out.println("----------------------------------------------"); }
		 */
		updatePercepts("control");
	}

	/**
	 * Update percepts.
	 */
	private void updatePercepts(String agName) {
		clearPercepts();
		if(agName.equals("control"))
			updateEnvironment();

		
		if (ban)
			addPercept(Literal.parseLiteral("paso1"));
		else
			addPercept(Literal.parseLiteral("paso2"));
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
		if(agName.equals("control")) {
			if (action.getFunctor().equals("skipStop") && modelInstance.ti != modelInstance.tn) {
				System.out.println("Skipping...");
				try {
					Thread.sleep(5000);
				}catch(Exception e) {
					
				}
				int agId = Integer.parseInt(action.getTerm(1).toString());
				Bus bus = modelInstance.listBuses.get(agId);
				for(Bus b : modelInstance.getActiveBuses()) {
					if(b.position - bus.position > -5 && b.position - bus.position<0) {
						bus.mustSkipStop=true;
					}
				}
			}
			if (action.getFunctor().equals("continue1") && modelInstance.ti != modelInstance.tn) {
				ban = false;
			}
			if (action.getFunctor().equals("continue2") && modelInstance.ti != modelInstance.tn) {
				ban = true;
			}
			
			updatePercepts("control");
		}else {
			int agId = Integer.valueOf(agName.replaceFirst(".*?(\\d+).*", "$1"))-1;
			System.out.println(agId);
			modelInstance.listBuses.get(agId).isReady=true;
			if(action.getFunctor().equals("doBusHold") && modelInstance.ti != modelInstance.tn) {
				System.out.println("Do BUSHOLDING");
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
				}
			}
			if (action.getFunctor().equals("continue1") && modelInstance.ti != modelInstance.tn) {
				ban2 = false;
				
			}
			if (action.getFunctor().equals("continue2") && modelInstance.ti != modelInstance.tn) {
				ban2 = true;
				
			}
			updatePercepts(agName);
		}
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
		System.out.println(modelInstance);
		try {
			Thread.sleep(5);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Free bus at first stop if there is a bus not active
		if (!modelInstance.isAllFree() && modelInstance.ti % modelInstance.releaseTime == 0) {
			if(modelInstance.ti%10==0) {
				for(Bus b : modelInstance.getActiveBuses()) {
					addPercept("bcBusPosition("+b.id+","+b.position+")");
					addPercept("bcPassengers("+b.id+","+b.position+")");
					addPercept("bcBusSpeed("+b.id+","+b.position+")");
					addPercept("bcBusNextStop("+b.id+","+b.position+")");	
				}
			}
			modelInstance.activeNextBus();

			try {
				String busName = getEnvironmentInfraTier().getRuntimeServices().createAgent("Bus_"+modelInstance.getActiveBuses().size(), // agent name
						"bus.asl", // AgentSpeak source
						null, // default agent class
						null, // default architecture class
						null, // default belief base parameters
						null, null);
				addPercept(busName,Literal.parseLiteral("onRoute"));
				getEnvironmentInfraTier().getRuntimeServices().startAgent(busName);
			} catch (Exception e) {
				System.out.println("Can't add agent");
				e.printStackTrace();
			} // default settings
		}

		// Call bus holding solver each period
		if (modelInstance.ti % modelInstance.busHoldingPeriod == 0 && modelInstance.ti != 0) {
				thread = new Thread(ms);
				ms = new ModelSolver(modelInstance);
				thread.start();
		}
		if(ms != null) {
			if(ms.isReady()) {
				modelInstance.h = ms.getMi().h;
				for(int i=0;i<modelInstance.h.length;i++) {
					for(int j=0;j<modelInstance.h[i].length;j++) {
						if(modelInstance.h[i][j]>0) {
							addPercept("control",Literal.parseLiteral("tellBH(Bus_"+(i+1)+","+(j+1)+","+modelInstance.h[i][j]+")"));
						}
					}
				}
				ms = null;
				thread=null;
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

		for(Bus b : modelInstance.getActiveBuses()) {
			String busName = "Bus_"+b.id;
			if(b.isOnStop) {
				addPercept(busName,Literal.parseLiteral("onStop"));
			}else {
				addPercept(busName,Literal.parseLiteral("onRoute"));
				addPercept("control",Literal.parseLiteral("validateSkipStop("+b.id+")"));
			}
		}
		
		// Update file positions
		modelInstance.updatePositionTxt();
		try {
			modelInstance.updateMetric();
		} catch (Exception e) {

		}
		modelInstance.ti += 1;
		if (modelInstance.ti == modelInstance.tn) {
			i++;
			float averageHeadway = (float) modelInstance.meanDistance.stream().mapToInt(a -> Math.round(a)).average().getAsDouble();
			int maxHeadway = modelInstance.meanDistance.stream().mapToInt(a -> Math.round(a)).max().getAsInt();
			int minHeadway = modelInstance.meanDistance.stream().mapToInt(a -> Math.round(a)).min().getAsInt();
			double stdDevHeadway = stdDev(modelInstance.meanDistance);  
			double varianceHeadway = Math.pow(stdDevHeadway,2);  
			totalDistances.add(averageHeadway);
			System.out.println("*******************************************");
			/*
			System.out.println("Stops: "+modelInstance.listStops.size());
			System.out.println("Buses: "+modelInstance.listBuses.size());
			System.out.println("Bus holding solver calls: "+modelInstance.busHoldingCalls);
			System.out.println("Bus Alight: "+modelInstance.alightPer);
			System.out.println("Bus Dwell: "+modelInstance.dwellPer);
			System.out.println("Free of buses: "+modelInstance.busHoldingCalls);
			System.out.println("Overtake: "+modelInstance.busesOvertake);
			System.out.println("Circular: "+modelInstance.circularRoute);
			System.out.println("Average Headway: "+averageHeadway);
			System.out.println("Max Headway: "+maxHeadway);
			System.out.println("Min Headway: "+minHeadway);
			System.out.println("Std Deviation: "+stdDevHeadway);
			System.out.println("Variance: "+varianceHeadway);
			*/
			System.out.println(""+modelInstance.listStops.size());
			System.out.println(""+modelInstance.listBuses.size());
			System.out.println(""+modelInstance.busHoldingCalls);
			System.out.println(""+modelInstance.alightPer);
			System.out.println(""+modelInstance.dwellPer);
			System.out.println(""+modelInstance.busHoldingCalls);
			System.out.println(""+modelInstance.busesOvertake);
			System.out.println(""+modelInstance.circularRoute);
			System.out.println(""+averageHeadway);
			System.out.println(""+maxHeadway);
			System.out.println(""+minHeadway);
			System.out.println(""+stdDevHeadway);
			System.out.println(""+varianceHeadway);
			System.out.println("*******************************************");
			if(i<samples) {
				modelInstance = ModelInstanceLoader.loadBusHoldingModels(folderName).get(0);
				modelInstance.busHoldingCalls+=2*(i);
				modelInstance.busHoldingPeriod = (int) Math.round(modelInstance.tn*0.9 / (modelInstance.busHoldingCalls+1));
				//modelInstance.dwellPer*=2*(i+1);
				//modelInstance.alightPer*=2*(i+1);
				//modelInstance.maxHeadway+=1*(i+1);
			}
			if(i==samples) {
				//System.out.println(totalDistances.stream().mapToDouble(a -> a).average().getAsDouble());
			}
		}
		
	}

	/**
	 * Std dev.
	 *
	 * @param inputArray the input array
	 * @return the double
	 */
	public static double stdDev(List<Float> inputArray) {
	    double sum = 0;
	    double sq_sum = 0;
	    for (int i = 0; i < inputArray.size(); ++i) {
	      float ai = inputArray.get(i);
	      sum += ai;
	      sq_sum += ai * ai;
	    }
	    double mean = sum / inputArray.size();
	    double variance = sq_sum / inputArray.size()- mean * mean;
	    return Math.sqrt(variance);
	  }
}
