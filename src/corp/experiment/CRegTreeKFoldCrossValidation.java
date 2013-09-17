package corp.experiment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import corp.data.Gazette;
import corp.data.annotation.AnnotationCache;
import corp.data.annotation.CorpDocumentSet;
import corp.data.annotation.CorpRelLabel;
import corp.data.annotation.CorpRelLabelPath;
import corp.data.feature.CorpRelFeature;
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
import corp.model.ModelTree;
import corp.model.evaluation.KFoldCrossValidation;
import corp.util.CorpProperties;
import corp.util.StringUtil;

public class CRegTreeKFoldCrossValidation {
	public static void main(String[] args) {
		System.out.println("Loading configuration properties...");
		
		CorpProperties properties = new CorpProperties("corp.properties");
		
		Random rand = new Random(properties.getRandomSeed());
		
		System.out.println("Loading Gazettes...");

		Gazette stopWordsGazette = new Gazette("StopWords", properties.getStopWordGazettePath());
		StringUtil.StringTransform stopWordsCleanFn = StringUtil.getStopWordsCleanFn(stopWordsGazette);
		
		Gazette corpGazette = new Gazette("Corp", properties.getCorpGazettePath());
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
				args.length > 0 ? Integer.parseInt(args[0]) : 3
		);
		
		System.out.println("Loaded " + documentSet.getDocuments().size() + " documents.");
		
		System.out.println("Constructing data set...");
		
		/* Construct corporate relation data set from documents */
		CorpRelFeaturizedDataSet dataSet = new CorpRelFeaturizedDataSet(documentSet);
		
		System.out.println("Constructing features...");

		/* Construct features */
		CorpRelFeature oneGramSentence = new CorpRelFeatureNGramSentence(1 /* n */,  2  /* minFeatureOccurrence */);
		CorpRelFeature oneGramContext = new CorpRelFeatureNGramContext(1 /* n */, 2 /* minFeatureOccurrence */, 0 /* contextWindowSize */);
		CorpRelFeature oneGramDep = new CorpRelFeatureNGramDep(1 /* n */, 2  /* minFeatureOccurrence */, CorpRelFeatureNGramDep.Mode.ParentsAndChildren /* mode */,true /* useRelationTypes */);
		CorpRelFeature stopWordsContains = new CorpRelFeatureGazetteContains(stopWordsGazette, CorpRelFeatureGazette.InputType.Mentioned);
		CorpRelFeature corpGazetteContains = new CorpRelFeatureGazetteContains(corpGazette, CorpRelFeatureGazette.InputType.Mentioned);
		CorpRelFeature cleanCorpGazetteContains = new CorpRelFeatureGazetteContains(cleanCorpGazette, CorpRelFeatureGazette.InputType.Mentioned);
		CorpRelFeature nonCorpGazetteContains = new CorpRelFeatureGazetteContains(nonCorpGazette, CorpRelFeatureGazette.InputType.Mentioned);
		CorpRelFeature corpGazetteDistance = new CorpRelFeatureGazetteEditDistance(corpGazette, CorpRelFeatureGazette.InputType.Mentioned);
		CorpRelFeature cleanCorpGazetteDistance = new CorpRelFeatureGazetteEditDistance(cleanCorpGazette, CorpRelFeatureGazette.InputType.Mentioned);
		CorpRelFeature nonCorpGazetteDistance = new CorpRelFeatureGazetteEditDistance(nonCorpGazette, CorpRelFeatureGazette.InputType.Mentioned);
		CorpRelFeature corpGazetteInitialismPrefix = new CorpRelFeatureGazetteInitialism(corpGazette, CorpRelFeatureGazette.InputType.Mentioned, true);
		CorpRelFeature cleanCorpGazetteInitialismPrefix = new CorpRelFeatureGazetteInitialism(cleanCorpGazette, CorpRelFeatureGazette.InputType.Mentioned, true);
		CorpRelFeature nonCorpGazetteInitialismPrefix = new CorpRelFeatureGazetteInitialism(nonCorpGazette, CorpRelFeatureGazette.InputType.Mentioned, true);
		CorpRelFeature corpGazetteInitialism = new CorpRelFeatureGazetteInitialism(corpGazette, CorpRelFeatureGazette.InputType.Mentioned, false);
		CorpRelFeature cleanCorpGazetteInitialism = new CorpRelFeatureGazetteInitialism(cleanCorpGazette, CorpRelFeatureGazette.InputType.Mentioned, false);
		CorpRelFeature nonCorpGazetteInitialism = new CorpRelFeatureGazetteInitialism(nonCorpGazette, CorpRelFeatureGazette.InputType.Mentioned, false);
		CorpRelFeature corpGazettePrefixTokensMin1 = new CorpRelFeatureGazettePrefixTokens(corpGazette, CorpRelFeatureGazette.InputType.Mentioned, 1);
		CorpRelFeature cleanCorpGazettePrefixTokensMin1 = new CorpRelFeatureGazettePrefixTokens(cleanCorpGazette, CorpRelFeatureGazette.InputType.Mentioned,1);
		CorpRelFeature nonCorpGazettePrefixTokensMin1 = new CorpRelFeatureGazettePrefixTokens(nonCorpGazette, CorpRelFeatureGazette.InputType.Mentioned, 1);
		CorpRelFeature corpGazettePrefixTokensMin2 = new CorpRelFeatureGazettePrefixTokens(corpGazette, CorpRelFeatureGazette.InputType.Mentioned, 2);
		CorpRelFeature cleanCorpGazettePrefixTokensMin2 = new CorpRelFeatureGazettePrefixTokens(cleanCorpGazette, CorpRelFeatureGazette.InputType.Mentioned,2);
		CorpRelFeature nonCorpGazettePrefixTokensMin2 = new CorpRelFeatureGazettePrefixTokens(nonCorpGazette, CorpRelFeatureGazette.InputType.Mentioned, 2);
		CorpRelFeature selfDistance = new CorpRelFeatureSelfEditDistance();
		CorpRelFeature selfInitialismPrefix = new CorpRelFeatureSelfInitialism(true);
		CorpRelFeature selfInitialism = new CorpRelFeatureSelfInitialism(false);
		CorpRelFeature selfPrefixTokensMin1 = new CorpRelFeatureSelfPrefixTokens(1);
		CorpRelFeature selfPrefixTokensMin2 = new CorpRelFeatureSelfPrefixTokens(2);
		CorpRelFeature selfDistanceClean = new CorpRelFeatureSelfEditDistance(stopWordsCleanFn);
		CorpRelFeature selfInitialismPrefixClean = new CorpRelFeatureSelfInitialism(true, stopWordsCleanFn);
		CorpRelFeature selfInitialismClean = new CorpRelFeatureSelfInitialism(false, stopWordsCleanFn);
		CorpRelFeature selfPrefixTokensMin1Clean = new CorpRelFeatureSelfPrefixTokens(1, stopWordsCleanFn);
		CorpRelFeature selfPrefixTokensMin2Clean = new CorpRelFeatureSelfPrefixTokens(2, stopWordsCleanFn);
		
		System.out.println("Running CReg Cross Validation on tree model...");
		
		List<CorpRelLabelPath> validRootLabelPaths = new ArrayList<CorpRelLabelPath>();
		validRootLabelPaths.add(new CorpRelLabelPath(CorpRelLabel.SelfRef));
		validRootLabelPaths.add(new CorpRelLabelPath(CorpRelLabel.OCorp));
		validRootLabelPaths.add(new CorpRelLabelPath(CorpRelLabel.NonCorp));
		validRootLabelPaths.add(new CorpRelLabelPath(CorpRelLabel.Generic));
		validRootLabelPaths.add(new CorpRelLabelPath(CorpRelLabel.Error));
		
		List<CorpRelFeature> rootModelFeatures = new ArrayList<CorpRelFeature>();
		//rootModelFeatures.add(oneGramSentence);
		rootModelFeatures.add(oneGramContext);
		//rootModelFeatures.add(oneGramDep);
		rootModelFeatures.add(stopWordsContains);
		rootModelFeatures.add(corpGazetteContains);
		//rootModelFeatures.add(cleanCorpGazetteContains);
		rootModelFeatures.add(nonCorpGazetteContains);
		rootModelFeatures.add(corpGazetteDistance);
		//rootModelFeatures.add(cleanCorpGazetteDistance);
		rootModelFeatures.add(nonCorpGazetteDistance);
		rootModelFeatures.add(corpGazetteInitialismPrefix);
		//rootModelFeatures.add(cleanCorpGazetteInitialismPrefix);
		rootModelFeatures.add(nonCorpGazetteInitialismPrefix);
		rootModelFeatures.add(corpGazetteInitialism); 
		//rootModelFeatures.add(cleanCorpGazetteInitialism); 
		rootModelFeatures.add(nonCorpGazetteInitialism);
		rootModelFeatures.add(corpGazettePrefixTokensMin1);
		//rootModelFeatures.add(cleanCorpGazettePrefixTokensMin1);
		rootModelFeatures.add(nonCorpGazettePrefixTokensMin1);
		//rootModelFeatures.add(corpGazettePrefixTokensMin2);
		//rootModelFeatures.add(cleanCorpGazettePrefixTokensMin2);
		//rootModelFeatures.add(nonCorpGazettePrefixTokensMin2);
		rootModelFeatures.add(selfDistance);
		rootModelFeatures.add(selfInitialismPrefix);
		rootModelFeatures.add(selfInitialism);
		rootModelFeatures.add(selfPrefixTokensMin1);
		//rootModelFeatures.add(selfPrefixTokensMin2);
		//rootModelFeatures.add(selfDistanceClean); 
		//rootModelFeatures.add(selfInitialismPrefixClean);
		//rootModelFeatures.add(selfInitialismClean);
		//rootModelFeatures.add(selfPrefixTokensMin1Clean);
		//rootModelFeatures.add(selfPrefixTokensMin2Clean); 
		
		ModelCReg rootModel = new ModelCReg(properties.getCregCommandPath(), validRootLabelPaths);
		
		List<CorpRelLabelPath> validOCorpLabelPaths = new ArrayList<CorpRelLabelPath>();
		validOCorpLabelPaths.add(new CorpRelLabelPath(new CorpRelLabel[] { CorpRelLabel.OCorp, CorpRelLabel.Family }));
		validOCorpLabelPaths.add(new CorpRelLabelPath(new CorpRelLabel[] { CorpRelLabel.OCorp, CorpRelLabel.Merger }));
		validOCorpLabelPaths.add(new CorpRelLabelPath(new CorpRelLabel[] { CorpRelLabel.OCorp, CorpRelLabel.Legal }));
		validOCorpLabelPaths.add(new CorpRelLabelPath(new CorpRelLabel[] { CorpRelLabel.OCorp, CorpRelLabel.Partner }));
		validOCorpLabelPaths.add(new CorpRelLabelPath(new CorpRelLabel[] { CorpRelLabel.OCorp, CorpRelLabel.NewHire }));
		validOCorpLabelPaths.add(new CorpRelLabelPath(new CorpRelLabel[] { CorpRelLabel.OCorp, CorpRelLabel.Cust }));
		validOCorpLabelPaths.add(new CorpRelLabelPath(new CorpRelLabel[] { CorpRelLabel.OCorp, CorpRelLabel.Suply }));
		validOCorpLabelPaths.add(new CorpRelLabelPath(new CorpRelLabel[] { CorpRelLabel.OCorp, CorpRelLabel.Compete }));
		validOCorpLabelPaths.add(new CorpRelLabelPath(new CorpRelLabel[] { CorpRelLabel.OCorp, CorpRelLabel.News }));
		validOCorpLabelPaths.add(new CorpRelLabelPath(new CorpRelLabel[] { CorpRelLabel.OCorp, CorpRelLabel.Finance }));
		validOCorpLabelPaths.add(new CorpRelLabelPath(new CorpRelLabel[] { CorpRelLabel.OCorp, CorpRelLabel.New }));

		List<CorpRelFeature> oCorpModelFeatures = new ArrayList<CorpRelFeature>();
		oCorpModelFeatures.add(oneGramSentence);
		oCorpModelFeatures.add(oneGramContext);
		oCorpModelFeatures.add(oneGramDep);
		oCorpModelFeatures.add(selfDistance);
		oCorpModelFeatures.add(selfInitialismPrefix);
		oCorpModelFeatures.add(selfInitialism);
		oCorpModelFeatures.add(selfPrefixTokensMin1);
		//oCorpModelFeatures.add(selfPrefixTokensMin2);
		//oCorpModelFeatures.add(selfDistanceClean); 
		//oCorpModelFeatures.add(selfInitialismPrefixClean);
		//oCorpModelFeatures.add(selfInitialismClean);
		//oCorpModelFeatures.add(selfPrefixTokensMin1Clean);
		//oCorpModelFeatures.add(selfPrefixTokensMin2Clean); 
		
		ModelCReg oCorpModel = new ModelCReg(properties.getCregCommandPath(), validOCorpLabelPaths);
		
		ModelTree treeModel = new ModelTree(false);
		treeModel.addModel(new CorpRelLabelPath(), rootModel, rootModelFeatures);
		treeModel.addModel(new CorpRelLabelPath(CorpRelLabel.OCorp), oCorpModel, oCorpModelFeatures);
		
		KFoldCrossValidation validation = new KFoldCrossValidation(
				treeModel, 
				dataSet,
				properties.getCrossValidationFolds(),
				new File(properties.getCregDataDirPath(), properties.getCrossValidationFolds() + "FoldCV").getAbsolutePath(),
				rand
		);
		
		validation.run(properties.getMaxThreads());
		
		System.out.println("Done.");
	}
}
