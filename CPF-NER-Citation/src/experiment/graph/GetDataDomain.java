package experiment.graph;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import experiment.io.IOClass;

public class GetDataDomain {
	public Map<String, String> labelDic;
	public Map<String, String> wordDic;
	public Map<String, String> nameDic;
	public Map<String, String> invertedLabelDic;
	public int nFeatures;
	public double nLabeled;
	public double nUnlabeled;
	
	public GetDataDomain() {
		wordDic = new HashMap<String, String>();
		
		labelDic = new HashMap<String, String>();
		labelDic.put("author", "0"); labelDic.put("title", "1"); labelDic.put("journal", "2"); labelDic.put("volume", "3"); labelDic.put("date", "4"); 
		labelDic.put("tech", "5"); labelDic.put("institution", "6"); labelDic.put("booktitle", "7"); labelDic.put("pages", "8"); labelDic.put("publisher", "9"); 
		labelDic.put("location", "10"); labelDic.put("editor", "11"); labelDic.put("note", "12");
		
		invertedLabelDic = new HashMap<String, String>();
		for (Entry<String, String> e : labelDic.entrySet()) {
			invertedLabelDic.put(e.getValue(), e.getKey());
		}
		
		nameDic = new HashMap<String, String>();
		
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
