package corp.experiment;

import java.io.File;
import java.util.List;
import corp.model.evaluation.KFoldCrossValidation;
import corp.util.SerializationUtil;
import edu.stanford.nlp.util.Pair;

/**
 * ExperimentTreeKCV represents a k-fold cross-validation training/evaluation 
 * experiment with optional grid-search using the "training" data with a 
 * corp.model.ModelTree model.  See the "experiments" directory for example 
 * configurations, and read the technical report (and the source tex file
 * for more detail).
 * 
 *  After the configuration is parsed, this class mainly uses the
 * corp.model.evaluation.KFoldCrossValidation class to carry out
 * the experiment.
 * 
 * @author Bill McDowell
 *
 */
public class ExperimentTreeKCV extends ExperimentTree {
	private int crossValidationFolds;
	
	public ExperimentTreeKCV() {
		
	}

	@Override
	protected void parse(List<String> lines) {
		super.parse(lines);
		
		this.output.debugWriteln("Parsing cross validation parameters...");
		
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			Pair<String, String> assignment = SerializationUtil.deserializeAssignment(line);	
			if (assignment == null)
				continue;
			else if (assignment.first().equals("crossValidationFolds"))
				this.crossValidationFolds = Integer.parseInt(assignment.second());
		}
	}

	@Override
	protected void execute(String name) {
		KFoldCrossValidation validation = new KFoldCrossValidation(
				this.modelTree, 
				this.dataSet,
				this.crossValidationFolds,
				new File(this.properties.getCregDataDirPath(), name + "." + this.crossValidationFolds + "FoldCV").getAbsolutePath(),
				this.rand,
				this.output,
				this.gridSearchParameters
		);
		
		validation.run(this.maxThreads);
	}
	
	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("First argument should be the name of the experiment");
			return;
		}
		
		ExperimentTreeKCV experiment = new ExperimentTreeKCV();
		experiment.run(args[0]);
	}
}
