package corp.data.feature;

import java.util.List;

import corp.data.Gazette;
import corp.data.annotation.CorpDocument;
import corp.data.annotation.CorpRelDatum;

public class CorpRelFeatureGazetteContains extends CorpRelFeatureGazette {
	public CorpRelFeatureGazetteContains(List<CorpDocument> documents, List<CorpRelDatum> data, Gazette gazette, CorpRelFeatureGazette.InputType inputType) {
		this.extremumType = CorpRelFeatureGazette.ExtremumType.Maximum;
		this.inputType = inputType;
		this.namePrefix = "Contains";
		this.gazette = gazette;
		init(documents, data);
	}

	@Override
	protected double extremum(String str) {
		if (this.gazette.contains(str))
			return 1.0;
		else 
			return 0.0;
	}
}
