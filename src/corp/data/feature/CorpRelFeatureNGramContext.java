package corp.data.feature;

import java.util.HashSet;
import java.util.List;

import corp.data.annotation.CorpDocument;
import corp.data.annotation.CorpDocumentTokenSpan;
import corp.data.annotation.CorpRelDatum;
import corp.util.BrownClusterer;
import corp.util.StanfordUtil;
import edu.stanford.nlp.ling.CoreLabel;

public class CorpRelFeatureNGramContext extends CorpRelFeatureNGram {
	private int contextWindowSize;
	
	public CorpRelFeatureNGramContext(
			int n, 
			int minFeatureOccurrence,
			int contextWindowSize) {
		this(n, minFeatureOccurrence, contextWindowSize, null);
	}
	
	public CorpRelFeatureNGramContext(
			int n, 
			int minFeatureOccurrence,
			int contextWindowSize,
			BrownClusterer clusterer) {
		this.n = n;
		this.minFeatureOccurrence = minFeatureOccurrence;
		this.contextWindowSize = contextWindowSize;
		this.namePrefix = "Context" + contextWindowSize;
		this.clusterer = clusterer;
	}

	@Override
	protected HashSet<String> getNGramsForDatum(CorpRelDatum datum) {
		List<CorpDocumentTokenSpan> tokenSpans = datum.getOtherOrgTokenSpans();
		HashSet<String> ngrams = new HashSet<String>();
		CorpDocument document = datum.getDocument();
		for (CorpDocumentTokenSpan tokenSpan : tokenSpans) {
			List<CoreLabel> tokens = StanfordUtil.getSentenceTokens(document.getSentenceAnnotation(tokenSpan.getSentenceIndex()));
			int startIndex = Math.max(0, tokenSpan.getTokenStartIndex() - this.contextWindowSize);
			int endIndex = Math.min(tokens.size(), tokenSpan.getTokenEndIndex() + this.contextWindowSize) - this.n + 1;
			for (int i = startIndex; i < endIndex; i++) {
				ngrams.add(getCleanNGram(tokens, i));
			}
		}
		return ngrams;
	}


	@Override
	public CorpRelFeature clone() {
		return new CorpRelFeatureNGramContext(this.n, this.minFeatureOccurrence, this.contextWindowSize, this.clusterer);
	}
}
