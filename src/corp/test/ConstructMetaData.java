package corp.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;

public class ConstructMetaData {
	public static void main(String[] args) {
		constructFromSource("C:/Users/Bill/Documents/projects/NoahsARK/sloan/Data/MetaData/Sources/CompustatMeta.tsv",
							"C:/Users/Bill/Documents/projects/NoahsARK/sloan/Data/MetaData/Corp.metadata",
							new String[] { "cusip", "cik", "conm", "tic", "sic" },
							new String[] { "id", "cik", "name", "ticker", "sic" }
		);
	}
	
	public static void constructFromSource(String inputFile, 
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
}
