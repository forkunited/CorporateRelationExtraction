package corp.scratch;

import java.util.ArrayList;
import java.util.List;

import corp.data.Gazetteer;
import corp.data.annotation.AnnotationCache;
import corp.data.annotation.CorpDocumentSet;
import corp.data.annotation.CorpRelLabel;
import corp.data.annotation.CorpRelLabelPath;
import corp.data.feature.CorpRelFeatureSelfEquality;
import corp.data.feature.CorpRelFeaturizedDataSet;
import corp.data.feature.CorpRelFeaturizedDatum;
import corp.util.CorpProperties;
import corp.util.OutputWriter;
import corp.util.StringUtil;

public class CorpKeyFnPerformance {
	public static void main(String[] args) {
		CorpProperties properties = new CorpProperties("corp.properties");
		Gazetteer stopWordGazetteer = new Gazetteer("StopWord", properties.getStopWordGazetteerPath());
		Gazetteer bloombergCorpTickerGazetteer = new Gazetteer("BloombergCorpTickerGazetteer", properties.getBloombergCorpTickerGazetteerPath());
			
		StringUtil.StringTransform stopWordCleanFn = StringUtil.getStopWordsCleanFn(stopWordGazetteer);
		StringUtil.StringTransform corpKeyFn = StringUtil.getCorpKeyFn(bloombergCorpTickerGazetteer,stopWordCleanFn);

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
				new OutputWriter()
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
		double fp = 0.0;
		double fn = 0.0;
		for (CorpRelFeaturizedDatum datum : data) {
			boolean corpKeyFnSelf = datum.getFeatureValues().get(0).equals(1.0);
			boolean labeledSelf = validPaths.get(0).equals(datum.getLabelPath().getLongestValidPrefix(validPaths));
			if (corpKeyFnSelf == labeledSelf)
				tp += 1.0;
			else if (corpKeyFnSelf) {
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
		
		double p = tp/(tp+fp);
		double r = tp/(tp+fn);
		double f1 = 2.0*p*r/(p+r);
		System.out.println("Precision: " + p);
		System.out.println("Recall: " + r);
		System.out.println("F1: " + f1);
	}
}
