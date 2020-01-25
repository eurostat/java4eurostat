/**
 * 
 */
package eu.europa.ec.eurostat.java4eurostat.base;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

/**
 * 
 * A statistical value, described by dimension-coordinates in the hypercube, and possibly flags.
 * 
 * @author julien Gaffuri
 *
 */
public class Stat {

	/**
	 * 
	 */
	public Stat(){}

	/**
	 * @param value The statistical value
	 * @param dims The dimension data: list of pairs: dimLabel1,dimValue1,dimLabel2,dimValue2,...
	 */
	public Stat(double value, String... dims){
		this();
		this.value = value;
		for(int i=0; i<dims.length; i=i+2) this.dims.put(dims[i], dims[i+1]);
	}

	/**
	 * Clone a stat object
	 */
	public Stat(Stat s){
		this();
		this.value = s.value;
		for(String key : s.dims.keySet()) this.dims.put(key, s.dims.get(key));
	}

	/**
	 * The value
	 */
	public double value;

	/**
	 * The dimensions
	 * Ex: gender - male ; time - 2015 ; country - PL
	 */
	public HashMap<String,String> dims = new HashMap<String,String>();

	/**
	 * The flags providing some metadata information
	 */
	private HashSet<Flag.FlagType> flags = null;

	/**
	 * Add a flag to the statistical value.
	 * @param flag
	 * @return
	 */
	public boolean addFlag(Flag.FlagType flag){
		if(flags == null) flags = new HashSet<Flag.FlagType>();
		return flags.add(flag);
	}

	/**
	 * Add flags to the statistical value.
	 * @param flags
	 */
	public void addAllFlags(String flags) {
		for (int i=0; i<flags.length(); i++){
			Flag.FlagType flag = Flag.code.get(""+flags.charAt(i));
			if(flag == null) {
				System.err.println("Unexpected flag: "+flags.charAt(i)+" for "+this);
				continue;
			}
			addFlag(flag);
		}
	}
	public boolean removeFlag(Flag.FlagType flag){
		if(flags == null) return false;
		return flags.remove(flag);
	}
	public boolean isFlagged(Flag.FlagType flag){
		if(flags == null) return false;
		return flags.contains(flag);
	}

	public String getFlags(){
		if(flags == null || flags.size()==0) return "";
		StringBuffer sb = new StringBuffer();
		for(Flag.FlagType f : flags) sb.append(f);
		return sb.toString();
	}

	public String getValueFlagged(){
		return value + getFlags();
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for(Entry<String,String> dim:dims.entrySet())
			sb.append(dim.getKey()).append(":").append(dim.getValue()).append(", ");
		sb.append(getValueFlagged());
		return sb.toString();
	}


	/**
	 * Return an array of dimension values.
	 * @param dimLabels The dimension labels.
	 * @return
	 */
	public String[] getDimValues(String[] dimLabels) {
		String[] dimValues = new String[dimLabels.length];
		for(int i=0; i<dimLabels.length; i++) dimValues[i] = dims.get(dimLabels[i]);
		return dimValues;
	}
}
