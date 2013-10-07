package corp.data.feature;

import corp.util.StringUtil;

public class CorpRelFeatureSelfEquality extends CorpRelFeatureSelf {
	public CorpRelFeatureSelfEquality() {
		super();
		this.namePrefix = "Equality";
		this.extremumType = CorpRelFeatureSelf.ExtremumType.Maximum;
	}
	
	public CorpRelFeatureSelfEquality(StringUtil.StringTransform cleanFn) {
		this();
		this.cleanFn = cleanFn;
	}
	
	protected double selfCompare(String mentioner, String mentioned) {
		return mentioner.equals(mentioned) ? 1.0 : 0.0;
	}

	@Override
	public CorpRelFeature clone() {
		return new CorpRelFeatureSelfEditDistance(this.cleanFn);
	}
}
