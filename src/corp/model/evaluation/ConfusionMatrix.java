package corp.model.evaluation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import corp.data.annotation.CorpRelDatum;
import corp.data.annotation.CorpRelLabel;
import corp.data.feature.CorpRelFeaturizedDatum;
import edu.stanford.nlp.util.Pair;

public class ConfusionMatrix {
	private Map<CorpRelLabel, Map<CorpRelLabel, List<CorpRelDatum>>> actualToPredicted;
	private List<CorpRelLabel> labels;
	
	public ConfusionMatrix(List<CorpRelLabel> labels) {
		this.labels = labels;
	}
	
	public boolean addData(List<Pair<CorpRelFeaturizedDatum, CorpRelLabel>> classifiedData) {
		this.actualToPredicted = new HashMap<CorpRelLabel, Map<CorpRelLabel, List<CorpRelDatum>>>();
		
		for (CorpRelLabel actualLabel : this.labels) {
			this.actualToPredicted.put(actualLabel, new HashMap<CorpRelLabel, List<CorpRelDatum>>());
			for (CorpRelLabel predictedLabel : this.labels) {
				this.actualToPredicted.get(actualLabel).put(predictedLabel, new ArrayList<CorpRelDatum>());
			}
		}
		
		for (Pair<CorpRelFeaturizedDatum, CorpRelLabel> classifiedDatum : classifiedData) {
			CorpRelLabel actualLabel = classifiedDatum.first().getLabel(this.labels);
			CorpRelLabel predictedLabel = classifiedDatum.second();
			this.actualToPredicted.get(actualLabel).get(predictedLabel).add(classifiedDatum.first());
		}
		
		return true;
	}
	
	public Map<CorpRelLabel, Map<CorpRelLabel, Integer>> getConfusionMatrix() {
		if (this.actualToPredicted == null)
			return null;
		
		Map<CorpRelLabel, Map<CorpRelLabel, Integer>> confusionMatrix = new HashMap<CorpRelLabel, Map<CorpRelLabel, Integer>>();
		for (Entry<CorpRelLabel, Map<CorpRelLabel, List<CorpRelDatum>>> entryActual : this.actualToPredicted.entrySet()) {
			confusionMatrix.put(entryActual.getKey(), new HashMap<CorpRelLabel, Integer>());
			for (Entry<CorpRelLabel, List<CorpRelDatum>> entryPredicted : entryActual.getValue().entrySet()) {
				confusionMatrix.get(entryActual.getKey()).put(entryPredicted.getKey(), entryPredicted.getValue().size());
			}
		}
		
		return confusionMatrix;
	}
	
	public String toString() {
		Map<CorpRelLabel, Map<CorpRelLabel, Integer>> confusionMatrix = getConfusionMatrix();
		StringBuilder confusionMatrixStr = new StringBuilder();
		
		confusionMatrixStr.append("\t");
		for (int i = 0; i < this.labels.size(); i++) {
			confusionMatrixStr.append(this.labels.get(i)).append(" (P)\t");
		}
		
		confusionMatrixStr.append("\n");
		
		for (int i = 0; i < this.labels.size(); i++) {
			confusionMatrixStr.append(this.labels.get(i)).append(" (A)\t");
			for (int j = 0; j < this.labels.size(); j++) {
				confusionMatrixStr.append(confusionMatrix.get(this.labels.get(i)).get(this.labels.get(j)))
								  .append("\t");
			}
			confusionMatrixStr.append("\n");
		}
		
		return confusionMatrixStr.toString();
	}
	
	public Map<CorpRelLabel, List<CorpRelDatum>> getPredictedForActual(CorpRelLabel actual) {
		return this.actualToPredicted.get(actual);
	}
	
	public Map<CorpRelLabel, List<CorpRelDatum>> getActualForPredicted(CorpRelLabel predicted) {
		Map<CorpRelLabel, List<CorpRelDatum>> actual = new HashMap<CorpRelLabel, List<CorpRelDatum>>();
	
		for (Entry<CorpRelLabel, Map<CorpRelLabel, List<CorpRelDatum>>> entry : this.actualToPredicted.entrySet()) {
			if (!actual.containsKey(entry.getKey()))
				actual.put(entry.getKey(), new ArrayList<CorpRelDatum>());
			actual.get(entry.getKey()).addAll(entry.getValue().get(predicted));
		}
		
		return actual;
	}
	
	public List<CorpRelDatum> getActualPredicted(CorpRelLabel actual, CorpRelLabel predicted) {
		return this.actualToPredicted.get(actual).get(predicted);
	}
}
