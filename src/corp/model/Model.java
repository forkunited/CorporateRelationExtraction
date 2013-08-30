package corp.model;

import java.util.List;

import corp.data.annotation.CorpRelLabel;
import corp.data.feature.CorpRelFeaturizedDataSet;
import corp.data.feature.CorpRelFeaturizedDatum;
import edu.stanford.nlp.util.Pair;

public abstract class Model {
	protected List<CorpRelLabel> validLabels;
	
	public boolean setValidLabels(List<CorpRelLabel> validLabels) {
		this.validLabels = validLabels;
		return true;
	}
	
	public abstract boolean deserialize(String modelPath);
	public abstract boolean train(CorpRelFeaturizedDataSet data, String outputPath);
	public abstract List<Pair<CorpRelFeaturizedDatum, CorpRelLabel>> classify(CorpRelFeaturizedDataSet data);
}
