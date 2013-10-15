package corp.data.annotation;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import corp.data.CorpMetaData;
import corp.util.OutputWriter;

public class CorpDocumentSet {
	private String corpRelDirPath;
	private ConcurrentHashMap<String, CorpDocument> annotatedDocuments; // Map Stanford annotation file names to documents
	private ConcurrentHashMap<String, CorpDocument> unannotatedDocuments;
	private AnnotationCache annotationCache;
	private int maxThreads;
	private boolean loadUnnanotatedRelations;
	private OutputWriter output;
	
	public CorpDocumentSet(String corpRelDirPath, AnnotationCache annotationCache, OutputWriter output, CorpMetaData metaData) {
		this(corpRelDirPath, annotationCache, 1, 0, 0, output, metaData);
	}
	
	public CorpDocumentSet(String corpRelDirPath, AnnotationCache annotationCache, int maxThreads, OutputWriter output, CorpMetaData metaData) {
		this(corpRelDirPath, annotationCache, maxThreads, 0, 0, output, metaData);
	}
	
	public CorpDocumentSet(String corpRelDirPath, AnnotationCache annotationCache, int maxThreads, int maxCorpRelDocuments, int maxUnannotatedDocuments, OutputWriter output, CorpMetaData metaData) {
		this(corpRelDirPath, annotationCache, maxThreads, maxCorpRelDocuments, maxUnannotatedDocuments, output, true, metaData);
	}
	
	public CorpDocumentSet(String corpRelDirPath, AnnotationCache annotationCache, int maxThreads, int maxCorpRelDocuments, int maxUnannotatedDocuments, OutputWriter output, boolean loadUnannotatedRelations, CorpMetaData metaData) {
		this.corpRelDirPath = corpRelDirPath;
		this.annotationCache = annotationCache;
		this.maxThreads = maxThreads;
		this.annotatedDocuments = new ConcurrentHashMap<String, CorpDocument>();
		this.unannotatedDocuments = new ConcurrentHashMap<String, CorpDocument>();
		this.output = output;
		this.loadUnnanotatedRelations = loadUnannotatedRelations;
		
		loadAnnotatedDocuments(maxCorpRelDocuments);
		loadUnannotatedDocuments(maxUnannotatedDocuments, metaData);
	}
	
	public List<CorpDocument> getDocuments() {
		List<CorpDocument> documents = new ArrayList<CorpDocument>();
		
		documents.addAll(getAnnotatedDocuments());
		documents.addAll(getUnannotatedDocuments());
		
		return documents;
	}
	
	public List<CorpDocument> getAnnotatedDocuments() {
		return new ArrayList<CorpDocument>(this.annotatedDocuments.values());
	}
	
	public List<CorpDocument> getUnannotatedDocuments() {
		return new ArrayList<CorpDocument>(this.unannotatedDocuments.values());
	}
	
	private void loadUnannotatedDocuments(int maxUnannotatedDocuments, CorpMetaData metaData) {
		if (maxUnannotatedDocuments == 0)
			return;
		File annotationDir = new File(this.annotationCache.getDocAnnoDirPath());
		this.output.debugWriteln("Loading unannotated documents from " + this.annotationCache.getDocAnnoDirPath() + "...");
		try {
			if (!annotationDir.exists() || !annotationDir.isDirectory())
				throw new IllegalArgumentException("Invalid annotation document directory: " + annotationDir.getAbsolutePath());

			ExecutorService threadPool = Executors.newFixedThreadPool(this.maxThreads);
			File[] annotationDirFiles = annotationDir.listFiles();
			List<File> annotationFiles = new ArrayList<File>();
			for (File annotationDirFile : annotationDirFiles) {
				if (annotationDirFile.isDirectory()) {
					File[] monthDirFiles = annotationDirFile.listFiles();
					for (File monthDirFile : monthDirFiles) {
						if (!monthDirFile.isDirectory())
							continue;
						File[] annotations = monthDirFile.listFiles();
						for (File annotation : annotations) {
							if (annotation.isDirectory())
								continue;
							if (annotation.getName().contains("-8-K-") && annotation.getName().contains("toked.nlp.obj"))
								annotationFiles.add(annotation);
						}
					}
				} else if (annotationDirFile.getName().contains("-8-K-") && annotationDirFile.getName().contains("toked.nlp.obj")) {
					annotationFiles.add(annotationDirFile);
				}
			}
			
			Collections.sort(annotationFiles, new Comparator<File>() { // Ensure determinism
			    public int compare(File o1, File o2) {
			        return o1.getAbsolutePath().compareTo(o2.getAbsolutePath());
			    }
			});
			
			Map<String, String> cikMap = metaData.getAttributeMap(CorpMetaData.Attribute.CIK, CorpMetaData.Attribute.NAME);
			
			int numDocuments = 0;
			for (File annotationFile : annotationFiles) {
				this.output.debugWriteln("Loading (unannotated) annotation file " + annotationFile.getAbsoluteFile() + "...");
				
				if (!this.annotatedDocuments.containsKey(annotationFile.getName()))
					threadPool.submit(new UnannotatedDocumentLoadThread(annotationFile, this.loadUnnanotatedRelations, cikMap));
				
				numDocuments++;
				if (maxUnannotatedDocuments > 0 && numDocuments >= maxUnannotatedDocuments)
					break;
			}
			
			threadPool.shutdown();
			threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		} catch (Exception e) {
			e.printStackTrace();
		}	
	
	}
	
	private class UnannotatedDocumentLoadThread implements Runnable {
		private File annotationFile;
		private boolean loadUnannotatedRelations;
		private Map<String, String> cikMap;
		
		public UnannotatedDocumentLoadThread(File annotationFile, boolean loadUnannotatedRelations, Map<String, String> cikMap) {
			this.annotationFile = annotationFile;
			this.loadUnannotatedRelations = loadUnannotatedRelations;
			this.cikMap = cikMap;
		}
		
		public void run() {
			CorpDocument document = new CorpDocument(this.annotationFile.getName(), annotationCache, output, this.cikMap);
			unannotatedDocuments.put(this.annotationFile.getName(), document);
			if (this.loadUnannotatedRelations) {
				synchronized (document) {
					document.loadUnannotatedCorpRels();
				}
			}
		}
	}
	
	
	private void loadAnnotatedDocuments(int maxCorpRelDocuments) {
		if (maxCorpRelDocuments == 0)
			return;
		
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
				//AnnotatedDocumentLoadThread loadThread = new AnnotatedDocumentLoadThread(corpRelFile);
				//loadThread.run();
				threadPool.submit(new AnnotatedDocumentLoadThread(corpRelFile));
				numDocuments++;
				
				if (maxCorpRelDocuments > 0 && numDocuments >= maxCorpRelDocuments)
					break;
			}
			
			threadPool.shutdown();
			threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
	private class AnnotatedDocumentLoadThread implements Runnable {
		private File corpRelFile;
		
		public AnnotatedDocumentLoadThread(File corpRelFile) {
			this.corpRelFile = corpRelFile;
		}
		
		public void run() {
			try {
				String corpRelFileName = this.corpRelFile.getName();
				String corpRelFilePath = this.corpRelFile.getAbsolutePath();
				int sentenceIndex = sentenceIndexFromCorpRelFileName(corpRelFileName);
				if (sentenceIndex < 0) {
					output.debugWriteln("Skipped file: " + corpRelFileName + " (couldn't get sentence index)");
					return;
				}
				
				String annotationFileName = annotationFileNameFromCorpRelFileName(corpRelFileName);
				if (annotationFileName == null) {
					output.debugWriteln("Skipped file: " + corpRelFileName + " (couldn't get annotation file name)");
					return;
				}
					
				CorpDocument document = fetchAnnotatedDocumentOrAdd(annotationFileName);
				synchronized (document) {
					document.loadAnnotatedCorpRelsFromFile(corpRelFilePath, sentenceIndex);
				}
			} catch (Exception e) {
				output.debugWriteln(corpRelFile + " load annotation thread failed: " + e.getMessage());
			}
		}
	}
	
	private synchronized CorpDocument fetchAnnotatedDocumentOrAdd(String annotationFileName) {
		CorpDocument document = null;
		if (this.annotatedDocuments.containsKey(annotationFileName)) {
			document = this.annotatedDocuments.get(annotationFileName);
		} else {
			document = new CorpDocument(annotationFileName, this.annotationCache, this.output);
			this.annotatedDocuments.put(annotationFileName, document);
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
