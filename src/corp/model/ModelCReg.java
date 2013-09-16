package corp.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import corp.util.CommandRunner;
import corp.data.annotation.CorpRelDatum;
import corp.data.annotation.CorpRelLabelPath;
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
	
	public ModelCReg(String cmdPath, List<CorpRelLabelPath> validPaths) {
		this.cmdPath = cmdPath;
		this.modelPath = null;
		this.validPaths = validPaths;
	}
	
	@Override
	public List<Pair<CorpRelDatum, Map<CorpRelLabelPath, Double>>> posterior(CorpRelFeaturizedDataSet data) {
		List<CorpRelFeaturizedDatum> datums = data.getFeaturizedData();
		String predictPPath = predict(datums);
		if (predictPPath == null)
			return null;
		else 
			return loadPData(predictPPath, datums, false);
	}
	
	@Override
	public List<Pair<CorpRelDatum, CorpRelLabelPath>> classify(CorpRelFeaturizedDataSet data) {
		List<CorpRelFeaturizedDatum> datums = data.getFeaturizedData();
		
		String predictYPath = predict(datums);
		if (predictYPath == null)
			return null;
		else 
			return loadYData(predictYPath, datums, false);
	}

	@Override
	public boolean deserialize(String modelPath) {
		this.modelPath = modelPath;
		return true;
	}

	@Override
	public boolean train(CorpRelFeaturizedDataSet data, String outputPath) {
		List<CorpRelFeaturizedDatum> datums = data.getFeaturizedLabeledData();
		
		String trainXPath = outputPath + ".train.x";
		String trainYPath = outputPath + ".train.y";
		
		System.out.println("CReg outputting training data for " + outputPath);
		
		if (!outputXData(trainXPath, datums, true))
			return false;
		if (!outputYData(trainYPath, datums))
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
	
	private boolean outputXData(String outputPath, List<CorpRelFeaturizedDatum> data, boolean requireLabels) {
        try {
    		BufferedWriter writeX = new BufferedWriter(new FileWriter(outputPath));
    		
    		int datumIndex = 0;
    		for (CorpRelFeaturizedDatum datum : data) {
    			if (requireLabels && (datum.getLabelPath() == null || datum.getLabelPath().getLongestValidPrefix(this.validPaths) == null))
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
    			if (datum.getLabelPath() == null)
    				continue;
    			CorpRelLabelPath labelPath = datum.getLabelPath().getLongestValidPrefix(this.validPaths);
    			if (labelPath == null)
    				continue;
				writeY.write("id" + datumIndex + "\t" + labelPath + "\n");
				datumIndex++;
    		}    		
    		
            writeY.close();
            return true;
        } catch (IOException e) { e.printStackTrace(); return false; }
	}
	
	private List<Pair<CorpRelDatum, CorpRelLabelPath>> loadYData(String path, List<CorpRelFeaturizedDatum> data, boolean requireLabels) {
		List<Pair<CorpRelDatum, CorpRelLabelPath>> yData = new ArrayList<Pair<CorpRelDatum, CorpRelLabelPath>>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			
			for (CorpRelDatum datum : data) {
    			if (requireLabels && (datum.getLabelPath() == null || datum.getLabelPath().getLongestValidPrefix(this.validPaths) == null))
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
				
				yData.add(new Pair<CorpRelDatum, CorpRelLabelPath>(datum, CorpRelLabelPath.fromString(lineParts[1])));
			}
	        
	        br.close();
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
		
		return yData;
	}

	private List<Pair<CorpRelDatum, Map<CorpRelLabelPath, Double>>> loadPData(String path, List<CorpRelFeaturizedDatum> data, boolean requireLabels) {
		List<Pair<CorpRelDatum, Map<CorpRelLabelPath, Double>>> pData = new ArrayList<Pair<CorpRelDatum, Map<CorpRelLabelPath, Double>>>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			
			for (CorpRelFeaturizedDatum datum : data) {
    			if (requireLabels && (datum.getLabelPath() == null || datum.getLabelPath().getLongestValidPrefix(this.validPaths) == null))
    				continue;
				
				String line = br.readLine();
				if (line == null) {
					br.close();
					return null;
				}
					
				String[] lineParts = line.split("\t");
				if (lineParts.length < 3) {
					br.close();
					return null;
				}
				
				/* NOTE: Should probably use a JSON parser for this... but I'm just doing it the quick and dirty way for now */
				String posteriorStr = lineParts[2];
				Matcher m = Pattern.compile("\"([A-Za-z0-9\\-]*)\"\\s*:\\s*([0-9\\.]*)").matcher(posteriorStr);
		        Map<CorpRelLabelPath, Double> p = new HashMap<CorpRelLabelPath, Double>();
				while(m.find()) {
		        	CorpRelLabelPath labelPath = CorpRelLabelPath.fromString(m.group(1));
		        	double pLabel = Double.parseDouble(m.group(2));
		        	p.put(labelPath, pLabel);
		        }
				pData.add(new Pair<CorpRelDatum, Map<CorpRelLabelPath, Double>>(datum, p));
			}
	        
	        br.close();
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	return null;
	    }
		
		return pData;
	}
	
	private String predict(List<CorpRelFeaturizedDatum> data) {
		String predictXPath = this.modelPath + ".predict.x";
		String predictOutPath = this.modelPath + ".predict.y";
		
		System.out.println("CReg outputting prediction data for " + this.modelPath);
		
		if (!outputXData(predictXPath, data, false))
			return null;
		
		String predictCmd = this.cmdPath + " -w " + this.modelPath + " -W -D --tx " + predictXPath + " > " + predictOutPath;
		predictCmd = predictCmd.replace("\\", "/"); 
		if (!CommandRunner.run(predictCmd))
			return null;
		
		System.out.println("CReg predicting data for " + this.modelPath);
		
		return predictOutPath;
	}
	
	@Override
	public Model clone() {
		ModelCReg cloneModel = new ModelCReg(this.cmdPath, this.validPaths);
		cloneModel.modelPath = this.modelPath;
		return cloneModel;
	}
}