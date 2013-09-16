package corp.data.annotation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

public class CorpRelDataSet {
	// Label graph... should be a acyclic.  Kept as DAG instead of tree just in case want to build a DAG model later...
	private Map<CorpRelLabel, List<CorpRelLabel>> labeledDAG; 
	private Map<CorpRelLabel, List<CorpRelDatum>> labeledData;
	private List<CorpRelDatum> unlabeledData;
	private Comparator<CorpRelLabel> labelComparator = new Comparator<CorpRelLabel>() {
	      @Override
          public int compare(CorpRelLabel o1, CorpRelLabel o2) {
	    	  if (o1 == null && o2 == null)
	    		  return 0;
	    	  else if (o1 == null)
	    		  return -1;
	    	  else if (o2 == null)
	    		  return 1;
	    	  else 
	    		  return o1.toString().compareTo(o2.toString());
          }
	};
	
	public CorpRelDataSet() {
		// Used treemap to ensure same ordering when iterating over data across
		// multiple runs.  Possibly not same ordering because CorpRelLabel.hashCode()
		// is from Object (based on reference).  This is because it's an enum.
		this.labeledDAG  = new TreeMap<CorpRelLabel, List<CorpRelLabel>>(this.labelComparator);
		this.labeledData = new TreeMap<CorpRelLabel, List<CorpRelDatum>>(this.labelComparator);
		this.unlabeledData = new ArrayList<CorpRelDatum>();
	}
	
	public CorpRelDataSet(CorpDocumentSet sourceDocuments) {
		this.labeledDAG  = new TreeMap<CorpRelLabel, List<CorpRelLabel>>(this.labelComparator);
		this.labeledData = new TreeMap<CorpRelLabel, List<CorpRelDatum>>(this.labelComparator);
		this.unlabeledData = new ArrayList<CorpRelDatum>();
		
		List<CorpDocument> documents = sourceDocuments.getDocuments();
		for (CorpDocument document : documents) {
			addData(document.getCorpRelDatums());
		}
	}
	
	public boolean addData(List<CorpRelDatum> data) {
		for (CorpRelDatum datum : data)
			if (!addDatum(datum))
				return false;
		return true;
	}
	
	public boolean addDatum(CorpRelDatum datum) {
		CorpRelLabelPath labelPath = datum.getLabelPath();
		if (labelPath == null) {
			this.unlabeledData.add(datum);
			return true;
		}
		
		CorpRelLabel[] labelPathArray = labelPath.toArray();
		if (!this.labeledDAG.containsKey(null))
			this.labeledDAG.put(null, new ArrayList<CorpRelLabel>());
		if (!this.labeledDAG.get(null).contains(labelPathArray[0]))
			this.labeledDAG.get(null).add(labelPathArray[0]);
		
		for (int i = 0; i < labelPathArray.length; i++) {
			if (!this.labeledDAG.containsKey(labelPathArray[i])) {
				this.labeledDAG.put(labelPathArray[i], new ArrayList<CorpRelLabel>());
				this.labeledData.put(labelPathArray[i], new ArrayList<CorpRelDatum>());
			}
			
			if (i + 1 < labelPathArray.length) {
				if (!this.labeledDAG.get(labelPathArray[i]).contains(labelPathArray[i+1]))
					this.labeledDAG.get(labelPathArray[i]).add(labelPathArray[i+1]);
			} else {
				this.labeledData.get(labelPathArray[i]).add(datum);
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
	
	public List<CorpRelDatum> getDataUnderPath(CorpRelLabelPath path, boolean includePathRootData) {
		List<CorpRelDatum> data = new ArrayList<CorpRelDatum>();
		Stack<CorpRelLabel> labelsToVisit = new Stack<CorpRelLabel>();
		HashSet<CorpRelLabel> visited = new HashSet<CorpRelLabel>();
		
		if (path.size() == 0) {
			if (!this.labeledDAG.containsKey(null))
				return data;
			List<CorpRelLabel> childLabels = this.labeledDAG.get(null);
			for (CorpRelLabel childLabel : childLabels)
				labelsToVisit.add(childLabel);		
		} else if (includePathRootData) {
			labelsToVisit.add(path.getLastLabel());
		} else {
			CorpRelLabel pathRoot = path.getLastLabel();
			if (!this.labeledDAG.containsKey(pathRoot))
				return data;
			
			List<CorpRelLabel> childLabels = this.labeledDAG.get(pathRoot);
			for (CorpRelLabel childLabel : childLabels)
				labelsToVisit.add(childLabel);
		}
		
		while (labelsToVisit.size() > 0) {
			CorpRelLabel currentLabel = labelsToVisit.pop();
			visited.add(currentLabel);
			
			if (this.labeledData.containsKey(currentLabel)) {
				List<CorpRelDatum> currentLabelData = this.labeledData.get(currentLabel);
				for (CorpRelDatum datum : currentLabelData) {
					if (datum.getLabelPath().isPrefixedBy(path))
						data.add(datum);
				}
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
}
