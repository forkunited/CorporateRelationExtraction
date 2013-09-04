package corp.model.evaluation;

import java.util.List;

import corp.data.annotation.CorpRelLabel;
import corp.data.feature.CorpRelFeaturizedDataSet;
import corp.data.feature.CorpRelFeaturizedDatum;
import corp.model.Model;
import edu.stanford.nlp.util.Pair;

public class AccuracyValidation {
	private Model model;
	private CorpRelFeaturizedDataSet trainData;
	private CorpRelFeaturizedDataSet testData;
	private String outputPath;
	
	public AccuracyValidation(Model model, 
							  CorpRelFeaturizedDataSet trainData,
							  CorpRelFeaturizedDataSet testData,
							  String outputPath) {
		this.model = model;
		this.trainData = trainData;
		this.testData = testData;
		this.outputPath = outputPath;
	}
	
	public double run() {
		if (!this.model.train(this.trainData, this.outputPath))
			return -1.0;
		
		List<Pair<CorpRelFeaturizedDatum, CorpRelLabel>> classifiedData =  this.model.classify(this.testData);
		double correct = 0;
		for (Pair<CorpRelFeaturizedDatum, CorpRelLabel> classifiedDatum : classifiedData) {
			correct += classifiedDatum.first().getLabel(this.model.getValidLabels()) == classifiedDatum.second() ? 1.0 : 0;
		}
		return correct/classifiedData.size();
	}
	
}
