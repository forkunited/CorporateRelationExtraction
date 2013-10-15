package corp.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import corp.data.annotation.CorpRelDatum;
import corp.data.annotation.CorpRelLabelPath;
import corp.data.feature.CorpRelFeature;
import corp.data.feature.CorpRelFeaturizedDataSet;
import edu.stanford.nlp.util.Pair;

public abstract class Model {
	protected List<CorpRelLabelPath> validPaths;
	protected List<CorpRelFeature> features;
	protected Map<String, Double> hyperParameters;
	protected boolean warmRestart;
	
	public void warmRestartOn() {
		this.warmRestart = true;
	}
	
	public List<CorpRelLabelPath> getValidLabelPaths() {
		return this.validPaths;
	}
	
	public List<Pair<CorpRelDatum, CorpRelLabelPath>> classify(CorpRelFeaturizedDataSet data) {
		List<Pair<CorpRelDatum, CorpRelLabelPath>> classifiedData = new ArrayList<Pair<CorpRelDatum, CorpRelLabelPath>>();
		List<Pair<CorpRelDatum, Map<CorpRelLabelPath, Double>>> posterior = posterior(data);
	
		for (Pair<CorpRelDatum, Map<CorpRelLabelPath, Double>> datumPosterior : posterior) {
			Map<CorpRelLabelPath, Double> p = datumPosterior.second();
			double max = Double.NEGATIVE_INFINITY;
			CorpRelLabelPath argMax = null;
			for (Entry<CorpRelLabelPath, Double> entry : p.entrySet()) {
				if (entry.getValue() > max) {
					max = entry.getValue();
					argMax = entry.getKey();
				}
			}
			classifiedData.add(new Pair<CorpRelDatum, CorpRelLabelPath>(datumPosterior.first(), argMax));
		}
	
		return classifiedData;
	}
	
	public void setHyperParameters(Map<String, Double> values) {
		for (Entry<String, Double> entry : values.entrySet()) {
			setHyperParameter(entry.getKey(), entry.getValue());
		}
	}
	
	public void setHyperParameter(String parameter, double value) {
		this.hyperParameters.put(parameter, value);
	}
	
	public double getHyperParameter(String parameter) {
		return this.hyperParameters.get(parameter);
	}
	
	public abstract boolean deserialize(String modelPath);
	public abstract boolean train(CorpRelFeaturizedDataSet data, String outputPath);
	public abstract List<Pair<CorpRelDatum, Map<CorpRelLabelPath, Double>>> posterior(CorpRelFeaturizedDataSet data);
	public abstract Model clone();
}
