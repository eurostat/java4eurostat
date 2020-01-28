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
	//private final static Logger LOGGER = LogManager.getLogger(Compacity.class.getName());

	/**
	 * Compute the number of possible positions in the hypercube.
	 * This is simply the product of the sizes of all dimensions.
	 * @param hc The input hypercube.
	 * @return the number of possible positions in the hypercube.
	 */
	public static int getMaxSize(StatsHypercube hc) {
		if(hc.dimLabels.size()==0) return 0;
		int size = 1;
		for(String dimLabel : hc.dimLabels)
			size *= hc.getDimValues(dimLabel).size();
		return size;
	}

	/**
	 * Give an overview of how full/sparse/empty is the hypercube.
	 * 0: not compact (almost empty). 1: very compact (all values provided)
	 * NB: The value could be greater than one if several values are provided for some positions. Check the unicity for that.
	 * @param hc The input hypercube.
	 * @param ignoreNaNValues Ignore NaN values.
	 * @param ignoreNullValues Ignore null values.
	 * @return the compacity indicator.
	 */
	public static double getCompacityIndicator(StatsHypercube hc, boolean ignoreNaNValues, boolean ignoreNullValues) {
		StatsHypercube hc_ = hc;
		if(ignoreNaNValues) hc_ = hc_.selectValueDifferentFrom(Double.NaN);
		if(ignoreNullValues) hc_ = hc_.selectValueDifferentFrom(0);
		return (1.0 * hc_.stats.size()) / (1.0 * getMaxSize(hc_));
	}


	//TODO write tests
	//TODO analyse compacity per dimension/value.
	//TODO make compact - by adding missing values
	//TODO clean - remove overlaps

	/*
	public static void main(String[] args) {
		String path = "./src/test/resources/";
		StatsHypercube hc = CSV.load(path+"ex.csv", "population");
		StatsHypercube hcNc = CSV.load(path+"ex_non_compact.csv", "population");
		StatsHypercube hcOv = CSV.load(path+"ex_overlap.csv", "population");
		StatsHypercube hcDirty = CSV.load(path+"ex_dirty.csv", "population");
		//hcOv.printInfo();

		//System.out.println(hc.dimLabels.size()); //3
		//System.out.println(hc.stats.size()); //12
		//System.out.println(hc.getDimLabels().length); //3
		//System.out.println(hc.getDimValues("country").size()); //2
		//System.out.println(hc.getDimValues("gender").size()); //3
		//System.out.println(hc.getDimValues("year").size()); //2
		//double[] qt = hc.getQuantiles(3); //[47.85, 119.5, 148.15]
		//for(double q : qt) System.out.println(" "+q);

		//System.out.println(getMaxSize(hc)); //12
		//System.out.println(getCompacityIndicator(hc, false, false)); //1.0
		//System.out.println(getMaxSize(hcNc)); //12
		//System.out.println(getCompacityIndicator(hcNc, false, false)); //0.75
		//System.out.println(getMaxSize(hcOv)); //12
		//System.out.println(getCompacityIndicator(hcOv, false, false)); //1.1666666666
		//System.out.println(getMaxSize(hcDirty)); //27
		//System.out.println(getCompacityIndicator(hcDirty, false, false)); //0.48148148148148145

		//System.out.println(checkUnicity(hc)); //0
		//System.out.println(checkUnicity(hcNc)); //0
		//System.out.println(checkUnicity(hcOv)); //2+2
		//System.out.println(checkUnicity(hcDirty)); //2+3
	}
	 */
}
