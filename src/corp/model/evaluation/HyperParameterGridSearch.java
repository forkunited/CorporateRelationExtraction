package corp.model.evaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import corp.data.feature.CorpRelFeaturizedDataSet;
import corp.model.Model;
import corp.util.OutputWriter;
import edu.stanford.nlp.util.Pair;

public class HyperParameterGridSearch {
	public class GridPosition {
		private TreeMap<String, Double> coordinates;
		
		public GridPosition() {
			this.coordinates = new TreeMap<String, Double>();
		}
		
		public double getParameterValue(String parameter) {
			return this.coordinates.get(parameter);
		}
		
		public void setParameterValue(String parameter, double value) {
			this.coordinates.put(parameter, value);
		}
		
		public Map<String, Double> getCoordinates() {
			return this.coordinates;
		}
		
		public GridPosition clone() {
			GridPosition clonePosition = new GridPosition();
			for (Entry<String, Double> entry : this.coordinates.entrySet())
				clonePosition.setParameterValue(entry.getKey(), entry.getValue());
			return clonePosition;
		}
		
		public String toString() {
			StringBuilder str = new StringBuilder();
			str.append("(");
			for (Entry<String, Double> entry : this.coordinates.entrySet()) {
				str.append(entry.getKey()).append("=").append(entry.getValue()).append(",");
			}
			str.delete(str.length() - 1, str.length());
			str.append(")");
			return str.toString();
		}
		
		public String toValueString(String separator) {
			StringBuilder str = new StringBuilder();
			for (Entry<String, Double> entry : this.coordinates.entrySet()) {
				str.append(entry.getValue()).append(separator);
			}
			str.delete(str.length() - 1, str.length());
			return str.toString();
		}
		
		public String toKeyString(String separator) {
			StringBuilder str = new StringBuilder();
			for (Entry<String, Double> entry : this.coordinates.entrySet()) {
				str.append(entry.getKey()).append(separator);
			}
			str.delete(str.length() - 1, str.length());
			return str.toString();
		}
	}
	
	private Model model;
	private CorpRelFeaturizedDataSet trainData;
	private CorpRelFeaturizedDataSet testData;
	private String outputPath;
	private Map<String, List<Double>> possibleParameterValues;
	private List<Pair<GridPosition, Double>> gridEvaluation;
	private OutputWriter output;
	
	public HyperParameterGridSearch(Model model,
									CorpRelFeaturizedDataSet trainData, 
									CorpRelFeaturizedDataSet testData,
									String outputPath,
									Map<String, List<Double>> possibleParameterValues,
									OutputWriter output) {
		this.model = model;
		this.trainData = trainData;
		this.testData = testData;
		this.outputPath = outputPath;
		this.possibleParameterValues = possibleParameterValues;
		this.gridEvaluation = null;
		this.output = output;
	}
	
	public String toString() {
		List<Pair<GridPosition, Double>> gridEvaluation = getGridEvaluation();
		StringBuilder gridEvaluationStr = new StringBuilder();
		
		gridEvaluationStr = gridEvaluationStr.append(gridEvaluation.get(0).first().toKeyString("\t")).append("\t").append("Evaluation");
		for (Pair<GridPosition, Double> positionEvaluation : gridEvaluation) {
			gridEvaluationStr = gridEvaluationStr.append(positionEvaluation.first().toValueString("\t"))
							 					 .append("\t")
							 					 .append(positionEvaluation.second())
							 					 .append("\n");
		}
		
		return gridEvaluationStr.toString();
	}
	
	public List<Pair<GridPosition, Double>> getGridEvaluation() {
		if (this.gridEvaluation != null)
			return this.gridEvaluation;
		
		this.gridEvaluation = new ArrayList<Pair<GridPosition, Double>>();
		
		List<GridPosition> grid = constructGrid();
		for (GridPosition position : grid) {
			double positionValue = evaluateGridPosition(position);
			this.gridEvaluation.add(new Pair<GridPosition, Double>(position, positionValue));
		}
		
		return this.gridEvaluation;
	}
	
	public GridPosition getBestPosition() {
		List<Pair<GridPosition, Double>> gridEvaluation = getGridEvaluation();
		double maxValue = Double.NEGATIVE_INFINITY;
		GridPosition maxPosition = null;
		
		for (Pair<GridPosition, Double> positionValue : gridEvaluation) {
			if (positionValue.second() > maxValue) {
				maxValue = positionValue.second();
				maxPosition = positionValue.first();
			}
		}
		
		return maxPosition;
	}
	
	private List<GridPosition> constructGrid() {
		List<GridPosition> positions = new ArrayList<GridPosition>();
		positions.add(new GridPosition());
		for (Entry<String, List<Double>> possibleValuesEntry : this.possibleParameterValues.entrySet()) {
			List<GridPosition> newPositions = new ArrayList<GridPosition>();
			
			for (GridPosition position : positions) {
				for (Double parameterValue : possibleValuesEntry.getValue()) {
					GridPosition newPosition = position.clone();
					newPosition.setParameterValue(possibleValuesEntry.getKey(), parameterValue);
					newPositions.add(newPosition);
				}
			}
			
			positions = newPositions;
		}
		
		return positions;
	}
	
	private double evaluateGridPosition(GridPosition position) {
		this.output.debugWriteln("Grid search evaluating model with hyper parameters " + position.toString() + " (" + this.outputPath + ")");
		Model positionModel = this.model.clone();
		Map<String, Double> parameterValues = position.getCoordinates();
		for (Entry<String, Double> entry : parameterValues.entrySet()) {
			positionModel.setHyperParameter(entry.getKey(), entry.getValue());	
		}
		
		AccuracyValidation accuracy = new AccuracyValidation(positionModel, this.trainData, this.testData, this.outputPath + "." + position.toValueString("_"), this.output);
		double computedAccuracy = accuracy.run();
		if (computedAccuracy < 0) {
			this.output.debugWriteln("Error: Grid search evaluation failed at position " + position.toString());
			return -1.0;
		}

		this.output.debugWriteln("Finished grid search evaluating model with hyper parameters " + position.toString() + " (" + this.outputPath + ")");
		
		return computedAccuracy;
	}
}
