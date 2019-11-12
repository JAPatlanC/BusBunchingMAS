package mathematicalModels;

import gurobi.GRB;
import gurobi.GRBConstr;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;
import model.Bus;
import model.ModelInstance;
import model.Stop;

/*
 * Classname: CMOTBusHoldingModel
 * Description: BusHolding Model designed by Citlali Maryuri Olvera Toscano for a rapid bus transport model 
 * Author: Jesús Angel Patlán Castillo
 * Changelog:
 * Date		 *********** Description
 * 28/09/2019			First Version
 * 
 */
public class CMOTBusHoldingModel {
	public GRBModel model;
	public ModelInstance mi;
	public int k; // # Buses
    public int s; // # Stops
    public float maxHold; //Max hold in the stops in minutes
    public float dwellPer; //Dwell time per person
    public float alightPer; //Alight time per person
    public float t0; //Initial time
    public int capacity; //Homogeneous capacity of buses
    public float headways; //Distances between buses
    public float turn; //Expected time per turn of a bus

    public float doors; //Time to open/close the doors in minutes of the buses
    
    public float[] lambdaArrive; //Arriving rates per stop
    public float[] lambdaDescend; //Descending rates per stop
    public float[] dist; //Distances (in time) between stops
    public float[] distDepot; //Distances (in time) from depot
    public float[] passengersLeft; //People waiting at stop S in time t0

    public float[] indexBuses = new float[k]; //Index of active buses
    public float[] positionBuses = new float[k];  //Position between buses (in time)
    public float[] passengers = new float[k]; // Passengers in each bus after stop Sdk
    public float[] sdk = new float[k]; // Previous stop that bus k has passed
    public GRBVar[][] h; //Holding time of bus k in stop s
    public GRBVar[][] td; //Departure time of bus k in stop s
    public GRBVar[][] p; 	      //Penalty for separation between buses
    public GRBVar[][] z; //Passengers of bus k in S
    public GRBVar[][] v1; //Min dwell of k in S
    public GRBVar[][] v2; //Min dwell of k in S
    public GRBVar[][] dwell;	//Dwell for bus k in s
    public GRBVar[][] alight; //Alight for bus k in S
	
	public CMOTBusHoldingModel(ModelInstance mi) throws GRBException {
			this.mi = mi;
	      // Create empty environment, set options, and start
	      GRBEnv env = new GRBEnv(true);
	      env.set("logFile", "mip1.log");
	      env.start();

		GRBModel model = new GRBModel(env);
		// Create variables
	      // Instances variables
	      double buffer=0.8; // Headaway % tolerance for penalty
	      
	      k = mi.getActiveBuses().size();// # Buses
	      s = mi.listStops.size(); // # Stops
	      maxHold=mi.maxHold; //Max hold in the stops in minutes
	      dwellPer=mi.dwellPer; //Dwell time per person
	      alightPer=mi.alightPer; //Alight time per person
	      t0=mi.t0; //Initial time
	      capacity=mi.listBuses.get(0).capacity; //Homogeneous capacity of buses
	      headways=mi.headways; //Distances between buses
	      turn=mi.turn; //Expected time per turn of a bus

	      doors = mi.doors; //Time to open/close the doors in minutes of the buses
	      
	      lambdaArrive = new float[s]; //Arriving rates per stop
	      lambdaDescend = new float[s]; //Descending rates per stop
	      dist = new float[s]; //Distances (in time) between stops
	      distDepot = new float[s]; //Distances (in time) from depot
	      passengersLeft = new float[s]; //People waiting at stop S in time t0
	      int count=0;
	      for(Stop st : mi.listStops) {
	    	  lambdaArrive[count]=st.arriveRate;
	    	  lambdaDescend[count]=st.descendRate;
	    	  dist[count]=st.distNextStop;
	    	  distDepot[count]=st.distDepot;
	    	  passengersLeft[count]=st.passengersLeft;
	    	  count++;
	      }
	      count=0;
	      indexBuses = new float[k]; //Index of active buses
	      positionBuses = new float[k];  //Position between buses (in time)
	      passengers = new float[k]; // Passengers in each bus after stop Sdk
	      sdk = new float[k]; // Previous stop that bus k has passed
	      for(Bus b : mi.getActiveBuses()) {
	    	  indexBuses[count] = b.id;
	    	  positionBuses[count] = b.position;
	    	  passengers[count] = b.passengers; 
	    	  sdk[count] = b.previousStop.id;
	    	  count++;
	      }
	      
	      //LM Variables
	      p = new GRBVar[k][s];
	      for(int i=0;i<k;i++) {
	    	  p[i] = model.addVars(s, GRB.CONTINUOUS);
	      }
	      for(int i=0; i<k;i++) {
	    	  for(int j=0; j<s;j++) {
			      p[i][j].set(GRB.StringAttr.VarName, "p"+i+","+j);
			      if(j>sdk[i] && j<s)
			    	  p[i][j].set(GRB.DoubleAttr.Obj, 1);
			      else
			    	  p[i][j].set(GRB.DoubleAttr.UB, 0);
	    	  }
	      }
	      model.update();
	      
	      td = new GRBVar[k][s];
	      for(int i=0;i<k;i++) {
	    	  td[i] = model.addVars(s, GRB.CONTINUOUS);
	      }
	      for(int i=0; i<k;i++) {
	    	  for(int j=0; j<s;j++) {
			      td[i][j].set(GRB.StringAttr.VarName, "td"+i+","+j);
			      if(j<=sdk[i])
			    	  td[i][j].set(GRB.DoubleAttr.UB, 0);
	    	  }
	      }
	      model.update();
	     
	      h = new GRBVar[k][s];
	      for(int i=0;i<k;i++) {
	    	  h[i] = model.addVars(s, GRB.CONTINUOUS);
	      }
	      for(int i=0; i<k;i++) {
	    	  for(int j=0; j<s;j++) {
			      h[i][j].set(GRB.StringAttr.VarName, "h"+i+","+j);
			      if(j<=sdk[i] || j==s-1)
			    	  h[i][j].set(GRB.DoubleAttr.UB, 0);
			      else
			    	  h[i][j].set(GRB.DoubleAttr.UB, maxHold);
	    	  }
	      }
	      model.update();
	      dwell = new GRBVar[k][s];
	      for(int i=0;i<k;i++) {
	    	  dwell[i] = model.addVars(s, GRB.CONTINUOUS);
	      }
	      for(int i=0; i<k;i++) {
	    	  for(int j=0; j<s;j++) {
	    		  dwell[i][j].set(GRB.StringAttr.VarName, "dwell"+i+","+j);
			      if(j<=sdk[i] || j==s-1)
			    	  dwell[i][j].set(GRB.DoubleAttr.UB, 0);
			      else
			    	  dwell[i][j].set(GRB.DoubleAttr.UB, capacity);
	    	  }
	      }
	      model.update();
	      
	      
	      alight = new GRBVar[k][s];
	      for(int i=0;i<k;i++) {
	    	  alight[i] = model.addVars(s, GRB.CONTINUOUS);
	      }
	      model.update();
	      
	      z = new GRBVar[k][s];

	      for(int i=0;i<k;++i) {
	    	  z[i] = model.addVars(s, GRB.CONTINUOUS);
	      }
	      for(int i=0; i<k;i++) {
	    	  for(int j=0; j<s;j++) {
	    		  z[i][j].set(GRB.StringAttr.VarName, "z"+i+","+j);
			      if(j<=sdk[i] || j==s-1)
			    	  z[i][j].set(GRB.DoubleAttr.UB, 0);
	    	  }
	      }
	      model.update();
	      
	      v1 = new GRBVar[k][s];

	      for(int i=0;i<k;i++) {
	    	  v1[i] = model.addVars(s, GRB.CONTINUOUS);
	      }
	      for(int i=0; i<k;i++) {
	    	  for(int j=0; j<s;j++) {
	    		  v1[i][j].set(GRB.StringAttr.VarName, "v1"+i+","+j);
			      if(j<=sdk[i] || j==s-1)
			    	  v1[i][j].set(GRB.DoubleAttr.UB, 0);
	    	  }
	      }
	      v2 = new GRBVar[k][s];

	      for(int i=0;i<k;i++) {
	    	  v2[i] = model.addVars(s, GRB.CONTINUOUS);
	      }
	      for(int i=0; i<k;i++) {
	    	  for(int j=0; j<s;j++) {
	    		  v2[i][j].set(GRB.StringAttr.VarName, "v2"+i+","+j);
			      if(j<=sdk[i] || j==s-1)
			    	  v2[i][j].set(GRB.DoubleAttr.UB, 0);
	    	  }
	      }
	      model.update();
	      // Objective Function
	      model.set(GRB.IntAttr.ModelSense,1);

	      model.update();
	      // Constraints
	      //Constraint 1: Penalty for bunching
	      for(int i=0; i<k-1;i++) {
	    	  for(int j=(int)sdk[i]+1; j<s-1;j++) {
			      GRBLinExpr expr = new GRBLinExpr();
			      expr.addTerm(1.0, td[i][j]); expr.addTerm(-1.0, td[i+1][j]); expr.addConstant(headways*buffer);
			      //System.out.println("(C1:"+i+","+j+") "+td[i][j]+" - "+td[i+1][j]+" "+headways*buffer+" <= "+p[i][j]);
			      model.addConstr(expr, GRB.LESS_EQUAL, p[i][j], "C1:"+i+","+j);
	    	  }
	      }
	      //Constraint 2: Penalty for tardiness
	      int MtsPorMinuto= 600;  //Metros por minuto
	      for(int i=0; i<k;i++) {
	    	  for(int j=(int) sdk[i]+2; j<s-1;j++) {
			      GRBLinExpr expr = new GRBLinExpr();
			      expr.addTerm(1.0, td[i][j-1]); expr.addConstant(dist[j]/MtsPorMinuto); expr.addTerm(dwellPer,dwell[i][j]);
			      expr.addTerm(alightPer, alight[i][j]); expr.addTerm(1, h[i][j]); expr.addConstant(doors);
			      model.addConstr(td[i][j], GRB.EQUAL, expr, "C2:"+i+","+j);
	    	  }
	      }
	      //Constraint 3: Penalty for tardiness at t0
	      for(int i=0; i<k;i++) {
	    	  int j = (int)sdk[i]+1;
  		  if(j<s-1) {
  			  GRBLinExpr expr = new GRBLinExpr();
			      expr.addConstant((distDepot[j]-positionBuses[i])/MtsPorMinuto); expr.addConstant(distDepot[j-1]/MtsPorMinuto);
			      expr.addTerm(dwellPer,dwell[i][j]);expr.addTerm(alightPer, alight[i][j]); expr.addTerm(1, h[i][j]); expr.addConstant(doors);
			      model.addConstr(td[i][j], GRB.EQUAL, expr, "C3:"+i+","+j);
  		  }
	    	  
	      }
	      //Constraint 4: Max hold in stop
	      for(int i=0; i<k;i++) {
	    	  for(int j=(int)sdk[i]+1; j<s;j++) {
			      GRBLinExpr expr = new GRBLinExpr();
			      expr.addTerm(1,h[i][j]); expr.addTerm(dwellPer, dwell[i][j]);expr.addTerm(alightPer, alight[i][j]);expr.addConstant(doors);
			     
			      model.addConstr(expr, GRB.LESS_EQUAL, maxHold, "C4:"+i+","+j);
	    	  }
	      }
	      //Constraint 5: Number of passengers for k in s
	      for(int i=0; i<k;i++) {
	    	  for(int j=(int)sdk[i]+1; j<s-1;j++) {
	    		  GRBLinExpr suma = new GRBLinExpr();
			      GRBLinExpr expr = new GRBLinExpr();
			      if(i>0) {
			    	  for(int ii=i-1;ii>=0;ii--) {
			    		  if(sdk[ii]+1>=sdk[i]+1 && sdk[ii]+1<j+1)
			    			  suma.addTerm(-1, dwell[ii][j]);
			    	  }
			      }
			      expr.addConstant(passengersLeft[j]);expr.addConstant(lambdaArrive[j]);expr.addConstant((distDepot[j]-positionBuses[i])/MtsPorMinuto);
			      expr.add(suma);
			      model.addConstr(z[i][j], GRB.EQUAL, expr, "C5:"+i+","+j);
	    	  }
	      }
	      //Constraint 6: Dwell (passengers aboarding k in s)

	      for(int i=0; i<k;i++) {
	    	  for(int j=(int)sdk[i]+1; j<s-1;j++) {
	    		  GRBLinExpr sumaX = new GRBLinExpr();
	    		  GRBLinExpr sumaY = new GRBLinExpr();
	    		  GRBLinExpr sumaXN = new GRBLinExpr();
	    		  GRBLinExpr sumaYN = new GRBLinExpr();
			      GRBLinExpr expr1 = new GRBLinExpr();
			      GRBLinExpr expr2 = new GRBLinExpr();
			      GRBLinExpr expr3 = new GRBLinExpr();
			      GRBLinExpr expr4 = new GRBLinExpr();
			      GRBLinExpr expr5 = new GRBLinExpr();
			      GRBLinExpr expr11 = new GRBLinExpr();
			      GRBLinExpr expr22 = new GRBLinExpr();
			      //GRBLinExpr expr33 = new GRBLinExpr();  == 1
			      GRBLinExpr expr44 = new GRBLinExpr();
			      GRBLinExpr expr55 = new GRBLinExpr();
			      for(int jj=(int)sdk[i]+1;jj<j;jj++) {
			    	  sumaX.addTerm(1, dwell[i][jj]);
			    	  sumaXN.addTerm(-1, dwell[i][jj]);
			      }
			      for(int jj=(int)sdk[i]+1;jj<=j;jj++) {
			    	  sumaY.addTerm(1, alight[i][jj]);
			    	  sumaYN.addTerm(-1, alight[i][jj]);
			      }
			      expr1.addTerm(2*turn, v1[i][j]); expr1.addConstant((-2)*turn);
			      expr11.addConstant(capacity);expr11.addConstant((-1)*passengers[i]);expr11.add(sumaY);expr11.add(sumaXN);
			      expr11.addTerm(-1, z[i][j]); expr11.add(sumaX);

			      expr2.addTerm(2*turn, v2[i][j]); expr2.addConstant((-2)*turn);
			      expr22.add(sumaXN); expr22.addTerm(1, z[i][j]); expr22.addConstant((-1)*capacity);
			      expr22.addConstant(passengers[i]); expr22.add(sumaY); expr22.add(sumaX);
			      
			      expr3.addTerm(1, v1[i][j]); expr3.addTerm(1, v2[i][j]);
			      
			      expr4.addTerm(1,dwell[i][j]); 
			      expr44.addTerm(10000, v2[i][j]); expr44.addConstant(-10000);
			      expr44.addConstant(capacity); expr44.addConstant((-1)*passengers[i]);
			      expr44.add(sumaY); expr44.add(sumaXN);
			      
			      expr5.addTerm(1, dwell[i][j]);
			      expr55.addTerm(1000,v1[i][j]); expr55.addConstant((-1)*1000); expr55.addTerm(1, z[i][j]);
			      expr55.add(sumaXN);
			      
			      model.addConstr(expr1, GRB.LESS_EQUAL, expr11, "C61:"+i+","+j);
			      model.addConstr(expr2, GRB.LESS_EQUAL, expr22, "C62:"+i+","+j);
			      model.addConstr(expr3, GRB.EQUAL, 1, "C63:"+i+","+j);
			      model.addConstr(expr4, GRB.GREATER_EQUAL, expr44, "C64:"+i+","+j);
			      model.addConstr(expr5, GRB.GREATER_EQUAL, expr55, "C65:"+i+","+j);
	    	  }
	      }
	      //Constraint 7: Alight in stop S
	      for(int i=0; i<k;i++) {
	    	  for(int j=(int)sdk[i]+1; j<s;j++) {
	    		  GRBLinExpr suma = new GRBLinExpr();
	    		  for(int jj=(int)sdk[i]+1;jj<j;jj++) {
	    			  suma.addTerm(lambdaDescend[j], dwell[i][jj]);
	    			  suma.addTerm(-1*lambdaDescend[j], alight[i][jj]);
	    		  }
			      GRBLinExpr exprRS = new GRBLinExpr();
			      GRBLinExpr exprLS = new GRBLinExpr();
			      
			      exprLS.addTerm(1, alight[i][j]);
			      exprRS.addConstant(passengers[i]*lambdaDescend[j]); exprRS.add(suma);
			     
			      model.addConstr(exprLS, GRB.EQUAL, exprRS, "C4:"+i+","+j);
	    	  }
	      }
	      //Constraint 8: Buses can't overtake
	      for(int i=1; i<k;i++) {
	    	  for(int j=(int)sdk[i-1]; j<s-1;j++) {
			      GRBLinExpr exprRS = new GRBLinExpr();
			      GRBLinExpr exprLS = new GRBLinExpr();

			      exprLS.addTerm(1, td[i][j]);
			      exprRS.addTerm(1, td[i-1][j]);
			     
			      model.addConstr(exprLS, GRB.GREATER_EQUAL, exprRS, "C8:"+i+","+j);
	    	  }
	      }

	      // Optimize model
	      model.getEnv().set(GRB.DoubleParam.TimeLimit,1000);
	      model.getEnv().set(GRB.DoubleParam.MIPGap,0.001);
		this.model = model;
	}
	public void optimize() throws GRBException{
		this.model.optimize();
	}
	public int[][] getValues() throws GRBException{
		int[][] holds = new int[mi.listBuses.size()][mi.listStops.size()];
	      if(model.get(GRB.IntAttr.Status) == GRB.OPTIMAL) {
		      for(int i=0;i<k;++i) {
		    	  for(int j=(int)sdk[i]+1;j<s-1;j++) {
		    		  holds[i][j] = (int) Math.round(h[i][j].get(GRB.DoubleAttr.X));
		    		  //System.out.println("h["+i+"]["+j+"]: "+h[i][j].get(GRB.DoubleAttr.X));
		    		  //System.out.println("alight: "+alightPer*alight[i][j].get(GRB.DoubleAttr.X));
		    		  //System.out.println("dwell: "+dwellPer*dwell[i][j].get(GRB.DoubleAttr.X));
		    		  //System.out.println("");
		    	  }
		      }
		      return holds;
	      }else {
	    	  //System.out.println("Infeasible model");
	    	  model.computeIIS();
	    	  GRBConstr[] constr = model.getConstrs();
	    	  //System.out.println("Couldn't satisfy the following constraint(s):");
	    	  for(int i=0;i<model.get(GRB.IntAttr.NumConstrs);i++) {

	    		  if(constr[i].get(GRB.IntAttr.IISConstr) == 1) {
	    			  //System.out.println(constr[i].get(GRB.StringAttr.ConstrName));
	    		  }
	    	  }
	    	  return holds;
	      }
	
	}
}
