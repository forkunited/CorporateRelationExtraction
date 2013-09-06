package corp.data.feature;

import java.util.List;

import corp.data.annotation.CorpDocumentTokenSpan;
import corp.data.annotation.CorpRelDatum;
import corp.util.StringUtil;

public abstract class CorpRelFeatureSelf extends CorpRelFeature {
	protected String namePrefix;
	
	@Override
	public void init(List<CorpRelDatum> data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<String> getNames(List<String> existingNames) {
		existingNames.add("Self_" + this.namePrefix);
		return existingNames;
	}

	@Override
	public List<Double> computeVector(CorpRelDatum datum,
			List<Double> existingVector) {
		List<CorpDocumentTokenSpan> tokenSpans = datum.getOtherOrgTokenSpans();
		double max = Double.NEGATIVE_INFINITY;
		for (CorpDocumentTokenSpan tokenSpan : tokenSpans) {
			max = Math.max(max, selfCompare(StringUtil.clean(datum.getAuthorCorpName()), StringUtil.clean(tokenSpan.toString())));
		}
		
		existingVector.add(max);
		return existingVector;
	}
	
	protected abstract double selfCompare(String mentioner, String mentioned);

}
