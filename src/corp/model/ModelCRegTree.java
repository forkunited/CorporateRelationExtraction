package corp.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import corp.data.annotation.CorpRelLabel;
import corp.data.feature.CorpRelFeaturizedDataSet;
import corp.data.feature.CorpRelFeaturizedDatum;
import corp.util.CorpProperties;
import edu.stanford.nlp.util.Pair;

/**
 * 
 * The basic idea of a CReg Tree Model is that it should call multiple CReg
 * Models and make a prediction. It may also use the posteriors provided by the
 * basic CReg Models.
 * 
 * @author Lingpeng Kong
 * 
 */

public class ModelCRegTree extends Model {
	/*
	 * There should be three layer of models, the first layer model, contains
	 * classes SelfRef, OtherCorp, NonCorp, Generic, Dontknow and Error. It
	 * seems that for now, since we do not use a "root" label (kind of like the
	 * Object class in Java) in our representation system, we have to hard-coded
	 * these classes. But for the second and third layer features, things are
	 * much easier (the label system has been generate dynamically by the data
	 * classes), so what we do is to have a HashMap<CorpRelLabel, ModelCReg>,
	 * where the key is the parent node we use in the second layer models, and
	 * the ModelCReg reflects a specific model for this node. Same thing
	 * happened in the third layer models, if the label is not in the HashMap,
	 * then basically everything is done, like the case "SelfRef", we are done
	 * when we reach the "SelfRef" node.
	 */
	private ModelCReg firstLayerModel;
	private HashMap<CorpRelLabel, ModelCReg> secondLayerModels;
	private HashMap<CorpRelLabel, ModelCReg> thirdLayerModels;
	
	public ModelCRegTree(){
		secondLayerModels = new HashMap<CorpRelLabel, ModelCReg> ();
		thirdLayerModels = new HashMap<CorpRelLabel, ModelCReg>();
	}
	
	@Override
	public List<Pair<CorpRelFeaturizedDatum, CorpRelLabel>> classify(
			List<CorpRelFeaturizedDatum> data) {
		/* A down-to-leaf implementation now */
		List<Pair<CorpRelFeaturizedDatum, CorpRelLabel>> list = new ArrayList<Pair<CorpRelFeaturizedDatum, CorpRelLabel>>();
		List<Pair<CorpRelFeaturizedDatum, CorpRelLabel>> fList = firstLayerModel.classify(data);
		HashMap<CorpRelLabel, List<CorpRelFeaturizedDatum>> secondLayerMap = new HashMap<CorpRelLabel, List<CorpRelFeaturizedDatum>>();
		for(Pair<CorpRelFeaturizedDatum, CorpRelLabel> fpair: fList){
			CorpRelLabel crl = fpair.second();
			if(!secondLayerModels.containsKey(crl)){
				/* No model in the second layer, come to a leaf, done */
				list.add(fpair);
			}else{
				if(!secondLayerMap.containsKey(crl)){
					secondLayerMap.put(crl, new ArrayList<CorpRelFeaturizedDatum>());
				}
				secondLayerMap.get(crl).add(fpair.first());
			}
		}
		HashMap<CorpRelLabel, List<CorpRelFeaturizedDatum>> thirdLayerMap = new HashMap<CorpRelLabel, List<CorpRelFeaturizedDatum>>();
		for(CorpRelLabel crl : secondLayerMap.keySet()){
			List<Pair<CorpRelFeaturizedDatum, CorpRelLabel>> tlist = secondLayerModels.get(crl).classify(secondLayerMap.get(crl));
			for(Pair<CorpRelFeaturizedDatum, CorpRelLabel> fpair: tlist){
				CorpRelLabel tcrl = fpair.second();
				if(!thirdLayerModels.containsKey(crl)){
					/* No model in the third layer, come to a leaf, done */
					list.add(fpair);
				}else{
					if(!thirdLayerMap.containsKey(tcrl)){
						thirdLayerMap.put(tcrl, new ArrayList<CorpRelFeaturizedDatum>());
					}
					thirdLayerMap.get(tcrl).add(fpair.first());
				}
			}
		}
		for(CorpRelLabel crl : thirdLayerMap.keySet()){
			List<Pair<CorpRelFeaturizedDatum, CorpRelLabel>> tlist = thirdLayerModels.get(crl).classify(secondLayerMap.get(crl));
			list.addAll(tlist);
		}
		return list;
	}
	
	@Override
	public boolean deserialize(String modelPath) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean train(List<CorpRelFeaturizedDatum> data, String outputPath) {
		return false;
	}
	
	/*TODO: For now, we just leave all the features to be the same set to save time */
	public boolean train(CorpRelFeaturizedDataSet data, String outputPath) {
		List<CorpRelLabel> firstLayerLabels = new ArrayList<CorpRelLabel>();
		
		firstLayerLabels.add(CorpRelLabel.SelfRef);
		firstLayerLabels.add(CorpRelLabel.OCorp);
		firstLayerLabels.add(CorpRelLabel.NonCorp);
		firstLayerLabels.add(CorpRelLabel.Generic);
		firstLayerLabels.add(CorpRelLabel.Error);
		
		CorpProperties properties = new CorpProperties("corp.properties");
		firstLayerModel = new ModelCReg(properties.getCregCommandPath(), firstLayerLabels);
		if(!firstLayerModel.train(data, outputPath+".first")){
			return false;
		}
		
		for(CorpRelLabel crl : firstLayerLabels){
			/* Get data for the second layer nodes */
			List<CorpRelFeaturizedDatum> subdata = data.getFeaturizedDataUnderLabel(crl, false);
			/* If there is not data under this label, it should be unnecessary to build a model for it */
			if(subdata.size() > 0){
				ModelCReg submodel = new ModelCReg(properties.getCregCommandPath(), data.getLabelChildren(crl));
				if(!submodel.train(subdata, outputPath+".second." + crl.name())){
					return false;
				}
				/*
				  TODO:Select second layer features here...
				 */	
				secondLayerModels.put(crl, submodel);
				List<CorpRelLabel> subsublabels = data.getLabelChildren(crl);
				for(CorpRelLabel scrl : subsublabels){
					/* Here we come to the third layer labels */
					/*
					  TODO:Select second layer features here...
					 */
					List<CorpRelFeaturizedDatum> subsubdata = data.getFeaturizedDataUnderLabel(scrl, false);
					if(subsubdata.size() > 0){
						ModelCReg subsubmodel = new ModelCReg(properties.getCregCommandPath(), data.getLabelChildren(scrl));
						if(!submodel.train(subdata, outputPath+".third." + scrl.name())){
							return false;
						}
						/*
						  TODO:Select second layer features here...
						 */	
						thirdLayerModels.put(scrl, subsubmodel);
					}
				}
			}
		}
		return true;
	}

	@Override
	public List<Pair<CorpRelFeaturizedDatum, HashMap<CorpRelLabel, Double>>> posterior(
			List<CorpRelFeaturizedDatum> data) {
		// TODO Auto-generated method stub
		return null;
	}

}
