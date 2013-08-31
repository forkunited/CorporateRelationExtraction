package corp.data.feature;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import corp.data.annotation.CorpDocument;
import corp.data.annotation.CorpDocumentTokenSpan;
import corp.data.annotation.CorpRelDatum;
import corp.util.StanfordUtil;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;

public class CorpRelFeatureNGramDep extends CorpRelFeatureNGram {
	public enum Mode {
		ParentsOnly,
		ChildrenOnly,
		ParentsAndChildren
	}
	
	private CorpRelFeatureNGramDep.Mode mode;
	private boolean useRelationTypes;
	
	public CorpRelFeatureNGramDep(
			List<CorpDocument> documents,
			List<CorpRelDatum> data, 
			int n, 
			int minFeatureOccurrence,
			CorpRelFeatureNGramDep.Mode mode,
			boolean useRelationTypes) {
		this.n = n;
		this.minFeatureOccurrence = minFeatureOccurrence;
		this.mode = mode;
		this.useRelationTypes = useRelationTypes;
		this.namePrefix = "Dep_" + this.mode + ((this.useRelationTypes) ? "_Rel" : "");
		init(documents, data);
	}

	@Override
	protected HashSet<String> getNGramsForDatum(CorpRelDatum datum) {
		List<CorpDocumentTokenSpan> tokenSpans = datum.getOtherOrgTokenSpans();
		HashSet<String> ngrams = new HashSet<String>();
		CorpDocument document = datum.getDocument();
		for (CorpDocumentTokenSpan tokenSpan : tokenSpans) {
			List<CoreLabel> tokens = StanfordUtil.getSentenceTokens(document.getSentenceAnnotation(tokenSpan.getSentenceIndex()));
			SemanticGraph deps = StanfordUtil.getSentenceDependencyGraph(document.getSentenceAnnotation(tokenSpan.getSentenceIndex()));
			int startIndex = tokenSpan.getTokenStartIndex();
			int endIndex = tokenSpan.getTokenEndIndex();
			for (int i = tokenSpan.getTokenStartIndex(); i < tokenSpan.getTokenEndIndex(); i++) {
				IndexedWord word = deps.getNodeByIndex(i+1);
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
						String ngram = getCleanNGram(tokens, depIndex);
						if (this.useRelationTypes)
							ngram += "_" + deps.reln(word, depWord).getShortName();
						ngrams.add(ngram);
					}
				}
			}
		}
		
		return ngrams;
	}
}