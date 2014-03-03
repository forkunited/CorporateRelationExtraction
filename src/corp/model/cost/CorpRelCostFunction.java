package corp.model.cost;

import java.util.ArrayList;
import java.util.List;

import corp.data.annotation.CorpRelLabelPath;
import corp.data.feature.CorpRelFeaturizedDatum;

public abstract class CorpRelCostFunction {
	protected List<CorpRelLabelPath> validPaths;
	protected String name;
	
	public abstract List<String> getNames(List<String> existingNames);
	public abstract List<Double> computeVector(CorpRelFeaturizedDatum datum, CorpRelLabelPath labelPath, List<Double> existingVector);

	public List<CorpRelLabelPath> getValidLabelPaths() {
		return this.validPaths;
	}
	
	public List<String> getNames() {
		return getNames(new ArrayList<String>());
	}
	
	public List<Double> computeVector(CorpRelFeaturizedDatum datum, CorpRelLabelPath labelPath) {
		return computeVector(datum, labelPath, new ArrayList<Double>());
	}
	
	public String toString() {
		return this.name;
	}
	
	public static CorpRelCostFunction fromString(String str, List<CorpRelLabelPath> validPaths) {
		if (str.equals("Constant"))
			return new CorpRelCostFunctionConstant();
		else if (str.equals("Label"))
			return new CorpRelCostFunctionLabel(validPaths);
		else
			return new CorpRelCostFunctionLabelPair(validPaths);
			
	}
}
