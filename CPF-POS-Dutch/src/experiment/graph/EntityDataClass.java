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
	public int nLabeled;
	public int nUnlabeled;

	public EntityDataClass() {
		this.entityData = new HashMap<String, dataStructure>();
		this.nLabeled = 0;
		this.nUnlabeled = 0;
	}
	
	public void LoadData(String test, Map<String, String> wordDic) {
		try {
			LoadOneFile(test, wordDic, "test");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void LoadData(String trainLabeled, String trainUnlabeled, Map<String, String> wordDic) {
		try {
			LoadOneFile(trainLabeled, wordDic, "labeled");
			this.nLabeled = entityData.size();
			LoadOneFile(trainUnlabeled, wordDic, "unlabeled");
			this.nUnlabeled = entityData.size() - this.nLabeled;
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
				
				// also include next_features
				if (i < n - 1)
					ds.data.get(i).next_features = obsList.get(i+1).ReturnFeatures();
				else {
					double[] next_features = new double[features.length];
					for (int j = 0; j < features.length; j++)
						next_features[j] = 0;
					ds.data.get(i).next_features = next_features;
				}
			}
		}
		nFeatures *= 2; // so we double the number of features by including next_features
		
		return nFeatures;
	}

	private void LoadOneFile(String fn, Map<String, String> wordDic, String id) throws IOException {
		// load data
		BufferedReader br = new BufferedReader(new FileReader(new File(fn)));
		String line = "";
		List<String> segmentList = new ArrayList<String>(); // keep all sentences
		int index = 0;
		while ((line=br.readLine()) != null) {
			if (line.length() == 0) {
				// save into entityData
				dataStructure ds = new dataStructure();
				ds.loadData(segmentList, wordDic);
				entityData.put(id + "_" + index, ds);
				
				index++;
				segmentList.clear();
			} else {
				segmentList.add(line);
			}
		}

		br.close();
	}
}
