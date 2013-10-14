package corp.model.evaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import corp.util.OutputWriter;
import edu.stanford.nlp.util.Pair;

public class KFoldCrossValidation {
	private Model model;
	private CorpRelFeaturizedDataSet[] folds;
	private String outputPath;
	private List<CorpRelFeature> originalFeatures;
	private Random rand;
	private OutputWriter output;
	private Map<String, List<Double>> possibleParameterValues;
	
	public KFoldCrossValidation(Model model, 
			CorpRelFeaturizedDataSet data,
			int k,
			String outputPath,
			Random rand,
			OutputWriter output) {
		this(model, data, k, outputPath, rand, output, null);
	}
	
	public KFoldCrossValidation(Model model, 
								CorpRelFeaturizedDataSet data,
								int k,
								String outputPath,
								Random rand,
								OutputWriter output,
								Map<String, List<Double>> possibleParameterValues) {
		this.model = model;
		this.folds = new CorpRelFeaturizedDataSet[k];
		this.outputPath = outputPath;
		this.originalFeatures = data.getFeatures();
		this.rand = rand;
		this.output = output;
		this.possibleParameterValues = possibleParameterValues;
		
		List<CorpRelDatum> datums = randomPermutation(data.getData());
		for (int i = 0; i < k; i++) {
			folds[i] = new CorpRelFeaturizedDataSet(this.output);
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
		this.output.resultsWriteln("Average Accuracy: " + avgAccuracy);
		this.output.resultsWriteln("Average Confusion Matrix:\n " + aggregateConfusions.toString(1.0/this.folds.length));
		
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
			output.debugWriteln("Initializing CV data sets for fold " + this.foldIndex);
			CorpRelFeaturizedDataSet testData = new CorpRelFeaturizedDataSet(this.maxThreads, output);
			CorpRelFeaturizedDataSet trainData = new CorpRelFeaturizedDataSet(this.maxThreads, output);
			CorpRelFeaturizedDataSet devData = new CorpRelFeaturizedDataSet(this.maxThreads, output);
			for (int j = 0; j < folds.length; j++) {
				if (j == foldIndex) {
					testData.addData(folds[j].getData());
				} else if (possibleParameterValues != null && j == ((foldIndex + 1) % folds.length)) {
					devData.addData(folds[j].getData());
				} else {
					trainData.addData(folds[j].getData());	
				}
			}
			
			output.debugWriteln("Initializing features for CV fold " + foldIndex);
			
			/* Need cloned bunch of features for each fold so that they can be reinitialized for each training set */
			for (CorpRelFeature feature : originalFeatures) {
				CorpRelFeature foldFeature = feature.clone();
				foldFeature.init(trainData.getData());
				
				trainData.addFeature(foldFeature);
				testData.addFeature(foldFeature);
			}
			
			Model foldModel = model.clone();
			
			if (possibleParameterValues != null) {
				HyperParameterGridSearch gridSearch = new HyperParameterGridSearch(foldModel,
										 trainData, 
										 devData,
										 outputPath + "." + foldIndex,
										 possibleParameterValues,
										 output); 
				HyperParameterGridSearch.GridPosition bestParameters = gridSearch.getBestPosition();
				output.resultsWriteln("Grid search on fold " + foldIndex + ": ");
				output.resultsWriteln(gridSearch.toString());
				
				if (bestParameters != null)
					foldModel.setHyperParameters(bestParameters.getCoordinates());
			}
			
			output.debugWriteln("Training model for CV fold " + foldIndex);
			

			AccuracyValidation accuracy = new AccuracyValidation(foldModel, trainData, testData, outputPath + "." + foldIndex, output);
			double computedAccuracy = accuracy.run();
			if (computedAccuracy < 0) {
				output.debugWriteln("Error: Validation failed on fold " + foldIndex);
				return new Pair<Double, ConfusionMatrix>(-1.0, null);
			} else {
				ConfusionMatrix confusions = accuracy.getConfusionMatrix();
				output.resultsWriteln("Accuracy on fold " + foldIndex + ": " + computedAccuracy);
				output.dataWriteln("--------------- Fold: " + foldIndex + " ---------------");
				output.dataWriteln(confusions.getActualToPredictedDescription());
				output.modelWriteln("--------------- Fold: " + foldIndex + " ---------------");
				output.modelWriteln(foldModel.toString());
				
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
