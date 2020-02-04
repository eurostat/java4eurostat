package eu.europa.ec.eurostat.java4eurostat.io;

import java.util.Date;
import java.util.HashMap;

import eu.europa.ec.eurostat.java4eurostat.base.StatsHypercube;
import junit.framework.TestCase;

/**
 * @author julien Gaffuri
 *
 */
public class EurobaseIOTest extends TestCase {

	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(EurobaseIOTest.class);
	}

	public void testGetData1() throws Exception {
		StatsHypercube hc = EurobaseIO.getData("prc_hicp_cow");
		//hc.printInfo();
	}

	//public void testGetData2() throws Exception {
	//StatsHypercube hc = EurobaseIO.getData("prc_hicp_cow", "geo", "EU", "geo", "EA", "lastTimePeriod", "4");
	//hc.printInfo();
	//}
	//public void testGetData3() throws Exception {
	//StatsHypercube hc = EurobaseIO.getData("prc_hicp_cow", "geo", "EU", "geo", "EA", "sinceTimePeriod", "2005");
	//hc.printInfo();
	//}
	public void testGetData4() throws Exception {
		StatsHypercube hc = EurobaseIO.getData("prc_hicp_cow", "time", "2016");
		//hc.printInfo();
	}

	//getDataFromDBCode("prc_hicp_cow", new DimValueEqualTo("geo","BG")).printInfo();



	public void testBulkDownload() throws Exception {
		EurobaseIO.getDataBulkDownload("prc_hicp_cow","target/db");
		StatsHypercube hc2 = EurostatTSV.load("target/db/prc_hicp_cow.tsv");
		//hc2.printInfo();
	}

	public void testEBUpdateDate1() throws Exception {
		Date date = EurobaseIO.getUpdateDate("prc_hicp_midx");
		//System.out.println(date);
	}

	public void testEBUpdateDate2() throws Exception {
		Date date = EurobaseIO.getUpdateDate("prc_hicp_cow");
		//System.out.println(date);
	}

	public void testEBUpdateDate3() throws Exception {
		Date date = EurobaseIO.getUpdateDate("acf_s_own");
		//System.out.println(date);
	}

	public void testEBUpdate1() throws Exception {
		EurobaseIO.update("target/db/", "prc_hicp_cow");
	}

	public void testEBUpdate2() throws Exception {
		EurobaseIO.update("target/db2/", "prc_hicp_cow", "prc_hicp_inw", "prc_hicp_midx", "prc_hicp_manr", "prc_hicp_mmor", "prc_hicp_mv12r", "prc_hicp_aind", "prc_hicp_cind", "prc_hicp_cann", "prc_hicp_cmon");
	}

	public void testGetDictionnary() throws Exception {
		HashMap<String, String> dict = EurobaseIO.getDictionnary("coicop");
		//System.out.println(dict);
		//System.out.println(dict.size());
		//System.out.println(dict.get("CP0112"));
	}

	public void testGetDictionnaryUpdateDate() throws Exception {
		Date date = EurobaseIO.getDictionnaryUpdateDate("coicop");
		//System.out.println(date);
	}


	//System.out.println( IOUtil.getDataFromURL(eurostatBaseURL + "estat-navtree-portlet-prod/BulkDownloadListing?sort=1&file=dic%2Fen%2Fcoicop.dic") );
	//getDataBulkDownload("acf_s_own","",true);
	//getDataBulk("acf_s_own").printInfo();
	//getDataBulk("prc_hicp_cow").printInfo();

}
