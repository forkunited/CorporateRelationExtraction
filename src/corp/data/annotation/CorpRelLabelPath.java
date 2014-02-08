package corp.data.annotation;

import java.util.Collection;

/**
 * 
 * CorpRelLabelPath represents a sequence of corporate relationship type 
 * labels, usually corresponding to a path through the relationship 
 * type taxonomy.
 * 
 * @author Bill McDowell
 *
 */
public class CorpRelLabelPath {
	private CorpRelLabel[] labelPath;
	
	public CorpRelLabelPath() {
		this.labelPath = new CorpRelLabel[0];
	}
	
	public CorpRelLabelPath(CorpRelLabel[] labelPath) {
		this.labelPath = labelPath;
	}
	
	public CorpRelLabelPath(CorpRelLabel singletonPath) {
		this.labelPath = new CorpRelLabel[1];
		this.labelPath[0] = singletonPath;
	}
	
	public CorpRelLabelPath extend(CorpRelLabel label) {
		CorpRelLabel[] extendedPath = new CorpRelLabel[this.labelPath.length + 1];
		for (int i = 0; i < this.labelPath.length; i++)
			extendedPath[i] = this.labelPath[i];
		extendedPath[extendedPath.length - 1] = label;
		return new CorpRelLabelPath(extendedPath);
	}
	
	public CorpRelLabelPath getPrefix(int length) {
		CorpRelLabel[] prefix = new CorpRelLabel[length];
		for (int i = 0; i < length; i++) {
			prefix[i] = this.labelPath[i];
		}
		
		return new CorpRelLabelPath(prefix);
	}
	
	public CorpRelLabel getLastLabel() {
		if (this.labelPath == null)
			return null;
		return this.labelPath[this.labelPath.length-1];
	}
	
	public CorpRelLabel getFirstValidLabel(Collection<CorpRelLabel> validLabels) {
		if (this.labelPath == null || this.labelPath.length == 0 )
			return null;
		
		for (CorpRelLabel label : this.labelPath) {
			if (validLabels.contains(label))
				return label;
		}
		
		return null;
	}
	
	public boolean isPrefixedBy(CorpRelLabelPath prefix) {
		if (prefix.labelPath.length > this.labelPath.length)
			return false;
		for (int i = 0; i < prefix.labelPath.length; i++) {
			if (prefix.labelPath[i] != this.labelPath[i])
				return false;
		}
		return true;
	}
	
	public CorpRelLabelPath getLongestValidPrefix(Collection<CorpRelLabelPath> validPrefixes) {
		if (this.labelPath.length == 0 )
			return null;
		
		CorpRelLabelPath longestValidPrefix = null;
		for (CorpRelLabelPath validPrefix: validPrefixes) {
			if (longestValidPrefix != null && longestValidPrefix.labelPath.length > validPrefix.labelPath.length)
				continue;
			if (isPrefixedBy(validPrefix))
				longestValidPrefix = validPrefix;
		}
		
		return longestValidPrefix;
	}
	
	public int size() {
		return this.labelPath.length;
	}
	
	@Override
	public boolean equals(Object o) {
		CorpRelLabelPath oLabelPath = (CorpRelLabelPath)o;
		if (oLabelPath.labelPath.length != this.labelPath.length)
			return false;
		for (int i = 0; i < this.labelPath.length; i++)
			if (this.labelPath[i] != oLabelPath.labelPath[i])
				return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}
	
	@Override
	public String toString() {
		StringBuilder labelPathStr = new StringBuilder();
		for (CorpRelLabel label : this.labelPath) {
			labelPathStr = labelPathStr.append(label).append("-");
		}
		
		if (labelPathStr.length() > 0)
			labelPathStr = labelPathStr.deleteCharAt(labelPathStr.length() - 1);
		
		return labelPathStr.toString();
	}
	
	public CorpRelLabel[] toArray() {
		return this.labelPath;
	}
	
	public static CorpRelLabelPath fromString(String pathStr) {
		if (pathStr.length() == 0)
			return new CorpRelLabelPath();
		
		String[] labelStrs = pathStr.split("-");
		CorpRelLabel[] labelPath = new CorpRelLabel[labelStrs.length];
		for (int i = 0; i < labelStrs.length; i++)
			labelPath[i] = CorpRelLabel.valueOf(labelStrs[i]);
		return new CorpRelLabelPath(labelPath);
	}
}
