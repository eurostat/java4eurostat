/**
 * 
 */
package eu.europa.ec.eurostat.java4eurostat.analysis;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

import eu.europa.ec.eurostat.java4eurostat.base.Stat;
import eu.europa.ec.eurostat.java4eurostat.base.StatsHypercube;
import eu.europa.ec.eurostat.java4eurostat.base.StatsIndex;

/**
 * @author julien Gaffuri
 *
 */
public class Validation {

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
		HashMap<String,Integer> unexpectedValues = new HashMap<>();

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
	 * Check that there is a unique value for each position in the hypercube.
	 * 
	 * @param hc
	 * @return
	 */
	public static HashMap<String,Integer> checkUnicity(StatsHypercube hc) {
		return checkUnicity( new StatsIndex(hc, hc.getDimLabels()));
	}

	/**
	 * Check that there is a unique value for each position in the hypercube.
	 * 
	 * @param index
	 * @return
	 */
	public static HashMap<String,Integer> checkUnicity(StatsIndex index) {
		HashMap<String, Integer> out = new HashMap<>();
		Set<String> keys = index.getKeys();
		if(keys == null || keys.size() == 0) {
			Collection<Stat> vals = index.getCollection();
			if(vals.size() == 1) return out;
			out.put(vals.iterator().next().dims.toString(), vals.size());
			return out;
		}
		//recursive call
		for(String key : keys)
			out.putAll( checkUnicity(index.getSubIndex(key)) );
		return out;
	}

}
