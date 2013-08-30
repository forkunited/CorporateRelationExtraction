package corp.data.feature;

import java.util.List;
import corp.data.annotation.CorpRelDatum;

public class CorpRelFeaturizedDatum extends CorpRelDatum {
	private CorpRelFeaturizedDataSet sourceDataSet;
	private List<Double> featureValues;
	
	public CorpRelFeaturizedDatum(CorpRelFeaturizedDataSet sourceDataSet, CorpRelDatum sourceDatum, List<Double> featureValues) {
		super(sourceDatum);
		this.sourceDataSet = sourceDataSet;
		this.featureValues = featureValues;
	}
	
	public CorpRelFeaturizedDataSet getSourceDataSet() {
		return this.sourceDataSet;
	}
	
	public List<Double> getFeatureValues() {
		return this.featureValues;
	}
}
