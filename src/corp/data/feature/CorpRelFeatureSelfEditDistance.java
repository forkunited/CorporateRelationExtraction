package corp.data.feature;

import corp.util.StringUtil;

public class CorpRelFeatureSelfEditDistance extends CorpRelFeatureSelf {
	public CorpRelFeatureSelfEditDistance() {
		this.namePrefix = "EditDistance";
		this.extremumType = CorpRelFeatureSelf.ExtremumType.Minimum;
	}
	
	protected double selfCompare(String mentioner, String mentioned) {
		return StringUtil.levenshteinDistance(mentioner, mentioned)/((double)(mentioner.length() + mentioned.length()));
	}

	@Override
	public CorpRelFeature clone() {
		return new CorpRelFeatureSelfEditDistance();
	}

}
