package corp.data.feature;

import java.util.List;

import corp.data.Gazette;
import corp.data.annotation.CorpDocument;
import corp.data.annotation.CorpDocumentTokenSpan;
import corp.data.annotation.CorpRelDatum;

public abstract class CorpRelFeatureGazette extends CorpRelFeature {
	protected enum ExtremumType {
		Minimum,
		Maximum
	}
	
	protected Gazette gazette;
	protected CorpRelFeatureGazette.ExtremumType extremumType;
	protected CorpRelFeatureGazette.InputType inputType;
	protected String namePrefix;

	@Override
	protected void init(List<CorpDocument> documents, List<CorpRelDatum> data) {
		// TODO Auto-generated method stub
		
	}
	
	public enum InputType {
		Mentioner,
		Mentioned
	}

	@Override
	public List<String> getNames(List<String> existingNames) {
		existingNames.add("GazetteExtremum_" + this.namePrefix + "_" + this.gazette.getName());
		return existingNames;
	}

	@Override
	public List<Double> computeVector(CorpRelDatum datum, List<Double> existingVector) {
		if (this.inputType == CorpRelFeatureGazette.InputType.Mentioned) {
			existingVector.add(getExtremumForMentioned(datum));
		} else if (this.inputType == CorpRelFeatureGazette.InputType.Mentioner) {
			existingVector.add(getExtremumForMentioner(datum));
		}
		
		return existingVector;
	}
	
	protected double getExtremumForMentioned(CorpRelDatum datum) {
		List<CorpDocumentTokenSpan> tokenSpans = datum.getOtherOrgTokenSpans();
		double extremum = (this.extremumType == CorpRelFeatureGazette.ExtremumType.Maximum) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
		for (CorpDocumentTokenSpan tokenSpan : tokenSpans) {
			double curExtremum = extremum(tokenSpan.toString());
			if (this.extremumType == CorpRelFeatureGazette.ExtremumType.Maximum && curExtremum > extremum
					|| this.extremumType == CorpRelFeatureGazette.ExtremumType.Minimum && curExtremum < extremum)
				extremum = curExtremum;	
		}
		return extremum;
	}
	
	protected double getExtremumForMentioner(CorpRelDatum datum) {
		return extremum(datum.getAuthorCorpName());
	}
	
	protected abstract double extremum(String input);
}
