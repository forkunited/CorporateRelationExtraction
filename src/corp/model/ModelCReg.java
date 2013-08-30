package corp.model;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import corp.util.CommandRunner;
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
	public List<Pair<CorpRelFeaturizedDatum, CorpRelLabel>> classify(CorpRelFeaturizedDataSet data) {
		String predictXPath = this.modelPath + ".predict.x";
		String predictYPath = this.modelPath + ".predict.y";
		
		/* FIXME: Output predictX data */
		
		String predictCmd = this.cmdPath + " -w " + this.modelPath + " -D -W --tx " + predictXPath + " > " + predictYPath;
		if (!CommandRunner.run(predictCmd))
			return null;
		
		List<Pair<CorpRelFeaturizedDatum, CorpRelLabel>> classifiedData = new ArrayList<Pair<CorpRelFeaturizedDatum, CorpRelLabel>>();
		/* FIXME: Load output */
		
		return classifiedData;
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
		
		List<String> features = data.getFeatureNames();
		List<CorpRelFeaturizedDatum> datums = data.getFeaturizedData();
		
        try {
    		BufferedWriter writeX = new BufferedWriter(new FileWriter(trainXPath));
    		BufferedWriter writeY = new BufferedWriter(new FileWriter(trainYPath));

    		int datumIndex = 0;
    		for (CorpRelFeaturizedDatum datum : datums) {
    			List<Double> values = datum.getFeatureValues();
    			writeX.write("id" + datumIndex + "\t{");
    			for (int i = 0; i < features.size(); i++) {
    				writeX.write("\"" + features.get(i) + "\": " + values.get(i));
    				if (i != features.size() - 1) {
    					writeX.write(", ");
    				}
    			}
				writeX.write("}\n");
				writeY.write("id" + datumIndex + "\t" + datum.getLabel(this.validLabels) + "\n");
				datumIndex++;
    		}    		
    		
            writeX.close();
            writeY.close();
        } catch (IOException e) { e.printStackTrace(); return false; }
		
		String trainCmd = this.cmdPath + " -x " + trainXPath + " -y " + trainYPath + " --l1 1.0 " + " --z " + outputPath; 
		if (!CommandRunner.run(trainCmd))
			return false;
		
		this.modelPath = outputPath;
		
		return true;
	}
}