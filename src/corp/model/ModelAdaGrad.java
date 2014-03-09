package corp.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import ark.util.FileUtil;
import ark.util.OutputWriter;

import corp.data.CorpDataTools;
import corp.data.annotation.CorpRelDatum;
import corp.data.annotation.CorpRelLabelPath;
import corp.data.feature.CorpRelFeaturizedDataSet;
import corp.data.feature.CorpRelFeaturizedDatum;
import corp.model.cost.CorpRelCostFunction;
import corp.model.cost.CorpRelCostFunctionConstant;
import edu.stanford.nlp.util.Pair;

public class ModelAdaGrad extends Model {
	private OutputWriter output;
	private Map<CorpRelLabelPath, Integer> pathIndices;
	private int trainingIterations;
	private int t;
	private double[] feature_w; // Labels x Input features
	private double[] feature_u; 
	private double[] feature_G;  // Just diagonal
	private double[] cost_v;
	private double[] cost_u; 
	private double[] cost_G;  // Just diagonal
	private Integer[] cost_i; // Cost indices for sorting v and G
	
	private class CostWeightComparator implements Comparator<Integer> {
	    @Override
	    public int compare(Integer i1, Integer i2) {
	    	double u_1 = cost_G[i1]*(2.0*cost_v[i1]-1);
	    	double u_2 = cost_G[i2]*(2.0*cost_v[i2]-1);
	    	
	    	if (u_1 > u_2)
	    		return -1;
	    	else if (u_1 < u_2)
	    		return 1;
	    	else 
	    		return 0;
	    }
	}
	
	public ModelAdaGrad(String existingModelPath, OutputWriter output, CorpDataTools dataTools) {
		this(new ArrayList<CorpRelLabelPath>(), new CorpRelCostFunctionConstant(), output, 0.001, 50);
		this.modelPath = existingModelPath;
		this.deserialize(existingModelPath, dataTools);
	}
	
	public ModelAdaGrad(List<CorpRelLabelPath> validPaths, OutputWriter output) {
		this(validPaths, new CorpRelCostFunctionConstant(), output, 0.001, 50);
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
	
		setCostFunction(costFunction);
	}
	
	@Override
	public boolean train(CorpRelFeaturizedDataSet data, String outputPath) {
		List<CorpRelFeaturizedDatum> datums = data.getFeaturizedLabeledData();
		
		this.pathIndices = new HashMap<CorpRelLabelPath, Integer>();
		for (int i = 0; i < this.validPaths.size(); i++)
			this.pathIndices.put(this.validPaths.get(i), i);
		
		this.modelPath = outputPath;
		
		if (this.feature_w == null) {
			this.t = 1;
			this.feature_w = new double[datums.get(0).getFeatureValues().size()*this.validPaths.size()];
			this.feature_u = new double[this.feature_w.length];
			this.feature_G = new double[this.feature_w.length];
			
			this.cost_v = new double[this.costFunction.getNames().size()];
			this.cost_u = new double[this.cost_v.length];
			this.cost_G = new double[this.cost_v.length];
		
			this.cost_i = new Integer[this.cost_v.length];
			for (int i = 0; i < this.cost_i.length; i++)
				this.cost_i[i] = i;
		}
		
		double[] prevFeature_w = Arrays.copyOf(this.feature_w, this.feature_w.length);
		double[] prevCost_v = Arrays.copyOf(this.cost_v, this.cost_v.length);
		
		this.output.debugWriteln("Training AdaGrad for " + this.trainingIterations + " iterations...");
		
		CostWeightComparator costWeightComparator = new CostWeightComparator();
		double[] feature_g = new double[this.feature_w.length];
		double[] cost_g = new double[this.cost_v.length];
		double lambda_1 = this.getHyperParameter("featuresL1");
		for (int iteration = 0; iteration < this.trainingIterations; iteration++) {
			for (CorpRelFeaturizedDatum datum : datums) {
				if (datum.getLabelPath() == null || datum.getLabelPath().getLongestValidPrefix(this.validPaths) == null)
					continue;
				
				CorpRelLabelPath datumLabel = datum.getLabelPath().getLongestValidPrefix(this.validPaths);
				CorpRelLabelPath bestLabel = argMaxScoreLabel(datum, true);
				List<Double> bestLabelCosts = this.costFunction.computeVector(datum, bestLabel);
				
				// Update feature weights
				for (int i = 0; i < this.feature_w.length; i++) { 
					feature_g[i] = -labelFeatureValue(i, datum, datumLabel)+labelFeatureValue(i, datum, bestLabel);
					
					this.feature_G[i] += feature_g[i]*feature_g[i];
					this.feature_u[i] += feature_g[i];
					
					if (Math.abs(this.feature_u[i])/this.t <= lambda_1)
						this.feature_w[i] = 0; 
					else 
						this.feature_w[i] = -Math.signum(this.feature_u[i])*(this.t/(Math.sqrt(this.feature_G[i])))*((Math.abs(this.feature_u[i])/this.t)-lambda_1); 
				}
				
				// Update cost weights
				for (int i = 0; i < this.cost_v.length; i++) {
					cost_g[i] = bestLabelCosts.get(i);
					this.cost_G[i] += cost_g[i]*cost_g[i];
					this.cost_u[i] += cost_g[i];
					
					if (this.cost_G[i] != 0)
						this.cost_v[i] -= cost_g[i]/Math.sqrt(this.cost_G[i]); 
				}
				
				
				// Project cost weights onto simplex \sum v_i = 1, v_i >= 0
				// Find p = max { j : u_j -  (1/(j*G_j))((sum_r=1^j u_r/G_r)-1) > 0 } 
				// where u and G are sorted desc
				Arrays.sort(this.cost_i, costWeightComparator);
				double sumVOverG = 0;
				double theta = 0;
				for (int p = 0; p < this.cost_v.length; p++) {
					if (this.cost_G[this.cost_i[p]] != 0)
						sumVOverG += this.cost_v[this.cost_i[p]]/this.cost_G[this.cost_i[p]];
					double prevTheta = theta;
					theta = (1.0/(p+1.0))*(sumVOverG - 1.0);
					if (this.cost_G[this.cost_i[p]] != 0 && this.cost_v[this.cost_i[p]]-theta/this.cost_G[this.cost_i[p]] <= 0) {
						this.output.debugWriteln("broke: v: " + this.cost_v[this.cost_i[p]] + " theta: " + theta + " G: " + this.cost_G[this.cost_i[p]]);
						theta = prevTheta;
						break;
					}
				}
				
				this.output.debugWriteln("theta " + theta);
				
				for (int j = 0; j < this.cost_v.length; j++) {
					this.cost_v[j] = Math.max(0, this.cost_v[j]-theta/this.cost_G[j]);
				}
				
				this.t++;
			}
	
			double vDiff = averageDiff(this.cost_v, prevCost_v);
			double wDiff = averageDiff(this.feature_w, prevFeature_w);
			double vSum = 0;
			for (int i = 0; i < this.cost_v.length; i++)
				vSum += this.cost_v[i];
			
			this.output.debugWriteln("Finished iteration " + iteration + " (v-diff (avg): " + vDiff + " w-diff (avg): " + wDiff + " v-sum: " + vSum + ").");
			prevCost_v = Arrays.copyOf(this.cost_v, prevCost_v.length);
			prevFeature_w = Arrays.copyOf(this.feature_w, prevFeature_w.length);
		}
		
		if (!serialize(data))
			return false;
		
		return true;
	}
	
	private double averageDiff(double[] v1, double[] v2) {
		double diffSum = 0.0;
		for (int i = 0; i < v1.length; i++)
			diffSum += Math.abs(v2[i] - v1[i]);
		return diffSum/v1.length;
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
		for (int i = labelIndex*numFeatures; i < (labelIndex+1)*numFeatures; i++) {
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
	
	private boolean serialize(CorpRelFeaturizedDataSet data) {
		if (!serializeParameters())
			return false;
		
		JSONObject otherDataJson = new JSONObject();
		otherDataJson.put("costFunction", this.costFunction.toString());
		otherDataJson.put("trainingIterations", this.trainingIterations);
		otherDataJson.put("t", this.t);
		JSONArray validPathsJson = new JSONArray();
		for (int i = 0; i < this.validPaths.size(); i++)
			validPathsJson.add(this.validPaths.get(i).toString());
		otherDataJson.put("validPaths", validPathsJson);
		
        try {
    		BufferedWriter w = new BufferedWriter(new FileWriter(this.modelPath + ".other"));  		
    		w.write(otherDataJson.toString());
    		w.close();
    		List<String> featureNames = data.getFeatureNames();
    		w = new BufferedWriter(new FileWriter(this.modelPath + ".features"));  	
    		w.write(this.feature_w.length + "\n");
    		for (int i = 0; i < this.validPaths.size(); i++) {
    			for (int j = 0; j < featureNames.size(); j++) {
    				String label = this.validPaths.get(i).toString();
    				String featureName = featureNames.get(j);
    				int index = i*featureNames.size()+j;
    				
    				double featureW = this.feature_w[index];
    				double featureG = this.feature_G[index];
    				double featureU = this.feature_u[index];
    				
    				w.write(label + "\t" + featureName + "\t" + featureW + "\t" + featureG + "\t" + featureU + "\n");
    			}
    		}
    		w.close();
    		
    		w = new BufferedWriter(new FileWriter(this.modelPath + ".costs"));  		
    		List<String> costNames = this.costFunction.getNames();
    		w.write(costNames.size() + "\n");
    		for (int i = 0; i < costNames.size(); i++) {
    			w.write(costNames.get(i) + "\t" + this.cost_v[i] + "\t" + this.cost_G[i] + "\t" + this.cost_u[i] + "\n");
    		}
    		w.close();
        } catch (IOException e) { 
        	e.printStackTrace(); 
        	return false; 
        }
        
		return true;
	}
	
	@Override
	public boolean deserialize(String modelPath, CorpDataTools dataTools) {
		if (!deserializeParameters())
			return false;
		
		BufferedReader r = FileUtil.getFileReader(modelPath + ".other");
		String line = null;
		StringBuffer lines = new StringBuffer();
		try {
			while ((line = r.readLine()) != null) {
				lines.append(line).append("\n");
			}
			r.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} 
		
		JSONObject otherDataJson = JSONObject.fromObject(lines.toString());
		this.validPaths = new ArrayList<CorpRelLabelPath>();
		this.pathIndices = new HashMap<CorpRelLabelPath, Integer>();
		JSONArray validPathsJson = otherDataJson.getJSONArray("validPaths");
		for (int i = 0; i < validPathsJson.size(); i++) {
			CorpRelLabelPath path = CorpRelLabelPath.fromString(validPathsJson.getString(i));
			this.validPaths.add(path);
			this.pathIndices.put(path, i);
		}
		this.costFunction = CorpRelCostFunction.fromString(otherDataJson.getString("costFunction"), this.validPaths);
		this.trainingIterations = otherDataJson.getInt("trainingIterations");
		this.t = otherDataJson.getInt("t");
		
		r = FileUtil.getFileReader(modelPath + ".features");
		try {
			line = r.readLine();
			int featureCount = Integer.parseInt(line);
			this.feature_w = new double[featureCount];
			this.feature_G = new double[featureCount];
			this.feature_u = new double[featureCount];
			
			int i = 0;
			while ((line = r.readLine()) != null) {
				String[] lineParts = line.split("\t");
				this.feature_w[i] = Double.parseDouble(lineParts[2]);
				this.feature_G[i] = Double.parseDouble(lineParts[3]);
				this.feature_u[i] = Double.parseDouble(lineParts[4]);			
				i++;
			}
			r.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} 
		
		r = FileUtil.getFileReader(modelPath + ".costs");
		try {
			line = r.readLine();
			int costCount = Integer.parseInt(line);
			this.cost_v = new double[costCount];
			this.cost_G = new double[costCount];
			this.cost_u = new double[costCount];
			
			int i = 0;
			while ((line = r.readLine()) != null) {
				String[] lineParts = line.split("\t");
				this.cost_v[i] = Double.parseDouble(lineParts[1]);
				this.cost_G[i] = Double.parseDouble(lineParts[2]);
				this.cost_u[i] = Double.parseDouble(lineParts[3]);			
				i++;
			}
			r.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} 
		
		this.cost_i = new Integer[this.cost_v.length];
		for (int i = 0; i < this.cost_i.length; i++)
			this.cost_i[i] = i;
		
		return true;
	}

	@Override
	public Model clone() {
		ModelAdaGrad cloneModel = new ModelAdaGrad(this.validPaths, this.costFunction, this.output, getHyperParameter("featuresL1"), this.trainingIterations);
		cloneModel.modelPath = this.modelPath;
		for (Entry<String, Double> hyperParameter : this.hyperParameters.entrySet())
			cloneModel.setHyperParameter(hyperParameter.getKey(), hyperParameter.getValue());
		return cloneModel;
	}
	
	@Override
	public List<Pair<CorpRelDatum, Map<CorpRelLabelPath, Double>>> posterior(
			CorpRelFeaturizedDataSet data) {
		
		this.pathIndices = new HashMap<CorpRelLabelPath, Integer>();
		for (int i = 0; i < this.validPaths.size(); i++)
			this.pathIndices.put(this.validPaths.get(i), i);
		
		List<CorpRelFeaturizedDatum> datums = data.getFeaturizedData();
		List<Pair<CorpRelDatum, Map<CorpRelLabelPath, Double>>> posteriors = new ArrayList<Pair<CorpRelDatum, Map<CorpRelLabelPath, Double>>>(datums.size());
		
		for (CorpRelFeaturizedDatum datum : datums) {
			posteriors.add(new Pair<CorpRelDatum, Map<CorpRelLabelPath, Double>>(datum, posteriorForDatum(datum)));
		}
		
		return posteriors;
	}

	private Map<CorpRelLabelPath, Double> posteriorForDatum(CorpRelFeaturizedDatum datum) {
		Map<CorpRelLabelPath, Double> posterior = new HashMap<CorpRelLabelPath, Double>(this.validPaths.size());
		double[] scores = new double[this.validPaths.size()];
		double max = Double.NEGATIVE_INFINITY;
		for (CorpRelLabelPath label : this.validPaths) {
			double score = scoreLabel(datum, label, false);
			scores[this.pathIndices.get(label)] = score;
			if (score > max)
				max = score;
		}
		
		double lse = 0;
		for (int i = 0; i < scores.length; i++)
			lse += Math.exp(scores[i] - max);
		lse = max + Math.log(lse);
		
		for (CorpRelLabelPath label : this.validPaths) {
			posterior.put(label, Math.exp(scores[this.pathIndices.get(label)]-lse));
		}
		
		return posterior;
	}

}
