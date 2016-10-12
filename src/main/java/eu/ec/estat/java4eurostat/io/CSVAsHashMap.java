/**
 * 
 */
package eu.ec.estat.java4eurostat.io;

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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author julien Gaffuri
 *
 */
public class CSVAsHashMap {

	/**
	 * Load a CSV file.
	 * 
	 * @param filePath
	 * @return
	 */
	public static ArrayList<HashMap<String,String>> load(String filePath) {
		return load(filePath, "\\s*(\"[^\"]*\"|[^,]*)\\s*");
	}

	/**
	 * Load a CSV file.
	 * 
	 * @param filePath
	 * @param patternString NB: for tab separated files, use "([^\t]*)"
	 * @return
	 */
	public static ArrayList<HashMap<String,String>> load(String filePath, String patternString) {
		ArrayList<HashMap<String,String>> data = new ArrayList<HashMap<String,String>>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(filePath));
			Pattern pattern = Pattern.compile(patternString);

			//read header
			String line = br.readLine();
			Matcher m = pattern.matcher(line);
			ArrayList<String> keys = new ArrayList<String>();
			while(m.find()){
				keys.add(m.group(1));
				m.find();
			}

			//read data
			while ((line = br.readLine()) != null) {
				m = pattern.matcher(line);
				LinkedHashMap<String,String> obj = new LinkedHashMap<String,String>();
				for(String key:keys){
					m.find();
					String value=m.group(1);
					if(!"".equals(value)) m.find();
					obj.put(key, value);
				}
				data.add(obj);
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
		return data;		
	}

	/**
	 * Store as csv file
	 * 
	 * @param data
	 * @param outPath
	 * @param outFile
	 */
	public static void save(ArrayList<HashMap<String, String>> data, String outPath, String outFile) { save(data, outPath, outFile, null); }
	public static void save(ArrayList<HashMap<String, String>> data, String outPath, String outFile, Comparator<String> keysComparator) {
		try {
			if(data.size()==0){
				System.err.println("Cannot save CSV file: Empty dataset.");
				return;
			}

			new File(outPath).mkdirs();
			File f=new File(outPath+outFile);
			if(f.exists()) f.delete();
			BufferedWriter bw = new BufferedWriter(new FileWriter(f, true));

			//write header
			Collection<String> keys = data.get(0).keySet(); int i=0;
			if(keysComparator != null){
				keys = new ArrayList<String>(keys);
				Collections.sort((ArrayList<String>)keys, keysComparator);
			}
			for(String key:keys ){
				bw.write(key);
				if(i<keys.size()-1) bw.write(",");
				i++;
			}
			bw.write("\n");

			//write data
			for(HashMap<String, String> obj : data){
				i=0;
				for(String key : keys){
					String value = obj.get(key);
					bw.write(value==null?"":value);
					if(i<keys.size()-1) bw.write(",");
					i++;
				}
				bw.write("\n");
			}
			bw.close();
		} catch (Exception e) {e.printStackTrace();}
	}

	/**
	 * Get all unique values of a column.
	 * 
	 * @param data
	 * @param key
	 * @param print
	 * @return
	 */
	public static HashSet<String> getUniqueValues(ArrayList<HashMap<String, String>> data, String key, boolean print) {
		HashSet<String> values = new HashSet<String>();
		for(HashMap<String, String> obj : data)
			values.add(obj.get(key));
		if(print){
			System.out.println(key + " " + values.size()+" values");
			System.out.println(values);
		}
		return values;
	}

}
