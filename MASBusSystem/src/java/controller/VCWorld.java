package controller;

/*
 * Classname: VCWorld
 * Description: Environment of the MAS Bus route
 * Author: Jesús Angel Patlán Castillo
 * Changelog:
 * Date		 *********** Description
 * 20/07/2019			First Version
 * 05/08/2019			Added Gurobi Dependency
 * 26/08/2019			Code cleaned
 * 
 */
import jason.asSyntax.*;
import jason.environment.*;
import model.Bus;
import model.ModelInstance;
import model.Stop;

import java.util.logging.*;

import gurobi.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class VCWorld extends Environment {

	private Logger logger = Logger.getLogger("MAS-BusSystem." + VCWorld.class.getName());


	/*
	 * Method: init Description: Initializes the MAS Bus System Parameters:
	 * String[]- args Return: NULL
	 */
	@Override
	public void init(String[] args) {
		logger.info("Initializing");
		// The name of the folder to read.
		String folderName = "C:\\Users\\ja_pa\\OneDrive\\Documentos\\Tesis\\instancias\\ninstancias\\";
		int i=0;
		for (ModelInstance mi : ModelInstanceLoader.loadBusHoldingModels(folderName)) {
			System.out.println("------Resolviendo instancia "+(i++)+"---------");
			ModelSolver.solveBusHolding(mi);
			System.out.println("---------------------------------------------");
		}
		updatePercepts();
	}

	/*
	 * Method: updatePercepts Description: Method to update the percepts of the
	 * agents Parameters: String[] - args Return: NULL
	 */
	private void updatePercepts() {

	}

	/*
	 * Method: executeAction Description: Method to execute an action of an agent
	 * Parameters: String agName - Agent who executes the action Structure action -
	 * Action to execute Return: NULL
	 */
	@Override
	public boolean executeAction(String agName, Structure action) {
		logger.info("--------------------------------------");
		logger.info("Agent: " + agName + " executing action: " + action);

		updatePercepts();
		return true;
	}

	/*
	 * Method: stop Description: Method to stop the MAS Bus System Parameters: NULL
	 * Return: NULL
	 */
	@Override
	public void stop() {
		super.stop();
	}
}
