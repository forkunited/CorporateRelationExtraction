package corp.data.annotation;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.stanford.nlp.pipeline.Annotation;

public class CorpDocumentSet {
	private String corpRelDirPath;
	private String stanfordAnnotationDirPath;
	private HashMap<String, CorpDocument> documents; // Map Stanford annotation file names to documents
	private LinkedHashMap<String, Annotation> annotationCache;
	
	
	public CorpDocumentSet(String corpRelDirPath, String stanfordAnnotationDirPath) {
		this(corpRelDirPath, stanfordAnnotationDirPath, 10, 0);
	}
	
	public CorpDocumentSet(String corpRelDirPath, String stanfordAnnotationDirPath, int stanfordAnnotationCacheSize) {
		this(corpRelDirPath, stanfordAnnotationDirPath, stanfordAnnotationCacheSize, 0);
	}
	
	public CorpDocumentSet(String corpRelDirPath, String stanfordAnnotationDirPath, final int stanfordAnnotationCacheSize, int maxCorpRelDocuments) {
		this.corpRelDirPath = corpRelDirPath;
		this.stanfordAnnotationDirPath = stanfordAnnotationDirPath;
		this.documents = new HashMap<String, CorpDocument>();
		
		this.annotationCache = new LinkedHashMap<String, Annotation>(stanfordAnnotationCacheSize+1, .75F, true) {
			private static final long serialVersionUID = 1L;

			// This method is called just after a new entry has been added
		    public boolean removeEldestEntry(Map.Entry<String, Annotation> eldest) {
		        return size() > stanfordAnnotationCacheSize;
		    }
		};
		
		loadDocuments(maxCorpRelDocuments);
	}
	
	public List<CorpDocument> getDocuments() {
		return new ArrayList<CorpDocument>(documents.values());
	}
	
	private void loadDocuments(int maxCorpRelDocuments) {
		File corpRelDir = new File(this.corpRelDirPath);
		File stanfordAnnotationDir = new File(this.stanfordAnnotationDirPath);
		
		try {
			if (!corpRelDir.exists() || !corpRelDir.isDirectory())
				throw new IllegalArgumentException("Invalid corporate relation document directory: " + corpRelDir.getAbsolutePath());
			if (!stanfordAnnotationDir.exists() || !stanfordAnnotationDir.isDirectory())
				throw new IllegalArgumentException("Invalid Stanford annotation document directory: " + stanfordAnnotationDir.getAbsolutePath());
			
			File[] corpRelFiles = corpRelDir.listFiles();
			int numDocuments = 0;
			for (File corpRelFile : corpRelFiles) {
				String corpRelFileName = corpRelFile.getName();
				String corpRelFilePath = corpRelFile.getAbsolutePath();
				int sentenceIndex = sentenceIndexFromCorpRelFileName(corpRelFileName);
				if (sentenceIndex < 0) {
					System.err.println("Skipped file: " + corpRelFileName + " (couldn't get sentence index)");
					continue;
				}
				
				String annotationFilePath = annotationFilePathFromCorpRelFileName(corpRelFileName);
				if (annotationFilePath == null) {
					System.err.println("Skipped file: " + corpRelFileName + " (couldn't get annotation file path)");
					continue;
				}
					
				CorpDocument document = null;
				if (this.documents.containsKey(annotationFilePath)) {
					document = this.documents.get(annotationFilePath);
				} else {
					document = new CorpDocument(annotationFilePath, this.annotationCache);
					this.documents.put(annotationFilePath, document);
				}
				
				document.loadCorpRelsFromFile(corpRelFilePath, sentenceIndex);
				numDocuments++;
				if (maxCorpRelDocuments != 0 && numDocuments >= maxCorpRelDocuments)
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private int sentenceIndexFromCorpRelFileName(String fileName) {
		int lineIndex = fileName.indexOf(".line");
		if (lineIndex < 0)
			return -1;
		int lastDotIndex = fileName.indexOf(".", lineIndex + 1);
		if (lastDotIndex < 0)
			return -1;
		
		int sentenceIndexStrStart = lineIndex + ".line".length();
		int sentenceIndexStrEnd = lastDotIndex;
		if (sentenceIndexStrStart >= sentenceIndexStrEnd)
			return -1;
		
		try {
			return Integer.parseInt(fileName.substring(sentenceIndexStrStart, sentenceIndexStrEnd)) - 1;
		} catch (NumberFormatException e) {
			return -1;
		}
	}
	
	private String annotationFilePathFromCorpRelFileName(String fileName) {
		if (fileName.indexOf(".nlp") < 0)
			return null;
		
		String stanfordAnnotationFileName = fileName.substring(0, fileName.indexOf(".nlp") + 4) + ".obj";
		File annotationFile = new File(this.stanfordAnnotationDirPath, stanfordAnnotationFileName);
		if (annotationFile.exists())
			return annotationFile.getAbsolutePath();
		
		int dateStartIndex = fileName.indexOf("-8-K-") + 5;
		String year = fileName.substring(dateStartIndex, dateStartIndex+4);
		String month = fileName.substring(dateStartIndex+4, dateStartIndex+6);
		annotationFile = new File(this.stanfordAnnotationDirPath, year + "/" + month + "/" + stanfordAnnotationFileName);
		if (annotationFile.exists())
			return annotationFile.getAbsolutePath();
		
		return null;
	}
}
