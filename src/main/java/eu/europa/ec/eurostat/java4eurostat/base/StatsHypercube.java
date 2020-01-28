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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	public final static Logger LOGGER = LogManager.getLogger(StatsHypercube.class.getName());

	/**
	 * The statistical values.
	 */
	public Collection<Stat> stats;

	/**
	 * The dimension labels.
	 * Ex: gender,time,country
	 */
	public Collection<String> dimLabels;
	public String[] getDimLabels(){ return this.dimLabels.toArray(new String[this.dimLabels.size()]); }

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
		HashSet<String> dimValues = new HashSet<>();
		for(Stat s : this.stats)
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
		HashSet<Stat> stats_ = new HashSet<>();
		for(Stat stat : this.stats)
			if(sel.keep(stat)) stats_.add(stat);
		return new StatsHypercube(stats_, new HashSet<>(this.dimLabels));
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
		for(Stat s : this.stats){
			String out = s.dims.remove(dimLabel);
			if(out==null)
				LOGGER.error("Error: dimension "+dimLabel+" not defined for "+s);
		}
		this.dimLabels.remove(dimLabel);
		return this;
	}

	/**
	 * Remove dimensions with no or a single value.
	 * @return
	 */
	public StatsHypercube shrinkDims() {
		Collection<String> toDelete = new ArrayList<>();
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
		this.stats.removeAll( selectDimValueEqualTo(dimLabel, dimValue).stats );
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
	 * Change a dimension value.
	 * 
	 * @param dimLabel The dimension of the value
	 * @param dimValueOld The old value, to change
	 * @param dimValueNew The new value, to use
	 */
	public void changeDimValue(String dimLabel, String dimValueOld, String dimValueNew) {
		for(Stat s : this.stats) {
			String dv = s.dims.get(dimLabel);
			if( dimValueOld.equals(dv) ) s.dims.put(dimLabel, dimValueNew);
		}
	}


	/**
	 * Apply an operation on the values.
	 * 
	 * @param op
	 * @return the object itself
	 */
	public StatsHypercube apply(UnaryOperator<Double> op) {
		for(Stat s : this.stats) s.value = op.apply(new Double(s.value)).doubleValue();
		return this;
	}

	/**
	 * Apply absolute value.
	 * @return the object itself
	 */
	public StatsHypercube abs() {
		return apply(new UnaryOperator<Double>() {
			@Override
			public Double apply(Double val) { return new Double(Math.abs(val.doubleValue())); }});
	}

	/**
	 * Multiply the values by a factor.
	 * @param factor
	 * @return the object itself
	 */
	public StatsHypercube mult(double factor){
		return apply(new UnaryOperator<Double>() {
			@Override
			public Double apply(Double val) {
				return new Double(val.doubleValue() * factor);
			}});
	}
	/**
	 * Add a qualtity to the values.
	 * @param valueToSum
	 * @return the object itself
	 */
	public StatsHypercube sum(double valueToSum){
		return apply(new UnaryOperator<Double>() {
			@Override
			public Double apply(Double val) {
				return new Double(val.doubleValue() + valueToSum);
			}});
	}
	/**
	 * Apply a power to the values.
	 * @param exp
	 * @return the object itself
	 */
	public StatsHypercube pow(double exp){
		return apply(new UnaryOperator<Double>() {
			@Override
			public Double apply(Double val) {
				return new Double(Math.pow(val.doubleValue(), exp));
			}});
	}
	/**
	 * Invert the values.
	 * @return the object itself
	 */
	public StatsHypercube inv() { return pow(-1); }
	/**
	 * Apply root square of the values
	 * @return the object itself
	 */
	public StatsHypercube sqrt() { return pow(0.5); }
	/**
	 * Remove a quantity to the values.
	 * @param valueToDiff
	 * @return the object itself
	 */
	public StatsHypercube diff(double valueToDiff){ return sum(-valueToDiff); }
	/**
	 * Divide all values by a quantity.
	 * @param valueToDiv
	 * @return the object itself
	 */
	public StatsHypercube div(double valueToDiv){ return mult(1/valueToDiv); }
	/**
	 * Change the sign of the values.
	 * @return the object itself
	 */
	public StatsHypercube opp(){ return mult(-1); }


	/**
	 * Apply an operation to the values, using values of another hypercube.
	 * @param op
	 * @param hc
	 * @return the object itself
	 */
	public StatsHypercube apply(BinaryOperator<Double> op, StatsHypercube hc) {
		String[] dimLabels = getDimLabels();
		StatsIndex hcI = new StatsIndex(hc, dimLabels);
		for(Stat s : this.stats) {
			//get values
			Double val1 = new Double(s.value);
			Double val2 = new Double(hcI.getSingleValue(s.getDimValues(dimLabels)));

			//compute and apply new figure
			s.value = op.apply(val1, val2).doubleValue();
		}
		return this;
	}

	/**
	 * Add the values of another hypercube.
	 * @param hc
	 * @return the object itself
	 */
	public StatsHypercube sum(StatsHypercube hc){
		return apply(new BinaryOperator<Double>() {
			@Override
			public Double apply(Double val1, Double val2) {
				return new Double(val1.doubleValue() + val2.doubleValue());
			}}, hc);
	}
	/**
	 * Remove the values of another hypercube.
	 * @param hc
	 * @return the object itself
	 */
	public StatsHypercube diff(StatsHypercube hc){
		return apply(new BinaryOperator<Double>() {
			@Override
			public Double apply(Double val1, Double val2) {
				return new Double(val1.doubleValue() - val2.doubleValue());
			}}, hc);
	}
	/**
	 * Divide by the values of another hypercube.
	 * @param hc
	 * @return the object itself
	 */
	public StatsHypercube div(StatsHypercube hc){
		return apply(new BinaryOperator<Double>() {
			@Override
			public Double apply(Double val1, Double val2) {
				return new Double(val1.doubleValue() / val2.doubleValue());
			}}, hc);
	}
	/**
	 * Multiply by the values of another hypercube.
	 * @param hc
	 * @return the object itself
	 */
	public StatsHypercube mult(StatsHypercube hc){
		return apply(new BinaryOperator<Double>() {
			@Override
			public Double apply(Double val1, Double val2) {
				return new Double(val1.doubleValue() * val2.doubleValue());
			}}, hc);
	}








	/**
	 * Print hypercube structure.
	 */
	public void printInfo() {
		printInfo(true);
	}
	public void printInfo(boolean printDimValues) {
		System.out.println("Information: " + this.stats.size() + " value(s) with " + this.dimLabels.size() + " dimension(s).");
		for(String lbl : this.dimLabels){
			ArrayList<String> vals = new ArrayList<>((getDimValues(lbl)));
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
		Collection<Double> vals = new HashSet<>();
		for(Stat s : this.stats) if(!Double.isNaN(s.value)) vals.add(new Double(s.value));
		StatsUtil.printStats(vals);
	}

	public double[] getQuantiles(int nb) {
		Collection<Double> vals = new HashSet<>();
		for(Stat s : this.stats) if(!Double.isNaN(s.value)) vals.add(new Double(s.value));
		return StatsUtil.getQuantiles(vals, nb);
	}

	public void printQuantiles(int nb) {
		Collection<Double> vals = new HashSet<>();
		for(Stat s : this.stats) if(!Double.isNaN(s.value)) vals.add(s.value);
		StatsUtil.printQuantiles(vals, nb);
	}

	/**
	 * Transform an hypercube with only one dimension into a hashmap.
	 * @return 
	 */
	public HashMap<String, Double> toMap(){
		HashMap<String, Double> map = new HashMap<>();
		String dimLabel = this.dimLabels.iterator().next();
		for(Stat s : this.stats) map.put(s.dims.get(dimLabel), s.value);
		return map;
	}


	//TODO copy hypercube
}
