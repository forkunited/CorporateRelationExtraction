package corp.scratch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ark.util.OutputWriter;
import corp.data.CorpDataTools;
import corp.data.annotation.AnnotationCache;
import corp.data.annotation.CorpDocumentSet;
import corp.data.annotation.CorpRelDatum;
import corp.data.annotation.CorpRelLabelPath;
import corp.util.CorpProperties;
import edu.stanford.nlp.util.Pair;

public class ComputeAnnotationAgreement {
	public static void main(String args[]) {
		OutputWriter output = new OutputWriter();
		CorpProperties properties = new CorpProperties();
		CorpDataTools dataTools = new CorpDataTools(properties, output);
		AnnotationCache annotationCache = new AnnotationCache(
			properties.getStanfordAnnotationDirPath(),
			properties.getStanfordAnnotationCacheSize(),
			properties.getStanfordCoreMapDirPath(),
			properties.getStanfordCoreMapCacheSize(),
			output
		);
	
		CorpDocumentSet trainDocumentSet = new CorpDocumentSet(
				properties.getCorpRelDirPath(), 
				annotationCache,
				1,
				-1,
				0,
				output,
				dataTools.getMetaData("CorpMetaData")
		);
		
		CorpDocumentSet testDocumentSet = new CorpDocumentSet(
				properties.getCorpRelTestDirPath(), 
				annotationCache,
				1,
				-1,
				0,
				output,
				dataTools.getMetaData("CorpMetaData")
		);
		
		CorpDocumentSet fullDocumentSet = new CorpDocumentSet(
				properties.getCorpRelTestDirPath(), 
				annotationCache,
				1,
				-1,
				0,
				output,
				dataTools.getMetaData("CorpMetaData")
		);
		
		fullDocumentSet.add(trainDocumentSet);
		
		List<CorpRelLabelPath> paths = new ArrayList<CorpRelLabelPath>();
		paths.add(null);
		paths.add(CorpRelLabelPath.fromString("SelfRef"));
		paths.add(CorpRelLabelPath.fromString("OCorp"));
		paths.add(CorpRelLabelPath.fromString("NonCorp"));
		paths.add(CorpRelLabelPath.fromString("Generic"));
		paths.add(CorpRelLabelPath.fromString("DontKnow"));
		paths.add(CorpRelLabelPath.fromString("Error"));
		paths.add(CorpRelLabelPath.fromString("OCorp-Family"));
		paths.add(CorpRelLabelPath.fromString("OCorp-Merger"));
		paths.add(CorpRelLabelPath.fromString("OCorp-Legal"));
		paths.add(CorpRelLabelPath.fromString("OCorp-Partner"));
		paths.add(CorpRelLabelPath.fromString("OCorp-NewHire"));
		paths.add(CorpRelLabelPath.fromString("OCorp-Cust"));
		paths.add(CorpRelLabelPath.fromString("OCorp-Suply"));
		paths.add(CorpRelLabelPath.fromString("OCorp-Compete"));
		paths.add(CorpRelLabelPath.fromString("OCorp-News"));
		paths.add(CorpRelLabelPath.fromString("OCorp-Finance"));
		paths.add(CorpRelLabelPath.fromString("NonCorp-US"));
		paths.add(CorpRelLabelPath.fromString("NonCorp-State"));
		paths.add(CorpRelLabelPath.fromString("NonCorp-Suply"));
		paths.add(CorpRelLabelPath.fromString("NonCorp-nonUS"));
		paths.add(CorpRelLabelPath.fromString("NonCorp-Ind"));
		paths.add(CorpRelLabelPath.fromString("NonCorp-Rating"));
		paths.add(CorpRelLabelPath.fromString("NonCorp-University"));
		paths.add(CorpRelLabelPath.fromString("Error-Person"));
		paths.add(CorpRelLabelPath.fromString("Error-Place"));
		paths.add(CorpRelLabelPath.fromString("Error-BadParse"));
		paths.add(CorpRelLabelPath.fromString("Error-Other"));
		paths.add(CorpRelLabelPath.fromString("Error-Garbage"));
		paths.add(CorpRelLabelPath.fromString("OCorp-Family-Parent"));
		paths.add(CorpRelLabelPath.fromString("OCorp-Family-Sub"));
		paths.add(CorpRelLabelPath.fromString("OCorp-Family-Division"));
		paths.add(CorpRelLabelPath.fromString("OCorp-Family-Sister"));
		paths.add(CorpRelLabelPath.fromString("OCorp-Merger-Aqu"));
		paths.add(CorpRelLabelPath.fromString("OCorp-Merger-Target"));
		paths.add(CorpRelLabelPath.fromString("OCorp-Merger-Merge"));
		paths.add(CorpRelLabelPath.fromString("OCorp-Merger-Other"));
		paths.add(CorpRelLabelPath.fromString("OCorp-Legal-Lawsuit"));
		paths.add(CorpRelLabelPath.fromString("OCorp-Legal-Alliance"));
		paths.add(CorpRelLabelPath.fromString("OCorp-Legal-Agreement"));
		paths.add(CorpRelLabelPath.fromString("OCorp-Suply-Sup"));
		paths.add(CorpRelLabelPath.fromString("OCorp-Suply-LegalS"));
		paths.add(CorpRelLabelPath.fromString("OCorp-Suply-IB"));
		paths.add(CorpRelLabelPath.fromString("OCorp-Suply-Cons"));
		paths.add(CorpRelLabelPath.fromString("OCorp-Suply-Audit"));
	
		List<CorpRelLabelPath> validPaths = new ArrayList<CorpRelLabelPath>();
		validPaths.add(CorpRelLabelPath.fromString("SelfRef"));
		validPaths.add(CorpRelLabelPath.fromString("OCorp"));
		validPaths.add(CorpRelLabelPath.fromString("NonCorp"));
		validPaths.add(CorpRelLabelPath.fromString("Generic"));
		validPaths.add(CorpRelLabelPath.fromString("DontKnow"));
		validPaths.add(CorpRelLabelPath.fromString("Error"));
		validPaths.add(CorpRelLabelPath.fromString("OCorp-Family"));
		validPaths.add(CorpRelLabelPath.fromString("OCorp-Merger"));
		validPaths.add(CorpRelLabelPath.fromString("OCorp-Legal"));
		validPaths.add(CorpRelLabelPath.fromString("OCorp-Partner"));
		validPaths.add(CorpRelLabelPath.fromString("OCorp-NewHire"));
		validPaths.add(CorpRelLabelPath.fromString("OCorp-Cust"));
		validPaths.add(CorpRelLabelPath.fromString("OCorp-Suply"));
		validPaths.add(CorpRelLabelPath.fromString("OCorp-Compete"));
		validPaths.add(CorpRelLabelPath.fromString("OCorp-News"));
		validPaths.add(CorpRelLabelPath.fromString("OCorp-Finance"));
		validPaths.add(CorpRelLabelPath.fromString("NonCorp-US"));
		validPaths.add(CorpRelLabelPath.fromString("NonCorp-State"));
		validPaths.add(CorpRelLabelPath.fromString("NonCorp-Suply"));
		validPaths.add(CorpRelLabelPath.fromString("NonCorp-nonUS"));
		validPaths.add(CorpRelLabelPath.fromString("NonCorp-Ind"));
		validPaths.add(CorpRelLabelPath.fromString("NonCorp-Rating"));
		validPaths.add(CorpRelLabelPath.fromString("NonCorp-University"));
		
		output.debugWriteln("-----------------------------------------");
		output.debugWriteln("FULL DOCUMENT SET");
		output.debugWriteln("-----------------------------------------");
		for (CorpRelLabelPath path : paths)
			computeCounts(fullDocumentSet, output, path);
		for (CorpRelLabelPath path : paths)
			computeCohens(fullDocumentSet, output, path, null);
		
		output.debugWriteln("-----------------------------------------");
		output.debugWriteln("TEST DOCUMENT SET");
		output.debugWriteln("-----------------------------------------");
		for (CorpRelLabelPath path : paths)
			computeCounts(testDocumentSet, output, path);
		for (CorpRelLabelPath path : paths)
			computeCohens(testDocumentSet, output, path, null);
		
		output.debugWriteln("-----------------------------------------");
		output.debugWriteln("TRAIN DOCUMENT SET");
		output.debugWriteln("-----------------------------------------");
		for (CorpRelLabelPath path : paths)
			computeCounts(trainDocumentSet, output, path);
		for (CorpRelLabelPath path : paths)
			computeCohens(trainDocumentSet, output, path, null);
	}
	
	private static void computeCounts(CorpDocumentSet documentSet, OutputWriter writer, CorpRelLabelPath pathPrefix) {
		// Sentence to user to mention key to datum
		Map<String, Map<String, Map<String, CorpRelDatum>>> sentenceAnnotatedDatums = documentSet.getSentenceAnnotatedDatums(pathPrefix);
		int sentenceCount = sentenceAnnotatedDatums.size();
		int mentionCount = 0;
		Map<Integer, Integer> mentionAnnotationsHistogram = new HashMap<Integer, Integer>();
		for (Entry<String, Map<String, Map<String, CorpRelDatum>>> sentenceEntry : sentenceAnnotatedDatums.entrySet()) {
			Map<String, Integer> mentionKeyCounts = new HashMap<String, Integer>();
			for (Map<String, CorpRelDatum> mentionMaps : sentenceEntry.getValue().values()) {
				for (String mentionKey : mentionMaps.keySet()) {
					if (!mentionKeyCounts.containsKey(mentionKey))
						mentionKeyCounts.put(mentionKey, 0);
					mentionKeyCounts.put(mentionKey, mentionKeyCounts.get(mentionKey) + 1);
				}
			}
			
			mentionCount += mentionKeyCounts.size();
			for (Integer count :  mentionKeyCounts.values()) {
				if (!mentionAnnotationsHistogram.containsKey(count))
					mentionAnnotationsHistogram.put(count, 1);
				else
					mentionAnnotationsHistogram.put(count, mentionAnnotationsHistogram.get(count) + 1);
			}
				
		}		
		
		if (pathPrefix == null) {
			writer.debugWriteln("Total: Sentences=" + sentenceCount + " Mentions=" + mentionCount);
			writer.debugWriteln("Total annotations per mention histogram: ");
			for (Entry<Integer, Integer> entry : mentionAnnotationsHistogram.entrySet())
				writer.debugWriteln(entry.getKey() + "\t" + entry.getValue());
			writer.debugWriteln("");
		} else {
			writer.debugWriteln(pathPrefix.toString() + ": Sentences=" + sentenceCount + " Mentions=" + mentionCount);
		}
	}
	
	
	private static void computeCohens(CorpDocumentSet documentSet, OutputWriter writer, CorpRelLabelPath pathPrefix, List<CorpRelLabelPath> validPaths) {
		// Sentence to user to mention key to datum
		Map<String, Map<String, Map<String, CorpRelDatum>>> annotatorDatums = documentSet.getAnnotatorDatums(pathPrefix);
		Map<String, Double> pairAgreements = new HashMap<String, Double>();
		Map<String, Integer> pairCounts = new HashMap<String, Integer>();
		Map<String, Double> pairAccuracies = new HashMap<String, Double>();
		for (Entry<String, Map<String, Map<String, CorpRelDatum>>> annotatorEntry1 : annotatorDatums.entrySet()) {
			Map<CorpRelLabelPath, Double> annotatorP1 = computeLabelDistribution(annotatorEntry1.getValue(), validPaths);
			for (Entry<String, Map<String, Map<String, CorpRelDatum>>> annotatorEntry2 : annotatorDatums.entrySet()) {
				if (annotatorEntry1.getKey().compareTo(annotatorEntry2.getKey()) >= 0)
					continue;
				Map<CorpRelLabelPath, Double> annotatorP2 = computeLabelDistribution(annotatorEntry2.getValue(), validPaths);
				Pair<Double, Integer> agreementCount = computeAgreement(annotatorEntry1.getValue(), annotatorEntry2.getValue(), validPaths);
				double expectedAgreement = computeExpectedAgreement(annotatorP1, annotatorP2);
				String pairKey = annotatorEntry1.getKey() + "\t" + annotatorEntry2.getKey();
				
				pairAgreements.put(pairKey, expectedAgreement < 1 && agreementCount.second() > 0 ? (agreementCount.first() - expectedAgreement)/(1 - expectedAgreement) : 0);
				pairAccuracies.put(pairKey, agreementCount.first());
				pairCounts.put(pairKey, agreementCount.second());
			}
		}		
		
		if (pathPrefix == null) {
			int annotatorCount = annotatorDatums.size();
			writer.debugWriteln("Overall Annotator Count: " + annotatorCount);
			writer.debugWriteln("Overall Agreements: ");
			double total = 0;
			int count = 0;
			for (Entry<String, Double> agreementEntry : pairAgreements.entrySet()) {
				int pairCount = pairCounts.get(agreementEntry.getKey());
				writer.debugWriteln(agreementEntry.getKey() + "\t" + agreementEntry.getValue() + " (" + pairAccuracies.get(agreementEntry.getKey()) + ")\t" + pairCount);
				if (pairCount > 0) {
					total += agreementEntry.getValue();
					count++;
				}
			}
			
			writer.debugWriteln("Overall Average: " + ((count > 0) ? (total/count) : "NaN"));
		} else {
			double total = 0;
			int count = 0;
			for (Entry<String, Double> agreementEntry : pairAgreements.entrySet()) {
				int pairCount = pairCounts.get(agreementEntry.getKey());
				if (pairCount > 0) {
					total += agreementEntry.getValue();
					count++;
				}
			}
			writer.debugWriteln(pathPrefix + " Average: " + ((count > 0) ? (total/count) : "NaN"));
		}
	}
	
	private static Map<CorpRelLabelPath, Double> computeLabelDistribution(Map<String, Map<String, CorpRelDatum>> data, List<CorpRelLabelPath> validPaths) {
		Map<CorpRelLabelPath, Double> dist = new HashMap<CorpRelLabelPath, Double>();
		double total = 0;
		for (Entry<String, Map<String, CorpRelDatum>> entry1 : data.entrySet()) {
			for (Entry<String, CorpRelDatum> entry2 : entry1.getValue().entrySet()) {
				CorpRelLabelPath path = null;
				if (validPaths == null)
					path = entry2.getValue().getLabelPath();
				else 
					path = entry2.getValue().getLabelPath().getLongestValidPrefix(validPaths);
				
				if (!dist.containsKey(path))
					dist.put(path, 1.0);
				else
					dist.put(path,  dist.get(path) + 1.0);
				total++;
			}
		}
		
		for (Entry<CorpRelLabelPath, Double> entry : dist.entrySet()) {
			entry.setValue(total > 0 ? entry.getValue() / total : 0);
		}
		
		return dist;
	}
	
	private static Pair<Double, Integer> computeAgreement(Map<String, Map<String, CorpRelDatum>> data1, Map<String, Map<String, CorpRelDatum>> data2, List<CorpRelLabelPath> validPaths) {
		double agree = 0.0;
		int total = 0;
		for (Entry<String, Map<String, CorpRelDatum>> entry1 : data1.entrySet()) {
			for (Entry<String, CorpRelDatum> entry2 : entry1.getValue().entrySet()) {
				if (!data2.containsKey(entry1.getKey()) || !data2.get(entry1.getKey()).containsKey(entry2.getKey()))
					continue;
				
				CorpRelLabelPath path1 = null, path2 = null;
				
				if (validPaths == null) {
					path2 = data2.get(entry1.getKey()).get(entry2.getKey()).getLabelPath();
					path1 = entry2.getValue().getLabelPath();
				} else {
					path2 = data2.get(entry1.getKey()).get(entry2.getKey()).getLabelPath().getLongestValidPrefix(validPaths);
					path1 = entry2.getValue().getLabelPath().getLongestValidPrefix(validPaths);
				}
				
				if (path1.equals(path2)) {
					agree += 1.0;
				}
				
				total++;
			}
		}
		
		return new Pair<Double, Integer>((total > 0 ? agree/total : 0.0), total);
	}
	
	private static double computeExpectedAgreement(Map<CorpRelLabelPath, Double> dist1, Map<CorpRelLabelPath, Double> dist2) {
		double expectedAgreement = 0.0;
		for (Entry<CorpRelLabelPath, Double> e1 : dist1.entrySet()) {
			if (!dist2.containsKey(e1.getKey()))
				continue;
			expectedAgreement += e1.getValue()*dist2.get(e1.getKey());
		}
		return expectedAgreement;
	}
}
