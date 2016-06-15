/**
 * 
 */
package eu.ec.java4eurostat.base;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

/**
 * @author julien Gaffuri
 *
 */
public class Stat {
	//dim label - dim value
	public HashMap<String,String> dims = new HashMap<String,String>();
	//value
	public double value;

	//flags
	private HashSet<Flag.FlagType> flags = null;
	public boolean addFlag(Flag.FlagType flag){
		if(flags == null) flags = new HashSet<Flag.FlagType>();
		return flags.add(flag);
	}
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

}
