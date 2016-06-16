/**
 * 
 */
package eu.ec.java4eurostat.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author julien Gaffuri
 *
 */
public class StatsIndex {

	/**
	 * The index recusrssive element: Can be:
	 *    - HashMap<String, StatsIndex>
	 *    - Collection<Stat>
	 *    - Stat
	 * depending on the case.
	 */
	private Object data;

	/**
	 * Build an index depending on the ordered list of dimension labels.
	 * 
	 * @param hc
	 * @param dimLabels
	 */
	public StatsIndex(StatsHypercube hc, String... dimLabels){
		if(dimLabels.length == 0){
			if(hc.stats.size()==0)
				data = null;
			else if(hc.stats.size()==1)
				data = hc.stats.iterator().next();
			else
				data = new HashSet<Stat>(hc.stats);
		} else {
			//TODO improve with in depth-first construction

			//partition hypercube by dimension values
			String dimLabel = dimLabels[0];
			HashMap<String, StatsHypercube> hcP = new HashMap<String, StatsHypercube>();
			for(Stat s : hc.stats){
				String dimValue = s.dims.get(dimLabel);
				StatsHypercube hcP_ = hcP.get(dimValue);
				if(hcP_ == null) {
					hcP_ = new StatsHypercube();
					hcP_.dimLabels = hc.dimLabels;
					hcP.put(dimValue, hcP_);
				}
				hcP_.stats.add(s);
			}

			//build index recursively from hypercube partition
			HashMap<String, StatsIndex> data_ = new HashMap<String, StatsIndex>();
			data = data_;
			String[] dimLabelsOut = new String[dimLabels.length-1]; for(int i=1; i<dimLabels.length; i++) dimLabelsOut[i-1] = dimLabels[i];
			for(Entry<String, StatsHypercube> e : hcP.entrySet()){
				data_.put(e.getKey(), new StatsIndex(e.getValue(), dimLabelsOut));
			}

			//HashSet<String> dimValues = hc.getDimValues(dimLabel);
			//recursive construction
			//for(String dimValue : dimValues)
			//	data_.put(dimValue, new StatsIndex(hc.select(dimLabel, dimValue), dimLabelsOut));
		}
	}

	/**
	 * Retrieve a subindex.
	 * 
	 * @param dimLabels
	 * @return
	 */
	public StatsIndex getSubIndex(String... dimLabels){
		StatsIndex out = this;
		for(String label : dimLabels){
			if(out.data instanceof Collection || out.data instanceof Stat){
				System.out.println("Problem in index values retrieval. Too many dimension labels? " + dimLabels);
				return null;
			}
			HashMap<String, StatsIndex> data_ = (HashMap<String, StatsIndex>)out.data;
			if(data_.get(label) == null) return null;
			out = data_.get(label);
		}
		return out;
	}

	/**
	 * @param dimLabels dim labels vector
	 * @return A stat collection, or null in case of impossibility
	 */
	public Collection<Stat> getCollection(String... dimLabels){
		StatsIndex si = getSubIndex(dimLabels);
		if(si==null) return null;
		else if(si.data instanceof Collection) return (Collection<Stat>)si.data;
		else if(si.data instanceof Stat){
			HashSet<Stat> col = new HashSet<Stat>();
			col.add((Stat)si.data);
			return col;
		}
		//build output collection recursivelly
		Collection<StatsIndex> sis = ((HashMap<String, StatsIndex>)si.data).values();
		HashSet<Stat> col = new HashSet<Stat>();
		for(StatsIndex si_ : sis) col.addAll(si_.getCollection());
		return col;
	}

	/**
	 * Return a subindex as an hypercube object.
	 * @param dims
	 * @param dimLabels
	 * @return
	 */
	public StatsHypercube getStatsHypercube(Collection<String> dims, String... dimLabels){
		StatsHypercube sh = new StatsHypercube();
		sh.stats = getCollection(dimLabels);
		sh.dimLabels.addAll(dims);
		return sh;
	}

	/**
	 * 
	 * @param dimLabels dim labels vector
	 * @return A stat, or null in case of impossibility
	 */
	public Stat getSingleStat(String... dimLabels){
		StatsIndex si = getSubIndex(dimLabels);
		if(si==null) return null;
		if(si.data instanceof Stat) return ((Stat)si.data);
		System.err.println("Unexpected data in statindex: Single value expected instead of " + si.data);
		return null;
	}

	/**
	 * @param dimLabels dim labels vector
	 * @return A single value, or NaN in case of impossibility
	 */
	public double getSingleValue(String... dimLabels){
		Stat s = getSingleStat(dimLabels);
		if(s == null) return Double.NaN;
		return s.value;
	}

	/**
	 * @param dimLabels dim labels vector
	 * @return A flagged single value, or NaN in case of impossibility
	 */
	public String getSingleValueFlagged(String... dimLabels){
		Stat s = getSingleStat(dimLabels);
		if(s == null) return null;
		return s.getValueFlagged();
	}

	/**
	 * @param dimLabels
	 * @return The index keys (which are dimension values).
	 */
	public Set<String> getKeys(String... dimLabels){
		StatsIndex si = getSubIndex(dimLabels);
		if(si == null) return null;
		if(data instanceof HashMap) return ((HashMap<String, StatsIndex>)si.data).keySet();
		return null;
	}

	/**
	 * @param dimLabels
	 * @return The index keys (which are dimension values), as a sorted list.
	 */
	public List<String> getKeysAsList(String... dimLabels){
		Set<String> set = getKeys(dimLabels);
		if(set == null) return null;
		ArrayList<String> list = new ArrayList<String>(set);
		Collections.sort(list);
		return list;
	}


	/**
	 * Print the index structure.
	 */
	public void print(){ print(0); }

	/**
	 * Print the index structure, with indentation.
	 */
	public void print(int indent){
		if(data instanceof Stat){
			for(int i=0;i<indent;i++) System.out.print("\t");
			System.out.println(((Stat)data).value);
		} else if(data instanceof Collection){
			for(int i=0;i<indent;i++) System.out.print("\t");
			System.out.println("#="+((Collection)data).size());
		} else {
			HashMap<String, StatsIndex> data_ = (HashMap<String, StatsIndex>) data;
			for(Entry<String, StatsIndex> e : data_.entrySet()){
				for(int i=0;i<indent;i++) System.out.print("\t");
				System.out.print(e.getKey());
				if(e.getValue().data instanceof Collection){
					Collection<Stat> col = (Collection<Stat>)e.getValue().data;
					if(col.size()==1) System.out.println(" "+col.iterator().next().value);
					else System.out.println(" "+col.size());
				} else {
					System.out.println();
					e.getValue().print(indent+1);
				}
			}
		}
	}

}
