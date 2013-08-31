package corp.data.annotation;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CorpDocumentSet {
	private String corpRelDirPath;
	private String stanfordAnnotationDirPath;
	private HashMap<String, CorpDocument> documents; // Map Stanford annotation file names to documents
	
	public CorpDocumentSet(String corpRelDirPath, String stanfordAnnotationDirPath) {
		this(corpRelDirPath, stanfordAnnotationDirPath, true, 0);
	}
	
	public CorpDocumentSet(String corpRelDirPath, String stanfordAnnotationDirPath, boolean cacheAnnotations) {
		this(corpRelDirPath, stanfordAnnotationDirPath, cacheAnnotations, 0);
	}
	
	public CorpDocumentSet(String corpRelDirPath, String stanfordAnnotationDirPath, boolean cacheAnnotations, int maxCorpRelDocuments) {
		this.corpRelDirPath = corpRelDirPath;
		this.stanfordAnnotationDirPath = stanfordAnnotationDirPath;
		this.documents = new HashMap<String, CorpDocument>();
		loadDocuments(cacheAnnotations, maxCorpRelDocuments);
	}
	
	public List<CorpDocument> getDocuments() {
		return new ArrayList<CorpDocument>(documents.values());
	}
	
	private void loadDocuments(boolean cacheAnnotations, int maxCorpRelDocuments) {
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
				if (sentenceIndex < 0)
					continue;
				
				String annotationFilePath = annotationFilePathFromCorpRelFileName(corpRelFileName);
				if (annotationFilePath == null)
					continue;
				
				CorpDocument document = null;
				if (this.documents.containsKey(annotationFilePath)) {
					document = this.documents.get(annotationFilePath);
				} else {
					document = new CorpDocument(annotationFilePath, cacheAnnotations);
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
		File annotationFile = new File(this.stanfordAnnotationDirPath, fileName.substring(0, fileName.indexOf(".nlp") + 4) + ".obj");
		if (!annotationFile.exists())
			return null;
		return annotationFile.getAbsolutePath();
	}
}
