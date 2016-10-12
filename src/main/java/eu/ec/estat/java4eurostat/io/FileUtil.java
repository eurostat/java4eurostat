/**
 * 
 */
package eu.ec.estat.java4eurostat.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;

/**
 * @author julien Gaffuri
 *
 */
public class FileUtil {

	//get all files in a folder (recursivelly)
	public static ArrayList<File> getFiles(String folderPath) {
		return getFiles(new File(folderPath));
	}
	public static ArrayList<File> getFiles(File folder) {
		ArrayList<File> files = new ArrayList<File>();
		for (File file : folder.listFiles())
			if (file.isDirectory())
				files.addAll(getFiles(file));
			else
				files.add(file);
		return files;
	}


	//count file line number
	public static int fileLineCount(String inputFilePath){
		int i=0;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(inputFilePath))));
			while (br.readLine() != null)
				i++;
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return i;
	}



	public static void findReplace(String inFilePath, String outFilePath, String find, String replace) {
		try {
			//Charset charset = StandardCharsets.UTF_8;
			String content = new String(Files.readAllBytes(Paths.get(inFilePath)));
			content = content.replaceAll(find, replace);
			Files.write(Paths.get(outFilePath), content.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void removeLine(String inFilePath, String outFilePath, LineCriteria lineC) {
		BufferedReader br = null;
		BufferedWriter bw = null;
		File f = new File("temp324954635");
		if(f.exists()) f.delete();

		try {
			br = new BufferedReader(new FileReader(inFilePath));
			bw = new BufferedWriter(new FileWriter(f, true));

			String line;
			while ((line = br.readLine()) != null) {
				if(!lineC.keep(line)) continue;
				bw.write(line); bw.newLine();
			}
			br.close(); bw.close();
			FileUtils.copyFile(f, new File(outFilePath));
			if(f.exists()) f.delete();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try { if (br != null)br.close(); } catch (Exception ex) { ex.printStackTrace(); }
			try { if (bw != null) bw.close(); } catch (Exception ex) { ex.printStackTrace(); }
		}
	}
	public interface LineCriteria{ boolean keep(String line); }

	public static void transformLine(String inFilePath, String outFilePath, LineTransformation lineC) {
		BufferedReader br = null;
		BufferedWriter bw = null;
		File f = new File("temp51978536425");
		if(f.exists()) f.delete();

		try {
			br = new BufferedReader(new FileReader(inFilePath));
			bw = new BufferedWriter(new FileWriter(f, true));

			String line;
			while ((line = br.readLine()) != null) {
				bw.write(lineC.transform(line)); bw.newLine();
			}
			br.close(); bw.close();
			FileUtils.copyFile(f, new File(outFilePath));
			if(f.exists()) f.delete();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try { if (br != null)br.close(); } catch (Exception ex) { ex.printStackTrace(); }
			try { if (bw != null) bw.close(); } catch (Exception ex) { ex.printStackTrace(); }
		}
	}
	public interface LineTransformation{ String transform(String line); }

	//merge files into one. Possibly insert content between them
	public static void merge(String[] files, String outFile) { merge(files, outFile, null); }
	public static void merge(String[] files, String outFile, String[] insert) {
		try {
			File outFile_ = new File(outFile);
			if(outFile_.exists()) outFile_.delete();
			BufferedWriter bw = new BufferedWriter(new FileWriter(outFile_, true));

			for(int i=0; i<files.length; i++) {

				if(insert!=null && insert.length>=files.length){
					if(insert[i] == null) continue;
					bw.write(insert[i]);
					bw.newLine();
				}

				File f = new File(files[i]);

				if(!f.exists()){
					System.out.println("File "+f+" does not exists.");
					continue;
				}

				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
				String line;
				while ((line = br.readLine()) != null) {
					bw.write(line);
					bw.newLine();
				}
				br.close();
			}

			if(insert!=null && insert.length>=files.length+1 && insert[files.length] != null){
				bw.write(insert[files.length]);
				bw.newLine();
			}
			bw.close();

		} catch (Exception e) { e.printStackTrace(); }

	}

	public static void delete(String[] files) {
		for(String f : files)
			if((new File(f)).exists())
				(new File(f)).delete();
	}

	public static String getPath(File file) {
		return file.getAbsolutePath().replace(file.getName(),"");
	}

}
