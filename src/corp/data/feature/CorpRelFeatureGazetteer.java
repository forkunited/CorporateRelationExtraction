package corp.data.feature;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import corp.data.Gazetteer;
import corp.data.annotation.CorpDocumentTokenSpan;
import corp.data.annotation.CorpRelDatum;

public abstract class CorpRelFeatureGazetteer extends CorpRelFeature {
	protected enum ExtremumType {
		Minimum,
		Maximum
	}
	
	public enum InputType {
		Mentioner,
		Mentioned
	}
	
	protected Gazetteer gazetteer;
	protected CorpRelFeatureGazetteer.ExtremumType extremumType;
	protected CorpRelFeatureGazetteer.InputType inputType;
	protected String namePrefix;
	
	@Override
	public void init(List<CorpRelDatum> data) {
		// TODO Auto-generated method stub	
	}

	@Override
	public void init(String initStr) {
		
	}
	
	@Override
	public Map<String, Double> computeMapNoInit(CorpRelDatum datum) {
		List<String> names = getNames();
		List<Double> values = computeVector(datum);
		Map<String, Double> map = new HashMap<String, Double>();
		for (int i = 0; i < names.size(); i++) {
			map.put(names.get(i), values.get(i));
		}
		return map;
	}
	
	@Override
	public List<String> getNames(List<String> existingNames) {
		existingNames.add("GazetteExtremum_" + this.inputType + "_" + this.namePrefix + "_" + this.gazetteer.getName());
		return existingNames;
	}

	@Override
	public List<Double> computeVector(CorpRelDatum datum, List<Double> existingVector) {
		if (this.inputType == CorpRelFeatureGazetteer.InputType.Mentioned) {
			existingVector.add(getExtremumForMentioned(datum));
		} else if (this.inputType == CorpRelFeatureGazetteer.InputType.Mentioner) {
			existingVector.add(getExtremumForMentioner(datum));
		}
		
		return existingVector;
	}
	
	protected double getExtremumForMentioned(CorpRelDatum datum) {
		List<CorpDocumentTokenSpan> tokenSpans = datum.getOtherOrgTokenSpans();
		double extremum = (this.extremumType == CorpRelFeatureGazetteer.ExtremumType.Maximum) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
		for (CorpDocumentTokenSpan tokenSpan : tokenSpans) {
			double curExtremum = extremum(tokenSpan.toString());
			if ((this.extremumType == CorpRelFeatureGazetteer.ExtremumType.Maximum && curExtremum > extremum)
					|| (this.extremumType == CorpRelFeatureGazetteer.ExtremumType.Minimum && curExtremum < extremum))
				extremum = curExtremum;	
		}
		return extremum;
	}
	
	protected double getExtremumForMentioner(CorpRelDatum datum) {
		return extremum(datum.getAuthorCorpName());
	}
	
	protected abstract double extremum(String input);
}
