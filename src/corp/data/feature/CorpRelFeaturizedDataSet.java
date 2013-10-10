package corp.data.feature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import corp.data.annotation.CorpDocumentSet;
import corp.data.annotation.CorpRelDataSet;
import corp.data.annotation.CorpRelDatum;
import corp.data.annotation.CorpRelLabel;
import corp.data.annotation.CorpRelLabelPath;
import corp.util.OutputWriter;

public class CorpRelFeaturizedDataSet extends CorpRelDataSet {
	private List<CorpRelFeature> features;
	private int maxThreads;
	private Map<CorpRelDatum, CorpRelFeaturizedDatum> featurizedDatums;
	private OutputWriter output;
	
	public CorpRelFeaturizedDataSet(OutputWriter output) {
		this(1, output);
	}
	
	public CorpRelFeaturizedDataSet(int maxThreads, OutputWriter output) {
		this(new ArrayList<CorpRelFeature>(), maxThreads, output);
	}
	
	public CorpRelFeaturizedDataSet(List<CorpRelFeature> features, int maxThreads, OutputWriter output) {
		super();
		this.features = features;
		this.maxThreads = maxThreads;
		this.featurizedDatums = new HashMap<CorpRelDatum, CorpRelFeaturizedDatum>();
		this.output = output;
	}
	
	public CorpRelFeaturizedDataSet(CorpDocumentSet sourceDocuments, OutputWriter output) {
		this(sourceDocuments, new ArrayList<CorpRelFeature>(), 1, output);
	}
	
	public CorpRelFeaturizedDataSet(CorpDocumentSet sourceDocuments, List<CorpRelFeature> features, int maxThreads, OutputWriter output) {
		super(sourceDocuments);
		this.features = features;
		this.maxThreads = maxThreads;
		this.featurizedDatums = new HashMap<CorpRelDatum, CorpRelFeaturizedDatum>();
		this.output = output;
	}
	
	public int getMaxThreads() {
		return this.maxThreads;
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
	
	public List<CorpRelFeaturizedDatum> getFeaturizedDataUnderPath(CorpRelLabelPath path , boolean includePathRoot) {
		return featurize(getDataUnderPath(path, includePathRoot));
	}
	
	public List<CorpRelFeaturizedDatum> getFeaturizedDataInLabel(CorpRelLabel label) {
		return featurize(getDataInLabel(label));
	}
	
	private List<CorpRelFeaturizedDatum> featurize(List<CorpRelDatum> data) {
		this.output.debugWriteln("Featurizing data set...");
		
		CorpRelFeaturizedDatum[] featurizedData = new CorpRelFeaturizedDatum[data.size()];
		ExecutorService threadPool = Executors.newFixedThreadPool(this.maxThreads);
		
		for (int i = 0; i < data.size(); i++) {
			if (this.featurizedDatums.containsKey(data.get(i)))
				featurizedData[i] = this.featurizedDatums.get(data.get(i));
			else 
				threadPool.submit(new FeaturizeDatumThread(this, featurizedData, data.get(i), i));
		}
		
		try {
			threadPool.shutdown();
			threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		List<CorpRelFeaturizedDatum> featurizedDataList = new ArrayList<CorpRelFeaturizedDatum>();
		for (int i = 0; i < featurizedData.length; i++) {
			if (!this.featurizedDatums.containsKey(data.get(i)))
				this.featurizedDatums.put(data.get(i), featurizedData[i]);
			featurizedDataList.add(featurizedData[i]);
		}
		
		return featurizedDataList;
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
			try {
				List<Double> featureValues = new ArrayList<Double>();
				for (CorpRelFeature feature : features) {
					featureValues = feature.computeVector(datum, featureValues);
				}
				this.featurizedDatums[this.index] = new CorpRelFeaturizedDatum(this.dataSet, this.datum, featureValues);
			} catch (OutOfMemoryError e) {
				output.debugWriteln("ERROR: Out of memory while featurizing data!");
				System.exit(0);
			}
		}
	}
	
}
