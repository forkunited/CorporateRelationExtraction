package corp.util;

import java.io.FileReader;
import java.util.Properties;

public class CorpProperties {
	private String corpRelDirPath;
	private String stanfordAnnotationDirPath;
	private int stanfordAnnotationCacheSize;
	private String stanfordCoreMapDirPath;
	private int stanfordCoreMapCacheSize;
	private String cregDataDirPath;
	private String cregCommandPath;
	private String corpGazettePath;
	private String nonCorpGazettePath;
	private int maxThreads;
	private int crossValidationFolds;
	
	public CorpProperties(String propertiesPath) {
		try {
			FileReader reader = new FileReader(propertiesPath);
			Properties properties = new Properties();
			properties.load(reader);
			
			this.corpRelDirPath = properties.getProperty("corpRelDirPath");
			this.stanfordAnnotationDirPath = properties.getProperty("stanfordAnnotationDirPath");
			this.stanfordAnnotationCacheSize = Integer.valueOf(properties.getProperty("stanfordAnnotationCacheSize"));
			this.stanfordCoreMapDirPath = properties.getProperty("stanfordCoreMapDirPath");
			this.stanfordCoreMapCacheSize = Integer.valueOf(properties.getProperty("stanfordCoreMapCacheSize"));
			this.cregDataDirPath = properties.getProperty("cregDataDirPath");
			this.cregCommandPath = properties.getProperty("cregCommandPath");
			this.corpGazettePath = properties.getProperty("corpGazettePath");
			this.nonCorpGazettePath = properties.getProperty("nonCorpGazettePath");
			this.maxThreads = Integer.valueOf(properties.getProperty("maxThreads"));
			this.crossValidationFolds = Integer.valueOf(properties.getProperty("crossValidationFolds"));
			
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getCorpRelDirPath() {
		return this.corpRelDirPath;
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
	
	public String getCorpGazettePath() {
		return this.corpGazettePath;
	}
	
	public String getNonCorpGazettePath() {
		return this.nonCorpGazettePath;
	}
	
	public int getMaxThreads() {
		return this.maxThreads;
	}
	
	public int getCrossValidationFolds() {
		return this.crossValidationFolds;
	}
}
