package corp.experiment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import corp.data.Gazette;
import corp.data.annotation.AnnotationCache;
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
import corp.data.feature.CorpRelFeatureSelfEditDistance;
import corp.data.feature.CorpRelFeatureSelfInitialism;
import corp.data.feature.CorpRelFeatureSelfPrefixTokens;
import corp.data.feature.CorpRelFeaturizedDataSet;
import corp.model.ModelCReg;
import corp.model.evaluation.KFoldCrossValidation;
import corp.util.CorpProperties;
import corp.util.StringUtil;

public class CRegKFoldCrossValidation {
	public static void main(String[] args) {
		System.out.println("Loading configuration properties...");
		
		CorpProperties properties = new CorpProperties("corp.properties");
		
		Random rand = new Random(properties.getRandomSeed());
		
		System.out.println("Loading Gazettes...");

		Gazette stopWordsGazette = new Gazette("StopWords", properties.getStopWordGazettePath());
		StringUtil.StringTransform stopWordsCleanFn = StringUtil.getStopWordsCleanFn(stopWordsGazette);
		
		//Gazette corpGazette = new Gazette("Corp", properties.getCorpGazettePath());
		Gazette cleanCorpGazette = new Gazette("StopWordsCorp", properties.getCorpGazettePath(), stopWordsCleanFn);
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
				args.length > 0 ? Integer.parseInt(args[0]) : 0
		);
		
		System.out.println("Loaded " + documentSet.getDocuments().size() + " documents.");
		System.out.println("Constructing data set...");
		
		/* Construct corporate relation data set from documents */
		CorpRelFeaturizedDataSet dataSet = new CorpRelFeaturizedDataSet(documentSet);
		
		System.out.println("Adding features...");
		
		/* Add features to data set */
		//dataSet.addFeature(
		//		new CorpRelFeatureNGramSentence(
		//				1 /* n */, 
		//				2  /* minFeatureOccurrence */)
		//);
		
		dataSet.addFeature(
			new CorpRelFeatureNGramContext(
					1 /* n */, 
					2 /*minFeatureOccurrence*/,
					0 /* contextWindowSize */)
		);
		
		//dataSet.addFeature(
		//	new CorpRelFeatureNGramDep(
		//			1 /* n */, 
		//			2  /* minFeatureOccurrence */,
		//			CorpRelFeatureNGramDep.Mode.ParentsAndChildren /* mode */,
		//			true /* useRelationTypes */)
		//);
		
		/* Gazette contains features */
		
		dataSet.addFeature(
				new CorpRelFeatureGazetteContains(
						stopWordsGazette, 
						CorpRelFeatureGazette.InputType.Mentioned)
			);
		
		/*dataSet.addFeature(
				new CorpRelFeatureGazetteContains(
						corpGazette, 
						CorpRelFeatureGazette.InputType.Mentioned)
			);
		*/
		dataSet.addFeature(
				new CorpRelFeatureGazetteContains(
						cleanCorpGazette, 
						CorpRelFeatureGazette.InputType.Mentioned)
			);
		
		dataSet.addFeature(
				new CorpRelFeatureGazetteContains(
						nonCorpGazette, 
						CorpRelFeatureGazette.InputType.Mentioned)
			);
		
		/* Gazette edit distance features */
		
		/*dataSet.addFeature(
				new CorpRelFeatureGazetteEditDistance(
						corpGazette, 
						CorpRelFeatureGazette.InputType.Mentioned)
			);
		*/
		dataSet.addFeature(
				new CorpRelFeatureGazetteEditDistance(
						cleanCorpGazette, 
						CorpRelFeatureGazette.InputType.Mentioned)
			);
		
		
		dataSet.addFeature(
				new CorpRelFeatureGazetteEditDistance(
						nonCorpGazette, 
						CorpRelFeatureGazette.InputType.Mentioned)
			);
		
		/* Gazette initialism features */
		
		/*dataSet.addFeature(
				new CorpRelFeatureGazetteInitialism(
						corpGazette, 
						CorpRelFeatureGazette.InputType.Mentioned, true)
			);
		*/
		dataSet.addFeature(
				new CorpRelFeatureGazetteInitialism(
						cleanCorpGazette, 
						CorpRelFeatureGazette.InputType.Mentioned, true)
			);
		
		
		dataSet.addFeature(
				new CorpRelFeatureGazetteInitialism(
						nonCorpGazette, 
						CorpRelFeatureGazette.InputType.Mentioned, true)
			);
		
		/*dataSet.addFeature(
		new CorpRelFeatureGazetteInitialism(
				corpGazette, 
				CorpRelFeatureGazette.InputType.Mentioned, false)
		);
		*/
		dataSet.addFeature(
				new CorpRelFeatureGazetteInitialism(
						cleanCorpGazette, 
						CorpRelFeatureGazette.InputType.Mentioned, false)
			);
		
		
		dataSet.addFeature(
				new CorpRelFeatureGazetteInitialism(
						nonCorpGazette, 
						CorpRelFeatureGazette.InputType.Mentioned, false)
			);
	
		/* Gazette prefix token features */
		
		/*dataSet.addFeature(
				new CorpRelFeatureGazettePrefixTokens(
						corpGazette, 
						CorpRelFeatureGazette.InputType.Mentioned,
						1)
			);
		*/
		dataSet.addFeature(
				new CorpRelFeatureGazettePrefixTokens(
						cleanCorpGazette, 
						CorpRelFeatureGazette.InputType.Mentioned,
						1)
			);
		
		dataSet.addFeature(
				new CorpRelFeatureGazettePrefixTokens(
						nonCorpGazette, 
						CorpRelFeatureGazette.InputType.Mentioned,
						1)
			);
		
		/*dataSet.addFeature(
		new CorpRelFeatureGazettePrefixTokens(
				corpGazette, 
				CorpRelFeatureGazette.InputType.Mentioned,
				2)
	);
*/
		
		dataSet.addFeature(
				new CorpRelFeatureGazettePrefixTokens(
						cleanCorpGazette, 
						CorpRelFeatureGazette.InputType.Mentioned,
						2)
			);
		
		dataSet.addFeature(
				new CorpRelFeatureGazettePrefixTokens(
						nonCorpGazette, 
						CorpRelFeatureGazette.InputType.Mentioned,
						2)
			);
		
		
		/* Self features */
		
		/*dataSet.addFeature(
				new CorpRelFeatureSelfEditDistance()
		);
		
		dataSet.addFeature(
				new CorpRelFeatureSelfInitialism(true)
		);
		
		dataSet.addFeature(
				new CorpRelFeatureSelfInitialism(false)
		);
		
		dataSet.addFeature(
				new CorpRelFeatureSelfPrefixTokens(1)
		);
		
		dataSet.addFeature(
				new CorpRelFeatureSelfPrefixTokens(2)
		);
		
		*/
		
		dataSet.addFeature(
				new CorpRelFeatureSelfEditDistance(stopWordsCleanFn)
		);
		
		dataSet.addFeature(
				new CorpRelFeatureSelfInitialism(true, stopWordsCleanFn)
		);
		
		dataSet.addFeature(
				new CorpRelFeatureSelfInitialism(false, stopWordsCleanFn)
		);
		
		dataSet.addFeature(
				new CorpRelFeatureSelfPrefixTokens(1, stopWordsCleanFn)
		);
		
		dataSet.addFeature(
				new CorpRelFeatureSelfPrefixTokens(2, stopWordsCleanFn)
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
				properties.getCrossValidationFolds(),
				new File(properties.getCregDataDirPath(), properties.getCrossValidationFolds() + "FoldCV").getAbsolutePath(),
				rand
		);
		
		validation.run(properties.getMaxThreads());
		
		System.out.println("Done.");
	}
}
