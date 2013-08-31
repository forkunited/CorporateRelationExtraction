package corp.data.annotation;

import java.io.BufferedReader;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import corp.util.StanfordUtil;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.CoreMap;

public class CorpDocument {
	private String stanfordFilePath;
	private Annotation cachedAnnotation;
	private List<CorpRelDatum> corpRelDatums;
	
	
	public CorpDocument(String stanfordAnnotationPath) {
		this(stanfordAnnotationPath, true);
	}

	public CorpDocument(String stanfordAnnotationPath, boolean cacheAnnotation) {
		this.stanfordFilePath = stanfordAnnotationPath;
		this.corpRelDatums = new ArrayList<CorpRelDatum>();
		
		if (cacheAnnotation) {
			this.cachedAnnotation = loadStanfordAnnotation();
		}
	}
	
	public void freeAnnotation() {
		this.cachedAnnotation = null;
	}
	
	public void cacheAnnotation() {
		this.cachedAnnotation = loadStanfordAnnotation();
	}
	
	public List<CorpRelDatum> getCorpRelDatums() {
		return this.corpRelDatums;
	}
	
	public Annotation getAnnotation() {
		if (this.cachedAnnotation != null)
			return this.cachedAnnotation;
		else
			return loadStanfordAnnotation();
	}
	
	public CoreMap getSentenceAnnotation(int sentenceIndex) {
		Annotation annotation = getAnnotation();
		List<CoreMap> sentenceAnnotations = annotation.get(SentencesAnnotation.class);
		if (sentenceIndex < sentenceAnnotations.size())
			return sentenceAnnotations.get(sentenceIndex);
		else
			return null;
	}
	
	public boolean loadCorpRelsFromFile(String path, int sentenceIndex) {
		boolean cachedAnnotation = false;
		if (this.cachedAnnotation == null) {
			cacheAnnotation();
			cachedAnnotation = true;
		}
		
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
	        		e.printStackTrace();
	        		continue;
	        	}
	        }
	        
	        br.close();
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	if (cachedAnnotation)
	    		freeAnnotation();
	    	return false;
	    }
		
		/* Create datums */
		for (Entry<String, CorpRelLabel[]> keyLabelEntry : keyToLabels.entrySet()) {
			if (!keyToTokenSpans.containsKey(keyLabelEntry.getKey()))
				continue;
			this.corpRelDatums.add(new CorpRelDatum(authorCorpName, keyToTokenSpans.get(keyLabelEntry.getKey())));
		}

		if (cachedAnnotation)
			freeAnnotation();
			
		return true;
	}
	
	private Annotation loadStanfordAnnotation() {
		return StanfordUtil.deserializeAnnotation(this.stanfordFilePath); 
	}
	
	private List<CorpDocumentTokenSpan> nerTextToTokenSpans(String nerText, int minSentenceIndex, int maxSentenceIndex) {
		String[] nerTextAndType = nerText.split("/");
		if (nerTextAndType.length != 2)
			return null;
	
		String nerTextOnly = nerTextAndType[0];
		String[] nerTokens = nerTextOnly.split("_");
		if (nerTokens.length == 0)
			return null;
		
		int numSentences = getAnnotation().get(SentencesAnnotation.class).size();
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
}
