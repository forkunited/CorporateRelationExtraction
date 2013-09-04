package corp.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import corp.data.annotation.CorpDocumentSet;
import corp.data.annotation.CorpDocumentTokenSpan;
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
		
		System.out.println("Loading document set...");
		
		/* This is the document set.  It represents a set of annotated documents. */
		CorpDocumentSet documentSet = new CorpDocumentSet(
				properties.getCorpRelDirPath(), 
				properties.getStanfordAnnotationDirPath(),
				properties.getStanfordAnnotationCacheSize(),
				3
		);
		
		System.out.println("Loaded " + documentSet.getDocuments().size() + " documents.");
		System.out.println("Constructing data set...");
		
		/* Construct corporate relation data set from documents */
		CorpRelFeaturizedDataSet dataSet = new CorpRelFeaturizedDataSet(documentSet);
		
		System.out.println("Initializing features...");
		
		/* Add features to data set */
		dataSet.addFeature(
				new CorpRelFeatureNGramSentence(
						documentSet.getDocuments(), 
						dataSet.getData(), 
						1 /* n */, 
						2  /* minFeatureOccurrence */)
		);
		
		dataSet.addFeature(
			new CorpRelFeatureNGramContext(
					documentSet.getDocuments(), 
					dataSet.getData(), 
					1 /* n */, 
					2 /*minFeatureOccurrence*/,
					1 /* contextWindowSize */)
		);
		
		dataSet.addFeature(
			new CorpRelFeatureNGramDep(
					documentSet.getDocuments(), 
					dataSet.getData(), 
					1 /* n */, 
					2  /* minFeatureOccurrence */,
					CorpRelFeatureNGramDep.Mode.ParentsAndChildren /* mode */,
					false /* useRelationTypes */)
		);
		
		System.out.println("Computing featurized data set...");
		
		/* Compute featurized data */
		List<CorpRelFeaturizedDatum> featurizedData = dataSet.getFeaturizedData();
		for (CorpRelFeaturizedDatum datum : featurizedData) {
			CorpDocumentTokenSpan otherOrg = datum.getOtherOrgTokenSpans().get(0);
			List<String> tokens = StanfordUtil.getDocumentSentenceTokenTexts(datum.getDocument().getAnnotation(), otherOrg.getSentenceIndex());
			String otherOrgStr = "";
			for (int i = otherOrg.getTokenStartIndex(); i < otherOrg.getTokenEndIndex(); i++)
				otherOrgStr += tokens.get(i) + " ";
			
			List<String> features = dataSet.getFeatureNames();
			for (int i = 0; i < features.size(); i++) {
				System.out.println(datum.getAuthorCorpName() + "\t" + otherOrgStr + "\t" + datum.getLastLabel() + "\t" + features.get(i) + "\t" + datum.getFeatureValues().get(i));
			}
		}
		
		System.out.println("Training CReg...");
		
		List<CorpRelLabel> validLabels = new ArrayList<CorpRelLabel>();
		validLabels.add(CorpRelLabel.SelfRef);
		validLabels.add(CorpRelLabel.OCorp);
		validLabels.add(CorpRelLabel.NonCorp);
		validLabels.add(CorpRelLabel.Generic);
		validLabels.add(CorpRelLabel.Error);
		ModelCReg model = new ModelCReg(properties.getCregCommandPath(), validLabels);
		model.train(dataSet, new File(properties.getCregDataDirPath(), "scratch").getAbsolutePath());
		
		System.out.println("Predicting CReg...");
		
		List<Pair<CorpRelFeaturizedDatum, CorpRelLabel>> predictions = model.classify(dataSet);
		
		System.out.println("Made " + predictions.size() + " predictions.");
		
		System.out.println("Done.");
	}
}
