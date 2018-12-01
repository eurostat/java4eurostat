/**
 * 
 */
package eu.europa.ec.eurostat.java4eurostat.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author julien Gaffuri
 *
 */
public class CompressUtil {

	public static void unGZIP(String inGZIPFile, String outUnGZIPFile){
		try {
			GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(inGZIPFile));
			FileOutputStream out = new FileOutputStream(outUnGZIPFile);
			int len;
			byte[] buffer = new byte[1024];
			while ((len = gzis.read(buffer)) > 0)
				out.write(buffer, 0, len);
			gzis.close();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void createZIP(String inPath, String zipFile, String[] inFiles){
		try {
			File outFile = new File(inPath+zipFile);
			if(outFile.exists()) outFile.delete();
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFile));
			for(String inFile : inFiles) {
				if(!new File(inPath+inFile).exists()) continue;
				ZipEntry e = new ZipEntry(inFile);
				out.putNextEntry(e);
				byte[] data = Files.readAllBytes(Paths.get(inPath+inFile));
				out.write(data, 0, data.length);
				out.closeEntry();
			}
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
