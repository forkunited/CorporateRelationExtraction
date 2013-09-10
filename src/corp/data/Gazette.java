package corp.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;

import corp.util.StringUtil;

/**
 * Gazette is a dictionary which tells us some NERs which we do know there names beforehand.
 * 
 * @authors Lingpeng Kong, Bill McDowell
 *
 */
public class Gazette {
	private HashSet<String> gazette;
	private String name;
	private StringUtil.StringTransform cleanFn;
	
	public Gazette(String name, String sourceFilePath, StringUtil.StringTransform cleanFn) {
		this(name, sourceFilePath);
		this.cleanFn = cleanFn;
	}
	
	public Gazette(String name, String sourceFilePath) {
		this.cleanFn = StringUtil.getDefaultCleanFn();
		this.gazette = new HashSet<String>();
		this.name = name;
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(sourceFilePath));
			String line = null;
			while ((line = br.readLine()) != null) {
				String content = line.split("\\t")[0].trim();
				if (content.matches(".+\\(.+\\)")) {
					String s1 = content.substring(0, content.indexOf("("));
					String s2 = content.substring(content.indexOf("(")+1, content.lastIndexOf(")"));
					this.gazette.add(cleanString(s1));
					this.gazette.add(cleanString(s2));
				}
				
				this.gazette.add(cleanString(content));
			}
			
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getName() {
		return this.name;
	}
	
	private String cleanString(String str) {		
		return this.cleanFn.transform(str);
	}
	
	public boolean contains(String str) {
		return this.gazette.contains(cleanString(str));
	}

	public double min(String str, StringUtil.StringPairMeasure fn) {
		double min = Double.POSITIVE_INFINITY;
		String cleanStr = cleanString(str);
		for (String gStr : this.gazette) {
			double curMin = fn.compute(cleanStr, gStr);
			min = (curMin < min) ? curMin : min;
		}
		return min;
	}
	
	public double max(String str, StringUtil.StringPairMeasure fn) {
		double max = Double.NEGATIVE_INFINITY;
		String cleanStr = cleanString(str);
		for (String gStr : this.gazette) {
			double curMax = fn.compute(cleanStr, gStr);
			max = (curMax > max) ? curMax : max;
		}
		return max;
	}
	
	public String removeTerms(String str) {
		String[] strTokens = str.split("\\s+");
		StringBuilder termsRemoved = new StringBuilder();
		for (int i = 0; i < strTokens.length; i++) {
			if (!contains(strTokens[i]))
				termsRemoved.append(strTokens[i]).append(" ");
		}
		
		return termsRemoved.toString().trim();
	}
}
