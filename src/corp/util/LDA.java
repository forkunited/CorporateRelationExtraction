package corp.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import cc.mallet.types.*;
import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.*;
import cc.mallet.topics.*;

import corp.data.annotation.CorpDocument;
import corp.data.annotation.CorpDocumentSet;
import corp.data.annotation.CorpRelDatum;
import ark.util.OutputWriter;

public class LDA {
	public interface DatumDocumentTransform {
		public String transform(CorpRelDatum datum);
	}
	
	private String name;
	private File inputFile;
	private File modelFile;
	private File stateFile;
	private File topWordsFile;
	private File wordWeightsFile;
	private File stopWordsFile;
	private int maxThreads;
	private OutputWriter output;
	
	private ParallelTopicModel model;
	private SerialPipes pipes;
	private TopicInferencer inferencer;
	
	public LDA(String name, File sourceDir, int maxThreads, OutputWriter output) {
		this.name = name;
		
		String sourceDirPath = sourceDir.getAbsolutePath();
		this.inputFile = new File(sourceDirPath, name + "_Input");
		this.modelFile = new File(sourceDirPath, name + "_Model");
		this.stateFile = new File(sourceDirPath, name + "_State");
		this.topWordsFile = new File(sourceDirPath, name + "_TopWords");
		this.wordWeightsFile = new File(sourceDirPath, name + "_WordWeights");
		this.stopWordsFile = new File(sourceDirPath, "stopwords.txt");
	
		this.maxThreads = maxThreads;
		this.output = output;
		
        // Begin by importing documents from text to feature sequences
        ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

        // Pipes: lowercase, tokenize, remove stopwords, map to features
        pipeList.add( new CharSequenceLowercase() );
        pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );
        pipeList.add( new TokenSequenceRemoveStopwords(this.stopWordsFile, "UTF-8", false, false, false) );
        pipeList.add( new TokenSequence2FeatureSequence() );
		
		this.pipes = new SerialPipes(pipeList);
	}
	
	public boolean run(CorpDocumentSet documents, DatumDocumentTransform documentFn, int seed, int numTopics, int iterations) {
		this.output.debugWriteln("Training LDA model " + this.name + "...");
		
		if (!constructInputData(documents, documentFn)) {
			this.output.debugWriteln("Error: Failed to construct LDA training data.");
			return false;
		}

        InstanceList instances = new InstanceList (this.pipes);

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
	        
	        this.model.setRandomSeed(seed);
	        this.model.setSaveSerializedModel(10, this.modelFile.getAbsolutePath());
	        this.model.setSaveState(10, this.stateFile.getAbsolutePath());
        
        	this.model.estimate();
        	this.inferencer = this.model.getInferencer();
        	
        	this.model.printTopicWordWeights(this.wordWeightsFile);
        	this.model.printTopWords(this.topWordsFile, 20, true);
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
				List<CorpRelDatum> data = doc.loadUnannotatedCorpRels(false);
				if (data == null)
					continue;
				String annotationName = (new File(doc.getAnnotationPath())).getName();
				
				for (int i = 0; i < data.size(); i++) {
					String documentStr = documentFn.transform(data.get(i));
					String documentName = annotationName + "_" + id;
					
					w.write(documentName + "\tX\t" + documentStr + "\n");
					
					id++;
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
		this.output.debugWriteln("Loading LDA model " + this.name + "...");
		
		try {
			this.model = ParallelTopicModel.read(this.modelFile);
			this.inferencer = this.model.getInferencer();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		this.output.debugWriteln("Finished loading LDA model " + this.name + ".");
		
		return true;
	}
	
	public List<double[]> computeTopicDistributions(List<CorpRelDatum> data, DatumDocumentTransform documentFn) {
        InstanceList instances = new InstanceList(this.pipes);
        List<double[]> topicDistributions = new ArrayList<double[]>();
        for (int i = 0; i < data.size(); i++) {
        	instances.addThruPipe(new Instance(documentFn.transform(data.get(i)), null, String.valueOf(i), null));
        	double[] p = this.inferencer.getSampledDistribution(instances.get(i), 10, 1, 5);
        	topicDistributions.add(p);
        }
        
		return topicDistributions;
	}

	public double[] computeTopicDistribution(CorpRelDatum datum, DatumDocumentTransform documentFn) {
		InstanceList instances = new InstanceList(this.pipes);
        instances.addThruPipe(new Instance(documentFn.transform(datum), null, "0", null));
        return this.inferencer.getSampledDistribution(instances.get(0), 10, 1, 5);
	}
}
