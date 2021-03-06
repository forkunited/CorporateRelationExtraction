package corp.scratch;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import corp.data.Gazetteer;
import ark.util.OutputWriter;
import ark.util.StanfordUtil;
import corp.util.StringUtil;
import corp.data.CorpMetaData;
import corp.data.annotation.AnnotationCache;
import corp.data.annotation.CorpDocument;
import corp.data.annotation.CorpDocumentSet;
import corp.data.annotation.CorpDocumentTokenSpan;
import corp.data.annotation.CorpRelDatum;
import corp.util.CorpKeyFn;
import corp.util.CorpProperties;
import corp.util.LDA;

/**
 * RunLDA uses the corp.util.LDA wrapper class to run the
 * Mallet implementation of LDA (http://mallet.cs.umass.edu/) on the corpus 
 * of press release documents.  RunLDA can be set to LDA treat either full 
 * press releases or just sentences containing organization mentions as 
 * LDA documents.
 * 
 * @author Bill McDowell
 *
 */
public class RunLDA {
	public static void main(String[] args) {
		String name = args[0];
		int numTopics = Integer.parseInt(args[1]);
		int iterations = Integer.parseInt(args[2]);
		int maxThreads = Integer.parseInt(args[3]);
		int maxDocuments = Integer.parseInt(args[4]);
		int randomSeed = Integer.parseInt(args[5]);
		boolean sentenceLevel = Boolean.parseBoolean(args[6]);
		
		OutputWriter output = new OutputWriter();
		CorpProperties properties = new CorpProperties();
		
		Gazetteer stopWordGazetteer = new Gazetteer("StopWord", properties.getStopWordGazetteerPath());
		Gazetteer bloombergCorpTickerGazetteer = new Gazetteer("BloombergCorpTickerGazetteer", properties.getBloombergCorpTickerGazetteerPath());
		Gazetteer nonCorpInitialismGazetteer = new Gazetteer("NonCorpInitialismGazetteer", properties.getNonCorpInitialismGazetteerPath());
		List<Gazetteer> corpKeyFnKeyMaps = new ArrayList<Gazetteer>();
		corpKeyFnKeyMaps.add(bloombergCorpTickerGazetteer);
		corpKeyFnKeyMaps.add(nonCorpInitialismGazetteer);
		
		StringUtil.StringTransform stopWordCleanFn =  StringUtil.getStopWordsCleanFn(stopWordGazetteer);
		final StringUtil.StringTransform cleanFn = StringUtil.getDefaultCleanFn();
		final StringUtil.StringTransform corpKeyFn = new CorpKeyFn(corpKeyFnKeyMaps,  stopWordCleanFn);
		
		
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
		
		LDA.DatumDocumentTransform documentFn = null;
		
		if (sentenceLevel) {
			documentFn = new LDA.DatumDocumentTransform() {
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
		}
		
		lda.run(documentSet, documentFn, randomSeed, numTopics, iterations);
		
		/* Test */
		if (sentenceLevel) {
			lda = new LDA(name,
				  new File(properties.getLDASourceDirectory()), 
				  maxThreads, 
				  output);
			System.out.println(lda.load());
			CorpDocument document = documentSet.getDocuments().get(0);
			CorpRelDatum datum = document.loadUnannotatedCorpRels(false).get(0);
			double[] dist = lda.computeTopicDistribution(datum, documentFn);
			for (int i = 0; i < dist.length; i++)
				System.out.println(dist[i]);
			System.out.println(datum);
		}
	}	
}
