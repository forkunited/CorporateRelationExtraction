package corp.util;

import java.io.FileReader;
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
	
	public CorpProperties(String propertiesPath) {
		try {
			FileReader reader = new FileReader(propertiesPath);
			Properties properties = new Properties();
			properties.load(reader);
			
			this.corpRelDirPath = properties.getProperty("corpRelDirPath");
			this.corpRelTestDirPath = properties.getProperty("corpRelTestDirPath");
			this.stanfordAnnotationDirPath = properties.getProperty("stanfordAnnotationDirPath");
			this.stanfordAnnotationCacheSize = Integer.valueOf(properties.getProperty("stanfordAnnotationCacheSize"));
			this.stanfordCoreMapDirPath = properties.getProperty("stanfordCoreMapDirPath");
			this.stanfordCoreMapCacheSize = Integer.valueOf(properties.getProperty("stanfordCoreMapCacheSize"));
			this.cregDataDirPath = properties.getProperty("cregDataDirPath");
			this.cregCommandPath = properties.getProperty("cregCommandPath");
			this.corpScrapedGazetteerPath = properties.getProperty("corpScrapedGazetteerPath");
			this.corpMetaDataGazetteerPath = properties.getProperty("corpMetaDataGazetteerPath");
			this.bloombergMetaDataGazetteerPath = properties.getProperty("bloombergMetaDataGazetteerPath");
			this.nonCorpScrapedGazetteerPath = properties.getProperty("nonCorpScrapedGazetteerPath");
			this.stopWordGazetteerPath = properties.getProperty("stopWordGazetteerPath");
			this.ngramStopWordGazetteerPath = properties.getProperty("ngramStopWordGazetteerPath");
			this.bloombergCorpTickerGazetteerPath = properties.getProperty("bloombergCorpTickerGazetteerPath");
			this.corpMetaDataPath = properties.getProperty("corpMetaDataPath");
			this.bloombergMetaDataPath = properties.getProperty("bloombergMetaDataPath");
			this.experimentInputPath = properties.getProperty("experimentInputPath");
			this.experimentOutputPath = properties.getProperty("experimentOutputPath");
			this.brownClustererCommandPath = properties.getProperty("brownClustererCommandPath");
			this.brownClustererSourceDocument = properties.getProperty("brownClustererSourceDocument");
			
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
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
}
