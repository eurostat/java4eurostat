package eu.europa.ec.eurostat.java4eurostat.analysis;

import eu.europa.ec.eurostat.java4eurostat.base.StatsHypercube;
import eu.europa.ec.eurostat.java4eurostat.io.CSV;
import junit.framework.TestCase;

/**
 * @author julien Gaffuri
 *
 */
public class CompacityTest extends TestCase {

	/*public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(CompacityTest.class);
	}*/

	public void test() throws Exception {

		String path = "./src/test/resources/";
		StatsHypercube hc = CSV.load(path+"ex.csv", "population");
		StatsHypercube hcNc = CSV.load(path+"ex_non_compact.csv", "population");
		StatsHypercube hcOv = CSV.load(path+"ex_overlap.csv", "population");
		StatsHypercube hcDirty = CSV.load(path+"ex_dirty.csv", "population");

		assertEquals(12, Compacity.getMaxSize(hc));
		assertEquals(1.0, Compacity.getCompacityIndicator(hc, false, false));
		assertEquals(12, Compacity.getMaxSize(hcNc));
		assertEquals(0.75, Compacity.getCompacityIndicator(hcNc, false, false));
		assertEquals(12, Compacity.getMaxSize(hcOv));
		assertEquals(3.5/3.0, Compacity.getCompacityIndicator(hcOv, false, false));
		assertEquals(27, Compacity.getMaxSize(hcDirty));
		assertEquals(0.48148148148148145, Compacity.getCompacityIndicator(hcDirty, false, false));
	}

}
