package corp.scratch;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import corp.data.CorpMetaData;
import corp.data.annotation.AnnotationCache;
import corp.data.annotation.CorpDocument;
import corp.data.annotation.CorpDocumentSet;
import corp.util.CorpProperties;
import ark.util.OutputWriter;
import ark.util.StanfordUtil;
import ark.util.StringUtil;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;

/**
 * ConstructBrownClusterData takes a set of corporate press release documents
 * through corp.annotation.CorpDocumentSet, and outputs clean text for input
 * to Brown clustering (https://github.com/percyliang/brown-cluster).  Words
 * are first cleaned using the default clean function from the ARKWater 
 * project, and then words that contain non-alphabetic symbols are removed, 
 * and numbers are replaced by "[NUMBER]".
 * 
 * @author Bill McDowell
 *
 */
public class ConstructBrownClusterData {
	public static void main(String[] args) {
		constructForAll();
	}
	
	protected void constructForAnnotated() {
		CorpProperties properties = new CorpProperties();
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
				new OutputWriter(),
				new CorpMetaData("Corp", properties.getCorpMetaDataPath())
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
						if (cleanTokenText.matches("[0-9]+"))
							sentenceStr.append("[NUMBER]").append(" ");
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
		CorpProperties properties = new CorpProperties();
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
				new OutputWriter(),
				false,
				new CorpMetaData("Corp", properties.getCorpMetaDataPath())
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
						if (cleanTokenText.matches("[0-9]+"))
							sentenceStr.append("[NUMBER]").append(" ");
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
