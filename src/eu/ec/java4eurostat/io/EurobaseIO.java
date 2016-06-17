/**
 * 
 */
package eu.ec.java4eurostat.io;

import java.io.File;

import eu.ec.java4eurostat.base.Selection.Criteria;
import eu.ec.java4eurostat.base.StatsHypercube;

/**
 * @author julien Gaffuri
 *
 */
public class EurobaseIO {
	public static String eurobaseWSURLBase = "http://ec.europa.eu/eurostat/wdds/rest/data/v2.1/json/en/";

	//TODO last date of update
	//TODO cache structure

	public static StatsHypercube getDataFromURL(String url, Criteria ssc){
		return JSONStat.load( IOUtil.getDataFromURL(url), ssc );
	}
	public static StatsHypercube getDataFromURL(String url){ return getDataFromURL(url, null); }

	public static StatsHypercube getDataFromDBCode(String eurobaseDatabaseCode, Criteria ssc){
		return getDataFromURL(eurobaseWSURLBase + eurobaseDatabaseCode, ssc);
	}
	public static StatsHypercube getDataFromDBCode(String eurobaseDatabaseCode){ return getDataFromDBCode(eurobaseDatabaseCode, null); }

	public static String eurobaseBulkURLBase = "http://ec.europa.eu/eurostat/estat-navtree-portlet-prod/BulkDownloadListing?file=data%2F";
	public static String eurobaseBulkURLSuf = ".tsv.gz";

	public static void getDataBulkDownload(String eurobaseDatabaseCode){ getDataBulkDownload(eurobaseDatabaseCode,""); }
	public static void getDataBulkDownload(String eurobaseDatabaseCode, String path){ getDataBulkDownload(eurobaseDatabaseCode,path,true); }
	public static void getDataBulkDownload(String eurobaseDatabaseCode, String path, boolean unzip){
		String dFilePath = path + File.separator + eurobaseDatabaseCode + eurobaseBulkURLSuf;
		IOUtil.downloadFile(eurobaseBulkURLBase + eurobaseDatabaseCode + eurobaseBulkURLSuf, dFilePath);
		if(unzip){
			CompressUtil.unGZIP(dFilePath, path + File.separator + eurobaseDatabaseCode+".tsv");
			new File(dFilePath).delete();
		}
	}

	public static StatsHypercube getDataBulk(String eurobaseDatabaseCode, Criteria ssc){
		getDataBulkDownload(eurobaseDatabaseCode,"");
		StatsHypercube hc = EurostatTSV.load(File.separator + eurobaseDatabaseCode + ".tsv", ssc);
		new File(File.separator + eurobaseDatabaseCode + ".tsv").delete();
		return hc;
	}
	public static StatsHypercube getDataBulk(String eurobaseDatabaseCode){ return getDataBulk(eurobaseDatabaseCode, null); }

	/*public static void main(String[] args) {
		getDataFromDBCode("prc_hicp_cow").printInfo();
		//getDataBulkDownload("acf_s_own","",true);
		//getDataBulk("acf_s_own").printInfo();
		//getDataBulk("prc_hicp_cow").printInfo();
	}*/

}
