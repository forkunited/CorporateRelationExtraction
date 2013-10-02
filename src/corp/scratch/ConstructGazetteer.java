package corp.scratch;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import corp.data.CorpMetaData;
import corp.data.Gazetteer;
import corp.data.annotation.AnnotationCache;
import corp.data.annotation.CorpDocumentSet;
import corp.data.annotation.CorpDocumentTokenSpan;
import corp.data.annotation.CorpRelDatum;
import corp.data.feature.CorpRelFeaturizedDataSet;
import corp.util.CorpProperties;
import corp.util.OutputWriter;
import corp.util.StringUtil;

/**
 * Stubs for constructing clean Gazetteers from old messy Gazetteer files
 * 
 * @author Bill
 *
 */
public class ConstructGazetteer {
	public static void main(String[] args) {
		constructOrgEntitySet("/home/wmcdowel/sloan/Data/Gazetteer/OrgEntity.gazetteer");
		/*constructFromOldGazetteer("C:/Users/Bill/Documents/projects/NoahsARK/sloan/Data/Gazetteer/Legacy/ncorplist",
								 "C:/Users/Bill/Documents/projects/NoahsARK/sloan/Data/Gazetteer/NonCorpScraped.gazetteer");
		constructFromOldGazetteer("C:/Users/Bill/Documents/projects/NoahsARK/sloan/Data/Gazetteer/Legacy/corp",
				 				  "C:/Users/Bill/Documents/projects/NoahsARK/sloan/Data/Gazetteer/CorpScraped.gazetteer");
		constructFromOldGazetteer("C:/Users/Bill/Documents/projects/NoahsARK/sloan/Data/Gazetteer/Legacy/stopwords",
				  				  "C:/Users/Bill/Documents/projects/NoahsARK/sloan/Data/Gazetteer/StopWord.gazetteer");
		constructFromMetaData("C:/Users/Bill/Documents/projects/NoahsARK/sloan/Data/MetaData/Corp.metadata",
				  			  "C:/Users/Bill/Documents/projects/NoahsARK/sloan/Data/Gazetteer/CorpMetaData.gazetteer");
		*/
	}
	
	public static void constructFromOldGazetteer(String inputFile, String outputFile) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(inputFile));
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			String line = null;
			
			int id = 0;
			while ((line = br.readLine()) != null) {
				String content = line.split("\\t")[0].trim();
				if (content.equals("edit"))
					continue;
				bw.write(id + "\t");
				System.out.print(id + "\t");
				if (content.matches(".+\\(.+\\)")) {
					String s1 = content.substring(0, content.indexOf("("));
					String s2 = content.substring(content.indexOf("(")+1, content.lastIndexOf(")"));
					bw.write(content + "\t" + s1 + "\t" + s2 + "\n");
					System.out.print(content + "\t" + s1 + "\t" + s2 + "\n");
				} else {
					bw.write(content + "\n");
					System.out.print(content + "\n");
				}
				
				id++;
			}
			br.close();
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void constructFromMetaData(String inputFile, String outputFile) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			
			CorpMetaData metaData = new CorpMetaData("gazetteerSource", inputFile);
			Map<String, CorpMetaData.Attributes> attributes = metaData.getAttributes();
			for (Entry<String, CorpMetaData.Attributes> entry : attributes.entrySet()) {
				bw.write(entry.getKey() + "\t" + entry.getValue().getName() + "\t" + entry.getValue().getTicker() + "\n");
				System.out.print(entry.getKey() + "\t" + entry.getValue().getName() + "\t" + entry.getValue().getTicker() + "\n");
			}
			
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void constructOrgEntitySet(String outputFile) {
		CorpProperties properties = new CorpProperties("corp.properties");
		CorpMetaData metaData = new CorpMetaData("GazetteerSource", properties.getCorpMetaDataPath());
		
		AnnotationCache annotationCache = new AnnotationCache(
				properties.getStanfordAnnotationDirPath(),
				properties.getStanfordAnnotationCacheSize(),
				properties.getStanfordCoreMapDirPath(),
				properties.getStanfordCoreMapCacheSize(),
				new OutputWriter()
		);
			
		CorpDocumentSet documentSet = new CorpDocumentSet(
				properties.getCorpRelDirPath(), 
				annotationCache,
				4,
				100,
				0,
				new OutputWriter()
		);
		
		CorpRelFeaturizedDataSet dataSet = new CorpRelFeaturizedDataSet(documentSet, new OutputWriter());
		List<CorpRelDatum> data = dataSet.getData();
		
		final StringUtil.StringTransform cleanFn = StringUtil.getStopWordsCleanFn(new Gazetteer("StopWord", properties.getStopWordGazetteerPath()));
		StringUtil.StringTransform keyFn = new StringUtil.StringTransform() {
			@Override
			public String transform(String str) {
				return cleanFn.transform(str).replace(" ", "_");
			}
		};
				
				
		TreeMap<String, HashSet<String>> entitySet = new TreeMap<String, HashSet<String>>();
		HashSet<String> addedNames = new HashSet<String>();
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			
			Map<String, CorpMetaData.Attributes> attributes = metaData.getAttributes();
			for (Entry<String, CorpMetaData.Attributes> entry : attributes.entrySet()) {
				String key = keyFn.transform(entry.getValue().getName());
				if (!entitySet.containsKey(key))
					entitySet.put(key, new HashSet<String>());
				String cleanName = cleanFn.transform(entry.getValue().getName());
				String cleanTicker = cleanFn.transform(entry.getValue().getTicker()); 
				entitySet.get(key).add(cleanName);
				entitySet.get(key).add(cleanTicker);
				addedNames.add(cleanName);
				addedNames.add(cleanTicker);
			}
			
			for (CorpRelDatum datum : data) {
				String cleanAuthor = cleanFn.transform(datum.getAuthorCorpName());
				if (cleanAuthor.length() > 0 && !addedNames.contains(cleanAuthor)) {
					String key = keyFn.transform(datum.getAuthorCorpName());
					if (!entitySet.containsKey(key))
						entitySet.put(key, new HashSet<String>());
					entitySet.get(key).add(cleanAuthor);
				}
				
				List<CorpDocumentTokenSpan> otherOrgs = datum.getOtherOrgTokenSpans();
				for (CorpDocumentTokenSpan otherOrg : otherOrgs) {
					String key = keyFn.transform(otherOrg.toString());
					if (key.length() == 0)
						continue;
					
					String cleanOrg = cleanFn.transform(otherOrg.toString());
					if (!addedNames.contains(cleanOrg)) {
						if (!entitySet.containsKey(key))
							entitySet.put(key, new HashSet<String>());
						entitySet.get(key).add(cleanOrg);
					}
				}
			}
			
			NavigableMap<String, HashSet<String>> sortedEntities = entitySet.descendingMap();
			for (Entry<String, HashSet<String>> entry : sortedEntities.entrySet()) {
				bw.write(entry.getKey() + "\t");
				System.out.print(entry.getKey() + "\t");
				for (String value : entry.getValue()) {
					bw.write(value + "\t");
					System.out.print(value + "\t");
				}
				bw.write("\n");
				System.out.print("\n");
			}
			
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
