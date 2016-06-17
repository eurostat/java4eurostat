/**
 * 
 */
package eu.ec.java4eurostat.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import org.apache.commons.math3.stat.StatUtils;

import eu.ec.java4eurostat.base.Stat;
import eu.ec.java4eurostat.base.StatsHypercube;
import eu.ec.java4eurostat.base.StatsIndex;
import eu.ec.java4eurostat.eurobase.EBTimeUtil;
import eu.ec.java4eurostat.util.Util;

/**
 * @author julien Gaffuri
 *
 */
public class TimeSeriesUtil {


	//print time series
	public static void printSeries(StatsIndex series) { printSeries(series.getCollection()); }
	public static void printSeries(Collection<Stat> series) {
		for(Stat s : sort(series))
			System.out.println(s.dims.get("time") + " " + s.getValueFlagged());
	}

	//sort time series
	public static ArrayList<Stat> sort(Collection<Stat> series){
		ArrayList<Stat> series_ = new ArrayList<Stat>(series);
		Collections.sort(series_, new Comparator<Stat>() {
			@Override
			public int compare(Stat s1, Stat s2) { return s1.dims.get("time").compareTo(s2.dims.get("time")); }
		});
		return series_;
	}



	public static double getAverageMonth(StatsIndex series, String startMonth, String endMonth, boolean allShouldBeThere, int inputRouding){
		int sY = EBTimeUtil.getYearInt(startMonth), sM = EBTimeUtil.getMonthInt(startMonth);
		int eY = EBTimeUtil.getYearInt(endMonth),   eM = EBTimeUtil.getMonthInt(endMonth);

		double sum =0, nb = 0; 
		for(int y=sY; y<=eY; y++){
			int sM_ = y==sY?sM:1, eM_ = y==eY?eM:12;
			for(int m=sM_; m<=eM_; m++){
				double v = series.getSingleValue( EBTimeUtil.getEBText(y, m) );
				if(Double.isNaN(v))
					if(allShouldBeThere) return Double.NaN;
					else continue;
				if(inputRouding >= 0 ) v = Util.round(v, inputRouding);
				nb++; sum+=v;
			}
		}
		if(nb==0) return Double.NaN;
		return sum/nb;
	}

	public static double getStdMonth(StatsIndex series, String startMonth, String endMonth, boolean allShouldBeThere){
		int sY = EBTimeUtil.getYearInt(startMonth), sM = EBTimeUtil.getMonthInt(startMonth);
		int eY = EBTimeUtil.getYearInt(endMonth),   eM = EBTimeUtil.getMonthInt(endMonth);

		ArrayList<Double> vals_ = new ArrayList<Double>();
		for(int y=sY; y<=eY; y++){
			int sM_ = y==sY?sM:1, eM_ = y==eY?eM:12;
			for(int m=sM_; m<=eM_; m++){
				double v = series.getSingleValue( EBTimeUtil.getEBText(y, m) );
				if(Double.isNaN(v))
					if(allShouldBeThere) return Double.NaN;
					else continue;
				vals_.add(v);
			}
		}
		if(vals_.size() == 0) return Double.NaN;
		double[] vals = new double[vals_.size()]; for(int i=0;i<vals_.size();i++) vals[i]=vals_.get(i);
		return Math.sqrt(StatUtils.variance(vals));
	}

	public static ArrayList<String> getTimeList(StatsIndex series){
		ArrayList<String> t = new ArrayList<String>(); t.addAll(series.getKeys());
		Collections.sort(t);
		return t;
	}

	public static StatsIndex getMovingAverageMonth(StatsIndex seriesM, int w){
		StatsHypercube sh = new StatsHypercube("time");
		ArrayList<String> times = getTimeList(seriesM);
		for(String time : times){
			Stat s = new Stat();
			s.value = getAverageMonth(seriesM, EBTimeUtil.get(time,-w), EBTimeUtil.get(time,w), false, -1);
			s.dims.put("time", time);
			sh.stats.add(s);
		}
		return new StatsIndex(sh, "time");
	}


	public static StatsIndex diff(StatsIndex series1, StatsIndex series2) {
		StatsHypercube sh = new StatsHypercube("time");
		ArrayList<String> times = getTimeList(series1);
		for(String time : times){
			Stat s = new Stat();
			s.value = series1.getSingleValue(time) - series2.getSingleValue(time);
			s.dims.put("time", time);
			sh.stats.add(s);
		}
		return new StatsIndex(sh, "time");
	}

	public static StatsIndex getMovingStdMonth(StatsIndex seriesM, int w) {
		StatsHypercube sh = new StatsHypercube("time");
		ArrayList<String> times = getTimeList(seriesM);
		for(String time : times){
			Stat s = new Stat();
			s.value = getStdMonth(seriesM, EBTimeUtil.get(time,-w), EBTimeUtil.get(time,w), false);
			s.dims.put("time", time);
			sh.stats.add(s);
		}
		return new StatsIndex(sh, "time");
	}




	public static class Gap implements Comparable<Gap>{
		public String label;
		public String t1, t2;
		public Gap(String label, String t1, String t2) { this.label = label; this.t1 = t1; this.t2 = t2; }
		@Override
		public int compareTo(Gap g) { return g.getDuration()-this.getDuration(); }
		public int getDuration() { return EBTimeUtil.getDuration(t1,t2); }
		public void print() {
			System.out.println(this.label+","+this.getDuration()+","+this.t1+","+this.t2+",");
		}
	}

	/**
	 * @param series The time series.
	 * @param label A label to identify the time series.
	 * @return List of gaps.
	 */
	public static ArrayList<Gap> performGapAnalysis(StatsIndex series, String label) {
		ArrayList<Gap> gaps = new ArrayList<Gap>();
		ArrayList<String> t = getTimeList(series);
		String t1 = t.get(0), t2;
		for(int i=1; i<t.size(); i++){
			t2 = t.get(i);
			String t2_ = EBTimeUtil.getNext(t1);
			if(!t2.equals(t2_)) gaps.add(new Gap(label,t1,t2));
			t1 = t2;
		}
		return gaps;
	}

	/**
	 * @param series The time series.
	 * @param w The half-width of the smoothing window for the trend construction and the dispersion calculation.
	 * @param thNbStd The number of STD used to consider a value as possible outlier.
	 * @param diffTh The minimal difference to consider a value as possible outlier.
	 * @param label A label to identify the time series.
	 * @return List of outliers.
	 */
	public static ArrayList<Outlier> performOutlierDetection(StatsIndex series, int w, double thNbStd, double diffTh, String label) {
		//compute trend
		StatsIndex seriesMean = getMovingAverageMonth(series, w);
		//compute difference to trend
		StatsIndex seriesDiff = diff(series, seriesMean);
		//compute std
		StatsIndex seriesStd = getMovingStdMonth(seriesDiff, w);

		/*System.out.println("series");
		TimeSeriesUtil.printSeries(series);
		System.out.println("seriesT");
		TimeSeriesUtil.printSeries(seriesT);
		System.out.println("seriesV");
		TimeSeriesUtil.printSeries(seriesV);
		System.out.println("seriesSD");
		TimeSeriesUtil.printSeries(seriesSD);*/

		ArrayList<Outlier> outliers = new ArrayList<Outlier>();

		for(String time : series.getKeys()){
			double diff = seriesDiff.getSingleValue(time);
			double std = seriesStd.getSingleValue(time);

			if(diff <= diffTh) continue;
			if(std <= diffTh/thNbStd) continue;

			double gravity = Math.abs(diff)/std;
			if(gravity <= thNbStd) continue;

			outliers.add(new Outlier(label, time, diff, std));
		}

		//Collections.sort(out);
		return outliers;
	}

	public static class Outlier implements Comparable<Outlier>{
		public String label;
		public String time;
		public double diff,std,gravity;
		public Outlier(String label, String time, double diff, double std) { this.label = label; this.time = time; this.diff = diff; this.std = std; this.gravity = diff/std; }
		@Override
		public int compareTo(Outlier o) { return (int)Math.round(100000*(o.gravity-this.gravity)); }
		public void print() {
			System.out.println(this.label+","+this.time+","+this.gravity+","+this.diff+","+this.std);
		}
	}



	//check if a year of a monthly timeseries is empty
	public static boolean isEmpty(StatsIndex series, int year) {
		for(int m=1; m<=12; m++)
			if(!Double.isNaN( series.getSingleValue(EBTimeUtil.getEBText(year, m)) )) return false;
		return true;
	}

	//multiplies all months of a year values by a factor
	public static void multiplyYearValues(StatsIndex series, int year, double factor){
		for(int m=1; m<=12; m++){
			Stat s = series.getSingleStat(EBTimeUtil.getEBText(year, m));
			if(s == null) continue;
			if(Double.isNaN(s.value)) continue;
			s.value *= factor;
		}
	}

}
