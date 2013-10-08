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
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;

public class ConstructBrownClusterData {
	public static void main(String[] args) {
		constructForAll();
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
				-1,
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
						if (cleanTokenText.matches("[0-9]*"))
							sentenceStr.append("[NUMBER]");
						if (cleanTokenText.length() != 0 && cleanTokenText.matches("[a-zA-Z]*"))
							sentenceStr = sentenceStr.append(cleanTokenText).append(" ");
					}
					String outSentence = sentenceStr.toString().trim();
					if (outSentence.length() > 0) {
						System.out.println("Output sentence: " + outSentence);
						w.write(outSentence + "\n");
					} else {
						System.out.println("Skipped empty sentence.");
					}
				}
			}
		
			w.close();
        } catch (IOException e) { e.printStackTrace(); }
	}
	
	protected static void constructForAll() {
		CorpProperties properties = new CorpProperties("corp.properties");
		String outputPath = properties.getBrownClustererSourceDocument();
		
		System.out.println("Loading documents...");
		
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
				32,
				0,
				-1,
				new OutputWriter()
		);
			
        try {
    		BufferedWriter w = new BufferedWriter(new FileWriter(outputPath));
        
			List<CorpDocument> documents = documentSet.getDocuments();
			for (CorpDocument document : documents) {
				Annotation annotation = document.getAnnotation();
				System.out.println("Running over document: " + document.getAnnotationPath());
				List<CoreMap> sentenceAnnotations = StanfordUtil.getDocumentSentences(annotation);
				for (int i = 0; i < sentenceAnnotations.size(); i++) {
					List<String> tokenTexts = StanfordUtil.getSentenceTokenTexts(sentenceAnnotations.get(i));
					StringBuilder sentenceStr = new StringBuilder();
					for (String tokenText : tokenTexts) {
						String cleanTokenText = StringUtil.clean(tokenText);
						if (cleanTokenText.matches("[0-9]*"))
							sentenceStr.append("[NUMBER]");
						if (cleanTokenText.length() != 0 && cleanTokenText.matches("[a-zA-Z]*"))
							sentenceStr = sentenceStr.append(cleanTokenText).append(" ");
					}
					String outSentence = sentenceStr.toString().trim();
					if (outSentence.length() > 0) {
						System.out.println("Output sentence: " + outSentence);
						w.write(outSentence + "\n");
					} else {
						System.out.println("Skipped empty sentence.");
					}
				}
			}
		
			w.close();
        } catch (IOException e) { e.printStackTrace(); }

	}
	
}
