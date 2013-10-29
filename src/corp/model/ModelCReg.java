package corp.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import corp.util.CommandRunner;
import corp.util.CorpProperties;
import corp.util.OutputWriter;
import corp.data.CorpDataTools;
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
	private OutputWriter output;
	
	public ModelCReg(String existingModelPath, OutputWriter output, CorpDataTools dataTools) {
		this((new CorpProperties()).getCregCommandPath(), new ArrayList<CorpRelLabelPath>(), output, 1.0, 1e-10);
		this.modelPath = existingModelPath;
		this.deserialize(existingModelPath, dataTools);
	}
	
	public ModelCReg(String cmdPath, String existingModelPath, OutputWriter output, CorpDataTools dataTools) {
		this(cmdPath, new ArrayList<CorpRelLabelPath>(), output, 1.0, 1e-10);
		this.modelPath = existingModelPath;
		this.deserialize(existingModelPath, dataTools);
	}
	
	public ModelCReg(String cmdPath, List<CorpRelLabelPath> validPaths, OutputWriter output) {
		this(cmdPath, validPaths, output, 1.0, 1e-10);
	}
	
	public ModelCReg(String cmdPath, List<CorpRelLabelPath> validPaths, OutputWriter output, double l1, double l2) {
		this.cmdPath = cmdPath;
		this.modelPath = null;
		this.validPaths = validPaths;
		this.output = output;
		this.hyperParameters = new HashMap<String, Double>();
		setHyperParameter("l1", l1);
		setHyperParameter("l2", l2);
		setHyperParameter("warmRestart", 0);
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
	public boolean deserialize(String modelPath, CorpDataTools dataTools) {
		this.modelPath = modelPath;
		if (!deserializeParameters())
			return false;
		
		return true;
	}

	@Override
	public boolean train(CorpRelFeaturizedDataSet data, String outputPath) {
		List<CorpRelFeaturizedDatum> datums = data.getFeaturizedLabeledData();
		
		String trainXPath = outputPath + ".train.x";
		String trainYPath = outputPath + ".train.y";
		
		this.output.debugWriteln("CReg outputting training data for " + outputPath);
		
		if (!outputXData(trainXPath, datums, true))
			return false;
		if (!outputYData(trainYPath, datums))
			return false;
		
		this.output.debugWriteln("CReg training model for " + outputPath);
		
		File outputFile = new File(outputPath);
		
		String trainCmd = this.cmdPath + 
						" -x " + trainXPath + 
						" -y " + trainYPath + 
						" --l1 " + getHyperParameter("l1") + 
						" --l2 " + getHyperParameter("l2") + 
						((hasHyperParameter("warmRestart") && getHyperParameter("warmRestart") > 0 && outputFile.exists()) ? " --weights " + outputPath : "") +
						" --z " + outputPath;
		trainCmd = trainCmd.replace("\\", "/"); 
		if (!CommandRunner.run(trainCmd))
			return false;
		
		this.modelPath = outputPath;
		
		if (!serializeParameters())
			return false;
		
		this.output.debugWriteln("CReg finished training model for " + outputPath);
		
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
		
		this.output.debugWriteln("CReg outputting prediction data for " + this.modelPath);
		
		if (!outputXData(predictXPath, data, false)) {
			this.output.debugWriteln("Error: CReg failed to output feature data.")
			return null;
		}
		
		String predictCmd = this.cmdPath + " -w " + this.modelPath + " -W -D --tx " + predictXPath + " > " + predictOutPath;
		predictCmd = predictCmd.replace("\\", "/"); 
		if (!CommandRunner.run(predictCmd)) {
			this.output.debugWriteln("Error: CReg failed to run on output data.");
			return null;
		}
		
		this.output.debugWriteln("CReg predicting data for " + this.modelPath);
		
		return predictOutPath;
	}
	
	@Override
	public Model clone() {
		ModelCReg cloneModel = new ModelCReg(this.cmdPath, this.validPaths, this.output, getHyperParameter("l1"), getHyperParameter("l2"));
		cloneModel.modelPath = this.modelPath;
		for (Entry<String, Double> hyperParameter : this.hyperParameters.entrySet())
			cloneModel.setHyperParameter(hyperParameter.getKey(), hyperParameter.getValue());
		return cloneModel;
	}
	
	@Override
	public String toString() {
		TreeMap<Double, List<String>> sortedWeights = new TreeMap<Double, List<String>>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(this.modelPath));
			String line = null;
			
			while ((line = br.readLine()) != null) {
				String[] lineParts = line.split("\t");
				Double value = null;
				if (lineParts.length < 3)
					continue;
				try {
					value = Math.abs(Double.parseDouble(lineParts[2]));
				} catch (NumberFormatException e) {
					continue;
				}
				
				if (!sortedWeights.containsKey(value))
					sortedWeights.put(value, new ArrayList<String>());
				sortedWeights.get(value).add(line);
			}
	        
	        br.close();
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
		
		StringBuilder retStr = new StringBuilder();
		
		retStr.append("CReg Model (").append(this.modelPath).append(")\n");
		
		NavigableMap<Double, List<String>> descendingWeights = sortedWeights.descendingMap();
		for (List<String> lines : descendingWeights.values())
			for (String line : lines)
				retStr.append(line).append("\n");
		
		return retStr.toString();
	}
}