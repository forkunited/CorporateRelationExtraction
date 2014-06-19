package corp.data.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import corp.data.CorpDataTools;
import corp.data.CorpMetaData;
import corp.data.annotation.CorpRelDatum;
import corp.util.SerializationUtil;

/**
 * CorpRelFeature represents a feature type for use in models that 
 * classify organization mentions from the corporate press release
 * data set.  A feature type computes a vector of real values
 * for each organization mention.
 * 
 * @author Bill McDowell
 *
 */
public abstract class CorpRelFeature {
	public CorpRelFeature() {
		
	}
	
	public abstract void init(List<CorpRelDatum> data);
	public abstract void init(String initStr);
	public abstract Map<String, Double> computeMapNoInit(CorpRelDatum datum);
	public abstract List<String> getNames(List<String> existingNames);
	public abstract List<Double> computeVector(CorpRelDatum datum, List<Double> existingVector);
	public abstract CorpRelFeature clone();
	public abstract String toString(boolean withInit);
	
	public String toString() {
		return toString(false);
	}
	
	public List<String> getNames() {
		return getNames(new ArrayList<String>());
	}
	
	public List<Double> computeVector(CorpRelDatum datum) {
		return computeVector(datum, new ArrayList<Double>());
	}
	
	public List<List<Double>> computeMatrix(List<CorpRelDatum> data, List<List<Double>> existingMatrix) {
		for (int i = 0; i < data.size(); i++) {
			this.computeVector(data.get(i), existingMatrix.get(i));
		}		
		return existingMatrix;
	}
	
	public List<List<Double>> computeMatrix(List<CorpRelDatum> data) {
		List<List<Double>> existingMatrix = new ArrayList<List<Double>>();
		for (int i = 0; i < data.size(); i++)
			existingMatrix.add(new ArrayList<Double>());
		return computeMatrix(data, existingMatrix);
	}
	
	public static CorpRelFeature fromString(String featureStr, CorpDataTools dataTools) {
		int argumentsStart = featureStr.indexOf("(");
		if (argumentsStart < 0)
			throw new IllegalArgumentException("Missing or invalid feature argument string.");
		int argumentsEnd = featureStr.indexOf(")", argumentsStart);
		if (argumentsEnd <= argumentsStart)
			throw new IllegalArgumentException("Missing or invalid feature argument string.");
		String featureName = featureStr.substring(0, argumentsStart);
		String argumentsStr = featureStr.substring(argumentsStart+1, argumentsEnd);
		Map<String, String> arguments = SerializationUtil.deserializeArguments(argumentsStr);
		
		String initStr = null;
		if (argumentsEnd + 1 < featureStr.length())
			initStr = featureStr.substring(argumentsEnd + 1, featureStr.length());
		
		CorpRelFeature feature = null;
		if (featureName.equals("GazetteerContains")) {
			feature = new CorpRelFeatureGazetteerContains(
					dataTools.getGazetteer(arguments.get("gazetteer")), 
					CorpRelFeatureGazetteer.InputType.valueOf(arguments.get("inputType")));
		} else if (featureName.equals("GazetteerEditDistance")) {
			feature = new CorpRelFeatureGazetteerEditDistance(
					dataTools.getGazetteer(arguments.get("gazetteer")), 
					CorpRelFeatureGazetteer.InputType.valueOf(arguments.get("inputType")));		
		} else if (featureName.equals("GazetteerInitialism")) {
			feature = new CorpRelFeatureGazetteerInitialism(
					dataTools.getGazetteer(arguments.get("gazetteer")), 
					CorpRelFeatureGazetteer.InputType.valueOf(arguments.get("inputType")),
					Boolean.parseBoolean(arguments.get("allowPrefix")));		
		} else if (featureName.equals("GazetteerPrefixTokens")) {
			feature = new CorpRelFeatureGazetteerPrefixTokens(
					dataTools.getGazetteer(arguments.get("gazetteer")), 
					CorpRelFeatureGazetteer.InputType.valueOf(arguments.get("inputType")),
					Integer.parseInt(arguments.get("minTokens")));
		} else if (featureName.equals("LDA")) {
			feature = new CorpRelFeatureLDA(
					dataTools.getLDA(arguments.get("lda")),
					dataTools.getCleanFn(arguments.get("corpKeyFn")),
					dataTools.getCleanFn(arguments.get("cleanFn")));
		} else if (featureName.equals("MetaDataAttribute")) {
			feature = new CorpRelFeatureMetaDataAttribute(
					dataTools.getGazetteer(arguments.get("gazetteer")), 
					dataTools.getMetaData(arguments.get("metaData")),
					CorpMetaData.Attribute.valueOf(arguments.get("attribute")),
					CorpRelFeatureMetaDataAttribute.InputType.valueOf(arguments.get("inputType")),
					Integer.parseInt(arguments.get("minFeatureOccurrence")),
					dataTools.getCollectionFn(arguments.get("attributeTransformFn")));	
		} else if (featureName.equals("NGramContext")) {
			feature = new CorpRelFeatureNGramContext(
					Integer.valueOf(arguments.get("n")),
					Integer.valueOf(arguments.get("minFeatureOccurrence")),
					Integer.valueOf(arguments.get("contextWindowSize")),
					dataTools.getCleanFn(arguments.get("cleanFn")),
					dataTools.getClusterer(arguments.get("clusterer")));
		} else if (featureName.equals("NGramDep")) {
			feature = new CorpRelFeatureNGramDep(
					Integer.valueOf(arguments.get("n")), 
					Integer.valueOf(arguments.get("minFeatureOccurrence")),
					CorpRelFeatureNGramDep.Mode.valueOf(arguments.get("mode")),
					Boolean.valueOf(arguments.get("useRelationTypes")),
					dataTools.getCleanFn(arguments.get("cleanFn")),
					dataTools.getClusterer(arguments.get("clusterer")));
		} else if (featureName.equals("NGramSentence")) {
			feature = new CorpRelFeatureNGramSentence(
					Integer.valueOf(arguments.get("n")),
					Integer.valueOf(arguments.get("minFeatureOccurrence")),
					dataTools.getCleanFn(arguments.get("cleanFn")),
					dataTools.getClusterer(arguments.get("clusterer")));
		} else if (featureName.equals("SelfEditDistance")) {
			feature = new CorpRelFeatureSelfEditDistance(dataTools.getCleanFn(arguments.get("cleanFn")));
		} else if (featureName.equals("SelfEquality")) {
			feature = new CorpRelFeatureSelfEquality(dataTools.getCleanFn(arguments.get("cleanFn")));
		} else if (featureName.equals("SelfInitialism")) {
			feature = new CorpRelFeatureSelfInitialism(
					Boolean.valueOf(arguments.get("allowPrefix")),
					dataTools.getCleanFn(arguments.get("cleanFn")));
		} else if (featureName.equals("SelfPrefixTokens")) {
			feature = new CorpRelFeatureSelfPrefixTokens(
					Integer.valueOf(arguments.get("minTokens")),
					dataTools.getCleanFn(arguments.get("cleanFn")));
		} else if (featureName.equals("SelfShareGazetteerId")) {
			feature = new CorpRelFeatureSelfShareGazetteerId(
					dataTools.getGazetteer(arguments.get("gazetteer")),
					dataTools.getCleanFn(arguments.get("cleanFn")));
		} else {
			throw new IllegalArgumentException("Invalid feature name: " + featureName);
		}
		
		if (initStr != null)
			feature.init(initStr);
		
		return feature;
	}
}
