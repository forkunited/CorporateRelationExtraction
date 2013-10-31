package corp.util;

import java.util.List;

import ark.data.Gazetteer;
import ark.util.StringUtil;

public class CorpKeyFn implements StringUtil.StringTransform {
	private Gazetteer keyMap;
	private StringUtil.StringTransform cleanFn;
	private String transformName;
	
	public CorpKeyFn(Gazetteer keyMap, StringUtil.StringTransform cleanFn) {
		this.keyMap = keyMap;
		this.cleanFn = cleanFn;
		this.transformName = "CorpKeyFn_" + ((keyMap == null) ? "" : keyMap.getName()) + "_" + cleanFn.toString();
	}
	
	public String toString() {
		return this.transformName;
	}
	
	public String transform(String str) {
		if (keyMap != null && keyMap.contains(str)) {
			List<String> keys = keyMap.getIds(str);
			if (keys.size() == 1)
				return keys.get(0);
		}
		
		str = cleanFn.transform(str);
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
