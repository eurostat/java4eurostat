/**
 * 
 */
package eu.europa.ec.eurostat.java4eurostat.base;

/**
 * Classes and interface for data selection/filtering/dicing/slicing
 * 
 * @author julien Gaffuri
 *
 */
public class Selection {
	//TODO define selection grammar?

	/**
	 * A generic selection criteria to specify whether a statistical value should be kept of not.
	 * Ex: All stats with gender=male, with value=0, with country starting with a 'A', etc.
	 * 
	 */
	public interface Criteria {
		boolean keep(Stat stat);
	}

	/** Selection criteria on values */
	public static class ValueEqualTo implements Criteria {
		double value;
		public ValueEqualTo(double value){ this.value = value; }
		public boolean keep(Stat stat) { return stat.value == value; }
	}
	/** Selection criteria on values */
	public static class ValueDifferentFrom implements Criteria {
		double value;
		public ValueDifferentFrom(double value){ this.value = value; }
		public boolean keep(Stat stat) { return stat.value != value; }
	}
	/** Selection criteria on values */
	public static class ValueGreaterThan implements Criteria {
		double value;
		public ValueGreaterThan(double value){ this.value = value; }
		public boolean keep(Stat stat) { return stat.value > value; }
	}
	/** Selection criteria on values */
	public static class ValueLowerThan implements Criteria {
		double value;
		public ValueLowerThan(double value){ this.value = value; }
		public boolean keep(Stat stat) { return stat.value < value; }
	}
	/** Selection criteria on values */
	public static class ValueGreaterOrEqualThan implements Criteria {
		double value;
		public ValueGreaterOrEqualThan(double value){ this.value = value; }
		public boolean keep(Stat stat) { return stat.value >= value; }
	}
	/** Selection criteria on values */
	public static class ValueLowerOrEqualThan implements Criteria {
		double value;
		public ValueLowerOrEqualThan(double value){ this.value = value; }
		public boolean keep(Stat stat) { return stat.value <= value; }
	}



	/** Selection criteria for statistics having dimension values */
	public static class DimValueEqualTo implements Criteria {
		String[] dimLabelValues;
		public DimValueEqualTo(String... dimLabelValues){ this.dimLabelValues = dimLabelValues; }

		public boolean keep(Stat stat) {
			for(int i=0; i<dimLabelValues.length; i+=2)
				if(!dimLabelValues[i+1].equals(stat.dims.get(dimLabelValues[i]))) return false;
			return true;
		}
	}
	/** Selection criteria for statistics having dimension values */
	public static class DimValueDifferentFrom implements Criteria {
		String[] dimLabelValues;
		public DimValueDifferentFrom(String... dimLabelValues){ this.dimLabelValues = dimLabelValues; }

		public boolean keep(Stat stat) {
			for(int i=0; i<dimLabelValues.length; i+=2)
				if(dimLabelValues[i+1].equals(stat.dims.get(dimLabelValues[i]))) return false;
			return true;
		}
	}
	/** Selection criteria on statistics dimension values */
	public static class DimValueGreaterThan implements Criteria {
		String dimLabel; double dimValue;
		public DimValueGreaterThan(String dimLabel, double dimValue){ this.dimLabel = dimLabel; this.dimValue = dimValue; }

		public boolean keep(Stat stat) {
			try { return Double.parseDouble(stat.dims.get(dimLabel)) > this.dimValue; } catch (NumberFormatException e) { return false; }
		}
	}
	/** Selection criteria on statistics dimension values */
	public static class DimValueLowerThan implements Criteria {
		String dimLabel; double dimValue;
		public DimValueLowerThan(String dimLabel, double dimValue){ this.dimLabel = dimLabel; this.dimValue = dimValue; }

		public boolean keep(Stat stat) {
			try { return Double.parseDouble(stat.dims.get(dimLabel)) < this.dimValue; } catch (NumberFormatException e) { return false; }
		}
	}
	/** Selection criteria on statistics dimension values */
	public static class DimValueGreaterOrEqualThan implements Criteria {
		String dimLabel; double dimValue;
		public DimValueGreaterOrEqualThan(String dimLabel, double dimValue){ this.dimLabel = dimLabel; this.dimValue = dimValue; }

		public boolean keep(Stat stat) {
			try { return Double.parseDouble(stat.dims.get(dimLabel)) >= this.dimValue; } catch (NumberFormatException e) { return false; }
		}
	}
	/** Selection criteria on statistics dimension values */
	public static class DimValueLowerOrEqualThan implements Criteria {
		String dimLabel; double dimValue;
		public DimValueLowerOrEqualThan(String dimLabel, double dimValue){ this.dimLabel = dimLabel; this.dimValue = dimValue; }

		public boolean keep(Stat stat) {
			try { return Double.parseDouble(stat.dims.get(dimLabel)) <= this.dimValue; } catch (NumberFormatException e) { return false; }
		}
	}

	/**
	 *  A composite data criteria (or)
	 */
	public static class Or implements Criteria {
		Criteria[] cri;
		public Or(Criteria... cri){ this.cri = cri; }

		public boolean keep(Stat stat) {
			for(int i=0; i<cri.length; i++) if(cri[i].keep(stat)) return true;
			return false;
		}
	}

	/**
	 *  A composite data criteria (and)
	 */
	public static class And implements Criteria {
		Criteria[] cri;
		public And(Criteria... cri){ this.cri = cri; }
		public boolean keep(Stat stat) {
			for(int i=0; i<cri.length; i++) if(!cri[i].keep(stat)) return false;
			return true;
		}
	}

	/**
	 *  A composite data criteria (not)
	 */
	public static class Not implements Criteria {
		Criteria cri;
		public Not(Criteria cri){ this.cri = cri; }

		public boolean keep(Stat stat) {
			return !cri.keep(stat);
		}
	}

}
