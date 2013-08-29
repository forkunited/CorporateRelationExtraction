package corp.data.annotation;

import java.util.List;

public class CorpRelDatum {
	private String authorCorpName;
	private List<CorpDocumentTokenSpan> otherOrgTokenSpans;
	private CorpRelLabel[] labelPath;
	
	public CorpRelDatum(String authorCorpName, List<CorpDocumentTokenSpan> otherOrgTokenSpans) {
		if (otherOrgTokenSpans.size() == 0)
			throw new IllegalArgumentException();
		
		this.authorCorpName = authorCorpName;
		this.otherOrgTokenSpans = otherOrgTokenSpans;
	}
	
	public boolean setLabelPath(CorpRelLabel[] labelPath) {
		this.labelPath = labelPath;
		return true;
	}
	
	public String getAuthorCorpName() {
		return this.authorCorpName;
	}
	
	public List<CorpDocumentTokenSpan> getOtherOrgTokenSpans() {
		return this.otherOrgTokenSpans;
	}
	
	public CorpDocument getDocument() {
		return this.otherOrgTokenSpans.get(0).getDocument();
	}
	
	public CorpRelLabel[] getLabelPath() {
		return this.labelPath;
	}
	
	public CorpRelLabel getLastLabel() {
		return this.labelPath[this.labelPath.length];
	}
}
