package corp.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import corp.data.Gazette;
import corp.data.annotation.AnnotationCache;
import corp.data.annotation.CorpDocumentSet;
import corp.data.annotation.CorpDocumentTokenSpan;
import corp.data.annotation.CorpRelDatum;
import corp.data.annotation.CorpRelLabel;
import corp.data.feature.CorpRelFeatureNGramContext;
import corp.data.feature.CorpRelFeatureNGramDep;
import corp.data.feature.CorpRelFeatureNGramSentence;
import corp.data.feature.CorpRelFeaturizedDataSet;
import corp.data.feature.CorpRelFeaturizedDatum;
import corp.model.ModelCReg;
import corp.util.CorpProperties;
import corp.util.StanfordUtil;
import edu.stanford.nlp.util.Pair;

/**
 * For periodically testing out short temporary snippets of code
 */
public class Scratch {
	public static void main(String[] args) {
		System.out.println("Loading configuration properties...");
		
		CorpProperties properties = new CorpProperties("corp.properties");
		
		System.out.println("Loading Gazettes...");
		Gazette corpGazette = new Gazette("Corp", properties.getCorpGazettePath());
		Gazette nonCorpGazette = new Gazette("NonCorp", properties.getNonCorpGazettePath());
		
		System.out.println("Loading Annotation Cache...");
		AnnotationCache annotationCache = new AnnotationCache(
			properties.getStanfordAnnotationDirPath(),
			properties.getStanfordAnnotationCacheSize(),
			properties.getStanfordCoreMapDirPath(),
			properties.getStanfordCoreMapCacheSize()
		);
		
		System.out.println("Loading document set...");
		
		/* This is the document set.  It represents a set of annotated documents. */
		CorpDocumentSet documentSet = new CorpDocumentSet(
				properties.getCorpRelDirPath(), 
				annotationCache,
				properties.getMaxThreads(),
				args.length > 0 ? Integer.parseInt(args[0]) : 4
		);
		
		System.out.println("Loaded " + documentSet.getDocuments().size() + " documents.");
		System.out.println("Constructing data set...");
		
		/* Construct corporate relation data set from documents */
		CorpRelFeaturizedDataSet dataSet = new CorpRelFeaturizedDataSet(documentSet);
		
		
		List<String> featureNames = dataSet.getFeatureNames();
		for (String featureName : featureNames)
			
		
		
		List<CorpRelFeaturizedDatum> datums = dataSet.getFeaturizedData();
		for (CorpRelFeaturizedDatum datum : datums) {
			System.out.println("Author: " + datum.getAuthorCorpName());
			List<CorpDocumentTokenSpan> tokenSpans = datum.getOtherOrgTokenSpans();
			for (CorpDocumentTokenSpan tokenSpan : tokenSpans) {
				System.out.println("Mentioned: " + tokenSpan.toString() + "(" + tokenSpan.getSentenceIndex() + " "+  tokenSpan.getTokenStartIndex() + " " + tokenSpan.getTokenEndIndex() + ")");
				System.out.println("Mentioned Corp: " + corpGazette.contains(tokenSpan.toString()));
				System.out.println("Author Corp: " + corpGazette.contains(datum.getAuthorCorpName().toString()));
				
				
				List<Double> values = datum.getFeatureValues();
			}
		}
	}
}
