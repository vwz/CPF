package experiment.inference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EdgeStructClass {
	public int nStates;
	public int nNodes;
	public int nEdges;
	public int[] V; // V[i] is the sum of the number of edges connected to nodes 0 ~ (i-1)
	public int[] E; // E = [ [indexes of edges connected to node 1] [indexes of edges connected to node 2] ... [indexes of edges connected to node nNodes]]
	public List<int[]> edgeEnds; // each record in edgeEnds is a vector of int[2], including node1.id and node2.id (node1.id < node2.id)
	public Map<Integer, List<int[]>> blanket; // each node has its Markov blanket, which consists of multiple edges 
	public Map<String, Integer> IndexEdgeStruct; // entityID - edgeStruct's edge index in mergedEdgeStruct
	public Map<String, List<String>> EdgeToConstraints; // entityID - the constraint it refers to
	
	EdgeStructClass(int nNodes, int nStates) {
		this.nStates = nStates;
		this.nNodes = nNodes;
		this.EdgeToConstraints = new HashMap<String, List<String>>();
	}
	
	public void MapEdgeToConstraints(Set<String> edges, String factorName) {
		for (String edgeID : edges) {
			if (!EdgeToConstraints.containsKey(edgeID)) {
				List<String> list = new ArrayList<String>();
				EdgeToConstraints.put(edgeID, list);
			}
			EdgeToConstraints.get(edgeID).add(factorName);
		}
	}
	
	/**
	 * Construct the graph G = (V,E)
	 * @param edgeEnds: edgeEnds has the content of <node1.id, node2.id, edge.samePath>
	 */
	public void MakeEdgeVE(List<int[]> adj) {
		this.nEdges = adj.size();
		edgeEnds = new ArrayList<int[]>(adj); // assume each record in adj has the sorted (ascending) node indexes
		V = new int[nNodes + 1]; // definition is given above
		E = new int[2 * nEdges]; // definition is given above
		
		int[] nNei = new int[nNodes];
		Arrays.fill(nNei, 0);
		
		List[] nei = new ArrayList[nNodes]; // nei[i]: the edge ID list for node i
		for (int i = 0; i < nNodes; i++) {
			nei[i] = new ArrayList<Integer>();
		}
		
		for (int i = 0; i < nEdges; i++) {
			int n1 = edgeEnds.get(i)[0];
			int n2 = edgeEnds.get(i)[1];
			nNei[n1]++;
			nNei[n2]++;
			nei[n1].add(i);
			nei[n2].add(i);
		}
		
		int edge = 0;
		for (int i = 0; i < nNodes; i++) {
			V[i] = edge;
			Collections.sort(nei[i]); // sort the edge ID list in ascending order for node i
			for (int j = 0; j < nei[i].size(); j++) 
				E[edge + j] = ((Integer) nei[i].get(j)).intValue();
			edge += nei[i].size();
		}
		V[nNodes] = edge;
		
		// Added to maintain a hash map for each node's Markov blanket, key=nodeID, value=edges
		this.blanket = new HashMap<Integer, List<int[]>>();
		for (int i = 0; i < nEdges; i++) {
			int n1 = edgeEnds.get(i)[0];
			int n2 = edgeEnds.get(i)[1];
			if (!blanket.containsKey(n1))
				blanket.put(n1, new ArrayList<int[]>());
			if (!blanket.containsKey(n2))
				blanket.put(n2, new ArrayList<int[]>());
			List<int[]> list1 = blanket.get(n1);
			list1.add(edgeEnds.get(i));
			blanket.put(n1, list1);
			List<int[]> list2 = blanket.get(n2);
			list2.add(edgeEnds.get(i));
			blanket.put(n2, list2);
		}
		
		// Added to maintain an index for the edge ID
		this.IndexEdgeStruct = new HashMap<String, Integer>();
		for (int i = 0; i < nEdges; i++) {
			int[] edgei = edgeEnds.get(i);
			String edgeID = edgei[0] + "_" + edgei[1];
			IndexEdgeStruct.put(edgeID, i);
		}
	}
	
	/**
	 * This is to combine two EdgeStruct's: used for combining general features and constraint features
	 * @param edgeStruct
	 * @return
	 */
	public EdgeStructClass Merge(EdgeStructClass edgeStruct) {
		EdgeStructClass edgeStructAll = new EdgeStructClass(nNodes, nStates);
		Set<int[]> edges = new HashSet(this.edgeEnds);
		edges.addAll(edgeStruct.edgeEnds); // combine two sets of edges: this is possible because we require the edges stored in "edgeEnds" are in the format of <nodeID1, nodeID2> with nodeID1 < nodeID2. 
		List<int[]> adj = new ArrayList<int[]>();
		adj.addAll(edges);
		edgeStructAll.MakeEdgeVE(adj);
		return edgeStructAll;
	}
}

