package experiment.decoder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import experiment.graph.LineClass2;
import experiment.graph.dataStructure;

public class InvertedIndexClass {
	private Map<String, String> labelDic;
	public Map<String, Set<Integer>> InvertedIndex; // <label, set of line ID's>
	
	public InvertedIndexClass(Map<String, String> labelDic) {
		this.labelDic = labelDic;
		this.InvertedIndex = new HashMap<String, Set<Integer>>();
		for (String label : labelDic.keySet()) {
			Set<Integer> value = new HashSet<Integer>();
			this.InvertedIndex.put(label, value);
		}
	}
	
	/**
	 * This is to build the inverted index map from the data
	 * @param ds
	 */
	public void Initialize(dataStructure ds) {
		for (int i = 0; i < ds.data.size(); i++) {
			LineClass2 lc = ds.data.get(i);
			if (!labelDic.containsKey(lc.predLabel))
				System.err.println("label unknown");
			Set<Integer> value = InvertedIndex.get(lc.predLabel);
			value.add(i);
			InvertedIndex.put(lc.predLabel, value);
		}
	}
	
	/**
	 * This is to update the instance i, from y_before to y_after
	 * @param i
	 * @param ybefore
	 * @param yafter
	 */
	public void Update(int i, String ybefore, String yafter) {
		Set<Integer> value = InvertedIndex.get(ybefore);
		value.remove(i);
		InvertedIndex.put(ybefore, value);
		value = InvertedIndex.get(yafter);
		value.add(i);
		InvertedIndex.put(yafter, value);
	}
}
