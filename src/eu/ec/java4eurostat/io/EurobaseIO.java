/**
 * 
 */
package eu.ec.java4eurostat.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import eu.ec.java4eurostat.base.Selection.Criteria;
import eu.ec.java4eurostat.util.Util;
import eu.ec.java4eurostat.base.StatsHypercube;

/**
 * @author julien Gaffuri
 *
 */
public class EurobaseIO {
	public static String eurobaseWSURLBase = "http://ec.europa.eu/eurostat/wdds/rest/data/v2.1/json/en/";
	public static String sinceTimePeriod = "sinceTimePeriod";
	public static String lastTimePeriod = "lastTimePeriod";

	//TODO last date of update
	//TODO cache structure

	public static StatsHypercube getDataFromURL(String url, Criteria ssc){ return JSONStat.load( IOUtil.getDataFromURL(url), ssc ); }
	public static StatsHypercube getDataFromURL(String url){ return getDataFromURL(url, null); }

	public static String getURL(String eurobaseDatabaseCode, String... paramData){
		return IOUtil.getURL(eurobaseWSURLBase + eurobaseDatabaseCode, paramData);
	}

	public static StatsHypercube getData(String eurobaseDatabaseCode, Criteria ssc, String... paramData){
		return getDataFromURL(getURL(eurobaseDatabaseCode, paramData), ssc);
	}
	public static StatsHypercube getData(String eurobaseDatabaseCode, String... paramData){
		return getDataFromURL(getURL(eurobaseDatabaseCode, paramData));
	}
	public static StatsHypercube getData(String eurobaseDatabaseCode){ return getDataFromURL(getURL(eurobaseDatabaseCode)); }

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



	public static Date getUpdateDate(String indic) {
		return getUpdateDate(indic, "data"); //use "dic%2Fen" for codelists
	}

	private static Date getUpdateDate(String indic, String dir) {
		try {
			String url_ = "http://ec.europa.eu/eurostat/estat-navtree-portlet-prod/BulkDownloadListing?dir="+dir;
			if("data".equals(dir)) url_ += "&start="+indic;
			URL url = new URL(url_);

			URLConnection urlc = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
			String line;
			while ((line = in.readLine()) != null){
				line = line.replace("start="+indic, "");
				if(line.contains(indic)) break;
			}
			in.close();

			line = line.substring(line.indexOf( ("data".equals(dir)?"tsv":"dic") + "</td>"), line.length());
			line = line.substring(line.indexOf("&nbsp;")+6, line.length());
			line = line.substring(0, line.indexOf("</td>"));

			//24/02/2015 11:00:17
			return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(line);
		}
		catch (Exception e) { e.printStackTrace(); return null; }
	}

	public static void update(String dataFolderPath, String... databaseCodes){
		try {
			System.out.println("Start data update from Eurobase...");

			String baseUrl1 = "http://ec.europa.eu/eurostat/estat-navtree-portlet-prod/BulkDownloadListing?sort=1&file=";
			String baseUrl2 = baseUrl1 + "data%2F";

			System.out.println("Read information on update last update dates");
			File f = new File(dataFolderPath+"update.txt");
			if(!f.exists()) f.createNewFile();
			HashMap<String, String> lastUpdates = DicUtil.load(dataFolderPath+"update.txt");

			for(int i=0; i<databaseCodes.length; i++){
				String indic = databaseCodes[i];
				System.out.print(indic+":");

				String lastUpdate_ = lastUpdates.get(indic);
				Date lastUpdate = lastUpdate_==null ? null : Util.df.parse(lastUpdate_);
				System.out.print(" Check last update date on Eurobase...");
				Date newUpdate = getUpdateDate(indic);
				if(newUpdate == null) {
					System.out.print("Not found.");
					newUpdate = Util.df.parse("1990-01-01 00:00:00");
				}
				else System.out.print(Util.df.format(newUpdate));

				if(lastUpdate == null || newUpdate.after(lastUpdate)){
					System.out.print(" download...");
					String tsvgz = dataFolderPath + indic + ".tsv.gz";
					IOUtil.downloadFile(baseUrl2 + indic + ".tsv.gz", tsvgz);
					System.out.print(" Done.");

					System.out.print(" Uncompress... ");
					String tsv = dataFolderPath + indic + ".tsv";
					CompressUtil.unGZIP(tsvgz, tsv);
					new File(tsvgz).delete();
					System.out.print(" Done.");

					System.out.print(" Update information file.");
					lastUpdates.put(indic, Util.df.format(newUpdate));
					DicUtil.save(lastUpdates, dataFolderPath+"update.txt");
					System.out.print(" Done.");
					System.out.println();
				} else {
					System.out.println(" No update necessary.");
				}
			}

			System.out.println("Data update from Eurobase done.");
		} catch (Exception e) { e.printStackTrace(); }
	}

	public static void main(String[] args) {
		update("data/", "prc_hicp_cow", "prc_hicp_inw", "prc_hicp_midx", "prc_hicp_manr", "prc_hicp_mmor", "prc_hicp_mv12r", "prc_hicp_aind", "prc_hicp_cind", "prc_hicp_cann", "prc_hicp_cmon");
		//System.out.println( getUpdateDate("prc_hicp_midx") );
		//System.out.println( getUpdateDate("prc_hicp_cow") );
		//System.out.println( getUpdateDate("acf_s_own") );
		//getDataFromDBCode("prc_hicp_cow", new DimValueEqualTo("geo","BG")).printInfo();
		//getDataBulkDownload("acf_s_own","",true);
		//getDataBulk("acf_s_own").printInfo();
		//getDataBulk("prc_hicp_cow").printInfo();
	}

}
