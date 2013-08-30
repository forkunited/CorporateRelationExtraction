package corp.test;

import java.util.List;

import corp.data.annotation.CorpDocumentSet;
import corp.data.feature.CorpRelFeatureNGramContext;
import corp.data.feature.CorpRelFeaturizedDataSet;
import corp.data.feature.CorpRelFeaturizedDatum;

/**
 * For periodically testing out short temporary snippets of code
 */
public class Scratch {
	public static void main(String[] args) {
		/* Here's some code to get a basic sense of how to use the 
		 * the redesigned representation of the data
		 * 
		 *  We can go through this tomorrow afternoon
		 */
		
		/* This is the document set.  It represents a set of annotated documents. 
		 * TODO: Get the paths from a configuration file */
		CorpDocumentSet documentSet = new CorpDocumentSet("pathToBryansAnnotations", "pathToStanfordAnnotations");
		
		/* Construct corporate relation data set from documents */
		CorpRelFeaturizedDataSet dataSet = new CorpRelFeaturizedDataSet(documentSet);
		
		/* Add feature to data set */
		dataSet.addFeature(new CorpRelFeatureNGramContext(documentSet.getDocuments(), dataSet.getData(), 0 /* n */, 
				0 /*minFeatureOccurrence*/,
				0 /* contextWindowSize */));
		
		/* Compute featurized data */
		List<CorpRelFeaturizedDatum> featurizedData = dataSet.getFeaturizedData();
		
		/* Here's a list of some useful functions in dataSet */
		dataSet.getFeatureNames(); // Gets all feature names
		dataSet.getData(); // Gets all the data in the set
		dataSet.getDataInLabel(/* label */null); // Gets all data directly within a given label
		dataSet.getDataUnderLabel(/* rootLabel */null, /* includeRootData */false); // Gets all data under some branch of the label hierarchy
		dataSet.getLabelChildren(/*label */ null); // Get all children of a label in the label hierarchy
		
		/* This iterates over the featurized data.  Each "datum" represents all of the features computed for a single
		 * corporate relationship.
		 */
		for (CorpRelFeaturizedDatum featurizedDatum : featurizedData) {
			/* Here's a list of some useful functions in featurized datum */
			featurizedDatum.getAuthorCorpName(); // Gets the author corporation name
			featurizedDatum.getDocument(); // Gets the original document
			featurizedDatum.getDocument().getAnnotation(); // Gets stanford annotation
			featurizedDatum.getFeatureValues(); // Gets feature values for datum
			featurizedDatum.getLabel(/* validLabels */null); // Gets relationship label that's within valid labels
			featurizedDatum.getLabelPath(); // Gets all the labels in the path
		}
	}
}
