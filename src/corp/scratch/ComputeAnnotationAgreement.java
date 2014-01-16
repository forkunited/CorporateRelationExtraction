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
	
		CorpDocumentSet documentSet = new CorpDocumentSet(
				properties.getCorpRelDirPath(), 
				annotationCache,
				1,
				100,
				0,
				output,
				dataTools.getMetaData("CorpMetaData")
		);
		
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
	
		for (CorpRelLabelPath path : paths)
			computeCounts(documentSet, output, path);
		//for (CorpRelLabelPath path : paths)
		//	computeAgreement(documentSet, output, path);
	}
	
	private static void computeCounts(CorpDocumentSet documentSet, OutputWriter writer, CorpRelLabelPath pathPrefix) {
		// Sentence to user to mention key to datum
		Map<String, Map<String, Map<String, CorpRelDatum>>> sentenceAnnotatedDatums = documentSet.getSentenceAnnotatedDatums();
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
	
	/*
	private static void computeCounts(CorpDocumentSet documentSet, OutputWriter writer, CorpRelLabelPath pathPrefix) {
		// Sentence to user to mention key to datum
		Map<String, Map<String, Map<String, CorpRelDatum>>> sentenceAnnotatedDatums = documentSet.getSentenceAnnotatedDatums();
		int sentenceCount = sentenceAnnotatedDatums.size();
		int mentionCount = 0;
		for (Entry<String, Map<String, Map<String, CorpRelDatum>>> sentenceEntry : sentenceAnnotatedDatums.entrySet()) {
			Set<String> mentionKeys = new HashSet<String>();
			for (Map<String, CorpRelDatum> mentionMaps : sentenceEntry.getValue().values())
				mentionKeys.addAll(mentionMaps.keySet());
			mentionCount += mentionKeys.size();
		}		
		
		writer.debugWriteln(pathPrefix.toString() + ": Sentences=" + sentenceCount + " Mentions=" + mentionCount);
	}*/
}
