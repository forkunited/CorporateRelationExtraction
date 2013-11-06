package corp.data.feature;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ark.util.StanfordUtil;
import ark.util.StringUtil;
import corp.data.annotation.CorpDocument;
import corp.data.annotation.CorpDocumentTokenSpan;
import corp.data.annotation.CorpRelDatum;
import corp.util.LDA;

public class CorpRelFeatureLDA extends CorpRelFeature {
	private LDA lda;
	private LDA.DatumDocumentTransform documentFn;
	private final StringUtil.StringTransform corpKeyFn;
	private final StringUtil.StringTransform cleanFn;
	
	public CorpRelFeatureLDA(LDA lda, final StringUtil.StringTransform corpKeyFn, final StringUtil.StringTransform cleanFn) {
		this.lda = lda;
		this.documentFn = new LDA.DatumDocumentTransform() {
			public String transform(CorpRelDatum datum) {
				CorpDocument datumDoc = datum.getDocument();
				List<CorpDocumentTokenSpan> spans = datum.getOtherOrgTokenSpans();
				Set<Integer> sentences = new HashSet<Integer>();
				Set<String> mentions = new HashSet<String>();
				String author = corpKeyFn.transform(datum.getAuthorCorpName());
				for (CorpDocumentTokenSpan span : spans) {
					mentions.add(corpKeyFn.transform(span.toString()));
					sentences.add(span.getSentenceIndex());
				
				}
				
				StringBuilder retStr = new StringBuilder();
				retStr.append(author).append(" ");
				for (String mention : mentions)
					retStr.append(mention).append(" ");
				for (Integer sentenceIndex : sentences) {
					List<String> tokenTexts = StanfordUtil.getSentenceTokenTexts(datumDoc.getSentenceAnnotation(sentenceIndex));
					for (String tokenText : tokenTexts) {
						String cleanToken = cleanFn.transform(tokenText);
						if (cleanToken == null || cleanToken.length() == 0)
							continue;
						retStr.append(cleanToken).append(" ");
					}
				}
				
				return retStr.toString();
			}
		};
		
		this.corpKeyFn = corpKeyFn;
		this.cleanFn = cleanFn;
	
		this.lda.load();
	}
	
	private String getNamePrefix() {
		return "LDA_" + this.lda.getName() + "_" + this.corpKeyFn.toString() + "_" + this.cleanFn.toString() + "_";
	}
	
	@Override
	public void init(List<CorpRelDatum> data) {

	}
	
	@Override
	public void init(String initStr) {

	}
	
	@Override
	public Map<String, Double> computeMapNoInit(CorpRelDatum datum) {
		String namePrefix = getNamePrefix();
		double[] topicDist = this.lda.computeTopicDistribution(datum, this.documentFn);
		Map<String, Double> map = new HashMap<String, Double>(topicDist.length);
		for (int i = 0; i < topicDist.length; i++)
			map.put(namePrefix + i, topicDist[i]);
		return map;
	}
	
	@Override
	public List<String> getNames(List<String> existingNames) {
		String namePrefix = getNamePrefix();
		for (int i = 0; i < this.lda.getNumTopics(); i++)
			existingNames.add(namePrefix + i);
		return existingNames;
	}
	
	@Override
	public List<Double> computeVector(CorpRelDatum datum,
			List<Double> existingVector) {
		double[] topicDist = this.lda.computeTopicDistribution(datum, this.documentFn);
		for (int i = 0; i < topicDist.length; i++)
			existingVector.add(topicDist[i]);
		return existingVector;
	}
	
	@Override
	public CorpRelFeature clone() {
		return new CorpRelFeatureLDA(this.lda, this.corpKeyFn, this.cleanFn);
	}
	
	@Override
	public String toString(boolean withInit) {
		String str = "LDA(lda=" + this.lda.getName() + "Gazetteer, " +
							"corpKeyFn=" + this.corpKeyFn.toString() + ", " +
							"cleanFn=" + this.cleanFn.toString() +
						")";
		
		return str;
	}
}
