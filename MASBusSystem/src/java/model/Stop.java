package model;

/*
 * Classname: Stop
 * Description: Model of stops of the MAS 
 * Author: Jesús Angel Patlán Castillo
 * Changelog:
 * Date		 *********** Description
 * 26/08/2019			First Version
 * 
 */
public class Stop {
	public int id; //Stop identifier
	public float arriveRate;
	public float descendRate;
	public float distPreviousStop;
	public float distNextStop;
	public Stop previousStop;
	public Stop nextStop;
	public float distDepot;
	public int passengersLeft;
	
	public Stop(int id) {
		this.id = id;
	}
	
	@Override
	public String toString() {
		return "Stop [arriveRate=" + arriveRate + ", descendRate=" + descendRate + ", distPreviousStop="
				+ distPreviousStop + ", distNextStop=" + distNextStop + ", distDepot=" + distDepot + ", passengersLeft=" + passengersLeft + "]";
	}
	
}
