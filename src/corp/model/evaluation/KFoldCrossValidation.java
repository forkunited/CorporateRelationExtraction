package corp.model.evaluation;

import java.util.List;

import corp.data.annotation.CorpRelDatum;
import corp.data.feature.CorpRelFeaturizedDataSet;
import corp.model.Model;

public class KFoldCrossValidation {
	private Model model;
	private CorpRelFeaturizedDataSet[] folds;
	private String outputPath;
	
	public KFoldCrossValidation(Model model, 
								CorpRelFeaturizedDataSet data,
								int k,
								String outputPath) {
		this.model = model;
		this.folds = new CorpRelFeaturizedDataSet[k];
		this.outputPath = outputPath;
		
		List<CorpRelDatum> datums = data.getData();
		for (int i = 0; i < k; i++) {
			folds[i] = new CorpRelFeaturizedDataSet(data.getFeatures());
			for (int d = i*datums.size()/k; d < (i+1)*datums.size()/k; d++) {
				folds[i].addDatum(datums.get(d));
			}
		}
	}
	
	public double run() {
		double avgAccuracy = 0.0;
		
		for (int i = 0; i < this.folds.length; i++) {
			CorpRelFeaturizedDataSet testData = this.folds[i];
			CorpRelFeaturizedDataSet trainData = new CorpRelFeaturizedDataSet(testData.getFeatures());
			for (int j = 0; j < this.folds.length; j++) {
				if (i != j) {
					trainData.addData(this.folds[j].getData());
				}
			}
			
			AccuracyValidation accuracy = new AccuracyValidation(this.model, trainData, testData, this.outputPath);
			double computedAccuracy = accuracy.run();
			if (computedAccuracy < 0) {
				System.err.println("Error: Validation failed on fold " + i);
				return -1.0;
			} else {
				System.out.println("Accuracy on fold " + i + ": " + computedAccuracy);
				avgAccuracy += computedAccuracy;
			}
		}
		
		avgAccuracy /= this.folds.length;
		System.out.println("Average Accuracy: " + avgAccuracy);
		
		return avgAccuracy;
	}
}
