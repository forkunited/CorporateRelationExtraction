package corp.data;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ark.util.FileUtil;

/**
 * 
 * CorpMetaData represents metadata represents deserialized attributes of 
 * corporations. In the past, we've used this class to load meta-data 
 * that was gathered from Compustat and Bloomberg, but this CorpMetaData
 * can be extended to include metadata from other sources.
 * 
 * A metadata file from which the class loads its content starts with a 
 * heading line of the form:
 * 
 * [attribute_name_1]	[attribute_name_2]	...	[attribute_name_n]
 * 
 * Followed by content lines of the form:
 * 
 * [ID]	[attribute_values_1]	[attribute_values_2]	...	[attribute_values_n]
 *
 * Currently, attribute names can be:
 * 
 * cik: Numbers for the corporation
 * name: Name of the corporation
 * ticker: Ticker symbols for the corporation
 * sic: SIC codes for the corporation
 * country: Countries in which the corporation is located
 * type: Types for the corporation
 * industry: Industries for the corporation
 * 
 * Several values for each attribute can be given for each corporation through
 * comma-separated lists (in each [attribute_values_i] in the input file).
 * 
 * @author Bill McDowell
 *
 */
public class CorpMetaData {
	public enum Attribute {
		CIK,
		NAME,
		TICKER,
		SIC,
		COUNTRY,
		TYPE,
		INDUSTRY
	}
	
	public class Attributes {
		private List<String> ciks;
		private List<String> names;
		private List<String> tickers;
		private List<String> sics;
		private List<String> countries;
		private List<String> types;
		private List<String> industries;
		
		public Attributes(List<String> ciks, 
						  List<String> names, 
						  List<String> tickers, 
						  List<String> sics, 
						  List<String> countries, 
						  List<String> types, 
						  List<String> industries) {
			this.ciks = ciks;
			this.names = names;
			this.tickers = tickers;
			this.sics = sics;
			this.countries = countries;
			this.types = types;
			this.industries = industries;
		}
		
		public List<String> getCiks() {
			return this.ciks;
		}
		
		public List<String> getNames() {
			return this.names;
		}
		
		public List<String> getTickers() {
			return this.tickers;
		}
		
		public List<String> getSics() {
			return this.sics;
		}
		
		public List<String> getCountries() {
			return this.countries;
		}
		
		public List<String> getTypes() {
			return this.types;
		}
		
		public List<String> getIndustries() {
			return this.industries;
		}
		
		public List<String> getAttribute(Attribute attribute) {
			if (attribute == Attribute.CIK) {
				return getCiks();
			} else if (attribute == Attribute.NAME) {
				return getNames();
			} else if (attribute == Attribute.SIC) {
				return getSics();
			} else if (attribute == Attribute.TICKER) {
				return getTickers();
			} else if (attribute == Attribute.COUNTRY) {
				return getCountries();
			} else if (attribute == Attribute.TYPE) {
				return getTypes();
			} else if (attribute == Attribute.INDUSTRY) {
				return getIndustries();
			} else {
				return null;
			}
		}
	}
	
	private String name;
	private Map<String, CorpMetaData.Attributes> idsToAttributes;
	
	public CorpMetaData(String name, String sourceFilePath) {
		this.name = name;
		this.idsToAttributes = new HashMap<String, Attributes>();
		
		try {
			BufferedReader br = FileUtil.getFileReader(sourceFilePath);
			String headingLine = br.readLine();
			if (headingLine == null) {
				br.close();
				throw new IllegalArgumentException();
			}
			String[] headings = headingLine.split("\\t");
			int cikCol = -1, nameCol = -1, tickerCol = -1, sicCol = -1, countryCol = -1, typeCol = -1, industryCol = -1;
			for (int i = 0; i < headings.length; i++) {
				if (headings[i].equals("cik"))
					cikCol = i;
				else if (headings[i].equals("name"))
					nameCol = i;
				else if (headings[i].equals("ticker"))
					tickerCol = i;
				else if (headings[i].equals("sic"))
					sicCol = i;
				else if (headings[i].equals("country"))
					countryCol = i;
				else if (headings[i].equals("type"))
					typeCol = i;
				else if (headings[i].equals("industry"))
					industryCol = i;
			}
			
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] lineValues = line.trim().split("\\t");
				
				String id = lineValues[0];
				
				List<String> cikValues = new ArrayList<String>(1);
				List<String> nameValues = new ArrayList<String>(1);
				List<String> tickerValues = new ArrayList<String>(1);
				List<String> sicValues = new ArrayList<String>(1);
				List<String> countryValues = new ArrayList<String>(1);
				List<String> typeValues = new ArrayList<String>(1);
				List<String> industryValues = new ArrayList<String>(1);
				
				if (cikCol >= 0) {
					cikValues.add(lineValues[cikCol]);
				}
				
				if (nameCol >= 0) {
					nameValues.add(lineValues[nameCol]);
				}
				
				if (tickerCol >= 0) {
					String[] tickers = lineValues[tickerCol].split(",");
					for (int i = 0; i < tickers.length; i++)
						tickerValues.add(tickers[i].trim());
				}
				
				if (sicCol >= 0) {
					sicValues.add(lineValues[sicCol].trim().replace(" ", "_"));
				}
				
				if (countryCol >= 0) {
					String[] countries = lineValues[countryCol].split(",");
					for (int i = 0; i < countries.length; i++)
						countryValues.add(countries[i].trim().replace(" ", "_"));
				}
				
				if (typeCol >= 0) {
					String[] types = lineValues[typeCol].split(",");
					for (int i = 0; i < types.length; i++)
						typeValues.add(types[i].trim().replace(" ", "_"));
				}
				
				if (industryCol >= 0 && industryCol < lineValues.length) {
					String[] industries = lineValues[industryCol].split(",");
					for (int i = 0; i < industries.length; i++)
						industryValues.add(industries[i].trim().replace(" ", "_"));
				}
				
				this.idsToAttributes.put(id, new Attributes(cikValues, nameValues, tickerValues, sicValues, countryValues, typeValues, industryValues));
			}
			
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getName() {
		return this.name;
	}
	
	public CorpMetaData.Attributes getAttributesById(String id) {
		if (this.idsToAttributes.containsKey(id))
			return this.idsToAttributes.get(id);
		else
			return null;
	}
	
	public Map<String, CorpMetaData.Attributes> getAttributes() {
		return this.idsToAttributes;
	}
	
	public List<String> getAttributeById(String id, CorpMetaData.Attribute attribute) {
		CorpMetaData.Attributes attributes = getAttributesById(id);
		if (attributes == null)
			return null;
		else
			return attributes.getAttribute(attribute);
	}
	
	public Map<String, String> getAttributeMap(CorpMetaData.Attribute keyAtt, CorpMetaData.Attribute valueAtt) {
		Map<String, String> attributeMap = new HashMap<String, String>();
		
		for (Entry<String, CorpMetaData.Attributes> entry : this.idsToAttributes.entrySet()) {
			List<String> keyAttValues = entry.getValue().getAttribute(keyAtt);
			List<String> valueAttValues = entry.getValue().getAttribute(valueAtt);
			for (String keyAttValue : keyAttValues) {
				for (String valueAttValue : valueAttValues) {
					attributeMap.put(keyAttValue, valueAttValue);
				}
			}
		}
		
		return attributeMap;
	}
}
