/**
 * 
 */
package eu.europa.ec.eurostat.java4eurostat.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.europa.ec.eurostat.java4eurostat.base.Stat;
import eu.europa.ec.eurostat.java4eurostat.base.StatsHypercube;
import eu.europa.ec.eurostat.java4eurostat.base.StatsIndex;
import eu.europa.ec.eurostat.java4eurostat.io.CSV;

/**
 * Operations on hypercube structures.
 * 
 * @author julien Gaffuri
 *
 */
public class Operations {
	public final static Logger LOGGER = LogManager.getLogger(Operations.class.getName());

	/**
	 * Compute a unary operation on hypercube values.
	 * 
	 * @param hc
	 * @param uop
	 * @return
	 */
	public static StatsHypercube compute(StatsHypercube hc, UnaryOperator<Double> uop){
		StatsHypercube out = new StatsHypercube(hc.getDimLabels());
		for(Stat s : hc.stats){
			Stat s_ = new Stat(hc.stats.iterator().next());
			s_.value = uop.apply(s.value);
			out.stats.add(s_);
		}
		return out;
	}

	/**
	 * Compute a binary operation.
	 * 
	 * @param hc1
	 * @param hc2
	 * @param bop
	 * @return
	 */
	public static StatsHypercube compute(StatsHypercube hc1, StatsHypercube hc2, BinaryOperator<Double> bop){
		String[] dimLabels = hc1.getDimLabels();
		StatsHypercube out = new StatsHypercube(dimLabels);
		StatsIndex hcI2 = new StatsIndex(hc2, dimLabels);
		for(Stat s : hc1.stats){
			//get stat value
			double val1 = s.value;
			if(Double.isNaN(val1)) continue;

			//retrieve value to compare with
			String[] dimValues = new String[dimLabels.length];
			for(int i=0; i<dimLabels.length; i++) dimValues[i] = s.dims.get(dimLabels[i]);
			double val2 = hcI2.getSingleValue(dimValues);
			if(Double.isNaN(val2)) continue;

			//compute comparison figure
			double outVal = bop.apply(val1, val2);
			if(Double.isNaN(outVal)) continue;

			//store comparison figures
			Stat sc = new Stat(outVal);
			for(int i=0; i<dimLabels.length; i++) sc.dims.put(dimLabels[i], s.dims.get(dimLabels[i]));
			out.stats.add(sc);
		}
		return out;
	}



	/**
	 * Compute an aggregated value of all values along a dimension.
	 * 
	 * @param hc
	 * @param agg
	 * @param dimLabel
	 * @param aggDimValue
	 * @return
	 */
	public static Collection<Stat> computeAggregation(StatsHypercube hc, Aggregator agg, String dimLabel, String aggDimValue) {
		Collection<Stat> out = new ArrayList<Stat>();

		//index along the other dimensions
		ArrayList<String> lbls = new ArrayList<>(hc.dimLabels);
		boolean b = lbls.remove(dimLabel);
		if(!b) LOGGER.error("Cannot compute aggregate " + aggDimValue + " along non-existing dimension: " + dimLabel);
		StatsIndex index = new StatsIndex(hc, lbls.toArray(new String[lbls.size()]));

		//get through index leaves
		for(Collection<Stat> leaf : index.getLeaves()) {

			if(leaf.size()==0) {
				LOGGER.warn("Unexpected empty leaf in stat index encourtered " + dimLabel + " - " + aggDimValue);
				continue;
			}

			//prepare aggregated stat
			Stat s = new Stat(leaf.iterator().next());
			s.dims.put(dimLabel, aggDimValue);
			if(LOGGER.isDebugEnabled()) LOGGER.debug("Compute total for "+s.dims);

			//compute aggregated value
			double[] vals = new double[leaf.size()]; int i=0;
			for(Stat s_ : leaf) vals[i++] = s_.value;
			s.value = agg.compute(vals);

			out.add(s);
		}
		return out;
	}
	public interface Aggregator { double compute(double[] values); }


	public static Collection<Stat> computeTotalDim(StatsHypercube hc, String dimLabel, String aggDimValue) {
		return computeAggregation(hc, new Aggregator() {
			@Override
			public double compute(double[] vals) {
				return StatUtils.sum(vals);
			}
		}, dimLabel, aggDimValue);
	}

	public static Collection<Stat> computeMaxDim(StatsHypercube hc, String dimLabel, String aggDimValue) {
		return computeAggregation(hc, new Aggregator() {
			@Override
			public double compute(double[] vals) {
				if(vals.length == 0) return Double.NaN;
				return StatUtils.max(vals);
			}
		}, dimLabel, aggDimValue);
	}

	public static Collection<Stat> computeMinDim(StatsHypercube hc, String dimLabel, String aggDimValue) {
		return computeAggregation(hc, new Aggregator() {
			@Override
			public double compute(double[] vals) {
				if(vals.length == 0) return Double.NaN;
				return StatUtils.min(vals);
			}
		}, dimLabel, aggDimValue);
	}

	public static Collection<Stat> computeMeanDim(StatsHypercube hc, String dimLabel, String aggDimValue) {
		return computeAggregation(hc, new Aggregator() {
			@Override
			public double compute(double[] vals) {
				if(vals.length == 0) return Double.NaN;
				return StatUtils.mean(vals);
			}
		}, dimLabel, aggDimValue);
	}

	public static Collection<Stat> computePercentileDim(StatsHypercube hc, int percentile, String dimLabel, String aggDimValue) {
		return computeAggregation(hc, new Aggregator() {
			@Override
			public double compute(double[] vals) {
				if(vals.length == 0) return Double.NaN;
				return StatUtils.percentile(vals, percentile);
			}
		}, dimLabel, aggDimValue);
	}

	public static Collection<Stat> computeMedianDim(StatsHypercube hc, String dimLabel, String aggDimValue) {
		return computePercentileDim(hc, 50, dimLabel, aggDimValue);
	}

	public static Collection<Stat> computeQuartile1Dim(StatsHypercube hc, String dimLabel, String aggDimValue) {
		return computePercentileDim(hc, 25, dimLabel, aggDimValue);
	}

	public static Collection<Stat> computeQuartile2Dim(StatsHypercube hc, String dimLabel, String aggDimValue) {
		return computePercentileDim(hc, 75, dimLabel, aggDimValue);
	}

	public static Collection<Stat> computeStdDim(StatsHypercube hc, String dimLabel, String aggDimValue) {
		return computeAggregation(hc, new Aggregator() {
			@Override
			public double compute(double[] vals) {
				if(vals.length == 0) return Double.NaN;
				return Math.sqrt(StatUtils.variance(vals));
			}
		}, dimLabel, aggDimValue);
	}

	public static Collection<Stat> computeRMSDim(StatsHypercube hc, String dimLabel, String aggDimValue) {
		return computeAggregation(hc, new Aggregator() {
			@Override
			public double compute(double[] vals) {
				if(vals.length == 0) return Double.NaN;
				return Math.sqrt(StatUtils.sumSq(vals)/vals.length);
			}
		}, dimLabel, aggDimValue);
	}


	public static void main(String[] args) {
		String path = "./src/test/resources/";
		StatsHypercube hc = CSV.load(path+"ex.csv", "population");

		//hc.printInfo();

		Collection<Stat> stats = computeTotalDim(hc, "country", "Total");
		System.out.println(stats);
	}

}
