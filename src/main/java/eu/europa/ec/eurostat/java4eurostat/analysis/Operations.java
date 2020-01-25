/**
 * 
 */
package eu.europa.ec.eurostat.java4eurostat.analysis;

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

	public static StatsHypercube computeDifference(StatsHypercube hc1, StatsHypercube hc2){ return computeDifference(hc1, hc2, false, false); }
	/**
	 * Compute difference of two stat datasets. It is assumed both have the same structure.
	 * 
	 * @param hc1 The first stat dataset
	 * @param hc2 The second stat dataset
	 * @param abs Compute absolute value of the difference
	 * @param ratio Compute percentage difference
	 * @return
	 */
	public static StatsHypercube computeDifference(StatsHypercube hc1, StatsHypercube hc2, boolean abs, boolean ratio){
		//TODO simplify it - extract functions

		String[] dimLabels = hc1.getDimLabels();
		StatsHypercube out = new StatsHypercube(dimLabels);
		StatsIndex hcI2 = new StatsIndex(hc2, dimLabels);
		for(Stat s : hc1.stats){
			//get stat value
			double val = s.value;
			if(Double.isNaN(val)) continue;

			//retrieve value to compare with
			String[] dimValues = new String[dimLabels.length];
			for(int i=0; i<dimLabels.length; i++) dimValues[i] = s.dims.get(dimLabels[i]);
			double valVal = hcI2.getSingleValue(dimValues);
			if(Double.isNaN(valVal)) continue;

			//compute comparison figure
			double compVal = valVal - val;
			if(ratio) compVal*=100/val;
			if(abs) compVal=Math.abs(compVal);

			//store comparison figures
			Stat sc = new Stat(compVal);
			for(int i=0; i<dimLabels.length; i++) sc.dims.put(dimLabels[i], s.dims.get(dimLabels[i]));
			out.stats.add(sc);
		}
		return out;
	}

	
	//TODO do sum
	//TODO do division
	//TODO do multi
	
}
