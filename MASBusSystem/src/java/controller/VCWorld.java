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
		updatePercepts();
	}

	/**
	 * Update percepts.
	 */
	private void updatePercepts() {
		clearPercepts();
		updateEnvironment();
		if (ban)
			addPercept(Literal.parseLiteral("onRoute"));
		else
			addPercept(Literal.parseLiteral("onStop"));
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
		if (action.getFunctor().equals("continue1") && modelInstance.ti != modelInstance.tn) {
			ban = false;
		}
		if (action.getFunctor().equals("continue2") && modelInstance.ti != modelInstance.tn) {
			ban = true;
		}
		updatePercepts();
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
		//System.out.println(modelInstance);
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Free bus at first stop if there is a bus not active
		if (!modelInstance.isAllFree() && modelInstance.ti % modelInstance.releaseTime == 0) {
			modelInstance.activeNextBus();
		}
		// Call bus holding solver each period
		if (modelInstance.ti % modelInstance.busHoldingPeriod == 0 && modelInstance.ti != 0) {
			//modelInstance = ModelSolver.solveBusHolding(modelInstance);
			ms = new ModelSolver(modelInstance);
			Thread thread = new Thread(ms);
			thread.start();
		}
		if(ms != null) {
			System.out.println(modelInstance);
			if(ms.isReady()) {
				modelInstance.h = ms.getMi().h;
				ms = null;
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
