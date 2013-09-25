package corp.scratch;
/*
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import corp.data.Gazetteer;
import corp.data.annotation.AnnotationCache;
import corp.data.annotation.CorpDocumentSet;
import corp.data.annotation.CorpRelLabel;
import corp.data.annotation.CorpRelLabelPath;
import corp.data.feature.CorpRelFeatureGazetteer;
import corp.data.feature.CorpRelFeatureGazetteerContains;
import corp.data.feature.CorpRelFeatureGazetteerEditDistance;
import corp.data.feature.CorpRelFeatureGazetteerInitialism;
import corp.data.feature.CorpRelFeatureGazetteerPrefixTokens;
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
*/
public class CRegKFoldCrossValidation {
	public static void main(String[] args) {
		/*System.out.println("Loading configuration properties...");
		
		CorpProperties properties = new CorpProperties("corp.properties");
		
		Random rand = new Random(properties.getRandomSeed());
		
		System.out.println("Loading Gazetteers...");

		Gazetteer stopWordsGazetteer = new Gazetteer("StopWords", properties.getStopWordGazetteerPath());
		StringUtil.StringTransform stopWordsCleanFn = StringUtil.getStopWordsCleanFn(stopWordsGazetteer);
		
		//Gazetteer corpGazetteer = new Gazetteer("Corp", properties.getCorpGazetteerPath());
		Gazetteer cleanCorpGazetteer = new Gazetteer("StopWordsCorp", properties.getCorpScrapedGazetteerPath(), stopWordsCleanFn);
		Gazetteer nonCorpGazetteer = new Gazetteer("NonCorp", properties.getNonCorpGazetteerPath());
		
		System.out.println("Loading Annotation Cache...");
		AnnotationCache annotationCache = new AnnotationCache(
			properties.getStanfordAnnotationDirPath(),
			properties.getStanfordAnnotationCacheSize(),
			properties.getStanfordCoreMapDirPath(),
			properties.getStanfordCoreMapCacheSize()
		);
		
		System.out.println("Loading document set...");
		
		/* This is the document set.  It represents a set of annotated documents. */
		/*CorpDocumentSet documentSet = new CorpDocumentSet(
				properties.getCorpRelDirPath(), 
				annotationCache,
				properties.getMaxThreads(),
				args.length > 0 ? Integer.parseInt(args[0]) : 0
		);
		
		System.out.println("Loaded " + documentSet.getDocuments().size() + " documents.");
		System.out.println("Constructing data set...");
		
		/* Construct corporate relation data set from documents */
		/*CorpRelFeaturizedDataSet dataSet = new CorpRelFeaturizedDataSet(documentSet);
		
		System.out.println("Adding features...");
		
		/* Add features to data set */
		//dataSet.addFeature(
		//		new CorpRelFeatureNGramSentence(
		//				1 /* n */, 
		//				2  /* minFeatureOccurrence */)
		//);
		
		/*dataSet.addFeature(
			new CorpRelFeatureNGramContext(
		/*			1 /* n *//*, 
					2 /*minFeatureOccurrence*///,
		//			0 /* contextWindowSize */)
		//);
		
		//dataSet.addFeature(
		//	new CorpRelFeatureNGramDep(
		//			1 /* n */, 
		//			2  /* minFeatureOccurrence */,
		//			CorpRelFeatureNGramDep.Mode.ParentsAndChildren /* mode */,
		//			true /* useRelationTypes */)
		//);
		
		/* Gazetteer contains features */
		
		/*dataSet.addFeature(
				new CorpRelFeatureGazetteerContains(
						stopWordsGazetteer, 
						CorpRelFeatureGazetteer.InputType.Mentioned)
			);
		
		/*dataSet.addFeature(
				new CorpRelFeatureGazetteerContains(
						corpGazetteer, 
						CorpRelFeatureGazetteer.InputType.Mentioned)
			);*/
		/*
		dataSet.addFeature(
				new CorpRelFeatureGazetteerContains(
						cleanCorpGazetteer, 
						CorpRelFeatureGazetteer.InputType.Mentioned)
			);
		
		dataSet.addFeature(
				new CorpRelFeatureGazetteerContains(
						nonCorpGazetteer, 
						CorpRelFeatureGazetteer.InputType.Mentioned)
			);
		
		/* Gazetteer edit distance features */
		
		/*dataSet.addFeature(
				new CorpRelFeatureGazetteerEditDistance(
						corpGazetteer, 
						CorpRelFeatureGazetteer.InputType.Mentioned)
			);*/
		/*
		dataSet.addFeature(
				new CorpRelFeatureGazetteerEditDistance(
						cleanCorpGazetteer, 
						CorpRelFeatureGazetteer.InputType.Mentioned)
			);
		
		
		dataSet.addFeature(
				new CorpRelFeatureGazetteerEditDistance(
						nonCorpGazetteer, 
						CorpRelFeatureGazetteer.InputType.Mentioned)
			);
		
		/* Gazetteer initialism features */
		
		/*dataSet.addFeature(
				new CorpRelFeatureGazetteerInitialism(
						corpGazetteer, 
						CorpRelFeatureGazetteer.InputType.Mentioned, true)
			);*/
		/*
		dataSet.addFeature(
				new CorpRelFeatureGazetteerInitialism(
						cleanCorpGazetteer, 
						CorpRelFeatureGazetteer.InputType.Mentioned, true)
			);
		
		
		dataSet.addFeature(
				new CorpRelFeatureGazetteerInitialism(
						nonCorpGazetteer, 
						CorpRelFeatureGazetteer.InputType.Mentioned, true)
			);
		
		/*dataSet.addFeature(
		new CorpRelFeatureGazetteerInitialism(
				corpGazetteer, 
				CorpRelFeatureGazetteer.InputType.Mentioned, false)
		);*/
		/*
		dataSet.addFeature(
				new CorpRelFeatureGazetteerInitialism(
						cleanCorpGazetteer, 
						CorpRelFeatureGazetteer.InputType.Mentioned, false)
			);
		
		
		dataSet.addFeature(
				new CorpRelFeatureGazetteerInitialism(
						nonCorpGazetteer, 
						CorpRelFeatureGazetteer.InputType.Mentioned, false)
			);
	
		/* Gazetteer prefix token features */
		
		/*dataSet.addFeature(
				new CorpRelFeatureGazetteerPrefixTokens(
						corpGazetteer, 
						CorpRelFeatureGazetteer.InputType.Mentioned,
						1)
			);*/
		/*
		dataSet.addFeature(
				new CorpRelFeatureGazetteerPrefixTokens(
						cleanCorpGazetteer, 
						CorpRelFeatureGazetteer.InputType.Mentioned,
						1)
			);
		
		dataSet.addFeature(
				new CorpRelFeatureGazetteerPrefixTokens(
						nonCorpGazetteer, 
						CorpRelFeatureGazetteer.InputType.Mentioned,
						1)
			);
		
		/*dataSet.addFeature(
		new CorpRelFeatureGazetteerPrefixTokens(
				corpGazetteer, 
				CorpRelFeatureGazetteer.InputType.Mentioned,
				2)
		);*/
		/*
		dataSet.addFeature(
				new CorpRelFeatureGazetteerPrefixTokens(
						cleanCorpGazetteer, 
						CorpRelFeatureGazetteer.InputType.Mentioned,
						2)
			);
		
		dataSet.addFeature(
				new CorpRelFeatureGazetteerPrefixTokens(
						nonCorpGazetteer, 
						CorpRelFeatureGazetteer.InputType.Mentioned,
						2)
			);
		
		
		/* Self features */
		/*
		dataSet.addFeature(
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
		
		List<CorpRelLabelPath> validLabelPaths = new ArrayList<CorpRelLabelPath>();
		validLabelPaths.add(new CorpRelLabelPath(CorpRelLabel.SelfRef));
		validLabelPaths.add(new CorpRelLabelPath(CorpRelLabel.OCorp));
		validLabelPaths.add(new CorpRelLabelPath(CorpRelLabel.NonCorp));
		validLabelPaths.add(new CorpRelLabelPath(CorpRelLabel.Generic));
		validLabelPaths.add(new CorpRelLabelPath(CorpRelLabel.Error));
		ModelCReg model = new ModelCReg(properties.getCregCommandPath(), validLabelPaths);
		
		KFoldCrossValidation validation = new KFoldCrossValidation(
				model, 
				dataSet,
				properties.getCrossValidationFolds(),
				new File(properties.getCregDataDirPath(), properties.getCrossValidationFolds() + "FoldCV").getAbsolutePath(),
				rand
		);
		
		validation.run(properties.getMaxThreads());
		
		System.out.println("Done.");*/
	}
}
