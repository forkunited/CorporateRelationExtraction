package corp.model.evaluation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import corp.data.annotation.CorpRelDatum;
import corp.data.annotation.CorpRelLabelPath;
import corp.data.feature.CorpRelFeaturizedDatum;
import edu.stanford.nlp.util.Pair;

public class ConfusionMatrix {
	private Map<CorpRelLabelPath, Map<CorpRelLabelPath, List<CorpRelDatum>>> actualToPredicted;
	private List<CorpRelLabelPath> labelPaths;
	
	public ConfusionMatrix(List<CorpRelLabelPath> labelPaths) {
		this.labelPaths = labelPaths;
	}
	
	public boolean addData(List<Pair<CorpRelFeaturizedDatum, CorpRelLabelPath>> classifiedData) {
		this.actualToPredicted = new HashMap<CorpRelLabelPath, Map<CorpRelLabelPath, List<CorpRelDatum>>>();
		
		for (CorpRelLabelPath actual : this.labelPaths) {
			this.actualToPredicted.put(actual, new HashMap<CorpRelLabelPath, List<CorpRelDatum>>());
			for (CorpRelLabelPath predicted : this.labelPaths) {
				this.actualToPredicted.get(actual).put(predicted, new ArrayList<CorpRelDatum>());
			}
		}
		
		for (Pair<CorpRelFeaturizedDatum, CorpRelLabelPath> classifiedDatum : classifiedData) {
			if (classifiedDatum.first().getLabelPath() == null)
				continue;
			CorpRelLabelPath actualLabelPath = classifiedDatum.first().getLabelPath().getLongestValidPrefix(this.labelPaths);
			if (actualLabelPath == null)
				continue;
			
			CorpRelLabelPath predictedLabelPath = classifiedDatum.second().getLongestValidPrefix(this.labelPaths);
			this.actualToPredicted.get(actualLabelPath).get(predictedLabelPath).add(classifiedDatum.first());
		}
		
		return true;
	}
	
	public Map<CorpRelLabelPath, Map<CorpRelLabelPath, Integer>> getConfusionMatrix() {
		if (this.actualToPredicted == null)
			return null;
		
		Map<CorpRelLabelPath, Map<CorpRelLabelPath, Integer>> confusionMatrix = new HashMap<CorpRelLabelPath, Map<CorpRelLabelPath, Integer>>();
		for (Entry<CorpRelLabelPath, Map<CorpRelLabelPath, List<CorpRelDatum>>> entryActual : this.actualToPredicted.entrySet()) {
			confusionMatrix.put(entryActual.getKey(), new HashMap<CorpRelLabelPath, Integer>());
			for (Entry<CorpRelLabelPath, List<CorpRelDatum>> entryPredicted : entryActual.getValue().entrySet()) {
				confusionMatrix.get(entryActual.getKey()).put(entryPredicted.getKey(), entryPredicted.getValue().size());
			}
		}
		
		return confusionMatrix;
	}
	
	public String toString() {
		Map<CorpRelLabelPath, Map<CorpRelLabelPath, Integer>> confusionMatrix = getConfusionMatrix();
		StringBuilder confusionMatrixStr = new StringBuilder();
		
		confusionMatrixStr.append("\t");
		for (int i = 0; i < this.labelPaths.size(); i++) {
			confusionMatrixStr.append(this.labelPaths.get(i)).append(" (P)\t");
		}
		
		confusionMatrixStr.append("\n");
		
		for (int i = 0; i < this.labelPaths.size(); i++) {
			confusionMatrixStr.append(this.labelPaths.get(i)).append(" (A)\t");
			for (int j = 0; j < this.labelPaths.size(); j++) {
				confusionMatrixStr.append(confusionMatrix.get(this.labelPaths.get(i)).get(this.labelPaths.get(j)))
								  .append("\t");
			}
			confusionMatrixStr.append("\n");
		}
		
		return confusionMatrixStr.toString();
	}
	
	public Map<CorpRelLabelPath, List<CorpRelDatum>> getPredictedForActual(CorpRelLabelPath actual) {
		return this.actualToPredicted.get(actual);
	}
	
	public Map<CorpRelLabelPath, List<CorpRelDatum>> getActualForPredicted(CorpRelLabelPath predicted) {
		Map<CorpRelLabelPath, List<CorpRelDatum>> actual = new HashMap<CorpRelLabelPath, List<CorpRelDatum>>();
	
		for (Entry<CorpRelLabelPath, Map<CorpRelLabelPath, List<CorpRelDatum>>> entry : this.actualToPredicted.entrySet()) {
			if (!actual.containsKey(entry.getKey()))
				actual.put(entry.getKey(), new ArrayList<CorpRelDatum>());
			actual.get(entry.getKey()).addAll(entry.getValue().get(predicted));
		}
		
		return actual;
	}
	
	public List<CorpRelDatum> getActualPredicted(CorpRelLabelPath actual, CorpRelLabelPath predicted) {
		return this.actualToPredicted.get(actual).get(predicted);
	}
}
