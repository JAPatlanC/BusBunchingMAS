package utils;

import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class Utils.
 */
public class Utils {

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
		double variance = sq_sum / inputArray.size() - mean * mean;
		return Math.sqrt(variance);
	}
}
