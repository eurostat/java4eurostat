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
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.StringTokenizer;

/**
 * @author julien Gaffuri
 *
 */
public class DicUtil {

	public static HashMap<String,String> load(String filePath) { return load(filePath, "\t"); }
	public static HashMap<String,String> load(String filePath, String sep) {
		HashMap<String,String> data = new HashMap<String,String>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(filePath));
			data = load(br, sep);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try { if (br != null) br.close(); } catch (Exception ex) { ex.printStackTrace(); }
		}
		return data;
	}

	public static HashMap<String,String> loadFromURL(String url) { return loadFromURL(url, "\t"); }
	public static HashMap<String,String> loadFromURL(String url, String sep) {
		HashMap<String,String> data = new HashMap<String,String>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new URL(url).openConnection().getInputStream()));
			data = load(br, sep);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try { if (br != null) br.close(); } catch (Exception ex) { ex.printStackTrace(); }
		}
		return data;
	}

	public static HashMap<String,String> load(BufferedReader br, String sep) {
		HashMap<String,String> data = new HashMap<String,String>();
		try {
			//read data
			String line;
			while ((line = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line, sep);
				data.put(st.nextToken(), st.nextToken());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try { if (br != null) br.close(); } catch (Exception ex) { ex.printStackTrace(); }
		}
		return data;
	}

	public static void save(HashMap<String, String> data, String filePath) {
		BufferedWriter bw = null;
		try {
			File f = new File(filePath);
			if(f.exists()) f.delete();
			bw = new BufferedWriter(new FileWriter(f, true));
			for(Entry<String,String> e : data.entrySet()) bw.write(e.getKey()+"\t"+e.getValue()+"\n");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { if (bw != null) bw.close(); } catch (Exception ex) { ex.printStackTrace(); }
		}
	}

}
