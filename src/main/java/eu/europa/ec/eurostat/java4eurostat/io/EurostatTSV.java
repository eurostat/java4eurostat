package eu.europa.ec.eurostat.java4eurostat.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import eu.europa.ec.eurostat.java4eurostat.base.Flag;
import eu.europa.ec.eurostat.java4eurostat.base.Selection.Criteria;
import eu.europa.ec.eurostat.java4eurostat.base.Stat;
import eu.europa.ec.eurostat.java4eurostat.base.StatsHypercube;

/**
 * @author julien Gaffuri
 *
 */
public class EurostatTSV {

	@SuppressWarnings("javadoc")
	public static StatsHypercube load(String inputFilePath){ return load( inputFilePath, null ); }

	/**
	 * @param inputFilePath
	 * @param ssc
	 * @return
	 */
	@SuppressWarnings("resource")
	public static StatsHypercube load(String inputFilePath, Criteria ssc){
		String sep="\t";
		BufferedReader br = null;
		StatsHypercube sh = new StatsHypercube();
		ArrayList<String> dimLabelsL = new ArrayList<>();
		try {
			//read dimensions
			br = new BufferedReader(new FileReader(new File(inputFilePath)));

			//read first line
			String line = br.readLine();
			StringTokenizer st = new StringTokenizer(line,sep);

			//read dim names
			StringTokenizer stDims = new StringTokenizer(st.nextToken(),",");
			while(stDims.hasMoreTokens()){
				dimLabelsL.add( stDims.nextToken().replace("\\time", "") );
			}
			dimLabelsL.add("time");
			sh.dimLabels.addAll(dimLabelsL);
			br.close();



			//read the files
			br = new BufferedReader(new FileReader(new File(inputFilePath)));

			//read the years
			line = br.readLine();
			st = new StringTokenizer(line,sep);
			st.nextToken();
			String[] times=new String[st.countTokens()];
			int i=0;
			while(st.hasMoreTokens())
				times[i++] = st.nextToken().replace(" ", "");

			while ((line = br.readLine()) != null) {
				st = new StringTokenizer(line,sep);

				//read dims
				String lbl = st.nextToken();

				int timeIndex=-1;
				while(st.hasMoreTokens()){
					timeIndex++;
					//TODO better manage flags
					String val_ = st.nextToken().replace(" ", "");
					if(val_.contains(":")) continue;

					Stat s = new Stat();

					//flags
					char[] flagCodes = val_.replaceAll("\\d","").replace(".", "").replace("-", "").toCharArray();
					for(int k=0; k<flagCodes.length; k++){
						char fc = flagCodes[k];
						Flag.FlagType ft = Flag.code.get(""+fc);
						if(ft == null){
							System.err.println("Unknown flag: "+fc);
							continue;
						}
						s.addFlag(ft);
					}

					//value
					s.value = Double.parseDouble( val_.replaceAll("[A-Za-z]", "") );

					//dims
					stDims = new StringTokenizer(lbl,","); int dimIndex=0;
					while(stDims.hasMoreTokens()){
						String dimLabel = dimLabelsL.get(dimIndex++);
						String dimValue = stDims.nextToken();
						s.dims.put(dimLabel, dimValue);
					}
					//year
					s.dims.put("time", times[timeIndex]);

					if(ssc!=null && !ssc.keep(s)) continue;

					sh.stats.add(s);
				}
			}

			br.close();
			br = null;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try { if (br != null) br.close(); } catch (Exception e) { e.printStackTrace(); }
		}
		return sh;
	}

	/*
	public static void save(StatsHypercube hc, String filePath){
		//TODO

		//write header  ---   indic_to,unit,nace_r2,geo\time	2015 	2014 	2013 	2012 	2011 	2010 	2009 	2008 	2007 
		//write dimension labels, finishing with geo and time.
		//store all time labels, ordered
		//write all time label values, ordered

		//write lines  ---    B004,NR,I551,BE24	571068 	: 	518368 	470992 	457292 	408248 	355218 	397598 	350601 	246675
		//index dataset, finishing with geo and time
		//walk through index tree.

	}
	 */

}
