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
		/**
		 * The method specifying when a stat has to be kept.
		 * 
		 * @param stat
		 * @return
		 */
		boolean keep(Stat stat);
	}

	/** Selection criteria on values */
	public static class ValueEqualTo implements Criteria {
		private double value;
		/**
		 * @param value
		 */
		/* */
		public ValueEqualTo(double value){ this.value = value; }
		@Override
		public boolean keep(Stat stat) { return stat.value == this.value; }
	}
	/** Selection criteria on values */
	public static class ValueDifferentFrom implements Criteria {
		private double value;
		/**
		 * @param value
		 */
		public ValueDifferentFrom(double value){ this.value = value; }
		@Override
		public boolean keep(Stat stat) { return stat.value != this.value; }
	}
	/** Selection criteria on values */
	public static class ValueGreaterThan implements Criteria {
		private double value;
		/**
		 * @param value
		 */
		public ValueGreaterThan(double value){ this.value = value; }
		@Override
		public boolean keep(Stat stat) { return stat.value > this.value; }
	}
	/** Selection criteria on values */
	public static class ValueLowerThan implements Criteria {
		private double value;
		/**
		 * @param value
		 */
		public ValueLowerThan(double value){ this.value = value; }
		@Override
		public boolean keep(Stat stat) { return stat.value < this.value; }
	}
	/** Selection criteria on values */
	public static class ValueGreaterOrEqualThan implements Criteria {
		private double value;
		/**
		 * @param value
		 */
		public ValueGreaterOrEqualThan(double value){ this.value = value; }
		@Override
		public boolean keep(Stat stat) { return stat.value >= this.value; }
	}
	/** Selection criteria on values */
	public static class ValueLowerOrEqualThan implements Criteria {
		private double value;
		/**
		 * @param value
		 */
		public ValueLowerOrEqualThan(double value){ this.value = value; }
		@Override
		public boolean keep(Stat stat) { return stat.value <= this.value; }
	}



	/** Selection criteria for statistics having dimension values */
	public static class DimValueEqualTo implements Criteria {
		private String[] dimLabelValues;
		/**
		 * @param dimLabelValues
		 */
		public DimValueEqualTo(String... dimLabelValues){ this.dimLabelValues = dimLabelValues; }

		@Override
		public boolean keep(Stat stat) {
			for(int i=0; i<this.dimLabelValues.length; i+=2)
				if(!this.dimLabelValues[i+1].equals(stat.dims.get(this.dimLabelValues[i]))) return false;
			return true;
		}
	}
	/** Selection criteria for statistics having dimension values */
	public static class DimValueDifferentFrom implements Criteria {
		private String[] dimLabelValues;
		/**
		 * @param dimLabelValues
		 */
		public DimValueDifferentFrom(String... dimLabelValues){ this.dimLabelValues = dimLabelValues; }

		@Override
		public boolean keep(Stat stat) {
			for(int i=0; i<this.dimLabelValues.length; i+=2)
				if(this.dimLabelValues[i+1].equals(stat.dims.get(this.dimLabelValues[i]))) return false;
			return true;
		}
	}
	/** Selection criteria on statistics dimension values */
	public static class DimValueGreaterThan implements Criteria {
		private String dimLabel; double dimValue;
		/**
		 * @param dimLabel
		 * @param dimValue
		 */
		public DimValueGreaterThan(String dimLabel, double dimValue){ this.dimLabel = dimLabel; this.dimValue = dimValue; }

		@Override
		public boolean keep(Stat stat) {
			try { return Double.parseDouble(stat.dims.get(this.dimLabel)) > this.dimValue; }
			catch (@SuppressWarnings("unused") NumberFormatException e) { return false; }
		}
	}
	/** Selection criteria on statistics dimension values */
	public static class DimValueLowerThan implements Criteria {
		private String dimLabel; double dimValue;
		/**
		 * @param dimLabel
		 * @param dimValue
		 */
		public DimValueLowerThan(String dimLabel, double dimValue){ this.dimLabel = dimLabel; this.dimValue = dimValue; }

		@Override
		public boolean keep(Stat stat) {
			try { return Double.parseDouble(stat.dims.get(this.dimLabel)) < this.dimValue; }
			catch (@SuppressWarnings("unused") NumberFormatException e) { return false; }
		}
	}
	/** Selection criteria on statistics dimension values */
	public static class DimValueGreaterOrEqualThan implements Criteria {
		private String dimLabel; double dimValue;
		/**
		 * @param dimLabel
		 * @param dimValue
		 */
		public DimValueGreaterOrEqualThan(String dimLabel, double dimValue){ this.dimLabel = dimLabel; this.dimValue = dimValue; }

		@Override
		public boolean keep(Stat stat) {
			try { return Double.parseDouble(stat.dims.get(this.dimLabel)) >= this.dimValue; }
			catch (@SuppressWarnings("unused") NumberFormatException e) { return false; }
		}
	}
	/** Selection criteria on statistics dimension values */
	public static class DimValueLowerOrEqualThan implements Criteria {
		private String dimLabel; double dimValue;
		/**
		 * @param dimLabel
		 * @param dimValue
		 */
		public DimValueLowerOrEqualThan(String dimLabel, double dimValue){ this.dimLabel = dimLabel; this.dimValue = dimValue; }

		@Override
		public boolean keep(Stat stat) {
			try { return Double.parseDouble(stat.dims.get(this.dimLabel)) <= this.dimValue; }
			catch (@SuppressWarnings("unused") NumberFormatException e) { return false; }
		}
	}

	/**
	 *  A composite data criteria (or)
	 */
	public static class Or implements Criteria {
		private Criteria[] cri;
		/**
		 * @param cri
		 */
		public Or(Criteria... cri){ this.cri = cri; }

		@Override
		public boolean keep(Stat stat) {
			for(int i=0; i<this.cri.length; i++) if(this.cri[i].keep(stat)) return true;
			return false;
		}
	}

	/**
	 *  A composite data criteria (and)
	 */
	public static class And implements Criteria {
		private Criteria[] cri;
		/**
		 * @param cri
		 */
		public And(Criteria... cri){ this.cri = cri; }
		@Override
		public boolean keep(Stat stat) {
			for(int i=0; i<this.cri.length; i++) if(!this.cri[i].keep(stat)) return false;
			return true;
		}
	}

	/**
	 *  A composite data criteria (not)
	 */
	public static class Not implements Criteria {
		private Criteria cri;
		/**
		 * @param cri
		 */
		public Not(Criteria cri){ this.cri = cri; }

		@Override
		public boolean keep(Stat stat) {
			return !this.cri.keep(stat);
		}
	}

}
