package corp.data.feature;

import corp.util.StringUtil;

public class CorpRelFeatureSelfInitialism extends CorpRelFeatureSelf {
	public CorpRelFeatureSelfInitialism() {
		this.namePrefix = "Initialism";
		this.extremumType = CorpRelFeatureSelf.ExtremumType.Maximum;
	}
	
	protected double selfCompare(String mentioner, String mentioned) {
		return (StringUtil.isInitialism(mentioned, mentioner) || StringUtil.isInitialism(mentioned, mentioner)) ? 1.0 : 0.0;
	}

	@Override
	public CorpRelFeature clone() {
		return new CorpRelFeatureSelfInitialism();
	}

}
