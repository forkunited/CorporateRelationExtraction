package corp.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import corp.util.CommandRunner;
import corp.data.annotation.CorpDocumentTokenSpan;
import corp.data.annotation.CorpRelLabel;
import corp.data.feature.CorpRelFeaturizedDataSet;
import corp.data.feature.CorpRelFeaturizedDatum;
import edu.stanford.nlp.util.Pair;

/**
 * This is a simple wrapper for CReg in Java
 * 
 * @author Lingpeng Kong, Bill McDowell
 * 
 */
public class ModelCReg extends Model {
	private String cmdPath;
	private String modelPath;
	
	public ModelCReg(String cmdPath, List<CorpRelLabel> validLabels) {
		this.cmdPath = cmdPath;
		this.modelPath = null;
		this.validLabels = validLabels;
	}
	
	@Override
	public List<Pair<CorpRelFeaturizedDatum, HashMap<CorpRelLabel, Double>>> posterior(
			CorpRelFeaturizedDataSet data) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<Pair<CorpRelFeaturizedDatum, CorpRelLabel>> classify(CorpRelFeaturizedDataSet data) {
		String predictXPath = this.modelPath + ".predict.x";
		String predictYPath = this.modelPath + ".predict.y";
		
		if (!outputXData(predictXPath, data))
			return null;
		
		String predictCmd = this.cmdPath + " -w " + this.modelPath + " -W --tx " + predictXPath + " > " + predictYPath;
		if (!CommandRunner.run(predictCmd))
			return null;
		
		return loadYData(predictYPath, data);
	}

	@Override
	public boolean deserialize(String modelPath) {
		this.modelPath = modelPath;
		return true;
	}

	@Override
	public boolean train(CorpRelFeaturizedDataSet data, String outputPath) {
		String trainXPath = outputPath + ".train.x";
		String trainYPath = outputPath + ".train.y";
		
		if (!outputXData(trainXPath, data))
			return false;
		if (!outputYData(trainYPath, data))
			return false;
		
		String trainCmd = this.cmdPath + " -x " + trainXPath + " -y " + trainYPath + " --l1 1.0 " + " --z " + outputPath; 
		if (!CommandRunner.run(trainCmd))
			return false;
		
		this.modelPath = outputPath;
		
		return true;
	}
	
	private boolean outputXData(String outputPath, CorpRelFeaturizedDataSet data) {
        try {
        	List<CorpRelFeaturizedDatum> datums = data.getFeaturizedData();
    		List<String> features = data.getFeatureNames();
    		BufferedWriter writeX = new BufferedWriter(new FileWriter(outputPath));
    		
    		int datumIndex = 0;
    		for (CorpRelFeaturizedDatum datum : datums) {
    			if (datum.getLabelPath() != null && datum.getLabel(this.validLabels) == null)
    				continue;
    			
    			List<Double> values = datum.getFeatureValues();
    			writeX.write("id" + datumIndex + "\t{");
    			for (int i = 0; i < features.size(); i++) {
    				writeX.write("\"" + features.get(i) + "\": " + values.get(i));
    				if (i != features.size() - 1) {
    					writeX.write(", ");
    				}
    			}
				writeX.write("}\n");
				datumIndex++;
    		}    		
    		
            writeX.close();
            return true;
        } catch (IOException e) { e.printStackTrace(); return false; }
	}
	
	private boolean outputYData(String outputPath, CorpRelFeaturizedDataSet data) {
        try {
        	List<CorpRelFeaturizedDatum> datums = data.getFeaturizedData();
    		BufferedWriter writeY = new BufferedWriter(new FileWriter(outputPath));

    		int datumIndex = 0;
    		for (CorpRelFeaturizedDatum datum : datums) {
    			if (datum.getLabelPath() != null && datum.getLabel(this.validLabels) == null)
    				continue;
    			
				writeY.write("id" + datumIndex + "\t" + datum.getLabel(this.validLabels) + "\n");
				datumIndex++;
    		}    		
    		
            writeY.close();
            return true;
        } catch (IOException e) { e.printStackTrace(); return false; }
	}
	
	private List<Pair<CorpRelFeaturizedDatum, CorpRelLabel>> loadYData(String path, CorpRelFeaturizedDataSet data) {
		List<Pair<CorpRelFeaturizedDatum, CorpRelLabel>> yData = new ArrayList<Pair<CorpRelFeaturizedDatum, CorpRelLabel>>();
		List<CorpRelFeaturizedDatum> datums = data.getFeaturizedData();
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			
			for (CorpRelFeaturizedDatum datum : datums) {
    			if (datum.getLabelPath() != null && datum.getLabel(this.validLabels) == null)
    				continue;
				
				String line = br.readLine();
				if (line == null) {
					br.close();
					return null;
				}
					
				String[] lineParts = line.split("\t");
				if (lineParts.length < 2) {
					br.close();
					return null;
				}
				
				yData.add(new Pair<CorpRelFeaturizedDatum, CorpRelLabel>(datum, CorpRelLabel.valueOf(lineParts[1])));
			}
	        
	        br.close();
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
		
		return yData;
	}
}