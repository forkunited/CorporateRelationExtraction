package corp.experiment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import corp.data.CorpMetaData;
import corp.data.Gazetteer;
import corp.data.annotation.AnnotationCache;
import corp.data.annotation.CorpDocumentSet;
import corp.data.feature.CorpRelFeature;
import corp.data.feature.CorpRelFeatureGazetteer;
import corp.data.feature.CorpRelFeatureGazetteerContains;
import corp.data.feature.CorpRelFeatureGazetteerEditDistance;
import corp.data.feature.CorpRelFeatureGazetteerInitialism;
import corp.data.feature.CorpRelFeatureGazetteerPrefixTokens;
import corp.data.feature.CorpRelFeatureMetaDataAttribute;
import corp.data.feature.CorpRelFeatureNGramContext;
import corp.data.feature.CorpRelFeatureNGramDep;
import corp.data.feature.CorpRelFeatureNGramSentence;
import corp.data.feature.CorpRelFeatureSelfEditDistance;
import corp.data.feature.CorpRelFeatureSelfInitialism;
import corp.data.feature.CorpRelFeatureSelfPrefixTokens;
import corp.data.feature.CorpRelFeatureSelfShareGazetteerId;
import corp.data.feature.CorpRelFeaturizedDataSet;
import corp.util.CorpProperties;
import corp.util.OutputWriter;
import corp.util.StringUtil;
import edu.stanford.nlp.util.Pair;

public abstract class Experiment {
	protected OutputWriter output;
	
	protected CorpProperties properties;
	protected int maxThreads;
	protected int maxDocuments;
	protected Random rand;
	
	protected Map<String, Gazetteer> gazetteers;
	protected Map<String, CorpMetaData> metaData;
	protected Map<String, StringUtil.StringTransform> cleanFns;
	
	protected AnnotationCache annotationCache;
	protected CorpRelFeaturizedDataSet dataSet;
	
	public void run(String name) {
		this.properties = new CorpProperties("corp.properties");
		
		List<String> inputLines = readInputFile(name);
		parseInitial(inputLines);
		initialize(name);
		parse(inputLines);
		execute(name);
		finish();	
	}
	
	protected void initialize(String name) {		
		this.output = new OutputWriter(
			new File(this.properties.getExperimentOutputPath(), name + ".debug.out"),
			new File(this.properties.getExperimentOutputPath(), name + ".results.out"),
			new File(this.properties.getExperimentOutputPath(), name + ".data.out")
		);
		
		this.output.debugWriteln("Loading Gazetteers...");

		this.cleanFns = new HashMap<String, StringUtil.StringTransform>();
		this.gazetteers = new HashMap<String, Gazetteer>();
		this.cleanFns.put("DefaultCleanFn", StringUtil.getDefaultCleanFn());
		
		this.gazetteers.put("StopWord", new Gazetteer("StopWord", properties.getStopWordGazetteerPath()));
		this.cleanFns.put("StopWordCleanFn", StringUtil.getStopWordsCleanFn(this.gazetteers.get("StopWord")));
		
		this.gazetteers.put("StopWordGazetteer", new Gazetteer("StopWord", this.properties.getStopWordGazetteerPath()));
		this.gazetteers.put("CorpScrapedGazetteer", new Gazetteer("CorpScraped", this.properties.getCorpScrapedGazetteerPath()));
		this.gazetteers.put("CorpMetaDataGazetteer", new Gazetteer("CorpMetaData", this.properties.getCorpMetaDataGazetteerPath()));
		this.gazetteers.put("StopWordCorpScrapedGazetteer", new Gazetteer("StopWordCorpScraped", this.properties.getCorpScrapedGazetteerPath(), this.cleanFns.get("StopWordCleanFn")));
		this.gazetteers.put("NonCorpScrapedGazetteer", new Gazetteer("NonCorpScraped", this.properties.getNonCorpScrapedGazetteerPath()));
		
		this.output.debugWriteln("Loading Meta Data");
		
		this.metaData = new HashMap<String, CorpMetaData>();
		this.metaData.put("CorpMetaData", new CorpMetaData("Corp", this.properties.getCorpMetaDataPath()));
		
		this.output.debugWriteln("Loading Annotation Cache...");
		this.annotationCache = new AnnotationCache(
			this.properties.getStanfordAnnotationDirPath(),
			this.properties.getStanfordAnnotationCacheSize(),
			this.properties.getStanfordCoreMapDirPath(),
			this.properties.getStanfordCoreMapCacheSize(),
			this.output
		);
		
		this.output.debugWriteln("Loading document set...");
		CorpDocumentSet documentSet = new CorpDocumentSet(
				this.properties.getCorpRelDirPath(), 
				this.annotationCache,
				this.maxThreads,
				this.maxDocuments,
				this.output
		);
		
		this.output.debugWriteln("Loaded " + documentSet.getDocuments().size() + " documents.");
		this.output.debugWriteln("Constructing data set...");
		
		this.dataSet = new CorpRelFeaturizedDataSet(documentSet, this.output);
	}
	
	protected List<String> readInputFile(String name) {
		List<String> lines = new ArrayList<String>();
		try {
			String inputPath = new File(this.properties.getExperimentInputPath(), name + ".experiment").getAbsolutePath();
			BufferedReader br = new BufferedReader(new FileReader(inputPath));
			String line = null;
			while ((line = br.readLine()) != null) {
				lines.add(line);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return lines;
	}
	
	protected void finish() {
		this.output.close();
	}
	
	protected void parseInitial(List<String> lines) {
		for (String line : lines) {
			Pair<String, String> assignment = parseAssignment(line);
			
			if (assignment == null)
				continue;
			else if (assignment.first().equals("randomSeed"))
				this.rand = new Random(Integer.parseInt(assignment.second()));
			else if (assignment.first().equals("maxThreads"))
				this.maxThreads = Integer.parseInt(assignment.second());
			else if (assignment.first().equals("maxDocuments"))
				this.maxDocuments = Integer.parseInt(assignment.second());
		}
	}
	
	protected CorpRelFeature parseFeature(String featureStr) {
		int argumentsStart = featureStr.indexOf("(");
		if (argumentsStart < 0)
			throw new IllegalArgumentException("Missing or invalid feature argument string.");
		int argumentsEnd = featureStr.indexOf(")", argumentsStart);
		if (argumentsEnd <= argumentsStart)
			throw new IllegalArgumentException("Missing or invalid feature argument string.");
		String featureName = featureStr.substring(0, argumentsStart);
		String argumentsStr = featureStr.substring(argumentsStart+1, argumentsEnd);
		Map<String, String> arguments = parseArguments(argumentsStr);
		
		CorpRelFeature feature = null;
		if (featureName.equals("GazetteerContains")) {
			feature = new CorpRelFeatureGazetteerContains(
					this.gazetteers.get(arguments.get("gazetteer")), 
					CorpRelFeatureGazetteer.InputType.valueOf(arguments.get("inputType")));
		} else if (featureName.equals("GazetteerEditDistance")) {
			feature = new CorpRelFeatureGazetteerEditDistance(
					this.gazetteers.get(arguments.get("gazetteer")), 
					CorpRelFeatureGazetteer.InputType.valueOf(arguments.get("inputType")));		
		} else if (featureName.equals("GazetteerInitialism")) {
			feature = new CorpRelFeatureGazetteerInitialism(
					this.gazetteers.get(arguments.get("gazetteer")), 
					CorpRelFeatureGazetteer.InputType.valueOf(arguments.get("inputType")),
					Boolean.parseBoolean(arguments.get("allowPrefix")));		
		} else if (featureName.equals("GazetteerPrefixTokens")) {
			feature = new CorpRelFeatureGazetteerPrefixTokens(
					this.gazetteers.get(arguments.get("gazetteer")), 
					CorpRelFeatureGazetteer.InputType.valueOf(arguments.get("inputType")),
					Integer.parseInt(arguments.get("minTokens")));				
		} else if (featureName.equals("MetaDataAttribute")) {
			feature = new CorpRelFeatureMetaDataAttribute(
					this.gazetteers.get(arguments.get("gazetteer")), 
					this.metaData.get(arguments.get("metaData")),
					CorpMetaData.Attribute.valueOf(arguments.get("attribute")),
					CorpRelFeatureMetaDataAttribute.InputType.valueOf(arguments.get("inputType")),
					Integer.parseInt(arguments.get("minFeatureOccurrence")));	
		} else if (featureName.equals("NGramContext")) {
			feature = new CorpRelFeatureNGramContext(
					Integer.valueOf(arguments.get("n")),
					Integer.valueOf(arguments.get("minFeatureOccurrence")),
					Integer.valueOf(arguments.get("contextWindowSize")));
		} else if (featureName.equals("NGramDep")) {
			feature = new CorpRelFeatureNGramDep(
					Integer.valueOf(arguments.get("n")), 
					Integer.valueOf(arguments.get("minFeatureOccurrence")),
					CorpRelFeatureNGramDep.Mode.valueOf(arguments.get("mode")),
					Boolean.valueOf(arguments.get("useRelationTypes")));
		} else if (featureName.equals("NGramSentence")) {
			feature = new CorpRelFeatureNGramSentence(
					Integer.valueOf(arguments.get("n")),
					Integer.valueOf(arguments.get("minFeatureOccurrence")));
		} else if (featureName.equals("SelfEditDistance")) {
			feature = new CorpRelFeatureSelfEditDistance(this.cleanFns.get(arguments.get("cleanFn")));
		} else if (featureName.equals("SelfInitialism")) {
			feature = new CorpRelFeatureSelfInitialism(
					Boolean.valueOf(arguments.get("allowPrefix")),
					this.cleanFns.get(arguments.get("cleanFn")));
		} else if (featureName.equals("SelfPrefixTokens")) {
			feature = new CorpRelFeatureSelfPrefixTokens(
					Integer.valueOf(arguments.get("minTokens")),
					this.cleanFns.get(arguments.get("cleanFn")));
		} else if (featureName.equals("SelfShareGazetteerId")) {
			feature = new CorpRelFeatureSelfShareGazetteerId(
					this.gazetteers.get(arguments.get("gazetteer")),
					this.cleanFns.get(arguments.get("cleanFn")));
		} else {
			throw new IllegalArgumentException("Invlalid feature name: " + featureName);
		}
		
		return feature;
	}
	
	protected Map<String, String> parseArguments(String argumentsStr) {
		String[] argumentStrs = argumentsStr.split(",");
		Map<String, String> arguments = new HashMap<String, String>();
		for (String argumentStr : argumentStrs) {
			Pair<String, String> assignment = parseAssignment(argumentStr);
			if (assignment == null)
				continue;
			arguments.put(assignment.first(), assignment.second());
		}
		return arguments;
	}
	
	protected Pair<String, String> parseAssignment(String assignmentStr) {
		int equalsIndex = assignmentStr.indexOf("=");
		if (!(equalsIndex >= 1 && equalsIndex < assignmentStr.length()))
			return null;
		
		String first = assignmentStr.substring(0, equalsIndex).trim();
		String second = null;
		if (equalsIndex == assignmentStr.length() - 1)
			second = "";
		else
			second = assignmentStr.substring(equalsIndex + 1).trim();
		
		if (!first.matches("[A-Za-z0-9]+"))
			return null;
		
		return new Pair<String, String>(first, second);
	}
	
	protected abstract void parse(List<String> lines);
	protected abstract void execute(String name);
}
