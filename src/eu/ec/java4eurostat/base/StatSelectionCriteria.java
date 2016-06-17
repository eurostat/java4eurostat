/**
 * 
 */
package eu.ec.java4eurostat.base;

/**
 * A selection criteria to decide whether a statistical value should be kept of not.
 * Ex: All stats with gender=male, with value=0, with country starting with a 'A', etc.
 * 
 * @author Julien Gaffuri
 *
 */
public interface StatSelectionCriteria {
	boolean keep(Stat stat);

	/**
	 * Selection criteria for statistics having dimension values
	 * @author Julien Gaffuri
	 */
	public class DimValuesEqualTo implements StatSelectionCriteria {
		String[] dimLabelsEqual, dimValuesEqual;
		DimValuesEqualTo(String[] dimLabelsEqual, String[] dimValuesEqual){ this.dimLabelsEqual = dimLabelsEqual; this.dimValuesEqual = dimValuesEqual; }

		@Override
		public boolean keep(Stat stat) {
			for(int i=0; i<dimLabelsEqual.length; i++)
				if(dimValuesEqual[i].equals(stat.dims.get(dimLabelsEqual[i]))) return false;
			return true;
		}
	}


	/** Selection criteria on values */
	public class ValueEqualTo implements StatSelectionCriteria {
		double value;
		ValueEqualTo(double value){ this.value = value; }
		@Override
		public boolean keep(Stat stat) { return stat.value == value; }
	}
	/** Selection criteria on values */
	public class ValueGreaterThan implements StatSelectionCriteria {
		double value;
		ValueGreaterThan(double value){ this.value = value; }
		@Override
		public boolean keep(Stat stat) { return stat.value > value; }
	}
	/** Selection criteria on values */
	public class ValueLowerThan implements StatSelectionCriteria {
		double value;
		ValueLowerThan(double value){ this.value = value; }
		@Override
		public boolean keep(Stat stat) { return stat.value < value; }
	}
	/** Selection criteria on values */
	public class ValueGreaterOrEqualThan implements StatSelectionCriteria {
		double value;
		ValueGreaterOrEqualThan(double value){ this.value = value; }
		@Override
		public boolean keep(Stat stat) { return stat.value >= value; }
	}
	/** Selection criteria on values */
	public class ValueLowerOrEqualThan implements StatSelectionCriteria {
		double value;
		ValueLowerOrEqualThan(double value){ this.value = value; }
		@Override
		public boolean keep(Stat stat) { return stat.value <= value; }
	}

}
