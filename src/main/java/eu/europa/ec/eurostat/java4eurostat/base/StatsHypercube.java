/**
 * 
 */
package eu.europa.ec.eurostat.java4eurostat.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

import eu.europa.ec.eurostat.java4eurostat.base.Selection.Criteria;
import eu.europa.ec.eurostat.java4eurostat.base.Selection.DimValueDifferentFrom;
import eu.europa.ec.eurostat.java4eurostat.base.Selection.DimValueEqualTo;
import eu.europa.ec.eurostat.java4eurostat.base.Selection.DimValueGreaterOrEqualThan;
import eu.europa.ec.eurostat.java4eurostat.base.Selection.DimValueGreaterThan;
import eu.europa.ec.eurostat.java4eurostat.base.Selection.DimValueLowerOrEqualThan;
import eu.europa.ec.eurostat.java4eurostat.base.Selection.DimValueLowerThan;
import eu.europa.ec.eurostat.java4eurostat.base.Selection.ValueDifferentFrom;
import eu.europa.ec.eurostat.java4eurostat.base.Selection.ValueEqualTo;
import eu.europa.ec.eurostat.java4eurostat.base.Selection.ValueGreaterOrEqualThan;
import eu.europa.ec.eurostat.java4eurostat.base.Selection.ValueGreaterThan;
import eu.europa.ec.eurostat.java4eurostat.base.Selection.ValueLowerOrEqualThan;
import eu.europa.ec.eurostat.java4eurostat.base.Selection.ValueLowerThan;
import eu.europa.ec.eurostat.java4eurostat.util.StatsUtil;

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
	public String[] getDimLabels(){ return dimLabels.toArray(new String[dimLabels.size()]); }

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
	public StatsHypercube selectDimValueEqualTo(String... dimLabelValues){ return select(new DimValueEqualTo(dimLabelValues)); }
	public StatsHypercube selectDimValueDifferentFrom(String... dimLabelValues){ return select(new DimValueDifferentFrom(dimLabelValues)); }
	public StatsHypercube selectDimValueGreaterThan(String dimLabel, double value){ return select(new DimValueGreaterThan(dimLabel, value)); }
	public StatsHypercube selectDimValueLowerThan(String dimLabel, double value){ return select(new DimValueLowerThan(dimLabel, value)); }
	public StatsHypercube selectDimValueGreaterOrEqualThan(String dimLabel, double value){ return select(new DimValueGreaterOrEqualThan(dimLabel, value)); }
	public StatsHypercube selectDimValueLowerOrEqualThan(String dimLabel, double value){ return select(new DimValueLowerOrEqualThan(dimLabel, value)); }

	/**
	 * Selection on values.
	 * Ex: stat.value greater than 50
	 */
	public StatsHypercube selectValueEqualTo(double value){ return select(new ValueEqualTo(value)); }
	public StatsHypercube selectValueDifferentFrom(double value){ return select(new ValueDifferentFrom(value)); }
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
	 * Remove dimensions with no or a single value.
	 * @return
	 */
	public StatsHypercube shrinkDims() {
		Collection<String> toDelete = new ArrayList<String>();
		for(String dimLabel : this.dimLabels) if(getDimValues(dimLabel).size() <= 1) toDelete.add(dimLabel);
		for(String dimLabel : toDelete) this.delete(dimLabel);
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

	//TODO change dim value



	//operations
	public StatsHypercube apply(UnaryOperator<Double> op) {
		for(Stat s : stats) s.value = op.apply(s.value);
		return this;
	}

	public StatsHypercube abs(UnaryOperator<Double> op) {
		return apply(new UnaryOperator<Double>() {
			@Override
			public Double apply(Double val) { return Math.abs(val); }});
	}
	public StatsHypercube mult(double valueToMult){
		return apply(new UnaryOperator<Double>() {
			@Override
			public Double apply(Double val) {
				return val * valueToMult;
			}});
	}
	public StatsHypercube sum(double valueToSum){
		return apply(new UnaryOperator<Double>() {
			@Override
			public Double apply(Double val) {
				return val + valueToSum;
			}});
	}
	public StatsHypercube pow(double exp){
		return apply(new UnaryOperator<Double>() {
			@Override
			public Double apply(Double val) {
				return Math.pow(val, exp);
			}});
	}
	public StatsHypercube inv(UnaryOperator<Double> op) { return pow(-1); }
	public StatsHypercube sqrt(UnaryOperator<Double> op) { return pow(0.5); }
	public StatsHypercube diff(double valueToDiff){ return sum(-valueToDiff); }
	public StatsHypercube div(StatsHypercube hc, double valueToDiv){ return mult(1/valueToDiv); }
	public StatsHypercube opp(){ return mult(-1); }


	public StatsHypercube apply(BinaryOperator<Double> op, StatsHypercube hc) {
		String[] dimLabels = getDimLabels();
		StatsIndex hcI = new StatsIndex(hc, dimLabels);
		for(Stat s : stats){
			//get stat value
			double val1 = s.value;
			if(Double.isNaN(val1)) continue;

			//retrieve other value
			//TODO
			String[] dimValues = new String[dimLabels.length];
			for(int i=0; i<dimLabels.length; i++) dimValues[i] = s.dims.get(dimLabels[i]);
			double val2 = hcI.getSingleValue(dimValues);

			//compute and apply new figure
			s.value = op.apply(val1, val2);
		}
		return this;
	}

	public StatsHypercube sum(StatsHypercube hc){
		return apply(new BinaryOperator<Double>() {
			@Override
			public Double apply(Double val1, Double val2) {
				return val1+val2;
			}}, hc);
	}
	public StatsHypercube diff(StatsHypercube hc){
		return apply(new BinaryOperator<Double>() {
			@Override
			public Double apply(Double val1, Double val2) {
				return val1-val2;
			}}, hc);
	}
	public StatsHypercube div(StatsHypercube hc){
		return apply(new BinaryOperator<Double>() {
			@Override
			public Double apply(Double val1, Double val2) {
				return val1/val2;
			}}, hc);
	}
	public StatsHypercube mult(StatsHypercube hc){
		return apply(new BinaryOperator<Double>() {
			@Override
			public Double apply(Double val1, Double val2) {
				return val1*val2;
			}}, hc);
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
	 * Print basic statistics from the hypercube values
	 */
	public void printBasicStats() {
		Collection<Double> vals = new HashSet<Double>();
		for(Stat s : this.stats) if(!Double.isNaN(s.value)) vals.add(s.value);
		StatsUtil.printStats(vals);
	}

	public double[] getQuantiles(int nb) {
		Collection<Double> vals = new HashSet<Double>();
		for(Stat s : this.stats) if(!Double.isNaN(s.value)) vals.add(s.value);
		return StatsUtil.getQuantiles(vals, nb);
	}

	public void printQuantiles(int nb) {
		Collection<Double> vals = new HashSet<Double>();
		for(Stat s : this.stats) if(!Double.isNaN(s.value)) vals.add(s.value);
		StatsUtil.printQuantiles(vals, nb);
	}

	/**
	 * Transform an hypercube with only one dimension into a hashmap.
	 * @return 
	 */
	public HashMap<String, Double> toMap(){
		HashMap<String, Double> map = new HashMap<String, Double>();
		String dimLabel = dimLabels.iterator().next();
		for(Stat s : stats) map.put(s.dims.get(dimLabel), s.value);
		return map;
	}

}
