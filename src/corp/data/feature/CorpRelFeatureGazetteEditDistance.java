package corp.data.feature;

import corp.data.Gazette;
import corp.util.StringUtil;

public class CorpRelFeatureGazetteEditDistance extends CorpRelFeatureGazette {
	public CorpRelFeatureGazetteEditDistance(Gazette gazette, CorpRelFeatureGazette.InputType inputType) {
		this.extremumType = CorpRelFeatureGazette.ExtremumType.Minimum;
		this.inputType = inputType;
		this.namePrefix = "EditDistance";
		this.gazette = gazette;
	}

	@Override
	protected double extremum(String str) {
		return this.gazette.min(str, new StringUtil.StringPairMeasure() {
			public double compute(String str1, String str2) {
				return StringUtil.levenshteinDistance(str1, str2);
			}
		});
	}

	@Override
	public CorpRelFeature clone() {
		return new CorpRelFeatureGazetteEditDistance(this.gazette, this.inputType);
	}
}
