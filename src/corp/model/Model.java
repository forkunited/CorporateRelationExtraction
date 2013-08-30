package corp.model;

import corp.data.feature.CorpRelFeaturizedDataSet;

public abstract class Model {
	public abstract void train(CorpRelFeaturizedDataSet data);
	public abstract void classify(CorpRelFeaturizedDataSet data);
}
