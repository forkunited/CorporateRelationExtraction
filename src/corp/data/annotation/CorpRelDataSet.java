package corp.data.annotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

public class CorpRelDataSet {
	private HashMap<CorpRelLabel, List<CorpRelLabel>> labeledDAG; // Label graph... should be a acyclic
	private HashMap<CorpRelLabel, List<CorpRelDatum>> labeledData;
	private List<CorpRelDatum> unlabeledData;
	private CorpDocumentSet sourceDocuments;
	
	public CorpRelDataSet(CorpDocumentSet sourceDocuments) {
		this.labeledDAG  = new HashMap<CorpRelLabel, List<CorpRelLabel>>();
		this.labeledData = new HashMap<CorpRelLabel, List<CorpRelDatum>>();
		this.unlabeledData = new ArrayList<CorpRelDatum>();
		this.sourceDocuments = sourceDocuments;
		
		List<CorpDocument> documents = this.sourceDocuments.getDocuments();
		for (CorpDocument document : documents) {
			addData(document.getCorpRelDatums());
		}
	}
	
	private boolean addData(List<CorpRelDatum> data) {
		for (CorpRelDatum datum : data)
			if (!addDatum(datum))
				return false;
		return true;
	}
	
	private boolean addDatum(CorpRelDatum datum) {
		CorpRelLabel[] labelPath = datum.getLabelPath();
		if (labelPath == null || labelPath.length == 0) {
			this.unlabeledData.add(datum);
			return true;
		}
		
		for (int i = 0; i < labelPath.length; i++) {
			if (!this.labeledDAG.containsKey(labelPath[i])) {
				this.labeledDAG.put(labelPath[i], new ArrayList<CorpRelLabel>());
				this.labeledData.put(labelPath[i], new ArrayList<CorpRelDatum>());
			}
			
			if (i + 1 < labelPath.length) {
				if (!this.labeledDAG.get(labelPath[i]).contains(labelPath[i+1]))
					this.labeledDAG.get(labelPath[i]).add(labelPath[i+1]);
			} else {
				this.labeledData.get(labelPath[i]).add(datum);
			}
		}
		
		return true;
	}
	
	public List<CorpRelDatum> getLabeledData() {
		List<CorpRelDatum> labeledData = new ArrayList<CorpRelDatum>();
		for (List<CorpRelDatum> data : this.labeledData.values())
			labeledData.addAll(data);
		return labeledData;
	}
	
	public List<CorpRelDatum> getUnlabeledData() {
		return this.unlabeledData;
	}
	
	public List<CorpRelDatum> getData() {
		List<CorpRelDatum> data = getLabeledData();
		data.addAll(getUnlabeledData());
		return data;
	}
	
	public List<CorpRelDatum> getDataUnderLabel(CorpRelLabel root, boolean includeRootData) {
		List<CorpRelDatum> data = new ArrayList<CorpRelDatum>();
		Stack<CorpRelLabel> labelsToVisit = new Stack<CorpRelLabel>();
		HashSet<CorpRelLabel> visited = new HashSet<CorpRelLabel>();
		
		if (includeRootData) {
			labelsToVisit.add(root);
		} else {
			if (!this.labeledDAG.containsKey(root))
				return data;
			
			List<CorpRelLabel> childLabels = this.labeledDAG.get(root);
			for (CorpRelLabel childLabel : childLabels)
				labelsToVisit.add(childLabel);
		}
		
		while (labelsToVisit.size() > 0) {
			CorpRelLabel currentLabel = labelsToVisit.pop();
			visited.add(currentLabel);
			
			if (this.labeledData.containsKey(currentLabel)) {
				data.addAll(this.labeledData.get(currentLabel));
			}
			
			if (!this.labeledDAG.containsKey(currentLabel))
				continue;
			
			List<CorpRelLabel> childLabels = this.labeledDAG.get(currentLabel);
			for (CorpRelLabel childLabel : childLabels) {
				if (!visited.contains(childLabel))
					labelsToVisit.push(childLabel);
			}
		}
		
		return data;
	}
	
	public List<CorpRelDatum> getDataInLabel(CorpRelLabel label) {
		if (!this.labeledData.containsKey(label))
			return new ArrayList<CorpRelDatum>();
		return this.labeledData.get(label);
	}
	
	public List<CorpRelLabel> getLabelChildren(CorpRelLabel label) {
		if (!this.labeledDAG.containsKey(label))
			return new ArrayList<CorpRelLabel>();
		return this.labeledDAG.get(label);
	}
	
	public CorpDocumentSet getSourceDocuments() {
		return this.sourceDocuments;
	}
}
