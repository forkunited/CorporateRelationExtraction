package corp.data.feature;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import corp.data.annotation.CorpDocumentTokenSpan;
import corp.data.annotation.CorpRelDatum;
import corp.util.StringUtil;

/**
 * 
 * CorpRelFeatureSelf represents a 'self' feature type.  For
 * each organization mention m, the feature computes some function
 * f(O(m), A(m)) where O(m) is the mentioned organization and A(m) is the 
 * authoring corporation. f is defined by the particular self-feature
 * being computed through one of the child classes of CorpRelFeatureSelf.
 * 
 * @author Bill McDowell
 *
 */
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
