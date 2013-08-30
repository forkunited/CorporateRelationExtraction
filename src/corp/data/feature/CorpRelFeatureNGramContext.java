package corp.data.feature;

import java.util.List;

import corp.data.annotation.CorpDocument;
import corp.data.annotation.CorpRelDatum;

public class CorpRelFeatureNGramContext extends CorpRelFeature {

	public CorpRelFeatureNGramContext(List<CorpDocument> documents, List<CorpRelDatum> data) {
		super(documents, data);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void init(List<CorpDocument> documents, List<CorpRelDatum> data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<String> getNames(List<String> existingNames) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Double> computeVector(CorpRelDatum datum,
			List<Double> existingVector) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<List<Double>> computeMatrix(List<CorpRelDatum> data,
			List<List<Double>> existingMatrix) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
