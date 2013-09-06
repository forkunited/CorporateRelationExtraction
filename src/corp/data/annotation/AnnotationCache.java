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

import corp.util.StanfordUtil;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;

public class AnnotationCache {
	private String docAnnoDirPath;
	private String sentenceAnnoDirPath;
	
	private Map<String, Annotation> docAnnoCache;
	private Map<String, CoreMap> sentenceAnnoCache;
	private ConcurrentHashMap<String, Object> locks;
	
	public AnnotationCache(String docAnnoDirPath, final int docAnnoCacheSize, String sentenceAnnoDirPath, final int sentenceAnnoCacheSize) {
		this.docAnnoDirPath = docAnnoDirPath;
		this.sentenceAnnoDirPath = sentenceAnnoDirPath;
		
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
				if (this.docAnnoCache.containsKey(documentName))
					return this.docAnnoCache.get(documentName);
			}
			
			Annotation docAnno = null;
			docAnno = StanfordUtil.deserializeAnnotation(getDocumentPath(documentName));
			
			synchronized (this.locks) {
				this.docAnnoCache.put(documentName, docAnno);
			}
			
			return docAnno;
		}
	}
	
	public CoreMap getSentenceAnnotation(String documentName, int sentenceIndex) {
		String sentenceName = getSentenceName(documentName, sentenceIndex);
		Object sentenceLock = getLock(sentenceName);
		
		synchronized (sentenceLock) {
			synchronized (this.locks) {
				if (this.sentenceAnnoCache.containsKey(sentenceName))
					return this.sentenceAnnoCache.get(sentenceName);
			}
			
			CoreMap sentenceAnno = null;
			synchronized (getLock(documentName)) {
				if (!sentenceAnnoFilesExist(documentName))
					saveSentenceAnnosForDocument(documentName);
			}
			
			sentenceAnno = StanfordUtil.deserializeCoreMap(getSentencePath(documentName, sentenceIndex));
				
			synchronized(this.locks) {				
				this.sentenceAnnoCache.put(sentenceName, sentenceAnno);
			}
			
			return sentenceAnno;
		}
	}
	
	public int getSentenceCount(String documentName) {
		synchronized (getLock(documentName)) {
			if (!sentenceAnnoFilesExist(documentName))
				saveSentenceAnnosForDocument(documentName);
		}
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(getDocumentSentenceMetaDataPath(documentName)));
			int sentenceCount = Integer.valueOf(br.readLine());
			br.close();
			
			return sentenceCount;
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	return -1;
	    }
	}
	
	private boolean sentenceAnnoFilesExist(String documentName) {
		File sentenceFile = new File(getDocumentSentenceMetaDataPath(documentName));
		return sentenceFile.exists();
	}
	
	private boolean saveSentenceAnnosForDocument(String documentName) {
		System.out.println("Note: No sentence annotation documents for " + documentName + ".  Creating them....  ");
        Annotation anno = this.getDocumentAnnotation(documentName);
        List<CoreMap> sentences = StanfordUtil.getDocumentSentences(anno);
        for (int i = 0; i < sentences.size(); i++) {
        	if (!StanfordUtil.serializeCoreMap(getSentencePath(documentName, i), sentences.get(i)))
        		return false;
        }
		
		int sentenceCount = sentences.size();
		try {
    		BufferedWriter bw = new BufferedWriter(new FileWriter(getDocumentSentenceMetaDataPath(documentName)));
    		bw.write(String.valueOf(sentenceCount));
            bw.close();
        } catch (IOException e) { e.printStackTrace(); return false; }
        return true;
	}
	
	private Object getLock(String name) {
		synchronized(this.locks) {				
			if (!this.locks.contains(name))
				this.locks.put(name, new Object());
		}
		
		return this.locks.get(name);
	}
	
	
	private String getDocumentPath(String documentName) {
		return (new File(this.docAnnoDirPath, documentName)).getAbsolutePath();
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
}
