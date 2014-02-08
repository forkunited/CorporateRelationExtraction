package corp.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

import cc.mallet.types.*;
import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.*;
import cc.mallet.topics.*;

import corp.data.annotation.CorpDocument;
import corp.data.annotation.CorpDocumentSet;
import corp.data.annotation.CorpRelDatum;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import ark.util.OutputWriter;
import ark.util.StanfordUtil;
import ark.util.StringUtil;

/**
 * LDA is a wrapper class for running the Mallet implementation 
 * (http://mallet.cs.umass.edu/) of LDA on the corporate press 
 * release documents.
 * 
 * @author Bill McDowell
 *
 */
public class LDA {
	public interface DatumDocumentTransform {
		public String transform(CorpRelDatum datum);
	}

	private String name;
	private File inputFile;
	private File modelFile;
	private File topWordsFile;
	private File wordWeightsFile;
	private File stopWordsFile;
	private int maxThreads;
	private OutputWriter output;
	
	private ParallelTopicModel model;
	private TopicInferencer inferencer;
	
	private TokenSequenceRemoveStopwords stopWordsPipe;
	private Pattern tokenSequencePattern;
	
	public LDA(String name, File sourceDir, int maxThreads, OutputWriter output) {
		this.name = name;
		
		String sourceDirPath = sourceDir.getAbsolutePath();
		this.inputFile = new File(sourceDirPath, name + "_Input");
		this.modelFile = new File(sourceDirPath, name + "_Model");
		this.topWordsFile = new File(sourceDirPath, name + "_TopWords");
		this.wordWeightsFile = new File(sourceDirPath, name + "_WordWeights");
		this.stopWordsFile = new File(sourceDirPath, "stopwords.txt");
	
		this.maxThreads = maxThreads;
		this.output = output;
		
		this.stopWordsPipe = new TokenSequenceRemoveStopwords(this.stopWordsFile, "UTF-8", false, false, false);
		this.tokenSequencePattern = Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}");
	}
	
	public String getName() {
		return this.name;
	}
	
	public boolean run(CorpDocumentSet documents, DatumDocumentTransform documentFn, int seed, int numTopics, int iterations) {
		this.output.debugWriteln("Training LDA model " + this.name + "...");
		
		if (!constructInputData(documents, documentFn)) {
			this.output.debugWriteln("Error: Failed to construct LDA training data.");
			return false;
		}

        InstanceList instances = new InstanceList(constructPipes());

        try {
	        Reader fileReader = new InputStreamReader(new FileInputStream(this.inputFile), "UTF-8");
	        instances.addThruPipe(new CsvIterator (fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),
	                                               3, 2, 1)); // data, label, name fields
	
	        // Create a model with alpha_t = 0.01, beta_w = 0.01
	        //  Note that the first parameter is passed as the sum over topics, while
	        //  the second is the parameter for a single dimension of the Dirichlet prior.
	        this.model = new ParallelTopicModel(numTopics, 1.0, 0.01);
	
	        this.model.addInstances(instances);
	        this.model.setNumThreads(this.maxThreads);
	        this.model.setNumIterations(iterations);
	        this.model.setOptimizeInterval(10);
	        this.model.setRandomSeed(seed);
        	this.model.estimate();
        	this.inferencer = this.model.getInferencer();
        	this.inferencer.setRandomSeed(1);
        	
        	this.model.printTopicWordWeights(this.wordWeightsFile);
        	this.model.printTopWords(this.topWordsFile, 100, true);
        	this.model.write(this.modelFile);
        } catch (Exception e) {
        	e.printStackTrace();
        	this.output.debugWriteln("Error: Failed to train LDA model:\n " + e.getStackTrace());
        	return false;
        }
        
		this.output.debugWriteln("Finished training LDA model " + this.name + ".");
		
		return true;
	}
	
	private boolean constructInputData(CorpDocumentSet documentSet, DatumDocumentTransform documentFn) {
		if (this.inputFile.exists()) {
			this.output.debugWriteln("LDA training data already exists for " + this.name + ". Using this.");
			return true;
		}
		
		this.output.dataWriteln("LDA constructing training data...");
		
		try {
			BufferedWriter w = new BufferedWriter(new FileWriter(this.inputFile));		
			List<CorpDocument> docs = documentSet.getDocuments();
			int id = 0;
			for (CorpDocument doc : docs) {
				this.output.dataWriteln("LDA constructing training data for " + doc.getAnnotationPath() + " (" + id + ")...");
				String annotationName = (new File(doc.getAnnotationPath())).getName();
				if (documentFn != null) {
					// One document per datum
					List<CorpRelDatum> data = doc.loadUnannotatedCorpRels(false);
					if (data == null)
						continue;
					
					for (int i = 0; i < data.size(); i++) {
						String documentStr = documentFn.transform(data.get(i));
						String documentName = annotationName + "_" + id;
						
						w.write(documentName + "\tX\t" + documentStr + "\n");
						
						id++;
					}
				} else {
					// One document per document
					Annotation docAnnotation = doc.getAnnotation();
					List<CoreMap> sentences = StanfordUtil.getDocumentSentences(docAnnotation);
					StringBuilder docStr = new StringBuilder();
					for (CoreMap sentence : sentences) {
						List<String> sentenceTokens = StanfordUtil.getSentenceTokenTexts(sentence);
						for (String sentenceToken : sentenceTokens)
							docStr = docStr.append(sentenceToken).append(" ");
					}
					String cleanDocStr = StringUtil.clean(docStr.toString());
					w.write(annotationName + "\tX\t" + cleanDocStr + "\n");
				}
			}
			
			w.close();
			
			this.output.debugWriteln("LDA finished constructing training data.");
			
			return true;
	    } catch (IOException e) { 
	    	e.printStackTrace(); 
	    	return false; 
	    }
		
	}
	
	public boolean load() {
		if (this.model != null)
			return true;
		
		this.output.debugWriteln("Loading LDA model " + this.name + "...");
		
		try {
			this.model = ParallelTopicModel.read(this.modelFile);
			this.inferencer = this.model.getInferencer();
			this.inferencer.setRandomSeed(1);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		this.output.debugWriteln("Finished loading LDA model " + this.name + ".");
		
		return true;
	}
	
	public Map<String, Map<String, Double>> loadWordWeights() {
		return loadWordWeights(false);
	}
	
	public Map<String, Map<String, Double>> loadWordWeights(boolean onlyOrgs) {
		Map<String, Map<String, Double>> wordWeights = new TreeMap<String, Map<String, Double>>();
		Set<String> orgs = null;
		if (onlyOrgs)
			orgs = loadOrgs();
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(this.wordWeightsFile));
			
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] lineParts = line.split("\t");
				String topic = lineParts[0];
				String word = lineParts[1];
				if (onlyOrgs && !orgs.contains(word))
					continue;
				
				double weight = Double.parseDouble(lineParts[2]);
				if (!wordWeights.containsKey(word))
					wordWeights.put(word, new TreeMap<String, Double>());
				if (!wordWeights.get(word).containsKey(topic))
					wordWeights.get(word).put(topic, weight);
				
			}
			
			br.close();
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	return null;
	    }
		
		return wordWeights;
	}
	
	public Set<String> loadOrgs() {
		Set<String> orgs = new HashSet<String>();
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(this.inputFile));
			
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] lineParts = line.split("\t");
				String wordsPart = lineParts[2];
				String[] words = wordsPart.split("\\s+");
				
				orgs.add(words[0]);
				orgs.add(words[1]);
			}
			
			br.close();
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	return null;
	    }
		
		return orgs;
	}
	
	public int getNumTopics() {
		return this.model.getNumTopics();
	}
	
	public synchronized List<double[]> computeTopicDistributions(List<CorpRelDatum> data, DatumDocumentTransform documentFn) {
        InstanceList instances = new InstanceList(constructPipes());
        List<double[]> topicDistributions = new ArrayList<double[]>();
        for (int i = 0; i < data.size(); i++) {
        	instances.addThruPipe(new Instance(documentFn.transform(data.get(i)), null, String.valueOf(i), null));
        	double[] p = this.inferencer.getSampledDistribution(instances.get(i), 10, 1, 5);
        	topicDistributions.add(p);
        }
        
		return topicDistributions;
	}

	public synchronized double[] computeTopicDistribution(CorpRelDatum datum, DatumDocumentTransform documentFn) {
		synchronized(documentFn) { // Hack... fixes issue with stop words file opened by multiple threads
			InstanceList instances = new InstanceList(constructPipes());
			String documentStr = documentFn.transform(datum);
	        instances.addThruPipe(new Instance(documentStr, null, "0", null));
	        return this.inferencer.getSampledDistribution(instances.get(0), 10, 1, 5);
		}
	}
	
	private SerialPipes constructPipes() {
		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();
        pipeList.add( new CharSequenceLowercase() );
        pipeList.add( new CharSequence2TokenSequence(this.tokenSequencePattern) );
        pipeList.add( this.stopWordsPipe );
        pipeList.add( new TokenSequence2FeatureSequence() );
		
        SerialPipes pipes = new SerialPipes(pipeList);
        
        return pipes;
	}
}
