/**
 * 
 */
package eu.ec.java4eurostat.base;

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
		@Override
		public boolean keep(Stat stat) { return stat.value == value; }
	}
	/** Selection criteria on values */
	public static class ValueGreaterThan implements Criteria {
		double value;
		public ValueGreaterThan(double value){ this.value = value; }
		@Override
		public boolean keep(Stat stat) { return stat.value > value; }
	}
	/** Selection criteria on values */
	public static class ValueLowerThan implements Criteria {
		double value;
		public ValueLowerThan(double value){ this.value = value; }
		@Override
		public boolean keep(Stat stat) { return stat.value < value; }
	}
	/** Selection criteria on values */
	public static class ValueGreaterOrEqualThan implements Criteria {
		double value;
		public ValueGreaterOrEqualThan(double value){ this.value = value; }
		@Override
		public boolean keep(Stat stat) { return stat.value >= value; }
	}
	/** Selection criteria on values */
	public static class ValueLowerOrEqualThan implements Criteria {
		double value;
		public ValueLowerOrEqualThan(double value){ this.value = value; }
		@Override
		public boolean keep(Stat stat) { return stat.value <= value; }
	}



	/** Selection criteria for statistics having dimension values */
	public static class DimValuesEqualTo implements Criteria {
		String[] dimLabelValues;
		public DimValuesEqualTo(String... dimLabelValues){ this.dimLabelValues = dimLabelValues; }

		@Override
		public boolean keep(Stat stat) {
			for(int i=0; i<dimLabelValues.length/2; i+=2)
				if(!dimLabelValues[i+1].equals(stat.dims.get(dimLabelValues[i]))) return false;
			return true;
		}
	}
	/** Selection criteria on statistics dimension values */
	public static class DimValuesGreaterThan implements Criteria {
		String dimLabel; double dimValue;
		public DimValuesGreaterThan(String dimLabel, double dimValue){ this.dimLabel = dimLabel; this.dimValue = dimValue; }

		@Override
		public boolean keep(Stat stat) {
			try { return Double.parseDouble(stat.dims.get(dimLabel)) > this.dimValue; } catch (NumberFormatException e) { return false; }
		}
	}
	/** Selection criteria on statistics dimension values */
	public static class DimValuesLowerThan implements Criteria {
		String dimLabel; double dimValue;
		public DimValuesLowerThan(String dimLabel, double dimValue){ this.dimLabel = dimLabel; this.dimValue = dimValue; }

		@Override
		public boolean keep(Stat stat) {
			try { return Double.parseDouble(stat.dims.get(dimLabel)) < this.dimValue; } catch (NumberFormatException e) { return false; }
		}
	}
	/** Selection criteria on statistics dimension values */
	public static class DimValuesGreaterOrEqualThan implements Criteria {
		String dimLabel; double dimValue;
		public DimValuesGreaterOrEqualThan(String dimLabel, double dimValue){ this.dimLabel = dimLabel; this.dimValue = dimValue; }

		@Override
		public boolean keep(Stat stat) {
			try { return Double.parseDouble(stat.dims.get(dimLabel)) >= this.dimValue; } catch (NumberFormatException e) { return false; }
		}
	}
	/** Selection criteria on statistics dimension values */
	public static class DimValuesLowerOrEqualThan implements Criteria {
		String dimLabel; double dimValue;
		public DimValuesLowerOrEqualThan(String dimLabel, double dimValue){ this.dimLabel = dimLabel; this.dimValue = dimValue; }

		@Override
		public boolean keep(Stat stat) {
			try { return Double.parseDouble(stat.dims.get(dimLabel)) <= this.dimValue; } catch (NumberFormatException e) { return false; }
		}
	}

	/**
	 *  A composite data criteria (or)
	 */
	public static class Or implements Criteria {
		Criteria[] cri;
		public Or(Criteria[] cri){ this.cri = cri; }
		@Override
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
		public And(Criteria[] cri){ this.cri = cri; }
		@Override
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
		@Override
		public boolean keep(Stat stat) {
			return !cri.keep(stat);
		}
	}

}
