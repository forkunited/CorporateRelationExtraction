package corp.data.feature;

import corp.data.Gazetteer;
import corp.util.StringUtil;

/**
 * For organization mention m and gazetteer G, 
 * CorpRelFeatureGazetteerEditDistance computes
 * 
 * max_{g\in G} E(g, O(m))
 * 
 * Or 
 * 
 * max_{g\in G} E(g, A(m))
 * 
 * Where E is normalized edit distance, O(m) is the mentioned 
 * organization and A(m) is the authoring  corporation.  Whether A(m) or O(m) 
 * is used is determined by the "input type".
 * 
 * @author Bill McDowell
 *
 */
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
