package corp.data.feature;

import corp.data.Gazetteer;
import corp.util.StringUtil;

public class CorpRelFeatureGazetteerEditDistance extends CorpRelFeatureGazetteer {
	public CorpRelFeatureGazetteerEditDistance(Gazetteer gazetteer, CorpRelFeatureGazetteer.InputType inputType) {
		this.extremumType = CorpRelFeatureGazetteer.ExtremumType.Minimum;
		this.inputType = inputType;
		this.namePrefix = "EditDistance";
		this.gazetteer = gazetteer;
	}

	@Override
	protected double extremum(String str) {
		return this.gazetteer.min(str, new StringUtil.StringPairMeasure() {
			public double compute(String str1, String str2) {
				return StringUtil.levenshteinDistance(str1, str2)/((double)(str1.length()+str2.length()));
			}
		});
	}

	@Override
	public CorpRelFeature clone() {
		return new CorpRelFeatureGazetteerEditDistance(this.gazetteer, this.inputType);
	}
	
	@Override
	public String toString(boolean withInit) {
		return "GazetteerEditDistance(gazetteer=" + this.gazetteer.getName() + "Gazetteer, inputType=" + this.inputType + ")";
	}
}
