/**
 * 
 */
package eu.ec.java4eurostat.io;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import eu.ec.java4eurostat.base.Stat;
import eu.ec.java4eurostat.base.StatsHypercube;

/**
 * @author julien Gaffuri
 *
 */
public class JSONStat {

	public static StatsHypercube get(String data) {
		//System.out.println(data);
		StatsHypercube hc = new StatsHypercube();
		JSONObject obj = new JSONObject(data);

		//read dimension labels
		JSONObject dimensions = obj.getJSONObject("dimension");
		for(Object o : dimensions.keySet()) hc.dimLabels.add(o.toString());

		//read dimension sizes
		JSONArray sizes = obj.getJSONArray("size");
		//System.out.println(sizes); [6,35,21]
		int nb = 1;
		for(int i=0; i<sizes.length(); i++) nb = nb*sizes.getInt(i);

		JSONObject values = obj.getJSONObject("value"); //TODO could be an array
		for(int i=0; i<nb; i++){
			Stat s = new Stat();
			try { s.value = values.getDouble(""+i); } catch (JSONException e) { continue; }

			int[] coords = new int[sizes.length()];
			int i_=i, nb_=nb;
			for(int coordI=0; coordI<coords.length; coordI++){
				nb_ = nb_ / sizes.getInt(coordI);
				coords[coordI] = i_;
				if(nb_>0) coords[coordI] /= nb_;
				i_ -= coords[coordI] * nb_;
			}
			//System.out.println( coords[0]+" " + coords[1]+" " + coords[2]+" " );

			for(int coordI=0; coordI<coords.length; coordI++){
				//TODO read dimension labels and values
				//coords[coordI]
				String dimLabel = "";
				String dimValue = "";
				s.dims.put(dimLabel, dimValue);
			}
			hc.stats.add(s);
		}

		//TODO read status for flags

		hc.printInfo();
		return hc;
	}


	public static void main(String[] args) {
		EurobaseIO.getData("prc_hicp_cow");
	}

}
