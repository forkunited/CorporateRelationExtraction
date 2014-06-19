package corp.data.feature;

import corp.util.StringUtil;

/**
 * 
 * For organization mention m, CorpRelFeatureSelfPrefixTokens computes 
 * 
 * 1(A(m) shares at least k prefix tokens with O(m))
 * 
 * Where A(m) is the authoring organization and O(m) is the mentioned 
 * organization.
 * 
 * @author Bill McDowell
 *
 */
public class CorpRelFeatureSelfPrefixTokens extends CorpRelFeatureSelf {
	private int minTokens;
	
	public CorpRelFeatureSelfPrefixTokens(int minTokens) {
		super();
		this.minTokens = minTokens;
		this.namePrefix = "PrefixTokens_Min" + this.minTokens;
		this.extremumType = CorpRelFeatureSelf.ExtremumType.Maximum;
	}
	
	public CorpRelFeatureSelfPrefixTokens(int minTokens, StringUtil.StringTransform cleanFn) {
		this(minTokens);
		this.cleanFn = cleanFn;
	}
	
	protected double selfCompare(String mentioner, String mentioned) {
		return StringUtil.prefixTokenOverlap(mentioner, mentioned) >= this.minTokens ? 1.0 : 0.0;
	}

	@Override
	public CorpRelFeature clone() {
		return new CorpRelFeatureSelfPrefixTokens(this.minTokens, this.cleanFn);
	}
	
	@Override
	public String toString(boolean withInit) {
		return "SelfPrefixTokens(cleanFn=" + this.cleanFn.toString() + ", minTokens=" + this.minTokens + ")";
	}
}
