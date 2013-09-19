package corp.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import corp.util.StringUtil;

/**
 * Gazette is a dictionary which tells us some NERs which we do know there names beforehand.
 * 
 * @authors Lingpeng Kong, Bill McDowell
 *
 */
public class Gazetteer {
	private HashMap<String, List<String>> gazetteer;
	private String name;
	private StringUtil.StringTransform cleanFn;
	
	public Gazetteer(String name, String sourceFilePath, StringUtil.StringTransform cleanFn) {
		this.cleanFn = cleanFn;
		this.gazetteer = new HashMap<String, List<String>>();
		this.name = name;
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(sourceFilePath));
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] lineValues = line.trim().split("\\t");
				if (lineValues.length < 2) {
					br.close();
					throw new IllegalArgumentException();
				}
				
				String id = lineValues[0];
				for (int i = 1; i < lineValues.length; i++) {
					String cleanValue = cleanString(lineValues[i]);
					if (!this.gazetteer.containsKey(cleanValue))
						this.gazetteer.put(cleanValue, new ArrayList<String>(2));
					if (!this.gazetteer.get(cleanValue).contains(id))
						this.gazetteer.get(cleanValue).add(id);
				}
			}
			
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Gazetteer(String name, String sourceFilePath) {
		this(name, sourceFilePath, StringUtil.getDefaultCleanFn());
	}

	public String getName() {
		return this.name;
	}
	
	private String cleanString(String str) {		
		return this.cleanFn.transform(str);
	}
	
	public boolean contains(String str) {
		return this.gazetteer.containsKey(cleanString(str));
	}
	
	public List<String> getIds(String str) {
		String cleanStr = cleanString(str);
		if (this.gazetteer.containsKey(cleanStr))
			return this.gazetteer.get(cleanStr);
		else
			return null;
	}
	
	public double min(String str, StringUtil.StringPairMeasure fn) {
		double min = Double.POSITIVE_INFINITY;
		String cleanStr = cleanString(str);
		for (String gStr : this.gazetteer.keySet()) {
			double curMin = fn.compute(cleanStr, gStr);
			min = (curMin < min) ? curMin : min;
		}
		return min;
	}
	
	public double max(String str, StringUtil.StringPairMeasure fn) {
		double max = Double.NEGATIVE_INFINITY;
		String cleanStr = cleanString(str);
		for (String gStr : this.gazetteer.keySet()) {
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
