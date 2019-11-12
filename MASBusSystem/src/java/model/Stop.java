package model;

import org.apache.commons.math3.distribution.PoissonDistribution;

// TODO: Auto-generated Javadoc
/**
 * Description: Model of stops of the MAS 
 * 
 */
public class Stop {
	
	/** The Stop identifier. */
	public int id;
	
	/** The arrive rate. */
	public float arriveRate;
	
	/** The descend rate. */
	public float descendRate;
	
	/** The dist previous stop. */
	public float distPreviousStop;
	
	/** The dist next stop. */
	public float distNextStop;
	
	/** The previous stop. */
	public Stop previousStop;
	
	/** The next stop. */
	public Stop nextStop;
	
	/** The dist depot. */
	public float distDepot;
	
	/** The passengers left. */
	public int passengersLeft;
	
	/** The distribution. */
	private PoissonDistribution distribution;

	/** The is bus on stop. */
	public boolean isBusOnStop;
	
	/**
	 * Instantiates a new stop.
	 *
	 * @param id the id
	 */
	public Stop(int id) {
		this.id = id;
	}
	
	/**
	 * To string.
	 *
	 * @return the string
	 */
	@Override
	public String toString() {
		return "Stop [id="+id+", arriveRate=" + arriveRate + ", descendRate=" + descendRate + ", distPreviousStop="
				+ distPreviousStop + ", distNextStop=" + distNextStop + ", distDepot=" + distDepot + ", passengersLeft=" + passengersLeft + "]";
	}
	
	/**
	 * Simulate arrive.
	 *
	 * @return the int
	 */
	public int simulateArrive() {
		if(distribution==null) {
			distribution = new PoissonDistribution(arriveRate);
		}
		int sample =distribution.sample();
		passengersLeft += sample;
		return sample;
	}
	
}
