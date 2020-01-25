/**
 * 
 */
package eu.europa.ec.eurostat.java4eurostat.analysis;

import eu.europa.ec.eurostat.java4eurostat.base.StatsHypercube;

/**
 * Compute the compacity of the 
 * 
 * @author julien Gaffuri
 *
 */
public class Compacity {

	/**
	 * Compute the number of possible positions in the hypercube.
	 * This is simply the product of the sizes of all dimensions.
	 * @param hc
	 * @return
	 */
	public int getMaxSize(StatsHypercube hc) {
		int size = 1;
		for(String dimLabel : hc.getDimLabels())
			size *= hc.getDimValues(dimLabel).size();
		return size;
	}

	/**
	 * Give an overview of how full/empty is the hypercube.
	 * 0: not compact (almost empty). 1: very compact (all values provided)
	 * NB: The value could be greater than one if several values are provided for some positions. Check the unicity for that.
	 * @param hc
	 * @param ignoreNaNValues
	 * @param ignoreNullValues
	 * @return
	 */
	public double getCompacityIndicator(StatsHypercube hc, boolean ignoreNaNValues, boolean ignoreNullValues) {
		if(ignoreNaNValues) hc = hc.selectValueDifferentFrom(Double.NaN);
		if(ignoreNullValues) hc = hc.selectValueDifferentFrom(0);
		return hc.stats.size() / getMaxSize(hc);
	}


	//TODO make compact
	//by adding missing values


	//TODO overlaps - values present several times
	//use index - count number of elements by leaf; should be 1 !
	public void checkUnicity(StatsHypercube hc) {
		//TODO
	}



	//TODO analyse quantity of data per dimension values

}
