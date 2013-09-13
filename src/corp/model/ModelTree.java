package corp.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import corp.data.annotation.CorpRelDatum;
import corp.data.annotation.CorpRelLabelPath;
import corp.data.feature.CorpRelFeaturizedDataSet;
import corp.data.feature.CorpRelFeaturizedDatum;
import edu.stanford.nlp.util.Pair;

/**
 * 
 * The basic idea of a CReg Tree Model is that it should call multiple CReg
 * Models and make a prediction. It may also use the posteriors provided by the
 * basic CReg Models.
 * 
 * @author Lingpeng Kong
 * 
 */
public class ModelTree extends Model {
	// This represents a tree as a set of paths mapped to models...
	// It's not the most space-efficient representation... but the tree is small... it will work for now.
	private Map<CorpRelLabelPath, Model> models = new HashMap<CorpRelLabelPath, Model>();
	private boolean allowSubpaths;
	
	public ModelTree(boolean allowSubpaths) {
		this.validPaths = new ArrayList<CorpRelLabelPath>();
		this.validPaths.add(new CorpRelLabelPath());
		
		this.allowSubpaths = allowSubpaths;
		this.models = new HashMap<CorpRelLabelPath, Model>();
	}
	
	public boolean addModel(CorpRelLabelPath path, Model model) {
		if (!this.validPaths.contains(path))
			return false;
		
		if (!this.allowSubpaths)
			this.validPaths.remove(path);
		
		
		for (int i = 0; i < model.validPaths.size(); i++) {
			if (path.size() != model.validPaths.get(i).size() - 1 || !model.validPaths.get(i).isPrefixedBy(path))
				return false;
		}
		
		this.models.put(path, model);
		for (int i = 0; i < model.validPaths.size(); i++) {
			if (!this.validPaths.contains(model.validPaths.get(i)))
				this.validPaths.add(model.validPaths.get(i));
		}
		
		return true;
	}

	@Override
	public boolean train(CorpRelFeaturizedDataSet data, String outputPath) {
		// TODO Output serialized model for deserialization later
		for (Entry<CorpRelLabelPath, Model> entry : this.models.entrySet()) {
			List<CorpRelDatum> pathData = data.getDataUnderPath(entry.getKey(), this.allowSubpaths);
			CorpRelFeaturizedDataSet pathDataSet = new CorpRelFeaturizedDataSet();
			pathDataSet.addData(pathData);
			if (!entry.getValue().train(pathDataSet, outputPath + "." + entry.getKey().toString()))
				return false;
		}
		
		return true;
	}
	
	@Override
	public List<Pair<CorpRelFeaturizedDatum, Map<CorpRelLabelPath, Double>>> posterior(CorpRelFeaturizedDataSet data) {
		List<Pair<CorpRelFeaturizedDatum, Map<CorpRelLabelPath, Double>>> posterior = new ArrayList<Pair<CorpRelFeaturizedDatum, Map<CorpRelLabelPath, Double>>>();
		if (this.models.size() == 0)
			return null;
		
		Map<CorpRelLabelPath, List<Pair<CorpRelFeaturizedDatum, Map<CorpRelLabelPath, Double>>>> modelPosteriors = new HashMap<CorpRelLabelPath, List<Pair<CorpRelFeaturizedDatum, Map<CorpRelLabelPath, Double>>>>();
		for (Entry<CorpRelLabelPath, Model> entry : this.models.entrySet()) {
			modelPosteriors.put(entry.getKey(), entry.getValue().posterior(data));
		}
		
		List<CorpRelFeaturizedDatum> datums = data.getFeaturizedData();
		for (int i = 0; i < datums.size(); i++) {
			Map<CorpRelLabelPath, Double> posteriorForDatum = posteriorForDatum(i, modelPosteriors);
			posterior.add(new Pair<CorpRelFeaturizedDatum, Map<CorpRelLabelPath, Double>>(datums.get(i), posteriorForDatum));
		}
		
		return posterior;
	}
	
	private Map<CorpRelLabelPath, Double> posteriorForDatum(int index, 
															Map<CorpRelLabelPath, List<Pair<CorpRelFeaturizedDatum, Map<CorpRelLabelPath, Double>>>> modelPosteriors) {
		Map<CorpRelLabelPath, Double> posterior = new HashMap<CorpRelLabelPath, Double>();
		Map<CorpRelLabelPath, Double> partialPosterior = new HashMap<CorpRelLabelPath, Double>();
		for (CorpRelLabelPath validPath : this.validPaths) {
			Double pValue = posteriorForDatumHelper(index, modelPosteriors, partialPosterior, validPath);
			if (this.allowSubpaths && modelPosteriors.containsKey(validPath)) {
				CorpRelLabelPath prefixPath = validPath.getPrefix(validPath.size() - 1);
				pValue *= modelPosteriors.get(prefixPath).get(index).second().get(validPath);
			}
			posterior.put(validPath,  pValue);
		}
		return posterior;
	}
	
	private Double posteriorForDatumHelper(int index, 
										   Map<CorpRelLabelPath, List<Pair<CorpRelFeaturizedDatum, Map<CorpRelLabelPath, Double>>>> modelPosteriors,
										   Map<CorpRelLabelPath, Double> partialPosterior,
										   CorpRelLabelPath path) {
		if (path.size() == 0)
			return 1.0;
		if (partialPosterior.containsKey(path))
			return partialPosterior.get(path);
		
		CorpRelLabelPath prefixPath = path.getPrefix(path.size() - 1);
		Map<CorpRelLabelPath, Double> modelP = modelPosteriors.get(prefixPath).get(index).second();
		if (!modelP.containsKey(path))
			return 0.0;
		
		Double pValue = modelP.get(path) * posteriorForDatumHelper(index, modelPosteriors, partialPosterior, prefixPath);
		partialPosterior.put(path, pValue);
		
		return pValue;
		
	}
	
	@Override
	public boolean deserialize(String modelPath) {
		// TODO Implement this after fixing train to output the model
		throw new UnsupportedOperationException();
	}

	
	@Override
	public Model clone() {
		ModelTree cloneModel = new ModelTree(this.allowSubpaths);
		for (Entry<CorpRelLabelPath, Model> entry : this.models.entrySet()) {
			cloneModel.models.put(entry.getKey(), entry.getValue());
		}
		
		cloneModel.validPaths = new ArrayList<CorpRelLabelPath>();
		for (CorpRelLabelPath validPath : this.validPaths) {
			cloneModel.validPaths.add(validPath);
		}
		
		return cloneModel;
	}
}