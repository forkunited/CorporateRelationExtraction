package corp.data.feature;

import java.util.List;
import corp.data.annotation.CorpRelDatum;

/**
 * 
 * CorpRelFeaturizedDatum extends corp.data.annotation.CorpRelDatum to
 * represent a single featurized organization mention within a press 
 * release document.  Each mention consists of several token spans 
 * (representing several instances of the same organization name), 
 * an optional relationship label taxonomy path if the mention is 
 * annotated, and a feature value vector computed from the text
 * surrounding the mention.
 * 
 * @author Bill McDowell
 *
 */
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
