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
// TODO: Auto-generated Javadoc

/**
 * Description: Solver for instances of ModelInstance 
 * Author: Jesús Angel Patlán Castillo
 * 
 */
public class ModelSolver implements Runnable{
	
	/**
	 * Solve bus holding.
	 *
	 * @param mi the mi
	 * @return the model instance
	 */
	public ModelInstance mi;
	
	public boolean ready;
	/*
	 * Method: solveBusHolding
	 * Description: Solves a ModelInstance by using bus holding strategy
	 * Parameters: ModelInstance - Instance to solve
	 * Return: NULL
	 */
	public ModelSolver(ModelInstance mi) {
		this.mi = mi;
		ready=false;
	}
	/*
	public static ModelInstance solveBusHolding(ModelInstance mi) {
		try {
			if(mi.busHoldingMethod.equals("CMOTV2")) {
				CMOTV2BusHoldingModel model = new CMOTV2BusHoldingModel(mi);
				model.optimize();	
				mi.h = model.getValues();
			}else {
				CMOTBusHoldingModel model = new CMOTBusHoldingModel(mi);
				model.optimize();
				mi.h = model.getValues();	
			}
		} catch (GRBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mi;
	}
	 */
	
	@Override
	public void run() {
		try{
			Thread.sleep(5000);
		}catch(Exception e) {
			
		}
		try {
			if(mi.busHoldingMethod.equals("CMOTV2")) {
				CMOTV2BusHoldingModel model = new CMOTV2BusHoldingModel(mi);
				model.optimize();	
				mi.h = model.getValues();
			}else {
				CMOTBusHoldingModel model = new CMOTBusHoldingModel(mi);
				model.optimize();
				mi.h = model.getValues();	
			}
			ready=true;
			System.out.println("LISTO");
		} catch (GRBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ModelInstance getMi() {
		return mi;
	}

	public void setMi(ModelInstance mi) {
		this.mi = mi;
	}

	public boolean isReady() {
		return ready;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
	}
	
	
}
