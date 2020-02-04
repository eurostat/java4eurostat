package eu.europa.ec.eurostat.java4eurostat.analysis;

import java.util.HashMap;

import eu.europa.ec.eurostat.java4eurostat.base.StatsHypercube;
import eu.europa.ec.eurostat.java4eurostat.io.CSV;
import junit.framework.TestCase;

/**
 * @author julien Gaffuri
 *
 */
public class ValidationTest extends TestCase {

	/*public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(ValidationTest.class);
	}*/

	public void testUnicity() throws Exception {
		StatsHypercube hc = CSV.load("./src/test/resources/ex.csv", "population");
		assertEquals(0, Validation.checkUnicity(hc).size());
	}

	public void testUnicityNc() throws Exception {
		StatsHypercube hcNc = CSV.load("./src/test/resources/ex_non_compact.csv", "population");
		assertEquals(0, Validation.checkUnicity(hcNc).size());
	}

	public void testUnicityOv() throws Exception {
		StatsHypercube hcOv = CSV.load("./src/test/resources/ex_overlap.csv", "population");

		HashMap<String, Integer> un = Validation.checkUnicity(hcOv);
		assertEquals(2.0, un.get("{country=Brasil, gender=Female, year=2013}").doubleValue());
		assertEquals(2.0, un.get("{country=Japan, gender=Male, year=2013}").doubleValue());
	}

	public void testUnicityDirty() throws Exception {
		StatsHypercube hcDirty = CSV.load("./src/test/resources/ex_dirty.csv", "population");
		HashMap<String, Integer> un = Validation.checkUnicity(hcDirty);
		assertEquals(2.0, un.get("{country=Brasil, gender=Female, year=2013}").doubleValue());
		assertEquals(3.0, un.get("{country=Brasil, gender=Total, year=2013}").doubleValue());
	}

}
