package corp.data.feature;

import java.util.List;

import corp.data.Gazette;
import corp.data.annotation.CorpDocument;
import corp.data.annotation.CorpRelDatum;
import corp.util.StringUtil;

public class CorpRelFeatureGazettePrefixTokens  extends CorpRelFeatureGazette {
	private int minTokens;
	
	public CorpRelFeatureGazettePrefixTokens(List<CorpDocument> documents, List<CorpRelDatum> data, Gazette gazette, CorpRelFeatureGazette.InputType inputType, int minTokens) {
		this.extremumType = CorpRelFeatureGazette.ExtremumType.Maximum;
		this.inputType = inputType;
		this.namePrefix = "PrefixTokens";
		this.gazette = gazette;
		this.minTokens = minTokens;
		init(documents, data);
	}

	@Override
	protected double extremum(String str) {
		double tokenPrefixCount = this.gazette.max(str, new StringUtil.StringPairMeasure() {
			public double compute(String str1, String str2) {
				return StringUtil.prefixTokenOverlap(str1, str2);
			}
		});
		
		if (tokenPrefixCount >= this.minTokens)
			return 1.0;
		else
			return 0.0;
	}
}
