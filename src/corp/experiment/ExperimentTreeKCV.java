package corp.experiment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import corp.data.annotation.CorpRelLabelPath;
import corp.data.feature.CorpRelFeature;
import corp.model.ModelCReg;
import corp.model.ModelTree;
import corp.model.evaluation.KFoldCrossValidation;
import edu.stanford.nlp.util.Pair;

public class ExperimentTreeKCV extends Experiment {
	private int crossValidationFolds;
	private boolean treeAllowSubpaths;
	private ModelTree modelTree;
	
	public ExperimentTreeKCV() {
		
	}

	@Override
	protected void parse(List<String> lines) {
		List<Pair<Integer, Integer>> modelStartEndLines = new ArrayList<Pair<Integer, Integer>>();
		int currentModelStartLine = -1;
		
		this.output.dataWriteln("Parsing tree models...");
		
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
		for (int i = modelStartEndLine.first(); i < modelStartEndLine.second(); i++) {
			Pair<String, String> assignment = parseAssignment(lines.get(i));
			if (assignment == null) {
				continue;
			} else if (assignment.first().equals("treeModelPath")) {
				modelPath = CorpRelLabelPath.fromString(assignment.second());
			} else if (assignment.first().equals("treeModel")) {
				// FIXME: Doing nothing for now... so only CReg supported
			} else if (assignment.first().equals("treeModelValidPath")) {
				modelValidPaths.add(CorpRelLabelPath.fromString(assignment.second()));
			} else if (assignment.first().equals("treeModelFeature")) {
				modelFeatures.add(parseFeature(assignment.second()));
			}
		}
		
		this.output.debugWriteln("Parsed tree model info for path " + ((modelPath.size() > 0) ? modelPath : "[Root]") + 
								 " with " + modelValidPaths.size() + " valid output paths and " + modelFeatures.size() +
								 " features.");
		
		ModelCReg model = new ModelCReg(this.properties.getCregCommandPath(), modelValidPaths, this.output);
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
				this.output
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
