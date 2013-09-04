package corp.experiment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import corp.data.Gazette;
import corp.data.annotation.CorpDocumentSet;
import corp.data.annotation.CorpRelLabel;
import corp.data.feature.CorpRelFeatureGazette;
import corp.data.feature.CorpRelFeatureGazetteContains;
import corp.data.feature.CorpRelFeatureGazetteEditDistance;
import corp.data.feature.CorpRelFeatureGazetteInitialism;
import corp.data.feature.CorpRelFeatureGazettePrefixTokens;
import corp.data.feature.CorpRelFeatureNGramContext;
import corp.data.feature.CorpRelFeatureNGramDep;
import corp.data.feature.CorpRelFeatureNGramSentence;
import corp.data.feature.CorpRelFeaturizedDataSet;
import corp.model.ModelCReg;
import corp.model.evaluation.KFoldCrossValidation;
import corp.util.CorpProperties;

public class CRegKFoldCrossValidation {
	public static void main(String[] args) {
		System.out.println("Loading configuration properties...");
		
		CorpProperties properties = new CorpProperties("corp.properties");
		
		System.out.println("Loading Gazettes...");
		Gazette corpGazette = new Gazette("Corp", properties.getCorpGazettePath());
		Gazette nonCorpGazette = new Gazette("NonCorp", properties.getNonCorpGazettePath());
		
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
		
		/* Gazette contains features */
		
		dataSet.addFeature(
				new CorpRelFeatureGazetteContains(
						documentSet.getDocuments(), 
						dataSet.getData(), 
						corpGazette, 
						CorpRelFeatureGazette.InputType.Mentioned)
			);
		
		dataSet.addFeature(
				new CorpRelFeatureGazetteContains(
						documentSet.getDocuments(), 
						dataSet.getData(), 
						corpGazette, 
						CorpRelFeatureGazette.InputType.Mentioner)
			);
		
		dataSet.addFeature(
				new CorpRelFeatureGazetteContains(
						documentSet.getDocuments(), 
						dataSet.getData(), 
						nonCorpGazette, 
						CorpRelFeatureGazette.InputType.Mentioned)
			);
		
		dataSet.addFeature(
				new CorpRelFeatureGazetteContains(
						documentSet.getDocuments(), 
						dataSet.getData(), 
						nonCorpGazette, 
						CorpRelFeatureGazette.InputType.Mentioner)
			);
		
		/* Gazette edit distance features */
		
		dataSet.addFeature(
				new CorpRelFeatureGazetteEditDistance(
						documentSet.getDocuments(), 
						dataSet.getData(), 
						corpGazette, 
						CorpRelFeatureGazette.InputType.Mentioned)
			);
		
		dataSet.addFeature(
				new CorpRelFeatureGazetteEditDistance(
						documentSet.getDocuments(), 
						dataSet.getData(), 
						corpGazette, 
						CorpRelFeatureGazette.InputType.Mentioner)
			);
		
		dataSet.addFeature(
				new CorpRelFeatureGazetteEditDistance(
						documentSet.getDocuments(), 
						dataSet.getData(), 
						nonCorpGazette, 
						CorpRelFeatureGazette.InputType.Mentioned)
			);
		
		dataSet.addFeature(
				new CorpRelFeatureGazetteEditDistance(
						documentSet.getDocuments(), 
						dataSet.getData(), 
						nonCorpGazette, 
						CorpRelFeatureGazette.InputType.Mentioner)
			);
		
		
		/* Gazette initialism features */
		
		dataSet.addFeature(
				new CorpRelFeatureGazetteInitialism(
						documentSet.getDocuments(), 
						dataSet.getData(), 
						corpGazette, 
						CorpRelFeatureGazette.InputType.Mentioned)
			);
		
		dataSet.addFeature(
				new CorpRelFeatureGazetteInitialism(
						documentSet.getDocuments(), 
						dataSet.getData(), 
						corpGazette, 
						CorpRelFeatureGazette.InputType.Mentioner)
			);
		
		dataSet.addFeature(
				new CorpRelFeatureGazetteInitialism(
						documentSet.getDocuments(), 
						dataSet.getData(), 
						nonCorpGazette, 
						CorpRelFeatureGazette.InputType.Mentioned)
			);
		
		dataSet.addFeature(
				new CorpRelFeatureGazetteInitialism(
						documentSet.getDocuments(), 
						dataSet.getData(), 
						nonCorpGazette, 
						CorpRelFeatureGazette.InputType.Mentioner)
			);
	
		/* Gazette prefix token features */
		
		dataSet.addFeature(
				new CorpRelFeatureGazettePrefixTokens(
						documentSet.getDocuments(), 
						dataSet.getData(), 
						corpGazette, 
						CorpRelFeatureGazette.InputType.Mentioned,
						2)
			);
		
		dataSet.addFeature(
				new CorpRelFeatureGazettePrefixTokens(
						documentSet.getDocuments(), 
						dataSet.getData(), 
						corpGazette, 
						CorpRelFeatureGazette.InputType.Mentioner,
						2)
			);
		
		dataSet.addFeature(
				new CorpRelFeatureGazettePrefixTokens(
						documentSet.getDocuments(), 
						dataSet.getData(), 
						nonCorpGazette, 
						CorpRelFeatureGazette.InputType.Mentioned,
						2)
			);
		
		dataSet.addFeature(
				new CorpRelFeatureGazettePrefixTokens(
						documentSet.getDocuments(), 
						dataSet.getData(), 
						nonCorpGazette, 
						CorpRelFeatureGazette.InputType.Mentioner,
						2)
			);
		
		
		System.out.println("Running CReg Cross Validation...");
		
		List<CorpRelLabel> validLabels = new ArrayList<CorpRelLabel>();
		validLabels.add(CorpRelLabel.SelfRef);
		validLabels.add(CorpRelLabel.OCorp);
		validLabels.add(CorpRelLabel.NonCorp);
		validLabels.add(CorpRelLabel.Generic);
		validLabels.add(CorpRelLabel.Error);
		ModelCReg model = new ModelCReg(properties.getCregCommandPath(), validLabels);
		
		KFoldCrossValidation validation = new KFoldCrossValidation(
				model, 
				dataSet,
				2,
				new File(properties.getCregDataDirPath(), "2FoldCV").getAbsolutePath()
		);
		
		validation.run();
		
		System.out.println("Done.");
	}
}
