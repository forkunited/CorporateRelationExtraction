package corp.model.cost;

import java.util.List;

import corp.data.annotation.CorpRelLabelPath;
import corp.data.feature.CorpRelFeaturizedDatum;

public class CorpRelCostFunctionLabelPair extends CorpRelCostFunction {
	public CorpRelCostFunctionLabelPair(List<CorpRelLabelPath> validPaths) {
		this.validPaths = validPaths;
		this.name = "LabelPair";
	}
	
	@Override
	public List<String> getNames(List<String> existingNames) {
		for (CorpRelLabelPath actualPath : this.validPaths) {
			for (CorpRelLabelPath predictedPath : this.validPaths) {
				if (actualPath.equals(predictedPath))
					continue;
				existingNames.add("LabelPair_A_" + actualPath.toString() + "_P_" + predictedPath.toString());
			}
		}

		return existingNames;
	}

	@Override
	public List<Double> computeVector(CorpRelFeaturizedDatum datum,
			CorpRelLabelPath labelPath, List<Double> existingVector) {
		for (CorpRelLabelPath actualPath : this.validPaths) {
			for (CorpRelLabelPath predictedPath : this.validPaths) {
				if (actualPath.equals(predictedPath))
					continue;
				if (!datum.getLabelPath().equals(labelPath) && datum.getLabelPath().equals(actualPath) && labelPath.equals(predictedPath))
					existingVector.add(1.0);
				else
					existingVector.add(0.0);
			}
		}
		
		return existingVector;
	}
}
