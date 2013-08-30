package corp.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;

/**
 * Gazette is a dictionary which tells us some NERs which we do know there names beforehand.
 * 
 * @authors Lingpeng Kong, Bill McDowell
 *
 */
public class Gazette {
	private HashSet<String> gazette;
	
	public Gazette(String sourceFilePath){
		this.gazette = new HashSet<String>();
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(sourceFilePath));
			String line = null;
			while ((line = br.readLine()) != null) {
				String content = line.split("\\t")[0].trim();
				if (content.matches(".+\\(.+\\)")) {
					this.gazette.add(content.substring(0, content.indexOf("(")).trim().toLowerCase());
					this.gazette.add(content.substring(content.indexOf("(")+1, content.lastIndexOf(")")).trim().toLowerCase());
				} else {
					this.gazette.add(content.trim().toLowerCase());
				}
			}
			
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean contains(String str) {
		return this.gazette.contains(str.toLowerCase());
	}
	
	/* FIXME: Add code, or a variation of it, from old project after discussing with Lingpeng */
}
