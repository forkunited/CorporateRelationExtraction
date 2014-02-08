package corp.experiment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import corp.data.CorpDataTools;
import corp.data.annotation.AnnotationCache;
import corp.data.annotation.CorpDocumentSet;
import corp.data.feature.CorpRelFeaturizedDataSet;
import corp.util.CorpProperties;
import ark.util.OutputWriter;
import ark.util.SerializationUtil;
import edu.stanford.nlp.util.Pair;

/**
 * Experiment represents an experiment run on corporate press release 
 * documents, defined by a an input configuration file in the 
 * "experiments" directory.  The syntax of the input configuration
 * files is somewhat intuitive, so you can probably figure it out
 * by reading through the examples in the "experiments" directory.
 * 
 * The particular operations of the experiment are defined by the classes 
 * that extend Experiment.
 *
 * The output is generally written to debug, results, model, and data
 * files in the output directory specified in the corp.properties 
 * configuration file.
 * 
 * @author Bill McDowell
 *
 */
public abstract class Experiment {
	protected OutputWriter output;
	
	protected CorpProperties properties;
	protected int maxThreads;
	protected int maxDocuments;
	protected Random rand;
	
	protected CorpDataTools dataTools;
	
	protected AnnotationCache annotationCache;
	protected CorpRelFeaturizedDataSet dataSet;
	
	public void run(String name) {
		this.properties = new CorpProperties();
		
		List<String> inputLines = readInputFile(name);
		parseInitial(inputLines);
		initialize(name);
		parse(inputLines);
		execute(name);
		finish();	
	}
	
	protected void initialize(String name) {
		this.output = new OutputWriter(
			new File(this.properties.getExperimentOutputPath(), name + ".debug.out"),
			new File(this.properties.getExperimentOutputPath(), name + ".results.out"),
			new File(this.properties.getExperimentOutputPath(), name + ".data.out"),
			new File(this.properties.getExperimentOutputPath(), name + ".model.out")
		);
		
		this.output.debugWriteln("Loading Data Tools...");
		this.dataTools = new CorpDataTools(this.properties, this.output);
				
		this.output.debugWriteln("Loading Annotation Cache...");
		this.annotationCache = new AnnotationCache(
			this.properties.getStanfordAnnotationDirPath(),
			this.properties.getStanfordAnnotationCacheSize(),
			this.properties.getStanfordCoreMapDirPath(),
			this.properties.getStanfordCoreMapCacheSize(),
			this.output
		);
		
		this.output.debugWriteln("Loading document set...");
		CorpDocumentSet documentSet = new CorpDocumentSet(
				this.properties.getCorpRelDirPath(), 
				this.annotationCache,
				this.maxThreads,
				this.maxDocuments,
				0,
				this.output,
				this.dataTools.getMetaData("CorpMetaData")
		);
		
		this.output.debugWriteln("Loaded " + documentSet.getDocuments().size() + " documents.");
		this.output.debugWriteln("Constructing data set...");
		
		this.dataSet = new CorpRelFeaturizedDataSet(documentSet, this.output);
	}
	
	protected List<String> readInputFile(String name) {
		List<String> lines = new ArrayList<String>();
		try {
			String inputPath = new File(this.properties.getExperimentInputPath(), name + ".experiment").getAbsolutePath();
			BufferedReader br = new BufferedReader(new FileReader(inputPath));
			String line = null;
			while ((line = br.readLine()) != null) {
				lines.add(line);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return lines;
	}
	
	protected void finish() {
		this.output.close();
	}
	
	protected void parseInitial(List<String> lines) {
		for (String line : lines) {
			Pair<String, String> assignment = SerializationUtil.deserializeAssignment(line);
			
			if (assignment == null)
				continue;
			else if (assignment.first().equals("randomSeed"))
				this.rand = new Random(Integer.parseInt(assignment.second()));
			else if (assignment.first().equals("maxThreads"))
				this.maxThreads = Integer.parseInt(assignment.second());
			else if (assignment.first().equals("maxDocuments"))
				this.maxDocuments = Integer.parseInt(assignment.second());
		}
	}
	
	protected abstract void parse(List<String> lines);
	protected abstract void execute(String name);
}
