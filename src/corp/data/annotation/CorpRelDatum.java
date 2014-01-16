package corp.data.annotation;

import java.util.List;

public class CorpRelDatum {
	private String authorCorpName;
	private List<CorpDocumentTokenSpan> otherOrgTokenSpans;
	private CorpRelLabelPath labelPath;
	private String annotator;
	private String annotationMentionKey;
	
	public CorpRelDatum(String authorCorpName, List<CorpDocumentTokenSpan> otherOrgTokenSpans) {
		if (otherOrgTokenSpans.size() == 0)
			throw new IllegalArgumentException();
		
		this.authorCorpName = authorCorpName;
		this.otherOrgTokenSpans = otherOrgTokenSpans;
	}
	
	public CorpRelDatum(CorpRelDatum copy) {
		this.authorCorpName = copy.authorCorpName;
		this.otherOrgTokenSpans = copy.otherOrgTokenSpans;
		this.labelPath = copy.labelPath;
	}
	
	public boolean setLabelPath(CorpRelLabelPath labelPath) {
		this.labelPath = labelPath;
		return true;
	}
	
	public boolean setAnnotator(String annotator) {
		this.annotator = annotator;
		return true;
	}
	
	public String getAnnotator() {
		return this.annotator;
	}
	
	public boolean setAnnotationMentionKey(String annotationMentionKey) {
		this.annotationMentionKey = annotationMentionKey;
		return true;
	}
	
	public String getAnnotationMentionKey() {
		return this.annotationMentionKey;
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
	
	public CorpRelLabelPath getLabelPath() {
		return this.labelPath;
	}
	
	@Override
	public String toString() {
		StringBuilder datumStr = new StringBuilder();
		
		datumStr.append("Label: ");
		if (this.labelPath != null) {
			datumStr.append(this.labelPath.toString()).append("\n");
		} else {
			datumStr.append("null\n");
		}
		
		datumStr.append("Author Corporation: " + this.authorCorpName).append("\n");
		datumStr.append("Mentioned Organizations: \n");
		for (CorpDocumentTokenSpan tokenSpan : this.otherOrgTokenSpans) {
			datumStr.append(tokenSpan.toString());
			datumStr.append("\n");
		}
		
		return datumStr.toString();
	}
	
	@Override
	public int hashCode() {
		// FIXME: Make better
		return this.toString().hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		CorpRelDatum datum = (CorpRelDatum)o;
		if (!this.authorCorpName.equals(datum.authorCorpName))
			return false;
		if ((this.labelPath != null && datum.labelPath != null && !this.labelPath.equals(datum.labelPath))
			|| (this.labelPath == null && datum.labelPath != null)
			|| (this.labelPath != null && datum.labelPath == null))
			return false;
		if (this.otherOrgTokenSpans.size() != datum.otherOrgTokenSpans.size())
			return false;
		for (int i = 0; i < this.otherOrgTokenSpans.size(); i++)
			if (!this.otherOrgTokenSpans.get(i).equals(datum.otherOrgTokenSpans.get(i)))
				return false;
		return true;
	}
}
