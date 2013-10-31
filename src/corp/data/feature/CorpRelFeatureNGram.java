package corp.data.feature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import corp.data.annotation.CorpRelDatum;
import ark.wrapper.BrownClusterer;
import ark.data.CounterTable;
import ark.util.SerializationUtil;
import ark.util.StanfordUtil;
import ark.util.Stemmer;
import ark.util.StringUtil;
import edu.stanford.nlp.ling.CoreLabel;

public abstract class CorpRelFeatureNGram extends CorpRelFeature {
	protected int minFeatureOccurrence;
	protected int n;
	protected String namePrefix;
	protected StringUtil.StringTransform cleanFn;
	protected BrownClusterer clusterer;
	protected HashMap<String, Integer> vocabulary;

	private String getNamePrefix() {
		String clustererName = "";
		if (this.clusterer != null)
			clustererName = this.clusterer.getName() + "_";
		
		return "NGram_" + clustererName + this.namePrefix + "_N" + this.n + "_MinF" + this.minFeatureOccurrence + "_" + this.cleanFn.toString() + "_"; 
	}
	
	@Override
	public void init(List<CorpRelDatum> data) {
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
	public void init(String initStr) {
		this.vocabulary = new HashMap<String, Integer>();
		Map<String, String> strVocabulary = SerializationUtil.deserializeArguments(initStr);
		for (Entry<String, String> entry : strVocabulary.entrySet())
			this.vocabulary.put(entry.getKey(), Integer.parseInt(entry.getValue()));
	}
	
	@Override
	public Map<String, Double> computeMapNoInit(CorpRelDatum datum) {
		String namePrefix = getNamePrefix();
		HashSet<String> attValues = getNGramsForDatum(datum);
		Map<String, Double> map = new HashMap<String, Double>(attValues.size());
		for (String attValue : attValues) {
			map.put(namePrefix + attValue, 1.0);
		}
		return map;
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
		List<String> names = new ArrayList<String>(Collections.nCopies(this.vocabulary.size(), (String)null));
		
		String namePrefix = getNamePrefix();
		for (Entry<String, Integer> v : this.vocabulary.entrySet())
			names.set(v.getValue(), namePrefix + v.getKey());
		existingNames.addAll(names);
		
		return existingNames;
	}

	protected String getCleanNGram(List<CoreLabel> tokens, int startIndex) {
		List<String> ngram = StanfordUtil.getTokensNGramTexts(tokens, startIndex, this.n);
		StringBuilder ngramGlue = new StringBuilder();
		for (String gram : ngram) {
			String cleanGram = this.cleanFn.transform(gram);
			if (cleanGram.length() == 0)
				continue;
			if (this.clusterer != null) {
				String cluster = this.clusterer.getCluster(cleanGram);
				if (cluster != null) {
					ngramGlue = ngramGlue.append(cluster).append("_");
				} 
			} else { 
				ngramGlue = ngramGlue.append(Stemmer.stem(cleanGram)).append("_");
			}
		}
		
		if (ngramGlue.length() == 0)
			return null;
		
		ngramGlue = ngramGlue.delete(ngramGlue.length() - 1, ngramGlue.length());
		return ngramGlue.toString();
	}
	
	protected abstract HashSet<String> getNGramsForDatum(CorpRelDatum datum);
}
