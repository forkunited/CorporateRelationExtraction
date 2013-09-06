package corp.data.feature;

import corp.util.StringUtil;

public class CorpRelFeatureSelfEditDistance extends CorpRelFeatureSelf {
	public CorpRelFeatureSelfEditDistance() {
		this.namePrefix = "EditDistance";
	}
	
	protected double selfCompare(String mentioner, String mentioned) {
		return -StringUtil.levenshteinDistance(mentioner, mentioned);
	}

	@Override
	public CorpRelFeature clone() {
		return new CorpRelFeatureSelfEditDistance();
	}

}
