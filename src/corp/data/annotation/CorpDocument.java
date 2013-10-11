package corp.data.annotation;

import java.io.BufferedReader;
import java.io.File;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import corp.util.OutputWriter;
import corp.util.StanfordUtil;
import corp.util.StringUtil;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.CoreMap;

public class CorpDocument {
	private String annotationFileName;
	private AnnotationCache annotationCache;
	private List<CorpRelDatum> corpRelDatums;
	private int year;
	private OutputWriter output;

	public CorpDocument(String annotationFileName,
						AnnotationCache annotationCache,
						OutputWriter output) {	
		this.annotationFileName = annotationFileName;
		this.annotationCache = annotationCache;
		this.corpRelDatums = new ArrayList<CorpRelDatum>();
		
		/* FIXME: Do this slightly differently... */
		int dateStartIndex = this.annotationFileName.indexOf("-8-K-") + 5;
		this.year = Integer.parseInt(this.annotationFileName.substring(dateStartIndex, dateStartIndex+4));
		
		this.output = output;
	}
	
	public int getYear() {
		return this.year;
	}
	
	public List<CorpRelDatum> getCorpRelDatums() {
		return this.corpRelDatums;
	}
	
	public String getAnnotationPath() {
		return new File(this.annotationCache.getDocAnnoDirPath(), this.annotationFileName).getAbsolutePath();
	}
	
	public Annotation getAnnotation() {
		return this.annotationCache.getDocumentAnnotation(this.annotationFileName);
	}
	
	public CoreMap getSentenceAnnotation(int sentenceIndex) {
		return this.annotationCache.getSentenceAnnotation(this.annotationFileName, sentenceIndex);
	}
	
	public int getSentenceCount() {
		return this.annotationCache.getSentenceCount(this.annotationFileName);
	}
	
	public boolean loadUnannotatedCorpRels(Map<String, String> corpCikNameMap) {
		String cik = getAuthorCikFromFileName();
		if (!corpCikNameMap.containsKey(cik))
			return false;
		String authorName = corpCikNameMap.get(cik);
		
		Annotation documentAnnotation = this.annotationCache.getDocumentAnnotation(this.annotationFileName);
		List<CoreMap> sentenceAnnotations = StanfordUtil.getDocumentSentences(documentAnnotation);
		HashMap<String, List<CorpDocumentTokenSpan>> nerSpans = new HashMap<String, List<CorpDocumentTokenSpan>>();
		for (int i = 0; i < sentenceAnnotations.size(); i++) {
			List<String> nerLabels = StanfordUtil.getSentenceNamedEntityTags(sentenceAnnotations.get(i));
			int curNerSpanStart = -1;
			for (int j = 0; j < nerLabels.size(); j++) {
				if (nerLabels.get(j).equals("ORGANIZATION") && curNerSpanStart < 0) {
					curNerSpanStart = j;
				} else if (!nerLabels.get(j).equals("ORGANIZATION") && curNerSpanStart >= 0) {
					CorpDocumentTokenSpan tokenSpan = new CorpDocumentTokenSpan(this, i, curNerSpanStart, j);
					String nerKey = StringUtil.clean(tokenSpan.toString(false)).replace(" ", "_");
					if (!nerSpans.containsKey(nerKey))
						nerSpans.put(nerKey, new ArrayList<CorpDocumentTokenSpan>());
					nerSpans.get(nerKey).add(tokenSpan);
					curNerSpanStart = -1;
				}
			}
			
			if (curNerSpanStart >= 0) {
				CorpDocumentTokenSpan tokenSpan = new CorpDocumentTokenSpan(this, i, curNerSpanStart, nerLabels.size());
				String nerKey = StringUtil.clean(tokenSpan.toString(false)).replace(" ", "_");
				if (!nerSpans.containsKey(nerKey))
					nerSpans.put(nerKey, new ArrayList<CorpDocumentTokenSpan>());
				nerSpans.get(nerKey).add(tokenSpan);
			}
		}
		
		for (List<CorpDocumentTokenSpan> tokenSpan : nerSpans.values()) {
			CorpRelDatum datum = new CorpRelDatum(authorName, tokenSpan);
			this.corpRelDatums.add(datum);
		}
		
		return true;
	}
	
	public boolean loadAnnotatedCorpRelsFromFile(String path, int sentenceIndex) {		
		String authorCorpName = null;
		HashMap<String, List<CorpDocumentTokenSpan>> keyToTokenSpans = new HashMap<String, List<CorpDocumentTokenSpan>>();
		HashMap<String, CorpRelLabel[]> keyToLabels = new HashMap<String, CorpRelLabel[]>();
		
		/* Parse annotations */
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			
			br.readLine(); // Read "WHO" Line
	        String[] whatParts = br.readLine().split(";");
	        br.readLine(); // Read "SENTENCES" Line
	        String[] keyParts = br.readLine().split(";");
	        String[] tagParts = br.readLine().split(";");

	        authorCorpName = whatParts[whatParts.length - 1].trim();
	        
	        /* Parse "KEYS" line */
	        for (int i = 1; i < keyParts.length; i++) {
	        	String[] keyText = keyParts[i].split(":");
	        	if (keyText.length < 2)
	        		continue;
	        	String key = keyText[0].trim();
	        	String text = keyText[1].trim();
	        	
	        	List<CorpDocumentTokenSpan> tokenSpans = nerTextToTokenSpans(text, sentenceIndex - 1, sentenceIndex + 1);
	        	if (tokenSpans == null || tokenSpans.size() == 0)
	        		continue;
	        	
	        	if (!keyToTokenSpans.containsKey(key))
	        		keyToTokenSpans.put(key, new ArrayList<CorpDocumentTokenSpan>());
	        	for (CorpDocumentTokenSpan tokenSpan : tokenSpans) {
	        		if (!keyToTokenSpans.get(key).contains(tokenSpan))
	        			keyToTokenSpans.get(key).add(tokenSpan);
	        	}
	        }
	        
	        /* Parse "TAGS" line */
	        /* NOTE: Assumes one longest path through tag hierarchy for each key */
	        for (int i = 1; i < tagParts.length; i++) {
	        	String[] labelStrPathAndLastLabelStr = tagParts[i].split(":");
	        	if (labelStrPathAndLastLabelStr.length < 2)
	        		continue;
	        	
	        	String[] labelStrPath = labelStrPathAndLastLabelStr[0].split("-");
	        	if (labelStrPath.length < 1)
	        		continue;
	        	
	        	String lastLabelStr = labelStrPathAndLastLabelStr[1].trim();
	        	String key = labelStrPath[0].trim();
	        	try {
		        	CorpRelLabel[] labelPath = new CorpRelLabel[labelStrPath.length];
		        	for (int j = 1; j < labelStrPath.length; j++) {
		        		labelPath[j-1] = CorpRelLabel.valueOf(labelStrPath[j].trim());
		        	}
		        	labelPath[labelPath.length - 1] = CorpRelLabel.valueOf(lastLabelStr);
		        	
		        	if (!keyToLabels.containsKey(key) || labelPath.length > keyToLabels.get(key).length) {
		        		keyToLabels.put(key, labelPath);
		        	} 
	        	} catch (Exception e) {
	        		this.output.debugWriteln("Failed to load corporate relationship document: " + path);
	        		e.printStackTrace();
	        		continue;
	        	}
	        }
	        
	        br.close();
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	return false;
	    }
		
		/* Create datums */
		for (Entry<String, CorpRelLabel[]> keyLabelEntry : keyToLabels.entrySet()) {
			if (!keyToTokenSpans.containsKey(keyLabelEntry.getKey()))
				continue;
			CorpRelDatum datum = new CorpRelDatum(authorCorpName, keyToTokenSpans.get(keyLabelEntry.getKey()));
			datum.setLabelPath(new CorpRelLabelPath(keyLabelEntry.getValue()));
			this.corpRelDatums.add(datum);
		}
			
		return true;
	}
	
	private List<CorpDocumentTokenSpan> nerTextToTokenSpans(String nerText, int minSentenceIndex, int maxSentenceIndex) {
		String[] nerTextAndType = nerText.split("/");
		if (nerTextAndType.length != 2)
			return null;
	
		String nerTextOnly = nerTextAndType[0];
		String[] nerTokens = nerTextOnly.split("_");
		if (nerTokens.length == 0)
			return null;
		
		int numSentences = getSentenceCount();
		List<CorpDocumentTokenSpan> spans = new ArrayList<CorpDocumentTokenSpan>();
		for (int sentenceIndex = Math.max(0, minSentenceIndex); sentenceIndex < Math.min(numSentences, maxSentenceIndex + 1); sentenceIndex++) {
			CoreMap sentenceAnnotation = getSentenceAnnotation(sentenceIndex);
			List<CoreLabel> sentenceTokens = sentenceAnnotation.get(TokensAnnotation.class);
			for (int i = 0; i < sentenceTokens.size(); i++) {
				boolean match = true;
				for (int j = i; j < Math.min(sentenceTokens.size(), i + nerTokens.length); j++) {
					if (!sentenceTokens.get(j).originalText().equals(nerTokens[j-i])) {
						match = false;
						break;
					}
				}
				
				if (match) {
					spans.add(new CorpDocumentTokenSpan(this, sentenceIndex, i, i+nerTokens.length));
				}
			}
		}
		
		return spans;
	}
	
	@Override
	public boolean equals(Object o) {
		CorpDocument document = (CorpDocument)o;
		return this.annotationFileName.equals(document.annotationFileName);
	}
	
	@Override
	public int hashCode() {
		return this.annotationFileName.hashCode();
	}
	
	private String getAuthorCikFromFileName() {
		return this.annotationFileName.substring(0, this.annotationFileName.indexOf("-8-K-"));
	}
}
