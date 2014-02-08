package corp.data.feature;

import ark.data.Gazetteer;
import ark.util.StringUtil;

/**
 * For organization mention m and gazetteer G, 
 * CorpRelFeatureGazetteerInitialism computes
 * 
 * max_{g\in G} 1(O(m) is an initialism for g)
 * 
 * Or 
 * 
 * max_{g\in G} 1(A(m) is an initialism for g)
 * 
 * Where O(m) is the mentioned organization and A(m) is the authoring 
 * corporation.  Whether A(m) or O(m) is used is determined by the
 * "input type".
 * 
 * @author Bill McDowell
 *
 */
public class CorpRelFeatureGazetteerInitialism  extends CorpRelFeatureGazetteer {
	private boolean allowPrefix;
	
	public CorpRelFeatureGazetteerInitialism(Gazetteer gazetteer, CorpRelFeatureGazetteer.InputType inputType, boolean allowPrefix) {
		this.extremumType = CorpRelFeatureGazetteer.ExtremumType.Maximum;
		this.inputType = inputType;
		this.allowPrefix = allowPrefix;
		this.namePrefix = "Initialism_Prefix" + ((this.allowPrefix) ? 1 : 0);
		this.gazetteer = gazetteer;
	}

	@Override
	protected double extremum(String str) {
		return this.gazetteer.max(str, new StringUtil.StringPairMeasure() {
			public double compute(String str1, String str2) {
				if (StringUtil.isInitialism(str1, str2, allowPrefix))
					return 1.0;
				else
					return 0.0;
			}
		});
	}

	@Override
	public CorpRelFeature clone() {
		return new CorpRelFeatureGazetteerInitialism(this.gazetteer, this.inputType, this.allowPrefix);
	}
	
	@Override
	public String toString(boolean withInit) {
		return "GazetteerInitialism(gazetteer=" + this.gazetteer.getName() + "Gazetteer, inputType=" + this.inputType + ", allowPrefix=" + this.allowPrefix + ")";
	}
}
