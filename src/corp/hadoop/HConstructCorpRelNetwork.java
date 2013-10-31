package corp.hadoop;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;


import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import ark.util.StringUtil;

public class HConstructCorpRelNetwork {
	
	public static class HConstructCorpRelNetworkMapper extends Mapper<Object, Text, Text, Text> {
		private Text relationId = new Text();
		private Text relationObj = new Text();
		private StringUtil.StringTransform corpKeyFn = null; // FIXME
		
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

			String line = value.toString();
			
			try {
				JSONObject relationObj = JSONObject.fromObject(value.toString());
				if (relationObj != null) {
					JSONArray mentions = relationObj.getJSONArray("mentions");
					if (mentions.size() == 0)
						return;
					String mention = mentions.getJSONObject(0).getString("text");
					String author = relationObj.getString("author");
				//	String relationId = this.corpKeyFn.transform(author) + "." + this.corpKeyFn.transform(mention);
					
					this.relationId.set(relationId);
					this.relationObj.set(value);
					context.write(this.relationId, this.relationObj);
				}
			} catch (Exception e1) {
				
			}
		}
	}

	public static class BBNDetectUsersReducer extends Reducer<LongWritable, Text, LongWritable, Text> {
		private Text fullText = new Text();
		
		public void reduce(LongWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			for (Text text : values) {
				fullText.append(text.getBytes(), 0, text.getLength());
				fullText.append("\t".getBytes(), 0, 1);
			}
			
			context.write(key, this.fullText);

		}
	}

	public static void main(String[] args) throws Exception {
		/*Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		@SuppressWarnings("deprecation")
		Job job = new Job(conf, "HBBNDetectUsers");
		job.setJarByClass(HBBNDetectUsers.class);
		job.setMapperClass(BBNDetectUsersMapper.class);
		job.setCombinerClass(BBNDetectUsersReducer.class);
		job.setReducerClass(BBNDetectUsersReducer.class);
		job.setOutputKeyClass(LongWritable.class);
		job.setOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);*/
	}
}

