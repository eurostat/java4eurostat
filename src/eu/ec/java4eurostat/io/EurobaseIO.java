/**
 * 
 */
package eu.ec.java4eurostat.io;

import java.io.File;

import eu.ec.java4eurostat.base.StatsHypercube;

/**
 * @author julien Gaffuri
 *
 */
public class EurobaseIO {
	public static String eurobaseWSURLBase = "http://ec.europa.eu/eurostat/wdds/rest/data/v2.1/json/en/";

	public static StatsHypercube getDataFromURL(String url){
		return JSONStat.get( IOUtil.getDataFromURL(url) );
	}

	public static StatsHypercube getDataFromDBCode(String eurobaseDatabaseCode){
		String url = eurobaseWSURLBase + eurobaseDatabaseCode;
		return getDataFromURL(url);
	}

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

	public static StatsHypercube getDataBulk(String eurobaseDatabaseCode){
		getDataBulkDownload(eurobaseDatabaseCode,"");
		StatsHypercube hc = EurostatTSV.load(File.separator + eurobaseDatabaseCode + ".tsv");
		new File(File.separator + eurobaseDatabaseCode + ".tsv").delete();
		return hc;
	}

	/*public static void main(String[] args) {
		getDataFromDBCode("prc_hicp_cow").printInfo();
		//getDataBulkDownload("acf_s_own","",true);
		//getDataBulk("acf_s_own").printInfo();
		//getDataBulk("prc_hicp_cow").printInfo();
	}*/

}
