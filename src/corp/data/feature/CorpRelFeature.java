package corp.data.feature;

import java.util.ArrayList;
import java.util.List;

import corp.data.annotation.CorpDocument;
import corp.data.annotation.CorpRelDatum;

public abstract class CorpRelFeature {
	public CorpRelFeature(List<CorpDocument> documents, List<CorpRelDatum> data) {
		init(documents, data);
	}
	
	protected abstract void init(List<CorpDocument> documents, List<CorpRelDatum> data);
	public abstract List<String> getNames(List<String> existingNames);
	public abstract List<Double> computeVector(CorpRelDatum datum, List<Double> existingVector);
	public abstract List<List<Double>> computeMatrix(List<CorpRelDatum> data, List<List<Double>> existingMatrix);

	public List<String> getNames() {
		return getNames(new ArrayList<String>());
	}
	
	public List<Double> computeVector(CorpRelDatum datum) {
		return computeVector(datum, new ArrayList<Double>());
	}
	
	public List<List<Double>> computeMatrix(List<CorpRelDatum> data) {
		List<List<Double>> existingMatrix = new ArrayList<List<Double>>();
		for (int i = 0; i < data.size(); i++)
			existingMatrix.add(new ArrayList<Double>());
		return computeMatrix(data, existingMatrix);
	}
}
