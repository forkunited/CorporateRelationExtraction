package corp.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import corp.data.annotation.AnnotationCache;
import corp.data.annotation.CorpDocumentSet;
import corp.data.annotation.CorpRelDatum;
import corp.data.feature.CorpRelFeaturizedDataSet;
import corp.util.CorpProperties;

public class ConstructDataSample {
	private static Random rand;
	private static CorpProperties properties;
	
	public static void main(String[] args) {
		properties = new CorpProperties("corp.properties");
		
		// Hard-coded paths to places on cab for now... this is just a one-off
		int n = Integer.parseInt("1500"/*args[0]*/);
		String possibleSamplesFilePath = "/home/wmcdowel/sloan/Data/CorpRelAnnotation/Setup/examplesFromParsed.txt"/*args[1]*/;
		String currentAnnotationDir = properties.getCorpRelDirPath()/*args[2]*/;
		int seed = Integer.parseInt("1"/*args[3]*/);
		
		rand = new Random(seed);
		
		HashMap<Integer, HashSet<String>> yearsToPossibleSamples = readYearsToPossibleSamples(possibleSamplesFilePath);
		HashMap<Integer, Double> yearSamplingDistribution = getYearSamplingDistribution(yearsToPossibleSamples);
		HashSet<String> currentAnnotationFileNames = readCurrentAnnotationFileNames(currentAnnotationDir);
		
		List<String> newSamples = new ArrayList<String>();
		while (newSamples.size() < n) {
			Integer year = sample(yearSamplingDistribution);
			if (yearsToPossibleSamples.get(year).size() == 0)
				continue;
			
			String sample = uniformSample(yearsToPossibleSamples.get(year));
			yearsToPossibleSamples.get(year).remove(sample);
			
			if (currentAnnotationFileNames.contains(getFileNameFromSample(sample)))
				continue;
			
			newSamples.add(sample);
		}
		
		for (String sample : newSamples)
			System.out.println(sample);
	}
	
	private static HashMap<Integer, Double> getYearSamplingDistribution(HashMap<Integer, HashSet<String>> yearsToPossibleSamples) {
		AnnotationCache annotationCache = new AnnotationCache(
			properties.getStanfordAnnotationDirPath(),
			properties.getStanfordAnnotationCacheSize(),
			properties.getStanfordCoreMapDirPath(),
			properties.getStanfordCoreMapCacheSize()
		);
		
		CorpDocumentSet documentSet = new CorpDocumentSet(
				properties.getCorpRelDirPath(), 
				annotationCache,
				properties.getMaxThreads(),
				0
		);
		
		CorpRelFeaturizedDataSet dataSet = new CorpRelFeaturizedDataSet(documentSet);
		List<CorpRelDatum> data = dataSet.getData();
		HashMap<Integer, Integer> yearDistribution = new HashMap<Integer, Integer>();
		for (CorpRelDatum datum : data) {
			if (!yearDistribution.containsKey(datum.getDocument().getYear()))
				yearDistribution.put(datum.getDocument().getYear(), 0);
			yearDistribution.put(datum.getDocument().getYear(), yearDistribution.get(datum.getDocument().getYear()) + 1);
		}
		
		for (Entry<Integer, HashSet<String>> entry : yearsToPossibleSamples.entrySet())
			if (!yearDistribution.containsKey(entry.getKey()))
				yearDistribution.put(entry.getKey(), 0);
		
		HashMap<Integer, Double> newYearSamplingDistribution = new HashMap<Integer, Double>();
		for (Entry<Integer, Integer> e : yearDistribution.entrySet()) {
			int dataSize = data.size();
			newYearSamplingDistribution.put(e.getKey(), (data.size() - e.getValue())/((yearDistribution.size() - 1.0)*(double)dataSize));
		}
		
		return newYearSamplingDistribution;
	}
	
	private static HashMap<Integer, HashSet<String>> readYearsToPossibleSamples(String path) {
		try {
			HashMap<Integer, HashSet<String>> yearsToPossibleSamples = new HashMap<Integer, HashSet<String>>();
			BufferedReader br = new BufferedReader(new FileReader(path));
				
			String sample = br.readLine();
			int year = getYearFromSample(sample);
			if (!yearsToPossibleSamples.containsKey(year))
				yearsToPossibleSamples.put(year, new HashSet<String>());
			yearsToPossibleSamples.get(year).add(sample);
			
	        br.close();
	        
	        System.out.println("Read samples from " + yearsToPossibleSamples.size() + " years.");
	        return yearsToPossibleSamples;
	    } catch (Exception e) {
	    	System.out.println("Failed to read samples: " + e.getMessage());
	    	e.printStackTrace();
	    	return null;
	    }
	}
	
	private static int getYearFromSample(String sample) {
		String fileName = getFileNameFromSample(sample);
		int dateStartIndex = fileName.indexOf("-8-K-") + 5;
		return Integer.parseInt(fileName.substring(dateStartIndex, dateStartIndex+4));
	}
	
	private static String getFileNameFromSample(String sample) {
		String[] sampleParts = sample.split("\t"); 
		File sampleFile = new File(sampleParts[0]);
		int lineNumber = Integer.parseInt(sampleParts[3]);
		return sampleFile.getName() + ".line" + lineNumber;
	}
	
	private static HashSet<String> readCurrentAnnotationFileNames(String currentAnnotationDir) {
		HashSet<String> annotationFileNames = new HashSet<String>();
		File[] corpRelFiles = (new File(currentAnnotationDir)).listFiles();
		for (int i = 0; i < corpRelFiles.length; i++)
			annotationFileNames.add(corpRelFiles[i].getName());
		return annotationFileNames;
	}
	
	private static Integer sample(HashMap<Integer, Double> distribution) {
		double p = rand.nextDouble();
		double distributionSum = 0;
		for (Entry<Integer, Double> entry : distribution.entrySet()) {
			distributionSum += entry.getValue();
			if (p <= distributionSum)
				return entry.getKey();
		}
		
		return null;
	}
	
	private static String uniformSample(HashSet<String> possibleValues) {
		int i = 0;
		int randomIndex = rand.nextInt(possibleValues.size());
		for (String value : possibleValues) {
			if (i == randomIndex)
				return value;
			i++;
		}
		return null;
	}
}
