package corp.model;

import java.util.HashMap;
import java.util.List;

import corp.data.annotation.CorpRelLabel;
import corp.data.feature.CorpRelFeaturizedDatum;
import edu.stanford.nlp.util.Pair;

/**
 * 
 * Here's where to put the tree model
 * @author Lingpeng
 *
 */
public class ModelCRegTree extends Model {
	@Override
	public List<Pair<CorpRelFeaturizedDatum, CorpRelLabel>> classify(
			List<CorpRelFeaturizedDatum> data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean deserialize(String modelPath) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean train(List<CorpRelFeaturizedDatum> data, String outputPath) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Pair<CorpRelFeaturizedDatum, HashMap<CorpRelLabel, Double>>> posterior(
			List<CorpRelFeaturizedDatum> data) {
		// TODO Auto-generated method stub
		return null;
	}

}
