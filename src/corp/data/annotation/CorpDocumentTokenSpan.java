package corp.data.annotation;

/**
 * Represents a span of tokens in a CorpDocument.  
 * 
 * @author Bill
 */
public class CorpDocumentTokenSpan {
	private CorpDocument document;
	private int sentenceIndex; // 0-based sentence index
	private int tokenStartIndex; // 0-based starting token index (inclusive)
	private int tokenEndIndex; // 0-based ending token index (exclusive)
	
	/**
	 * @param document Source document
	 * @param sentenceIndex 0-based sentence index in document
	 * @param tokenStartIndex  0-based starting token index (inclusive)
	 * @param tokenEndIndex 0-based ending token index (exclusive)
	 */
	public CorpDocumentTokenSpan(CorpDocument document, int sentenceIndex, int tokenStartIndex, int tokenEndIndex) {
		this.document = document;
		this.sentenceIndex = sentenceIndex;
		this.tokenStartIndex = tokenStartIndex;
		this.tokenEndIndex = tokenEndIndex;
	}
	
	public CorpDocument getDocument() {
		return this.document;
	}
	
	public int getSentenceIndex() {
		return this.sentenceIndex;
	}
	
	public int getTokenStartIndex() {
		return this.tokenStartIndex;
	}
	
	public int getTokenEndIndex() {
		return this.tokenEndIndex;
	}
	
	public boolean equals(Object o) {
		CorpDocumentTokenSpan tokenSpan = (CorpDocumentTokenSpan)o;
		return this.sentenceIndex == tokenSpan.sentenceIndex
				&& this.tokenStartIndex == tokenSpan.tokenStartIndex
				&& this.tokenEndIndex == tokenSpan.tokenEndIndex;
		
	}
}
