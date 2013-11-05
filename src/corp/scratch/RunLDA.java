package corp.scratch;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ark.data.Gazetteer;
import ark.util.OutputWriter;
import ark.util.StanfordUtil;
import ark.util.StringUtil;
import corp.data.CorpMetaData;
import corp.data.annotation.AnnotationCache;
import corp.data.annotation.CorpDocument;
import corp.data.annotation.CorpDocumentSet;
import corp.data.annotation.CorpDocumentTokenSpan;
import corp.data.annotation.CorpRelDatum;
import corp.util.CorpKeyFn;
import corp.util.CorpProperties;
import corp.util.LDA;

public class RunLDA {
	public static void main(String[] args) {
		String name = args[0];
		int numTopics = Integer.parseInt(args[1]);
		int iterations = Integer.parseInt(args[2]);
		int maxThreads = Integer.parseInt(args[3]);
		int maxDocuments = Integer.parseInt(args[4]);
		int randomSeed = Integer.parseInt(args[5]);
		
		OutputWriter output = new OutputWriter();
		CorpProperties properties = new CorpProperties();
		
		Gazetteer stopWordGazetteer = new Gazetteer("StopWord", properties.getStopWordGazetteerPath());
		Gazetteer tickerGazetteer = new Gazetteer("BloombergCorpTickerGazetteer", properties.getBloombergCorpTickerGazetteerPath());

		StringUtil.StringTransform stopWordCleanFn =  StringUtil.getStopWordsCleanFn(stopWordGazetteer);
		final StringUtil.StringTransform cleanFn = StringUtil.getDefaultCleanFn();
		final StringUtil.StringTransform corpKeyFn = new CorpKeyFn(tickerGazetteer,  stopWordCleanFn);
		
		
		LDA lda = new LDA(name,
						  new File(properties.getLDASourceDirectory()), 
						  maxThreads, 
						  output);
		
		System.out.println("Loading documents...");
		
		AnnotationCache annotationCache = new AnnotationCache(
				properties.getStanfordAnnotationDirPath(),
				properties.getStanfordAnnotationCacheSize(),
				new OutputWriter()
		);
			
		CorpDocumentSet documentSet = new CorpDocumentSet(
				properties.getCorpRelDirPath(), 
				annotationCache,
				maxThreads,
				0,
				maxDocuments,
				new OutputWriter(),
				false,
				new CorpMetaData("Corp", properties.getCorpMetaDataPath())
		);
		
		LDA.DatumDocumentTransform documentFn = new LDA.DatumDocumentTransform() {
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
		
		lda.run(documentSet, documentFn, randomSeed, numTopics, iterations);
		
		/* Test */
		lda = new LDA(name,
			  new File(properties.getLDASourceDirectory()), 
			  maxThreads, 
			  output);
		System.out.println(lda.load());
		CorpDocument document = documentSet.getDocuments().get(0);
		CorpRelDatum datum = document.loadUnannotatedCorpRels(false).get(0);
		double[] dist = lda.computeTopicDistribution(datum, documentFn);
		System.out.println(dist.toString());
		System.out.println(datum);
	}	
}
