package corp.util;

import ark.util.ARKProperties;

/**
 * CorpProperties reads in property values from a configuration file. These
 * are used throughout the rest of the code.
 * 
 * @author Bill McDowell
 *
 */
public class CorpProperties extends ARKProperties {
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
	private String nonCorpInitialismGazetteerPath;
	private String corpMetaDataPath;
	private String bloombergMetaDataPath;
	private String experimentInputPath;
	private String experimentOutputPath;
	private String brownClustererCommandPath;
	private String brownClustererSourceDocument;
	private String latentFactionsCommandPath;
	private String latentFactionsSourceDirPath;
	private String LDASourceDirPath;
	
	public CorpProperties() {
		// FIXME: Do this differently... environment variables...? Or just add
		// your hadoop hdfs path to make it work on the Hadoop cluster in the 
		// bad way.
		super(new String[] { "corp.properties", "/user/wmcdowell/sloan/Projects/CorporateRelationExtraction/corp.properties" } );

		this.corpRelDirPath = loadProperty("corpRelDirPath");
		this.corpRelTestDirPath = loadProperty("corpRelTestDirPath");
		this.stanfordAnnotationDirPath = loadProperty("stanfordAnnotationDirPath");
		this.stanfordAnnotationCacheSize = Integer.valueOf(loadProperty("stanfordAnnotationCacheSize"));
		this.stanfordCoreMapDirPath = loadProperty("stanfordCoreMapDirPath");
		this.stanfordCoreMapCacheSize = Integer.valueOf(loadProperty("stanfordCoreMapCacheSize"));
		this.cregDataDirPath = loadProperty("cregDataDirPath");
		this.cregCommandPath = loadProperty("cregCommandPath");
		this.corpScrapedGazetteerPath = loadProperty("corpScrapedGazetteerPath");
		this.corpMetaDataGazetteerPath = loadProperty("corpMetaDataGazetteerPath");
		this.bloombergMetaDataGazetteerPath = loadProperty("bloombergMetaDataGazetteerPath");
		this.nonCorpScrapedGazetteerPath = loadProperty("nonCorpScrapedGazetteerPath");
		this.stopWordGazetteerPath = loadProperty("stopWordGazetteerPath");
		this.ngramStopWordGazetteerPath = loadProperty("ngramStopWordGazetteerPath");
		this.bloombergCorpTickerGazetteerPath = loadProperty("bloombergCorpTickerGazetteerPath");
		this.nonCorpInitialismGazetteerPath = loadProperty("nonCorpInitialismGazetteerPath");
		this.corpMetaDataPath = loadProperty("corpMetaDataPath");
		this.bloombergMetaDataPath = loadProperty("bloombergMetaDataPath");
		this.experimentInputPath = loadProperty("experimentInputPath");
		this.experimentOutputPath = loadProperty("experimentOutputPath");
		this.brownClustererCommandPath = loadProperty("brownClustererCommandPath");
		this.brownClustererSourceDocument = loadProperty("brownClustererSourceDocument");
		this.latentFactionsCommandPath = loadProperty("latentFactionsCommandPath");
		this.latentFactionsSourceDirPath = loadProperty("latentFactionsSourceDirPath");
		this.LDASourceDirPath = loadProperty("LDASourceDirPath");
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
	
	public String getNonCorpInitialismGazetteerPath() {
		return this.nonCorpInitialismGazetteerPath;
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
	
	public String getLDASourceDirectory() {
		return this.LDASourceDirPath;
	}
}
