package corp.data.feature;

import corp.util.StringUtil;

public class CorpRelFeatureSelfPrefixTokens extends CorpRelFeatureSelf {
	private int minTokens;
	
	public CorpRelFeatureSelfPrefixTokens(int minTokens) {
		this.namePrefix = "PrefixTokens";
		this.minTokens = minTokens;
	}
	
	protected double selfCompare(String mentioner, String mentioned) {
		return StringUtil.prefixTokenOverlap(mentioner, mentioned) >= this.minTokens ? 1.0 : 0.0;
	}

	@Override
	public CorpRelFeature clone() {
		return new CorpRelFeatureSelfPrefixTokens(this.minTokens);
	}

}
