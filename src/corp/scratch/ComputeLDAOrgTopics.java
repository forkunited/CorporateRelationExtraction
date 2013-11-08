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
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(properties.getLDASourceDirectory(), name + "_OrgTopics")));
			
			for (Entry<String, Map<String, Double>> entry : wordWeights.entrySet()) {
				Map<String, Double> normalizedDist = normalizeDistribution(entry.getValue());
				
				bw.write(entry.getKey() + "\t");
				for (Entry<String, Double> weightEntry : normalizedDist.entrySet())
					bw.write(weightEntry.getKey() + ": " + weightEntry.getValue() + "\t");
				bw.write("\n");
			}
			
			bw.close();
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
