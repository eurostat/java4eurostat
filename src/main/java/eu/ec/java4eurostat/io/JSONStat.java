/**
 * 
 */
package eu.ec.java4eurostat.io;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import eu.ec.java4eurostat.base.Selection.Criteria;
import eu.ec.java4eurostat.base.Stat;
import eu.ec.java4eurostat.base.StatsHypercube;

/**
 * @author julien Gaffuri
 *
 */
public class JSONStat {

	public static StatsHypercube load(String data) { return load(data, true); }
	public static StatsHypercube load(String data, boolean statusAsEurostatFlags) { return load(data, statusAsEurostatFlags, null); }
	public static StatsHypercube load(String data, boolean statusAsEurostatFlags, Criteria ssc) {
		//System.out.println(data);
		StatsHypercube hc = new StatsHypercube();
		JSONObject obj = new JSONObject(data);

		//read dimension labels
		JSONObject dimensions = obj.getJSONObject("dimension");
		for(Object o : dimensions.keySet()) hc.dimLabels.add(o.toString());

		//read dimension sizes
		JSONArray sizes = obj.getJSONArray("size");

		//read dimension values
		HashMap<String,JSONObject> dimValues = new HashMap<String,JSONObject>();
		for(int coordI=0; coordI<sizes.length(); coordI++){
			String dimLabel = obj.getJSONArray("id").getString(coordI);
			JSONObject ind = dimensions.getJSONObject(dimLabel).getJSONObject("category").getJSONObject("index");
			ind = invert(ind);
			dimValues.put(dimLabel, ind);
		}

		//compute number of values
		int nb = 1;
		for(int i=0; i<sizes.length(); i++) nb = nb*sizes.getInt(i);

		JSONObject values = obj.getJSONObject("value"); //TODO This might be an array
		JSONObject status = obj.getJSONObject("status"); //TODO This might be a lot of other stuff...
		for(int i=0; i<nb; i++){
			Stat s = new Stat();

			//get value. If none, continue.
			try { s.value = values.getDouble(""+i); } catch (JSONException e) { continue; }

			//get status/flags
			if(statusAsEurostatFlags)
				try { s.addAllFlags(status.getString(""+i)); } catch (Exception e) {}

			//compute value coordinates in the hypercube
			int[] coords = new int[sizes.length()];
			int i_=i, nb_=nb;
			for(int coordI=0; coordI<coords.length; coordI++){
				nb_ = nb_ / sizes.getInt(coordI);
				coords[coordI] = i_;
				if(nb_>0) coords[coordI] /= nb_;
				i_ -= coords[coordI] * nb_;
			}

			//assign dimension values depending on coordinates
			for(int coordI=0; coordI<coords.length; coordI++){
				String dimLabel = obj.getJSONArray("id").getString(coordI);
				String dimValue = dimValues.get(dimLabel).getString(""+coords[coordI]);
				s.dims.put(dimLabel, dimValue);
			}

			if(ssc!=null && !ssc.keep(s)) continue;

			hc.stats.add(s);
		}

		return hc;
	}

	private static JSONObject invert(JSONObject obj) {
		JSONObject out = new JSONObject();
		for(Object key : obj.keySet())
			out.put(obj.get(key.toString()).toString(), key);
		return out;
	}

	public static void main(String[] args) {
		//StatsHypercube hc = JSONStat.load( IOUtil.getDataFromURL("http://ec.europa.eu/eurostat/wdds/rest/data/v2.1/json/en/prc_hicp_midx?geo=AT&unit=I15&coicop=CP00&sinceTimePeriod=2016M01") );
		//hc.printInfo();
		//for(Stat s : hc.stats) System.out.println(s);

		//EurobaseIO.getData("prc_hicp_cow").printInfo();
	}

	//TODO save

}
