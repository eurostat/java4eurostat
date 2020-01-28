/**
 * 
 */
package eu.europa.ec.eurostat.java4eurostat.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author julien Gaffuri
 *
 */
public class StatsIndex {
	public final static Logger LOGGER = LogManager.getLogger(StatsIndex.class.getName());

	/**
	 * The index recurssive element: Can be:
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
				this.data = null;
			else if(hc.stats.size()==1)
				this.data = hc.stats.iterator().next();
			else
				this.data = new HashSet<>(hc.stats);
		} else {
			//TODO improve with in depth-first construction

			//partition hypercube by dimension values
			String dimLabel = dimLabels[0];
			HashMap<String, StatsHypercube> hcP = new HashMap<>();
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
			HashMap<String, StatsIndex> data_ = new HashMap<>();
			this.data = data_;
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
				LOGGER.warn("Problem in index values retrieval. Too many dimension labels? " + dimLabels);
				return null;
			}
			@SuppressWarnings("unchecked")
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
		else if(si.data instanceof Collection)
			return (Collection<Stat>)si.data;
		else if(si.data instanceof Stat){
			HashSet<Stat> col = new HashSet<>();
			col.add((Stat)si.data);
			return col;
		}
		//build output collection recursivelly
		@SuppressWarnings("unchecked")
		Collection<StatsIndex> sis = ((HashMap<String, StatsIndex>)si.data).values();
		HashSet<Stat> col = new HashSet<>();
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
		LOGGER.error("Unexpected data in statindex: Single value expected instead of " + si.data);
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
		if(si == null)
			return null;
		if(this.data instanceof HashMap)
			return ((HashMap<String, StatsIndex>)si.data).keySet();
		return null;
	}

	/**
	 * @param dimLabels
	 * @return The index keys (which are dimension values), as a sorted list.
	 */
	public List<String> getKeysAsList(String... dimLabels){
		Set<String> set = getKeys(dimLabels);
		if(set == null) return null;
		ArrayList<String> list = new ArrayList<>(set);
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
		if(this.data instanceof Stat){
			for(int i=0;i<indent;i++) System.out.print("\t");
			System.out.println(((Stat)this.data).value);
		} else if(this.data instanceof Collection){
			for(int i=0;i<indent;i++) System.out.print("\t");
			System.out.println("#="+((Collection<?>)this.data).size());
		} else {
			@SuppressWarnings("unchecked")
			HashMap<String, StatsIndex> data_ = (HashMap<String, StatsIndex>) this.data;
			for(Entry<String, StatsIndex> e : data_.entrySet()){
				for(int i=0;i<indent;i++) System.out.print("\t");
				System.out.print(e.getKey());
				if(e.getValue().data instanceof Collection){
					@SuppressWarnings("unchecked")
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

	/**
	 * @return The leaves of the index, as collections
	 */
	public Collection<Collection<Stat>> getLeaves() {
		Collection<Collection<Stat>> out = new ArrayList<>();
		if(this.data instanceof Stat) {
			ArrayList<Stat> s = new ArrayList<>();
			s.add((Stat)this.data);
			out.add(s);
		} else if(this.data instanceof Collection) {
			out.add((Collection<Stat>) this.data);
		} else if(this.data instanceof HashMap) {
			@SuppressWarnings("unchecked")
			HashMap<String, StatsIndex> data_ = (HashMap<String, StatsIndex>) this.data;
			for(StatsIndex si : data_.values())
				out.addAll(si.getLeaves());
		} else
			LOGGER.error("Unexpected type for statindex data: " + this.data.getClass().getSimpleName());
		return out;
	}

}
