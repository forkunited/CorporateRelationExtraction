package corp.scratch;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Map;
import java.util.Map.Entry;

import corp.data.CorpMetaData;

/**
 * Stubs for constructing clean Gazetteers from old messy Gazetteer files
 * 
 * @author Bill
 *
 */
public class ConstructGazetteer {
	public static void main(String[] args) {
		constructFromOldGazetteer("C:/Users/Bill/Documents/projects/NoahsARK/sloan/Data/Gazetteer/Legacy/ncorplist",
								 "C:/Users/Bill/Documents/projects/NoahsARK/sloan/Data/Gazetteer/NonCorpScraped.gazetteer");
		constructFromOldGazetteer("C:/Users/Bill/Documents/projects/NoahsARK/sloan/Data/Gazetteer/Legacy/corp",
				 				  "C:/Users/Bill/Documents/projects/NoahsARK/sloan/Data/Gazetteer/CorpScraped.gazetteer");
		constructFromOldGazetteer("C:/Users/Bill/Documents/projects/NoahsARK/sloan/Data/Gazetteer/Legacy/stopwords",
				  				  "C:/Users/Bill/Documents/projects/NoahsARK/sloan/Data/Gazetteer/StopWord.gazetteer");
		constructFromMetaData("C:/Users/Bill/Documents/projects/NoahsARK/sloan/Data/MetaData/Corp.metadata",
				  			  "C:/Users/Bill/Documents/projects/NoahsARK/sloan/Data/Gazetteer/CorpMetaData.gazetteer");
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
}
