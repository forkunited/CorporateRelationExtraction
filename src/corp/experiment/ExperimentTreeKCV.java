package corp.experiment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import corp.data.annotation.CorpRelLabelPath;
import corp.data.feature.CorpRelFeature;
import corp.model.Model;
import corp.model.ModelCReg;
import corp.model.ModelTree;
import corp.model.ModelUniform;
import corp.model.evaluation.KFoldCrossValidation;
import edu.stanford.nlp.util.Pair;

public class ExperimentTreeKCV extends Experiment {
	private int crossValidationFolds;
	private boolean treeAllowSubpaths;
	private ModelTree modelTree;
	private Map<String, List<Double>> gridSearchParameters;
	
	public ExperimentTreeKCV() {
		
	}

	@Override
	protected void parse(List<String> lines) {
		List<Pair<Integer, Integer>> modelStartEndLines = new ArrayList<Pair<Integer, Integer>>();
		int currentModelStartLine = -1;
		
		this.output.debugWriteln("Parsing tree models...");
		
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			Pair<String, String> assignment = parseAssignment(line);	
			if (assignment == null)
				continue;
			else if (assignment.first().equals("crossValidationFolds"))
				this.crossValidationFolds = Integer.parseInt(assignment.second());
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
			Pair<String, String> assignment = parseAssignment(lines.get(i));
			if (assignment == null) {
				continue;
			} else if (assignment.first().equals("treeModelPath")) {
				modelPath = CorpRelLabelPath.fromString(assignment.second());
			} else if (assignment.first().equals("treeModel")) {
				if (assignment.second().startsWith("CReg"))
					model = new ModelCReg(this.properties.getCregCommandPath(), modelValidPaths, this.output);
				else
					model = new ModelUniform(modelValidPaths, this.output);
				
				int parametersStart = assignment.second().indexOf("(");
				int parametersEnd = assignment.second().indexOf(")", parametersStart);
				if (parametersEnd > parametersStart) {
					String parametersStr = assignment.second().substring(parametersStart+1, parametersEnd);
					Map<String, String> parameters = parseArguments(parametersStr);
					for (Entry<String, String> entry : parameters.entrySet())
						model.setHyperParameter(entry.getKey(), Double.valueOf(entry.getValue()));
				}
			} else if (assignment.first().equals("treeModelValidPath")) {
				modelValidPaths.add(CorpRelLabelPath.fromString(assignment.second()));
			} else if (assignment.first().equals("treeModelFeature")) {
				modelFeatures.add(parseFeature(assignment.second()));
			} else if (assignment.first().equals("treeModelParameterSearch")) {
				if (this.gridSearchParameters == null)
					this.gridSearchParameters = new HashMap<String, List<Double>>();
				int parametersStart = assignment.second().indexOf("(");
				int parametersEnd = assignment.second().indexOf(")", parametersStart);
				String parametersStr = assignment.second().substring(parametersStart+1, parametersEnd);
				Map<String, String> parameters = parseArguments(parametersStr);
				for (Entry<String, String> entry : parameters.entrySet()) {
					String parameter = modelPath.toString() + "_" + entry.getKey();
					if (!this.gridSearchParameters.containsKey(parameter))
						this.gridSearchParameters.put(parameter, new ArrayList<Double>());
					this.gridSearchParameters.get(parameter).add(Double.valueOf(entry.getValue()));
				}
			}
		}
		
		this.output.debugWriteln("Parsed tree model info for path " + ((modelPath.size() > 0) ? modelPath : "[Root]") + 
								 " with " + modelValidPaths.size() + " valid output paths and " + modelFeatures.size() +
								 " features.");
		
		this.modelTree.addModel(modelPath, model, modelFeatures);
	}

	@Override
	protected void execute(String name) {
		KFoldCrossValidation validation = new KFoldCrossValidation(
				this.modelTree, 
				this.dataSet,
				this.crossValidationFolds,
				new File(this.properties.getCregDataDirPath(), name + "." + this.crossValidationFolds + "FoldCV").getAbsolutePath(),
				this.rand,
				this.output,
				this.gridSearchParameters
		);
		
		validation.run(this.maxThreads);
	}
	
	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("First argument should be the name of the experiment");
			return;
		}
		
		ExperimentTreeKCV experiment = new ExperimentTreeKCV();
		experiment.run(args[0]);
	}
}
