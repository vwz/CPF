package experiment.inference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import experiment.constraint.ConstraintClass;
import experiment.constraint.ObsClass;
import experiment.graph.LineClass2;
import experiment.graph.dataStructure;
import experiment.parameter.metaParaClass;

public class ConstraintGraphClass {
	public ConstraintGraphClass() {	}

	/**
	 * Step 1. Construct the graph by maintaining an EdgeStructClass
	 * @param fin: training data file
	 * @return
	 * @throws IOException
	 */
	public EdgeStructClass ConstructGraph(dataStructure ds, List<ObsClass> obsList, Map<String, String> labelDic, ConstraintClass cc) {
		int nStates = labelDic.size();
		EdgeStructClass edgeStruct = new EdgeStructClass(ds.data.size(), nStates);

		Set<String> set = new HashSet<String>();

		List<String> consList = cc.constraints;
		for (int k = 0; k < consList.size(); k++) {
			String factorName = consList.get(k);
			if (!cc.ConsNodeOrEdgeIndex.get(factorName).equals("edge")) continue;
			if (!cc.ConsTypeIndex.get(factorName).equals("y")) continue;

			if (factorName.equals("Bio_Bio_Similar")) {
				Set<String> edges = Link_Bio_Bio_Similar(ds.data, obsList); 
				edgeStruct.MapEdgeToConstraints(edges, factorName);
				set.addAll(edges);
			}
			if (factorName.equals("Employment_Bio_ShareOrg")) {
				Set<String> edges = Link_Employment_Bio_ShareOrg(ds.data, obsList); 
				edgeStruct.MapEdgeToConstraints(edges, factorName);
				set.addAll(edges);
			}

			//System.out.println("Total adj size = " + set.size());
		}

		List<int[]> adj = ConvertSetIntoIntList(set);
		edgeStruct.MakeEdgeVE(adj);
		return edgeStruct;
	}

	public EdgeStructClass ConstructGraph(dataStructure ds, List<ObsClass> obsList, Map<String, String> labelDic, ConstraintClass cc, metaParaClass mpc, InferClass inferP) {
		int nStates = labelDic.size();
		EdgeStructClass edgeStruct = new EdgeStructClass(ds.data.size(), nStates);

		Set<String> set = new HashSet<String>();

		List<String> consList = cc.constraints;
		for (int k = 0; k < consList.size(); k++) {
			String factorName = consList.get(k);
			if (!cc.ConsNodeOrEdgeIndex.get(factorName).equals("edge")) continue;
			if (!cc.ConsTypeIndex.get(factorName).equals("y")) continue;

			if (factorName.equals("Bio_Bio_Similar")) {
				String state = "Bio";
				int stateID = Integer.valueOf(labelDic.get(state));
				Set<String> edges = Link_Bio_Bio_Similar(ds.data, obsList, stateID, mpc.eps.get("Bio_Bio_Similar"), inferP); 
				edgeStruct.MapEdgeToConstraints(edges, factorName);
				set.addAll(edges);
			}
			if (factorName.equals("Employment_Bio_ShareOrg")) {
				String state1 = "Employment";
				int stateID1 = Integer.valueOf(labelDic.get(state1));
				String state2 = "Bio";
				int stateID2 = Integer.valueOf(labelDic.get(state2));
				Set<String> edges = Link_Employment_Bio_ShareOrg(ds.data, obsList, stateID1, stateID2, mpc.eps.get("Employment_Bio_ShareOrg"), inferP);
				edgeStruct.MapEdgeToConstraints(edges, factorName);
				set.addAll(edges);
			}

			//System.out.println("Total adj size = " + set.size());
		}

		List<int[]> adj = ConvertSetIntoIntList(set);
		edgeStruct.MakeEdgeVE(adj);
		return edgeStruct;
	}

	private List<int[]> ConvertSetIntoIntList(Set<String> set) {
		List<int[]> adj = new ArrayList<int[]>();
		for (String e : set) {
			String[] segs = e.split("_");
			int i = Integer.valueOf(segs[0]);
			int j = Integer.valueOf(segs[1]);
			int[] edge = new int[] {i, j};
			adj.add(edge);
		}
		return adj;
	}

	private Set<String> Link_Bio_Bio_Similar(List<LineClass2> data, List<ObsClass> obsList) {
		Set<String> adj = new HashSet<String>();
		int nNodes = data.size();
		for (int i = 0; i < nNodes; i++) {
			LineClass2 lc1 = data.get(i);
			ObsClass obs1 = obsList.get(i);
			// sufficiently long as Bio
			if (!obs1.HasNoMoreThanTenWords) {
				for (int j = i + 1; j < nNodes; j++) {
					LineClass2 lc2 = data.get(j);
					ObsClass obs2 = obsList.get(j);
					if (!obs2.HasNoMoreThanTenWords && !lc1.pageID.equals(lc2.pageID)) {
						String edge = i + "_" + j;
						adj.add(edge);
					}
				}
			}
		}

		return adj;
	}
	
	private Set<String> Link_Employment_Bio_ShareOrg(List<LineClass2> data, List<ObsClass> obsList) {
		Set<String> adj = new HashSet<String>();
		for (int i = 0; i < data.size() - 1; i++) {
			ObsClass obs1 = obsList.get(i);
			LineClass2 lc1 = data.get(i);
			for (int j = i + 1; j < data.size(); j++) {
				ObsClass obs2 = obsList.get(j);
				LineClass2 lc2 = data.get(j);
				String edge = i + "_" + j; 
				adj.add(edge);
			}
		}

		return adj;
	}

	private Set<String> Link_Bio_Bio_Similar(List<LineClass2> data, List<ObsClass> obsList, int stateID, double eps, InferClass inferP) {
		Set<String> adj = new HashSet<String>();
		int nNodes = data.size();
		for (int i = 0; i < nNodes; i++) {
			LineClass2 lc1 = data.get(i);
			ObsClass obs1 = obsList.get(i);
			// sufficiently long as Bio
			if (!obs1.HasNoMoreThanTenWords) {
				for (int j = i + 1; j < nNodes; j++) {
					LineClass2 lc2 = data.get(j);
					ObsClass obs2 = obsList.get(j);
					if (!obs2.HasNoMoreThanTenWords && !lc1.pageID.equals(lc2.pageID)) {
						if (Math.log(inferP.nodeBel[i][stateID]) +  Math.log(inferP.nodeBel[j][stateID]) >= eps) {
							String edge = i + "_" + j;
							adj.add(edge);
						}
					}
				}
			}
		}

		return adj;
	}

	private Set<String> Link_Employment_Bio_ShareOrg(List<LineClass2> data, List<ObsClass> obsList, int stateID1, int stateID2, double eps, InferClass inferP) {
		// stateID1 = Employment, stateID2 = Bio
		Set<String> adj = new HashSet<String>();
		for (int i = 0; i < data.size() - 1; i++) {
			ObsClass obs1 = obsList.get(i);
			LineClass2 lc1 = data.get(i);

			for (int j = i + 1; j < data.size(); j++) {
				ObsClass obs2 = obsList.get(j);
				LineClass2 lc2 = data.get(j);

				boolean IsOthers = (obs1.StartWithOtherEntityName & !obs1.HasEntityName) | obs1.StartSingularAndHasEntityName;
				boolean IsEdu = obs1.StartWithEducation | obs1.HasWordForEducation | obs1.PrecededByEducationOrgInBio;
				boolean EmploymentCondition = obs1.HasNoMoreThanTenWords & !IsOthers & !obs1.HasWordForAward & !obs1.IsBioCandidate2 & !IsEdu;
				boolean BioCondition = obs2.IsBioCandidate2;
				boolean case1 = EmploymentCondition & BioCondition;

				IsOthers = (obs2.StartWithOtherEntityName & !obs2.HasEntityName) | obs2.StartSingularAndHasEntityName;
				IsEdu = obs2.StartWithEducation | obs2.HasWordForEducation | obs2.PrecededByEducationOrgInBio;
				EmploymentCondition = obs2.HasNoMoreThanTenWords & !IsOthers & !obs2.HasWordForAward & !obs2.IsBioCandidate2 & !IsEdu;
				BioCondition = obs1.IsBioCandidate2;
				boolean case2 = EmploymentCondition & BioCondition;

				if (case1) {
					if (Math.log(inferP.nodeBel[i][stateID1]) +  Math.log(inferP.nodeBel[j][stateID2]) >= eps) {
						String edge = i + "_" + j;
						adj.add(edge);
					} 
				} else if (case2) {
					if (Math.log(inferP.nodeBel[i][stateID2]) +  Math.log(inferP.nodeBel[j][stateID1]) >= eps) {
						String edge = i + "_" + j;
						adj.add(edge);
					}
				}
			}
		}

		return adj;
	}
}
