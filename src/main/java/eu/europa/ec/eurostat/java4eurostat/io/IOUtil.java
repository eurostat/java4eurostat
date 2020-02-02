/**
 * 
 */
package eu.europa.ec.eurostat.java4eurostat.io;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * @author julien Gaffuri
 *
 */
public class IOUtil {

	/**
	 * @param paramData
	 * @return
	 */
	public static String getURLParametersPart(String... paramData){
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<paramData.length; i+=2){
			if("".equals(paramData[i])) continue;
			if(i>0) sb.append("&");
			sb.append(paramData[i]).append("=").append(paramData[i+1]);
		}
		return sb.toString();
	}

	/**
	 * @param urlBase
	 * @param paramData
	 * @return
	 */
	public static String getURL(String urlBase, String... paramData){
		return urlBase + (paramData == null? "" : "?" + getURLParametersPart(paramData));
	}

	/**
	 * @param url
	 * @return
	 */
	public static String getDataFromURL(String url) {
		try {
			String line;
			StringBuffer sb = new StringBuffer();
			BufferedReader br = new BufferedReader(new InputStreamReader(new URL(url).openConnection().getInputStream()));
			while ((line = br.readLine()) != null) sb.append(line);
			br.close();
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @param url
	 * @param path
	 */
	public static void downloadFile(String url, String path) {
		try {
			URL url_ = new URL(url);

			/*File f = new File(path);
			if(f.exists()) f.delete();
			f.createNewFile();*/

			InputStream in = url_.openStream();
			Files.copy(in, Paths.get(path), StandardCopyOption.REPLACE_EXISTING);
			in.close();

			//ReadableByteChannel rbc = Channels.newChannel(url_.openStream());
			//FileOutputStream fos = new FileOutputStream(path);
			//fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			//fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
