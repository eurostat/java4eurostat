/**
 * 
 */
package eu.ec.estat.java4eurostat.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import eu.ec.estat.java4eurostat.base.Selection.Criteria;
import eu.ec.estat.java4eurostat.base.Selection.DimValueEqualTo;
import eu.ec.estat.java4eurostat.base.Selection.DimValueGreaterOrEqualThan;
import eu.ec.estat.java4eurostat.base.Selection.DimValueGreaterThan;
import eu.ec.estat.java4eurostat.base.Selection.DimValueLowerOrEqualThan;
import eu.ec.estat.java4eurostat.base.Selection.DimValueLowerThan;
import eu.ec.estat.java4eurostat.base.Selection.ValueEqualTo;
import eu.ec.estat.java4eurostat.base.Selection.ValueGreaterOrEqualThan;
import eu.ec.estat.java4eurostat.base.Selection.ValueGreaterThan;
import eu.ec.estat.java4eurostat.base.Selection.ValueLowerOrEqualThan;
import eu.ec.estat.java4eurostat.base.Selection.ValueLowerThan;

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
	 * Extract an hypercube based on a selection criteria
	 * 
	 * @param sel The selection criterion
	 * @return The extracted hypercube
	 */
	public StatsHypercube select(Criteria sel) {
		HashSet<Stat> stats_ = new HashSet<Stat>();
		for(Stat stat : this.stats)
			if(sel.keep(stat)) stats_.add(stat);
		return new StatsHypercube(stats_, new HashSet<String>(this.dimLabels));
	}

	/**
	 * Extract an hypercube of stats having dimLabel=dimValue
	 * Ex: gender=male, country=HU, etc.
	 * 
	 * @param dimLabelValues List of couples (label,value) Example; "gender","male","country","HU"
	 * @return
	 */
	public StatsHypercube selectDimValueEqualTo(String... dimLabelValues){
		return select(new DimValueEqualTo(dimLabelValues));
	}
	public StatsHypercube selectDimValueGreaterThan(String dimLabel, double value){ return select(new DimValueGreaterThan(dimLabel, value)); }
	public StatsHypercube selectDimValueLowerThan(String dimLabel, double value){ return select(new DimValueLowerThan(dimLabel, value)); }
	public StatsHypercube selectDimValueGreaterOrEqualThan(String dimLabel, double value){ return select(new DimValueGreaterOrEqualThan(dimLabel, value)); }
	public StatsHypercube selectDimValueLowerOrEqualThan(String dimLabel, double value){ return select(new DimValueLowerOrEqualThan(dimLabel, value)); }

	/**
	 * Selection on values.
	 * Ex: stat.value > 50
	 */
	public StatsHypercube selectValueEqualTo(double value){ return select(new ValueEqualTo(value)); }
	public StatsHypercube selectValueGreaterThan(double value){ return select(new ValueGreaterThan(value)); }
	public StatsHypercube selectValueLowerThan(double value){ return select(new ValueLowerThan(value)); }
	public StatsHypercube selectValueGreaterOrEqualThan(double value){ return select(new ValueGreaterOrEqualThan(value)); }
	public StatsHypercube selectValueLowerOrEqualThan(double value){ return select(new ValueLowerOrEqualThan(value)); }

	/**
	 * Delete a dimension.
	 * This should be used, for example, to free memory when all stats have a unique dimension value.
	 * 
	 * @param dimLabel
	 */
	public StatsHypercube delete(String dimLabel){
		for(Stat s:stats){
			String out = s.dims.remove(dimLabel);
			if(out==null)
				System.err.println("Error: dimension "+dimLabel+" not defined for "+s);
		}
		dimLabels.remove(dimLabel);
		return this;
	}

	/**
	 * Delete all stats having a given value for a dimension
	 * 
	 * @param dimLabel
	 * @param dimValue
	 */
	public StatsHypercube delete(String dimLabel, String dimValue){
		stats.removeAll( selectDimValueEqualTo(dimLabel, dimValue).stats );
		return this;
	}

	/**
	 * Delete the stats having a label value with a given length
	 * 
	 * @param dimLabel
	 * @param size
	 */
	public StatsHypercube delete(String dimLabel, int size) {
		HashSet<String> values = getDimValues(dimLabel);
		for(String v:values){
			if(v.length() != size) continue;
			delete(dimLabel,v);
		}
		return this;
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

}
