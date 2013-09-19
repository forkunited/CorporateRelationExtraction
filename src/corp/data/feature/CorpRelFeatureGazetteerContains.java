package corp.data.feature;

import corp.data.Gazetteer;

public class CorpRelFeatureGazetteerContains extends CorpRelFeatureGazetteer {
	public CorpRelFeatureGazetteerContains(Gazetteer gazetteer, CorpRelFeatureGazetteer.InputType inputType) {
		this.extremumType = CorpRelFeatureGazetteer.ExtremumType.Maximum;
		this.inputType = inputType;
		this.namePrefix = "Contains";
		this.gazetteer = gazetteer;
	}

	@Override
	protected double extremum(String str) {
		if (this.gazetteer.contains(str))
			return 1.0;
		else 
			return 0.0;
	}

	@Override
	public CorpRelFeature clone() {
		return new CorpRelFeatureGazetteerContains(this.gazetteer, this.inputType);
	}
}
