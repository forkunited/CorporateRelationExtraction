package corp.hadoop;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import corp.util.CorpKeyFn;
import corp.util.CorpProperties;

import ark.data.Gazetteer;
import ark.util.StringUtil;

public class HConstructCorpRelNetwork {
	
	public static class HConstructCorpRelNetworkMapper extends Mapper<Object, Text, Text, Text> {
		private Text relationId = new Text();
		private Text relationObj = new Text();
		
		private CorpProperties properties = new CorpProperties();
		private Gazetteer stopWordGazetteer = new Gazetteer("StopWord", this.properties.getStopWordGazetteerPath());
		private Gazetteer bloombergCorpTickerGazetteer = new Gazetteer("BloombergCorpTickerGazetteer", this.properties.getBloombergCorpTickerGazetteerPath());
		private StringUtil.StringTransform stopWordCleanFn = StringUtil.getStopWordsCleanFn(this.stopWordGazetteer);
		private CorpKeyFn corpKeyFn = new CorpKeyFn(this.bloombergCorpTickerGazetteer, this.stopWordCleanFn);
		
		/*
		 * Skip badly gzip'd files
		 */
		public void run(Context context) throws InterruptedException {
			try {
				setup(context);
				while (context.nextKeyValue()) {
					map(context.getCurrentKey(), context.getCurrentValue(),
							context);
				}
				cleanup(context);
			} catch (Exception e) {

			}
		}

		public void map(Object key, Text value, Context context) {
			try {
				JSONObject relationObj = JSONObject.fromObject(value.toString());
				if (relationObj != null) {
					JSONArray mentions = relationObj.getJSONArray("mentions");
					if (mentions.size() == 0) {
						throw new IllegalArgumentException("Relation object missing mentions...");
					}
					String mention = mentions.getJSONObject(0).getString("text");
					String author = relationObj.getString("author");
					String annotationFile = relationObj.getString("annotationFile");
					int dateStartIndex = annotationFile.indexOf("-8-K-") + 5;
					String year = annotationFile.substring(dateStartIndex, dateStartIndex+4);
					String relationId = this.corpKeyFn.transform(author) + "." + this.corpKeyFn.transform(mention);
					String relationIdYear = year + "." + relationId;
					String relationIdFull = "FULL." + relationId;
					this.relationId.set(relationIdFull);
					this.relationObj.set(value);
					context.write(this.relationId, this.relationObj);
					this.relationId.set(relationIdYear);
					context.write(this.relationId, this.relationObj);
				} else {
					throw new IllegalArgumentException("Invalid relation object.");
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public static class HConstructCorpRelNetworkReducer extends Reducer<Text, Text, Text, Text> {
		private Text fullText = new Text();
		
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			this.fullText.clear();
			
			Map<String, Double> posteriorSum = new HashMap<String, Double>();
			JSONArray sources = new JSONArray();
			int count = 0;
			for (Text text : values) {
				String textStr = text.toString();
				JSONObject textJSON = JSONObject.fromObject(textStr);
				JSONObject p = textJSON.getJSONObject("p");
				
				Set pEntries = p.entrySet();
				for (Object o : pEntries) {
					Entry e = (Entry)o;
					String label = e.getKey().toString();
					double pValue = Double.parseDouble(e.getValue().toString());
					if (!posteriorSum.containsKey(label))
						posteriorSum.put(label, 0.0);
					posteriorSum.put(label, posteriorSum.get(label) + pValue);
				}
				
				sources.add(JSONObject.fromObject(textStr));
				count++;
			}
			
			JSONObject posteriorObj = JSONObject.fromObject(posteriorSum);
			JSONObject aggregateObj = new JSONObject();
			aggregateObj.put("p", posteriorObj);
			aggregateObj.put("count", count);
			
			byte[] aggregateStr = aggregateObj.toString().getBytes();
			byte[] sourcesStr = sources.toString().getBytes();
			
			this.fullText.append(aggregateStr, 0, aggregateStr.length);
			this.fullText.append("\t".getBytes(), 0, 1);
			this.fullText.append(sourcesStr, 0, sourcesStr.length);
			
			context.write(key, this.fullText);
		}
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		@SuppressWarnings("deprecation")
		Job job = new Job(conf, "HConstructCorpRelNetwork");
		job.setJarByClass(HConstructCorpRelNetwork.class);
		job.setMapperClass(HConstructCorpRelNetworkMapper.class);
		job.setReducerClass(HConstructCorpRelNetworkReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}

