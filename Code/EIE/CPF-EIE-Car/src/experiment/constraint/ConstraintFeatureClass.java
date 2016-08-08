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
		if (factorName.equalsIgnoreCase("Price_HasNumberOrKeywords")) {
			f_flag = lc1.predLabel.equalsIgnoreCase("Price") & (obs1.NumberSet.size() > 0 | obs1.HasWordForPrice);
			g_flag = lc1.predLabel.equalsIgnoreCase("Price");
		}
	}
	
	public void GetEdgeFeatureSignal(String factorName, LineClass2 lc1, LineClass2 lc2, ObsClass obs1, ObsClass obs2, dataStructure ds, List<ObsClass> obsList) {
		if (factorName.equalsIgnoreCase("PricePrice_SimilarValue")) {
			g_flag = lc1.predLabel.equalsIgnoreCase("Price") & lc2.predLabel.equalsIgnoreCase("Price");
			f_flag = lc1.predLabel.equalsIgnoreCase("Price") & lc2.predLabel.equalsIgnoreCase("Price") & ObsClass.HasSimilarValue(obs1, obs2);
		} else if (factorName.equalsIgnoreCase("Verdict_MoreSpec_DriveRide")) {
			g_flag = false;
			f_flag = false;
			
			if (lc1.predLabel.equalsIgnoreCase("Verdict") & lc2.predLabel.equalsIgnoreCase("DriveRide") & obs2.IsDriveRide) {
				g_flag = true;
				f_flag = (obs1.SpecMapCount > obs2.SpecMapCount);
			} else if (lc2.predLabel.equalsIgnoreCase("Verdict") & lc1.predLabel.equalsIgnoreCase("DriveRide") & obs1.IsDriveRide) {
				g_flag = true;
				f_flag = (obs2.SpecMapCount > obs1.SpecMapCount);
			}
		}
	}
	
	public void Clear() {
		f_flag = false;
		g_flag = false;
	}
}
