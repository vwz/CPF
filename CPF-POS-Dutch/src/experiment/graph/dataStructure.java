package experiment.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class dataStructure {
	public List<LineClass2> data;

	public dataStructure() {
		data = new ArrayList<LineClass2>();
	}

	public void loadData(List<String> segmentList, Map<String, String> wordDic) {
		for (int i = 0; i < segmentList.size(); i++) {
			String line = segmentList.get(i);
			
			LineClass2 lc = new LineClass2();
			lc.ParseLine(line, i);
			if (!lc.success) continue;

			if (i == 0) {
				lc.hasPrev = false;
				lc.Prev = -1;
			} else {
				lc.hasPrev = true;
				lc.Prev = data.size() - 1;
			} 

			lc.SetWordID(wordDic);

			data.add(lc);
		}

		if (data.size() > 0) {
			for (int i = 0; i < data.size() - 1; i++) {
				LineClass2 lc1 = data.get(i);
				LineClass2 lc2 = data.get(i + 1);
				
				lc1.hasNext = true;
				lc1.Next = i + 1;
				data.set(i, lc1);
			}
			LineClass2 lc = data.get(data.size() - 1);
			lc.hasNext = false;
			lc.Next = -1;
			data.set(data.size() - 1, lc);
		}
	}

	public void append(dataStructure ds2) {
		for (int i = 0; i < ds2.data.size(); i++) {
			LineClass2 lc = ds2.data.get(i);
			data.add(lc);
		}
	}

	public void setPredLabel() {
		for (int i = 0; i < data.size(); i++) {
			LineClass2 lc = data.get(i);
			lc.predLabel = lc.label;
			data.set(i, lc);
		}
	}
}
