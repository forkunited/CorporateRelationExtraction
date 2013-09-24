package corp.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import corp.data.annotation.CorpRelDatum;
import corp.data.annotation.CorpRelLabelPath;
import corp.data.feature.CorpRelFeaturizedDataSet;
import corp.data.feature.CorpRelFeaturizedDatum;
import corp.util.OutputWriter;
import edu.stanford.nlp.util.Pair;

public class ModelUniform extends Model {
	private String modelPath;
	private OutputWriter output;
	
	public ModelUniform(List<CorpRelLabelPath> validPaths, OutputWriter output) {
		this.validPaths = validPaths;
		this.output = output;
	}
	
	@Override
	public List<Pair<CorpRelDatum, Map<CorpRelLabelPath, Double>>> posterior(CorpRelFeaturizedDataSet data) {
		List<CorpRelFeaturizedDatum> datums = data.getFeaturizedData();
		List<Pair<CorpRelDatum, Map<CorpRelLabelPath, Double>>> posterior = new ArrayList<Pair<CorpRelDatum, Map<CorpRelLabelPath, Double>>>();
		Map<CorpRelLabelPath, Double> constantP = new HashMap<CorpRelLabelPath, Double>();
		for (CorpRelLabelPath path : this.validPaths) {
			constantP.put(path, 1.0/this.validPaths.size());
		}
		for (CorpRelFeaturizedDatum datum : datums) {
			posterior.add(new Pair<CorpRelDatum, Map<CorpRelLabelPath, Double>>(datum, constantP));
		}
		
		return posterior;
	}
	
	@Override
	public boolean deserialize(String modelPath) {
		return false;
	}

	@Override
	public boolean train(CorpRelFeaturizedDataSet data, String outputPath) {
		return true;
	}
	
	@Override
	public Model clone() {
		ModelUniform cloneModel = new ModelUniform(this.validPaths, this.output);
		cloneModel.modelPath = this.modelPath;
		return cloneModel;
	}

}
