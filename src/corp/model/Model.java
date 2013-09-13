package corp.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import corp.data.annotation.CorpRelLabelPath;
import corp.data.feature.CorpRelFeaturizedDataSet;
import corp.data.feature.CorpRelFeaturizedDatum;
import edu.stanford.nlp.util.Pair;

public abstract class Model {
	protected List<CorpRelLabelPath> validPaths;
	
	public List<CorpRelLabelPath> getValidLabelPaths() {
		return this.validPaths;
	}
	
	public List<Pair<CorpRelFeaturizedDatum, CorpRelLabelPath>> classify(CorpRelFeaturizedDataSet data) {
		List<Pair<CorpRelFeaturizedDatum, CorpRelLabelPath>> classifiedData = new ArrayList<Pair<CorpRelFeaturizedDatum, CorpRelLabelPath>>();
		List<Pair<CorpRelFeaturizedDatum, Map<CorpRelLabelPath, Double>>> posterior = posterior(data);
	
		for (Pair<CorpRelFeaturizedDatum, Map<CorpRelLabelPath, Double>> datumPosterior : posterior) {
			Map<CorpRelLabelPath, Double> p = datumPosterior.second();
			double max = Double.NEGATIVE_INFINITY;
			CorpRelLabelPath argMax = null;
			for (Entry<CorpRelLabelPath, Double> entry : p.entrySet()) {
				if (entry.getValue() > max) {
					max = entry.getValue();
					argMax = entry.getKey();
				}
			}
			classifiedData.add(new Pair<CorpRelFeaturizedDatum, CorpRelLabelPath>(datumPosterior.first(), argMax));
		}
	
		return classifiedData;
	}
	
	public abstract boolean deserialize(String modelPath);
	public abstract boolean train(CorpRelFeaturizedDataSet data, String outputPath);
	public abstract List<Pair<CorpRelFeaturizedDatum, Map<CorpRelLabelPath, Double>>> posterior(CorpRelFeaturizedDataSet data);
	public abstract Model clone();
}
