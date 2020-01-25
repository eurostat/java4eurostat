/**
 * 
 */
package eu.europa.ec.eurostat.java4eurostat.analysis;

import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

import eu.europa.ec.eurostat.java4eurostat.base.Stat;
import eu.europa.ec.eurostat.java4eurostat.base.StatsHypercube;
import eu.europa.ec.eurostat.java4eurostat.base.StatsIndex;

/**
 * Operations hypercube structures
 * 
 * @author julien Gaffuri
 *
 */
public class Operations {

	/**
	 * Compute a unary operation.
	 * 
	 * @param hc
	 * @param uop
	 * @return
	 */
	public static StatsHypercube compute(StatsHypercube hc, UnaryOperator<Double> uop){
		String[] dimLabels = hc.getDimLabels();
		StatsHypercube out = new StatsHypercube(dimLabels);
		for(Stat s : hc.stats){
			//get stat value
			double val = s.value;
			if(Double.isNaN(val)) continue;

			//compute comparison figure
			double outVal = uop.apply(val);
			if(Double.isNaN(val)) continue;

			//store comparison figures
			Stat sc = new Stat(outVal);
			for(int i=0; i<dimLabels.length; i++) sc.dims.put(dimLabels[i], s.dims.get(dimLabels[i]));
			out.stats.add(sc);
		}
		return out;
	}

	/**
	 * Compute a binary operation.
	 * 
	 * @param hc1
	 * @param hc2
	 * @param bop
	 * @return
	 */
	public static StatsHypercube compute(StatsHypercube hc1, StatsHypercube hc2, BinaryOperator<Double> bop){
		String[] dimLabels = hc1.getDimLabels();
		StatsHypercube out = new StatsHypercube(dimLabels);
		StatsIndex hcI2 = new StatsIndex(hc2, dimLabels);
		for(Stat s : hc1.stats){
			//get stat value
			double val1 = s.value;
			if(Double.isNaN(val1)) continue;

			//retrieve value to compare with
			String[] dimValues = new String[dimLabels.length];
			for(int i=0; i<dimLabels.length; i++) dimValues[i] = s.dims.get(dimLabels[i]);
			double val2 = hcI2.getSingleValue(dimValues);
			if(Double.isNaN(val2)) continue;

			//compute comparison figure
			double outVal = bop.apply(val1, val2);
			if(Double.isNaN(outVal)) continue;

			//store comparison figures
			Stat sc = new Stat(outVal);
			for(int i=0; i<dimLabels.length; i++) sc.dims.put(dimLabels[i], s.dims.get(dimLabels[i]));
			out.stats.add(sc);
		}
		return out;
	}


	/**
	 * Compute the total of all values along a dimension
	 * 
	 * @param hc
	 * @param dim
	 * @param totalDimValue
	 */
	public void computeTotalDim(StatsHypercube hc, String dim, String totalDimValue) {
		//TODO index along the other dimensions
		//TODO go accros all other dimensions
		//TODO compute total
		//TODO create new stat with total value
	}

	//TODO copy

}
