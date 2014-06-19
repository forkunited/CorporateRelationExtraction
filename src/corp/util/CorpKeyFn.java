package corp.util;

import java.util.List;

import corp.data.Gazetteer;
import corp.util.StringUtil;

/**
 * CorpKeyFn computes the entity resolution hashing function described in the
 * "Entity Resolution Function" section (3.3.1) of the Sloan tech report.  It
 * is used to resolve organization names to entities.
 * 
 * The class takes "key maps" and a string cleaning function as parameters.  The
 * "key maps" map stock tickers and non-corporate initialisms to entities, and
 * the string cleaning function cleans extra characters and stop-words from
 * the names.  
 * 
 * See corp.data.CorpDataTools for example instantiations of this class.  The
 * instantiation described in the tech report pdf is in there.
 * 
 * @author Bill McDowell
 *
 */
public class CorpKeyFn implements StringUtil.StringTransform {
	private List<Gazetteer> keyMaps;
	private StringUtil.StringTransform cleanFn;
	private String transformName;
	
	public CorpKeyFn(List<Gazetteer> keyMaps, StringUtil.StringTransform cleanFn) {
		this.keyMaps = keyMaps;
		this.cleanFn = cleanFn;
		this.transformName = "CorpKeyFn_";
		
		if (keyMaps != null) {
			for (Gazetteer keyMap : keyMaps)
				this.transformName += keyMap.getName() + "_";
		}
		
		this.transformName += cleanFn.toString();
	}
	
	public String toString() {
		return this.transformName;
	}
	
	public String transform(String str) {
		if (this.keyMaps != null) {
			for (Gazetteer keyMap : this.keyMaps) {
				List<String> keys = keyMap.getIds(str);
				if (keys != null && keys.size() == 1)
					return keys.get(0);	
			}
		}
		
		str = this.cleanFn.transform(str);
		String[] strParts = str.split(" ");
		StringBuilder transformedStr = new StringBuilder();
		for (String strPart : strParts) {
			if (strPart.length() > 1)
				transformedStr.append(strPart).append("_");
		}
		
		if (transformedStr.length() > 0) {
			transformedStr.delete(transformedStr.length() - 1, transformedStr.length());
			return transformedStr.toString();
		} else {
			return str;
		}
	}
}
