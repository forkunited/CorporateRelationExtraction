package corp.data.feature;

import corp.util.StringUtil;

public class CorpRelFeatureSelfInitialism extends CorpRelFeatureSelf {
	private boolean allowPrefix;
	
	public CorpRelFeatureSelfInitialism(boolean allowPrefix) {
		this.namePrefix = "Initialism";
		this.extremumType = CorpRelFeatureSelf.ExtremumType.Maximum;
		this.allowPrefix = allowPrefix;
	}
	
	protected double selfCompare(String mentioner, String mentioned) {
		return (StringUtil.isInitialism(mentioned, mentioner, this.allowPrefix) || StringUtil.isInitialism(mentioned, mentioner, this.allowPrefix)) ? 1.0 : 0.0;
	}

	@Override
	public CorpRelFeature clone() {
		return new CorpRelFeatureSelfInitialism(this.allowPrefix);
	}

}
