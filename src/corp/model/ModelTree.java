package corp.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import corp.data.CorpDataTools;
import corp.data.annotation.CorpRelDatum;
import corp.data.annotation.CorpRelLabelPath;
import corp.data.feature.CorpRelFeature;
import corp.data.feature.CorpRelFeaturizedDataSet;
import corp.util.OutputWriter;
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
	private Map<CorpRelLabelPath, Model> models;
	private Map<CorpRelLabelPath, List<CorpRelFeature>> extraFeatures;
	private OutputWriter output;
	
	public ModelTree(String existingModelPath, OutputWriter output, CorpDataTools dataTools) {
		this(false, output);
		deserialize(existingModelPath, dataTools);
	}
	
	public ModelTree(boolean allowSubpaths, OutputWriter output) {
		this.validPaths = new ArrayList<CorpRelLabelPath>();
		this.validPaths.add(new CorpRelLabelPath());
		this.hyperParameters = new HashMap<String, Double>();
		this.models = new HashMap<CorpRelLabelPath, Model>();
		this.extraFeatures = new HashMap<CorpRelLabelPath, List<CorpRelFeature>>();
		this.output = output;
		
		setHyperParameter("allowSubpaths", (allowSubpaths) ? 1 : 0);
	}
	
	public boolean addModel(CorpRelLabelPath path, Model model, List<CorpRelFeature> extraFeatures) {
		if (!this.validPaths.contains(path))
			return false;
		
		if (this.getHyperParameter("allowSubpaths") == 0)
			this.validPaths.remove(path);
		
		
		for (int i = 0; i < model.validPaths.size(); i++) {
			if (path.size() != model.validPaths.get(i).size() - 1 || !model.validPaths.get(i).isPrefixedBy(path))
				return false;
		}
		
		this.models.put(path, model);
		this.extraFeatures.put(path, extraFeatures);
		for (int i = 0; i < model.validPaths.size(); i++) {
			if (!this.validPaths.contains(model.validPaths.get(i)))
				this.validPaths.add(model.validPaths.get(i));
		}
		
		return true;
	}

	@Override
	public boolean train(CorpRelFeaturizedDataSet data, String outputPath) {
		for (Entry<CorpRelLabelPath, Model> entry : this.models.entrySet()) {
			List<CorpRelDatum> pathData = data.getDataUnderPath(entry.getKey(), this.getHyperParameter("allowSubpaths") > 0);
			CorpRelFeaturizedDataSet pathDataSet = new CorpRelFeaturizedDataSet(data.getMaxThreads(), this.output);
			pathDataSet.addData(pathData);
			for (int i = 0; i < data.getFeatureCount(); i++)
				pathDataSet.addFeature(data.getFeature(i));
			for (int i = 0; i < this.extraFeatures.get(entry.getKey()).size(); i++) {
				this.extraFeatures.get(entry.getKey()).get(i).init(pathData);
				pathDataSet.addFeature(this.extraFeatures.get(entry.getKey()).get(i));
			}
			
			if (!entry.getValue().train(pathDataSet, getOutputPathForModel(outputPath, entry.getKey())))
				return false;
		}
		
		this.modelPath = outputPath;
		if (!serializeParameters())
			return false;
		
		if (!serializeFeatures())
			return false;
		
		return true;
	}
	
	private boolean serializeFeatures() {
		if (this.modelPath == null)
			return false;
		
        try {
    		BufferedWriter w = new BufferedWriter(new FileWriter(this.modelPath + ".f"));  		
    		for (Entry<CorpRelLabelPath, List<CorpRelFeature>> entry : this.extraFeatures.entrySet()) {
    			for (CorpRelFeature feature : entry.getValue())
    				w.write(entry.getKey().toString() + "\t" + feature.toString(true) + "\n");
    		}
            w.close();
            return true;
        } catch (IOException e) { e.printStackTrace(); return false; }
	}
	
	
	@Override
	public List<Pair<CorpRelDatum, Map<CorpRelLabelPath, Double>>> posterior(CorpRelFeaturizedDataSet data) {
		List<Pair<CorpRelDatum, Map<CorpRelLabelPath, Double>>> posterior = new ArrayList<Pair<CorpRelDatum, Map<CorpRelLabelPath, Double>>>();
		if (this.models.size() == 0)
			return null;
		
		Map<CorpRelLabelPath, List<Pair<CorpRelDatum, Map<CorpRelLabelPath, Double>>>> modelPosteriors = new HashMap<CorpRelLabelPath, List<Pair<CorpRelDatum, Map<CorpRelLabelPath, Double>>>>();
		for (Entry<CorpRelLabelPath, Model> entry : this.models.entrySet()) {
			CorpRelFeaturizedDataSet dataExtraFeatures = new CorpRelFeaturizedDataSet(data.getMaxThreads(), this.output);
			dataExtraFeatures.addData(data.getData());
			for (int i = 0; i < data.getFeatureCount(); i++)
				dataExtraFeatures.addFeature(data.getFeature(i));
			for (int i = 0; i < this.extraFeatures.get(entry.getKey()).size(); i++)
				dataExtraFeatures.addFeature(this.extraFeatures.get(entry.getKey()).get(i));
			
			modelPosteriors.put(entry.getKey(), entry.getValue().posterior(dataExtraFeatures));
		}
		
		List<CorpRelDatum> datums = data.getData();
		for (int i = 0; i < datums.size(); i++) {
			Map<CorpRelLabelPath, Double> posteriorForDatum = posteriorForDatum(i, modelPosteriors);
			posterior.add(new Pair<CorpRelDatum, Map<CorpRelLabelPath, Double>>(datums.get(i), posteriorForDatum));
		}
		
		return posterior;
	}
	
	private Map<CorpRelLabelPath, Double> posteriorForDatum(int index, 
															Map<CorpRelLabelPath, List<Pair<CorpRelDatum, Map<CorpRelLabelPath, Double>>>> modelPosteriors) {
		Map<CorpRelLabelPath, Double> posterior = new HashMap<CorpRelLabelPath, Double>();
		Map<CorpRelLabelPath, Double> partialPosterior = new HashMap<CorpRelLabelPath, Double>();
		for (CorpRelLabelPath validPath : this.validPaths) {
			Double pValue = posteriorForDatumHelper(index, modelPosteriors, partialPosterior, validPath);
			if (this.getHyperParameter("allowSubpaths") > 0 && modelPosteriors.containsKey(validPath)) {
				CorpRelLabelPath prefixPath = validPath.getPrefix(validPath.size() - 1);
				pValue *= modelPosteriors.get(prefixPath).get(index).second().get(validPath);
			}
			posterior.put(validPath,  pValue);
		}
		return posterior;
	}
	
	private Double posteriorForDatumHelper(int index, 
										   Map<CorpRelLabelPath, List<Pair<CorpRelDatum, Map<CorpRelLabelPath, Double>>>> modelPosteriors,
										   Map<CorpRelLabelPath, Double> partialPosterior,
										   CorpRelLabelPath path) {
		if (path.size() == 0)
			return 1.0;
		if (partialPosterior.containsKey(path))
			return partialPosterior.get(path);
		
		CorpRelLabelPath prefixPath = path.getPrefix(path.size() - 1);
		if (!modelPosteriors.containsKey(prefixPath))
			this.output.debugWriteln("MISSING MODEL POSTERIOR " + prefixPath);
		else if (modelPosteriors.get(prefixPath) == null)
			this.output.debugWriteln("NULL AT MODEL POSTERIOR " + prefixPath);
		if (modelPosteriors.get(prefixPath).get(index) == null)
			this.output.debugWriteln("NULL AT " + prefixPath + " " + index);
		
		Map<CorpRelLabelPath, Double> modelP = modelPosteriors.get(prefixPath).get(index).second();
		if (!modelP.containsKey(path))
			return 0.0;
		
		Double pValue = modelP.get(path) * posteriorForDatumHelper(index, modelPosteriors, partialPosterior, prefixPath);
		partialPosterior.put(path, pValue);
		
		return pValue;
		
	}
	
	@Override
	public boolean deserialize(String modelPath, CorpDataTools dataTools) {
		File modelPathFile = new File(modelPath);
		File modelDir = modelPathFile.getParentFile();
		final String modelPathFileNamePrefix = modelPathFile.getName();
		
		File[] modelFiles = modelDir.listFiles(new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return name.startsWith(modelPathFileNamePrefix) && name.endsWith("Path");
		    }
		});
	
		this.models = new HashMap<CorpRelLabelPath, Model>();
		for (File modelFile : modelFiles) {
			int lastDotIndex = modelFile.getName().lastIndexOf(".");
			int pathIndex = modelFile.getName().lastIndexOf("Path");
			String modelLabelPathStr = modelFile.getName().substring(lastDotIndex + 1, pathIndex);
			CorpRelLabelPath modelLabelPath = CorpRelLabelPath.fromString(modelLabelPathStr);
			this.models.put(modelLabelPath, new ModelCReg(modelFile.getAbsolutePath(), this.output, dataTools)); // FIXME: Currently assumes all models are CReg... 
		}
		
		this.modelPath = modelPath;
		if (!deserializeParameters())
			return false;
		
		if (!deserializeFeatures(dataTools))
			return false;
		
		return true;
	}

	protected boolean deserializeFeatures(CorpDataTools dataTools) {
		if (this.modelPath == null)
			return false;
		
		this.extraFeatures = new HashMap<CorpRelLabelPath, List<CorpRelFeature>>();
        try {
    		BufferedReader r = new BufferedReader(new FileReader(this.modelPath + ".f"));  		
    		String line = null;
    		while ((line = r.readLine()) != null) {
    			int tabIndex = line.indexOf("\t");
    			if (tabIndex < 0) {
    				r.close();
    				return false;
    			}
    		
    			CorpRelLabelPath path = CorpRelLabelPath.fromString(line.substring(0, tabIndex));
    			if (!this.extraFeatures.containsKey(path))
    				this.extraFeatures.put(path, new ArrayList<CorpRelFeature>());
    			this.extraFeatures.get(path).add(CorpRelFeature.fromString(line.substring(tabIndex+1, line.length()), dataTools));
    		}
    		r.close();
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
	}
	
	@Override
	public Model clone() {
		ModelTree cloneModel = new ModelTree(this.getHyperParameter("allowSubpaths") > 0, this.output);
		for (Entry<CorpRelLabelPath, Model> entry : this.models.entrySet()) {
			cloneModel.models.put(entry.getKey(), entry.getValue().clone());
			cloneModel.extraFeatures.put(entry.getKey(), new ArrayList<CorpRelFeature>());
			List<CorpRelFeature> features = this.extraFeatures.get(entry.getKey());
			for (CorpRelFeature feature : features)
				cloneModel.extraFeatures.get(entry.getKey()).add(feature.clone());
		}
		
		cloneModel.validPaths = new ArrayList<CorpRelLabelPath>();
		for (CorpRelLabelPath validPath : this.validPaths) {
			cloneModel.validPaths.add(validPath);
		}
		
		for (Entry<String, Double> hyperParameter: this.hyperParameters.entrySet()) {
			cloneModel.setHyperParameter(hyperParameter.getKey(), hyperParameter.getValue());
		}
		
		return cloneModel;
	}
	
	@Override
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		
		for (Entry<CorpRelLabelPath, Model> entry : this.models.entrySet()) {
			retStr.append(entry.getKey().toString()).append("\n");
			retStr.append(entry.getValue().toString()).append("\n\n\n");
		}
		
		return retStr.toString();
	}
	
	@Override
	public void setHyperParameter(String parameter, double value) {
		if (parameter.equals("warmRestart")) {
			for (Entry<CorpRelLabelPath, Model> model : this.models.entrySet()) {
				model.getValue().setHyperParameter("warmRestart", value);
			}
		} else if (!parameter.contains("_"))
			this.hyperParameters.put(parameter, value);
		else {
			String[] parameterParts = parameter.split("_");
			CorpRelLabelPath modelPath = CorpRelLabelPath.fromString(parameterParts[0]);
			String modelParameter = parameterParts[1];
			this.models.get(modelPath).setHyperParameter(modelParameter, value);
		}
	}
	
	@Override
	public double getHyperParameter(String parameter) {
		if (!parameter.contains("_"))
			return this.hyperParameters.get(parameter);
		else {
			String[] parameterParts = parameter.split("_");
			CorpRelLabelPath modelPath = CorpRelLabelPath.fromString(parameterParts[0]);
			String modelParameter = parameterParts[1];
			return this.models.get(modelPath).getHyperParameter(modelParameter);
		}
	}
	
	private String getOutputPathForModel(String outputPathPrefix, CorpRelLabelPath modelPath) {
		return outputPathPrefix + "." + modelPath.toString() + "Path";
	}
}
