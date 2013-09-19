package corp.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class CorpMetaData {
	public enum Attribute {
		CIK,
		NAME,
		TICKER,
		SIC
	}
	
	public class Attributes {
		private String cik;
		private String name;
		private String ticker;
		private String sic;
		
		public Attributes(String cik, String name, String ticker, String sic) {
			this.cik = cik;
			this.name = name;
			this.ticker = ticker;
			this.sic = sic;
		}
		
		public String getCik() {
			return this.cik;
		}
		
		public String getName() {
			return this.name;
		}
		
		public String getTicker() {
			return this.ticker;
		}
		
		public String getSic() {
			return this.sic;
		}
		
		public String getAttribute(Attribute attribute) {
			if (attribute == Attribute.CIK) {
				return getCik();
			} else if (attribute == Attribute.NAME) {
				return getName();
			} else if (attribute == Attribute.SIC) {
				return getSic();
			} else if (attribute == Attribute.TICKER) {
				return getTicker();
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
			BufferedReader br = new BufferedReader(new FileReader(sourceFilePath));
			String headingLine = br.readLine();
			if (headingLine == null) {
				br.close();
				throw new IllegalArgumentException();
			}
			String[] headings = headingLine.split("\\t");
			int cikCol = -1, nameCol = -1, tickerCol = -1, sicCol = -1;
			for (int i = 0; i < headings.length; i++) {
				if (headings[i].equals("cik"))
					cikCol = i;
				else if (headings[i].equals("name"))
					nameCol = i;
				else if (headings[i].equals("ticker"))
					tickerCol = i;
				else if (headings[i].equals("sic"))
					sicCol = i;
			}
			
			if (cikCol < 0 || nameCol < 0 || tickerCol < 0 || sicCol < 0) {
				br.close();
				throw new IllegalArgumentException();
			}
			
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] lineValues = line.trim().split("\\t");
				if (lineValues.length < 5) {
					br.close();
					throw new IllegalArgumentException();
				}
				
				String id = lineValues[0];
				String cikValue = lineValues[cikCol];
				String nameValue = lineValues[nameCol];
				String tickerValue = lineValues[tickerCol];
				String sicValue = lineValues[sicCol];
				
				this.idsToAttributes.put(id, new Attributes(cikValue, nameValue, tickerValue, sicValue));
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
	
	public String getAttributeById(String id, CorpMetaData.Attribute attribute) {
		CorpMetaData.Attributes attributes = getAttributesById(id);
		if (attributes == null)
			return null;
		else
			return attributes.getAttribute(attribute);
	}
}
