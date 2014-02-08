package corp.data.feature;

import ark.data.Gazetteer;

/**
 * For organization mention m and gazetteer G, 
 * CorpRelFeatureGazetteerContains computes
 * 
 * max_{g\in G} 1(g=O(m))
 * 
 * Or 
 * 
 * max_{g\in G} 1(g=A(m))
 * 
 * Where O(m) is the mentioned organization and A(m) is the authoring 
 * corporation.  Whether A(m) or O(m) is used is determined by the
 * "input type".
 * 
 * @author Bill McDowell
 *
 */
public class CorpRelFeatureGazetteerContains extends CorpRelFeatureGazetteer {
	public CorpRelFeatureGazetteerContains(Gazetteer gazetteer, CorpRelFeatureGazetteer.InputType inputType) {
		this.extremumType = CorpRelFeatureGazetteer.ExtremumType.Maximum;
		this.inputType = inputType;
		this.namePrefix = "Contains";
		this.gazetteer = gazetteer;
	}

	@Override
	protected double extremum(String str) {
		if (this.gazetteer.contains(str))
			return 1.0;
		else 
			return 0.0;
	}

	@Override
	public CorpRelFeature clone() {
		return new CorpRelFeatureGazetteerContains(this.gazetteer, this.inputType);
	}
	
	@Override
	public String toString(boolean withInit) {
		return "GazetteerContains(gazetteer=" + this.gazetteer.getName() + "Gazetteer, inputType=" + this.inputType + ")";
	}
}
