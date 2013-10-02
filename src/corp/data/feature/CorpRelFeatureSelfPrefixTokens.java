package corp.data.feature;

import corp.util.StringUtil;

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

}
