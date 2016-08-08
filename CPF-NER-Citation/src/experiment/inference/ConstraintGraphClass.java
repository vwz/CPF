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

			if (factorName.equals("title_title_Continuous")) {
				Set<String> edges = Link_title_title_Continuous(ds.data, obsList); 
				edgeStruct.MapEdgeToConstraints(edges, factorName);
				set.addAll(edges);
			}
			else if (factorName.equals("booktitle_title_CloseAfter")) {
				Set<String> edges = Link_booktitle_title_CloseAfter(ds.data, obsList); 
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

			if (factorName.equals("title_title_Continuous")) {
				String state = "title";
				int stateID = Integer.valueOf(labelDic.get(state));
				Set<String> edges = Link_title_title_Continuous(ds.data, obsList, stateID, mpc.eps.get("title_title_Continuous"), inferP); 
				edgeStruct.MapEdgeToConstraints(edges, factorName);
				set.addAll(edges);
			}
			else if (factorName.equals("booktitle_title_CloseAfter")) {
				String state1 = "booktitle";
				int stateID1 = Integer.valueOf(labelDic.get(state1));
				String state2 = "title";
				int stateID2 = Integer.valueOf(labelDic.get(state2));
				Set<String> edges = Link_booktitle_title_CloseAfter(ds.data, obsList, stateID1, stateID2, mpc.eps.get("booktitle_title_CloseAfter"), inferP);
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

	/**
	 * 
	 * @param data
	 * @param obsList
	 * @return
	 */
	private Set<String> Link_title_title_Continuous(List<LineClass2> data, List<ObsClass> obsList) {
		Set<String> adj = new HashSet<String>();
		int nNodes = data.size();
		for (int i = 0; i < nNodes; i++) {
			for (int j = i + 1; j < nNodes; j++) {
				String edge = i + "_" + j;
				adj.add(edge);
			}
		}

		return adj;
	}
	
	private Set<String> Link_title_title_Continuous(List<LineClass2> data, List<ObsClass> obsList, int stateID, double eps, InferClass inferP) {
		Set<String> adj = new HashSet<String>();
		int nNodes = data.size();
		for (int i = 0; i < nNodes; i++) {
			for (int j = i + 1; j < nNodes; j++) {
				if (Math.log(inferP.nodeBel[i][stateID] + Math.log(inferP.nodeBel[j][stateID])) >= eps) {
					String edge = i + "_" + j;
					adj.add(edge);
				}
			}
		}

		return adj;
	}
	
	/**
	 * 
	 * @param data
	 * @param obsList
	 * @return
	 */
	private Set<String> Link_booktitle_title_CloseAfter(List<LineClass2> data, List<ObsClass> obsList) {
		Set<String> adj = new HashSet<String>();
		for (int i = 0; i < data.size() - 1; i++) {
			for (int j = i + 1; j < data.size(); j++) {
				String edge = i + "_" + j; 
				adj.add(edge);
			}
		}

		return adj;
	}
	
	private Set<String> Link_booktitle_title_CloseAfter(List<LineClass2> data, List<ObsClass> obsList, int stateID1, int stateID2, double eps, InferClass inferP) {
		Set<String> adj = new HashSet<String>();
		int nNodes = data.size();
		for (int i = 0; i < nNodes; i++) {
			for (int j = i + 1; j < nNodes; j++) {
				if (Math.log(inferP.nodeBel[i][stateID1] + Math.log(inferP.nodeBel[j][stateID2])) >= eps) {
					String edge = i + "_" + j; // title_booktitle
					adj.add(edge);
				} else if (Math.log(inferP.nodeBel[i][stateID2] + Math.log(inferP.nodeBel[j][stateID1])) >= eps) {
					String edge = i + "_" + j; // booktitle_title
					adj.add(edge);
				}
			}
		}

		return adj;
	}
}