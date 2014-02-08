package corp.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import corp.data.CorpDataTools;
import corp.data.annotation.CorpRelDatum;
import corp.data.annotation.CorpRelLabelPath;
import corp.data.feature.CorpRelFeaturizedDataSet;
import corp.data.feature.CorpRelFeaturizedDatum;
import ark.util.OutputWriter;
import edu.stanford.nlp.util.Pair;

/**
 * 
 * ModelUniform represents a model that assigns equal probability to each
 * of the possible corporate relationship labels.  This kind of model, 
 * constrained to a single label that is always given probability 1, is used
 * within a ModelTree to develop different levels of the taxonomy in 
 * isolation.  For example, to develop the "Other-corporation" model alone,
 * we build a ModelTree with a uniform model at the root that has 
 * "Other-corporation" as its only valid label, and with a logistic 
 * regression (ModelCReg) at "Other-corporation".  This results in the 
 * ModelTree only training and evaluating the "Other-corporation" branch
 * of the taxonomy.
 * 
 * @author Bill McDowell
 *
 */
public class ModelUniform extends Model {
	private String modelPath;
	private OutputWriter output;
	
	public ModelUniform(List<CorpRelLabelPath> validPaths, OutputWriter output) {
		this.validPaths = validPaths;
		this.output = output;
		this.hyperParameters = new HashMap<String, Double>();
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
	public boolean deserialize(String modelPath, CorpDataTools dataTools) {
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

	@Override
	public String toString() {
		return "";
	}
}
