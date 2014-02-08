package corp.data.feature;

import ark.util.StringUtil;

/**
 * 
 * For organization mention m, CorpRelFeatureSelfEquality computes 
 * 
 * 1(C(A(m))=C(O(m)))
 * 
 * Where A(m) is the authoring corporation, and O(m) is the mentioned 
 * organization, and C is a string cleaning function.
 * 
 * @author Bill McDowell
 *
 */
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
	
	@Override
	public String toString(boolean withInit) {
		return "SelfEquality(cleanFn=" + this.cleanFn.toString() + ")";
	}
}
