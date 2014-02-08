package corp.test;

/*import static org.junit.Assert.*;

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import corp.data.annotation.AnnotationCache;
import corp.data.annotation.CorpDocumentSet;
import corp.data.feature.CorpRelFeatureNGramContext;
import corp.data.feature.CorpRelFeatureNGramDep;
import corp.data.feature.CorpRelFeatureNGramSentence;
import corp.data.feature.CorpRelFeaturizedDataSet;
import corp.data.feature.CorpRelFeaturizedDatum;
import corp.util.CorpProperties;
import corp.util.OutputWriter;*/

/**
 * 
 * CorpRelFeatureTest was originally used to ensure that features were being
 * computed accurately, but it hasn't been updated to reflect changes in the
 * code.  I've kept some of the old code here in case it comes in handy in the
 * future, but it might just be a good idea to start over with testing if 
 * that seems necessary.
 * 
 * @author Bill McDowell
 *
 */
public class CorpRelFeatureTest {
	/*private static OutputWriter output;
	private static CorpProperties properties;
	private static CorpRelFeaturizedDataSet dataSet;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		CorpRelFeatureTest.properties = new CorpProperties("corp.properties");
		CorpRelFeatureTest.output = new OutputWriter();
				
		CorpRelFeatureTest.output.debugWriteln("Loading Annotation Cache...");
		AnnotationCache annotationCache = new AnnotationCache(
				CorpRelFeatureTest.properties.getStanfordAnnotationDirPath(),
				CorpRelFeatureTest.properties.getStanfordAnnotationCacheSize(),
				CorpRelFeatureTest.properties.getStanfordCoreMapDirPath(),
				CorpRelFeatureTest.properties.getStanfordCoreMapCacheSize(),
				CorpRelFeatureTest.output
			);
			
			CorpRelFeatureTest.output.debugWriteln("Loading document set...");
			CorpDocumentSet documentSet = new CorpDocumentSet(
					CorpRelFeatureTest.properties.getCorpRelTestDirPath(), 
					annotationCache,
					1,
					0,
					CorpRelFeatureTest.output
			);
			
			CorpRelFeatureTest.output.debugWriteln("Loaded " + documentSet.getDocuments().size() + " documents.");
			CorpRelFeatureTest.output.debugWriteln("Constructing data set...");
			
			CorpRelFeatureTest.dataSet = new CorpRelFeaturizedDataSet(documentSet, CorpRelFeatureTest.output);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception { }

	@Test
	public void CorpRelFeatureNGramSentenceTest() {
		CorpRelFeatureNGramSentence nGramSentence = new CorpRelFeatureNGramSentence(3,1);
		CorpRelFeaturizedDataSet data = new CorpRelFeaturizedDataSet(CorpRelFeatureTest.output);
		
		data.addData(CorpRelFeatureTest.dataSet.getData());
		data.addFeature(nGramSentence);
		nGramSentence.init(data.getData());
		
		List<String> names = data.getFeatureNames();
		for (String name : names)
			CorpRelFeatureTest.output.debugWrite(name + " ");
		CorpRelFeatureTest.output.debugWrite("\n");
		List<CorpRelFeaturizedDatum> featurizedDatums = data.getFeaturizedData();
		for (CorpRelFeaturizedDatum datum : featurizedDatums) {
			CorpRelFeatureTest.output.debugWriteln(datum.toString());
			CorpRelFeatureTest.output.debugWriteln(datum.getFeatureValues().toString());
		}
	}
	
	@Test
	public void CorpRelFeatureNGramDepTest() {
		CorpRelFeatureNGramDep nGramDep = new CorpRelFeatureNGramDep(3,1, CorpRelFeatureNGramDep.Mode.ParentsAndChildren,
				true);
		CorpRelFeaturizedDataSet data = new CorpRelFeaturizedDataSet(CorpRelFeatureTest.output);
		
		data.addData(CorpRelFeatureTest.dataSet.getData());
		data.addFeature(nGramDep);
		nGramDep.init(data.getData());
		
		List<String> names = data.getFeatureNames();
		for (String name : names)
			CorpRelFeatureTest.output.debugWrite(name + " ");
		CorpRelFeatureTest.output.debugWrite("\n");
		List<CorpRelFeaturizedDatum> featurizedDatums = data.getFeaturizedData();
		for (CorpRelFeaturizedDatum datum : featurizedDatums) {
			CorpRelFeatureTest.output.debugWriteln(datum.toString());
			CorpRelFeatureTest.output.debugWriteln(datum.getFeatureValues().toString());
		}
	}
	
	@Test
	public void CorpRelFeatureNGramContextTest() {
		CorpRelFeatureNGramContext nGramContext = new CorpRelFeatureNGramContext(1, 1, 1);
		CorpRelFeaturizedDataSet data = new CorpRelFeaturizedDataSet(CorpRelFeatureTest.output);
		
		data.addData(CorpRelFeatureTest.dataSet.getData());
		data.addFeature(nGramContext);
		nGramContext.init(data.getData());
		
		List<String> names = data.getFeatureNames();
		for (String name : names)
			CorpRelFeatureTest.output.debugWrite(name + " ");
		CorpRelFeatureTest.output.debugWrite("\n");
		List<CorpRelFeaturizedDatum> featurizedDatums = data.getFeaturizedData();
		for (CorpRelFeaturizedDatum datum : featurizedDatums) {
			CorpRelFeatureTest.output.debugWriteln(datum.toString());
			CorpRelFeatureTest.output.debugWriteln(datum.getFeatureValues().toString());
		}
	}*/
}
