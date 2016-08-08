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
		if (factorName.equalsIgnoreCase("speaker_namebutnothost")) {
			f_flag = lc1.predLabel.equalsIgnoreCase("speaker") & obs1.fea_name_honorifics & !obs1.precededbyhost;
			g_flag = lc1.predLabel.equalsIgnoreCase("speaker");
		} 
	}

	public void GetEdgeFeatureSignal(String factorName, LineClass2 lc1, LineClass2 lc2, ObsClass obs1, ObsClass obs2, dataStructure ds, List<ObsClass> obsList) {
		if (factorName.equalsIgnoreCase("stime_stime_sametime")) {
			f_flag = lc1.predLabel.equalsIgnoreCase("stime") & lc2.predLabel.equalsIgnoreCase("stime") & obs1.nearNumber & obs2.nearNumber & obs1.istime & obs2.istime & obs1.time_value.equals(obs2.time_value);
			g_flag = lc1.predLabel.equalsIgnoreCase("stime") & lc2.predLabel.equalsIgnoreCase("stime") & obs1.nearNumber & obs2.nearNumber;
		}
		if (factorName.equalsIgnoreCase("stime_etime_earlier")) {
			f_flag = false;
			g_flag = false;
			if (lc1.predLabel.equalsIgnoreCase("stime") & lc2.predLabel.equalsIgnoreCase("etime") & obs1.nearNumber & obs2.nearNumber) {
				g_flag = true;
				if (obs1.istime & obs2.istime)
					f_flag = Integer.valueOf(obs1.time_value) < Integer.valueOf(obs2.time_value);
			} else if (lc2.predLabel.equalsIgnoreCase("stime") & lc1.predLabel.equalsIgnoreCase("etime") & obs1.nearNumber & obs2.nearNumber) {
				g_flag = true;
				if (obs1.istime & obs2.istime)
					f_flag = Integer.valueOf(obs2.time_value) < Integer.valueOf(obs1.time_value);
			}
		}
	}

	public void Clear() {
		f_flag = false;
		g_flag = false;
	}
}
