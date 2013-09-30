package corp.scratch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import corp.data.annotation.AnnotationCache;
import corp.data.annotation.CorpDocumentSet;
import corp.data.annotation.CorpRelDatum;
import corp.data.annotation.CorpRelLabel;
import corp.data.annotation.CorpRelLabelPath;
import corp.data.feature.CorpRelFeatureSelfEditDistance;
import corp.data.feature.CorpRelFeatureSelfInitialism;
import corp.data.feature.CorpRelFeatureSelfPrefixTokens;
import corp.data.feature.CorpRelFeaturizedDataSet;
import corp.model.ModelCReg;
import corp.util.CorpProperties;
import corp.util.OutputWriter;
import edu.stanford.nlp.util.Pair;

/**
 * For periodically testing out short temporary snippets of code
 */
public class Scratch {
	public static void main(String[] args) {
		System.out.println("Loading configuration properties...");
		
		CorpProperties properties = new CorpProperties("corp.properties");
		
		System.out.println("Loading Annotation Cache...");
		AnnotationCache annotationCache = new AnnotationCache(
			properties.getStanfordAnnotationDirPath(),
			properties.getStanfordAnnotationCacheSize(),
			properties.getStanfordCoreMapDirPath(),
			properties.getStanfordCoreMapCacheSize(),
			new OutputWriter()
		);
		
		System.out.println("Loading document set...");
		
		/* This is the document set.  It represents a set of annotated documents. */
		CorpDocumentSet documentSet = new CorpDocumentSet(
				properties.getCorpRelDirPath(), 
				annotationCache,
				4,
				args.length > 0 ? Integer.parseInt(args[0]) : 5,
				0,
				new OutputWriter()
		);
		
		System.out.println("Loaded " + documentSet.getDocuments().size() + " documents.");
		System.out.println("Constructing data set...");
		
		List<CorpRelLabelPath> validLabels = new ArrayList<CorpRelLabelPath>();
		validLabels.add(new CorpRelLabelPath(CorpRelLabel.SelfRef));
		validLabels.add(new CorpRelLabelPath(CorpRelLabel.OCorp));
		validLabels.add(new CorpRelLabelPath(CorpRelLabel.NonCorp));
		validLabels.add(new CorpRelLabelPath(CorpRelLabel.Generic));
		validLabels.add(new CorpRelLabelPath(CorpRelLabel.Error));
		
		/* Construct corporate relation data set from documents */
		CorpRelFeaturizedDataSet dataSet = new CorpRelFeaturizedDataSet(documentSet, new OutputWriter());
		System.out.println("Loaded " + dataSet.getData().size() + " datums.");
		
		dataSet.addFeature(
				new CorpRelFeatureSelfEditDistance()
		);
		
		dataSet.addFeature(
				new CorpRelFeatureSelfInitialism(true)
		);
		
		dataSet.addFeature(
				new CorpRelFeatureSelfPrefixTokens(1)
		);
		
		ModelCReg model = new ModelCReg(properties.getCregCommandPath(), validLabels, new OutputWriter());
		model.train(dataSet, new File(properties.getCregDataDirPath(), "PosteriorTest").getAbsolutePath());
		List<Pair<CorpRelDatum, Map<CorpRelLabelPath, Double>>> posterior = model.posterior(dataSet);
		for (Pair<CorpRelDatum, Map<CorpRelLabelPath, Double>> datumPosterior : posterior) {
			System.out.println(datumPosterior.first().toString());
			for (Entry<CorpRelLabelPath, Double> entry : datumPosterior.second().entrySet())
				System.out.print(entry.getKey() + " " + entry.getValue() + "\t");
			System.out.println();
		}
		
		/*List<String> features = dataSet.getFeatureNames();
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
				
				break;
				//System.out.println("");
			}
			System.out.println("");
		}*/
	}
}
