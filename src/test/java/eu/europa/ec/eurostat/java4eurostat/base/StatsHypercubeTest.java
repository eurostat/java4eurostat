package eu.europa.ec.eurostat.java4eurostat.base;

import eu.europa.ec.eurostat.java4eurostat.io.CSV;
import junit.framework.TestCase;

/**
 * @author julien Gaffuri
 *
 */
public class StatsHypercubeTest extends TestCase {

	/*public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(StatsHypercubeTest.class);
	}*/

	public void test() throws Exception {

		String path = "./src/test/resources/";
		StatsHypercube hc = CSV.load(path+"ex.csv", "population");
		StatsHypercube hcNc = CSV.load(path+"ex_non_compact.csv", "population");
		StatsHypercube hcOv = CSV.load(path+"ex_overlap.csv", "population");
		StatsHypercube hcDirty = CSV.load(path+"ex_dirty.csv", "population");

		//TODO
		assertEquals(3, hc.dimLabels.size());
		assertEquals(12, hc.stats.size());
		assertEquals(3, hc.getDimLabels().length);
		assertEquals(2, hc.getDimValues("country").size());
		assertEquals(3, hc.getDimValues("gender").size());
		assertEquals(2, hc.getDimValues("year").size());
		//double[] qt = hc.getQuantiles(3); //[47.85, 119.5, 148.15]
		//for(double q : qt) System.out.println(" "+q);
	}

}
