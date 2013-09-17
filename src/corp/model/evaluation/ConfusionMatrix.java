package corp.model.evaluation;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import corp.data.annotation.CorpRelDatum;
import corp.data.annotation.CorpRelLabelPath;
import corp.util.StanfordUtil;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;

public class ConfusionMatrix {
	private Map<CorpRelLabelPath, Map<CorpRelLabelPath, List<CorpRelDatum>>> actualToPredicted;
	private List<CorpRelLabelPath> labelPaths;
	
	public ConfusionMatrix(List<CorpRelLabelPath> labelPaths) {
		this.labelPaths = labelPaths;
		this.actualToPredicted = new HashMap<CorpRelLabelPath, Map<CorpRelLabelPath, List<CorpRelDatum>>>();
	}
	
	public boolean add(ConfusionMatrix otherMatrix) {
		for (Entry<CorpRelLabelPath, Map<CorpRelLabelPath, List<CorpRelDatum>>> otherMatrixEntryActual : otherMatrix.actualToPredicted.entrySet()) {
			CorpRelLabelPath actual = otherMatrixEntryActual.getKey();
			if (!this.actualToPredicted.containsKey(actual))
				this.actualToPredicted.put(actual, new HashMap<CorpRelLabelPath, List<CorpRelDatum>>());
			for (Entry<CorpRelLabelPath, List<CorpRelDatum>> otherMatrixEntryPredicted : otherMatrixEntryActual.getValue().entrySet()) {
				CorpRelLabelPath predicted = otherMatrixEntryPredicted.getKey();
				if (!this.actualToPredicted.get(actual).containsKey(predicted))
					this.actualToPredicted.get(actual).put(predicted, new ArrayList<CorpRelDatum>());
				this.actualToPredicted.get(actual).get(predicted).addAll(otherMatrixEntryPredicted.getValue());
			}
		}
		
		return true;
	}
	
	public boolean addData(List<Pair<CorpRelDatum, CorpRelLabelPath>> classifiedData) {
		for (CorpRelLabelPath actual : this.labelPaths) {
			this.actualToPredicted.put(actual, new HashMap<CorpRelLabelPath, List<CorpRelDatum>>());
			for (CorpRelLabelPath predicted : this.labelPaths) {
				this.actualToPredicted.get(actual).put(predicted, new ArrayList<CorpRelDatum>());
			}
		}
		
		for (Pair<CorpRelDatum, CorpRelLabelPath> classifiedDatum : classifiedData) {
			if (classifiedDatum.first().getLabelPath() == null)
				continue;
			CorpRelLabelPath actualLabelPath = classifiedDatum.first().getLabelPath().getLongestValidPrefix(this.labelPaths);
			CorpRelLabelPath predictedLabelPath = classifiedDatum.second().getLongestValidPrefix(this.labelPaths);
			
			if (actualLabelPath == null || predictedLabelPath == null)
				continue;
			
			this.actualToPredicted.get(actualLabelPath).get(predictedLabelPath).add(classifiedDatum.first());
		}
		
		return true;
	}
	
	public Map<CorpRelLabelPath, Map<CorpRelLabelPath, Double>> getConfusionMatrix(double scale) {
		if (this.actualToPredicted == null)
			return null;
		
		Map<CorpRelLabelPath, Map<CorpRelLabelPath, Double>> confusionMatrix = new HashMap<CorpRelLabelPath, Map<CorpRelLabelPath, Double>>();
		
		for (Entry<CorpRelLabelPath, Map<CorpRelLabelPath, List<CorpRelDatum>>> entryActual : this.actualToPredicted.entrySet()) {
			confusionMatrix.put(entryActual.getKey(), new HashMap<CorpRelLabelPath, Double>());
			for (Entry<CorpRelLabelPath, List<CorpRelDatum>> entryPredicted : entryActual.getValue().entrySet()) {
				confusionMatrix.get(entryActual.getKey()).put(entryPredicted.getKey(), entryPredicted.getValue().size()*scale);
			}
		}
		return confusionMatrix;
	}
	
	public Map<CorpRelLabelPath, Map<CorpRelLabelPath, Double>> getConfusionMatrix() {
		return getConfusionMatrix(1.0);
	}
	
	public String toString(double scale) {
		Map<CorpRelLabelPath, Map<CorpRelLabelPath, Double>> confusionMatrix = getConfusionMatrix(scale);
		StringBuilder confusionMatrixStr = new StringBuilder();
		
		confusionMatrixStr.append("\t");
		for (int i = 0; i < this.labelPaths.size(); i++) {
			confusionMatrixStr.append(this.labelPaths.get(i)).append(" (P)\t");
		}
		
		confusionMatrixStr.append("\n");
		DecimalFormat cleanDouble = new DecimalFormat("0.00");
		for (int i = 0; i < this.labelPaths.size(); i++) {
			confusionMatrixStr.append(this.labelPaths.get(i)).append(" (A)\t");
			for (int j = 0; j < this.labelPaths.size(); j++) {
				if (confusionMatrix.containsKey(this.labelPaths.get(i)) && confusionMatrix.get(this.labelPaths.get(i)).containsKey(this.labelPaths.get(j))) {
					String cleanDoubleStr = cleanDouble.format(confusionMatrix.get(this.labelPaths.get(i)).get(this.labelPaths.get(j)));
					confusionMatrixStr.append(cleanDoubleStr)
									  .append("\t");
				
				} else
					confusionMatrixStr.append("0.0\t");
			}
			confusionMatrixStr.append("\n");
		}
		
		return confusionMatrixStr.toString();
	}
	
	public String toString() {
		return toString(1.0);
	}
	
	public String getActualToPredictedDescription() {
		StringBuilder description = new StringBuilder();
		
		for (Entry<CorpRelLabelPath, Map<CorpRelLabelPath, List<CorpRelDatum>>> entryActual : this.actualToPredicted.entrySet()) {
			for (Entry<CorpRelLabelPath, List<CorpRelDatum>> entryPredicted : entryActual.getValue().entrySet()) {
				for (CorpRelDatum datum: entryPredicted.getValue()) {
					List<String> sentenceTokens = StanfordUtil.getSentenceTokenTexts(datum.getDocument().getSentenceAnnotation(datum.getOtherOrgTokenSpans().get(0).getSentenceIndex()));
					description.append("PREDICTED: ").append(entryPredicted.getKey()).append("\n");
					description.append("ACTUAL: ").append(entryPredicted.getKey()).append("\n");
					description.append("FIRST SENTENCE: ");
					for (String token : sentenceTokens)
						description.append(token).append(" ");
					description.append("\n");
					description.append(datum.toString()).append("\n\n");
					
				}
			}
		}
		
		return description.toString();
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
