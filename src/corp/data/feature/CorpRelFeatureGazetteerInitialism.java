package corp.data.feature;

import corp.data.Gazetteer;
import corp.util.StringUtil;

public class CorpRelFeatureGazetteerInitialism  extends CorpRelFeatureGazetteer {
	private boolean allowPrefix;
	
	public CorpRelFeatureGazetteerInitialism(Gazetteer gazetteer, CorpRelFeatureGazetteer.InputType inputType, boolean allowPrefix) {
		this.extremumType = CorpRelFeatureGazetteer.ExtremumType.Maximum;
		this.inputType = inputType;
		this.allowPrefix = allowPrefix;
		this.namePrefix = "Initialism_Prefix" + ((this.allowPrefix) ? 1 : 0);
		this.gazetteer = gazetteer;
	}

	@Override
	protected double extremum(String str) {
		return this.gazetteer.max(str, new StringUtil.StringPairMeasure() {
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
		return new CorpRelFeatureGazetteerInitialism(this.gazetteer, this.inputType, this.allowPrefix);
	}
}
