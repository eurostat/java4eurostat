package eu.ec.java4eurostat.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

import eu.ec.java4eurostat.base.Flag;
import eu.ec.java4eurostat.base.Stat;
import eu.ec.java4eurostat.base.StatsHypercube;

/**
 * @author julien Gaffuri
 *
 */
public class TSV {

	public static StatsHypercube load(String... inputFilePath){ return load( inputFilePath, null ); }

	public static StatsHypercube load(String inputFilePath, StatsHypercube.StatSelectionCriteria ssc){ return load( inputFilePath, false, ssc ); }
	public static StatsHypercube load(String inputFilePath, boolean mess, StatsHypercube.StatSelectionCriteria ssc){ return load( new String[]{inputFilePath}, mess, ssc ); }

	public static StatsHypercube load(String[] inputFilePaths, StatsHypercube.StatSelectionCriteria ssc){ return load( inputFilePaths, false, ssc ); }
	public static StatsHypercube load(String[] inputFilePaths, boolean mess, StatsHypercube.StatSelectionCriteria ssc){ return load( new ArrayList<String>(Arrays.asList(inputFilePaths)), mess, ssc ); }

	public static StatsHypercube load(ArrayList<String> inputFilePaths, StatsHypercube.StatSelectionCriteria ssc){ return load( inputFilePaths, false, ssc ); }
	public static StatsHypercube load(ArrayList<String> inputFilePaths, boolean mess, StatsHypercube.StatSelectionCriteria ssc){
		if(inputFilePaths==null || inputFilePaths.size()==0) return null;
		String sep="\t";
		BufferedReader br = null;
		StatsHypercube sh = new StatsHypercube();
		ArrayList<String> dimLabelsL = new ArrayList<String>();
		try {
			//read dimensions in first file
			br = new BufferedReader(new FileReader(new File(inputFilePaths.get(0))));

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

			for(String inputFilePath : inputFilePaths){
				if(mess) System.out.println("   Loading " + inputFilePath);
				br = new BufferedReader(new FileReader(new File(inputFilePath)));

				//read the years
				line = br.readLine();
				st = new StringTokenizer(line,sep);
				st.nextToken();
				String[] years=new String[st.countTokens()];
				int i=0;
				while(st.hasMoreTokens())
					years[i++] = st.nextToken();

				while ((line = br.readLine()) != null) {
					st = new StringTokenizer(line,sep);

					//read dims
					String lbl = st.nextToken();

					int yearIndex=-1;
					while(st.hasMoreTokens()){
						yearIndex++;
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
						s.dims.put("time", years[yearIndex]);

						if(ssc!=null && !ssc.keep(s)) continue;

						sh.stats.add(s);
					}
				}

				if (br != null) br.close();
				br = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try { if (br != null) br.close(); } catch (Exception e) { e.printStackTrace(); }
		}		
		return sh;
	}

}
