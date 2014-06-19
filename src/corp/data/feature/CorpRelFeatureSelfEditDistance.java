package corp.data.feature;

import corp.util.StringUtil;

/**
 * 
 * For organization mention m, CorpRelFeatureSelfEditDistance computes the 
 * normalized edit distance between the authoring corporation A(m), and the
 * mentioned organization O(m).
 * 
 * @author Bill McDowell
 *
 */
public class CorpRelFeatureSelfEditDistance extends CorpRelFeatureSelf {
	public CorpRelFeatureSelfEditDistance() {
		super();
		this.namePrefix = "EditDistance";
		this.extremumType = CorpRelFeatureSelf.ExtremumType.Minimum;
	}
	
	public CorpRelFeatureSelfEditDistance(StringUtil.StringTransform cleanFn) {
		this();
		this.cleanFn = cleanFn;
	}
	
	protected double selfCompare(String mentioner, String mentioned) {
		return StringUtil.levenshteinDistance(mentioner, mentioned)/((double)(mentioner.length() + mentioned.length()));
	}

	@Override
	public CorpRelFeature clone() {
		return new CorpRelFeatureSelfEditDistance(this.cleanFn);
	}
	
	@Override
	public String toString(boolean withInit) {
		return "SelfEditDistance(cleanFn=" + this.cleanFn.toString() + ")";
	}
}
