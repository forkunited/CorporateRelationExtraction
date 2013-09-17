package corp.model.evaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import corp.data.annotation.CorpRelDatum;
import corp.data.feature.CorpRelFeature;
import corp.data.feature.CorpRelFeaturizedDataSet;
import corp.model.Model;
import edu.stanford.nlp.util.Pair;

public class KFoldCrossValidation {
	private Model model;
	private CorpRelFeaturizedDataSet[] folds;
	private String outputPath;
	private List<CorpRelFeature> originalFeatures;
	private Random rand;
	
	public KFoldCrossValidation(Model model, 
								CorpRelFeaturizedDataSet data,
								int k,
								String outputPath,
								Random rand) {
		this.model = model;
		this.folds = new CorpRelFeaturizedDataSet[k];
		this.outputPath = outputPath;
		this.originalFeatures = data.getFeatures();
		this.rand = rand;
		
		List<CorpRelDatum> datums = randomPermutation(data.getData());
		for (int i = 0; i < k; i++) {
			folds[i] = new CorpRelFeaturizedDataSet();
			for (int d = i*datums.size()/k; d < (i+1)*datums.size()/k; d++) {
				folds[i].addDatum(datums.get(d));
			}
		}
	}
	
	public double run(int maxThreads) {
		double avgAccuracy = 0.0;
		
		ConfusionMatrix aggregateConfusions = new ConfusionMatrix(this.model.getValidLabelPaths());
		
		ExecutorService threadPool = Executors.newFixedThreadPool(2);
		List<ValidationThread> tasks = new ArrayList<ValidationThread>();
		for (int i = 0; i < this.folds.length; i++) {
			tasks.add(new ValidationThread(i, maxThreads/2));
		}
		
		try {
			List<Future<Pair<Double, ConfusionMatrix>>> results = threadPool.invokeAll(tasks);
			threadPool.shutdown();
			threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			for (Future<Pair<Double, ConfusionMatrix>> result : results) {
				Pair<Double, ConfusionMatrix> accuracyAndConfusion = result.get();
				avgAccuracy += accuracyAndConfusion.first();
				aggregateConfusions.add(accuracyAndConfusion.second());
			}
		} catch (Exception e) {
			e.printStackTrace();
			return -1.0;
		}
		
		avgAccuracy /= this.folds.length;
		System.out.println("Average Accuracy: " + avgAccuracy);
		System.out.println("Average Confusion Matrix:\n " + aggregateConfusions.toString(1.0/this.folds.length));
		
		return avgAccuracy;
	}
	
	private class ValidationThread implements Callable<Pair<Double, ConfusionMatrix>> {
		private int foldIndex;
		private int maxThreads;
		
		public ValidationThread(int foldIndex, int maxThreads) {
			this.foldIndex = foldIndex;
			this.maxThreads = maxThreads;
		}
		
		public Pair<Double, ConfusionMatrix> call() {
			System.out.println("Initializing CV data sets for fold " + this.foldIndex);
			CorpRelFeaturizedDataSet testData = new CorpRelFeaturizedDataSet(this.maxThreads);
			CorpRelFeaturizedDataSet trainData = new CorpRelFeaturizedDataSet(this.maxThreads);
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
				return new Pair<Double, ConfusionMatrix>(-1.0, null);
			} else {
				ConfusionMatrix confusions = accuracy.getConfusionMatrix();
				System.out.println("Accuracy on fold " + foldIndex + ": " + computedAccuracy);
				System.out.println(confusions.getActualToPredictedDescription());
				
				return new Pair<Double, ConfusionMatrix>(computedAccuracy, accuracy.getConfusionMatrix());
			}
		}
	}
	
	private List<CorpRelDatum> randomPermutation(List<CorpRelDatum> data) {
		for (int i = 0; i < data.size(); i++) {
			int j = this.rand.nextInt(i+1);
			CorpRelDatum temp = data.get(i);
			data.set(i, data.get(j));
			data.set(j, temp);
		}
		return data;
	}
}
