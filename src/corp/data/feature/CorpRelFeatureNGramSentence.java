package corp.data.feature;

import java.util.HashSet;
import java.util.List;

import corp.data.annotation.CorpDocument;
import corp.data.annotation.CorpDocumentTokenSpan;
import corp.data.annotation.CorpRelDatum;
import corp.util.BrownClusterer;
import corp.util.StanfordUtil;
import edu.stanford.nlp.ling.CoreLabel;

public class CorpRelFeatureNGramSentence extends CorpRelFeatureNGram {
	
	public CorpRelFeatureNGramSentence(
			int n, 
			int minFeatureOccurrence) {
		this(n, minFeatureOccurrence, null);
	}
	
	public CorpRelFeatureNGramSentence(
			int n, 
			int minFeatureOccurrence,
			BrownClusterer clusterer) {
		this.n = n;
		this.minFeatureOccurrence = minFeatureOccurrence;
		this.namePrefix = "Sentence";
		this.clusterer = clusterer;
	}

	@Override
	protected HashSet<String> getNGramsForDatum(CorpRelDatum datum) {
		List<CorpDocumentTokenSpan> tokenSpans = datum.getOtherOrgTokenSpans();
		HashSet<String> ngrams = new HashSet<String>();
		CorpDocument document = datum.getDocument();
		for (CorpDocumentTokenSpan tokenSpan : tokenSpans) {
			List<CoreLabel> tokens = StanfordUtil.getSentenceTokens(document.getSentenceAnnotation(tokenSpan.getSentenceIndex()));
			for (int i = 0; i < tokens.size()-this.n+1; i++) {
				String ngram = getCleanNGram(tokens, i);
				if (ngram != null)
					ngrams.add(ngram);
			}
		}
		return ngrams;
	}


	@Override
	public CorpRelFeature clone() {
		return new CorpRelFeatureNGramSentence(this.n, this.minFeatureOccurrence, this.clusterer);
	}
}
