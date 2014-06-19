package corp.data.feature;

import corp.util.StringUtil;

/**
 * 
 * For organization mention m, CorpRelFeatureSelfInitialism computes 
 * 
 * 1(A(m) is an initialism for O(m) or O(m) is an initialism for A(m))
 * 
 * Where A(m) is the authoring organization and O(m) is the mentioned 
 * organization.
 * 
 * @author Bill McDowell
 *
 */
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
	
	@Override
	public String toString(boolean withInit) {
		return "SelfInitialism(cleanFn=" + this.cleanFn.toString() + ", allowPrefix=" + this.allowPrefix + ")";
	}

}
