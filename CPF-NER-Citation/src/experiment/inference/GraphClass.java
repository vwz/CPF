package experiment.inference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import experiment.graph.LineClass2;
import experiment.graph.dataStructure;

public class GraphClass {
	
	public GraphClass() { }

	/**
	 * Step 1. Construct the graph by maintaining an EdgeStructClass
	 * @param fin: training data file
	 * @return
	 * @throws IOException
	 */
	public EdgeStructClass ConstructGraph(dataStructure ds, Map<String, String> labelDic) {
		// Link data records in the same Table/List
		List<int[]> adj = GenSequence(ds.data);
		
		int nStates = labelDic.size();
		EdgeStructClass edgeStruct = new EdgeStructClass(ds.data.size(), nStates);
		edgeStruct.MakeEdgeVE(adj);
		return edgeStruct;
	}
	
	private List<int[]> GenSequence(List<LineClass2> data) {
		List<int[]> adj = new ArrayList<int[]>();
	
		for (int i = 1; i < data.size(); i++) {
			int j = i - 1;
			int[] edge = new int[]{j, i};
			adj.add(edge);
		}
		
		return adj;
	}
}
