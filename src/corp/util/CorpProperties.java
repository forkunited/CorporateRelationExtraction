package corp.util;

import java.io.FileReader;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

public class CorpProperties {
	private String corpRelDirPath;
	private String corpRelTestDirPath;
	private String stanfordAnnotationDirPath;
	private int stanfordAnnotationCacheSize;
	private String stanfordCoreMapDirPath;
	private int stanfordCoreMapCacheSize;
	private String cregDataDirPath;
	private String cregCommandPath;
	private String corpScrapedGazetteerPath;
	private String corpMetaDataGazetteerPath;
	private String bloombergMetaDataGazetteerPath;
	private String nonCorpScrapedGazetteerPath;
	private String stopWordGazetteerPath;
	private String ngramStopWordGazetteerPath;
	private String bloombergCorpTickerGazetteerPath;
	private String corpMetaDataPath;
	private String bloombergMetaDataPath;
	private String experimentInputPath;
	private String experimentOutputPath;
	private String brownClustererCommandPath;
	private String brownClustererSourceDocument;
	private String latentFactionsCommandPath;
	private String latentFactionsSourceDirPath;
	
	public CorpProperties(String propertiesPath) {
		try {
			FileReader reader = new FileReader(propertiesPath);
			Properties properties = new Properties();
			properties.load(reader);
			Map<String, String> env = System.getenv();
			
			this.corpRelDirPath = loadProperty(env, properties, "corpRelDirPath");
			this.corpRelTestDirPath = loadProperty(env, properties, "corpRelTestDirPath");
			this.stanfordAnnotationDirPath = loadProperty(env, properties, "stanfordAnnotationDirPath");
			this.stanfordAnnotationCacheSize = Integer.valueOf(loadProperty(env, properties, "stanfordAnnotationCacheSize"));
			this.stanfordCoreMapDirPath = loadProperty(env, properties, "stanfordCoreMapDirPath");
			this.stanfordCoreMapCacheSize = Integer.valueOf(loadProperty(env, properties, "stanfordCoreMapCacheSize"));
			this.cregDataDirPath = loadProperty(env, properties, "cregDataDirPath");
			this.cregCommandPath = loadProperty(env, properties, "cregCommandPath");
			this.corpScrapedGazetteerPath = loadProperty(env, properties, "corpScrapedGazetteerPath");
			this.corpMetaDataGazetteerPath = loadProperty(env, properties, "corpMetaDataGazetteerPath");
			this.bloombergMetaDataGazetteerPath = loadProperty(env, properties, "bloombergMetaDataGazetteerPath");
			this.nonCorpScrapedGazetteerPath = loadProperty(env, properties, "nonCorpScrapedGazetteerPath");
			this.stopWordGazetteerPath = loadProperty(env, properties, "stopWordGazetteerPath");
			this.ngramStopWordGazetteerPath = loadProperty(env, properties, "ngramStopWordGazetteerPath");
			this.bloombergCorpTickerGazetteerPath = loadProperty(env, properties, "bloombergCorpTickerGazetteerPath");
			this.corpMetaDataPath = loadProperty(env, properties, "corpMetaDataPath");
			this.bloombergMetaDataPath = loadProperty(env, properties, "bloombergMetaDataPath");
			this.experimentInputPath = loadProperty(env, properties, "experimentInputPath");
			this.experimentOutputPath = loadProperty(env, properties, "experimentOutputPath");
			this.brownClustererCommandPath = loadProperty(env, properties, "brownClustererCommandPath");
			this.brownClustererSourceDocument = loadProperty(env, properties, "brownClustererSourceDocument");
			this.latentFactionsCommandPath = loadProperty(env, properties, "latentFactionsCommandPath");
			this.latentFactionsSourceDirPath = loadProperty(env, properties, "latentFactionsSourceDirPath");
			
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private String loadProperty(Map<String, String> env, Properties properties, String property) {
		String propertyValue = properties.getProperty(property);
		for (Entry<String, String> envEntry : env.entrySet())
			propertyValue = propertyValue.replace("${" + envEntry.getKey() + "}", envEntry.getValue());
		return propertyValue;
	}
	
	public String getCorpRelDirPath() {
		return this.corpRelDirPath;
	}
	
	public String getCorpRelTestDirPath() {
		return this.corpRelTestDirPath;
	}
	
	public String getStanfordAnnotationDirPath() {
		return this.stanfordAnnotationDirPath;
	}
	
	public int getStanfordAnnotationCacheSize() {
		return this.stanfordAnnotationCacheSize;
	}
	
	public String getStanfordCoreMapDirPath() {
		return this.stanfordCoreMapDirPath;
	}
	
	public int getStanfordCoreMapCacheSize() {
		return this.stanfordCoreMapCacheSize;
	}
	
	public String getCregDataDirPath() {
		return this.cregDataDirPath;
	}
	
	public String getCregCommandPath() {
		return this.cregCommandPath;
	}
	
	public String getCorpScrapedGazetteerPath() {
		return this.corpScrapedGazetteerPath;
	}
	
	public String getCorpMetaDataGazetteerPath() {
		return this.corpMetaDataGazetteerPath;
	}
	
	public String getBloombergMetaDataGazetteerPath() {
		return this.bloombergMetaDataGazetteerPath;
	}
	
	public String getNonCorpScrapedGazetteerPath() {
		return this.nonCorpScrapedGazetteerPath;
	}
	
	public String getStopWordGazetteerPath() {
		return this.stopWordGazetteerPath;
	}
	
	public String getNGramStopWordGazetteerPath() {
		return this.ngramStopWordGazetteerPath;
	}
	
	public String getBloombergCorpTickerGazetteerPath() {
		return this.bloombergCorpTickerGazetteerPath;
	}
	
	public String getCorpMetaDataPath() {
		return this.corpMetaDataPath;
	}
	
	public String getBloombergMetaDataPath() {
		return this.bloombergMetaDataPath;
	}

	public String getExperimentOutputPath() {
		return this.experimentOutputPath;
	}
	
	public String getExperimentInputPath() {
		return this.experimentInputPath;
	}
	
	public String getBrownClustererCommandPath() {
		return this.brownClustererCommandPath;
	}
	
	public String getBrownClustererSourceDocument() {
		return this.brownClustererSourceDocument;
	}
	
	public String getLatentFactionsCommandPath() {
		return this.latentFactionsCommandPath;
	}
	
	public String getLatentFactionsSourceDirectory() {
		return this.latentFactionsSourceDirPath;
	}
}
