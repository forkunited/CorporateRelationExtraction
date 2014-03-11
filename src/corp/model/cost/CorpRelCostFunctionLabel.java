package corp.model.cost;

import java.util.List;

import corp.data.annotation.CorpRelLabelPath;
import corp.data.feature.CorpRelFeaturizedDatum;

public class CorpRelCostFunctionLabel extends CorpRelCostFunction {

	public CorpRelCostFunctionLabel(List<CorpRelLabelPath> validPaths) {
		this.validPaths = validPaths;
		this.name = "Label";
	}
	
	@Override
	public List<String> getNames(List<String> existingNames) {
		for (CorpRelLabelPath validPath : this.validPaths) {
			existingNames.add("Label_" + validPath.toString());
		}

		return existingNames;
	}

	@Override
	public List<Double> computeVector(CorpRelFeaturizedDatum datum,
			CorpRelLabelPath labelPath, List<Double> existingVector) {
		for (CorpRelLabelPath validPath : this.validPaths) {
			if (labelPath.equals(validPath) && !labelPath.equals(datum.getLabelPath()))
				existingVector.add(1.0);
			else
				existingVector.add(0.0);
		}	
		
		return existingVector;
	}
}
