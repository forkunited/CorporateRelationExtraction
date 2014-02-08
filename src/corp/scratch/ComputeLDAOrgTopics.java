package corp.scratch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import ark.util.OutputWriter;
import corp.util.CorpProperties;
import corp.util.LDA;

/**
 * ComputeLDAOrgTopics takes the output of LDA (http://mallet.cs.umass.edu/),
 * and computes the most likely topic for each word.
 * 
 * @author Bill McDowell
 *
 */
public class ComputeLDAOrgTopics {
	public static void main(String[] args) {
		String name = args[0];
		OutputWriter output = new OutputWriter();
		CorpProperties properties = new CorpProperties();
		
		LDA lda = new LDA(name,
						  new File(properties.getLDASourceDirectory()), 
						  1, 
						  output);
		
		Map<String, Map<String, Double>> wordWeights = lda.loadWordWeights(true);
		
		try {
			BufferedWriter orgTopicsW = new BufferedWriter(new FileWriter(new File(properties.getLDASourceDirectory(), name + "_OrgTopics")));
			BufferedWriter orgMaxTopicsW = new BufferedWriter(new FileWriter(new File(properties.getLDASourceDirectory(), name + "_MaxOrgTopics")));
			for (Entry<String, Map<String, Double>> entry : wordWeights.entrySet()) {
				Map<String, Double> normalizedDist = normalizeDistribution(entry.getValue());
				double maxTopicValue = 0;
				String maxTopic = null;
				orgTopicsW.write(entry.getKey() + "\t");
				for (Entry<String, Double> weightEntry : normalizedDist.entrySet()) {
					orgTopicsW.write(weightEntry.getKey() + ": " + weightEntry.getValue() + "\t");
					if (weightEntry.getValue() > maxTopicValue) {
						maxTopicValue = weightEntry.getValue();
						maxTopic = weightEntry.getKey();
					}
				}
				orgTopicsW.write("\n");
			
				orgMaxTopicsW.write(entry.getKey() + "\t" + maxTopic + "\t" + maxTopicValue + "\n");
			}
			
			orgTopicsW.close();
			orgMaxTopicsW.close();
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	private static Map<String, Double> normalizeDistribution(Map<String, Double> distribution) {
		Map<String, Double> normDist = new TreeMap<String, Double>();
		double norm = 0;
		for (Double value : distribution.values())
			norm += value;
		for (Entry<String, Double> entry : distribution.entrySet())
			normDist.put(entry.getKey(), entry.getValue()/norm);
		
		return normDist;
		
	}
}
