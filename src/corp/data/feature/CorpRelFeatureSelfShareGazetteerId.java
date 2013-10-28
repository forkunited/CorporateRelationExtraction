package corp.data.feature;

import java.util.List;

import corp.data.Gazetteer;
import corp.util.StringUtil;

public class CorpRelFeatureSelfShareGazetteerId extends CorpRelFeatureSelf {
	private Gazetteer gazetteer;
	
	public CorpRelFeatureSelfShareGazetteerId(Gazetteer gazetteer) {
		super();
		this.namePrefix = "ShareGazetteerId_" + gazetteer.getName();
		this.extremumType = CorpRelFeatureSelf.ExtremumType.Maximum;
		this.gazetteer = gazetteer;
	}
	
	public CorpRelFeatureSelfShareGazetteerId(Gazetteer gazetteer, StringUtil.StringTransform cleanFn) {
		this(gazetteer);
		this.cleanFn = cleanFn;
	}
	
	protected double selfCompare(String mentioner, String mentioned) {
		List<String> mentionerIds = this.gazetteer.getIds(mentioner);
		List<String> mentionedIds = this.gazetteer.getIds(mentioned);
		if (mentionerIds == null || mentionedIds == null)
			return 0.0;
		for (String mentionedId : mentionedIds) {
			if (mentionerIds.contains(mentionedId))
				return 1.0;
		}
		
		return 0.0;
	}

	@Override
	public CorpRelFeature clone() {
		return new CorpRelFeatureSelfShareGazetteerId(this.gazetteer, this.cleanFn);
	}
	
	@Override
	public String toString(boolean withInit) {
		return "SelfShareGazetteerId(gazetteer=" + this.gazetteer.getName() + "Gazetteer, cleanFn=" + this.cleanFn.toString() + ")";
	}
}