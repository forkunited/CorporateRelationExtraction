package corp.test;

import java.util.ArrayList;
import java.util.List;

import corp.data.Gazette;
import corp.data.annotation.AnnotationCache;
import corp.data.annotation.CorpDocumentSet;
import corp.data.annotation.CorpDocumentTokenSpan;
import corp.data.annotation.CorpRelLabel;
import corp.data.feature.CorpRelFeatureSelfEditDistance;
import corp.data.feature.CorpRelFeatureSelfInitialism;
import corp.data.feature.CorpRelFeatureSelfPrefixTokens;
import corp.data.feature.CorpRelFeaturizedDataSet;
import corp.data.feature.CorpRelFeaturizedDatum;
import corp.util.CorpProperties;

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
				args.length > 0 ? Integer.parseInt(args[0]) : 8
		);
		
		System.out.println("Loaded " + documentSet.getDocuments().size() + " documents.");
		System.out.println("Constructing data set...");
		
		List<CorpRelLabel> validLabels = new ArrayList<CorpRelLabel>();
		validLabels.add(CorpRelLabel.SelfRef);
		validLabels.add(CorpRelLabel.OCorp);
		validLabels.add(CorpRelLabel.NonCorp);
		validLabels.add(CorpRelLabel.Generic);
		validLabels.add(CorpRelLabel.Error);
		
		/* Construct corporate relation data set from documents */
		CorpRelFeaturizedDataSet dataSet = new CorpRelFeaturizedDataSet(documentSet);
		
		dataSet.addFeature(
				new CorpRelFeatureSelfEditDistance()
		);
		
		dataSet.addFeature(
				new CorpRelFeatureSelfInitialism(true)
		);
		
		dataSet.addFeature(
				new CorpRelFeatureSelfPrefixTokens(1)
		);
		
		List<String> features = dataSet.getFeatureNames();
		List<CorpRelFeaturizedDatum> datums = dataSet.getFeaturizedData();
		for (CorpRelFeaturizedDatum datum : datums) {
			System.out.println("Author: " + datum.getAuthorCorpName());
			List<CorpDocumentTokenSpan> tokenSpans = datum.getOtherOrgTokenSpans();
			for (CorpDocumentTokenSpan tokenSpan : tokenSpans) {
				System.out.println("Mentioned: " + tokenSpan.toString() + "(" + tokenSpan.getSentenceIndex() + " "+  tokenSpan.getTokenStartIndex() + " " + tokenSpan.getTokenEndIndex() + ")");
				System.out.println("Mentioned Corp: " + corpGazette.contains(tokenSpan.toString()));
				System.out.println("Author Corp: " + corpGazette.contains(datum.getAuthorCorpName().toString()));
				System.out.println("Mentioned NonCorp: " + nonCorpGazette.contains(tokenSpan.toString()));
				System.out.println("Author NonCorp: " + nonCorpGazette.contains(datum.getAuthorCorpName()));
				System.out.println("Label: " + datum.getLabel(validLabels));
				
				List<Double> values = datum.getFeatureValues();
				for (int i = 0; i < features.size(); i++) {
					System.out.println(features.get(i) + "\t" + values.get(i));
				}
				System.out.println("");
			}
			System.out.println("");
		}
	}
}
