/**
 * 
 */
package eu.ec.java4eurostat.io;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * @author julien Gaffuri
 *
 */
public class IOUtil {

	public static String getURLParametersPart(String... paramData){
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<paramData.length; i+=2){
			if("".equals(paramData[i])) continue;
			if(i>0) sb.append("&");
			sb.append(paramData[i]).append("=").append(paramData[i+1]);
		}
		return sb.toString();
	}

	public static String getURL(String urlBase, String... paramData){
		return urlBase + (paramData == null? "" : "?" + getURLParametersPart(paramData));
	}

	public static String getDataFromURL(String url) {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new URL(url).openConnection().getInputStream()));
			String line; StringBuffer sb = new StringBuffer();
			while ((line = in.readLine()) != null) sb.append(line);
			in.close();
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void downloadFile(String url, String path) {
		try {
			URL website = new URL(url);
			ReadableByteChannel rbc = Channels.newChannel(website.openStream());

			/*File f = new File(path);
			if(f.exists()) f.delete();
			f.createNewFile();*/

			FileOutputStream fos = new FileOutputStream(path);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
