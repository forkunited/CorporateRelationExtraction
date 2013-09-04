package corp.data.feature;

import java.util.List;

import corp.data.Gazette;
import corp.data.annotation.CorpDocument;
import corp.data.annotation.CorpRelDatum;
import corp.util.StringUtil;

public class CorpRelFeatureGazetteEditDistance extends CorpRelFeatureGazette {
	public CorpRelFeatureGazetteEditDistance(List<CorpDocument> documents, List<CorpRelDatum> data, Gazette gazette, CorpRelFeatureGazette.InputType inputType) {
		this.extremumType = CorpRelFeatureGazette.ExtremumType.Minimum;
		this.inputType = inputType;
		this.namePrefix = "EditDistance";
		this.gazette = gazette;
		init(documents, data);
	}

	@Override
	protected double extremum(String str) {
		return this.gazette.min(str, new StringUtil.StringPairMeasure() {
			public double compute(String str1, String str2) {
				return StringUtil.levenshteinDistance(str1, str2);
			}
		});
	}
}
