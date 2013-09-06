package corp.data.feature;

import corp.data.Gazette;

public class CorpRelFeatureGazetteContains extends CorpRelFeatureGazette {
	public CorpRelFeatureGazetteContains(Gazette gazette, CorpRelFeatureGazette.InputType inputType) {
		this.extremumType = CorpRelFeatureGazette.ExtremumType.Maximum;
		this.inputType = inputType;
		this.namePrefix = "Contains";
		this.gazette = gazette;
	}

	@Override
	protected double extremum(String str) {
		if (this.gazette.contains(str))
			return 1.0;
		else 
			return 0.0;
	}

	@Override
	public CorpRelFeature clone() {
		return new CorpRelFeatureGazetteContains(this.gazette, this.inputType);
	}
}
