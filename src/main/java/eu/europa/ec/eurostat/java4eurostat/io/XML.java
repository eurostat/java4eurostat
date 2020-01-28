package eu.europa.ec.eurostat.java4eurostat.io;

import java.net.URL;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

/**
 * @author julien Gaffuri
 *
 */
public class XML {

	/**
	 * Get a XML document from a URL.
	 * 
	 * @param urlString
	 * @return
	 */
	public static Document parseXMLfromURL(String urlString){
		try{
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new URL(urlString).openConnection().getInputStream());
			new URL(urlString).openConnection().getInputStream().close();
			return doc;
		}
		catch(Exception e){
			e.printStackTrace();
		}       
		return null;
	}

}
