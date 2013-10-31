package corp.model.evaluation;

import java.util.List;

import corp.data.annotation.CorpRelDatum;
import corp.data.annotation.CorpRelLabelPath;
import corp.data.feature.CorpRelFeaturizedDataSet;
import corp.model.Model;
import ark.util.OutputWriter;
import edu.stanford.nlp.util.Pair;

public class AccuracyValidation {
	private Model model;
	private CorpRelFeaturizedDataSet trainData;
	private CorpRelFeaturizedDataSet testData;
	private String modelOutputPath;
	private ConfusionMatrix confusionMatrix;
	private OutputWriter output;
	
	public AccuracyValidation(Model model, 
							  CorpRelFeaturizedDataSet trainData,
							  CorpRelFeaturizedDataSet testData,
							  String modelOutputPath,
							  OutputWriter output) {
		this.model = model;
		this.trainData = trainData;
		this.testData = testData;
		this.modelOutputPath = modelOutputPath;
		this.output = output;
	}
	
	public double run() {
		this.output.debugWriteln("Training model and outputting to " + this.modelOutputPath);
		if (!this.model.train(this.trainData, this.modelOutputPath))
			return -1.0;
		
		this.output.debugWriteln("Model classifying data (" + this.modelOutputPath + ")");
		
		List<Pair<CorpRelDatum, CorpRelLabelPath>> classifiedData =  this.model.classify(this.testData);
		if (classifiedData == null)
			return -1.0;
		
		this.output.debugWriteln("Computing model score (" + this.modelOutputPath + ")");
		
		double correct = 0;
		double total = 0;
		for (Pair<CorpRelDatum, CorpRelLabelPath> classifiedDatum : classifiedData) {
			if (classifiedDatum.first().getLabelPath() == null)
				continue;
			CorpRelLabelPath actual = classifiedDatum.first().getLabelPath().getLongestValidPrefix(this.model.getValidLabelPaths());
			if (actual == null)
				continue;
			correct += actual.equals(classifiedDatum.second()) ? 1.0 : 0;
			total++;
		}
		
		this.confusionMatrix = new ConfusionMatrix(this.model.getValidLabelPaths());
		this.confusionMatrix.addData(classifiedData);
		
		return correct/total;
	}
	
	public ConfusionMatrix getConfusionMatrix() {
		return this.confusionMatrix;
	}
}
