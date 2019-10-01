package model;
/*
 * Classname: Bus
 * Description: Model of buses of the MAS 
 * Author: Jesús Angel Patlán Castillo
 * Changelog:
 * Date		 *********** Description
 * 26/08/2019			First Version
 * 
 */
public class Bus {
	public int id; //Bus identifier
	public int capacity; //For the model, it's an homogeneous capacity
	public int passengers; // Passengers onboard
	public float position; // Position of the bus in the route
	public Stop previousStop; // Previous stop
	public float distPreviousBus; // Distance between the previous bus
	public float distNextBus; // Distance between the next bus
	public Bus nextBus;
	public Bus previousBus;

	public Bus(int id) {
		this.id = id;
	}
	
	@Override
	public String toString() {
		return "Bus [capacity=" + capacity + ", passengers=" + passengers + ", position=" + position + ", previousStop="
				+ previousStop.id + "]";
	}
	
	
}
