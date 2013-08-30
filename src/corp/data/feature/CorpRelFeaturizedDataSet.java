package corp.data.feature;

import java.util.ArrayList;
import java.util.List;

import corp.data.annotation.CorpDocumentSet;
import corp.data.annotation.CorpRelDataSet;
import corp.data.annotation.CorpRelDatum;
import corp.data.annotation.CorpRelLabel;

public class CorpRelFeaturizedDataSet extends CorpRelDataSet {
	private List<CorpRelFeature> features;
	
	public CorpRelFeaturizedDataSet(CorpDocumentSet sourceDocuments) {
		this(sourceDocuments, new ArrayList<CorpRelFeature>());
	}
	
	public CorpRelFeaturizedDataSet(CorpDocumentSet sourceDocuments, List<CorpRelFeature> features) {
		super(sourceDocuments);
		this.features = features;
	}
	
	public boolean addFeature(CorpRelFeature feature) {
		return this.features.add(feature);
	}
	
	public boolean removeFeature(CorpRelFeature feature) {
		return this.features.remove(feature);
	}
	
	public List<String> getFeatureNames() {
		List<String> featureNames = new ArrayList<String>();
		for (CorpRelFeature feature : this.features)
			feature.getNames(featureNames);
		return featureNames;
	}
	
	public CorpRelFeature getFeature(int index) {
		return this.features.get(index);
	}
	
	public int getFeatureCount() {
		return this.features.size();
	}
	
	public List<CorpRelFeaturizedDatum> getFeaturizedLabeledData() {
		return featurize(getLabeledData());
	}
	
	public List<CorpRelFeaturizedDatum> getFeaturizedUnlabeledData() {
		return featurize(getUnlabeledData());
	}
	
	public List<CorpRelFeaturizedDatum> getFeaturizedData() {
		return featurize(getData());
	}
	
	public List<CorpRelFeaturizedDatum> getFeaturizedDataUnderLabel(CorpRelLabel root, boolean includeRoot) {
		return featurize(getDataUnderLabel(root, includeRoot));
	}
	
	public List<CorpRelFeaturizedDatum> getFeaturizedDataInLabel(CorpRelLabel label) {
		return featurize(getDataInLabel(label));
	}
	
	private List<CorpRelFeaturizedDatum> featurize(List<CorpRelDatum> data) {
		List<CorpRelFeaturizedDatum> featurizedData = new ArrayList<CorpRelFeaturizedDatum>();
		for (CorpRelDatum datum : data) {
			List<Double> featureValues = new ArrayList<Double>();
			for (CorpRelFeature feature : this.features) {
				featureValues = feature.computeVector(datum, featureValues);
			}
			featurizedData.add(new CorpRelFeaturizedDatum(this, datum, featureValues));
		}
		
		return featurizedData;
	}
	
}
