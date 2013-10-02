package corp.data.feature;

import corp.data.Gazetteer;
import corp.util.StringUtil;

public class CorpRelFeatureGazetteerPrefixTokens  extends CorpRelFeatureGazetteer {
	private int minTokens;
	
	public CorpRelFeatureGazetteerPrefixTokens(Gazetteer gazetteer, CorpRelFeatureGazetteer.InputType inputType, int minTokens) {
		this.extremumType = CorpRelFeatureGazetteer.ExtremumType.Maximum;
		this.inputType = inputType;
		this.namePrefix = "PrefixTokens_Min" + this.minTokens;
		this.gazetteer = gazetteer;
		this.minTokens = minTokens;
	}

	@Override
	protected double extremum(String str) {
		double tokenPrefixCount = this.gazetteer.max(str, new StringUtil.StringPairMeasure() {
			public double compute(String str1, String str2) {
				return StringUtil.prefixTokenOverlap(str1, str2);
			}
		});
		
		if (tokenPrefixCount >= this.minTokens)
			return 1.0;
		else
			return 0.0;
	}

	@Override
	public CorpRelFeature clone() {
		return new CorpRelFeatureGazetteerPrefixTokens(this.gazetteer, this.inputType, this.minTokens);
	}
}
