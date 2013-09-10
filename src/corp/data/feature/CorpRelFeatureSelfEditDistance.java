package corp.data.feature;

import corp.util.StringUtil;

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
		return new CorpRelFeatureSelfEditDistance();
	}

}
