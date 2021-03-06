package corp.scratch;

import java.util.ArrayList;
import java.util.List;

import corp.data.CorpMetaData;
import corp.data.Gazetteer;
import corp.data.annotation.AnnotationCache;
import corp.data.annotation.CorpDocumentSet;
import corp.data.annotation.CorpRelLabel;
import corp.data.annotation.CorpRelLabelPath;
import corp.data.feature.CorpRelFeatureSelfEquality;
import corp.data.feature.CorpRelFeaturizedDataSet;
import corp.data.feature.CorpRelFeaturizedDatum;
import corp.util.CorpKeyFn;
import corp.util.CorpProperties;
import ark.util.OutputWriter;
import corp.util.StringUtil;

/**
 * CorpKeyFnPerformance evaluates an organization name hashing
 * function (represented by corp.util.CorpKeyFn) that resolves names
 * to entities.  The details of this evaluation are given in the
 * "Entity Resolution Function" section (3.3.1) of the Sloan
 * technical report.  The basic idea is that we use author-to-mention
 * relations annotated with "Self-reference" to evaluate whether
 * the hashing function correctly resolves coreferring names
 * to the same entity.
 * 
 * @author Bill McDowell
 *
 */
public class CorpKeyFnPerformance {
	public static void main(String[] args) {
		CorpProperties properties = new CorpProperties();
		Gazetteer stopWordGazetteer = new Gazetteer("StopWord", properties.getStopWordGazetteerPath());
		Gazetteer bloombergCorpTickerGazetteer = new Gazetteer("BloombergCorpTickerGazetteer", properties.getBloombergCorpTickerGazetteerPath());
		Gazetteer nonCorpInitialismGazetteer = new Gazetteer("NonCorpInitialismGazetteer", properties.getNonCorpInitialismGazetteerPath());
		StringUtil.StringTransform stopWordCleanFn = StringUtil.getStopWordsCleanFn(stopWordGazetteer);
		List<Gazetteer> corpKeyFnKeyMaps = new ArrayList<Gazetteer>();
		corpKeyFnKeyMaps.add(bloombergCorpTickerGazetteer);
		corpKeyFnKeyMaps.add(nonCorpInitialismGazetteer);
		StringUtil.StringTransform corpKeyFn = new CorpKeyFn(corpKeyFnKeyMaps,stopWordCleanFn);

		AnnotationCache annotationCache = new AnnotationCache(
			properties.getStanfordAnnotationDirPath(),
			properties.getStanfordAnnotationCacheSize(),
			properties.getStanfordCoreMapDirPath(),
			properties.getStanfordCoreMapCacheSize(),
			new OutputWriter()
		);
		
		CorpDocumentSet documentSet = new CorpDocumentSet(
				properties.getCorpRelDirPath(), 
				annotationCache,
				4,
				-1,
				0,
				new OutputWriter(),
				new CorpMetaData("Corp", properties.getCorpMetaDataPath())
		);
		
		CorpRelFeaturizedDataSet dataSet = new CorpRelFeaturizedDataSet(documentSet, new OutputWriter());
		
		CorpRelFeatureSelfEquality corpKeyFnFeature = new CorpRelFeatureSelfEquality(corpKeyFn);
		corpKeyFnFeature.init(dataSet.getData());
		dataSet.addFeature(corpKeyFnFeature);
		
		List<CorpRelFeaturizedDatum> data = dataSet.getFeaturizedLabeledData();
		
		List<CorpRelLabelPath> validPaths = new ArrayList<CorpRelLabelPath>();
		validPaths.add(new CorpRelLabelPath(CorpRelLabel.SelfRef));
		validPaths.add(new CorpRelLabelPath(CorpRelLabel.OCorp));
		validPaths.add(new CorpRelLabelPath(CorpRelLabel.NonCorp));
		validPaths.add(new CorpRelLabelPath(CorpRelLabel.Generic));
		validPaths.add(new CorpRelLabelPath(CorpRelLabel.Error));
		
		double tp = 0.0;
		double tn = 0.0;
		double fp = 0.0;
		double fn = 0.0;
		for (CorpRelFeaturizedDatum datum : data) {
			CorpRelLabelPath path = datum.getLabelPath().getLongestValidPrefix(validPaths);
			if (path == null)
				continue;
			
			boolean corpKeyFnSelf = datum.getFeatureValues().get(0).equals(1.0);
			boolean labeledSelf = validPaths.get(0).equals(path);
			if (corpKeyFnSelf && labeledSelf) {
				tp += 1.0;
			} else if (!corpKeyFnSelf && !labeledSelf) {
				tn += 1.0;
			} else if (corpKeyFnSelf) {
				fp += 1.0;
				System.out.println("False Positive: " + datum.getAuthorCorpName() + "\t" + datum.getOtherOrgTokenSpans().get(0).toString());
			} else if (labeledSelf) {
				fn += 1.0;
				System.out.println("False Negative: " + datum.getAuthorCorpName() + 
									" (" + corpKeyFn.transform(datum.getAuthorCorpName()) + 
									")\t" + datum.getOtherOrgTokenSpans().get(0).toString() +
									" (" + corpKeyFn.transform(datum.getOtherOrgTokenSpans().get(0).toString()) + ")");
			}
		}
		
		double accuracy = (tp+tn)/(tp+tn+fp+fn);
		double p = tp/(tp+fp);
		double r = tp/(tp+fn);
		double f1 = 2.0*p*r/(p+r);
		System.out.println("Accuracy: " + accuracy);
		System.out.println("Precision: " + p);
		System.out.println("Recall: " + r);
		System.out.println("F1: " + f1);
	}
}
