/**
 * 
 */
package eu.europa.ec.eurostat.java4eurostat.analysis;

import eu.europa.ec.eurostat.java4eurostat.base.Stat;
import eu.europa.ec.eurostat.java4eurostat.base.StatsHypercube;
import eu.europa.ec.eurostat.java4eurostat.base.StatsIndex;

/**
 * Operations hypercube structures
 * 
 * @author julien Gaffuri
 *
 */
public class Operations {

	/**
	 * A unary operation
	 * 
	 * @author julien Gaffuri
	 *
	 */
	public interface UnaryOperation {
		double compute(double val);
	}

	/**
	 * Compute a unary operation.
	 * 
	 * @param hc
	 * @param uop
	 * @return
	 */
	public static StatsHypercube compute(StatsHypercube hc, UnaryOperation uop){
		String[] dimLabels = hc.getDimLabels();
		StatsHypercube out = new StatsHypercube(dimLabels);
		for(Stat s : hc.stats){
			//get stat value
			double val = s.value;
			if(Double.isNaN(val)) continue;

			//compute comparison figure
			double outVal = uop.compute(val);
			if(Double.isNaN(val)) continue;

			//store comparison figures
			Stat sc = new Stat(outVal);
			for(int i=0; i<dimLabels.length; i++) sc.dims.put(dimLabels[i], s.dims.get(dimLabels[i]));
			out.stats.add(sc);
		}
		return out;
	}

	/**
	 * Apply a unary operation.
	 * 
	 * @param hc
	 * @param uop
	 */
	public static void apply(StatsHypercube hc, UnaryOperation uop){
		for(Stat s : hc.stats){
			double val = s.value;
			if(Double.isNaN(val)) continue;
			s.value = uop.compute(val);
		}
	}

	/**
	 * A binary operation
	 * 
	 * @author julien Gaffuri
	 *
	 */
	public interface BinaryOperation {
		double compute(double val1, double val2);
	}

	/**
	 * Compute a binary operation.
	 * 
	 * @param hc1
	 * @param hc2
	 * @param bop
	 * @return
	 */
	public static StatsHypercube compute(StatsHypercube hc1, StatsHypercube hc2, BinaryOperation bop){
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
			double outVal = bop.compute(val1, val2);
			if(Double.isNaN(outVal)) continue;

			//store comparison figures
			Stat sc = new Stat(outVal);
			for(int i=0; i<dimLabels.length; i++) sc.dims.put(dimLabels[i], s.dims.get(dimLabels[i]));
			out.stats.add(sc);
		}
		return out;
	}





	public static StatsHypercube opp(StatsHypercube hc){
		return compute(hc, new UnaryOperation() {
			@Override
			public double compute(double val) {
				return -val;
			}});
	}
	public static StatsHypercube abs(StatsHypercube hc){
		return compute(hc, new UnaryOperation() {
			@Override
			public double compute(double val) {
				return Math.abs(val);
			}});
	}
	public static StatsHypercube sum(StatsHypercube hc, double valueToSum){
		return compute(hc, new UnaryOperation() {
			@Override
			public double compute(double val) {
				return val + valueToSum;
			}});
	}
	public static StatsHypercube diff(StatsHypercube hc, double valueToDiff){
		return compute(hc, new UnaryOperation() {
			@Override
			public double compute(double val) {
				return val - valueToDiff;
			}});
	}
	public static StatsHypercube mult(StatsHypercube hc, double valueToMult){
		return compute(hc, new UnaryOperation() {
			@Override
			public double compute(double val) {
				return val * valueToMult;
			}});
	}
	public static StatsHypercube div(StatsHypercube hc, double valueToDiv){
		return compute(hc, new UnaryOperation() {
			@Override
			public double compute(double val) {
				return val / valueToDiv;
			}});
	}





	public static StatsHypercube sum(StatsHypercube hc1, StatsHypercube hc2){
		return compute(hc1, hc2, new BinaryOperation() {
			@Override
			public double compute(double val1, double val2) {
				return val1+val2;
			}});
	}
	public static StatsHypercube diff(StatsHypercube hc1, StatsHypercube hc2){
		return compute(hc1, hc2, new BinaryOperation() {
			@Override
			public double compute(double val1, double val2) {
				return val1-val2;
			}});
	}
	public static StatsHypercube div(StatsHypercube hc1, StatsHypercube hc2){
		return compute(hc1, hc2, new BinaryOperation() {
			@Override
			public double compute(double val1, double val2) {
				return val1/val2;
			}});
	}
	public static StatsHypercube mult(StatsHypercube hc1, StatsHypercube hc2){
		return compute(hc1, hc2, new BinaryOperation() {
			@Override
			public double compute(double val1, double val2) {
				return val1*val2;
			}});
	}

	/**
	 * Compute the total of all values along a dimension
	 * 
	 * @param hc
	 * @param dim
	 * @param totalDimValue
	 */
	public void computeTotalDim(StatsHypercube hc, String dim, String totalDimValue) {
		//TODO index along the other dimensions
		//TODO go accros all other dimensions
		//TODO compute total
		//TODO create new stat with total value
	}

	//TODO copy

}
