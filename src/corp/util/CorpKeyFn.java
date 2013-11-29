package corp.util;

import java.util.List;

import ark.data.Gazetteer;
import ark.util.StringUtil;

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
