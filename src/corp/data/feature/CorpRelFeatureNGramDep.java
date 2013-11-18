package corp.data.feature;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import corp.data.annotation.CorpDocument;
import corp.data.annotation.CorpDocumentTokenSpan;
import corp.data.annotation.CorpRelDatum;
import ark.wrapper.BrownClusterer;
import ark.util.SerializationUtil;
import ark.util.StanfordUtil;
import ark.util.StringUtil;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.GrammaticalRelation;

public class CorpRelFeatureNGramDep extends CorpRelFeatureNGram {
	public enum Mode {
		ParentsOnly,
		ChildrenOnly,
		ParentsAndChildren
	}
	
	private CorpRelFeatureNGramDep.Mode mode;
	private boolean useRelationTypes;
	
	public CorpRelFeatureNGramDep(
			int n, 
			int minFeatureOccurrence,
			CorpRelFeatureNGramDep.Mode mode,
			boolean useRelationTypes) {
		this(n, minFeatureOccurrence, mode, useRelationTypes, StringUtil.getDefaultCleanFn(), null);
	}
	
	public CorpRelFeatureNGramDep(
			int n, 
			int minFeatureOccurrence,
			CorpRelFeatureNGramDep.Mode mode,
			boolean useRelationTypes,
			StringUtil.StringTransform cleanFn,
			BrownClusterer clusterer) {
		this.n = n;
		this.minFeatureOccurrence = minFeatureOccurrence;
		this.mode = mode;
		this.useRelationTypes = useRelationTypes;
		this.namePrefix = "Dep_" + this.mode + ((this.useRelationTypes) ? "_Rel" : "");
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
			SemanticGraph deps = StanfordUtil.getSentenceDependencyGraph(document.getSentenceAnnotation(tokenSpan.getSentenceIndex()));
			int startIndex = tokenSpan.getTokenStartIndex();
			int endIndex = tokenSpan.getTokenEndIndex();

			for (int i = tokenSpan.getTokenStartIndex(); i < tokenSpan.getTokenEndIndex(); i++) {
				IndexedWord word = deps.getNodeByIndexSafe(i+1);
				if (word == null)
					continue;
				
				List<IndexedWord> depWords = new ArrayList<IndexedWord>();
				if (this.mode == CorpRelFeatureNGramDep.Mode.ChildrenOnly || this.mode == CorpRelFeatureNGramDep.Mode.ParentsAndChildren) {
					depWords.addAll(deps.getChildList(word));
				}
				
				if (this.mode == CorpRelFeatureNGramDep.Mode.ParentsOnly || this.mode == CorpRelFeatureNGramDep.Mode.ParentsAndChildren) {
					depWords.addAll(deps.getParentList(word));
				}
				
				for (IndexedWord depWord : depWords) {
					int depIndex = depWord.index() - 1; // Convert to 0-based
					if (depIndex < tokens.size() - this.n + 1 
							&& (depIndex < startIndex || depIndex >= endIndex)) {
						List<String> ngrams = getCleanNGrams(tokens, depIndex);
						if (ngrams == null)
							continue;
						for (String ngram : ngrams) {
							if (this.useRelationTypes) {
								GrammaticalRelation gr = deps.reln(word, depWord);
								if (gr == null)
									gr = deps.reln(depWord, word);
								ngram += "_" + ((gr == null) ? "" : gr.getShortName());
							}
							retNgrams.add(ngram);
						}
					}
				}
			}
		}
		
		return retNgrams;
	}

	@Override
	public CorpRelFeature clone() {
		return new CorpRelFeatureNGramDep(this.n, this.minFeatureOccurrence, this.mode, this.useRelationTypes, this.cleanFn, this.clusterer);
	}
	
	@Override
	public String toString(boolean withInit) {
		String str = "NGramDep(n=" + this.n + ", " +
						"minFeatureOccurrence=" + this.minFeatureOccurrence + ", " +
						"mode=" + this.mode + ", " +
						"useRelationTypes=" + this.useRelationTypes + ", " +
						"cleanFn=" + this.cleanFn.toString() + ", " +
						"clusterer=" + ((this.clusterer != null) ? this.clusterer.getName() : "None") + ")";
	
		if (withInit) {
			str += "\t" + SerializationUtil.serializeArguments(this.vocabulary);
		}
	
		return str;
	}
}