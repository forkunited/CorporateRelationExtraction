package corp.data.feature;

import corp.util.StringUtil;

public class CorpRelFeatureSelfInitialism extends CorpRelFeatureSelf {
	private boolean allowPrefix;
	
	public CorpRelFeatureSelfInitialism(boolean allowPrefix) {
		super();
		this.allowPrefix = allowPrefix;
		this.namePrefix = "Initialism_Prefix" + ((this.allowPrefix) ? 1 : 0);
		this.extremumType = CorpRelFeatureSelf.ExtremumType.Maximum;
	}
	
	public CorpRelFeatureSelfInitialism(boolean allowPrefix, StringUtil.StringTransform cleanFn) {
		this(allowPrefix);
		this.cleanFn = cleanFn;
	}
	
	protected double selfCompare(String mentioner, String mentioned) {
		return (StringUtil.isInitialism(mentioned, mentioner, this.allowPrefix) || StringUtil.isInitialism(mentioned, mentioner, this.allowPrefix)) ? 1.0 : 0.0;
	}

	@Override
	public CorpRelFeature clone() {
		return new CorpRelFeatureSelfInitialism(this.allowPrefix, this.cleanFn);
	}

}
