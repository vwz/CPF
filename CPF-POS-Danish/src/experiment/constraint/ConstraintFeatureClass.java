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
		if (factorName.equalsIgnoreCase("CONJ_notinlast")) {
			f_flag = lc1.predLabel.equalsIgnoreCase("CONJ") & !obs1.isLast; 
			g_flag = lc1.predLabel.equalsIgnoreCase("CONJ");
		} 
	}

	public void GetEdgeFeatureSignal(String factorName, LineClass2 lc1, LineClass2 lc2, ObsClass obs1, ObsClass obs2, dataStructure ds, List<ObsClass> obsList) {
		
		if (factorName.equalsIgnoreCase("NUM_NUM_close")) {
			boolean isclose = getslot(Math.abs(lc2.position - lc1.position), ds.data.size()) <= 3;
			boolean isnum = lc1.isnumber & lc2.isnumber;
			f_flag = lc1.predLabel.equalsIgnoreCase("NUM") & lc2.predLabel.equalsIgnoreCase("NUM") & isnum & isclose;
			g_flag = lc1.predLabel.equalsIgnoreCase("NUM") & lc2.predLabel.equalsIgnoreCase("NUM"); 
		}
		
		if (factorName.equalsIgnoreCase("ADJ_NUM_faraway")) {
			g_flag = false;
			f_flag = false;
			boolean isfaraway = Math.abs(lc2.position - lc1.position) >= 3;
			if (lc1.predLabel.equalsIgnoreCase("ADJ") & lc2.predLabel.equalsIgnoreCase("NUM") & lc1.precededbyPron) { 
				g_flag = true;
				f_flag = isfaraway & lc1.isADJ & lc2.isnumber;
			} else if (lc2.predLabel.equalsIgnoreCase("ADJ") & lc1.predLabel.equalsIgnoreCase("NUM") & lc2.precededbyPron) { 
				g_flag = true;
				f_flag = isfaraway & lc2.isADJ & lc1.isnumber;
			}
		}
	}
	
	private int getslot(int i, int n) {
		return (int) Math.floor(((double) i / (double) n) / (1.0 / 5));
	}

	public void Clear() {
		f_flag = false;
		g_flag = false;
	}
}
