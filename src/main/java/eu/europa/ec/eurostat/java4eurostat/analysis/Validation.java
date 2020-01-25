/**
 * 
 */
package eu.europa.ec.eurostat.java4eurostat.analysis;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import eu.europa.ec.eurostat.java4eurostat.base.Stat;
import eu.europa.ec.eurostat.java4eurostat.base.StatsHypercube;

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

	
	//TODO check unicity
	//check that for each combination of dimension values, a single stat value is present

}
