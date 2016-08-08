package experiment.inference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MergedEdgeStructClass {
	public EdgeStructClass mergedEdgeStruct; // entityID - edgeStruct + edgeStructCons
	public Map<String, Integer> IndexEdgeStruct; // entityID - edgeStruct's edge index in mergedEdgeStruct
	public Map<String, Integer> IndexEdgeStructCons; // entityID - edgeStructCons's edge index in mergedEdgeStruct
	
	public MergedEdgeStructClass() { }
	
	public void Process(EdgeStructClass edgeStruct, EdgeStructClass edgeStructCons) {
		this.mergedEdgeStruct = Merge(edgeStruct, edgeStructCons);
		this.IndexEdgeStruct = BuildIndex(mergedEdgeStruct.edgeEnds, edgeStruct.edgeEnds);
		this.IndexEdgeStructCons = BuildIndex(mergedEdgeStruct.edgeEnds, edgeStructCons.edgeEnds);
	}
	
	/**
	 * This is to build the index between edgeStrct/edgeStructCons and mergedEdgeStruct
	 * @param adj_full: this is the full adj list from mergedEdgeStruct
	 * @param adj: this is the adj list from edgeStruct/edgeStructCons
	 * @return edgeID (String: constructed from int[] for HashMap) of edgeStruct/edgeStructCons - its ID in mergedEdgeStruct 
	 */
	private Map<String, Integer> BuildIndex(List<int[]> adj_full, List<int[]> adj) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		
		Map<String, Integer> full = new HashMap<String, Integer>();
		for (int i = 0; i < adj_full.size(); i++) {
			int[] edge = adj_full.get(i);
			String edgeID = edge[0] + "_" + edge[1];
			full.put(edgeID, i);
		}
		
		for (int i = 0; i < adj.size(); i++) {
			int[] edge = adj.get(i);
			String edgeID = edge[0] + "_" + edge[1];
			if (!full.containsKey(edgeID)) continue;
			map.put(edgeID, full.get(edgeID));
		}
		return map;
	}
	
	/**
	 * This is to merge edgeStruct and edgeStructCons
	 * @param edgeStruct
	 * @param edgeStructCons
	 */
	private EdgeStructClass Merge(EdgeStructClass edgeStruct, EdgeStructClass edgeStructCons) {
		int nNodes = edgeStruct.nNodes;
		int nStates = edgeStruct.nStates;
		EdgeStructClass mergedEdgeStruct = new EdgeStructClass(nNodes, nStates);
		
		// merge the adjacency list
		Set<String> setEdge = ConvertIntoStringSet(edgeStruct.edgeEnds);
		Set<String> setEdgeCons = ConvertIntoStringSet(edgeStructCons.edgeEnds);
		setEdge.addAll(setEdgeCons);
		List<int[]> adj = ConvertIntoIntArrayList(setEdge);
		
		mergedEdgeStruct.MakeEdgeVE(adj);
		return mergedEdgeStruct;
	}
	
	private Set<String> ConvertIntoStringSet(List<int[]> adj) {
		Set<String> set = new HashSet<String>();
		for (int i = 0; i < adj.size(); i++) {
			int[] edge = adj.get(i);
			String value = edge[0] + "_" + edge[1];
			if (!set.contains(value))
				set.add(value);
		}
		return set;
	}
	
	private List<int[]> ConvertIntoIntArrayList(Set<String> set) {
		List<int[]> adj = new ArrayList<int[]>();
		for (String e : set) {
			String[] segs = e.split("_");
			int[] edge = new int[] {Integer.valueOf(segs[0]), Integer.valueOf(segs[1])};
			adj.add(edge);
		}
		return adj;
	}
}
