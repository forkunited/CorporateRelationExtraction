package corp.data.feature;

import java.util.HashSet;
import java.util.List;

import corp.data.annotation.CorpDocument;
import corp.data.annotation.CorpDocumentTokenSpan;
import corp.data.annotation.CorpRelDatum;
import ark.wrapper.BrownClusterer;
import corp.util.SerializationUtil;
import ark.util.StanfordUtil;
import corp.util.StringUtil;
import edu.stanford.nlp.ling.CoreLabel;

/**
 * 
 * For each organization mention m, CorpRelFeatureNGramSentence computes the 
 * vector:
 * 
 * <1(v_1\in S(m))), 1(v_2 \in S(m)), ... , 1(v_n \in S(m))>
 * 
 * Where S(m) is text of the sentence containing m, and v_i 
 * is an n-gram in vocabulary of possible n-grams from the full
 * data-set.  
 * 
 * @author Bill McDowell
 *
 */
public class CorpRelFeatureNGramSentence extends CorpRelFeatureNGram {
	
	public CorpRelFeatureNGramSentence(
			int n, 
			int minFeatureOccurrence) {
		this(n, minFeatureOccurrence, StringUtil.getDefaultCleanFn(), null);
	}
	
	public CorpRelFeatureNGramSentence(
			int n, 
			int minFeatureOccurrence,
			StringUtil.StringTransform cleanFn,
			BrownClusterer clusterer) {
		this.n = n;
		this.minFeatureOccurrence = minFeatureOccurrence;
		this.namePrefix = "Sentence";
		this.cleanFn = cleanFn;
		this.clusterer = clusterer;
	}

	@Override
	protected HashSet<String> getNGramsForDatum(CorpRelDatum datum) {
		List<CorpDocumentTokenSpan> tokenSpans = datum.getOtherOrgTokenSpans();
		HashSet<String> retNgrams = new HashSet<String>();
		CorpDocument document = datum.getDocument();
		for (CorpDocumentTokenSpan tokenSpan : tokenSpans) {
			List<CoreLabel> tokens = StanfordUtil.getSentenceTokens(document.getSentenceAnnotation(tokenSpan.getSentenceIndex()));
			for (int i = 0; i < tokens.size()-this.n+1; i++) {
				List<String> ngrams = getCleanNGrams(tokens, i);
				if (ngrams != null) {
					retNgrams.addAll(ngrams);
				}
			}
		}
		return retNgrams;
	}


	@Override
	public CorpRelFeature clone() {
		return new CorpRelFeatureNGramSentence(this.n, this.minFeatureOccurrence, this.cleanFn, this.clusterer);
	}
	
	@Override
	public String toString(boolean withInit) {
		String str = "NGramSentence(n=" + this.n + ", " +
									"minFeatureOccurrence=" + this.minFeatureOccurrence + ", " +
									"cleanFn=" + this.cleanFn.toString() + ", " +
									"clusterer=" + ((this.clusterer != null) ? this.clusterer.getName() : "None") + ")";
	
		if (withInit) {
			str += "\t" + SerializationUtil.serializeArguments(this.vocabulary);
		}
		
		return str;
	}
}
