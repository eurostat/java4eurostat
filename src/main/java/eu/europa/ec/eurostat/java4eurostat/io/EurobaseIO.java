/**
 * 
 */
package eu.europa.ec.eurostat.java4eurostat.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import eu.europa.ec.eurostat.java4eurostat.analysis.Selection.Criteria;
import eu.europa.ec.eurostat.java4eurostat.base.StatsHypercube;
import eu.europa.ec.eurostat.java4eurostat.util.Util;

/**
 * @author julien Gaffuri
 *
 */
public class EurobaseIO {

	public static String eurostatBaseURL = "https://ec.europa.eu/eurostat/";
	
	/***/
	public static String eurobaseWSURLBase = eurostatBaseURL + "wdds/rest/data/v2.1/json/en/";

	/**
	 * @param lg
	 * @param version
	 */
	public static void updateEurobaseWSURLBase(String lg, String version){ eurobaseWSURLBase = eurostatBaseURL + "wdds/rest/data/v"+version+"/json/"+lg+"/"; }

	/***/
	public static String sinceTimePeriod = "sinceTimePeriod";
	/***/
	public static String lastTimePeriod = "lastTimePeriod";

	/**
	 * @param url
	 * @param ssc
	 * @return
	 */
	public static StatsHypercube getDataFromURL(String url, Criteria ssc){ return JSONStat.load( IOUtil.getDataFromURL(url), true, ssc ); }

	/**
	 * @param url
	 * @return
	 */
	public static StatsHypercube getDataFromURL(String url){ return getDataFromURL(url, null); }

	/**
	 * @param eurobaseDatabaseCode
	 * @param paramData
	 * @return
	 */
	public static String getURL(String eurobaseDatabaseCode, String... paramData){
		return IOUtil.getURL(eurobaseWSURLBase + eurobaseDatabaseCode, paramData);
	}

	/**
	 * @param eurobaseDatabaseCode
	 * @param ssc
	 * @param paramData
	 * @return
	 */
	public static StatsHypercube getData(String eurobaseDatabaseCode, Criteria ssc, String... paramData){
		return getDataFromURL(getURL(eurobaseDatabaseCode, paramData), ssc);
	}

	@SuppressWarnings("javadoc")
	public static StatsHypercube getData(String eurobaseDatabaseCode, String... paramData){
		return getDataFromURL(getURL(eurobaseDatabaseCode, paramData));
	}

	/**
	 * @param eurobaseDatabaseCode
	 * @return
	 */
	public static StatsHypercube getData(String eurobaseDatabaseCode){ return getDataFromURL(getURL(eurobaseDatabaseCode)); }

	/***/
	public static String eurobaseBulkURLBase = eurostatBaseURL + "estat-navtree-portlet-prod/BulkDownloadListing?file=data%2F";
	/***/
	public static String eurobaseBulkURLSuf = ".tsv.gz";


	/**
	 * @param eurobaseDatabaseCode
	 * @param path
	 * @param unzip
	 */
	public static void getDataBulkDownload(String eurobaseDatabaseCode, String path, boolean unzip){
		if(!new File(path).exists()) new File(path).mkdirs();
		String dFilePath = path + File.separator + eurobaseDatabaseCode + eurobaseBulkURLSuf;
		IOUtil.downloadFile(eurobaseBulkURLBase + eurobaseDatabaseCode + eurobaseBulkURLSuf, dFilePath);		
		if(unzip){
			CompressUtil.unGZIP(dFilePath, path + File.separator + eurobaseDatabaseCode+".tsv");
			new File(dFilePath).delete();
		}
	}
	@SuppressWarnings("javadoc")
	public static void getDataBulkDownload(String eurobaseDatabaseCode){ getDataBulkDownload(eurobaseDatabaseCode,""); }
	@SuppressWarnings("javadoc")
	public static void getDataBulkDownload(String eurobaseDatabaseCode, String path){ getDataBulkDownload(eurobaseDatabaseCode,path,true); }


	/**
	 * @param eurobaseDatabaseCode
	 * @param ssc
	 * @return
	 */
	public static StatsHypercube getDataBulk(String eurobaseDatabaseCode, Criteria ssc){
		getDataBulkDownload(eurobaseDatabaseCode,"");
		StatsHypercube hc = EurostatTSV.load(File.separator + eurobaseDatabaseCode + ".tsv", ssc);
		new File(File.separator + eurobaseDatabaseCode + ".tsv").delete();
		return hc;
	}

	/**
	 * @param eurobaseDatabaseCode
	 * @return
	 */
	public static StatsHypercube getDataBulk(String eurobaseDatabaseCode){ return getDataBulk(eurobaseDatabaseCode, null); }



	/**
	 * Get a database update date.
	 * @param indic
	 * @return
	 */
	public static Date getUpdateDate(String indic) {
		return getUpdateDate(indic, "data"); //use "dic%2Fen" for codelists
	}

	@SuppressWarnings("null")
	private static Date getUpdateDate(String indic, String dir) {
		try {
			String url_ = eurostatBaseURL + "estat-navtree-portlet-prod/BulkDownloadListing?dir="+dir;
			if("data".equals(dir)) url_ += "&start="+indic;
			URL url = new URL(url_);

			URLConnection urlc = url.openConnection();
			String line = "";
			BufferedReader in = null;
			try {
				in = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
				while ((line = in.readLine()) != null){
					line = line.replace("start="+indic, "");
					if(line.contains(indic)) break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if(in!=null) in.close();
			}

			line = line.substring(line.indexOf( ("data".equals(dir)?"tsv":"dic") + "</td>"), line.length());
			line = line.substring(line.indexOf("&nbsp;")+6, line.length());
			line = line.substring(0, line.indexOf("</td>"));

			//24/02/2015 11:00:17
			return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(line);
		}
		catch (Exception e) { e.printStackTrace(); return null; }
	}

	/**
	 * Update TSV files located into a folder based on the last update date and newly published data
	 * @param dataFolderPath The TSV file folder
	 * @param databaseCodes The database codes to download/update
	 */
	public static void update(String dataFolderPath, String... databaseCodes){
		try {
			if(!new File(dataFolderPath).exists()) new File(dataFolderPath).mkdirs();

			System.out.println("Start data update from Eurobase...");

			String baseUrl1 = eurostatBaseURL + "estat-navtree-portlet-prod/BulkDownloadListing?sort=1&file=";
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





	/**
	 * 
	 */
	public static String eurobaseDictionnaryURLBase = eurostatBaseURL + "estat-navtree-portlet-prod/BulkDownloadListing?sort=1&file=dic%2Fen%2F";

	/**
	 * Load a dictionnary from https://ec.europa.eu/eurostat/estat-navtree-portlet-prod/BulkDownloadListing?dir=dic%2Fen
	 * 
	 * @param code the dictionnary code. Example: "geo" for geographical regions.
	 * @return
	 */
	public static HashMap<String,String> getDictionnary(String code){
		return DicUtil.loadFromURL(eurobaseDictionnaryURLBase + code + ".dic");
	}

	/**
	 * Get a database update date.
	 * @param indic
	 * @return
	 */
	public static Date getDictionnaryUpdateDate(String indic) {
		return getUpdateDate(indic, "dic%2Fen");
	}

/*
	public static void main(String[] args) throws Exception {
		System.out.println("Start");

		//test 1
		//StatsHypercube hc = EurobaseIO.getData("prc_hicp_cow");
		//hc.printInfo();

		//test 2
		//EurobaseIO.getDataBulkDownload("prc_hicp_cow","target/db");
		//StatsHypercube hc2 = EurostatTSV.load("target/db/prc_hicp_cow.tsv");
		//hc2.printInfo();

		//test 3
		//EurobaseIO.update("target/db/", "prc_hicp_cow");

		System.out.println("End");

		//System.out.println(getDictionnaryUpdateDate("coicop"));
		//HashMap<String, String> dict = getDictionnary("coicop");
		//System.out.println(dict.size());
		//System.out.println(dict.get("CP0112"));

		//System.out.println( IOUtil.getDataFromURL(eurostatBaseURL + "estat-navtree-portlet-prod/BulkDownloadListing?sort=1&file=dic%2Fen%2Fcoicop.dic") );

		//getData("prc_hicp_cow", "geo", "EU", "geo", "EA", "lastTimePeriod", "4").printInfo();
		//getData("prc_hicp_cow", "geo", "EU", "geo", "EA", "sinceTimePeriod", "2005").printInfo();
		//public static String sinceTimePeriod = "sinceTimePeriod";

		//getData("prc_hicp_cow", "time", "2016").printInfo();;
		//update("data/", "prc_hicp_cow", "prc_hicp_inw", "prc_hicp_midx", "prc_hicp_manr", "prc_hicp_mmor", "prc_hicp_mv12r", "prc_hicp_aind", "prc_hicp_cind", "prc_hicp_cann", "prc_hicp_cmon");
		//System.out.println( getUpdateDate("prc_hicp_midx") );
		//System.out.println( getUpdateDate("prc_hicp_cow") );
		//System.out.println( getUpdateDate("acf_s_own") );
		//getDataFromDBCode("prc_hicp_cow", new DimValueEqualTo("geo","BG")).printInfo();
		//getDataBulkDownload("acf_s_own","",true);
		//getDataBulk("acf_s_own").printInfo();
		//getDataBulk("prc_hicp_cow").printInfo();
	}
*/
}
