package corp.scratch;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
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
import corp.util.CorpKeyFn;
import corp.util.CorpProperties;
import ark.util.OutputWriter;
import corp.util.StringUtil;

/**
 * ConstructGazetteer contains functions for putting data from various sources
 * into the gazetteer format used by the ark.data.Gazetteer class from the 
 * ARKWater project.  These will probably be useless in the future (and they
 * contain hard-coded paths), but I've kept them in the project just in case
 * they can serve as examples for something in the future.
 * 
 * @author Bill McDowell
 *
 */
public class ConstructGazetteer {
	public static void main(String[] args) {
		//constructOrgEntitySet("/home/wmcdowel/sloan/Data/Gazetteer/OrgEntity.gazetteer");
		/*constructFromOldGazetteer("C:/Users/Bill/Documents/projects/NoahsARK/sloan/Data/Gazetteer/Legacy/ncorplist",
								 "C:/Users/Bill/Documents/projects/NoahsARK/sloan/Data/Gazetteer/NonCorpScraped.gazetteer");
		constructFromOldGazetteer("C:/Users/Bill/Documents/projects/NoahsARK/sloan/Data/Gazetteer/Legacy/corp",
				 				  "C:/Users/Bill/Documents/projects/NoahsARK/sloan/Data/Gazetteer/CorpScraped.gazetteer");
		constructFromOldGazetteer("C:/Users/Bill/Documents/projects/NoahsARK/sloan/Data/Gazetteer/Legacy/stopwords",
				  				  "C:/Users/Bill/Documents/projects/NoahsARK/sloan/Data/Gazetteer/StopWord.gazetteer");
		constructFromMetaData("C:/Users/Bill/Documents/projects/NoahsARK/sloan/Data/MetaData/Corp.metadata",
				  			  "C:/Users/Bill/Documents/projects/NoahsARK/sloan/Data/Gazetteer/CorpMetaData.gazetteer");
		*/
		//constructFromMetaData("C:/Users/Bill/Documents/projects/NoahsARK/sloan/Data/MetaData/Bloomberg.metadata",
		//					  "C:/Users/Bill/Documents/projects/NoahsARK/sloan/Data/Gazetteer/BloombergMetaData.gazetteer");
		//constructTickerGazetteer("C:/Users/Bill/Documents/projects/NoahsARK/sloan/Data/Gazetteer/BloombergCorpTicker.gazetteer");
		constructNonCorpInitialismGazetteer("C:/Users/Bill/Documents/projects/NoahsARK/sloan/Data/Gazetteer/NonCorpInitialism.gazetteer");
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
				bw.write(entry.getKey() + "\t" + entry.getValue().getNames().get(0) + "\t");
				System.out.print(entry.getKey() + "\t" + entry.getValue().getNames().get(0) + "\t"); 
				HashSet<String> tickers = new HashSet<String>();
				tickers.addAll(entry.getValue().getTickers());
				
				for (String ticker : tickers) {
					bw.write(ticker + "\t");
					System.out.print(ticker + "\t");
				}
				bw.write("\n");
				System.out.print("\n");
			}
			
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void constructOrgEntitySet(String outputFile) {
		CorpProperties properties = new CorpProperties();
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
				new OutputWriter(),
				metaData
		);
		
		CorpRelFeaturizedDataSet dataSet = new CorpRelFeaturizedDataSet(documentSet, new OutputWriter());
		List<CorpRelDatum> data = dataSet.getData();
		
		final StringUtil.StringTransform cleanFn = StringUtil.getStopWordsCleanFn(new Gazetteer("StopWord", properties.getStopWordGazetteerPath()));
		StringUtil.StringTransform keyFn = new StringUtil.StringTransform() {
			@Override
			public String transform(String str) {
				str = cleanFn.transform(str);
				String[] strParts = str.split(" ");
				StringBuilder transformedStr = new StringBuilder();
				for (String strPart : strParts) {
					if (strPart.length() > 1)
						transformedStr.append(strPart).append("_");
				}
				transformedStr.delete(transformedStr.length() - 1, transformedStr.length());
				
				return transformedStr.toString();
			}
		};
				
				
		TreeMap<String, HashSet<String>> entitySet = new TreeMap<String, HashSet<String>>();
		HashSet<String> addedNames = new HashSet<String>();
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			
			/*Map<String, CorpMetaData.Attributes> attributes = metaData.getAttributes();
			for (Entry<String, CorpMetaData.Attributes> entry : attributes.entrySet()) {
				String key = keyFn.transform(entry.getValue().getName());
				if (!entitySet.containsKey(key))
					entitySet.put(key, new HashSet<String>());
				String cleanTicker = cleanFn.transform(entry.getValue().getTicker()); 
				entitySet.get(key).add(entry.getValue().getName());
				entitySet.get(key).add(cleanTicker);
				addedNames.add(cleanTicker);
			}*/
			
			for (CorpRelDatum datum : data) {
				String cleanAuthor = cleanFn.transform(datum.getAuthorCorpName());
				if (cleanAuthor.length() > 0 && !addedNames.contains(cleanAuthor)) {
					String key = keyFn.transform(datum.getAuthorCorpName());
					if (!entitySet.containsKey(key))
						entitySet.put(key, new HashSet<String>());
					entitySet.get(key).add(datum.getAuthorCorpName());
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
						entitySet.get(key).add(otherOrg.toString());
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
	
	public static void constructTickerGazetteer(String outputFile) {
		CorpProperties properties = new CorpProperties();
		Gazetteer stopWordGazetteer = new Gazetteer("StopWord", properties.getStopWordGazetteerPath());
		StringUtil.StringTransform stopWordCleanFn = StringUtil.getStopWordsCleanFn(stopWordGazetteer);
		StringUtil.StringTransform corpKeyFn = new CorpKeyFn(null, stopWordCleanFn);
		CorpMetaData corpMetaData = new CorpMetaData("Corp", properties.getCorpMetaDataPath());
		CorpMetaData bloombergMetaData = new CorpMetaData("Bloomberg", properties.getBloombergMetaDataPath());
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			Map<String, HashSet<String>> corpKeysToTickers = new HashMap<String, HashSet<String>>();
			
			Map<String, CorpMetaData.Attributes> corpAttributes = corpMetaData.getAttributes();
			for (Entry<String, CorpMetaData.Attributes> entry : corpAttributes.entrySet()) {
				String corpKey = corpKeyFn.transform(entry.getValue().getNames().get(0));
				if (corpKey.length() == 0)
					continue;
				
				if (!corpKeysToTickers.containsKey(corpKey))
					corpKeysToTickers.put(corpKey, new HashSet<String>());
				List<String> tickers = entry.getValue().getTickers();
				for (String ticker : tickers) { 
					corpKeysToTickers.get(corpKey).add(ticker);
				}
			}
			
			Map<String, CorpMetaData.Attributes> bloombergAttributes = bloombergMetaData.getAttributes();
			for (Entry<String, CorpMetaData.Attributes> entry : bloombergAttributes.entrySet()) {
				String corpKey = corpKeyFn.transform(entry.getValue().getNames().get(0));
				if (corpKey.length() == 0)
					continue;
				
				if (!corpKeysToTickers.containsKey(corpKey))
					corpKeysToTickers.put(corpKey, new HashSet<String>());
				List<String> tickers = entry.getValue().getTickers();
				for (String ticker : tickers) { 
					corpKeysToTickers.get(corpKey).add(ticker);
				}
			}
		
			for (Entry<String, HashSet<String>> entry : corpKeysToTickers.entrySet()) {
				if (entry.getValue().size() == 0)
					continue;
				
				bw.write(entry.getKey() + "\t");
				System.out.print(entry.getKey() + "\t"); 
				for (String ticker : entry.getValue()) {
					bw.write(ticker + "\t");
					System.out.print(ticker + "\t");
				}
				bw.write("\n");
				System.out.print("\n");
			}
			
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void constructNonCorpInitialismGazetteer(String outputFile) {
		CorpProperties properties = new CorpProperties();
		Gazetteer stopWordGazetteer = new Gazetteer("StopWord", properties.getStopWordGazetteerPath());
		StringUtil.StringTransform stopWordCleanFn = StringUtil.getStopWordsCleanFn(stopWordGazetteer);
		StringUtil.StringTransform corpKeyFn = new CorpKeyFn(null, stopWordCleanFn);
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			BufferedReader br = new BufferedReader(new FileReader(properties.getNonCorpScrapedGazetteerPath()));
			Map<String, HashSet<String>> nonCorpKeysToInitialisms = new HashMap<String, HashSet<String>>();
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] lineParts = line.split("\t");
				if (lineParts.length < 3)
					continue;
				String name = lineParts[lineParts.length - 2];
				String initialism = lineParts[lineParts.length - 1];
			
				if (!initialism.toUpperCase().equals(initialism))
					continue;
				String nonCorpKey = corpKeyFn.transform(name);
				if (!nonCorpKeysToInitialisms.containsKey(nonCorpKey))
					nonCorpKeysToInitialisms.put(nonCorpKey, new HashSet<String>());
				nonCorpKeysToInitialisms.get(nonCorpKey).add(initialism);
			}
			
			for (Entry<String, HashSet<String>> entry : nonCorpKeysToInitialisms.entrySet()) {
				if (entry.getValue().size() == 0)
					continue;
				
				bw.write(entry.getKey() + "\t");
				System.out.print(entry.getKey() + "\t");
				for (String ticker : entry.getValue()) {
					bw.write(ticker + "\t");
					System.out.print(ticker + "\t");
				}
				
				bw.write("\n");
				System.out.print("\n");
			}
			
			bw.write("new_york_stock_exchange\tNYSE\n");
			System.out.print("new_york_stock_exchange\tNYSE\n");
			
			bw.close();
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
