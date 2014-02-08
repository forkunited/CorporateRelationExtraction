package corp.scratch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import corp.data.CorpMetaData;
import corp.data.annotation.AnnotationCache;
import corp.data.annotation.CorpDocumentSet;
import corp.data.annotation.CorpDocumentTokenSpan;
import corp.data.annotation.CorpRelDatum;
import corp.data.annotation.CorpRelLabel;
import corp.data.feature.CorpRelFeaturizedDataSet;
import corp.util.CorpProperties;
import ark.data.CounterTable;
import ark.util.OutputWriter;
import ark.util.StringUtil;

/**
 * MiscellaneousStats contains functions for computing miscellaneous 
 * statistics on the corporate press release documents.  Currently,
 * this just contains a function that computes a histogram of unigrams
 * that occur within mentioned organization names.  This histogram was
 * used to motivate choices for organization stop words to use in 
 * the entity resolution function (H in the Sloan tech report implemented
 * by corp.util.CorpKeyFn).
 * 
 * @author Bill McDowell
 *
 */
public class MiscellaneousStats {
	public static void main(String[] args) {
		computeOrganizationTermCounts();
	}
	
	public static void computeOrganizationTermCounts() {
		System.out.println("Loading configuration properties...");
		
		CorpProperties properties = new CorpProperties();
		
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
				-1,
				0,
				new OutputWriter(),
				new CorpMetaData("Corp", properties.getCorpMetaDataPath())
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
		CorpRelFeaturizedDataSet dataSet = new CorpRelFeaturizedDataSet(documentSet, new OutputWriter());
		List<CorpRelDatum> data = dataSet.getData();
		CounterTable terms = new CounterTable();
		for (CorpRelDatum datum : data) {
			List<CorpDocumentTokenSpan> tokenSpans = datum.getOtherOrgTokenSpans();
			String authorCorpName = datum.getAuthorCorpName();

			String[] authorTerms = StringUtil.clean(authorCorpName).split(" ");
			for (String authorTerm : authorTerms)
				terms.incrementCount(authorTerm);
			
			for (CorpDocumentTokenSpan tokenSpan : tokenSpans) {
				String mentionedCorpName = tokenSpan.toString();
				String[] mentionedTerms = StringUtil.clean(mentionedCorpName).split(" ");
				for (String mentionedTerm : mentionedTerms)
					terms.incrementCount(mentionedTerm);
			}
		}
		
		TreeMap<Integer, List<String>> sortedCounts = terms.getSortedCounts();
		for (Entry<Integer, List<String>> entry : sortedCounts.entrySet()) {
			for (String term : entry.getValue()) {
				System.out.println(entry.getKey() + " " + term);
			}
		}
	}
}
