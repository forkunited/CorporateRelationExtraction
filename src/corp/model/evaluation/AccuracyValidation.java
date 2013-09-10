package corp.model.evaluation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
	private Map<CorpRelLabel, Map<CorpRelLabel, List<CorpRelFeaturizedDatum>>> actualToPredicted;
	
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
		System.out.println("Training model and outputting to " + this.outputPath);
		if (!this.model.train(this.trainData, this.outputPath))
			return -1.0;
		
		System.out.println("Model classifying data (" + this.outputPath + ")");
		
		List<Pair<CorpRelFeaturizedDatum, CorpRelLabel>> classifiedData =  this.model.classify(this.testData);
		if (classifiedData == null)
			return -1.0;
		
		System.out.println("Computing model score (" + this.outputPath + ")");
		
		double correct = 0;
		for (Pair<CorpRelFeaturizedDatum, CorpRelLabel> classifiedDatum : classifiedData) {
			correct += classifiedDatum.first().getLabel(this.model.getValidLabels()) == classifiedDatum.second() ? 1.0 : 0;
		}
		
		computeActualToPredicted(classifiedData);
		
		return correct/classifiedData.size();
	}
	
	public Map<CorpRelLabel, Map<CorpRelLabel, Integer>> getConfusionMatrix() {
		if (this.actualToPredicted == null)
			return null;
		
		Map<CorpRelLabel, Map<CorpRelLabel, Integer>> confusionMatrix = new HashMap<CorpRelLabel, Map<CorpRelLabel, Integer>>();
		for (Entry<CorpRelLabel, Map<CorpRelLabel, List<CorpRelFeaturizedDatum>>> entryActual : this.actualToPredicted.entrySet()) {
			confusionMatrix.put(entryActual.getKey(), new HashMap<CorpRelLabel, Integer>());
			for (Entry<CorpRelLabel, List<CorpRelFeaturizedDatum>> entryPredicted : entryActual.getValue().entrySet()) {
				confusionMatrix.get(entryActual.getKey()).put(entryPredicted.getKey(), entryPredicted.getValue().size());
			}
		}
		
		return confusionMatrix;
	}
	
	public String getConfusionMatrixString() {
		Map<CorpRelLabel, Map<CorpRelLabel, Integer>> confusionMatrix = getConfusionMatrix();
		StringBuilder confusionMatrixStr = new StringBuilder();
		List<CorpRelLabel> validLabels = this.model.getValidLabels();
		
		confusionMatrixStr.append("\t");
		for (int i = 0; i < validLabels.size(); i++) {
			confusionMatrixStr.append(validLabels.get(i)).append("\t");
		}
		
		for (int i = 0; i < validLabels.size(); i++) {
			confusionMatrixStr.append(validLabels.get(i)).append("\t");
			for (int j = 0; j < validLabels.size(); j++) {
				confusionMatrixStr.append(confusionMatrix.get(validLabels.get(i)).get(validLabels.get(i)))
								  .append("\t");
			}
			confusionMatrixStr.append("\n");
		}
		
		return confusionMatrixStr.toString();
	}
	
	private void computeActualToPredicted(List<Pair<CorpRelFeaturizedDatum, CorpRelLabel>> classifiedData) {
		this.actualToPredicted = new HashMap<CorpRelLabel, Map<CorpRelLabel, List<CorpRelFeaturizedDatum>>>();
		List<CorpRelLabel> validLabels = this.model.getValidLabels();
		
		for (CorpRelLabel actualLabel : validLabels) {
			this.actualToPredicted.put(actualLabel, new HashMap<CorpRelLabel, List<CorpRelFeaturizedDatum>>());
			for (CorpRelLabel predictedLabel : validLabels) {
				this.actualToPredicted.get(actualLabel).put(predictedLabel, new ArrayList<CorpRelFeaturizedDatum>());
			}
		}
		
		for (Pair<CorpRelFeaturizedDatum, CorpRelLabel> classifiedDatum : classifiedData) {
			CorpRelLabel actualLabel = classifiedDatum.first().getLabel(validLabels);
			CorpRelLabel predictedLabel = classifiedDatum.second();
			this.actualToPredicted.get(actualLabel).get(predictedLabel).add(classifiedDatum.first());
		}
	}
}
