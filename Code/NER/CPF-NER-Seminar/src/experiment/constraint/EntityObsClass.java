package experiment.constraint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import experiment.graph.EntityDataClass;
import experiment.graph.GetDataDomain;
import experiment.graph.LineClass2;
import experiment.graph.dataStructure;

public class EntityObsClass {
	public Map<String, List<ObsClass>> EntityObsList;

	public EntityObsClass() {
		this.EntityObsList = new HashMap<String, List<ObsClass>>();
	}

	public void Process(EntityDataClass edc, GetDataDomain gdd) {
		for (Entry<String, dataStructure> e : edc.entityData.entrySet()) {
			String entity = e.getKey();
			dataStructure ds = e.getValue();

			List<ObsClass> obsList = new ArrayList<ObsClass>();

			for (int i = 0; i < ds.data.size(); i++) {
				LineClass2 lcc = ds.data.get(i); 
				ObsClass obs = new ObsClass();

				// get observations that do not require context
				LineClass2 prev1_lcc = null;
				if (i > 1)
					prev1_lcc = ds.data.get(i - 1);
				LineClass2 prev2_lcc = null;
				if (i > 2)
					prev2_lcc = ds.data.get(i - 2);
				
				obs.CollectObs(lcc, prev1_lcc, prev2_lcc);

				obsList.add(obs);
			}

			EntityObsList.put(entity,  obsList);
		}
	}
}