package corp.data.feature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import corp.data.annotation.CorpDocumentSet;
import corp.data.annotation.CorpRelDataSet;
import corp.data.annotation.CorpRelDatum;
import corp.data.annotation.CorpRelLabel;

public class CorpRelFeaturizedDataSet extends CorpRelDataSet {
	private List<CorpRelFeature> features;
	private int maxThreads;
	
	public CorpRelFeaturizedDataSet() {
		this(1);
	}
	
	public CorpRelFeaturizedDataSet(int maxThreads) {
		this(new ArrayList<CorpRelFeature>(), maxThreads);
	}
	
	public CorpRelFeaturizedDataSet(List<CorpRelFeature> features, int maxThreads) {
		super();
		this.features = features;
		this.maxThreads = maxThreads;
	}
	
	public CorpRelFeaturizedDataSet(CorpDocumentSet sourceDocuments) {
		this(sourceDocuments, new ArrayList<CorpRelFeature>(), 1);
	}
	
	public CorpRelFeaturizedDataSet(CorpDocumentSet sourceDocuments, List<CorpRelFeature> features, int maxThreads) {
		super(sourceDocuments);
		this.features = features;
		this.maxThreads = maxThreads;
	}
	
	public boolean addFeature(CorpRelFeature feature) {
		return this.features.add(feature);
	}
	
	public boolean removeFeature(CorpRelFeature feature) {
		return this.features.remove(feature);
	}
	
	public List<String> getFeatureNames() {
		List<String> featureNames = new ArrayList<String>();
		for (CorpRelFeature feature : this.features)
			feature.getNames(featureNames);
		return featureNames;
	}
	
	public CorpRelFeature getFeature(int index) {
		return this.features.get(index);
	}
	
	public List<CorpRelFeature> getFeatures() {
		return this.features;
	}
	
	public int getFeatureCount() {
		return this.features.size();
	}
	
	public List<CorpRelFeaturizedDatum> getFeaturizedLabeledData() {
		return featurize(getLabeledData());
	}
	
	public List<CorpRelFeaturizedDatum> getFeaturizedUnlabeledData() {
		return featurize(getUnlabeledData());
	}
	
	public List<CorpRelFeaturizedDatum> getFeaturizedData() {
		return featurize(getData());
	}
	
	public List<CorpRelFeaturizedDatum> getFeaturizedDataUnderLabel(CorpRelLabel root, boolean includeRoot) {
		return featurize(getDataUnderLabel(root, includeRoot));
	}
	
	public List<CorpRelFeaturizedDatum> getFeaturizedDataInLabel(CorpRelLabel label) {
		return featurize(getDataInLabel(label));
	}
	
	private List<CorpRelFeaturizedDatum> featurize(List<CorpRelDatum> data) {
		System.out.println("Featurizing data set...");
		
		CorpRelFeaturizedDatum[] featurizedData = new CorpRelFeaturizedDatum[data.size()];
		
		ExecutorService threadPool = Executors.newFixedThreadPool(1);//this.maxThreads);
		
		for (int i = 0; i < data.size(); i++) {
			threadPool.submit(new FeaturizeDatumThread(this, featurizedData, data.get(i), i));
		}
		
		try {
			threadPool.shutdown();
			threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return new ArrayList<CorpRelFeaturizedDatum>(Arrays.asList(featurizedData));
	}
	
	private class FeaturizeDatumThread implements Runnable {
		private CorpRelFeaturizedDataSet dataSet;
		private CorpRelFeaturizedDatum[] featurizedDatums;
		private CorpRelDatum datum;
		private int index;
		
		public FeaturizeDatumThread(CorpRelFeaturizedDataSet dataSet, CorpRelFeaturizedDatum[] featurizedDatums, CorpRelDatum datum, int index) {
			this.dataSet = dataSet;
			this.featurizedDatums = featurizedDatums;
			this.datum = datum;
			this.index = index;
		}
		
		@Override
		public void run() {
			List<Double> featureValues = new ArrayList<Double>();
			for (CorpRelFeature feature : features) {
				long start = System.currentTimeMillis();
				featureValues = feature.computeVector(datum, featureValues);
				long end = System.currentTimeMillis();
				System.out.println("Computed feature " + feature.getNames().get(0) + " in " + (end - start)+ "ms");
			}
			this.featurizedDatums[this.index] = new CorpRelFeaturizedDatum(this.dataSet, this.datum, featureValues);
			System.out.println("Datum " + this.index + " finished at " + System.currentTimeMillis());
		}
	}
	
}
