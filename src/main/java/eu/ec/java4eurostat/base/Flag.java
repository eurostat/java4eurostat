/**
 * 
 */
package eu.ec.java4eurostat.base;

import java.util.HashMap;

/**
 * 
 * Flags are codes added to the data and defining a specific characteristic.
 * http://ec.europa.eu/eurostat/data/database/information
 * 
 * @author julien Gaffuri
 *
 */
public class Flag {

	public enum FlagType { 
		b,
		c,
		d,
		e,
		f,
		i,
		n,
		p,
		r,
		s,
		u,
		x,
		z
	}

	public static HashMap<String, FlagType> code;
	public static HashMap<FlagType, String> text;
	static{
		code = new HashMap<String, FlagType>();
		code.put("b", FlagType.b);
		code.put("c", FlagType.c);
		code.put("d", FlagType.d);
		code.put("e", FlagType.e);
		code.put("f", FlagType.f);
		code.put("i", FlagType.i);
		code.put("n", FlagType.n);
		code.put("p", FlagType.p);
		code.put("r", FlagType.r);
		code.put("s", FlagType.s);
		code.put("u", FlagType.u);
		code.put("x", FlagType.x);
		code.put("z", FlagType.z);

		text = new HashMap<FlagType, String>();
		text.put(FlagType.b, "break in time series");
		text.put(FlagType.c, "confidential");
		text.put(FlagType.d, "definition differs, see metadata");
		text.put(FlagType.e, "estimated");
		text.put(FlagType.f, "forecast");
		text.put(FlagType.i, "see metadata (phased out) ");
		text.put(FlagType.n, "not significant ");
		text.put(FlagType.p, "provisional");
		text.put(FlagType.r, "revised");
		text.put(FlagType.s, "Eurostat estimate (phased out)");
		text.put(FlagType.u, "low reliability");
		text.put(FlagType.x, "under embargo");
		text.put(FlagType.z, "not applicable ");
	}

}
