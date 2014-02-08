package corp.data.annotation;

import java.util.List;

import ark.util.StanfordUtil;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.CoreMap;

/**
 * CorpDocumentTokenSpan represents a span of tokens in a press release 
 * document.  This is mainly useful in representing the locations of the
 * organization mentions within the document.  
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
	
	@Override
	public int hashCode() {
		// TODO Make this better
		return this.document.hashCode()^(this.sentenceIndex*3)^(this.tokenEndIndex*31)^(this.tokenStartIndex*23);
	}
	
	@Override
	public boolean equals(Object o) {
		CorpDocumentTokenSpan tokenSpan = (CorpDocumentTokenSpan)o;
		return this.sentenceIndex == tokenSpan.sentenceIndex
				&& this.tokenStartIndex == tokenSpan.tokenStartIndex
				&& this.tokenEndIndex == tokenSpan.tokenEndIndex
				&& this.document.equals(tokenSpan.document);
		
	}
	
	@Override
	public String toString() {
		return toString(true);
	}
	
	public String toString(boolean useSentenceAnnotation) {
		CoreMap sentenceAnnotation = null;
		if (useSentenceAnnotation)
			sentenceAnnotation = this.document.getSentenceAnnotation(this.sentenceIndex);
		else
			sentenceAnnotation = StanfordUtil.getDocumentSentence(this.document.getAnnotation(), this.sentenceIndex);
		
		List<CoreLabel> tokens = StanfordUtil.getSentenceTokens(sentenceAnnotation);
		List<String> tokenSpanTokens = StanfordUtil.getTokensNGramTexts(tokens, this.tokenStartIndex, this.tokenEndIndex-this.tokenStartIndex);
		StringBuilder str = new StringBuilder();
		
		for (String token : tokenSpanTokens) {
			str = str.append(token).append(" ");
		}
		
		return str.toString();
	}
}
