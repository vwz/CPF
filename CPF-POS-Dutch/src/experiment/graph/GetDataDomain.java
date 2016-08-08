package experiment.graph;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import experiment.io.IOClass;

public class GetDataDomain {
	public Map<String, String> labelDic;
	public Map<String, String> wordDic;
	public Map<String, String> invertedLabelDic;
	public int nFeatures;
	public double nLabeled;
	public double nUnlabeled;
	
	public GetDataDomain() {
		wordDic = new HashMap<String, String>();
		
		labelDic = new HashMap<String, String>();
		labelDic.put("VERB", "0"); labelDic.put("NOUN", "1"); labelDic.put("PRON", "2"); labelDic.put("ADJ", "3"); labelDic.put("ADV", "4"); 
		labelDic.put("ADP", "5"); labelDic.put("CONJ", "6"); labelDic.put("DET", "7"); labelDic.put("NUM", "8"); labelDic.put("PRT", "9"); 
		labelDic.put("X", "10"); labelDic.put(".", "11");
		
		invertedLabelDic = new HashMap<String, String>();
		for (Entry<String, String> e : labelDic.entrySet()) {
			invertedLabelDic.put(e.getValue(), e.getKey());
		}
		
		nFeatures = 0;
		nLabeled = 0;
		nUnlabeled = 0;
	}
	
	// Once the word dictionary is saved into some file, then directly load from the file
	public void LoadWordDicFromFile(String fn) {
		try {
			wordDic = IOClass.LoadIDMap(fn);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
