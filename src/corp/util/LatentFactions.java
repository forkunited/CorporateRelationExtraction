package corp.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import corp.data.annotation.CorpDocumentTokenSpan;
import corp.data.annotation.CorpRelDatum;
import corp.data.feature.CorpRelFeature;
import corp.data.feature.CorpRelFeaturizedDataSet;

public class LatentFactions {
	private File inputDir;
	private File settingsFile;
	private File wordDistributionsFile;
	private File factionDistributionsFile;
	private File tempOutputDir;
	private File authorsFile;
	private File vocabularyFile;
	private File citationsFile;
	private File coauthorshipFile;
	
	private StringUtil.StringTransform authorKeyFn;
	private String name;
	private String cmdPath;
	private int maxThreads;
	private int numFactions;
	private int iterations;
	private OutputWriter output;
	private CorpRelFeaturizedDataSet data;
	
	private Map<String, double[]> factionDistributions;
	private Map<String, Double>[][] wordDistributions;
	
	public LatentFactions(String name, String cmdPath, File sourceDir, int maxThreads, int numFactions, int iterations, StringUtil.StringTransform authorKeyFn, OutputWriter output) {
		this.name = name;
		this.cmdPath = cmdPath;
		this.maxThreads = maxThreads;
		this.numFactions = numFactions;
		this.iterations = iterations;
		this.authorKeyFn = authorKeyFn;
		this.output = output;
		
		this.inputDir = new File(sourceDir.getAbsolutePath(), "Input/" + this.name + "/");
		this.settingsFile = new File(sourceDir.getAbsolutePath(), "Input/" + this.name + "/" + this.name + ".settings");
		this.authorsFile = new File(sourceDir.getAbsolutePath(), "Input/" + this.name + "/" + this.name + "_authors.txt");
		this.vocabularyFile = new File(sourceDir.getAbsolutePath(), "Input/" + this.name + "/" + this.name + "_vocabulary.txt");
		this.citationsFile = new File(sourceDir.getAbsolutePath(), "Input/" + this.name + "/" + this.name + "_citations.txt");
		this.coauthorshipFile = new File(sourceDir.getAbsolutePath(), "Input/" + this.name + "/" + this.name + "_coauthorship.txt");
		
		this.wordDistributionsFile = new File(sourceDir.getAbsolutePath(), "Output/" + this.name + "_words");
		this.factionDistributionsFile = new File(sourceDir.getAbsolutePath(), "Output/" + this.name + "_factions");
		this.tempOutputDir = new File(sourceDir, "OutputTemp/" + this.name + "/");
	}
	
	public boolean setData(CorpRelFeaturizedDataSet data) {
		this.data = data;
		return true;
	}
	
	public String getName() {
		return this.name;
	}
	
	public synchronized int getFaction(String author) {
		double[] factionDistribution = getFactionDistribution(this.authorKeyFn.transform(author));
		int maxFaction = -1;
		Double maxValue = Double.NEGATIVE_INFINITY;

		for (int i = 0; i < factionDistribution.length; i++) {
			if (factionDistribution[i] > maxValue) {
				maxValue = factionDistribution[i];
				maxFaction = i;
			}
		}
		
		return maxFaction;
	}
	
	public synchronized double[] getFactionDistribution(String author) {
		if (this.factionDistributions == null)
			if (!load())
				return null;
		
		String authorKey = this.authorKeyFn.transform(author);
		if (!this.factionDistributions.containsKey(authorKey))
			return null;
		
		return this.factionDistributions.get(authorKey);
	}
	
	public synchronized Map<String, Double> getWordDistribution(int firstFaction, int secondFaction) {
		if (this.wordDistributions == null)
			if (!load())
				return null;
		
		if (this.wordDistributions.length <= firstFaction || this.wordDistributions[firstFaction].length <= secondFaction)
			return null;
		
		return this.wordDistributions[firstFaction][secondFaction];
	}
	
	@SuppressWarnings("unchecked")
	public boolean load() {		
		if (!this.wordDistributionsFile.exists() || !this.factionDistributionsFile.exists())
			if (!run())
				return false;
		
		if (!this.wordDistributionsFile.exists() || !this.factionDistributionsFile.exists())
			return false;
		
		this.factionDistributions = new HashMap<String, double[]>();
		this.wordDistributions = new Map[this.numFactions][this.numFactions];
		for (int i = 0; i < this.numFactions; i++) {
			for (int j = 0; j < this.numFactions; j++) {
				this.wordDistributions[i][j] = new HashMap<String, Double>();
			}
		}
		
		this.output.debugWriteln("Loading factions for " + this.name + "...");
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(this.wordDistributionsFile));
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] lineParts = line.split("\t");
				if (lineParts.length < 3) {
					br.close();
					this.wordDistributions = null;
					this.factionDistributions = null;
					return false;
				}
				
				int firstFaction = Integer.parseInt(lineParts[0]);
				int secondFaction = Integer.parseInt(lineParts[1]);
				String[] wordValues = lineParts[2].split(" ");
				Map<String, Double> wordDistribution = new HashMap<String, Double>();
				for (String wordValue : wordValues) {
					String[] wordValueParts = wordValue.split(":");
					if (wordValueParts.length < 2) {
						br.close();
						this.wordDistributions = null;
						this.factionDistributions = null;
						return false;			
					}
					
					String word = wordValueParts[0];
					double value = Double.parseDouble(wordValueParts[1]);
					wordDistribution.put(word, value);
				}
				
				this.wordDistributions[firstFaction][secondFaction] = wordDistribution;
			}
			br.close();
			
			Map<Integer, String> authors = loadAuthors();
			
			br = new BufferedReader(new FileReader(this.factionDistributionsFile));
			line = null;
			while ((line = br.readLine()) != null) {
				String[] lineParts = line.split("\t");
				if (lineParts.length < 2) {
					br.close();
					this.wordDistributions = null;
					this.factionDistributions = null;
					return false;
				}
				
				int authorIndex = Integer.parseInt(lineParts[0]);
				String[] distributionValues = lineParts[1].split(" ");
				double[] factionDistribution = new double[this.numFactions];
				for (int i = 0; i < distributionValues.length; i++) {
					factionDistribution[i] = Double.parseDouble(distributionValues[i]);
				}
				
				this.factionDistributions.put(authors.get(authorIndex), factionDistribution);
			}
			br.close();
		
		} catch (Exception e) {
			this.factionDistributions = null;
			this.wordDistributions = null;
			e.printStackTrace();
			return false;
		}
		
		this.output.debugWriteln("Loaded factions for " + this.name + ".");
		
		return true;
	}
	
	private Map<Integer, String> loadAuthors() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(this.authorsFile));
			String line = null;
			
			line = br.readLine(); // Read heading line
			Map<Integer, String> authors = new HashMap<Integer, String>();
			while ((line = br.readLine()) != null) {
				String[] lineParts = line.split("\t");
				if (lineParts.length < 2) {
					br.close();
					throw new IllegalArgumentException("Invalid authors file.");
				}
				authors.put(Integer.parseInt(lineParts[0]), lineParts[1]);
			}
			
			br.close();
			return authors;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private boolean run() {
		this.output.debugWriteln("Outputting input data for latent factions " + this.name + "...");
		
		if (!this.tempOutputDir.exists() && !this.tempOutputDir.mkdirs())
			return false;
		if (!this.inputDir.exists() && !this.inputDir.mkdirs())
			return false;
		
		ConcurrentHashMap<String, Integer> vocabulary = new ConcurrentHashMap<String, Integer>();
		ConcurrentHashMap<String, Map<String, Map<String, Integer>>> citations = new ConcurrentHashMap<String, Map<String, Map<String, Integer>>>();
		ConcurrentHashMap<String, Integer> authors = new ConcurrentHashMap<String, Integer>();
		
		if (!extractCorpData(vocabulary, citations, authors))
			return false;
		if (!outputSettings())
			return false;
		if (!outputAuthors(authors))
			return false;
		if (!outputVocabulary(vocabulary))
			return false;
		if (!outputCitations(citations, authors))
			return false;
		if (!outputCoauthorships())
			return false;
		
		String factionCmd = this.cmdPath + " -s " + this.settingsFile.getAbsolutePath();
		factionCmd = factionCmd.replace("\\", "/");
		
		this.output.debugWriteln("Running latent factions " + this.name + "...");
		
		if (!CommandRunner.run(factionCmd))
			return false;
		
		this.output.debugWriteln("Finished running latent factions.");
		
		if (!transferTempOutput())
			return false;

		this.output.debugWriteln("Transferred latent factions output.");
		
		return true;
	}
	
	private boolean extractCorpData(ConcurrentHashMap<String, Integer> vocabulary, 
									ConcurrentHashMap<String, Map<String, Map<String, Integer>>> citations,
									ConcurrentHashMap<String, Integer> authors) {
		List<CorpRelDatum> data = this.data.getData();
		AtomicInteger maxAuthorId = new AtomicInteger(0);
		ExecutorService threadPool = Executors.newFixedThreadPool(this.maxThreads);
		for (int i = 0; i < data.size(); i++) {
			threadPool.submit(new ExtractCorpDataThread(vocabulary, citations, authors, data.get(i), maxAuthorId));
		}
		
		try {
			threadPool.shutdown();
			threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
	
	private class ExtractCorpDataThread implements Runnable {
		private ConcurrentHashMap<String, Integer> vocabulary;
		private ConcurrentHashMap<String, Map<String, Map<String, Integer>>> citations;
		private ConcurrentHashMap<String, Integer> authors;
		private CorpRelDatum datum;
		private AtomicInteger maxAuthorId;
		
		public ExtractCorpDataThread(ConcurrentHashMap<String, Integer> vocabulary, 
									 ConcurrentHashMap<String, Map<String, Map<String, Integer>>> citations,
									 ConcurrentHashMap<String, Integer> authors,
									 CorpRelDatum datum,
									 AtomicInteger maxAuthorId) {
			this.vocabulary = vocabulary;
			this.citations = citations;
			this.authors = authors;
			this.datum = datum;
			this.maxAuthorId = maxAuthorId;
		}
		
		public void run() {
			
			String author = authorKeyFn.transform(this.datum.getAuthorCorpName());
			synchronized (this.authors) {
				if (!this.authors.containsKey(author)) {
					this.authors.put(author, this.maxAuthorId.incrementAndGet());
				}
			}
			
			List<CorpRelFeature> features = data.getFeatures();
			for (CorpRelFeature feature : features) {
				Map<String, Double> featureValues = feature.computeMapNoInit(this.datum);
				for (Entry<String, Double> featureValue : featureValues.entrySet()) {
					synchronized (this.vocabulary) {
						if (!this.vocabulary.containsKey(featureValue.getKey())) {
							this.vocabulary.put(featureValue.getKey(), (int)Math.round(featureValue.getValue()));
						} else {
							this.vocabulary.put(featureValue.getKey(), 
								this.vocabulary.get(featureValue.getKey()) + (int)Math.round(featureValue.getValue()));
						}
					}
				}
				
				List<CorpDocumentTokenSpan> mentions = datum.getOtherOrgTokenSpans();
				for (CorpDocumentTokenSpan mention : mentions) {
					String mentionedAuthor = authorKeyFn.transform(mention.toString());
					synchronized (this.authors) {
						if (!this.authors.containsKey(mentionedAuthor))
							this.authors.put(mentionedAuthor, this.maxAuthorId.incrementAndGet());
					}
					
					synchronized (this.citations) {
						if (!this.citations.containsKey(author))
							this.citations.put(author, new HashMap<String, Map<String, Integer>>());
						if (!this.citations.get(author).containsKey(mentionedAuthor))
							this.citations.get(author).put(mentionedAuthor, new HashMap<String, Integer>());
						for (Entry<String, Double> featureValue : featureValues.entrySet()) {
							if (!this.citations.get(author).get(mentionedAuthor).containsKey(featureValue.getKey()))
								this.citations.get(author).get(mentionedAuthor).put(featureValue.getKey(), (int)Math.round(featureValue.getValue()));
							else {
								this.citations.get(author).get(mentionedAuthor).put(featureValue.getKey(),
									this.citations.get(author).get(mentionedAuthor).get(featureValue.getKey()) + (int)Math.round(featureValue.getValue()));
							}
						}
					}
				}
			}
		}
	}
	
	
	private boolean outputAuthors(Map<String, Integer> authors) {
		this.output.debugWriteln("Outputting authors...");
		
		try {
			BufferedWriter w = new BufferedWriter(new FileWriter(this.authorsFile));
			w.write("#author_id\tauthor_name\tauthor_affiliations|...\n");
			
			for (Entry<String, Integer> entry : authors.entrySet()) {
				w.write(entry.getValue() + "\t" + entry.getKey() + "\t\n");
			}
			
			w.close();
	        return true;
	    } catch (IOException e) { e.printStackTrace(); return false; }
	}
	
	private boolean outputVocabulary(Map<String, Integer> vocabulary) {
		this.output.debugWriteln("Outputting vocabulary...");
		try {
			BufferedWriter w = new BufferedWriter(new FileWriter(this.vocabularyFile));
			
			w.write("#word\tcounts\n");
			
			for (Entry<String, Integer> entry : vocabulary.entrySet()) {
				w.write(entry.getKey() + "\t" + entry.getValue() + "\t\n");
			}
			
			w.close();
	        return true;
	    } catch (IOException e) { e.printStackTrace(); return false; }
	}
	
	private boolean outputCitations(Map<String, Map<String, Map<String, Integer>>> citations, Map<String, Integer> authors) {
		this.output.debugWriteln("Outputting citations...");
		try {
			BufferedWriter w = new BufferedWriter(new FileWriter(this.citationsFile));
			
			w.write("#citer\tcitee\tword counts\n");
			
			for (Entry<String, Map<String, Map<String, Integer>>> authorEntry : citations.entrySet()) {
				Integer authorIndex = authors.get(authorEntry.getKey());
				for (Entry<String, Map<String, Integer>> mentionedEntry : authorEntry.getValue().entrySet()) {
					Integer mentionedIndex = authors.get(mentionedEntry.getKey());
					w.write(authorIndex + "\t" + mentionedIndex + "\t");
					StringBuilder wordDistStr = new StringBuilder();
					for (Entry<String, Integer> wordEntry : mentionedEntry.getValue().entrySet()) {
						wordDistStr.append(wordEntry.getKey()).append(":").append(wordEntry.getValue()).append(" ");
					}
					w.write(wordDistStr.toString().trim() + "\n");
				}
			}
			
			w.close();
	        return true;
	    } catch (IOException e) { e.printStackTrace(); return false; }
	}
	
	private boolean outputCoauthorships() {
		this.output.debugWriteln("Outputting coauthorships...");
		try {
			BufferedWriter w = new BufferedWriter(new FileWriter(this.coauthorshipFile));
			
			/* None for now... */
			
			w.close();
	        return true;
	    } catch (IOException e) { e.printStackTrace(); return false; }
	}
	
	private boolean transferTempOutput() {
		String iterationStr = String.valueOf(this.iterations);
		for (int i = iterationStr.length();  i < 5; i++)
			iterationStr = "0" + iterationStr;
		
		String factionSourceFileName = iterationStr + "_gamma.txt";
		String wordSourceFileName = iterationStr + "_sage.txt";
		
		try {
			Files.copy(new File(this.tempOutputDir.getAbsolutePath(), factionSourceFileName).toPath(), 
					 	this.factionDistributionsFile.toPath());
			Files.copy(new File(this.tempOutputDir.getAbsolutePath(), wordSourceFileName).toPath(), 
				 	this.wordDistributionsFile.toPath());
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private boolean outputSettings() {
		
        try {
    		BufferedWriter w = new BufferedWriter(new FileWriter(this.settingsFile));
    		
    		w.write("output = " + this.tempOutputDir.getAbsolutePath() + "\n");
    		w.write("authors-db = " + this.authorsFile.getAbsolutePath() + "\n");
    		w.write("vocabulary = " + this.vocabularyFile.getAbsolutePath() + "\n");
    		w.write("citations-db = " + this.citationsFile.getAbsolutePath() + "\n");
    		w.write("coauthorships = " + this.coauthorshipFile.getAbsolutePath() + "\n");
    		w.write("G = " + this.numFactions + "\n");
            w.write("model = gibbs+factions\n");
            w.write("uniform-background-words = false\n");
            w.write("alpha = 1\n");
            w.write("update-alpha = true\n");
            w.write("sage-l1penalty = 5\n");
            w.write("save-intervals = 10\n");
	        w.write("save-likelihood = 5\n");
	        w.write("update-interval = 10\n");
	        w.write("e-steps = 3\n");
	        w.write("m-steps = 10\n");
	        w.write("iterations = " + this.iterations + "\n");
	        w.write("gibbs-burnin = 200\n");
	        w.write("annealing = 1:1.2 300:1\n");
	        w.write("reset-samples = false\n");
	        w.write("likelihood-threads = 4\n");
			w.write("e-step-threads = 4\n");
			w.write("m-step-threads = 4\n");
            
    		w.close();
            return true;
        } catch (IOException e) { e.printStackTrace(); return false; }
	}
}
