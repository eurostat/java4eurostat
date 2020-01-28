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
			Stat s_ = new Stat(s);
			s_.value = uop.apply(new Double(s.value)).doubleValue();
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
			//retrieve values
			double v1 = s.value;
			double v2 = hcI2.getSingleValue(s.getDimValues(dimLabels));

			//compute new value
			Stat s_ = new Stat(s);
			s_.value = bop.apply(new Double(v1), new Double(v2)).doubleValue();
			out.stats.add(s_);
		}
		return out;
	}



	/**
	 * Compute an aggregated value of all values along a dimension.
	 * 
	 * @param hc
	 * @param agg The aggregation operation.
	 * @param dimLabel The dimension to aggregate along.
	 * @param aggDimValue The dimension value for the new aggregated value.
	 * @return
	 */
	public static Collection<Stat> computeAggregation(StatsHypercube hc, Aggregator agg, String dimLabel, String aggDimValue) {
		Collection<Stat> out = new ArrayList<>();

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
			if(LOGGER.isDebugEnabled()) LOGGER.debug("Compute aggregate for " + s.dims);

			//get values to aggregate
			//TODO: provide possiblity to ignore some dimension values
			double[] vals = new double[leaf.size()]; int i=0;
			for(Stat s_ : leaf) vals[i++] = s_.value;

			//compute aggregated value
			s.value = agg.compute(vals);

			out.add(s);
		}
		return out;
	}
	public interface Aggregator { double compute(double[] values); }


	/**
	 * Compute the sum of all values along a dimension.
	 * 
	 * @param hc
	 * @param dimLabel
	 * @param sumDimValue The dimension value for the new value.
	 * @return
	 */
	public static Collection<Stat> computeSumDim(StatsHypercube hc, String dimLabel, String sumDimValue) {
		return computeAggregation(hc, new Aggregator() {
			@Override
			public double compute(double[] vals) {
				return StatUtils.sum(vals);
			}
		}, dimLabel, sumDimValue);
	}

	/**
	 * Compute the maximum of all values along a dimension.
	 * 
	 * @param hc
	 * @param dimLabel
	 * @param maxDimValue The dimension value for the new value.
	 * @return
	 */
	public static Collection<Stat> computeMaxDim(StatsHypercube hc, String dimLabel, String maxDimValue) {
		return computeAggregation(hc, new Aggregator() {
			@Override
			public double compute(double[] vals) {
				if(vals.length == 0) return Double.NaN;
				return StatUtils.max(vals);
			}
		}, dimLabel, maxDimValue);
	}

	/**
	 * Compute the minimum of all values along a dimension.
	 * 
	 * @param hc
	 * @param dimLabel
	 * @param minDimValue The dimension value for the new value.
	 * @return
	 */
	public static Collection<Stat> computeMinDim(StatsHypercube hc, String dimLabel, String minDimValue) {
		return computeAggregation(hc, new Aggregator() {
			@Override
			public double compute(double[] vals) {
				if(vals.length == 0) return Double.NaN;
				return StatUtils.min(vals);
			}
		}, dimLabel, minDimValue);
	}

	/**
	 * Compute the mean of all values along a dimension.
	 * 
	 * @param hc
	 * @param dimLabel
	 * @param meanDimValue The dimension value for the new value.
	 * @return
	 */
	public static Collection<Stat> computeMeanDim(StatsHypercube hc, String dimLabel, String meanDimValue) {
		return computeAggregation(hc, new Aggregator() {
			@Override
			public double compute(double[] vals) {
				if(vals.length == 0) return Double.NaN;
				return StatUtils.mean(vals);
			}
		}, dimLabel, meanDimValue);
	}


	/**
	 * Compute a percentile of all values along a dimension.
	 * 
	 * @param hc
	 * @param percentile
	 * @param dimLabel
	 * @param percentileDimValue The dimension value for the new value.
	 * @return
	 */
	public static Collection<Stat> computePercentileDim(StatsHypercube hc, int percentile, String dimLabel, String percentileDimValue) {
		return computeAggregation(hc, new Aggregator() {
			@Override
			public double compute(double[] vals) {
				if(vals.length == 0) return Double.NaN;
				return StatUtils.percentile(vals, percentile);
			}
		}, dimLabel, percentileDimValue);
	}

	/**
	 * Compute the median of all values along a dimension.
	 * 
	 * @param hc
	 * @param dimLabel
	 * @param medianDimValue The dimension value for the new value.
	 * @return
	 */
	public static Collection<Stat> computeMedianDim(StatsHypercube hc, String dimLabel, String medianDimValue) {
		return computePercentileDim(hc, 50, dimLabel, medianDimValue);
	}

	public static Collection<Stat> computeQuartile1Dim(StatsHypercube hc, String dimLabel, String q1DimValue) {
		return computePercentileDim(hc, 25, dimLabel, q1DimValue);
	}

	public static Collection<Stat> computeQuartile2Dim(StatsHypercube hc, String dimLabel, String q2DimValue) {
		return computePercentileDim(hc, 75, dimLabel, q2DimValue);
	}

	public static Collection<Stat> computeStdDim(StatsHypercube hc, String dimLabel, String stdDimValue) {
		return computeAggregation(hc, new Aggregator() {
			@Override
			public double compute(double[] vals) {
				if(vals.length == 0) return Double.NaN;
				return Math.sqrt(StatUtils.variance(vals));
			}
		}, dimLabel, stdDimValue);
	}

	public static Collection<Stat> computeRMSDim(StatsHypercube hc, String dimLabel, String rmsDimValue) {
		return computeAggregation(hc, new Aggregator() {
			@Override
			public double compute(double[] vals) {
				if(vals.length == 0) return Double.NaN;
				return Math.sqrt(StatUtils.sumSq(vals)/vals.length);
			}
		}, dimLabel, rmsDimValue);
	}

	/*
	public static void main(String[] args) {
		String path = "./src/test/resources/";
		StatsHypercube hc = CSV.load(path+"ex.csv", "population");

		//hc.printInfo();

		Collection<Stat> stats = computeSumDim(hc, "country", "Total");
		System.out.println(stats);
	}
	 */
}
