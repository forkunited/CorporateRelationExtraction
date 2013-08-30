package corp.data.feature;

import java.util.HashSet;
import java.util.List;

import corp.data.annotation.CorpDocument;
import corp.data.annotation.CorpDocumentTokenSpan;
import corp.data.annotation.CorpRelDatum;
import corp.util.StanfordUtil;
import edu.stanford.nlp.ling.CoreLabel;

public class CorpRelFeatureNGramSentence extends CorpRelFeatureNGram {
	
	public CorpRelFeatureNGramSentence(
			List<CorpDocument> documents,
			List<CorpRelDatum> data, 
			int n, 
			int minFeatureOccurrence) {
		this.n = n;
		this.minFeatureOccurrence = minFeatureOccurrence;
		this.namePrefix = "Sentence";
		init(documents, data);
	}


	@Override
	protected HashSet<String> getNGramsForDatum(CorpRelDatum datum) {
		List<CorpDocumentTokenSpan> tokenSpans = datum.getOtherOrgTokenSpans();
		HashSet<String> ngrams = new HashSet<String>();
		CorpDocument document = datum.getDocument();
		for (CorpDocumentTokenSpan tokenSpan : tokenSpans) {
			List<CoreLabel> tokens = StanfordUtil.getSentenceTokens(document.getSentenceAnnotation(tokenSpan.getSentenceIndex()));
			for (int i = 0; i < tokens.size()-this.n+1; i++) {
				ngrams.add(getCleanNGram(tokens, i));
			}
		}
		return ngrams;
	}
}
