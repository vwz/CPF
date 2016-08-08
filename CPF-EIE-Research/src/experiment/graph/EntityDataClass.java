package experiment.graph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import experiment.constraint.EntityObsClass;
import experiment.constraint.ObsClass;
import experiment.io.IOClass;

public class EntityDataClass {
	public Map<String, dataStructure> entityData; // entityID - his/her data
	public Map<String, String> entityIndicator; // entityID - labeled/unlabeled

	public EntityDataClass() {
		this.entityData = new HashMap<String, dataStructure>();
		this.entityIndicator = new HashMap<String, String>(); 
	}
	
	public void LoadData(String test, Map<String, String> wordDic) {
		try {
			LoadOneFile(test, wordDic);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void LoadData(String trainLabeled, String trainUnlabeled, Map<String, String> wordDic) {
		try {
			LoadOneFile(trainLabeled, wordDic);
			LoadOneFile(trainUnlabeled, wordDic);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setPredLabel() {
		for (Entry<String, dataStructure> e : entityData.entrySet()) {
			dataStructure ds = e.getValue();
			ds.setPredLabel();
			entityData.put(e.getKey(), ds);
		}
	}
	
	public int getFeatures(EntityObsClass eoc) {
		int nFeatures = 0;
		for (Entry<String, dataStructure> e : entityData.entrySet()) {
			dataStructure ds = e.getValue();
			List<ObsClass> obsList = eoc.EntityObsList.get(e.getKey());
			int n = obsList.size();
			for (int i = 0; i < n; i++) {
				double[] features = obsList.get(i).ReturnFeatures();
				ds.data.get(i).features = features;
				nFeatures = features.length;
			}
		}
		
		return nFeatures;
	}

	private void LoadOneFile(String fn, Map<String, String> wordDic) throws IOException {
		// load labeled data
		BufferedReader br = new BufferedReader(new FileReader(new File(fn)));
		String line = "";
		String prevEntityID = "";
		List<String> segmentList = new ArrayList<String>();
		while ((line=br.readLine()) != null) {
			String[] segs = line.split(" ");
			String curEntityID = segs[0];
			if (!curEntityID.equalsIgnoreCase(prevEntityID) && prevEntityID.length() > 0) {
				dataStructure ds = new dataStructure();
				ds.loadData(segmentList, wordDic);
				entityData.put(prevEntityID, ds);
				segmentList.clear();
			}
			segmentList.add(line);
			prevEntityID = curEntityID;
		}
		if (segmentList.size() > 0) {
			dataStructure ds = new dataStructure();
			ds.loadData(segmentList, wordDic);
			entityData.put(prevEntityID, ds);
			segmentList.clear();
		}
		br.close();
	}
	
	public void LoadEntity(String trainLabeled, String trainUnlabeled) throws IOException {
		Map<String, String> id = IOClass.LoadIDMap(trainLabeled);
		for (Entry<String, String> e : id.entrySet()) {
			id.put(e.getKey(), "labeled");
		}
		entityIndicator.putAll(id);
		
		id.clear();
		id = IOClass.LoadIDMap(trainUnlabeled);
		for (Entry<String, String> e : id.entrySet()) {
			id.put(e.getKey(), "unlabeled");
		}
		entityIndicator.putAll(id);
	}
}
