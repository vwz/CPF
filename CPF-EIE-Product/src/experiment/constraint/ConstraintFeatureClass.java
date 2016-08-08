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
		if (factorName.equalsIgnoreCase("HeadSize_HasSize")) {
			f_flag = lc1.predLabel.equalsIgnoreCase("HeadSize") & obs1.HasWordSize;
			g_flag = lc1.predLabel.equalsIgnoreCase("HeadSize");
		}
	}
	
	public void GetEdgeFeatureSignal(String factorName, LineClass2 lc1, LineClass2 lc2, ObsClass obs1, ObsClass obs2, dataStructure ds, List<ObsClass> obsList) {
		if (factorName.equalsIgnoreCase("StrungWeight_StrungWeight_SameValue")) {
			f_flag = lc1.predLabel.equalsIgnoreCase("StrungWeight") & lc2.predLabel.equalsIgnoreCase("StrungWeight") & ObsClass.HasSameNumber(lc1, lc2);
			g_flag = lc1.predLabel.equalsIgnoreCase("StrungWeight") & lc2.predLabel.equalsIgnoreCase("StrungWeight");
		}
		if (factorName.equalsIgnoreCase("StrungWeight_Larger_UnstrungWeight")) {
			g_flag = false;
			f_flag = false;
			
			if (obs1.HasWordWeight && obs2.HasWordWeight) {
				if (lc1.predLabel.equalsIgnoreCase("StrungWeight") & lc2.predLabel.equalsIgnoreCase("UnstrungWeight")) {
					g_flag = true;
					f_flag = ObsClass.HasLargerValue(lc1, lc2);
				} else if (lc2.predLabel.equalsIgnoreCase("StrungWeight") & lc1.predLabel.equalsIgnoreCase("UnstrungWeight")) {
					g_flag = true;
					f_flag = ObsClass.HasLargerValue(lc2, lc1);
				}
			}
		}
	}
	
	public void Clear() {
		f_flag = false;
		g_flag = false;
	}
}
