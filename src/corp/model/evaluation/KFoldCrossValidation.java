package corp.model.evaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import corp.data.annotation.CorpRelDatum;
import corp.data.feature.CorpRelFeature;
import corp.data.feature.CorpRelFeaturizedDataSet;
import corp.model.Model;

public class KFoldCrossValidation {
	private Model model;
	private CorpRelFeaturizedDataSet[] folds;
	private String outputPath;
	private List<CorpRelFeature> originalFeatures;
	
	public KFoldCrossValidation(Model model, 
								CorpRelFeaturizedDataSet data,
								int k,
								String outputPath) {
		this.model = model;
		this.folds = new CorpRelFeaturizedDataSet[k];
		this.outputPath = outputPath;
		this.originalFeatures = data.getFeatures();
		
		List<CorpRelDatum> datums = data.getData();
		for (int i = 0; i < k; i++) {
			folds[i] = new CorpRelFeaturizedDataSet();
			for (int d = i*datums.size()/k; d < (i+1)*datums.size()/k; d++) {
				folds[i].addDatum(datums.get(d));
			}
		}
	}
	
	public double run(int maxThreads) {
		double avgAccuracy = 0.0;
		ExecutorService threadPool = Executors.newFixedThreadPool(maxThreads);
		List<ValidationThread> tasks = new ArrayList<ValidationThread>();
		for (int i = 0; i < this.folds.length; i++) {
			tasks.add(new ValidationThread(i));
		}
		
		try {
			List<Future<Double>> results = threadPool.invokeAll(tasks);
			threadPool.shutdown();
			threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			for (Future<Double> result : results) {
				avgAccuracy += result.get();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return -1.0;
		}
		
		avgAccuracy /= this.folds.length;
		System.out.println("Average Accuracy: " + avgAccuracy);
		
		return avgAccuracy;
	}
	
	private class ValidationThread implements Callable<Double> {
		private int foldIndex;
		
		public ValidationThread(int foldIndex) {
			this.foldIndex = foldIndex;
		}
		
		public Double call() {
			System.out.println("Initializing CV data sets for fold " + this.foldIndex);
			CorpRelFeaturizedDataSet testData = new CorpRelFeaturizedDataSet();
			CorpRelFeaturizedDataSet trainData = new CorpRelFeaturizedDataSet();
			for (int j = 0; j < folds.length; j++) {
				if (foldIndex != j) {
					trainData.addData(folds[j].getData());
				} else {
					testData.addData(folds[j].getData());
				}
			}
			
			System.out.println("Initializing features for CV fold " + foldIndex);
			
			/* Need cloned bunch of features for each fold so that they can be reinitialized for each training set */
			for (CorpRelFeature feature : originalFeatures) {
				CorpRelFeature foldFeature = feature.clone();
				foldFeature.init(trainData.getData());
				
				trainData.addFeature(foldFeature);
				testData.addFeature(foldFeature);
			}
			
			System.out.println("Training model for CV fold " + foldIndex);
			
			AccuracyValidation accuracy = new AccuracyValidation(model.clone(), trainData, testData, outputPath + "." + foldIndex);
			double computedAccuracy = accuracy.run();
			if (computedAccuracy < 0) {
				System.err.println("Error: Validation failed on fold " + foldIndex);
				return -1.0;
			} else {
				System.out.println("Accuracy on fold " + foldIndex + ": " + computedAccuracy);
				return computedAccuracy;
			}
		}
	}
}
