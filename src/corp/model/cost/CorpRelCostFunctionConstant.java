package corp.model.cost;

import java.util.List;

import corp.data.annotation.CorpRelLabelPath;
import corp.data.feature.CorpRelFeaturizedDatum;

public class CorpRelCostFunctionConstant extends CorpRelCostFunction {
	public CorpRelCostFunctionConstant() {
		this.name = "Constant";
	}
	
	@Override
	public List<String> getNames(List<String> existingNames) {
		existingNames.add("Constant");
		return existingNames;
	}

	@Override
	public List<Double> computeVector(CorpRelFeaturizedDatum datum,
			CorpRelLabelPath labelPath, List<Double> existingVector) {
		existingVector.add(1.0);
		
		return existingVector;
	}
	
	public String toString() {
		return this.name;
	}
}
