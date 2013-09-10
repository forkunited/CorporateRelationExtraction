package corp.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

public class CounterTable{
	public HashMap<String, Integer> counts;
	
	public CounterTable(){
		this.counts= new HashMap<String,Integer>();
	}
	
	public void incrementCount(String w){
		if(this.counts.containsKey(w)){
			this.counts.put(w, this.counts.get(w) + 1);
		}else{
			this.counts.put(w, 1);
		}
	}
	
	public void removeCountsLessThan(int minCount) {
		List<String> valuesToRemove = new ArrayList<String>();
		for (Entry<String, Integer> entry : this.counts.entrySet()) {
			if (entry.getValue() < minCount)
				valuesToRemove.add(entry.getKey());
		}
		
		for (String valueToRemove : valuesToRemove)
			this.counts.remove(valueToRemove);
	}
	
	public HashMap<String, Integer> buildIndex() {
		HashMap<String, Integer> index = new HashMap<String, Integer>();
		int i = 0;
		
		for (Entry<String, Integer> entry : this.counts.entrySet()) {
			index.put(entry.getKey(), i);
			i++;
		}
		
		return index;
	}
	
	public TreeMap<Integer, String> getSortedCounts() {
		TreeMap<Integer, String> sortedCounts = new TreeMap<Integer, String>();
		
		for (Entry<String, Integer> entry : this.counts.entrySet()) {
			sortedCounts.put(entry.getValue(), entry.getKey());
		}
		
		return sortedCounts;
	}
	
	public int getSize() {
		return this.counts.size();
	}
}