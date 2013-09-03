package corp.model;

import java.util.HashMap;
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
	
	public boolean train(CorpRelFeaturizedDataSet data, String outputPath) {
		return train(data.getFeaturizedData(), outputPath);
	}
	
	public List<Pair<CorpRelFeaturizedDatum, CorpRelLabel>> classify(CorpRelFeaturizedDataSet data) {
		return classify(data.getFeaturizedData());
	}
	
	public List<Pair<CorpRelFeaturizedDatum, HashMap<CorpRelLabel, Double>>> posterior(CorpRelFeaturizedDataSet data) {
		return posterior(data.getFeaturizedData());
	}
	
	public abstract boolean deserialize(String modelPath);
	public abstract boolean train(List<CorpRelFeaturizedDatum> data, String outputPath);
	public abstract List<Pair<CorpRelFeaturizedDatum, CorpRelLabel>> classify(List<CorpRelFeaturizedDatum> data);
	public abstract List<Pair<CorpRelFeaturizedDatum, HashMap<CorpRelLabel, Double>>> posterior(List<CorpRelFeaturizedDatum> data);
}
