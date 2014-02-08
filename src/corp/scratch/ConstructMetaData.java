package corp.scratch;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * ConstructMetaData contains functions for putting data from various sources
 * into the meta-data format used by the corp.data.CorpMetaData class.  These
 *  will probably be useless in the future (and they contain hard-coded paths), 
 * but I've kept them in the project just in case they can serve as examples or 
 * something in the future.
 * 
 * @author Bill McDowell
 *
 */
public class ConstructMetaData {
	public static void main(String[] args) {
		/*constructFromSourceWithColumnNames("C:/Users/Bill/Documents/projects/NoahsARK/sloan/Data/MetaData/Sources/CompustatMeta.tsv",
							"C:/Users/Bill/Documents/projects/NoahsARK/sloan/Data/MetaData/Corp.metadata",
							new String[] { "cusip", "cik", "conm", "tic", "sic" },
							new String[] { "id", "cik", "name", "ticker", "sic" }
		);*/
		
		constructFromBloombergSource("C:/Users/Bill/Documents/projects/NoahsARK/sloan/Data/MetaData/Sources/Boomberg.tsv",
							"C:/Users/Bill/Documents/projects/NoahsARK/sloan/Data/MetaData/Bloomberg.metadata");
	}
	
	public static void constructFromSourceWithColumnNames(String inputFile, 
										   String outputFile, 
										   String[] inputColumns, 
										   String[] outputColumns) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(inputFile));
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			int[] inputColumnIndices = new int[inputColumns.length];
			
			String columnLine = br.readLine();
			String[] inputSourceColumns = columnLine.split("\\t");
			
			for (int i = 0; i < inputColumns.length; i++) {
				for (int j = 0; j < inputSourceColumns.length; j++) {
					if (inputColumns[i].equals(inputSourceColumns[j])) {
						inputColumnIndices[i] = j;
						break;
					}
				}
				System.out.print(outputColumns[i]);
				bw.write(outputColumns[i]);
				if (i != outputColumns.length - 1) {
					System.out.print("\t");
					bw.write("\t");
				} else {
					System.out.print("\n");
					bw.write("\n");
				}
			}
			
			
			String line = null;
			HashMap<String, String[]> uniqueIdColumnValues = new HashMap<String, String[]>();
			while ((line = br.readLine()) != null) {
				String[] columnValues = line.split("\t");
				if (columnValues[inputColumnIndices[0]].trim().length() == 0)
					continue;
				uniqueIdColumnValues.put(columnValues[inputColumnIndices[0]], columnValues);
			}
			
			for (String[] columnValues : uniqueIdColumnValues.values()) {
				for (int i = 0; i < outputColumns.length; i++) {
					System.out.print(columnValues[inputColumnIndices[i]]);
					bw.write(columnValues[inputColumnIndices[i]]);
					if (i != outputColumns.length - 1) {
						System.out.print("\t");
						bw.write("\t");
					} else {
						System.out.print("\n");
						bw.write("\n");
					}
				}
			}
			
			System.out.println("Output " + uniqueIdColumnValues.size() + " lines of meta data.");
			
			br.close();
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void constructFromBloombergSource(String inputFile, String outputFile) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(inputFile));
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			String[] outputCols = new String[] { "id", "name", "ticker", "country", "type", "industry"};

			String line = null;
			HashMap<String, HashMap<String, List<String>>> uniqueNameColumnValues = new HashMap<String, HashMap<String, List<String>>>();
			while ((line = br.readLine()) != null) { 
				String[] lineParts = line.split("\t");
				String name = null;
				String ticker = null;
				String country = null;
				String type = null;
				String industry = null;
				
				if (lineParts[0].trim().matches(".+\\(.+\\)")) {
					name = lineParts[0].substring(0, lineParts[0].indexOf("(")).trim();
				} else {
					name = lineParts[0].trim();
				}
				
				if (!uniqueNameColumnValues.containsKey(name)) {
					uniqueNameColumnValues.put(name, new HashMap<String, List<String>>());
					for (String colName : outputCols)
						uniqueNameColumnValues.get(name).put(colName, new ArrayList<String>());
				}
				
				ticker = lineParts[1].split(":")[0].trim();
				country = lineParts[2].trim();
				type = lineParts[3].trim();
				industry = lineParts[4].trim();
				
				uniqueNameColumnValues.get(name).get("ticker").add(ticker);
				uniqueNameColumnValues.get(name).get("country").add(country);
				uniqueNameColumnValues.get(name).get("type").add(type);
				uniqueNameColumnValues.get(name).get("industry").add(industry);
			}
			
			for (int i = 0; i < outputCols.length; i++) {
				bw.write(outputCols[i]);
				System.out.print(outputCols[i]);
				if (i == outputCols.length - 1) {
					System.out.print("\n");
					bw.write("\n");
				} else {
					System.out.print("\t");
					bw.write("\t");
				}
			}
			
			int id = 0;
			for (Entry<String, HashMap<String, List<String>>> e : uniqueNameColumnValues.entrySet()) {
				bw.write(id + "\t" + e.getKey() + "\t");
				System.out.print(id + "\t" + e.getKey() + "\t");
				for (int i = 2; i < outputCols.length; i++) {
					List<String> colVals = e.getValue().get(outputCols[i]);
					for (int j = 0; j < colVals.size(); j++) {
						System.out.print(colVals.get(j));
						bw.write(colVals.get(j));
						if (j != colVals.size() - 1) {
							System.out.print(",");
							bw.write(",");
						}
					}
					
					if (i == outputCols.length - 1) {
						System.out.print("\n");
						bw.write("\n");
					} else {
						System.out.print("\t");
						bw.write("\t");
					}
				}
				id++;
			}
			
			br.close();
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
