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
		ValidationResult[] validationResults = new ValidationResult[this.folds.length];
 		for (int i = 0; i < this.folds.length; i++) {
			tasks.add(new ValidationThread(i, maxThreads/2));
		}
		
		try {
			List<Future<ValidationResult>> results = threadPool.invokeAll(tasks);
			threadPool.shutdown();
			threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			for (Future<ValidationResult> futureResult : results) {
				ValidationResult result = futureResult.get();
				validationResults[result.getFoldIndex()] = result;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return -1.0;
		}
		
		this.output.resultsWriteln("Fold\tAccuracy");
		for (int i = 0; i < validationResults.length; i++) {
			this.output.resultsWriteln(i + "\t" + validationResults[i].getAccuracy());
			avgAccuracy += validationResults[i].getAccuracy();
			aggregateConfusions.add(validationResults[i].getConfusionMatrix());
		}
		
		
		avgAccuracy /= this.folds.length;
		this.output.resultsWriteln("Average Accuracy:\t" + avgAccuracy);
		this.output.resultsWriteln("Average Confusion Matrix:\n " + aggregateConfusions.toString(1.0/this.folds.length));
		this.output.resultsWriteln("\nGrid search results:");
		
		this.output.resultsWrite(validationResults[0].getGridEvaluation().get(0).first().toKeyString("\t") + "\t");
		for (int i = 0; i < this.folds.length; i++)
			this.output.resultsWrite("Fold " + i + "\t");
		this.output.resultsWrite("\n");
		
		List<Pair<HyperParameterGridSearch.GridPosition, List<Double>>> gridFoldResults = new ArrayList<Pair<HyperParameterGridSearch.GridPosition, List<Double>>>();
		for (int i = 0; i < validationResults.length; i++) {
			List<Pair<HyperParameterGridSearch.GridPosition, Double>> gridEvaluation = validationResults[i].getGridEvaluation();
			for (int j = 0; j < gridEvaluation.size(); j++) {
				if (gridFoldResults.size() <= j)
					gridFoldResults.add(new Pair<HyperParameterGridSearch.GridPosition, List<Double>>(gridEvaluation.get(j).first(), new ArrayList<Double>()));
				gridFoldResults.get(j).second().add(gridEvaluation.get(j).second());
			}
		}
		
		for (Pair<HyperParameterGridSearch.GridPosition, List<Double>> gridFoldResult : gridFoldResults) {
			this.output.resultsWrite(gridFoldResult.first().toValueString("\t") + "\t");
			for (int i = 0; i < gridFoldResult.second().size(); i++) {
				this.output.resultsWrite(gridFoldResult.second().get(i) + "\t");
			}
			this.output.resultsWrite("\n");
		}
		
		return avgAccuracy;
	}
	
	private class ValidationResult {
		private int foldIndex;
		private double accuracy;
		private ConfusionMatrix confusionMatrix;
		private List<Pair<HyperParameterGridSearch.GridPosition, Double>> gridEvaluation;
		
		public ValidationResult(int foldIndex, double accuracy, ConfusionMatrix confusionMatrix, List<Pair<HyperParameterGridSearch.GridPosition, Double>> gridEvaluation) {
			this.foldIndex = foldIndex;
			this.accuracy = accuracy;
			this.confusionMatrix = confusionMatrix;
			this.gridEvaluation = gridEvaluation;
		}
		
		public int getFoldIndex() {
			return this.foldIndex;
		}
		
		public double getAccuracy() {
			return this.accuracy;
		}
		
		public ConfusionMatrix getConfusionMatrix() {
			return this.confusionMatrix;
		}
		
		public List<Pair<HyperParameterGridSearch.GridPosition, Double>> getGridEvaluation() {
			return this.gridEvaluation;
		}
	}
	
	private class ValidationThread implements Callable<ValidationResult> {
		private int foldIndex;
		private int maxThreads;
		
		public ValidationThread(int foldIndex, int maxThreads) {
			this.foldIndex = foldIndex;
			this.maxThreads = maxThreads;
		}
		
		public ValidationResult call() {
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
			List<Pair<HyperParameterGridSearch.GridPosition, Double>> gridEvaluation = null;
			if (possibleParameterValues != null) {
				HyperParameterGridSearch gridSearch = new HyperParameterGridSearch(foldModel,
										 trainData, 
										 devData,
										 outputPath + "." + foldIndex,
										 possibleParameterValues,
										 output); 
				HyperParameterGridSearch.GridPosition bestParameters = gridSearch.getBestPosition();
				gridEvaluation = gridSearch.getGridEvaluation();
				
				output.debugWriteln("Grid search on fold " + foldIndex + ": \n" + gridSearch.toString());
				
				if (bestParameters != null)
					foldModel.setHyperParameters(bestParameters.getCoordinates());
			}
			
			output.debugWriteln("Training model for CV fold " + foldIndex);
			

			AccuracyValidation accuracy = new AccuracyValidation(foldModel, trainData, testData, outputPath + "." + foldIndex, output);
			double computedAccuracy = accuracy.run();
			if (computedAccuracy < 0) {
				output.debugWriteln("Error: Validation failed on fold " + foldIndex);
				return new ValidationResult(foldIndex, -1, null, null);
			} else {
				ConfusionMatrix confusions = accuracy.getConfusionMatrix();
				output.debugWriteln("Accuracy on fold " + foldIndex + ": " + computedAccuracy);
				output.dataWriteln("--------------- Fold: " + foldIndex + " ---------------");
				output.dataWriteln(confusions.getActualToPredictedDescription());
				output.modelWriteln("--------------- Fold: " + foldIndex + " ---------------");
				output.modelWriteln(foldModel.toString());
				
				return new ValidationResult(foldIndex, computedAccuracy, accuracy.getConfusionMatrix(), gridEvaluation);
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
