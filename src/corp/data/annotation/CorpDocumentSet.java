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
		this(corpRelDirPath, stanfordAnnotationDirPath, true);
	}
	
	public CorpDocumentSet(String corpRelDirPath, String stanfordAnnotationDirPath, boolean cacheAnnotations) {
		this.corpRelDirPath = corpRelDirPath;
		this.stanfordAnnotationDirPath = stanfordAnnotationDirPath;
		this.documents = new HashMap<String, CorpDocument>();
		loadDocuments(cacheAnnotations);
	}
	
	public List<CorpDocument> getDocuments() {
		return new ArrayList<CorpDocument>(documents.values());
	}
	
	public CorpRelDataSet generateDataSet() {
		CorpRelDataSet dataSet = new CorpRelDataSet();
		for (CorpDocument document : this.documents.values()) {
			dataSet.addData(document.getCorpRelDatums());
		}
		return dataSet;
	}
	
	private void loadDocuments(boolean cacheAnnotations) {
		File corpRelDir = new File(this.corpRelDirPath);
		File stanfordAnnotationDir = new File(this.stanfordAnnotationDirPath);
		
		try {
			if (!corpRelDir.exists() || !corpRelDir.isDirectory() || !stanfordAnnotationDir.exists() || !stanfordAnnotationDir.isDirectory())
				throw new IllegalArgumentException();
			
			File[] corpRelFiles = corpRelDir.listFiles();
			for (File corpRelFile : corpRelFiles) {
				String corpRelFileName = corpRelFile.getName();
				String corpRelFilePath = corpRelFile.getPath();
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
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private int sentenceIndexFromCorpRelFileName(String fileName) {
		int lineIndex = fileName.indexOf(".line");
		if (lineIndex < 0)
			return -1;
		int lastDotIndex = fileName.indexOf(".");
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
