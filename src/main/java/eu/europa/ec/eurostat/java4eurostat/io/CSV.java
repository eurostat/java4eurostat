/**
 * 
 */
package eu.europa.ec.eurostat.java4eurostat.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.europa.ec.eurostat.java4eurostat.analysis.Selection;
import eu.europa.ec.eurostat.java4eurostat.analysis.Selection.Criteria;
import eu.europa.ec.eurostat.java4eurostat.base.Stat;
import eu.europa.ec.eurostat.java4eurostat.base.StatsHypercube;
import eu.europa.ec.eurostat.java4eurostat.base.StatsIndex;

/**
 * @author julien Gaffuri
 *
 */
public class CSV {
	private final static Logger LOGGER = LogManager.getLogger(CSV.class.getName());

	private static final String PAT = "\\s*(\"[^\"]*\"|[^,]*)\\s*";

	@SuppressWarnings("javadoc")
	public static StatsHypercube load(String inputFilePath, String valueLabel) { return load(inputFilePath, valueLabel, PAT, null); }
	@SuppressWarnings("javadoc")
	public static StatsHypercube load(String inputFilePath, String valueLabel, String patternString) { return load(inputFilePath, valueLabel, patternString, null); }
	@SuppressWarnings("javadoc")
	public static StatsHypercube load(String inputFilePath, String valueLabel, Criteria ssc) { return load(inputFilePath, valueLabel, PAT, ssc); }

	/**
	 * Load a CSV file.
	 * 
	 * @param inputFilePath
	 * @param valueLabel The label of the column with values
	 * @param patternString NB: for tab separated files, use "([^\t]*)"
	 * @param ssc Selection criteria
	 * @return
	 */
	public static StatsHypercube load(String inputFilePath, String valueLabel, String patternString, Selection.Criteria ssc) {
		StatsHypercube hc = new StatsHypercube();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(inputFilePath));
			Pattern pattern = Pattern.compile(patternString);

			//read header
			String line = br.readLine();
			Matcher m = pattern.matcher(line);
			ArrayList<String> keys = new ArrayList<>();
			while(m.find()){
				keys.add(m.group(1));
				m.find();
			}
			hc.dimLabels.addAll(keys);
			hc.dimLabels.remove(valueLabel);

			//read data
			while ((line = br.readLine()) != null) {
				m = pattern.matcher(line);
				Stat s = new Stat();
				for(String key : keys){
					m.find();
					String value = m.group(1);
					if(!"".equals(value)) m.find();
					if(key.equals(valueLabel)) {
						try {
							s.value = Double.parseDouble(value);
						} catch (@SuppressWarnings("unused") NumberFormatException e) {
							LOGGER.warn("Could not parse statistical value: "+value);
							s.value = Double.NaN;
						}
					}
					else
						s.dims.put(key, value);
				}

				if(ssc!=null && !ssc.keep(s)) continue;

				hc.stats.add(s);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return hc;		
	}


	/**
	 * Load a CSV file of statistical data for which several values are specified per raw.
	 * 
	 * @param csvFilePath The CSV file.
	 * @param newDimLabel The new dimension label for the different value columns.
	 * @param valueColumns The header of the raws containing statistical values.
	 * @return
	 */
	public static StatsHypercube loadMultiValues(String csvFilePath, String newDimLabel, String... valueColumns) {
		ArrayList<HashMap<String, String>> data = CSVAsHashMap.load(csvFilePath);

		//get list of dimension labels
		ArrayList<String> dimLabels = new ArrayList<>(data.iterator().next().keySet());
		for(String vc : valueColumns) {
			boolean b = dimLabels.remove(vc);
			if(!b) System.out.println("Pb " + vc);
		}

		//build new hypercube
		StatsHypercube hc = new StatsHypercube( dimLabels.toArray(new String[dimLabels.size()]) );
		hc.dimLabels.add(newDimLabel);

		//fill new hypercube
		for(HashMap<String, String> elt : data) {
			for(String value : valueColumns) {
				if(elt.get(value) == null) {
					System.out.println("no data found for "+value+"   "+elt);
					continue;
				}
				Stat s = new Stat();
				for(String dl_ : dimLabels) s.dims.put(dl_, elt.get(dl_));
				s.dims.put(newDimLabel, value);
				s.value = Double.parseDouble(elt.get(value));
				hc.stats.add(s);
			}
		}
		return hc;
	}








	public static void save(StatsHypercube hc, String valueLabel, String outFile) { save(hc,valueLabel,outFile, ","); }
	public static void save(StatsHypercube hc, String valueLabel, String outFile, String separator) { save(hc, valueLabel, outFile, separator, null); }

	/**
	 * Save a hypercube as a CSV file.
	 * 
	 * @param hc
	 * @param valueLabel The text to use for the column containing the values.
	 * @param outFile The output file path.
	 * @param separator The separator.
	 * @param keysComparator A comparator in case the columns have to be ordered.
	 */
	public static void save(StatsHypercube hc, String valueLabel, String outFile, String separator, Comparator<String> keysComparator) {
		try {
			File f = FileUtil.getFile(outFile, true, true);
			BufferedWriter bw = new BufferedWriter(new FileWriter(f, true));

			//write header
			Collection<String> dimLabels = new ArrayList<>(hc.dimLabels);
			if(keysComparator != null){
				Collections.sort((ArrayList<String>)dimLabels, keysComparator);
			}
			int i=0;
			for(String dimLabel:dimLabels ){
				if(dimLabel.contains(separator)) bw.write("\"");
				bw.write(dimLabel);
				if(dimLabel.contains(separator)) bw.write("\"");
				if(i<dimLabels.size()-1) bw.write(",");
				i++;
			}
			bw.write(","+valueLabel+"\n");

			//write data
			for(Stat s : hc.stats){
				i=0;
				for(String dimLabel : dimLabels){
					String dimValue = s.dims.get(dimLabel);
					if(dimValue.contains(separator)) bw.write("\"");
					bw.write(dimValue);
					if(dimValue.contains(separator)) bw.write("\"");
					if(i<dimLabels.size()-1) bw.write(",");
					i++;
				}
				bw.write(",");
				//write value as a double or as an int (to avoid writing the ".0") in the end.
				if( (s.value % 1) == 0 ) bw.write(""+(int)s.value);
				else bw.write(""+s.value);
				bw.write("\n");
			}
			bw.close();
		} catch (Exception e) {e.printStackTrace();}
	}




	/**
	 * @param hcI
	 * @param idName
	 * @param outFile
	 */
	public static void save(StatsIndex hcI, String idName, String outFile) {
		try {
			File outFile_ = new File(outFile);
			if(outFile_.exists()) outFile_.delete();
			BufferedWriter bw = new BufferedWriter(new FileWriter(outFile_, true));
			//write header
			bw.write(idName+",value"); bw.newLine();
			//write file
			for(String geo : hcI.getKeys()){
				bw.write(geo+","+hcI.getSingleValue(geo));
				bw.newLine();
			}
			bw.close();
		} catch (IOException e) { e.printStackTrace(); }
	}




	public static void saveMultiValues(StatsHypercube hc, String outFile, String separator, Comparator<String> keysComparator, String dimLabelColumn) {
		saveMultiValues(hc, outFile, separator, keysComparator, dimLabelColumn, hc.getDimValues(dimLabelColumn).toArray(new String[]{}));
	}

	/**
	 * Save a hypercube as a CSV file for which several values are specified per raw.
	 * 
	 * @param hc 
	 * @param outFile The output file path.
	 * @param dimLabelColumn The new dimension label for the different value columns.
	 * @param valueColumns The header of the raws containing statistical values.
	 * @return
	 */
	public static void saveMultiValues(StatsHypercube hc, String outFile, String separator, Comparator<String> keysComparator, String dimLabelColumn, String... valueColumns) {
		try {
			File f = FileUtil.getFile(outFile, true, true);
			BufferedWriter bw = new BufferedWriter(new FileWriter(f, true));

			//write header
			ArrayList<String> dimLabels = new ArrayList<>(hc.dimLabels);
			dimLabels.remove(dimLabelColumn);
			if(keysComparator != null)
				Collections.sort(dimLabels, keysComparator);
			for(String dimLabel : dimLabels ){
				if(dimLabel.contains(separator)) bw.write("\"");
				bw.write(dimLabel);
				if(dimLabel.contains(separator)) bw.write("\"");
				bw.write(",");
			}
			int i=0;
			for(String valueColumn : valueColumns ){
				if(valueColumn.contains(separator)) bw.write("\"");
				bw.write(valueColumn);
				if(valueColumn.contains(separator)) bw.write("\"");
				if(i<valueColumns.length-1) bw.write(",");
				i++;
			}
			bw.write("\n");

			
			//TODO
			StatsIndex si = new StatsIndex(hc, dimLabelColumn);

			//write data
			for(Stat s : hc.stats){
				for(String dimLabel : dimLabels){
					String dimValue = s.dims.get(dimLabel);
					if(dimValue.contains(separator)) bw.write("\"");
					bw.write(dimValue);
					if(dimValue.contains(separator)) bw.write("\"");
					bw.write(",");
				}
				i=0;
				for(String valueColumn : valueColumns ){
					//write value as a double or as an int (to avoid writing the ".0") in the end.
					if( (s.value % 1) == 0 ) bw.write(""+(int)s.value);
					else bw.write(""+s.value);
					if(i<valueColumns.length-1) bw.write(",");
					i++;
				}
				bw.write("\n");
			}
			bw.close();
		} catch (Exception e) {e.printStackTrace();}
	}

}
