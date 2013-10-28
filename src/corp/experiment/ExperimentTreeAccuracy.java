package corp.experiment;

import java.io.File;
import java.util.List;

import corp.data.annotation.CorpDocumentSet;
import corp.data.feature.CorpRelFeaturizedDataSet;
import corp.model.evaluation.AccuracyValidation;
import corp.model.evaluation.ConfusionMatrix;
import corp.util.SerializationUtil;
import edu.stanford.nlp.util.Pair;

public class ExperimentTreeAccuracy extends ExperimentTree {
	public ExperimentTreeAccuracy() {
		
	}

	@Override
	protected void parse(List<String> lines) {
		super.parse(lines);
		
		this.output.debugWriteln("Parsing accuracy parameters...");
		
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			Pair<String, String> assignment = SerializationUtil.deserializeAssignment(line);	
			if (assignment == null)
				continue;
			// Add more parameters to parse here
		}
	}

	@Override
	protected void execute(String name) {		
		CorpDocumentSet testDocumentSet = new CorpDocumentSet(
				this.properties.getCorpRelTestDirPath(), 
				this.annotationCache,
				this.maxThreads,
				-1,
				0,
				this.output,
				dataTools.getMetaData("CorpMetaData")
		);
		
		this.output.debugWriteln("Loaded " + testDocumentSet.getDocuments().size() + " test documents.");
		this.output.debugWriteln("Constructing test data set...");
		
		CorpRelFeaturizedDataSet testData = new CorpRelFeaturizedDataSet(testDocumentSet, this.output);
		
		AccuracyValidation validation = new AccuracyValidation(
					this.modelTree,
					this.dataSet,
					testData,
				  	new File(this.properties.getCregDataDirPath(), name + ".Accuracy").getAbsolutePath(),
				  	this.output);
		
		double accuracy = validation.run();
		ConfusionMatrix confusions = validation.getConfusionMatrix();
		
		this.output.resultsWriteln("Test Accuracy:\t" + accuracy);
		this.output.resultsWriteln("\nTest Confusion Matrix:\n " + confusions);
		this.output.dataWriteln(confusions.getActualToPredictedDescription());
		this.output.modelWriteln(this.modelTree.toString());
		
	}
	
	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("First argument should be the name of the experiment");
			return;
		}
		
		ExperimentTreeAccuracy experiment = new ExperimentTreeAccuracy();
		experiment.run(args[0]);
	}
}