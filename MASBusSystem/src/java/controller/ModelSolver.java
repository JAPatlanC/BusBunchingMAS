package controller;
import gurobi.GRB;
import gurobi.GRBConstr;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;
import jason.infra.repl.mi;
import mathematicalModels.CMOTBusHoldingModel;
import mathematicalModels.CMOTV2BusHoldingModel;
import model.Bus;
import model.ModelInstance;
import model.Stop;
/*
 * Classname: ModelSolver
 * Description: Solver for instances of ModelInstance 
 * Author: Jesús Angel Patlán Castillo
 * Changelog:
 * Date		 *********** Description
 * 05/08/2019			First Version: Added solveBusHolding
 * 26/08/2019			Code clean
 * 29/08/2019			Integration with ModelInstance class
 * 
 */
public class ModelSolver {
	/*
	 * Method: solveBusHolding
	 * Description: Solves a ModelInstance by using bus holding strategy
	 * Parameters: ModelInstance - Instance to solve
	 * Return: NULL
	 */
	public static void solveBusHolding(ModelInstance mi) {
		try {
			//CMOTBusHoldingModel model = new CMOTBusHoldingModel(mi);
			CMOTV2BusHoldingModel model = new CMOTV2BusHoldingModel(mi);
			model.optimize();
			model.getValues();
		} catch (GRBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
