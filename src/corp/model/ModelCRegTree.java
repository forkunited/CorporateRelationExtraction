package corp.model;

import java.util.List;

import corp.data.annotation.CorpRelLabel;
import corp.data.feature.CorpRelFeaturizedDataSet;
import corp.data.feature.CorpRelFeaturizedDatum;
import edu.stanford.nlp.util.Pair;

public class ModelCRegTree extends Model {
	@Override
	public List<Pair<CorpRelFeaturizedDatum, CorpRelLabel>> classify(
			CorpRelFeaturizedDataSet data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean deserialize(String modelPath) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean train(CorpRelFeaturizedDataSet data, String outputPath) {
		// TODO Auto-generated method stub
		return false;
	}

}
