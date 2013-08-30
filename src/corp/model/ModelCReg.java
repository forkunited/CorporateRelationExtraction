package corp.model;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
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
	
	public ModelCReg(String cmdPath) {
		this.cmdPath = cmdPath;
		this.modelPath = null;
	}
	
	@Override
	public List<Pair<CorpRelFeaturizedDatum, CorpRelLabel>> classify(
			CorpRelFeaturizedDataSet data) {
		// TODO Auto-generated method stub
		return null;
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

    		for (CorpRelFeaturizedDatum datum : datums) {
    			// FIXME
    			//"id1\t{"feature1": 1.0, "feature2": -10}"
    			//id1\t10.1
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