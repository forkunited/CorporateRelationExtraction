package corp.scratch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import corp.data.CorpDataTools;
import corp.data.CorpMetaData;
import corp.data.annotation.AnnotationCache;
import corp.data.annotation.CorpDocument;
import corp.data.annotation.CorpDocumentSet;
import corp.data.annotation.CorpDocumentTokenSpan;
import corp.data.annotation.CorpRelDatum;
import corp.data.annotation.CorpRelLabelPath;
import corp.data.feature.CorpRelFeaturizedDataSet;
import corp.model.ModelTree;
import corp.util.CorpProperties;
import corp.util.OutputWriter;
import corp.util.StanfordUtil;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.Pair;

public class RunModelTree {
	public static void main(String[] args) {
		String modelPath = args[0];
		String outputPath = args[1];
		int maxThreads = Integer.parseInt(args[2]);
		int maxDocuments = Integer.parseInt(args[3]);
		int batchSize = Integer.parseInt(args[4]);
		
		OutputWriter output = new OutputWriter();
		CorpProperties properties = new CorpProperties("corp.properties");
		CorpDataTools dataTools = new CorpDataTools(properties, output);
		
		output.debugWriteln("Loading model...");
		
		ModelTree model = new ModelTree(modelPath, output, dataTools);
		
		output.debugWriteln("Loading documents...");
		
		AnnotationCache annotationCache = new AnnotationCache(
				properties.getStanfordAnnotationDirPath(),
				properties.getStanfordAnnotationCacheSize(),
				new OutputWriter()
		);
			
		CorpDocumentSet documentSet = new CorpDocumentSet(
				properties.getCorpRelDirPath(), 
				annotationCache,
				maxThreads,
				0,
				maxDocuments,
				new OutputWriter(),
				false,
				new CorpMetaData("Corp", properties.getCorpMetaDataPath())
		);
		
		output.debugWriteln("Running model to compute posteriors...");
		
		List<CorpDocument> docs = documentSet.getDocuments();
		List<CorpRelDatum> dataBatch = new ArrayList<CorpRelDatum>();
		int numDocs = 0;
		int numBatches = 0;
		for (CorpDocument doc : docs) {
			List<CorpRelDatum> docData = doc.loadUnannotatedCorpRels(false);
			if (docData == null)
				continue;
			dataBatch.addAll(docData);
			if (dataBatch.size() >= batchSize) {
				if (!runForBatch(model, dataBatch, maxThreads, output, outputPath)) {
					output.debugWriteln("Batch failed... exiting.");
					return;
				}
				dataBatch = new ArrayList<CorpRelDatum>();
				numBatches++;
				output.debugWriteln("Ran classifier for " + numBatches + " batches.");
			}
			numDocs++;
			output.debugWriteln("Loaded data for " + numDocs + " documents.");
		}
		
		if (!runForBatch(model, dataBatch, maxThreads, output, outputPath)) {
			output.debugWriteln("Batch failed... exiting.");
			numBatches++;
			output.debugWriteln("Ran classifier for " + numBatches + " batches.");
		}
	}
	
	private static boolean runForBatch(ModelTree model, List<CorpRelDatum> dataBatch, int maxThreads, OutputWriter output, String outputPath) {
		if (dataBatch.size() == 0)
			return true;
		CorpRelFeaturizedDataSet dataSet = new CorpRelFeaturizedDataSet(maxThreads, output);
		dataSet.addData(dataBatch);
		List<Pair<CorpRelDatum, Map<CorpRelLabelPath, Double>>> posterior = model.posterior(dataSet);
		if (posterior == null)
			return false;
		
        try {
    		BufferedWriter w = new BufferedWriter(new FileWriter(outputPath, true));  		
    		
    		for (Pair<CorpRelDatum, Map<CorpRelLabelPath, Double>> p : posterior) {
	    		JSONObject outputJSON = new JSONObject();
	    		String author = p.first().getAuthorCorpName();
	    		File annotationFile = new File(p.first().getDocument().getAnnotationPath());
	    		
	    		outputJSON.put("author", author);
	    		outputJSON.put("annotationFile", annotationFile.getName());
	    		
	    		JSONArray mentions = new JSONArray();
	    		Map<Integer, JSONObject> sentenceObjects = new HashMap<Integer, JSONObject>();
	    		Annotation documentAnnotation = p.first().getDocument().getAnnotation();
	    		List<CorpDocumentTokenSpan> tokenSpans = p.first().getOtherOrgTokenSpans();
	    		for (int i = 0; i < tokenSpans.size(); i++) {
	    			String mention = tokenSpans.get(i).toString(false);
	    			int mentionSentenceIndex = tokenSpans.get(i).getSentenceIndex();
	    			int mentionTokenStartIndex = tokenSpans.get(i).getTokenStartIndex();
	    			int mentionTokenEndIndex = tokenSpans.get(i).getTokenEndIndex();
	    			
	    			JSONObject mentionObject = new JSONObject();
	    			mentionObject.put("text", mention);
	    			mentionObject.put("sentence", mentionSentenceIndex);
	    			mentionObject.put("tokenStart", mentionTokenStartIndex);
	    			mentionObject.put("tokenEnd", mentionTokenEndIndex);
	    			mentions.add(mentionObject);
	    			
	    			if (!sentenceObjects.containsKey(mentionSentenceIndex)) {
	    				String mentionSentence = StanfordUtil.getDocumentSentenceText(documentAnnotation, mentionSentenceIndex);
	    				JSONObject sentenceObject = new JSONObject();
	    				sentenceObject.put("text", mentionSentence);
	    				sentenceObject.put("index", mentionSentenceIndex);
	    				sentenceObjects.put(mentionSentenceIndex, sentenceObject);
	    			}
	    			
	    		}
	    		
	    		JSONArray sentences = new JSONArray();
	    		sentences.addAll(sentenceObjects.values());
	    		
	    		outputJSON.put("mentions", mentions);
	    		outputJSON.put("sentences", sentences);
	    		
	    		JSONObject pObject = new JSONObject();
	    		for (Entry<CorpRelLabelPath, Double> labelP : p.second().entrySet()) {
	    			pObject.put(labelP.getKey().toString(), labelP.getValue());
	    		}
	    		outputJSON.put("p", pObject);
	    		
	    		w.write(outputJSON.toString() + "\n"); 
    		}
    		
            w.close();
            return true;
        } catch (IOException e) { e.printStackTrace(); return false; }
	}
}