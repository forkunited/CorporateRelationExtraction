package corp.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class BrownClusterer {
	private String name;
	private File sourceDocument;
	private int numClusters;
	private String cmdPath;
	private OutputWriter output;
	
	private String sourceName;
	private File clusterDocument;
	
	private Map<String, String> wordsToClusters;
	
	public BrownClusterer(String name, String cmdPath, File sourceDocument, int numClusters, OutputWriter output) {
		this.name = name;
		this.sourceDocument = sourceDocument;
		this.numClusters = numClusters;
		this.cmdPath = cmdPath;
		this.output = output;
		
		this.sourceName = sourceDocument.getName().split(".")[0];
		this.clusterDocument = new File(this.sourceDocument.getParentFile().getAbsolutePath(), 
										this.sourceName + "-c" + this.numClusters + "-p1.out/paths");
		this.wordsToClusters = null;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getCluster(String word) {
		if (this.wordsToClusters == null)
			if (!loadWordsToClusters())
				return null;	
		if (!this.wordsToClusters.containsKey(word))
			return null;
		return this.wordsToClusters.get(word);
	}
	
	public Map<String, String> getClusterMap() {
		if (this.wordsToClusters == null)
			if (!loadWordsToClusters())
				return null;
		
		return this.wordsToClusters;
	}
	
	private boolean loadWordsToClusters() {
		if (!this.clusterDocument.exists())
			if (!runClustering())
				return false;
		
		if (!this.clusterDocument.exists())
			return false;
		
		this.wordsToClusters = new HashMap<String, String>();
		
		this.output.debugWriteln("Loading clusters for Brown clusterer " + this.name + "...");
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(this.clusterDocument));
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] lineParts = line.split("\t");
				if (lineParts.length < 3) {
					br.close();
					this.wordsToClusters = null;
					return false;
				}
				String cluster = lineParts[0];
				String word = lineParts[1];
				this.wordsToClusters.put(word, cluster);
			}
			br.close();
		} catch (Exception e) {
			this.wordsToClusters = null;
			e.printStackTrace();
			return false;
		}
		
		this.output.debugWriteln("Loaded " + this.wordsToClusters.size() + " clusters for Brown clusterer " + this.name + ".");
		
		return true;
	}
	
	private boolean runClustering() {
		String clusteringCmd = this.cmdPath + " --text " + this.sourceDocument.getAbsolutePath() + " --c " + this.numClusters;
		clusteringCmd = clusteringCmd.replace("\\", "/");
		
		this.output.debugWriteln("Running Brown clustering " + this.name + "...");
		
		if (!CommandRunner.run(clusteringCmd, this.sourceDocument.getParentFile()))
			return false;
		
		this.output.debugWriteln("Finished running Brown clustering.");
		
		return true;
	}
}
