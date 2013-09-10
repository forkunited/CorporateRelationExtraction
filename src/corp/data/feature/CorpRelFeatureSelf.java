package corp.data.feature;

import java.util.List;

import corp.data.annotation.CorpDocumentTokenSpan;
import corp.data.annotation.CorpRelDatum;
import corp.util.StringUtil;

public abstract class CorpRelFeatureSelf extends CorpRelFeature {
	protected enum ExtremumType {
		Minimum,
		Maximum
	}
	
	protected StringUtil.StringTransform cleanFn;
	protected String namePrefix;
	protected ExtremumType extremumType;
	
	public CorpRelFeatureSelf() {
		this.cleanFn = StringUtil.getDefaultCleanFn();
	}
	
	@Override
	public void init(List<CorpRelDatum> data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<String> getNames(List<String> existingNames) {
		existingNames.add("Self_" + this.namePrefix + "_" + this.cleanFn.toString());
		return existingNames;
	}

	@Override
	public List<Double> computeVector(CorpRelDatum datum,
			List<Double> existingVector) {
		List<CorpDocumentTokenSpan> tokenSpans = datum.getOtherOrgTokenSpans();
		double extremum = (this.extremumType == ExtremumType.Maximum) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
		for (CorpDocumentTokenSpan tokenSpan : tokenSpans) {
			double compareValue = selfCompare(this.cleanFn.transform(datum.getAuthorCorpName()), this.cleanFn.transform(tokenSpan.toString()));
			if (this.extremumType == ExtremumType.Maximum)
				extremum = Math.max(extremum, compareValue);
			else 
				extremum = Math.min(extremum, compareValue);
		}
		
		existingVector.add(extremum);
		return existingVector;
	}
	
	protected abstract double selfCompare(String mentioner, String mentioned);

}
