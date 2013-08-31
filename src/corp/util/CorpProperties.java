package corp.util;

import java.io.FileReader;
import java.util.Properties;

public class CorpProperties {
	private String corpRelDirPath;
	private String stanfordAnnotationDirPath;
	private boolean cacheAnnotations;
	
	public CorpProperties(String propertiesPath) {
		try {
			FileReader reader = new FileReader(propertiesPath);
			Properties properties = new Properties();
			properties.load(reader);
			
			this.corpRelDirPath = properties.getProperty("corpRelDirPath");
			this.stanfordAnnotationDirPath = properties.getProperty("stanfordAnnotationDirPath");
			this.cacheAnnotations = Boolean.valueOf(properties.getProperty("cacheAnnotations"));
			
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
	
	public boolean getCacheAnnotations() {
		return this.cacheAnnotations;
	}
}
