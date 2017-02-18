/**
 * 
 */
package eu.ec.estat.java4eurostat.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import eu.ec.estat.java4eurostat.base.Stat;
import eu.ec.estat.java4eurostat.base.StatsHypercube;
import eu.ec.estat.java4eurostat.util.StatsUtil;

/**
 * @author julien Gaffuri
 *
 */
public class Validation {
	//TODO generic validation library?

	/**
	 * Check dimension values are within a list of valid values.
	 * 
	 * @param hc
	 * @param dimLabel
	 * @param expectedDimValues
	 * @param print
	 * @return
	 */
	public static HashMap<String,Integer> checkDimensionValuesValidity(StatsHypercube hc, String dimLabel, Collection<String> expectedDimValues, boolean print) {
		HashMap<String,Integer> unexpectedValues = new HashMap<String,Integer>();

		//list and count unexpected values
		for(Stat s : hc.stats){
			String DimValue = s.dims.get(dimLabel);
			if(expectedDimValues.contains(DimValue)) continue;
			if(unexpectedValues.get(DimValue)==null)
				unexpectedValues.put(DimValue, 1);
			else
				unexpectedValues.put(DimValue, unexpectedValues.get(DimValue)+1);
		}

		if(print){
			//show result
			System.err.println(unexpectedValues.size()+" unexpected values");
			for(Entry<String,Integer> missing : unexpectedValues.entrySet())
				System.err.println("\t"+missing.getKey()+" (found "+missing.getValue()+" times)");
		}
		return unexpectedValues;
	}


	/**
	 * Print some statistics on an hypercube values
	 * 
	 * @param hc
	 */
	public static void printBasicStatistics(StatsHypercube hc){
		ArrayList<Double> vals = new ArrayList<Double>();
		for(Stat s : hc.stats){
			if(Double.isNaN(s.value)) continue;
			vals.add(s.value);
		}
		double[] vals_ = new double[vals.size()]; for(int i=0; i<vals.size(); i++) vals_[i]=vals.get(i);
		StatsUtil.printStats(vals_);
	}


}
