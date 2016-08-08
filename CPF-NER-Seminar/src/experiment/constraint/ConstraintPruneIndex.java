package experiment.constraint;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import experiment.graph.dataStructure;
import experiment.inference.InferClass;
import experiment.parameter.metaParaClass;

public class ConstraintPruneIndex {
	public Map<String, Set<String>> nodeMap;
	public Map<String, Set<String>> edgeMap;
	
	public ConstraintPruneIndex() {
		this.nodeMap = new HashMap<String, Set<String>>();
		this.edgeMap = new HashMap<String, Set<String>>();
	}
	
	public void UpdateNodes(dataStructure ds, ConstraintClass cc, Map<String, String> labelDic, metaParaClass mpc, InferClass inferP) {
		List<String> consList = cc.constraints;
		
		int nNodes = ds.data.size();
		for (int j = 0; j < nNodes; j++) {
			String nodeID = String.valueOf(j);
			
			for (Entry<String, String> label : labelDic.entrySet()) {
				String yprop = label.getKey();
				int stateID = Integer.valueOf(label.getValue());

				for (int k = 0; k < consList.size(); k++) {
					String factorName = consList.get(k);
					if (!cc.ConsNodeOrEdgeIndex.get(factorName).equals("node")) continue;
					if (!cc.ConsTypeIndex.get(factorName).equals("y")) continue;

					// do filtering
					Set<String> labels = new HashSet<String>(cc.ConsLabelIndex.get(factorName));
					if (!labels.contains(yprop)) continue;
					
					double p = inferP.nodeBel[j][stateID];
					if (Math.log(p) < mpc.eps.get(factorName)) continue;
					
					UpdateNodeMap(factorName, nodeID);
				}
			}
		}
	}
	
	public void UpdateEdges() {
		
	}
	
	private void UpdateNodeMap(String factorName, String nodeID) {
		if (!nodeMap.containsKey(factorName)) {
			Set<String> set = new HashSet<String>();
			nodeMap.put(factorName, set);
		}
		nodeMap.get(factorName).add(nodeID);
	}
	
	private void UpdateEdgeMap(String factorName, String edgeID) {
		if (!edgeMap.containsKey(factorName)) {
			Set<String> set = new HashSet<String>();
			edgeMap.put(factorName, set);
		}
		edgeMap.get(factorName).add(edgeID);
	}
}