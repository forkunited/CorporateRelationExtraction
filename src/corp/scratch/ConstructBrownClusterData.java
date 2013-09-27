package corp.scratch;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import corp.data.annotation.AnnotationCache;
import corp.data.annotation.CorpDocument;
import corp.data.annotation.CorpDocumentSet;
import corp.util.CorpProperties;
import corp.util.OutputWriter;
import corp.util.StanfordUtil;
import corp.util.StringUtil;

public class ConstructBrownClusterData {
	public static void main(String[] args) {

	}
	
	protected void constructForAnnotated() {
		CorpProperties properties = new CorpProperties("corp.properties");
		String outputPath = properties.getBrownClustererSourceDocument();
		
		AnnotationCache annotationCache = new AnnotationCache(
				properties.getStanfordAnnotationDirPath(),
				properties.getStanfordAnnotationCacheSize(),
				properties.getStanfordCoreMapDirPath(),
				properties.getStanfordCoreMapCacheSize(),
				new OutputWriter()
		);
			
		CorpDocumentSet documentSet = new CorpDocumentSet(
				properties.getCorpRelDirPath(), 
				annotationCache,
				4,
				0,
				new OutputWriter()
		);
			
        try {
    		BufferedWriter w = new BufferedWriter(new FileWriter(outputPath));
        
			List<CorpDocument> documents = documentSet.getDocuments();
			for (CorpDocument document : documents) {
				for (int i = 0; i < document.getSentenceCount(); i++) {
					List<String> tokenTexts = StanfordUtil.getSentenceTokenTexts(document.getSentenceAnnotation(i));
					StringBuilder sentenceStr = new StringBuilder();
					for (String tokenText : tokenTexts) {
						String cleanTokenText = StringUtil.clean(tokenText);
						if (cleanTokenText.length() != 0)
							sentenceStr = sentenceStr.append(cleanTokenText).append(" ");
					}
					w.write(sentenceStr.toString().trim() + "\n");
				}
			}
		
			w.close();
        } catch (IOException e) { e.printStackTrace(); }
	}
	
	protected void constructForAll() {
		CorpProperties properties = new CorpProperties("corp.properties");
		String outputPath = properties.getBrownClustererSourceDocument();
		
		
		/////////////////
		/*
        try {
    		BufferedWriter w = new BufferedWriter(new FileWriter(outputPath));
        
			List<CorpDocument> documents = documentSet.getDocuments();
			for (CorpDocument document : documents) {
				for (int i = 0; i < document.getSentenceCount(); i++) {
					List<String> tokenTexts = StanfordUtil.getSentenceTokenTexts(document.getSentenceAnnotation(i));
					StringBuilder sentenceStr = new StringBuilder();
					for (String tokenText : tokenTexts) {
						String cleanTokenText = StringUtil.clean(tokenText);
						if (cleanTokenText.length() != 0)
							sentenceStr = sentenceStr.append(cleanTokenText).append(" ");
					}
					w.write(sentenceStr.toString().trim() + "\n");
				}
			}
		
			w.close();
        } catch (IOException e) { e.printStackTrace(); }*/
	}
	
}
