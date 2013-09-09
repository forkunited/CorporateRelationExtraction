package corp.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
		int N_2 = Integer.parseInt("2500"/*args[0]*/);
		String possibleSamplesFilePath = "/home/wmcdowel/sloan/Data/CorpRelAnnotation/Setup/examplesFromParsed.txt"/*args[1]*/;
		String currentAnnotationDir = properties.getCorpRelDirPath()/*args[2]*/;
		int seed = Integer.parseInt("1"/*args[3]*/);
		String outputPath = "/home/wmcdowel/sloan/Data/CorpRelAnnotation/Setup/newSample.txt";
		
		rand = new Random(seed);
		
		HashMap<Integer, HashSet<String>> yearsToPossibleSamples = readYearsToPossibleSamples(possibleSamplesFilePath);
		HashMap<Integer, Double> yearSamplingDistribution = getYearSamplingDistribution(yearsToPossibleSamples, N_2);
		HashSet<String> currentAnnotationFileNames = readCurrentAnnotationFileNames(currentAnnotationDir);
		
		List<String> newSamples = new ArrayList<String>();
		while (newSamples.size() < N_2) {
			Integer year = sample(yearSamplingDistribution);
			if (yearsToPossibleSamples.get(year).size() == 0)
				continue;
			
			String sample = uniformSample(yearsToPossibleSamples.get(year));
			yearsToPossibleSamples.get(year).remove(sample);
			
			String sampleFileName = getFileNameFromSample(sample);
			System.out.println("Trying to add sample " + sampleFileName);
			if (currentAnnotationFileNames.contains(sampleFileName)) {
				System.out.println("Data already contains " + sampleFileName + ". Skipping. ");
				continue;
			}
				
			newSamples.add(sample);
		}
		
		outputSamples(outputPath, newSamples);
	}
	
	private static HashMap<Integer, Double> getYearSamplingDistribution(HashMap<Integer, HashSet<String>> yearsToPossibleSamples, int N_2) {
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
		
		double N_1 = data.size();
		double N = N_2+N_1;
		double f_2 = N_2/N;
		double k = yearDistribution.size();
		
		List<Integer> overSampledYears = new ArrayList<Integer>();
		for (Entry<Integer, Integer> e : yearDistribution.entrySet()) {
			if (e.getValue() >= N/k) {
				overSampledYears.add(e.getKey());
			}
		}
		
		k = k - overSampledYears.size();
		for (Integer overSampledYear : overSampledYears) {
			N_1 = N_1 - yearDistribution.get(overSampledYear);
			yearDistribution.remove(overSampledYear);
		}
		N = N_1+N_2;
		f_2=N_2/N;
		
		HashMap<Integer, Double> newYearSamplingDistribution = new HashMap<Integer, Double>();
		for (Entry<Integer, Integer> e : yearDistribution.entrySet()) {
			double n_i = e.getValue();
			newYearSamplingDistribution.put(e.getKey(), 1.0/(f_2*k)-n_i/(f_2*N));
		}
		
		System.out.println("Original year distribution: ");
		for (Entry<Integer, Integer> e : yearDistribution.entrySet())
			System.out.println(e.getKey() + " " + e.getValue()/(double)data.size());
		
		System.out.println("Year sampling distribution: ");
		for (Entry<Integer, Double> entry : newYearSamplingDistribution.entrySet())
			System.out.println(entry.getKey() + " " + entry.getValue());
		
		return newYearSamplingDistribution;
	}
	
	private static HashMap<Integer, HashSet<String>> readYearsToPossibleSamples(String path) {
		try {
			HashMap<Integer, HashSet<String>> yearsToPossibleSamples = new HashMap<Integer, HashSet<String>>();
			BufferedReader br = new BufferedReader(new FileReader(path));
			String sample = null;
			while ((sample = br.readLine()) != null) {
				int year = getYearFromSample(sample);
				if (!yearsToPossibleSamples.containsKey(year))
					yearsToPossibleSamples.put(year, new HashSet<String>());
				yearsToPossibleSamples.get(year).add(sample);
			}
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
		return sampleFile.getName() + ".line" + lineNumber + ".txt";
	}
	
	private static HashSet<String> readCurrentAnnotationFileNames(String currentAnnotationDir) {
		HashSet<String> annotationFileNames = new HashSet<String>();
		File[] corpRelFiles = (new File(currentAnnotationDir)).listFiles();
		for (int i = 0; i < corpRelFiles.length; i++) {
			System.out.println("Reading current annotation file name: " + corpRelFiles[i].getName());
			annotationFileNames.add(corpRelFiles[i].getName());
		}
		return annotationFileNames;
	}
	
	private static Integer sample(HashMap<Integer, Double> distribution) {
		double p = rand.nextDouble();
		double distributionSum = 0;
		for (Entry<Integer, Double> entry : distribution.entrySet()) {
			distributionSum += entry.getValue();
			if (p < distributionSum)
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
	
	private static void outputSamples(String outputPath, List<String> samples) {
        try {
    		BufferedWriter w = new BufferedWriter(new FileWriter(outputPath));
    		for (String sample : samples) {
    			w.write(sample + "\n");
    		}    		
    		
            w.close();
        } catch (IOException e) { e.printStackTrace(); }
	}
}
