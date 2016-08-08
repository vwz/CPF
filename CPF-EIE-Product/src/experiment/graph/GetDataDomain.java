package experiment.graph;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
		labelDic.put("HeadSize", "0"); labelDic.put("UnstrungWeight", "1"); labelDic.put("StrungWeight", "2"); 
		labelDic.put("BeamWidth", "3"); labelDic.put("Length", "4"); labelDic.put("Others", "5");
		
		invertedLabelDic = new HashMap<String, String>();
		for (Entry<String, String> e : labelDic.entrySet()) {
			invertedLabelDic.put(e.getValue(), e.getKey());
		}
		
		nameDic = new HashMap<String, String>();
		
		nFeatures = 0;
		nLabeled = 0;
		nUnlabeled = 0;
	}
	
	// Load entity name dictionary
	public void LoadEntityDicFromFile(String fnLabeledTrain, String fnUnlabeledTrain) {
		try {
			nameDic = IOClass.LoadWordDic(fnLabeledTrain);
			nLabeled = (double) nameDic.size();
			nameDic.putAll(IOClass.LoadIDMap(fnUnlabeledTrain));
			nUnlabeled = (double) nameDic.size() - nLabeled;
			System.out.println("#entity: " + nLabeled + "/" + nUnlabeled);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// Load entity name dictionary
	public void LoadEntityDicFromFile(String fnTest) {
		try {
			nameDic = IOClass.LoadWordDic(fnTest);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// Once the word dictionary is saved into some file, then directly load from the file
	public void LoadWordDicFromFile(String fn) {
		try {
			wordDic = IOClass.LoadIDMap(fn);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void ProcessToGetEntityDic(String fin, String fout) {
		try {
			GetEntityDicFromData(fin, fout);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void GetEntityDicFromData(String fin, String fout) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(fout)));
		BufferedReader br = new BufferedReader(new FileReader(new File(fin)));
		Set<String> entitySet = new HashSet<String>();
		String line = "";
		while ((line = br.readLine()) != null) {
			String[] segs = line.split("\t\\|\\|\t", -1);
			
			// segs[0] is: "entityID pageID lineIndex"
			String[] tmp = segs[0].split(" ");
			if (tmp.length != 3)
				return;
			String entityID = tmp[0];
			if (!entitySet.contains(entityID)) {
				bw.write(entityID + "\t");
				bw.newLine();
				entitySet.add(entityID);
			}
		}
		br.close();
		bw.close();
	}
	
	
	// Save the word dictionary (extracted from both training and test data) into a file
	public void ProcessToGetWordDic(String TrainLabeled, String TrainUnlabeled, String WordDicFn) {
		try {
			LoadWordDicFromData(TrainLabeled);
			LoadWordDicFromData(TrainUnlabeled);
			SaveWordDicToFile(WordDicFn);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// sub-function-1 for ProcessToGetWordDic
	private void LoadWordDicFromData(String fn) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(fn)));
		String line = "";
		while ((line=br.readLine()) != null) {
			LineClass2 lc = new LineClass2();
			lc.ParseLine(line);
			if (!lc.success) continue;

			for (int i = 0; i < lc.wordList.size(); i++) {
				String word = lc.wordList.get(i);
				if (!wordDic.containsKey(word))
					wordDic.put(word, String.valueOf(wordDic.size()));
			}
		}
		br.close();
	}
	
	// sub-function-2 for ProcessToGetWordDic
	private void SaveWordDicToFile(String fn) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(fn)));
		for (Entry<String, String> e : wordDic.entrySet())
			bw.write(e.getKey() + "\t" + e.getValue() + "\n");
		bw.close();
	}
}
