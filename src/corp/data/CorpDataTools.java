package corp.data;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import corp.util.BrownClusterer;
import corp.util.CorpProperties;
import corp.util.OutputWriter;
import corp.util.StringUtil;

public class CorpDataTools {
	private CorpProperties properties;
	private OutputWriter output;
	
	private Map<String, Gazetteer> gazetteers;
	private Map<String, CorpMetaData> metaData;
	private Map<String, StringUtil.StringTransform> cleanFns;
	private Map<String, StringUtil.StringCollectionTransform> collectionFns;
	private Map<String, BrownClusterer> clusterers;
	
	public CorpDataTools(CorpProperties properties, OutputWriter output) {
		this.properties = properties;
		this.output = output;
		this.gazetteers = new HashMap<String, Gazetteer>();
		this.metaData = new HashMap<String, CorpMetaData>();
		this.cleanFns = new HashMap<String, StringUtil.StringTransform>();
		this.collectionFns = new HashMap<String, StringUtil.StringCollectionTransform>();
		this.clusterers = new HashMap<String, BrownClusterer>();
		
		this.collectionFns.put("Prefixes", StringUtil.getPrefixesFn());
		this.collectionFns.put("None", null);
		
		this.gazetteers.put("StopWordGazetteer", new Gazetteer("StopWord", this.properties.getStopWordGazetteerPath()));
		this.gazetteers.put("NGramStopWordGazetteer", new Gazetteer("NGramStopWord", this.properties.getNGramStopWordGazetteerPath()));
		this.gazetteers.put("BloombergCorpTickerGazetteer", new Gazetteer("BloombergCorpTicker", this.properties.getBloombergCorpTickerGazetteerPath()));
			
		this.cleanFns.put("DefaultCleanFn", StringUtil.getDefaultCleanFn());
		this.cleanFns.put("StopWordCleanFn_StopWord", StringUtil.getStopWordsCleanFn(this.gazetteers.get("StopWordGazetteer")));
		this.cleanFns.put("StopWordCleanFn_NGramStopWord", StringUtil.getStopWordsCleanFn(this.gazetteers.get("NGramStopWordGazetteer")));
		this.cleanFns.put("CorpKeyFn_BloombergCorpTicker_StopWordCleanFn_StopWord", StringUtil.getCorpKeyFn(this.gazetteers.get("BloombergCorpTickerGazetteer"), this.cleanFns.get("StopWordCleanFn_StopWord")));
		
		this.gazetteers.put("CorpScrapedGazetteer", new Gazetteer("CorpScraped", this.properties.getCorpScrapedGazetteerPath()));
		this.gazetteers.put("CorpMetaDataGazetteer", new Gazetteer("CorpMetaData", this.properties.getCorpMetaDataGazetteerPath()));
		this.gazetteers.put("BloombergMetaDataGazetteer", new Gazetteer("BloombergMetaData", this.properties.getBloombergMetaDataGazetteerPath()));
		this.gazetteers.put("StopWordCorpScrapedGazetteer", new Gazetteer("StopWordCorpScraped", this.properties.getCorpScrapedGazetteerPath(), this.cleanFns.get("StopWordCleanFn_StopWord")));
		
		this.gazetteers.put("CorpKeyCorpScrapedGazetteer", new Gazetteer("CorpKeyCorpScraped", this.properties.getCorpScrapedGazetteerPath(), this.cleanFns.get("CorpKeyFn_BloombergCorpTicker_StopWordCleanFn_StopWord")));
		this.gazetteers.put("CorpKeyCorpMetaDataGazetteer", new Gazetteer("CorpKeyCorpMetaData", this.properties.getCorpMetaDataGazetteerPath(), this.cleanFns.get("CorpKeyFn_BloombergCorpTicker_StopWordCleanFn_StopWord")));
		this.gazetteers.put("CorpKeyBloombergMetaDataGazetteer", new Gazetteer("CorpKeyBloombergMetaData", this.properties.getBloombergMetaDataGazetteerPath(), this.cleanFns.get("CorpKeyFn_BloombergCorpTicker_StopWordCleanFn_StopWord")));
		
		this.gazetteers.put("NonCorpScrapedGazetteer", new Gazetteer("NonCorpScraped", this.properties.getNonCorpScrapedGazetteerPath()));
				
		this.metaData.put("CorpMetaData", new CorpMetaData("Corp", this.properties.getCorpMetaDataPath()));
		this.metaData.put("BloombergMetaData", new CorpMetaData("Bloomberg", this.properties.getBloombergMetaDataPath()));
		
		this.clusterers.put("None", null);
	}
	
	public CorpDataTools(CorpProperties properties,
						 OutputWriter output,
						 Map<String, Gazetteer> gazetteers,
						 Map<String, CorpMetaData> metaData,
						 Map<String, StringUtil.StringTransform> cleanFns,
						 Map<String, StringUtil.StringCollectionTransform> collectionFns,
						 Map<String, BrownClusterer> clusterers) {
		this.properties = properties;
		this.output = output;
		this.gazetteers = gazetteers;
		this.metaData = metaData;
		this.cleanFns = cleanFns;
		this.collectionFns = collectionFns;
		this.clusterers = clusterers;
	}
	
	public Gazetteer getGazetteer(String name) {
		return this.gazetteers.get(name);
	}
	
	public CorpMetaData getMetaData(String name) {
		return this.metaData.get(name);
	}
	
	public StringUtil.StringTransform getCleanFn(String name) {
		return this.cleanFns.get(name);
	}
	
	public StringUtil.StringCollectionTransform getCollectionFn(String name) {
		return this.collectionFns.get(name);
	}
	
	public BrownClusterer getClusterer(String name) {
		if (!name.contains("Brown") || !name.contains("_"))
			return null;
		String[] nameParts = name.split("_");
		int numClusters = Integer.parseInt(nameParts[0]);
		
		if (!this.clusterers.containsKey(name)) {
			this.clusterers.put(name, 
					new BrownClusterer(name, 
										this.properties.getBrownClustererCommandPath(), 
										new File(this.properties.getBrownClustererSourceDocument()), 
										numClusters,
										this.output));
		}
		
		return this.clusterers.get(name);
	}
}
