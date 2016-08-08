package experiment.constraint;

import java.util.List;

import experiment.graph.LineClass2;
import experiment.graph.dataStructure;

public class ConstraintFeatureClass {
	public boolean f_flag;
	public boolean g_flag;

	public ConstraintFeatureClass() {
		f_flag = false;
		g_flag = false;
	}

	public void GetNodeFeatureSignal(String factorName, LineClass2 lc1, ObsClass obs1, List<LineClass2> variables, List<ObsClass> obsList, int i) {
		if (factorName.equalsIgnoreCase("author_inonethird")) {
			f_flag = lc1.predLabel.equalsIgnoreCase("author") & obs1.inonethird;
			g_flag = lc1.predLabel.equalsIgnoreCase("author");
		}
	}

	public void GetEdgeFeatureSignal(String factorName, LineClass2 lc1, LineClass2 lc2, ObsClass obs1, ObsClass obs2, dataStructure ds, List<ObsClass> obsList) {
		if (factorName.equalsIgnoreCase("title_title_Continuous")) {
			f_flag = lc1.predLabel.equalsIgnoreCase("title") & lc2.predLabel.equalsIgnoreCase("title") & isContinuous(obs1.index, obs2.index, obsList);
			g_flag = lc1.predLabel.equalsIgnoreCase("title") & lc2.predLabel.equalsIgnoreCase("title");
		} 
		else if (factorName.equalsIgnoreCase("booktitle_title_CloseAfter")) {
			f_flag = false;
			g_flag = false;
			if (lc1.predLabel.equalsIgnoreCase("booktitle") & lc2.predLabel.equalsIgnoreCase("title")) {
				g_flag = true;
				f_flag = (obs1.index > obs2.index) & !isContinuous(obs1.index, obs2.index, obsList) & obs1.intwothird;
			} else if (lc2.predLabel.equalsIgnoreCase("booktitle") & lc1.predLabel.equalsIgnoreCase("title")) {
				g_flag = true;
				f_flag = (obs2.index > obs1.index) & !isContinuous(obs1.index, obs2.index, obsList) & obs2.intwothird;
			}
		}
	}

	public void Clear() {
		f_flag = false;
		g_flag = false;
	}
	
	private Boolean isContinuous(int index1, int index2, List<ObsClass> obsList) {
		// ensure index1 < index2
		if (index1 > index2) {
			int tmp = index1;
			index1 = index2;
			index2 = tmp;
		}
		for (int i = index1; i < index2; i++) {
			ObsClass obs = obsList.get(i);
			if (obs.isPunctuation)
				return false;
		}
		return true;
	}
}
