package corp.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ark.util.OutputWriter;

import corp.data.CorpDataTools;
import corp.data.annotation.CorpRelDatum;
import corp.data.annotation.CorpRelLabelPath;
import corp.data.feature.CorpRelFeaturizedDataSet;
import corp.data.feature.CorpRelFeaturizedDatum;
import corp.model.cost.CorpRelCostFunction;
import corp.model.cost.CorpRelCostFunctionConstant;
import corp.util.CorpProperties;
import edu.stanford.nlp.util.Pair;

public class ModelAdaGrad extends Model {
	private OutputWriter output;
	private CorpRelCostFunction costFunction;
	private Map<CorpRelLabelPath, Integer> pathIndices;
	private int trainingIterations;
	private int t;
	private double[] feature_w; // Labels x Input features
	private double[] feature_u; 
	private double[] feature_G;  // Just diagonal
	private double[] cost_v;
	private double[] cost_u; 
	private double[] cost_G;  // Just diagonal
	
	public ModelAdaGrad(String existingModelPath, OutputWriter output, CorpDataTools dataTools) {
		this(new ArrayList<CorpRelLabelPath>(), new CorpRelCostFunctionConstant(), output, 1.0, 10);
		this.modelPath = existingModelPath;
		this.deserialize(existingModelPath, dataTools);
	}
	
	public ModelAdaGrad(List<CorpRelLabelPath> validPaths, OutputWriter output) {
		this(validPaths, new CorpRelCostFunctionConstant(), output, 1.0, 10);
	}
	
	public ModelAdaGrad(List<CorpRelLabelPath> validPaths, CorpRelCostFunction costFunction, OutputWriter output, double featuresL1, int trainingIterations) {
		this.modelPath = null;
		this.validPaths = validPaths;
		this.output = output;
		this.costFunction = costFunction;
		this.hyperParameters = new HashMap<String, Double>();
		this.trainingIterations = trainingIterations;
		setHyperParameter("featuresL1", featuresL1);
		
		this.pathIndices = new HashMap<CorpRelLabelPath, Integer>();
		for (int i = 0; i < this.validPaths.size(); i++)
			this.pathIndices.put(this.validPaths.get(i), i);
	}
	
	@Override
	public boolean train(CorpRelFeaturizedDataSet data, String outputPath) {
		List<CorpRelFeaturizedDatum> datums = data.getFeaturizedLabeledData();
		
		if (this.feature_w == null) {
			this.t = 0;
			this.feature_w = new double[datums.get(0).getFeatureValues().size()*this.validPaths.size()];
			this.feature_u = new double[this.feature_w.length];
			this.feature_G = new double[this.feature_w.length];
			
			this.cost_v = new double[this.costFunction.getNames().size()];
			this.cost_u = new double[this.cost_v.length];
			this.cost_G = new double[this.cost_v.length];
		}
		
		double[] feature_g = new double[this.feature_w.length];
		double[] cost_g = new double[this.cost_v.length];
		double lambda_1 = this.getHyperParameter("featuresL1");
		for (int iteration = 0; iteration < this.trainingIterations; iteration++) {
			for (CorpRelFeaturizedDatum datum : datums) {
				if (datum.getLabelPath() == null || datum.getLabelPath().getLongestValidPrefix(this.validPaths) == null)
					continue;
				
				CorpRelLabelPath bestLabel = argMaxScoreLabel(datum, true);
				List<Double> bestLabelCosts = this.costFunction.computeVector(datum, bestLabel);
				
				// Update feature weights
				for (int i = 0; i < this.feature_w.length; i++) { 
					feature_g[i] = -labelFeatureValue(i, datum, datum.getLabelPath())+labelFeatureValue(i, datum, bestLabel);
					this.feature_G[i] += feature_g[i]*feature_g[i];
					this.feature_u[i] += feature_g[i];
					if (feature_g[i] <= lambda_1)
						this.feature_w[i] = 0; 
					else
						this.feature_w[i] = 100000; // FIXME Update weight
				}
				
				// Update cost weights
				for (int i = 0; i < this.cost_v.length; i++) {
					cost_g[i] = bestLabelCosts.get(i);
					this.cost_G[i] += cost_g[i]*cost_g[i];
					this.cost_u[i] += cost_g[i];
					this.cost_v[i] = 0; // FIXME Update weight
				}
				
				this.t++;
			}
		}
		
		return true;
	}
	
	private CorpRelLabelPath argMaxScoreLabel(CorpRelFeaturizedDatum datum, boolean includeCost) {
		CorpRelLabelPath maxLabel = null;
		double maxScore = Double.NEGATIVE_INFINITY;
		for (CorpRelLabelPath label : this.validPaths) {
			double score = scoreLabel(datum, label, includeCost);
			if (score >= maxScore) {
				maxScore = score;
				maxLabel = label;
			}
		}
		return maxLabel;
	}
	
	private double scoreLabel(CorpRelFeaturizedDatum datum, CorpRelLabelPath label, boolean includeCost) {
		double score = 0;
		int labelIndex = this.pathIndices.get(label);
		int numFeatures = datum.getFeatureValues().size();
		for (int i = labelIndex*numFeatures; i < (labelIndex+1)*this.feature_w.length; i++) {
			score += this.feature_w[i]*labelFeatureValue(i, datum, label);
		}
		
		if (includeCost) {
			List<Double> costs = this.costFunction.computeVector(datum, label);
			for (int i = 0; i < this.cost_v.length; i++)
				score += costs.get(i)*this.cost_v[i];
		}
		
		return score;
	}
	
	private double labelFeatureValue(int weightIndex, CorpRelFeaturizedDatum datum, CorpRelLabelPath label) {
		int labelIndex = this.pathIndices.get(label);
		int numFeatures = datum.getFeatureValues().size();
		int featureLabelIndex = (weightIndex >= numFeatures) ? weightIndex / numFeatures : weightIndex;
		if (featureLabelIndex != labelIndex)
			return 0.0;
		
		int featureIndex = featureLabelIndex % numFeatures;
		return datum.getFeatureValues().get(featureIndex);
	}
	
	@Override
	public boolean deserialize(String modelPath, CorpDataTools dataTools) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Pair<CorpRelDatum, Map<CorpRelLabelPath, Double>>> posterior(
			CorpRelFeaturizedDataSet data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Model clone() {
		// TODO Auto-generated method stub
		return null;
	}

}
