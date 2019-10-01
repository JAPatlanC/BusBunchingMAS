package model;

import java.util.ArrayList;
import java.util.List;

/*
 * Classname: ModelInstance
 * Description: Instance of the model, represented by a snapshot of the environment. 
 * Author: Jesús Angel Patlán Castillo
 * Changelog:
 * Date		 *********** Description
 * 26/08/2019			First Version
 * 
 */
public class ModelInstance {
	
	public List<Bus> listBuses; //Buses of MAS
	public List<Stop> listStops; //Stops of MAS
	public float maxHold; //Maximum holding time
	public float dwellPer; //Time for a passenger to descend from the bus
	public float alightPer; //Time for a passenger to get inside the buss
	public float t0; //Instance initial time
	public int doors; //Time for the doors to open/close
	public float headways; //Distances between buses
	public float turn; //Total route length
	
	public ModelInstance() {
		listBuses = new ArrayList<Bus>();
		listStops = new ArrayList<Stop>();
	}

	@Override
	public String toString() {
		System.out.println("***ModelInstance BEGIN***");
		System.out.println("[Instance time=" + t0 + ", maxHold=" + maxHold + ", dwellPer=" + dwellPer + ", alightPer=" 
		+ alightPer + ", doors=" + doors + ", headways=" + headways + ", turn=" + turn + "]");
		System.out.println("---Stops ("+listStops.size()+")---");
		for(Stop s : listStops) {
			System.out.println(s);
		}
		System.out.println("---Buses ("+listBuses.size()+")---");
		for(Bus b : listBuses) {
			System.out.println(b);
		}
		return "***ModelInstance END***";
	}

	
}
