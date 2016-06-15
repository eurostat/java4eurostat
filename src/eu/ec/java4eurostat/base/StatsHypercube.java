/**
 * 
 */
package eu.ec.java4eurostat.base;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

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

	public void exportAsCSV(String outFilePath) {
		exportAsCSV(outFilePath, "geo", "time");
	}

	public void exportAsCSV(String outFilePath, String geoLabel, String timeLabel) {
		exportAsCSV(outFilePath, geoLabel, timeLabel, null);
	}

	public void exportAsCSV(String outFilePath, String geoLabel, String timeLabel, String addLabel) {
		exportAsCSV(outFilePath, geoLabel, timeLabel, addLabel, "\t");
	}

	//TODO ??? export all dims in a clean way
	public void exportAsCSV(String outFilePath, String geoLabel, String timeLabel, String addLabel, String sep) {
		try {
			new File(outFilePath).delete();
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outFilePath), true));

			//write header
			StringBuffer sb = new StringBuffer();
			sb.append(geoLabel).append(sep).append(timeLabel);
			if(addLabel!=null)
				sb.append(sep).append(addLabel);
			sb.append(sep).append("value").append("\n");
			bw.write(sb.toString());

			//write data
			for(Stat s : stats){
				sb = new StringBuffer();
				sb.append(s.dims.get(geoLabel)).append(sep).append(s.dims.get(timeLabel));
				if(addLabel!=null)
					sb.append(sep).append(s.dims.get(addLabel));
				sb.append(sep).append(s.value).append("\n");
				bw.write(sb.toString());
			}
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	public void checkGeoIds(Collection<String> nIds) {
		checkGeoIds("geo",nIds);
	}
	public void checkGeoIds(String geoLabel, Collection<String> nIds) {
		HashMap<String,Integer> missings = new HashMap<String,Integer>();
		//list and count missings
		for(Stat s : stats){
			String geoId = s.dims.get(geoLabel);
			if(nIds.contains(geoId)) continue;
			if(missings.get(geoId)==null)
				missings.put(geoId, 1);
			else
				missings.put(geoId, missings.get(geoId)+1);
		}
		//show result
		System.err.println("\t"+missings.size()+" geolocations missing");
		for(Entry<String,Integer> missing : missings.entrySet())
			System.err.println("\tUnknown geolocation id: "+missing.getKey()+" ("+missing.getValue()+" times)");
	}
	 */
}
