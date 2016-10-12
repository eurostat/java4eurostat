/**
 * 
 */
package eu.ec.estat.java4eurostat.util;

import java.io.BufferedWriter;
import java.io.IOException;

import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.stat.StatUtils;

/**
 * @author julien Gaffuri
 *
 */
public class StatsUtil {

	public static void printStats(double[] vals){
		System.out.println("Max = " + StatUtils.max(vals));
		System.out.println("Min = " + StatUtils.min(vals));
		System.out.println("Mean = " + StatUtils.mean(vals));
		System.out.println("Median = " + StatUtils.percentile(vals, 50));
		//System.out.println("RMS = " + Math.sqrt(StatUtils.sumSq(vals)));
		System.out.println("Q1 = " + StatUtils.percentile(vals, 25));
		System.out.println("Q2 = " + StatUtils.percentile(vals, 75));
		System.out.println("Std = " + Math.sqrt(StatUtils.variance(vals)));
	}

	public static void writeStats(BufferedWriter bw, double[] vals) throws MathIllegalArgumentException, IOException {
		bw.write("Max," + StatUtils.max(vals) + "\n");
		bw.write("Min," + StatUtils.min(vals) + "\n");
		bw.write("Mean," + StatUtils.mean(vals) + "\n");
		bw.write("Median," + StatUtils.percentile(vals, 50) + "\n");
		//bw.write("RMS," + Math.sqrt(StatUtils.sumSq(vals)) + "\n");
		bw.write("Q1," + StatUtils.percentile(vals, 25) + "\n");
		bw.write("Q2," + StatUtils.percentile(vals, 75) + "\n");
		bw.write("Std," + Math.sqrt(StatUtils.variance(vals)) + "\n");
	}

}
