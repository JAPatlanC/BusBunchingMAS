package model;
// TODO: Auto-generated Javadoc

/**
 * Description: Model of buses of the MAS
 * 
 */
public class Bus {
	
	/** The Bus identifier. */
	public int id;
	
	/** The capacity. */
	public int capacity;
	
	/** The passengers onboard. */
	public int passengers;
	
	/**  If the bus is already in the system. */
	public boolean isActive;
	
	/** The Position of the bus in the route. */
	public double position;
	
	/** The previous stop. */
	public Stop previousStop;
	
	/** The Distance between the previous bus. */
	public float distPreviousBus;
	
	/** The Distance between the next bus. */
	public float distNextBus;
	
	/** The next bus. */
	public Bus nextBus;
	
	/** The previous bus. */
	public Bus previousBus;

	/** If the bus is on any stop. */
	public boolean isOnStop;
	
	/** If the bus is on descend state. */
	public boolean isOnDescendState;
	
	/** If the bus is on aboard state. */
	public boolean isOnAboardState;
	
	/** If the bus is holding on the current stop. */
	public boolean isBusHolding;
	
	/** If the bus is at the end of the route. */
	public boolean isAtEndRouteState;
	
	/** The Holding time of the current bus. */
	public int h;
	
	/** If the bus agent already specified an action **/
	public boolean isReady;

	/** The bus must skip the next stop. */
	public boolean mustSkipStop;

	/** The speed of the bus. */
	public double speed;
	
	/**
	 * Instantiates a new bus.
	 *
	 * @param id the id
	 */
	public Bus(int id) {
		this.id = id;
		this.isBusHolding=false;
		isReady=true;
		speed=0;
	}
	
	/**
	 * The status of the bus.
	 *
	 * @return the string
	 */
	public String status() {
		if(this.isAtEndRouteState)
			return "EndRouteState";
		if(!this.isActive)
			return "InactiveState";
		if(this.isBusHolding)
			return "BusHoldingState";
		if(this.isOnAboardState) 
			return "AboardState";
		if(this.isOnDescendState)
			return "DescendState";
		return "OnRouteState";
	}
	
	/**
	 * To string.
	 *
	 * @return the string
	 */
	@Override
	public String toString() {
		String previousStopId="NA";
		if(this.isBusHolding)
			return "********BusHolding******";
		if(previousStop != null)
			previousStopId = String.valueOf(previousStop.id);
		return "Bus [id="+id+", capacity=" + capacity + ", passengers=" + passengers + ", position=" + position + ", previousStop="
				+ previousStopId + ", h="+h+", status= "+status()+"]";
	}
	
	
}
