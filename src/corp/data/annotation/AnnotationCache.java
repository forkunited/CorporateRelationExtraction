package corp.data.annotation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ark.util.OutputWriter;
import ark.util.StanfordUtil;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;

/**
 * 
 * AnnotationCache holds LRU caches of Stanford CoreNLP annotated documents
 * and sentences.  The cache assumes that if a document is requested, then that 
 * document exists, and it should be loaded from disk if it is not in the cache.
 * On the other hand, if a sentence is requested, and it is not in memory
 * or on disk, then the document annotation is loaded instead, and split up
 * into sentence annotations on disk so that the full document does not need
 * to be loaded next time the same sentence is requested.
 * 
 * This cache is mainly as helper for classes representing press release 
 * documents and corporate relation annotations in corp.data.annotation. When
 * other classes throughout the project request information from the document
 * classes, the document classes query the cache.  This is necessary because
 * the Stanford CoreNLP annotation documents take up a lot of memory.
 * 
 * @author Bill McDowell
 *
 */
public class AnnotationCache {
	private String docAnnoDirPath;
	private String sentenceAnnoDirPath;
	
	private Map<String, Integer> docSentenceCountCache;
	private Map<String, Annotation> docAnnoCache;
	private Map<String, CoreMap> sentenceAnnoCache;
	private ConcurrentHashMap<String, Object> locks;
	private OutputWriter output;
	
	public AnnotationCache(String docAnnoDirPath, final int docAnnoCacheSize, OutputWriter output) {
		this.docAnnoDirPath = docAnnoDirPath;
		this.output = output;
		
		this.docAnnoCache = Collections.synchronizedMap(new LinkedHashMap<String, Annotation>(docAnnoCacheSize+1, .75F, true) {
			private static final long serialVersionUID = 1L;

			// This method is called just after a new entry has been added
		    public boolean removeEldestEntry(Map.Entry<String, Annotation> eldest) {
		        return size() > docAnnoCacheSize;
		    }
		});
		
		this.sentenceAnnoCache = null;
		this.sentenceAnnoDirPath = null;
		
		this.locks = new ConcurrentHashMap<String, Object>();
		
		File docAnnoDir = new File(this.docAnnoDirPath);
		if (!docAnnoDir.exists() || !docAnnoDir.isDirectory())
			throw new IllegalArgumentException("Invalid Stanford Document Annotation Directory: " + docAnnoDir.getAbsolutePath());
	}
	
	public AnnotationCache(String docAnnoDirPath, final int docAnnoCacheSize, String sentenceAnnoDirPath, final int sentenceAnnoCacheSize, OutputWriter output) {
		this.docAnnoDirPath = docAnnoDirPath;
		this.sentenceAnnoDirPath = sentenceAnnoDirPath;
		this.output = output;
		
		this.docSentenceCountCache = Collections.synchronizedMap(new LinkedHashMap<String, Integer>(docAnnoCacheSize+1, .75F, true) {
			private static final long serialVersionUID = 1L;

			// This method is called just after a new entry has been added
		    public boolean removeEldestEntry(Map.Entry<String, Integer> eldest) {
		        return size() > docAnnoCacheSize;
		    }
		});
		
		this.docAnnoCache = Collections.synchronizedMap(new LinkedHashMap<String, Annotation>(docAnnoCacheSize+1, .75F, true) {
			private static final long serialVersionUID = 1L;

			// This method is called just after a new entry has been added
		    public boolean removeEldestEntry(Map.Entry<String, Annotation> eldest) {
		        return size() > docAnnoCacheSize;
		    }
		});
		
		this.sentenceAnnoCache = Collections.synchronizedMap(new LinkedHashMap<String, CoreMap>(sentenceAnnoCacheSize+1, .75F, true) {
			private static final long serialVersionUID = 1L;

			// This method is called just after a new entry has been added
		    public boolean removeEldestEntry(Map.Entry<String, CoreMap> eldest) {
		        return size() > sentenceAnnoCacheSize;
		    }
		});
		
		this.locks = new ConcurrentHashMap<String, Object>();
		
		File docAnnoDir = new File(this.docAnnoDirPath);
		File sentenceAnnoDir = new File(this.sentenceAnnoDirPath);
		
		if (!docAnnoDir.exists() || !docAnnoDir.isDirectory())
			throw new IllegalArgumentException("Invalid Stanford Document Annotation Directory: " + docAnnoDir.getAbsolutePath());
		if (!sentenceAnnoDir.exists() || !sentenceAnnoDir.isDirectory())
			throw new IllegalArgumentException("Invalid Stanford Sentence Annotation Directory: " + sentenceAnnoDir.getAbsolutePath());
					
	}
	
	public String getDocAnnoDirPath() {
		return this.docAnnoDirPath;
	}
	
	public String getSentenceAnnoDirPath() {
		return this.sentenceAnnoDirPath;
	}
	
	public Annotation getDocumentAnnotation(String documentName) {
		Object documentLock = getLock(documentName);
		
		synchronized (documentLock) {
			synchronized (this.locks) {
				if (this.docAnnoCache.containsKey(documentName)) {
					return this.docAnnoCache.get(documentName);
				} else {
					this.output.debugWriteln("Loading document annotation for " + documentName);
				}
			}
			
			String documentPath = getDocumentPath(documentName);
			Annotation docAnno = null;
			synchronized (this) {
				this.output.debugWriteln("Deserializing document annotation for " + documentName);
				while (docAnno == null) {
					docAnno = StanfordUtil.deserializeAnnotation(documentPath);
					if (docAnno == null)
						this.output.debugWriteln("Failed to deserialize annotation for " + documentName + " retrying...");
				}
			}
			
			this.output.debugWriteln("Finished deserializing annotation for " + documentName);
			
			synchronized (this.locks) {
				this.docAnnoCache.put(documentName, docAnno);
			}
			
			this.output.debugWriteln("Loaded document annotation for " + documentName);
			
			return docAnno;
		}
	}
	
	public CoreMap getSentenceAnnotation(String documentName, int sentenceIndex) {
		if (this.sentenceAnnoCache == null) {
			Annotation documentAnnotation = getDocumentAnnotation(documentName);
			return StanfordUtil.getDocumentSentences(documentAnnotation).get(sentenceIndex);
		}
		
		String sentenceName = getSentenceName(documentName, sentenceIndex);
		Object sentenceLock = getLock(sentenceName);
		
		synchronized (sentenceLock) {
			synchronized (this.locks) {
				if (this.sentenceAnnoCache.containsKey(sentenceName)) {
					return this.sentenceAnnoCache.get(sentenceName);
				}
			}
			
			CoreMap sentenceAnno = null;
			synchronized (getLock(documentName)) {
				if (!sentenceAnnoFilesExist(documentName)) {
					saveSentenceAnnosForDocument(documentName);
				} else {
					this.output.debugWriteln("Loading sentence annotation for " + sentenceName);
				}
			}
			
			sentenceAnno = StanfordUtil.deserializeCoreMap(getSentencePath(documentName, sentenceIndex));
				
			synchronized(this.locks) {				
				this.sentenceAnnoCache.put(sentenceName, sentenceAnno);
			}
			
			return sentenceAnno;
		}
	}
	
	public int getSentenceCount(String documentName) {
		if (this.docSentenceCountCache == null) {
			Annotation documentAnnotation = getDocumentAnnotation(documentName);
			return StanfordUtil.getDocumentSentences(documentAnnotation).size();
		}
		
		synchronized (getLock(documentName)) {
			synchronized (this.locks) {
				if (this.docSentenceCountCache.containsKey(documentName))
					return this.docSentenceCountCache.get(documentName);
			}
			
			if (!sentenceAnnoFilesExist(documentName)) {
				saveSentenceAnnosForDocument(documentName);
			} else {
				this.output.debugWriteln("Loading sentence count for " + documentName);
			}
		
			int sentenceCount = 0;
			try {
				BufferedReader br = new BufferedReader(new FileReader(getDocumentSentenceMetaDataPath(documentName)));
				sentenceCount = Integer.valueOf(br.readLine());
				br.close();
		    } catch (Exception e) {
		    	e.printStackTrace();
		    	return -1;
		    }
			
			synchronized (this.locks) {
				this.docSentenceCountCache.put(documentName, sentenceCount);
			}

			return sentenceCount;
		}
	}
	
	private boolean sentenceAnnoFilesExist(String documentName) {
		File sentenceFile = new File(getDocumentSentenceMetaDataPath(documentName));
		return sentenceFile.exists();
	}
	
	private boolean saveSentenceAnnosForDocument(String documentName) {
		this.output.debugWriteln("Note: No sentence annotation documents for " + documentName + ".  Creating them....  ");
        Annotation anno = this.getDocumentAnnotation(documentName);
        List<CoreMap> sentences = StanfordUtil.getDocumentSentences(anno);
        for (int i = 0; i < sentences.size(); i++) {
        	if (!StanfordUtil.serializeCoreMap(getSentencePath(documentName, i), sentences.get(i))) {
        		this.output.debugWriteln("Failed to serialized sentence " + documentName + " " + i);
        		return false;
        	}
        }
		
		int sentenceCount = sentences.size();
		try {
    		BufferedWriter bw = new BufferedWriter(new FileWriter(getDocumentSentenceMetaDataPath(documentName)));
    		bw.write(String.valueOf(sentenceCount));
            bw.close();
        } catch (IOException e) { 
        	this.output.debugWriteln("Failed to output document meta data " + documentName);
        	e.printStackTrace(); 
        	return false; 
        }
		
		this.output.debugWriteln("Finished outputting sentence annotation documents for " + documentName + ".");
        return true;
	}
	
	private Object getLock(String name) {
		synchronized(this.locks) {	
			if (!this.locks.containsKey(name))
				this.locks.put(name, new Object());
		}
		
		return this.locks.get(name);
	}
	
	private String getDocumentPath(String documentName) {
		File annotationFile = new File(this.docAnnoDirPath, documentName);
		if (annotationFile.exists())
			return annotationFile.getAbsolutePath();
		else {
			String year = getYear(documentName);
			String month = getMonth(documentName);
			annotationFile = new File(this.docAnnoDirPath, year + "/" + month + "/" + documentName);
			if (annotationFile.exists())
				return annotationFile.getAbsolutePath();
			else
				return null;
		}
	}
	
	private String getDocumentSentenceMetaDataPath(String documentName) {
		return (new File(this.sentenceAnnoDirPath, documentName + ".sentences")).getAbsolutePath();
	}
	
	private String getSentencePath(String documentName, int sentenceIndex) {
		return (new File(this.sentenceAnnoDirPath, documentName + "." + sentenceIndex)).getAbsolutePath();
	}
	
	private String getSentenceName(String documentName, int sentenceIndex) {
		return documentName + "." + sentenceIndex;
	}
	
	private String getYear(String documentName) {
		int dateStartIndex = documentName.indexOf("-8-K-") + 5;
		return documentName.substring(dateStartIndex, dateStartIndex+4);
	}
	
	private String getMonth(String documentName) {
		int dateStartIndex = documentName.indexOf("-8-K-") + 5;
		return documentName.substring(dateStartIndex+4, dateStartIndex+6);
	}
}
