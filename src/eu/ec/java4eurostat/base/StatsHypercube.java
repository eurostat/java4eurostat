/**
 * 
 */
package eu.ec.java4eurostat.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

/**
 * An hypercube of statistical values.
 * 
 * @author julien Gaffuri
 *
 */
public class StatsHypercube {
	/**
	 * The statistical values.
	 */
	public Collection<Stat> stats;

	/**
	 * The dimension labels.
	 * Ex: gender,time,country
	 */
	public Collection<String> dimLabels;


	public StatsHypercube(String... dimLabels){
		this();
		for(String dimLabel : dimLabels) this.dimLabels.add(dimLabel);
	}

	public StatsHypercube(){
		this(new HashSet<Stat>(), new HashSet<String>());
	}

	private StatsHypercube(Collection<Stat> stats, Collection<String> dimLabels){
		this.stats = stats;
		this.dimLabels = dimLabels;
	}



	/**
	 * Return all dimension values for a dimension.
	 * Ex: Return (male,female) from "gender".
	 * 
	 * @param dimLabel
	 * @return
	 */
	public HashSet<String> getDimValues(String dimLabel) {
		HashSet<String> dimValues = new HashSet<String>();
		for(Stat s : stats)
			dimValues.add(s.dims.get(dimLabel));
		return dimValues;
	}

	/**
	 * A selection criteria to decide whether a statistical value should be kept of not.
	 * Ex: All stats with gender=male, with value=0, with country starting with a 'A', etc.
	 * 
	 * @author Julien Gaffuri
	 *
	 */
	public interface StatSelectionCriteria { boolean keep(Stat stat); }

	/**
	 * Extract an hypercube based on a selection criteria
	 * 
	 * @param sel The selection criterion
	 * @return The extracted hypercube
	 */
	public StatsHypercube select(StatSelectionCriteria sel) {
		HashSet<Stat> stats_ = new HashSet<Stat>();
		for(Stat stat : this.stats)
			if(sel.keep(stat)) stats_.add(stat);
		return new StatsHypercube(stats_, new HashSet<String>(this.dimLabels));
	}

	//get all stats having a dim value
	/**
	 * Extract an hypercube of stats having dimLabel=dimValue
	 * Ex: gender=male, country=HU, etc.
	 * 
	 * @param dimLabel
	 * @param dimValue
	 * @return
	 */
	public StatsHypercube select(final String dimLabel, final String dimValue){
		if(!dimLabels.contains(dimLabel)) System.err.println("No dimension label: " + dimLabel);
		return select(new StatSelectionCriteria() {
			@Override
			public boolean keep(Stat stat) {
				return dimValue.equals( stat.dims.get(dimLabel) );
			}
		});
	}

	/**
	 * Delete a dimension.
	 * This should be used, for example, to free memory when all stats have a unique dimension value.
	 * 
	 * @param dimLabel
	 */
	public void delete(String dimLabel){
		for(Stat s:stats){
			String out = s.dims.remove(dimLabel);
			if(out==null)
				System.err.println("Error: dimension "+dimLabel+" not defined for "+s);
		}
		dimLabels.remove(dimLabel);
	}

	/**
	 * Delete all stats having a given value for a dimension
	 * 
	 * @param dimLabel
	 * @param dimValue
	 */
	public void delete(String dimLabel, String dimValue){
		stats.removeAll( select(dimLabel, dimValue).stats );
	}

	/**
	 * Delete the stats having a label value with a given length
	 * 
	 * @param dimLabel
	 * @param size
	 */
	public void delete(String dimLabel, int size) {
		HashSet<String> values = getDimValues(dimLabel);
		for(String v:values){
			if(v.length() != size) continue;
			delete(dimLabel,v);
		}
	}

	/**
	 * Print hypercube structure.
	 */
	public void printInfo() {
		printInfo(true);
	}
	public void printInfo(boolean printDimValues) {
		System.out.println("Information: "+stats.size()+" value(s) with "+dimLabels.size()+" dimension(s).");
		for(String lbl : dimLabels){
			ArrayList<String> vals = new ArrayList<String>((getDimValues(lbl)));
			System.out.println("   Dimension: "+lbl + " ("+vals.size()+" dimension values)");
			Collections.sort(vals);
			for(String val : vals)
				if(printDimValues) System.out.println("      "+val);
		}
	}

	/**
	 * Check dimension values are within a list of valid values.
	 * 
	 * @param dimLabel
	 * @param expectedDimValues
	 * @param print
	 * @return
	 */
	public HashMap<String,Integer> checkDimensionValuesValidity(String dimLabel, Collection<String> expectedDimValues, boolean print) {
		HashMap<String,Integer> unexpectedValues = new HashMap<String,Integer>();

		//list and count unexpected values
		for(Stat s : stats){
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
}
