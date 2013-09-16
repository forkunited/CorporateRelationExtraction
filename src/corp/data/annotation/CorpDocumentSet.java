package corp.data.annotation;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CorpDocumentSet {
	private String corpRelDirPath;
	private ConcurrentHashMap<String, CorpDocument> documents; // Map Stanford annotation file names to documents
	private AnnotationCache annotationCache;
	private int maxThreads;
	
	public CorpDocumentSet(String corpRelDirPath, AnnotationCache annotationCache) {
		this(corpRelDirPath, annotationCache, 1, 0);
	}
	
	public CorpDocumentSet(String corpRelDirPath, AnnotationCache annotationCache, int maxThreads) {
		this(corpRelDirPath, annotationCache, maxThreads, 0);
	}
	
	public CorpDocumentSet(String corpRelDirPath, AnnotationCache annotationCache, int maxThreads, int maxCorpRelDocuments) {
		this.corpRelDirPath = corpRelDirPath;
		this.annotationCache = annotationCache;
		this.maxThreads = maxThreads;
		this.documents = new ConcurrentHashMap<String, CorpDocument>();
		
		loadDocuments(maxCorpRelDocuments);
	}
	
	public List<CorpDocument> getDocuments() {
		return new ArrayList<CorpDocument>(documents.values());
	}
	
	private void loadDocuments(int maxCorpRelDocuments) {
		File corpRelDir = new File(this.corpRelDirPath);
		
		try {
			if (!corpRelDir.exists() || !corpRelDir.isDirectory())
				throw new IllegalArgumentException("Invalid corporate relation document directory: " + corpRelDir.getAbsolutePath());

			ExecutorService threadPool = Executors.newFixedThreadPool(this.maxThreads);
			File[] corpRelFiles = corpRelDir.listFiles();
			Arrays.sort(corpRelFiles, new Comparator<File>() { // Ensure determinism
			    public int compare(File o1, File o2) {
			        return o1.getAbsolutePath().compareTo(o2.getAbsolutePath());
			    }
			});
			
			int numDocuments = 0;
			for (File corpRelFile : corpRelFiles) {
				threadPool.submit(new DocumentLoadThread(corpRelFile));
				
				numDocuments++;
				if (maxCorpRelDocuments != 0 && numDocuments >= maxCorpRelDocuments)
					break;
			}
			
			threadPool.shutdown();
			threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
	private class DocumentLoadThread implements Runnable {
		private File corpRelFile;
		
		public DocumentLoadThread(File corpRelFile) {
			this.corpRelFile = corpRelFile;
		}
		
		public void run() {
			String corpRelFileName = this.corpRelFile.getName();
			String corpRelFilePath = this.corpRelFile.getAbsolutePath();
			int sentenceIndex = sentenceIndexFromCorpRelFileName(corpRelFileName);
			if (sentenceIndex < 0) {
				System.err.println("Skipped file: " + corpRelFileName + " (couldn't get sentence index)");
				return;
			}
			
			String annotationFileName = annotationFileNameFromCorpRelFileName(corpRelFileName);
			if (annotationFileName == null) {
				System.err.println("Skipped file: " + corpRelFileName + " (couldn't get annotation file name)");
				return;
			}
				
			CorpDocument document = fetchDocumentOrAdd(annotationFileName);
			synchronized (document) {
				document.loadCorpRelsFromFile(corpRelFilePath, sentenceIndex);
			}
		}
	}
	
	private synchronized CorpDocument fetchDocumentOrAdd(String annotationFileName) {
		CorpDocument document = null;
		if (this.documents.containsKey(annotationFileName)) {
			document = this.documents.get(annotationFileName);
		} else {
			document = new CorpDocument(annotationFileName, this.annotationCache);
			this.documents.put(annotationFileName, document);
		}
		
		return document;
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
	
	private String annotationFileNameFromCorpRelFileName(String fileName) {
		if (fileName.indexOf(".nlp") < 0)
			return null;
		
		String annotationFileName = fileName.substring(0, fileName.indexOf(".nlp") + 4) + ".obj";
		File annotationFile = new File(this.annotationCache.getDocAnnoDirPath(), annotationFileName);
		if (annotationFile.exists())
			return annotationFileName;
		
		int dateStartIndex = fileName.indexOf("-8-K-") + 5;
		String year = fileName.substring(dateStartIndex, dateStartIndex+4);
		String month = fileName.substring(dateStartIndex+4, dateStartIndex+6);
		annotationFile = new File(this.annotationCache.getDocAnnoDirPath(), year + "/" + month + "/" + annotationFileName);
		if (annotationFile.exists())
			return annotationFileName;
		
		return null;
	}
}
