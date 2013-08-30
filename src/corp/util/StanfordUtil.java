package corp.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;

/**
 * Wrappers for making Stanford CoreNLP easier to use.  There are static wrapper methods for the following purposes:
 * 	- Calling CorenNLP functions which require specifying a class to extract from a deserialized object
 * 		- This is useful because it's difficult to remember all of the classes, and they don't auto-complete
 *	- Serializing and deserializing document annotations
 * @author Bill
 *
 */
public class StanfordUtil {
	/* Wrapper functions */
	
	public static Map<Integer, CorefChain> getDocumentCorefChain(Annotation documentAnnotation) {
		return documentAnnotation.get(CorefChainAnnotation.class);
	}
	
	public static List<CoreMap> getDocumentSentences(Annotation documentAnnotation) {
		return documentAnnotation.get(SentencesAnnotation.class);
	}
	
	public static CoreMap getDocumentSentence(Annotation documentAnnotation, int sentenceIndex) {
		return StanfordUtil.getDocumentSentences(documentAnnotation).get(sentenceIndex);
	}
	
	public static List<CoreLabel> getDocumentSentenceTokens(Annotation documentAnnotation, int sentenceIndex) {
		return StanfordUtil.getSentenceTokens(StanfordUtil.getDocumentSentences(documentAnnotation).get(sentenceIndex));
	}
	
	public static List<String> getDocumentSentenceTokenTexts(Annotation documentAnnotation, int sentenceIndex) {
		List<CoreLabel> tokens = StanfordUtil.getDocumentSentenceTokens(documentAnnotation, sentenceIndex);
		List<String> tokenTexts = new ArrayList<String>();
		for (CoreLabel token : tokens)
			tokenTexts.add(StanfordUtil.getTokenText(token));
		return tokenTexts;
	}
	
	public static List<String> getDocumentSentencePartsOfSpeech(Annotation documentAnnotation, int sentenceIndex) {
		List<CoreLabel> tokens = StanfordUtil.getDocumentSentenceTokens(documentAnnotation, sentenceIndex);
		List<String> tokenPartsOfSpeech = new ArrayList<String>();
		for (CoreLabel token : tokens)
			tokenPartsOfSpeech.add(StanfordUtil.getTokenPartOfSpeech(token));
		return tokenPartsOfSpeech;
	}
	
	public static List<String> getDocumentSentenceNamedEntityTags(Annotation documentAnnotation, int sentenceIndex) {
		List<CoreLabel> tokens = StanfordUtil.getDocumentSentenceTokens(documentAnnotation, sentenceIndex);
		List<String> tokenNamedEntityTags = new ArrayList<String>();
		for (CoreLabel token : tokens)
			tokenNamedEntityTags.add(StanfordUtil.getTokenNamedEntityTag(token));
		return tokenNamedEntityTags;
	}
	
	public static Tree getDocumentSentenceParseTree(Annotation documentAnnotation, int sentenceIndex) {
		return StanfordUtil.getSentenceParseTree(StanfordUtil.getDocumentSentences(documentAnnotation).get(sentenceIndex));
	}
	
	public static SemanticGraph getSentenceDependencyGraph(CoreMap sentenceAnnotation) {
		return sentenceAnnotation.get(CollapsedCCProcessedDependenciesAnnotation.class);
	}
	
	public static Tree getSentenceParseTree(CoreMap sentenceAnnotation) {
		return sentenceAnnotation.get(TreeAnnotation.class);
	}
	
	public static List<CoreLabel> getSentenceTokens(CoreMap sentenceAnnotation) {
		return sentenceAnnotation.get(TokensAnnotation.class);
	}
	
	public static List<String> getTokensNGramTexts(List<CoreLabel> tokens, int startIndex, int n) {
		List<String> ngram = new ArrayList<String>();
		for (int i = startIndex; i < startIndex + n; i++) {
			ngram.add(StanfordUtil.getTokenText(tokens.get(i)));
		}
		return ngram;
	}
	
	public static String getTokenText(CoreLabel tokenAnnotation) {
		return tokenAnnotation.get(TextAnnotation.class);
	}
	
	public static String getTokenPartOfSpeech(CoreLabel tokenAnnotation) {
		return tokenAnnotation.get(PartOfSpeechAnnotation.class);
	}
	
	public static String getTokenNamedEntityTag(CoreLabel tokenAnnotation) {
		return tokenAnnotation.get(NamedEntityTagAnnotation.class);
	}
	
	/* Serialization */
	
	public static boolean serializeAnnotation(String path, Annotation annotation){
		try {
			ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(path));
			stream.writeObject(annotation);
			stream.flush();
			stream.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static Annotation deserializeAnnotation(String path){
		try {
			ObjectInputStream stream = new ObjectInputStream(new FileInputStream(path));
			Annotation annotation = (Annotation)stream.readObject();
			stream.close();
			return annotation;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
