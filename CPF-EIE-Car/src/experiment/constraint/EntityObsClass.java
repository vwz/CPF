package experiment.constraint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import experiment.graph.EntityDataClass;
import experiment.graph.GetDataDomain;
import experiment.graph.LineClass2;
import experiment.graph.dataStructure;
import experiment.specs.EntitySpecClass;
import experiment.specs.SpecClass;
import experiment.specs.SpecNounClass;

public class EntityObsClass {
	public Map<String, List<ObsClass>> EntityObsList;
	
	public EntityObsClass() {
		this.EntityObsList = new HashMap<String, List<ObsClass>>();
	}
	
	public void Process(EntityDataClass edc, GetDataDomain gdd, EntitySpecClass esc, SpecNounClass snc) {
		double nrel  = 0;
		double ntotal = 0;
		Map<String, Double> count = new HashMap<String, Double>();
		
		for (Entry<String, dataStructure> e : edc.entityData.entrySet()) {
			String entity = e.getKey();
			String name = gdd.nameDic.get(entity);
			SpecClass sc = esc.MapEntitySpecs.get(entity);
			dataStructure ds = e.getValue();
			
			List<ObsClass> obsList = new ArrayList<ObsClass>();
			
			for (int i = 0; i < ds.data.size(); i++) {
				LineClass2 lcc = ds.data.get(i);			
				ObsClass obs = new ObsClass();
				
				// get observations that do not require context
				obs.CollectObs(lcc, name, sc, snc);
				
				// get observations that requires context
				if (!obs.SurroundIndicatorForReliability && i > 0) {
					int j = i - 1;
					LineClass2 lcc2 = ds.data.get(j);
					if (lcc2.pageID.equals(lcc.pageID) && lcc2.lineID - lcc.lineID == j - i) {
						ObsClass obs2 = obsList.get(j);
						if (obs2.SurroundIndicatorForReliability)
							obs.SurroundIndicatorForReliability = true;
					}
				}
				
				obsList.add(obs);
			}
		
			EntityObsList.put(entity,  obsList);
		}
	}
	
	private Map<String, Double> sortByComparator(Map<String, Double> unsortMap) {
		 
		// Convert Map to List
		List<Map.Entry<String, Double>> list = 
			new LinkedList<Map.Entry<String, Double>>(unsortMap.entrySet());
 
		// Sort list with comparator, to compare the Map values
		Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
			public int compare(Map.Entry<String, Double> o1,
                                           Map.Entry<String, Double> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});
 
		// Convert sorted map back to a Map
		Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
		for (Iterator<Map.Entry<String, Double>> it = list.iterator(); it.hasNext();) {
			Map.Entry<String, Double> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}
}