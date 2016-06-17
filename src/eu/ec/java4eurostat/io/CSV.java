/**
 * 
 */
package eu.ec.java4eurostat.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.ec.java4eurostat.base.Selection;
import eu.ec.java4eurostat.base.Selection.Criteria;
import eu.ec.java4eurostat.base.Stat;
import eu.ec.java4eurostat.base.StatsHypercube;

/**
 * @author julien Gaffuri
 *
 */
public class CSV {
	private static final String PAT = "\\s*(\"[^\"]*\"|[^,]*)\\s*";

	public static StatsHypercube load(String inputFilePath, String valueLabel) { return load(inputFilePath, valueLabel, PAT, null); }
	public static StatsHypercube load(String inputFilePath, String valueLabel, String patternString) { return load(inputFilePath, valueLabel, patternString, null); }
	public static StatsHypercube load(String inputFilePath, String valueLabel, Criteria ssc) { return load(inputFilePath, valueLabel, PAT, ssc); }

	/**
	 * Load a CSV file.
	 * 
	 * @param filePath
	 * @param valueLabel The label of the column with values
	 * @param patternString NB: for tab separated files, use "([^\t]*)"
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
			ArrayList<String> keys = new ArrayList<String>();
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
					if(key.equals(valueLabel))
						s.value = Double.parseDouble(value);
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

	//TODO save

	public static void main(String[] args) {
		load("data/ex.csv", "population").printInfo();
		/*load("data/ex.csv", "population", new StatsHypercube.StatSelectionCriteria(){
			@Override
			public boolean keep(Stat stat) {
				return "Total".equals(stat.dims.get("gender"));
			}}).printInfo();*/
	}

}
