package eu.europa.ec.eurostat.java4eurostat.analysis;

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

	public void test() throws Exception {

		String path = "./src/test/resources/";
		StatsHypercube hc = CSV.load(path+"ex.csv", "population");
		StatsHypercube hcNc = CSV.load(path+"ex_non_compact.csv", "population");
		StatsHypercube hcOv = CSV.load(path+"ex_overlap.csv", "population");
		StatsHypercube hcDirty = CSV.load(path+"ex_dirty.csv", "population");

		//TODO
		/*
		System.out.println(Validation.checkUnicity(hc)); //0
		System.out.println(Validation.checkUnicity(hcNc)); //0
		System.out.println(Validation.checkUnicity(hcOv)); //2+2
		System.out.println(Validation.checkUnicity(hcDirty)); //2+3
		 */
	}

}
