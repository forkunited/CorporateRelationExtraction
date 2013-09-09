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
import corp.data.annotation.CorpRelLabel;
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
			List<CorpRelFeaturizedDatum> data) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<Pair<CorpRelFeaturizedDatum, CorpRelLabel>> classify(List<CorpRelFeaturizedDatum> data) {
		String predictXPath = this.modelPath + ".predict.x";
		String predictYPath = this.modelPath + ".predict.y";
		
		System.out.println("CReg outputting classification data for " + this.modelPath);
		
		if (!outputXData(predictXPath, data))
			return null;
		
		String predictCmd = this.cmdPath + " -w " + this.modelPath + " -W --tx " + predictXPath + " > " + predictYPath;
		predictCmd = predictCmd.replace("\\", "/"); 
		if (!CommandRunner.run(predictCmd))
			return null;
		
		System.out.println("CReg classifying data for " + this.modelPath);
		
		return loadYData(predictYPath, data);
	}

	@Override
	public boolean deserialize(String modelPath) {
		this.modelPath = modelPath;
		return true;
	}

	@Override
	public boolean train(List<CorpRelFeaturizedDatum> data, String outputPath) {
		String trainXPath = outputPath + ".train.x";
		String trainYPath = outputPath + ".train.y";
		
		System.out.println("CReg outputting training data for " + outputPath);
		
		if (!outputXData(trainXPath, data))
			return false;
		if (!outputYData(trainYPath, data))
			return false;
		
		System.out.println("CReg training model for " + outputPath);
		
		String trainCmd = this.cmdPath + " -x " + trainXPath + " -y " + trainYPath + " --l1 1.0 " + " --z " + outputPath;
		trainCmd = trainCmd.replace("\\", "/"); 
		if (!CommandRunner.run(trainCmd))
			return false;
		
		this.modelPath = outputPath;
		
		System.out.println("CReg finished training model for " + outputPath);
		
		return true;
	}
	
	private boolean outputXData(String outputPath, List<CorpRelFeaturizedDatum> data) {
        try {
    		BufferedWriter writeX = new BufferedWriter(new FileWriter(outputPath));
    		
    		int datumIndex = 0;
    		for (CorpRelFeaturizedDatum datum : data) {
    			if (datum.getLabelPath() != null && datum.getLabel(this.validLabels) == null)
    				continue;
    			List<String> features = datum.getSourceDataSet().getFeatureNames();
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
	
	private boolean outputYData(String outputPath, List<CorpRelFeaturizedDatum> data) {
        try {
    		BufferedWriter writeY = new BufferedWriter(new FileWriter(outputPath));

    		int datumIndex = 0;
    		for (CorpRelFeaturizedDatum datum : data) {
    			if (datum.getLabelPath() != null && datum.getLabel(this.validLabels) == null)
    				continue;
    			
				writeY.write("id" + datumIndex + "\t" + datum.getLabel(this.validLabels) + "\n");
				datumIndex++;
    		}    		
    		
            writeY.close();
            return true;
        } catch (IOException e) { e.printStackTrace(); return false; }
	}
	
	private List<Pair<CorpRelFeaturizedDatum, CorpRelLabel>> loadYData(String path, List<CorpRelFeaturizedDatum> data) {
		List<Pair<CorpRelFeaturizedDatum, CorpRelLabel>> yData = new ArrayList<Pair<CorpRelFeaturizedDatum, CorpRelLabel>>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			
			for (CorpRelFeaturizedDatum datum : data) {
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

	@Override
	public Model clone() {
		return new ModelCReg(this.cmdPath, this.validLabels);
	}
}