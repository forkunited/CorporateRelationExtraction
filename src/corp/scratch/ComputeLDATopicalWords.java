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
import edu.stanford.nlp.util.Pair;

public class ComputeLDATopicalWords {
	public static void main(String[] args) {
		String name = args[0];
		double stopWordsThreshold = 0.0;
		if (args.length > 1)
			stopWordsThreshold = Double.valueOf(args[1]);
		
		OutputWriter output = new OutputWriter();
		CorpProperties properties = new CorpProperties();
		
		LDA lda = new LDA(name,
						  new File(properties.getLDASourceDirectory()), 
						  1, 
						  output);
		
		Map<String, Map<String, Double>> wordWeights = lda.loadWordWeights();
		TreeMap<Double, Pair<String, String>> entropies = new TreeMap<Double, Pair<String, String>>();
		for (Entry<String, Map<String, Double>> wordEntries : wordWeights.entrySet()) {
			output.debugWriteln("Computing entropy for word " + wordEntries.getKey());
			entropies.put(computeEntropy(wordEntries.getValue()), new Pair<String, String>(wordEntries.getKey(), argMax(wordEntries.getValue())));
		}
		
		output.debugWriteln("Outputting...");
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(properties.getLDASourceDirectory(), name + "_WordEntopies")));
			BufferedWriter sbw = null;
			if (stopWordsThreshold > 0) {
				int stopWordsPercent = (int)Math.floor(stopWordsThreshold*100);
				sbw = new BufferedWriter(new FileWriter(new File(properties.getLDASourceDirectory(), name + "_StopWords_" + stopWordsPercent)));
			}
			
			int i = 0;
			for (Entry<Double, Pair<String, String>> entry : entropies.entrySet()) {
				bw.write(entry.getValue().first() + "\t" + entry.getValue().second() + "\t" + entry.getKey() + "\n");
				if (stopWordsThreshold > 0 && i < entropies.size()*stopWordsThreshold) {
					sbw.write(entry.getValue().first() + "\n");
				}
				i++;
			}
			
			if (stopWordsThreshold > 0) {
				sbw.close();
			}
			
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static double computeEntropy(Map<String, Double> distribution) {
		double norm = 0.0;
		for (Double value : distribution.values())
			norm += value;
		
		double entropy = 0;
		for (Entry<String, Double> entry : distribution.entrySet()) {
			double p = entry.getValue()/norm;
			entropy += -p*Math.log(p);
		}
		return entropy;
	}
	
	public static String argMax(Map<String, Double> distribution) {
		double max = Double.NEGATIVE_INFINITY;
		String maxArg = null;
		for (Entry<String, Double> entry : distribution.entrySet()) {
			if (entry.getValue() > max) {
				max = entry.getValue();
				maxArg = entry.getKey();
			}
		}
		return maxArg;
	}
}
