package corp.experiment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import corp.data.annotation.CorpRelLabelPath;
import corp.data.feature.CorpRelFeature;
import corp.model.Model;
import corp.model.ModelAdaGrad;
import corp.model.ModelCReg;
import corp.model.ModelTree;
import corp.model.ModelUniform;
import corp.model.cost.CorpRelCostFunction;
import ark.util.SerializationUtil;
import edu.stanford.nlp.util.Pair;

/**
 * 
 * ExperimentTree represents a training/evaluation experiment involving
 * a corp.model.ModelTree model.  The relationship type sub-taxonomy, 
 * features types, and hyper-parameters used by the ModelTree model
 * in the experiment are defined through the input configuration file.
 * See the "experiments" directory for examples. 
 * 
 * @author Bill McDowell
 *
 */
public abstract class ExperimentTree extends Experiment {
	protected boolean treeAllowSubpaths;
	protected ModelTree modelTree;
	protected Map<String, List<Double>> gridSearchParameters;

	@Override
	protected void parse(List<String> lines) {
		List<Pair<Integer, Integer>> modelStartEndLines = new ArrayList<Pair<Integer, Integer>>();
		int currentModelStartLine = -1;
		
		this.output.debugWriteln("Parsing tree models...");
		
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			Pair<String, String> assignment = SerializationUtil.deserializeAssignment(line);	
			if (assignment == null)
				continue;
			else if (assignment.first().equals("treeAllowSubpaths"))
				this.treeAllowSubpaths = Boolean.parseBoolean(assignment.second());
			else if (assignment.first().equals("treeModelPath")) {
				int modelPathLine = i;
				if (currentModelStartLine >= 0)
					modelStartEndLines.add(new Pair<Integer, Integer>(currentModelStartLine, modelPathLine));
				currentModelStartLine = modelPathLine;
			}
		}
		
		if (currentModelStartLine >= 0)
			modelStartEndLines.add(new Pair<Integer, Integer>(currentModelStartLine, lines.size()));
		
		this.modelTree = new ModelTree(this.treeAllowSubpaths, this.output);
		
		for (Pair<Integer, Integer> modelStartEndLine : modelStartEndLines)
			parseModel(lines, modelStartEndLine);
	}
	
	private void parseModel(List<String> lines, Pair<Integer, Integer> modelStartEndLine) {
		CorpRelLabelPath modelPath = null;
		List<CorpRelLabelPath> modelValidPaths = new ArrayList<CorpRelLabelPath>();
		List<CorpRelFeature> modelFeatures = new ArrayList<CorpRelFeature>();
		Model model = null;
		for (int i = modelStartEndLine.first(); i < modelStartEndLine.second(); i++) {
			Pair<String, String> assignment = SerializationUtil.deserializeAssignment(lines.get(i));
			if (assignment == null) {
				continue;
			} else if (assignment.first().equals("treeModelPath")) {
				modelPath = CorpRelLabelPath.fromString(assignment.second());
			} else if (assignment.first().equals("treeModel")) {
				if (assignment.second().startsWith("CReg"))
					model = new ModelCReg(this.properties.getCregCommandPath(), modelValidPaths, this.output);
				else if (assignment.second().startsWith("AdaGrad"))
					model = new ModelAdaGrad(modelValidPaths, this.output);
				else
					model = new ModelUniform(modelValidPaths, this.output);
				
				int parametersStart = assignment.second().indexOf("(");
				int parametersEnd = assignment.second().indexOf(")", parametersStart);
				if (parametersEnd > parametersStart) {
					String parametersStr = assignment.second().substring(parametersStart+1, parametersEnd);
					Map<String, String> parameters = SerializationUtil.deserializeArguments(parametersStr);
					for (Entry<String, String> entry : parameters.entrySet())
						model.setHyperParameter(entry.getKey(), Double.valueOf(entry.getValue()));
				}
			} else if (assignment.first().equals("treeModelValidPath")) {
				modelValidPaths.add(CorpRelLabelPath.fromString(assignment.second()));
			} else if (assignment.first().equals("treeModelFeature")) {
				modelFeatures.add(CorpRelFeature.fromString(assignment.second(), this.dataTools));
			} else if (assignment.first().equals("treeModelCostFunction")) {
				model.setCostFunction(CorpRelCostFunction.fromString(assignment.second(), modelValidPaths));
			} else if (assignment.first().equals("treeModelCostFunctionMode")) {
				model.setCostMode(Model.CostMode.valueOf(assignment.second()));
			} else if (assignment.first().equals("treeModelParameterSearch")) {
				if (this.gridSearchParameters == null)
					this.gridSearchParameters = new HashMap<String, List<Double>>();
				
				Pair<String, String> parameterValues = SerializationUtil.deserializeAssignment(assignment.second());
				String parameter = modelPath.toString() + "_" + parameterValues.first();
				List<String> values = SerializationUtil.deserializeList(parameterValues.second());
				if (!this.gridSearchParameters.containsKey(parameter))
					this.gridSearchParameters.put(parameter, new ArrayList<Double>());
				for (String value : values)
					this.gridSearchParameters.get(parameter).add(Double.valueOf(value));
			}
		}
		
		this.output.debugWriteln("Parsed tree model info for path " + ((modelPath.size() > 0) ? modelPath : "[Root]") + 
								 " with " + modelValidPaths.size() + " valid output paths and " + modelFeatures.size() +
								 " features.");
		
		this.modelTree.addModel(modelPath, model, modelFeatures);
	}
}