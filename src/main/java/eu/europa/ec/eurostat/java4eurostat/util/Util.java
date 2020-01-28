package eu.europa.ec.eurostat.java4eurostat.util;

import java.text.DateFormat;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

/**
 * 
 * Some general purpose functions.
 * 
 * @author julien Gaffuri
 *
 */
public class Util {
	/**
	 * The "yyyy-MM-dd HH:mm:ss" date format.
	 */
	public static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/**
	 * Double[] to double[]...
	 * 
	 * @param in
	 * @return
	 */
	public static double[] toDoubleArray(Double[] in) {
		double[] out = new double[in.length];
		for(int i=0; i<in.length; i++) out[i]=in[i].doubleValue();
		return out;
	}


	/**
	 * Print progress in %
	 * 
	 * @param nbDone
	 * @param nbTot
	 */
	public static void printProgress(int nbDone, int nbTot) {
		int ratio = 100*nbDone/nbTot;
		int ratioP = 100*(nbDone-1)/nbTot;
		if(ratio != ratioP) System.out.println(ratio + "% done");
	}

	/**
	 * Round a number
	 * 
	 * @param x
	 * @param decimalNB
	 * @return
	 */
	public static double round(double x, int decimalNB) {
		double pow = Math.pow(10, decimalNB);
		return ( (int)(x * pow + 0.5) ) / pow;
	}



	/**
	 * 
	 */
	public static final Pattern DIACRITICS_AND_FRIENDS = Pattern.compile("[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+");

	/**
	 * @param str
	 * @return
	 */
	public static String stripDiacritics(String str) {
		String str_ = str;
		str_ = Normalizer.normalize(str_, Normalizer.Form.NFD);
		str_ = DIACRITICS_AND_FRIENDS.matcher(str_).replaceAll("");
		return str_;
	}

	/**
	 * @param str
	 * @return
	 */
	public static String stripWeirdCaracters(String str) {
		String string = Normalizer.normalize(str, Normalizer.Form.NFD);
		return string.replaceAll("[^\\p{ASCII}]", "");
	}

	/**
	 * Check if a piece of text is an integer.
	 * @param str
	 * @return
	 */
	public static boolean isInteger(String str) {
		if (str == null) return false;
		int length = str.length();
		if (length == 0) return false;

		int i = 0;
		if (str.charAt(0) == '-') {
			if (length == 1) {
				return false;
			}
			i = 1;
		}
		for (; i < length; i++) {
			char c = str.charAt(i);
			if (c < '0' || c > '9') {
				return false;
			}
		}
		return true;
	}

	/**
	 * Check if a text represent a numerical value.
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isNumeric(String s){
		try { Double.parseDouble(s); }
		catch(@SuppressWarnings("unused") NumberFormatException e) { return false; }
		return true;
	}

	/**
	 * Count the number of occurences of a character in a string
	 * 
	 * @param st
	 * @param thing
	 * @return
	 */
	public static int getOccurencesNumber(String st, String thing){
		return st.length() - st.replace(thing, "").length();
	}

	/**
	 * @param map
	 * @return
	 */
	public static HashMap<String,String> reverseMap(Map<String,String> map){
		HashMap<String,String> mapOut = new HashMap<>();
		for(Entry<String,String> e : map.entrySet())
			mapOut.put(e.getValue(), e.getKey());
		return mapOut;
	}


	/**
	 * @param d
	 * @param nbDec
	 * @return
	 */
	public static String addTrailingZeros(double d, int nbDec) {
		String d_ = Double.toString(d);
		if(d_.contains("E")) return d_;
		if(!d_.contains(".")) d_ += ".";
		int nb = d_.split("\\.")[1].length();
		if(nb>=nbDec) return d_;
		for(int i=0; i<nbDec-nb; i++) d_ += "0";
		return d_;
	}

}
