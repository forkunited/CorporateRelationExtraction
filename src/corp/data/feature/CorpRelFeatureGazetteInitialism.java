package corp.data.feature;

import corp.data.Gazette;
import corp.util.StringUtil;

public class CorpRelFeatureGazetteInitialism  extends CorpRelFeatureGazette {
	private boolean allowPrefix;
	
	public CorpRelFeatureGazetteInitialism(Gazette gazette, CorpRelFeatureGazette.InputType inputType, boolean allowPrefix) {
		this.extremumType = CorpRelFeatureGazette.ExtremumType.Maximum;
		this.inputType = inputType;
		this.namePrefix = "Initialism";
		this.gazette = gazette;
		this.allowPrefix = allowPrefix;
	}

	@Override
	protected double extremum(String str) {
		return this.gazette.max(str, new StringUtil.StringPairMeasure() {
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
		return new CorpRelFeatureGazetteInitialism(this.gazette, this.inputType, this.allowPrefix);
	}
}
