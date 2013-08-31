package corp.data.feature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import corp.data.annotation.CorpDocument;
import corp.data.annotation.CorpRelDatum;
import corp.util.CounterTable;
import corp.util.StanfordUtil;
import corp.util.Stemmer;
import edu.stanford.nlp.ling.CoreLabel;

public abstract class CorpRelFeatureNGram extends CorpRelFeature {
	protected int minFeatureOccurrence;
	protected int n;
	protected String namePrefix;
	private HashMap<String, Integer> vocabulary;

	@Override
	protected void init(List<CorpDocument> documents, List<CorpRelDatum> data) {
		CounterTable counter = new CounterTable();
		for (CorpRelDatum datum : data) {
			HashSet<String> ngramsForDatum = getNGramsForDatum(datum);
			for (String ngram : ngramsForDatum) {
				counter.incrementCount(ngram);
			}
		}
		
		counter.removeCountsLessThan(this.minFeatureOccurrence);
		this.vocabulary = counter.buildIndex();
	}

	@Override
	public List<Double> computeVector(CorpRelDatum datum, List<Double> existingVector) {
		HashSet<String> ngramsForDatum = getNGramsForDatum(datum);
		List<Double> featureValues = new ArrayList<Double>(Collections.nCopies(this.vocabulary.size(), 0.0));
		for (String ngram : ngramsForDatum) {
			if (this.vocabulary.containsKey(ngram))
				featureValues.set(this.vocabulary.get(ngram), 1.0);		
		}
		
		existingVector.addAll(featureValues);
		return existingVector;
	}

	@Override
	public List<String> getNames(List<String> existingNames) {
		for (String v : this.vocabulary.keySet())
			existingNames.add("NGram_" + this.namePrefix + "_N" + this.n + "_MinF" + this.minFeatureOccurrence + "_" + v);
		return existingNames;
	}

	protected String getCleanNGram(List<CoreLabel> tokens, int startIndex) {
		List<String> ngram = StanfordUtil.getTokensNGramTexts(tokens, startIndex, this.n);
		StringBuilder ngramGlue = new StringBuilder();
		for (String gram : ngram) {
			ngramGlue = ngramGlue.append(Stemmer.stem(gram.toLowerCase())).append("_");
		}
		ngramGlue = ngramGlue.delete(ngramGlue.length() - 1, ngramGlue.length());
		return ngramGlue.toString();
	}
	
	protected abstract HashSet<String> getNGramsForDatum(CorpRelDatum datum);
}
